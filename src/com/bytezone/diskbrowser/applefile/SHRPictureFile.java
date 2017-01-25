package com.bytezone.diskbrowser.applefile;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class SHRPictureFile extends HiResImage
{
  List<Block> blocks = new ArrayList<Block> ();
  Main mainBlock;
  Multipal multipalBlock;

  public SHRPictureFile (String name, byte[] buffer, int fileType, int auxType, int eof)
  {
    super (name, buffer, fileType, auxType, eof);

    int ptr = 0;
    while (ptr < buffer.length)
    {
      int len = HexFormatter.unsignedLong (buffer, ptr);
      int nameLen = buffer[ptr + 4] & 0xFF;
      String kind = HexFormatter.getPascalString (buffer, ptr + 4);
      byte[] data = new byte[Math.min (len - (nameLen + 5), buffer.length - ptr)];
      System.arraycopy (buffer, ptr, data, 0, data.length);

      if ("MAIN".equals (kind))
      {
        mainBlock = new Main (kind, data);
        blocks.add (mainBlock);
      }
      else if ("MULTIPAL".equals (kind))
      {
        multipalBlock = new Multipal (kind, data);
        blocks.add (multipalBlock);
      }
      else
        blocks.add (new Block (kind, data));

      ptr += len;
    }
    createImage ();
  }

  @Override
  protected void createMonochromeImage ()
  {
    makeScreen (unpackedBuffer);
  }

  @Override
  protected void createColourImage ()
  {
    image = new BufferedImage (320, mainBlock.numScanLines, BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();

    int element = 0;
    int ptr = 0;
    for (int row = 0; row < mainBlock.numScanLines; row++)
    {
      DirEntry dirEntry = mainBlock.scanLineDirectory[row];
      int hi = dirEntry.mode & 0xFF00;
      int lo = dirEntry.mode & 0x00FF;

      ColorTable colorTable = multipalBlock != null ? multipalBlock.colorTables[row]
          : mainBlock.colorTables[lo & 0x0F];
      boolean fillMode = (lo & 0x20) != 0;

      if (fillMode)
        System.out.println ("fillmode " + fillMode);

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

  class Block
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

  class Multipal extends Block
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

  class Main extends Block
  {
    int masterMode;
    int pixelsPerScanLine;
    int numColorTables;
    ColorTable[] colorTables;
    int numScanLines;
    DirEntry[] scanLineDirectory;
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

    // Super Hi-res IIGS
    protected int unpackLine (byte[] buffer, byte[] newBuf, int newPtr)
    {
      byte[] fourBuf = new byte[4];

      int ptr = 0;
      while (ptr < buffer.length)
      {
        int type = (buffer[ptr] & 0xC0) >> 6;         // 0-3
        int count = (buffer[ptr++] & 0x3F) + 1;       // 1-64

        if (ptr >= buffer.length)
          break;

        switch (type)
        {
          case 0:
            while (count-- != 0)
              if (newPtr < unpackedBuffer.length && ptr < buffer.length)
                newBuf[newPtr++] = buffer[ptr++];
            break;

          case 1:
            byte b = buffer[ptr++];
            while (count-- != 0)
              if (newPtr < unpackedBuffer.length)
                newBuf[newPtr++] = b;
            break;

          case 2:
            for (int i = 0; i < 4; i++)
              if (ptr < buffer.length)
                fourBuf[i] = buffer[ptr++];
            while (count-- != 0)
              for (int i = 0; i < 4; i++)
                if (newPtr < unpackedBuffer.length)
                  newBuf[newPtr++] = fourBuf[i];
            break;

          case 3:
            b = buffer[ptr++];
            count *= 4;
            while (count-- != 0)
              if (newPtr < unpackedBuffer.length)
                newBuf[newPtr++] = b;
            break;
        }
      }

      return newPtr;
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

  class ColorTable
  {
    int id;
    ColorEntry[] entries = new ColorEntry[16];

    public ColorTable ()
    {
      // default empty table
      id = -1;
      for (int i = 0; i < 16; i++)
      {
        entries[i] = new ColorEntry ();
      }
    }

    public ColorTable (int id, byte[] data, int offset)
    {
      this.id = id;
      for (int i = 0; i < 16; i++)
      {
        entries[i] = new ColorEntry (data, offset);
        offset += 2;
      }
    }

    String toLine ()
    {

      StringBuilder text = new StringBuilder ();

      text.append (String.format (" %X", id));
      for (int i = 0; i < 16; i++)
        text.append (String.format ("  %04X", entries[i].value));

      return text.toString ();
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("%2d ColorTable%n", id));
      for (int i = 0; i < 8; i++)
        text.append (String.format ("  %2d: %04X", i, entries[i].value));
      text.append ("\n");
      for (int i = 8; i < 16; i++)
        text.append (String.format ("  %2d: %04X", i, entries[i].value));

      return text.toString ();
    }
  }

  class ColorEntry
  {
    int value;          // 0RGB
    Color color;

    public ColorEntry ()
    {
      // default empty entry
      value = 0;
      color = new Color (0, 0, 0);
    }

    public ColorEntry (byte[] data, int offset)
    {
      value = HexFormatter.unsignedShort (data, offset);

      int red = ((value >> 8) & 0x0f) * 17;
      int green = ((value >> 4) & 0x0f) * 17;
      int blue = (value & 0x0f) * 17;
      color = new Color (red, green, blue);
    }

    @Override
    public String toString ()
    {
      return String.format ("ColorEntry: %04X", value);
    }
  }

  class DirEntry
  {
    int numBytes;
    int mode;

    public DirEntry (byte[] data, int offset)
    {
      numBytes = HexFormatter.unsignedShort (data, offset);
      mode = HexFormatter.unsignedShort (data, offset + 2);
    }

    @Override
    public String toString ()
    {
      return String.format ("Bytes: %5d, mode: %02X", numBytes, mode);
    }
  }
}