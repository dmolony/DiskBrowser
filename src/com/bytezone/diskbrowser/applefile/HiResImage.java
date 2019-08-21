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
  //  File Type   Aux     Name
  //   $06 BIN                 isGif()               - OriginalHiResImage
  //   $06 BIN                 isPng()               - OriginalHiResImage
  //   $06 BIN          .BMP   isBmp()               - OriginalHiResImage
  //   $06 BIN          .AUX                         - DoubleHiResImage
  //   $06 BIN          .PAC                         - DoubleHiResImage
  //   $06 BIN          .A2FC                        - DoubleHiResImage
  //   $06 BIN   $2000  eof $4000                    - DoubleHiResImage
  //   $06 BIN   $1FFF  eof $1FF8/$1FFF/$2000/$4000  - OriginalHiResImage
  //   $06 BIN   $2000  eof $1FF8/$1FFF/$2000/$4000  - OriginalHiResImage
  //   $06 BIN   $4000  eof $1FF8/$1FFF/$2000/$4000  - OriginalHiResImage   (?)
  //   $06 BIN   $4000  eof $4000                    - DoubleHiResImage     (?)
  //   $06 BIN          .3200                        - SHRPictureFile2
  //   $06 BIN          .3201                        - SHRPictureFile2

  //   $08 FOT  <$4000  Apple II Graphics File       -       ???
  //   $08 FOT   $4000  Packed Hi-Res file           -       ???
  //   $08 FOT   $4001  Packed Double Hi-Res file    -       ???
  //   $08 FOT   $8066  Fadden Hi-res                - FaddenHiResImage

  // * $C0 PNT   $0000  Paintworks Packed Super Hi-Res           - SHRPictureFile2
  // * $C0 PNT   $0001  Packed IIGS Super Hi-Res Image           - SHRPictureFile2
  // * $C0 PNT   $0002  IIGS Super Hi-Res Picture File (APF)     - SHRPictureFile
  //   $C0 PNT   $0003  Packed IIGS QuickDraw II PICT File       - SHRPictureFile2 *
  // * $C0 PNT   $0004  Packed Super Hi-Res 3200 (Brooks) .3201  - SHRPictureFile2
  //   $C0 PNT   $1000
  //   $C0 PNT   $8000  Drawplus? Paintworks Gold?
  //   $C0 PNT   $8001  GTv background picture
  //   $C0 PNT   $8005  DreamGraphix document
  //   $C0 PNT   $8006  GIF

  // * $C1 PIC   $0000  IIGS Super Hi-Res Image                  - SHRPictureFile2
  //   $C1 PIC   $0001  IIGS QuickDraw II PICT File              - SHRPictureFile2 *
  // * $C1 PIC   $0002  Super Hi-Res 3200 (Brooks) .3200         - SHRPictureFile2
  //   $C1 PIC   $2000  ?
  //   $C1 PIC   $8001  Allison raw image
  //   $C1 PIC   $8002  Thunderscan
  //   $C1 PIC   $8003  DreamGraphix

  //   $C2 ANI          Paintworks animation
  //   $C3 PAL          Paintworks palette

  static PaletteFactory paletteFactory = new PaletteFactory ();

  static final byte[] pngHeader =
      { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

  static boolean colourQuirks;
  static boolean monochrome;

  int fileType;
  int auxType;
  int eof;

  int paletteIndex;
  String failureReason = "";

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
    if (failureReason.isEmpty ())
      if (isGif (buffer) || isPng (buffer) || isBmp (buffer) || isTiff (buffer))
        makeImage ();
      else if (monochrome)
        createMonochromeImage ();
      else
        createColourImage ();
  }

  abstract void createMonochromeImage ();

  abstract void createColourImage ();

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
   * Files of type $08 and any auxiliary type less than or equal to $3FFF contain a
   * standard Apple II graphics file in one of several modes. After determining that
   * the auxiliary type is not $4000 or $4001 (which have been defined for high-resolution
   * and double high-resolution pictures packed with the Apple IIGS PackBytes routine),
   * you can determine the mode of the file by examining byte +120 (+$78). The value of
   * this byte, which ranges from zero to seven, is interpreted as follows:
   *
     Mode                        Page 1    Page 2
     280 x 192 Black & White       0         4
     280 x 192 Limited Color       1         5
     560 x 192 Black & White       2         6
     140 x 192 Full Color          3         7
   */

  // SHR see - http://noboot.com/charlie/cb2e_p3.htm

  // also: https://groups.google.com/forum/#!topic/comp.sys.apple2/zYhZ5YdNNxQ

  @Override
  public String getText ()
  {
    String auxText = "";
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("Image File : %s%nFile type  : $%02X    %s%n", name,
        fileType, ProdosConstants.fileTypes[fileType]));

    switch (fileType)
    {
      case ProdosConstants.FILE_TYPE_FOT:          // 0x08
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
        else if (auxType == 0x8066)
          auxText = "Fadden Hi-Res File";
        else
          auxText = "Unknown aux: " + auxType;
        break;

      case ProdosConstants.FILE_TYPE_PNT:           // 0xC0
        switch (auxType)
        {
          case 0:
            auxText = "Paintworks Packed SHR Image";
            break;
          case 1:
            auxText = "Packed Super Hi-Res Image";
            break;
          case 2:
            auxText = "Super Hi-Res Image (Apple Preferred Format)";
            break;
          case 3:
            auxText = "Packed QuickDraw II PICT File";
            break;
          case 4:
            auxText = "Packed Super Hi-Res 3200 color image";
            break;
          default:
            auxText = "Unknown aux: " + auxType;
        }
        break;

      case ProdosConstants.FILE_TYPE_PIC:           // 0xC1
        switch (auxType)
        {
          case 0:
          case 0x2000:
            auxText = "Super Hi-res Screen Image";
            break;
          case 1:
            auxText = "QuickDraw PICT File";
            break;
          case 2:
            auxText = "Super Hi-Res 3200 color image";
            break;
          default:
            auxText = "Unknown aux: " + auxType;
        }
    }

    if (!auxText.isEmpty ())
      text.append (String.format ("Aux type   : $%04X  %s%n", auxType, auxText));

    text.append (String.format ("File size  : %,d%n", buffer.length));
    text.append (String.format ("EOF        : %,d%n", eof));
    if (!failureReason.isEmpty ())
      text.append (String.format ("Failure    : %s%n", failureReason));

    text.deleteCharAt (text.length () - 1);
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

  // this should call unpackLine()
  byte[] unpack (byte[] buffer) throws ArrayIndexOutOfBoundsException
  {
    // routine found here - http://kpreid.livejournal.com/4319.html

    byte[] newBuf = new byte[calculateBufferSize (buffer)];
    byte[] fourBuf = new byte[4];

    int ptr = 0, newPtr = 0;
    while (ptr < buffer.length)
    {
      int type = (buffer[ptr] & 0xC0) >> 6;         // 0-3
      int count = (buffer[ptr++] & 0x3F) + 1;       // 1-64

      switch (type)
      {
        case 0:                           // copy next 1-64 bytes as is
          count = Math.min (count, buffer.length - ptr);
          while (count-- != 0)
            newBuf[newPtr++] = buffer[ptr++];
          break;

        case 1:                          // repeat next byte 3/5/6/7 times
          byte b = buffer[ptr++];
          while (count-- != 0)
            newBuf[newPtr++] = b;
          break;

        case 2:                          // repeat next 4 bytes (count) times
          for (int i = 0; i < 4; i++)
            fourBuf[i] = buffer[ptr++];
          while (count-- != 0)
            for (int i = 0; i < 4; i++)
              newBuf[newPtr++] = fourBuf[i];
          break;

        case 3:                          // repeat next byte (4*count) times
          b = buffer[ptr++];
          count *= 4;
          while (count-- != 0)
            newBuf[newPtr++] = b;
          break;
      }
    }
    return newBuf;
  }

  // Super Hi-res IIGS (MAIN in $C0/02)
  int unpackLine (byte[] buffer, byte[] newBuf, int newPtr)
  {
    byte[] fourBuf = new byte[4];

    int ptr = 0;
    while (ptr < buffer.length)
    {
      int type = (buffer[ptr] & 0xC0) >> 6;         // 0-3
      int count = (buffer[ptr++] & 0x3F) + 1;       // 1-64

      if (ptr >= buffer.length)       // needed for NAGELxx
        break;

      switch (type)
      {
        case 0:
          while (count-- != 0)
            if (newPtr < newBuf.length && ptr < buffer.length)
              newBuf[newPtr++] = buffer[ptr++];
          break;

        case 1:
          byte b = buffer[ptr++];
          while (count-- != 0)
            if (newPtr < newBuf.length)
              newBuf[newPtr++] = b;
          break;

        case 2:
          for (int i = 0; i < 4; i++)
            if (ptr < buffer.length)
              fourBuf[i] = buffer[ptr++];
          while (count-- != 0)
            for (int i = 0; i < 4; i++)
              if (newPtr < newBuf.length)
                newBuf[newPtr++] = fourBuf[i];
          break;

        case 3:
          b = buffer[ptr++];
          count *= 4;
          while (count-- != 0)
            if (newPtr < newBuf.length)
              newBuf[newPtr++] = b;
          break;
      }
    }

    return newPtr;
  }

  private int calculateBufferSize (byte[] buffer)
  {
    int ptr = 0;
    int size = 0;
    while (ptr < buffer.length)
    {
      int type = (buffer[ptr] & 0xC0) >> 6;         // 0-3
      int count = (buffer[ptr++] & 0x3F) + 1;       // 1-64

      if (type == 0)
      {
        ptr += count;
        size += count;
      }
      else if (type == 1)
      {
        ptr++;
        size += count;
      }
      else if (type == 2)
      {
        ptr += 4;
        size += count * 4;
      }
      else
      {
        ptr++;
        size += count * 4;
      }
    }
    return size;
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

  // http://commandlinefanatic.com/cgi-bin/showarticle.cgi?article=art011
  // https://www.w3.org/Graphics/GIF/spec-gif89a.txt
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

  public static boolean isTiff (byte[] buffer)
  {
    if (buffer.length < 3)
      return false;
    String text = new String (buffer, 0, 2);
    if (!"II".equals (text) && !"MM".equals (text))
      return false;
    if (buffer[2] != 0x2A)
      return false;
    return true;
  }

  // http://www.daubnet.com/en/file-format-bmp
  public static boolean isBmp (byte[] buffer)
  {
    if (buffer.length < 26)
      return false;

    String text = new String (buffer, 0, 2);
    int size = HexFormatter.unsignedLong (buffer, 2);

    if (false)
    {
      int empty = HexFormatter.unsignedLong (buffer, 6);
      int offset = HexFormatter.unsignedLong (buffer, 10);
      int header = HexFormatter.unsignedLong (buffer, 14);
      int width = HexFormatter.unsignedLong (buffer, 18);
      int height = HexFormatter.unsignedLong (buffer, 22);

      System.out.println (buffer.length);
      System.out.println (size);
      System.out.println (empty);
      System.out.println (offset);
      System.out.println (header);
      System.out.println (width);
      System.out.println (height);
    }

    return text.equals ("BM") && size <= buffer.length;
  }

  public static boolean isAPP (byte[] buffer)
  {
    if (buffer.length < 4)
      return false;
    return buffer[0] == (byte) 0xC1 && buffer[1] == (byte) 0xD0
        && buffer[2] == (byte) 0xD0 && buffer[3] == 0;
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
        entries[i] = new ColorEntry ();
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

      text.append (String.format ("%02X", id));
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