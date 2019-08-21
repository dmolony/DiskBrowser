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
  private final boolean debug = false;

  // 0xC0/02 - Apple IIGS Super Hi-Res Picture File (APF)
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
        case "MASK":
        case "PATS":
        case "SCIB":
          if (debug)
            System.out.println (kind + " not written");
          blocks.add (new Block (kind, data));
          break;

        case "NOTE":                                  // Convert 3200
        case "SuperConvert":
        case "EOA ":                                  // DeluxePaint
        case "Platinum Paint":
        case "VSDV":
        case "VSMK":
          blocks.add (new Block (kind, data));
          break;

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
        int pix1 = (buffer[ptr] & 0xF0) >> 4;
        int pix2 = buffer[ptr] & 0x0F;
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
    int width = mainBlock.pixelsPerScanLine == 320 ? 640 : mainBlock.pixelsPerScanLine;
    image =
        new BufferedImage (width, mainBlock.numScanLines * 2, BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();

    int element1 = 0;         // first line
    int element2 = width;     // second line
    int ptr = 0;              // index into buffer

    for (int line = 0; line < mainBlock.numScanLines; line++)
    {
      DirEntry dirEntry = mainBlock.scanLineDirectory[line];
      int hi = dirEntry.mode & 0xFF00;      // always 0
      int lo = dirEntry.mode & 0x00FF;      // mode bit if hi == 0

      if (hi != 0)
        System.out.println ("hi not zero");

      ColorTable colorTable = multipalBlock != null ? multipalBlock.colorTables[line]
          : mainBlock.colorTables[lo & 0x0F];

      //      boolean fillMode = (lo & 0x20) != 0;
      //      if (fillMode)
      //        System.out.println ("fillmode " + fillMode);

      // 320 mode
      if (mainBlock.pixelsPerScanLine == 320)
      {
        for (int i = 0; i < 160; i++)       // two pixels per col
        {
          int left = (buffer[ptr] & 0xF0) >> 4;
          int right = buffer[ptr++] & 0x0F;

          // get left/right colors
          int rgbLeft = colorTable.entries[left].color.getRGB ();
          int rgbRight = colorTable.entries[right].color.getRGB ();

          // draw left/right pixels on current line
          dataBuffer.setElem (element1++, rgbLeft);
          dataBuffer.setElem (element1++, rgbLeft);
          dataBuffer.setElem (element1++, rgbRight);
          dataBuffer.setElem (element1++, rgbRight);

          // draw same left/right pixels on next line
          dataBuffer.setElem (element2++, rgbLeft);
          dataBuffer.setElem (element2++, rgbLeft);
          dataBuffer.setElem (element2++, rgbRight);
          dataBuffer.setElem (element2++, rgbRight);
        }
        element1 += width;        // skip line already drawn
        element2 += width;        // one line ahead
      }
      else
      {
        int max = mainBlock.pixelsPerScanLine / 4;
        for (int col = 0; col < max; col++)       // four pixels per col
        {
          int p1 = (buffer[ptr] & 0xC0) >> 6;
          int p2 = (buffer[ptr] & 0x30) >> 4;
          int p3 = (buffer[ptr] & 0x0C) >> 2;
          int p4 = (buffer[ptr++] & 0x03);

          // get pixel colors
          int rgb1 = colorTable.entries[p1 + 8].color.getRGB ();
          int rgb2 = colorTable.entries[p2 + 12].color.getRGB ();
          int rgb3 = colorTable.entries[p3].color.getRGB ();
          int rgb4 = colorTable.entries[p4 + 4].color.getRGB ();

          // draw pixels on current line
          dataBuffer.setElem (element1++, rgb1);
          dataBuffer.setElem (element1++, rgb2);
          dataBuffer.setElem (element1++, rgb3);
          dataBuffer.setElem (element1++, rgb4);

          // draw same pixels on next line
          dataBuffer.setElem (element2++, rgb1);
          dataBuffer.setElem (element2++, rgb2);
          dataBuffer.setElem (element2++, rgb3);
          dataBuffer.setElem (element2++, rgb4);
        }
        element1 += width;        // skip line already drawn
        element2 += width;        // one line ahead
      }
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder (super.getText ());
    text.append ("\n\n");

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

      //      System.out.printf ("mm %02X, pix %d%n", masterMode, pixelsPerScanLine);
      //      System.out.printf ("color tables: %d%n", numColorTables);

      ptr += 6;
      colorTables = new ColorTable[numColorTables];
      for (int i = 0; i < numColorTables; i++)
      {
        colorTables[i] = new ColorTable (i, data, ptr);
        ptr += 32;
      }

      numScanLines = HexFormatter.unsignedShort (data, ptr);
      ptr += 2;

      scanLineDirectory = new DirEntry[numScanLines];
      packedScanLines = new byte[numScanLines][];

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

      int width = pixelsPerScanLine == 320 ? 160 : pixelsPerScanLine / 4;
      byte[] unpackedBuffer = new byte[numScanLines * width];
      ptr = 0;
      for (int line = 0; line < numScanLines; line++)
      {
        if (isOddAndEmpty (packedScanLines[line]))
        {
          System.out.println ("Odd number of bytes in empty buffer in " + name);
          break;
        }

        ptr = unpackLine (packedScanLines[line], unpackedBuffer, ptr);

        // something strange happening here
        if (line == 102 && name.equals ("DRAGON.SHR"))
          ptr -= 132;
      }

      SHRPictureFile1.this.buffer = unpackedBuffer;
    }

    private boolean isOddAndEmpty (byte[] buffer)
    {
      if (buffer.length % 2 == 0)
        return false;
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