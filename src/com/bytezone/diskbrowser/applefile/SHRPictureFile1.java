package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class SHRPictureFile1 extends HiResImage
// -----------------------------------------------------------------------------------//
{
  private final List<Block> blocks = new ArrayList<> ();
  private Main mainBlock;
  private Multipal multipalBlock;
  private final boolean debug = false;

  // PNT - 0xC0/02 - Apple IIGS Super Hi-Res Picture File (APF)
  // ---------------------------------------------------------------------------------//
  public SHRPictureFile1 (String name, byte[] buffer, int fileType, int auxType, int eof)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer, fileType, auxType, eof);

    int ptr = 0;
    while (ptr < buffer.length)
    {
      int len = Utility.getLong (buffer, ptr);
      if (len == 0 || len > buffer.length)
      {
        System.out.printf ("Block length: %d%n", len);
        break;
      }

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
        case "816/Paint":
        case "SHRConvert":
          blocks.add (new Block (kind, data));
          break;

        case "Nseq":
          blocks.add (new Nseq (kind, data));
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

  // ---------------------------------------------------------------------------------//
  @Override
  void createMonochromeImage ()
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  @Override
  void createColourImage ()
  // ---------------------------------------------------------------------------------//
  {
    if (mainBlock == null)
    {
      System.out.println ("No MAIN block in image file");
      return;
    }

    boolean mode320 = (mainBlock.masterMode & 0x80) == 0;

    int imageWidth = mainBlock.pixelsPerScanLine;
    if (mode320)
      imageWidth *= 2;        // every horizontal pixel is drawn twice

    image = new BufferedImage (imageWidth, mainBlock.numScanLines * 2,
        BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();

    int element = 0;
    int ptr = 0;

    for (int line = 0; line < mainBlock.numScanLines; line++)
    {
      DirEntry dirEntry = mainBlock.scanLineDirectory[line];
      int hi = dirEntry.mode & 0xFF00;      // always 0
      int lo = dirEntry.mode & 0x00FF;      // mode bit if hi == 0

      boolean fillMode = (dirEntry.mode & 0x20) != 0;
      // assert fillMode == false;

      if (hi != 0)
        System.out.println ("hi not zero");

      ColorTable colorTable = //
          multipalBlock != null ? multipalBlock.colorTables[line]
              : mainBlock.colorTables[lo & 0x0F];

      int dataWidth = mainBlock.pixelsPerScanLine / (mode320 ? 2 : 4);

      if (mode320)       // two pixels per byte, each shown twice
        ptr = mode320Line (ptr, element, dataWidth, colorTable, dataBuffer, imageWidth);
      else              // four pixels per byte
        ptr = mode640Line (ptr, element, dataWidth, colorTable, dataBuffer, imageWidth);

      element += imageWidth * 2;        // drawing two lines at a time
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (super.getText ());

    if (mainBlock == null)
      text.append ("\nFailure    : No MAIN block\n");
    text.append ("\n\n");

    for (Block block : blocks)
    {
      text.append (block);
      text.append ("\n\n");
    }

    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private class Block
  // ---------------------------------------------------------------------------------//
  {
    String kind;
    byte[] data;
    int size;

    // -------------------------------------------------------------------------------//
    public Block (String kind, byte[] data)
    // -------------------------------------------------------------------------------//
    {
      this.kind = kind;
      this.data = data;
      size = Utility.getLong (data, 0);
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Block ..... %s%n", kind));
      text.append (String.format ("Size ...... %04X  %<d%n%n", size));

      int headerSize = 5 + kind.length ();
      text.append (HexFormatter.format (data, headerSize, data.length - headerSize));

      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private class Multipal extends Block
  // ---------------------------------------------------------------------------------//
  {
    int numColorTables;
    ColorTable[] colorTables;

    // -------------------------------------------------------------------------------//
    public Multipal (String kind, byte[] data)
    // -------------------------------------------------------------------------------//
    {
      super (kind, data);

      int ptr = 5 + kind.length ();
      numColorTables = Utility.getShort (data, ptr);

      ptr += 2;
      colorTables = new ColorTable[numColorTables];

      for (int i = 0; i < numColorTables; i++)
      {
        if (ptr < data.length - 32)
          colorTables[i] = new ColorTable (i, data, ptr);
        else
          colorTables[i] = new ColorTable (i, 0x00);      // default empty table !! not
                                                          // finished
        ptr += 32;
      }
    }

    // ---------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // ---------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Kind ................. %s%n", kind));
      text.append (String.format ("NumColorTables ....... %d%n%n", numColorTables));

      for (int line = 0; line < numColorTables; line++)
      {
        text.append (colorTables[line]);
        text.append ("\n\n");
      }

      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private class Main extends Block
  // ---------------------------------------------------------------------------------//
  {
    int masterMode;                     // 0 = Brooks, 0 = PNT 320 80 = PNT 640
    int pixelsPerScanLine;              // image width in pixels
    int numColorTables;                 // 1 = Brooks, 16 = Other (may be zero)
    ColorTable[] colorTables;           // [numColorTables]
    int numScanLines;                   // image height in pixels
    DirEntry[] scanLineDirectory;       // [numScanLines]
    byte[][] packedScanLines;
    boolean mode640;
    int dataWidth;

    public Main (String kind, byte[] data)
    {
      super (kind, data);

      int ptr = 5 + kind.length ();
      masterMode = Utility.getShort (data, ptr);
      pixelsPerScanLine = Utility.getShort (data, ptr + 2);
      numColorTables = Utility.getShort (data, ptr + 4);
      mode640 = (masterMode & 0x80) != 0;

      ptr += 6;
      colorTables = new ColorTable[numColorTables];
      for (int i = 0; i < numColorTables; i++)
      {
        colorTables[i] = new ColorTable (i, data, ptr);
        ptr += 32;
      }

      numScanLines = Utility.getShort (data, ptr);
      ptr += 2;

      scanLineDirectory = new DirEntry[numScanLines];
      packedScanLines = new byte[numScanLines][];

      for (int line = 0; line < numScanLines; line++)
      {
        DirEntry dirEntry = new DirEntry (data, ptr);
        scanLineDirectory[line] = dirEntry;
        packedScanLines[line] = new byte[dirEntry.numBytes];
        ptr += 4;
      }

      for (int line = 0; line < numScanLines; line++)
      {
        int numBytes = scanLineDirectory[line].numBytes;
        if (ptr + numBytes > data.length)
        {
          System.out.println ("breaking early");
          break;
        }

        System.arraycopy (data, ptr, packedScanLines[line], 0, numBytes);
        ptr += numBytes;
      }

      dataWidth = pixelsPerScanLine / (mode640 ? 4 : 2);

      byte[] unpackedBuffer = new byte[numScanLines * dataWidth];
      ptr = 0;
      for (int line = 0; line < numScanLines; line++)
      {
        // if (isOddAndEmpty (packedScanLines[line]))
        // {
        // System.out.println ("Odd number of bytes in empty buffer in " + name);
        // break;
        // }

        int bytesUnpacked = unpack (packedScanLines[line], 0,
            packedScanLines[line].length, unpackedBuffer, ptr);

        if (bytesUnpacked != dataWidth && false)
          System.out.printf ("Unexpected line width %3d  %5d  %3d  %3d%n", line, ptr,
              bytesUnpacked, dataWidth);

        ptr += dataWidth;
      }

      SHRPictureFile1.this.buffer = unpackedBuffer;
    }

    // -------------------------------------------------------------------------------//
    // private boolean isOddAndEmpty (byte[] buffer)
    // //
    // -------------------------------------------------------------------------------//
    // {
    // if (buffer.length % 2 == 0)
    // return false;
    // for (byte b : buffer)
    // if (b != 0)
    // return false;
    // return true;
    // }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Kind ................. %s%n", kind));
      text.append (String.format ("MasterMode ........... %04X%n", masterMode));
      text.append (String.format ("PixelsPerScanLine .... %d / %d = %d bytes%n",
          pixelsPerScanLine, (mode640 ? 4 : 2), dataWidth));
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
      text.append ("--------------------------------\n");

      int lineSize = 24;
      for (int i = 0; i < scanLineDirectory.length; i++)
      {
        DirEntry dirEntry = scanLineDirectory[i];
        byte[] packedScanLine = packedScanLines[i];
        text.append (
            String.format ("%3d  %04X  %3d   ", i, dirEntry.mode, packedScanLine.length));
        int ptr = 0;
        while (true)
        {
          String hex = HexFormatter.getHexString (packedScanLine, ptr, lineSize);
          text.append (hex);
          if (ptr == 0)
          {
            if (hex.length () < 71)
              text.append (("                                        "
                  + "                               ").substring (hex.length ()));
          }
          ptr += lineSize;
          if (ptr >= packedScanLine.length)
            break;
          text.append ("\n                 ");
        }
        text.append ("\n");

        if (true)
        {
          text.append ("\n");
          text.append (debug (packedScanLine, 0, packedScanLine.length));
          text.append ("\n");
        }
      }

      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private class Nseq extends Block
  // ---------------------------------------------------------------------------------//
  {
    // -------------------------------------------------------------------------------//
    public Nseq (String kind, byte[] data)
    // -------------------------------------------------------------------------------//
    {
      super (kind, data);
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Block ..... %s%n", kind));
      text.append (String.format ("Size ...... %04X  %<d%n%n", size));

      int ptr = 5 + kind.length ();
      while (ptr < data.length)
      {
        text.append (HexFormatter.format (data, ptr, 4) + "\n");
        ptr += 4;
      }

      text.deleteCharAt (text.length () - 1);

      return text.toString ();
    }
  }
}