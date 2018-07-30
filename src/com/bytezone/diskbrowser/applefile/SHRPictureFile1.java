package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class SHRPictureFile1 extends HiResImage
{
  private final List<Block> blocks = new ArrayList<Block> ();
  private Main mainBlock;
  private Multipal multipalBlock;

  // 0xC0 aux = 2 - Apple IIGS Super Hi-Res Picture File
  public SHRPictureFile1 (String name, byte[] buffer, int fileType, int auxType, int eof)
  {
    super (name, buffer, fileType, auxType, eof);

    int ptr = 0;
    while (ptr < buffer.length)
    {
      int len = HexFormatter.unsignedLong (buffer, ptr);
      if (len == 0)
        break;

      String kind = HexFormatter.getPascalString (buffer, ptr + 4);
      byte[] data = new byte[Math.min (len, buffer.length - ptr)];
      System.arraycopy (buffer, ptr, data, 0, data.length);

      switch (kind)
      {
        case "MAIN":
          mainBlock = new Main (kind, data);
          blocks.add (mainBlock);
          break;

        case "MULTIPAL":
          multipalBlock = new Multipal (kind, data);
          blocks.add (multipalBlock);
          break;

        case "PALETTES":
          System.out.println ("PALETTES not written");
          blocks.add (new Block (kind, data));
          break;

        case "MASK":
          System.out.println ("MASK not written");
          blocks.add (new Block (kind, data));
          break;

        case "PATS":
          System.out.println ("PATS not written");
          blocks.add (new Block (kind, data));
          break;

        case "SCIB":
          System.out.println ("SCIB not written");
          blocks.add (new Block (kind, data));
          break;

        //        case "SuperConvert":
        //        case "NOTE":
        //        case "EOA ":                                  // DeluxePaint
        //        case "Platinum Paint":
        //          blocks.add (new Block (kind, data));
        //          break;

        default:
          blocks.add (new Block (kind, data));
          System.out.println ("Unknown block type: " + kind + " in " + name);
          break;
      }

      ptr += len;
    }
    createImage ();
  }

  @Override
  void createMonochromeImage ()
  {
    image = new BufferedImage (320, 200, BufferedImage.TYPE_BYTE_GRAY);
    DataBuffer db = image.getRaster ().getDataBuffer ();

    int element = 0;
    int ptr = 0;
    for (int row = 0; row < 200; row++)
      for (int col = 0; col < 160; col++)
      {
        int pix1 = (unpackedBuffer[ptr] & 0xF0) >> 4;
        int pix2 = unpackedBuffer[ptr] & 0x0F;
        if (pix1 > 0)
          db.setElem (element, 255);
        if (pix2 > 0)
          db.setElem (element + 1, 255);
        element += 2;
        ptr++;
      }
  }

  @Override
  void createColourImage ()
  {
    image = new BufferedImage (mainBlock.pixelsPerScanLine, mainBlock.numScanLines,
        BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();

    if (mainBlock.pixelsPerScanLine != 320)
      System.out.println ("Pixels per scanline: " + mainBlock.pixelsPerScanLine);

    int element = 0;
    int ptr = 0;
    for (int row = 0; row < mainBlock.numScanLines; row++)
    {
      DirEntry dirEntry = mainBlock.scanLineDirectory[row];
      int hi = dirEntry.mode & 0xFF00;      // always 0
      int lo = dirEntry.mode & 0x00FF;      // mode bit if hi == 0

      if (hi != 0)
        System.out.println ("hi not zero");

      ColorTable colorTable = multipalBlock != null ? multipalBlock.colorTables[row]
          : mainBlock.colorTables[lo & 0x0F];

      boolean fillMode = (lo & 0x20) != 0;
      if (fillMode)
        System.out.println ("fillmode " + fillMode);

      // 320 mode
      for (int col = 0; col < 160; col++)
      {
        int left = (unpackedBuffer[ptr] & 0xF0) >> 4;
        int right = unpackedBuffer[ptr] & 0x0F;

        dataBuffer.setElem (element++, colorTable.entries[left].color.getRGB ());
        dataBuffer.setElem (element++, colorTable.entries[right].color.getRGB ());

        ptr++;
      }
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder (super.getText ());

    for (Block block : blocks)
    {
      text.append (block);
      text.append ("\n\n");
    }

    if (blocks.size () > 0)
    {
      text.deleteCharAt (text.length () - 1);
      text.deleteCharAt (text.length () - 1);
    }

    return text.toString ();
  }

  private class Block
  {
    String kind;
    byte[] data;

    public Block (String kind, byte[] data)
    {
      this.kind = kind;
      this.data = data;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Kind ...... %s%n%n", kind));
      text.append (HexFormatter.format (data));

      return text.toString ();
    }
  }

  private class Multipal extends Block
  {
    int numPalettes;
    ColorTable[] colorTables;

    public Multipal (String kind, byte[] data)
    {
      super (kind, data);

      int ptr = 5 + kind.length ();
      numPalettes = HexFormatter.unsignedShort (data, ptr);

      ptr += 2;
      colorTables = new ColorTable[numPalettes];
      for (int i = 0; i < numPalettes; i++)
      {
        if (ptr < data.length - 32)
          colorTables[i] = new ColorTable (i, data, ptr);
        else
          colorTables[i] = new ColorTable ();      // default empty table
        ptr += 32;
      }
    }
  }

  private class Main extends Block
  {
    int masterMode;                     // 0 = Brooks, 0 = PNT 320 80 = PNT 640
    int pixelsPerScanLine;              // 320 or 640
    int numColorTables;                 // 1 = Brooks, 16 = Other (may be zero)
    ColorTable[] colorTables;           // [numColorTables]
    int numScanLines;                   // >0
    DirEntry[] scanLineDirectory;       // [numScanLines]
    byte[][] packedScanLines;

    public Main (String kind, byte[] data)
    {
      super (kind, data);

      int ptr = 5 + kind.length ();
      masterMode = HexFormatter.unsignedShort (data, ptr);
      pixelsPerScanLine = HexFormatter.unsignedShort (data, ptr + 2);
      numColorTables = HexFormatter.unsignedShort (data, ptr + 4);

      ptr += 6;
      colorTables = new ColorTable[numColorTables];
      for (int i = 0; i < numColorTables; i++)
      {
        colorTables[i] = new ColorTable (i, data, ptr);
        ptr += 32;
      }

      numScanLines = HexFormatter.unsignedShort (data, ptr);
      scanLineDirectory = new DirEntry[numScanLines];
      packedScanLines = new byte[numScanLines][];

      ptr += 2;
      for (int i = 0; i < numScanLines; i++)
      {
        DirEntry dirEntry = new DirEntry (data, ptr);
        scanLineDirectory[i] = dirEntry;
        packedScanLines[i] = new byte[dirEntry.numBytes];
        ptr += 4;
      }

      for (int i = 0; i < numScanLines; i++)
      {
        int len = scanLineDirectory[i].numBytes;
        if (ptr + len > data.length)
          break;

        System.arraycopy (data, ptr, packedScanLines[i], 0, len);
        ptr += len;
      }

      if (true)
      {
        unpackedBuffer = new byte[numScanLines * 160];
        ptr = 0;
        for (int line = 0; line < numScanLines; line++)
        {
          byte[] lineBuffer = packedScanLines[line];
          if (lineBuffer.length % 2 == 1 && isEmpty (lineBuffer))
          {
            System.out.println ("Odd number of bytes in empty buffer in " + name);
            break;
          }
          ptr = unpackLine (lineBuffer, unpackedBuffer, ptr);
        }
      }
    }

    private boolean isEmpty (byte[] buffer)
    {
      for (byte b : buffer)
        if (b != 0)
          return false;
      return true;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Kind ................. %s%n", kind));
      text.append (String.format ("MasterMode ........... %04X%n", masterMode));
      text.append (String.format ("PixelsPerScanLine .... %d%n", pixelsPerScanLine));
      text.append (String.format ("NumColorTables ....... %d%n", numColorTables));
      text.append (String.format ("NumScanLines ......... %d%n%n", numScanLines));

      text.append ("Color Tables\n");
      text.append ("------------\n\n");

      text.append (" # ");
      for (int i = 0; i < 16; i++)
        text.append (String.format ("  %02X  ", i));
      text.deleteCharAt (text.length () - 1);
      text.deleteCharAt (text.length () - 1);
      text.append ("\n---");
      for (int i = 0; i < 16; i++)
        text.append (" ---- ");
      text.deleteCharAt (text.length () - 1);
      text.append ("\n");

      for (ColorTable colorTable : colorTables)
      {
        text.append (colorTable.toLine ());
        text.append ("\n");
      }

      text.append ("\nScan Lines\n");
      text.append ("----------\n\n");

      text.append (" #   Mode  Len       Packed Data\n");
      text.append ("---  ----  ---   ---------------------------------------");
      text.append ("------------------------------------------\n");

      int lineSize = 24;
      for (int i = 0; i < scanLineDirectory.length; i++)
      {
        DirEntry dirEntry = scanLineDirectory[i];
        byte[] packedScanLine = packedScanLines[i];
        text.append (
            String.format ("%3d   %3d  %3d   ", i, dirEntry.mode, packedScanLine.length));
        int ptr = 0;
        while (true)
        {
          text.append (HexFormatter.getHexString (packedScanLine, ptr, lineSize));
          ptr += lineSize;
          if (ptr >= packedScanLine.length)
            break;
          text.append ("\n                 ");
        }
        text.append ("\n");
      }

      return text.toString ();
    }
  }
}