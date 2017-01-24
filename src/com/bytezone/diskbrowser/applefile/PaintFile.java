package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class PaintFile extends HiResImage
{
  List<Block> blocks = new ArrayList<Block> ();

  public PaintFile (String name, byte[] buffer, int fileType, int auxType)
  {
    super (name, buffer, fileType, auxType);

    int ptr = 0;
    while (ptr < buffer.length)
    {
      int len = HexFormatter.unsignedLong (buffer, ptr);
      int nameLen = buffer[ptr + 4] & 0xFF;
      String kind = HexFormatter.getPascalString (buffer, ptr + 4);
      byte[] data = new byte[Math.min (len - (nameLen + 5), buffer.length - ptr)];
      System.arraycopy (buffer, ptr, data, 0, data.length);

      if ("MAIN".equals (kind))
        blocks.add (new Main (kind, data));
      else
        blocks.add (new Block (kind, data));

      ptr += len;
    }
  }

  @Override
  protected void createMonochromeImage ()
  {
  }

  @Override
  protected void createColourImage ()
  {
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

    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);

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

      text.append (" #   Mode         Packed Data\n");
      text.append ("---  ----  ---------------------------------------------");
      text.append ("------------------------------------------\n");
      for (int i = 0; i < scanLineDirectory.length; i++)
      {
        DirEntry dirEntry = scanLineDirectory[i];
        byte[] packedScanLine = packedScanLines[i];
        text.append (String.format ("%3d   %2d   ", i, dirEntry.mode));
        text.append (HexFormatter.getHexString (packedScanLine));
        text.append ("\n");
      }

      return text.toString ();
    }
  }

  class ColorTable
  {
    int id;
    ColorEntry[] entries = new ColorEntry[16];

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

    public ColorEntry (byte[] data, int offset)
    {
      value = HexFormatter.unsignedShort (data, offset);
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