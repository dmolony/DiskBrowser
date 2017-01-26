package com.bytezone.diskbrowser.applefile;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public abstract class HiResImage extends AbstractFile
{
  protected static PaletteFactory paletteFactory = new PaletteFactory ();

  private static final byte[] pngHeader =
      { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

  protected static boolean colourQuirks;
  protected static boolean monochrome;

  protected int fileType;
  protected int auxType;
  protected int eof;

  protected byte[] unpackedBuffer;
  protected int paletteIndex;

  public HiResImage (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  public HiResImage (String name, byte[] buffer, int loadAddress)
  {
    this (name, buffer, loadAddress, false);
  }

  public HiResImage (String name, byte[] buffer, int loadAddress, boolean scrunched)
  {
    super (name, buffer);

    this.loadAddress = loadAddress;         // for the disassembly listing

    if (scrunched)
      this.buffer = unscrunch (buffer);
  }

  public HiResImage (String name, byte[] buffer, int fileType, int auxType, int eof)
  {
    super (name, buffer);

    this.fileType = fileType;
    this.auxType = auxType;
    this.eof = eof;
  }

  protected void createImage ()
  {
    if (isGif (buffer) || isPng (buffer) || isBmp (buffer))
      makeImage ();
    else if (monochrome)
      createMonochromeImage ();
    else
      createColourImage ();
  }

  protected abstract void createMonochromeImage ();

  protected abstract void createColourImage ();

  public void checkPalette ()
  {
    if (!monochrome && paletteIndex != paletteFactory.getCurrentPaletteIndex ())
      createImage ();
  }

  public void setPalette ()
  {
    if (!monochrome)
      createImage ();
  }

  public void setColourQuirks (boolean value)
  {
    if (colourQuirks == value)
      return;

    colourQuirks = value;

    if (!monochrome)
      createImage ();
  }

  public void setMonochrome (boolean value)
  {
    if (monochrome == value)
      return;

    monochrome = value;
    createImage ();
  }

  public static void setDefaultColourQuirks (boolean value)
  {
    colourQuirks = value;
  }

  public static void setDefaultMonochrome (boolean value)
  {
    monochrome = value;
  }

  /*-
     Mode                        Page 1    Page 2
     280 x 192 Black & White       0         4
     280 x 192 Limited Color       1         5
     560 x 192 Black & White       2         6
     140 x 192 Full Color          3         7
   */

  // SHR see - http://noboot.com/charlie/cb2e_p3.htm

  @Override
  public String getText ()
  {
    String auxText = "";
    StringBuilder text = new StringBuilder ("Image File : " + name);
    text.append (String.format ("%nFile type  : $%02X    %s", fileType,
        ProdosConstants.fileTypes[fileType]));

    switch (fileType)
    {
      case ProdosConstants.FILE_TYPE_PICT:          // 0x08
        if (auxType < 0x4000)
        {
          auxText = "Apple II Graphics File";
          byte mode = buffer[0x78];                // 0-7
          System.out.println ("Prodos PICT, mode=" + mode);   // see mode table above
        }
        else if (auxType == 0x4000)
          auxText = "Packed Hi-Res File";
        else if (auxType == 0x4001)
          auxText = "Packed Double Hi-Res File";
        break;

      case ProdosConstants.FILE_TYPE_PNT:           // 0xC0
        if (auxType == 0)
          auxText = "Paintworks Packed SHR Image";
        else if (auxType == 1)
          auxText = "Packed Super Hi-Res Image";
        else if (auxType == 2)
          auxText = "Super Hi-Res Image (Apple Preferred)";
        else if (auxType == 3)
          auxText = "Packed QuickDraw II PICT File";
        break;

      case ProdosConstants.FILE_TYPE_PIC:           // 0xC1
        if (auxType == 0)
          auxText = "Super Hi-res Screen Image";
        else if (auxType == 1)
          auxText = "QuickDraw PICT File";
        else if (auxType == 2)
          auxText = "Super Hi-Res 3200 color image";
    }

    if (!auxText.isEmpty ())
      text.append (String.format ("%nAux type   : $%04X  %s", auxType, auxText));

    text.append (String.format ("%nFile size  : %,d", buffer.length));
    text.append (String.format ("%nEOF        : %,d", eof));
    if (unpackedBuffer != null)
    {
      text.append (String.format ("%nUnpacked   : %,d%n%n", unpackedBuffer.length));
      //          text.append (HexFormatter.format (unpackedBuffer));
    }

    return text.toString ();
  }

  /*
  * Unpack the Apple PackBytes format.
  *
  * Format is:
  *  <flag><data> ...
  *
  * Flag values (first 6 bits of flag byte):
  *  00xxxxxx: (0-63) 1 to 64 bytes follow, all different
  *  01xxxxxx: (0-63) 1 to 64 repeats of next byte
  *  10xxxxxx: (0-63) 1 to 64 repeats of next 4 bytes
  *  11xxxxxx: (0-63) 1 to 64 repeats of next byte taken as 4 bytes
  *              (as in 10xxxxxx case)
  */

  // Super Hi-res IIGS
  protected byte[] unpackBytes (byte[] buffer)
  {
    // routine found here - http://kpreid.livejournal.com/4319.html

    byte[] newBuf = new byte[32768];        // this might be wrong
    byte[] fourBuf = new byte[4];

    int ptr = 0, newPtr = 0;
    while (ptr < buffer.length)
    {
      int type = (buffer[ptr] & 0xC0) >> 6;         // 0-3
      int count = (buffer[ptr++] & 0x3F) + 1;       // 1-64

      switch (type)
      {
        case 0:
          while (count-- != 0)
            newBuf[newPtr++] = buffer[ptr++];
          break;

        case 1:
          byte b = buffer[ptr++];
          while (count-- != 0)
            newBuf[newPtr++] = b;
          break;

        case 2:
          for (int i = 0; i < 4; i++)
            fourBuf[i] = buffer[ptr++];
          while (count-- != 0)
            for (int i = 0; i < 4; i++)
              newBuf[newPtr++] = fourBuf[i];
          break;

        case 3:
          b = buffer[ptr++];
          count *= 4;
          while (count-- != 0)
            newBuf[newPtr++] = b;
          break;
      }
    }

    return newBuf;
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

  // Beagle Bros routine to expand a hi-res screen
  private byte[] unscrunch (byte[] src)
  {
    byte[] dst = new byte[0x2000];
    int p1 = 0;
    int p2 = 0;

    while (p1 < dst.length)
    {
      byte b = src[p2++];
      if ((b == (byte) 0x80) || (b == (byte) 0xFF))
      {
        b &= 0x7F;
        int rpt = src[p2++];
        for (int i = 0; i < rpt; i++)
          dst[p1++] = b;
      }
      else
        dst[p1++] = b;
    }
    return dst;
  }

  protected void makeImage ()
  {
    try
    {
      image = ImageIO.read (new ByteArrayInputStream (buffer));
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
    catch (IndexOutOfBoundsException e)     // some BMP files cause this
    {
      System.out.println ("Error in makeImage()");
      System.out.println (e.getMessage ());
    }
  }

  public static boolean isGif (byte[] buffer)
  {
    if (buffer.length < 6)
      return false;

    String text = new String (buffer, 0, 6);
    return text.equals ("GIF89a") || text.equals ("GIF87a");
  }

  public static boolean isPng (byte[] buffer)
  {
    if (buffer.length < pngHeader.length)
      return false;

    for (int i = 0; i < pngHeader.length; i++)
      if (pngHeader[i] != buffer[i])
        return false;

    return true;
  }

  public static boolean isBmp (byte[] buffer)
  {
    if (buffer.length < 2)
      return false;

    String text = new String (buffer, 0, 2);
    return text.equals ("BM");
  }

  public static PaletteFactory getPaletteFactory ()
  {
    return paletteFactory;
  }

  public static List<Palette> getPalettes ()
  {
    return paletteFactory.getPalettes ();
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

    void reverse ()
    {
      for (int i = 0; i < 8; i++)
      {
        ColorEntry temp = entries[i];
        entries[i] = entries[15 - i];
        entries[15 - i] = temp;
      }
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