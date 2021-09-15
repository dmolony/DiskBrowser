package com.bytezone.diskbrowser.applefile;

import java.awt.Color;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public abstract class HiResImage extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  static final String[] auxTypes =
      { "Paintworks Packed SHR Image", "Packed Super Hi-Res Image",
          "Super Hi-Res Image (Apple Preferred Format)", "Packed QuickDraw II PICT File",
          "Packed Super Hi-Res 3200 color image", "DreamGraphix" };
  static final int COLOR_TABLE_SIZE = 32;
  static final int COLOR_TABLE_OFFSET_AUX_0 = 32_256;
  static final int COLOR_TABLE_OFFSET_AUX_2 = 32_000;
  public static final int FADDEN_AUX = 0x8066;
  private byte[] fourBuf = new byte[4];
  private ColorTable defaultColorTable320 = new ColorTable (0, 0x00);
  private ColorTable defaultColorTable640 = new ColorTable (0, 0x80);

  // ---- ---- ------ -------------------------------------- ------------------------
  // File Type Aux Name Description
  // ---- ---- ------ -------------------------------------- ------------------------
  // $06 BIN isGif() OriginalHiResImage
  // $06 BIN isPng() OriginalHiResImage
  // $06 BIN .BMP isBmp() OriginalHiResImage
  // $06 BIN .AUX DoubleHiResImage
  // $06 BIN .PAC DoubleHiResImage
  // $06 BIN .A2FC DoubleHiResImage
  // $06 BIN $2000 eof $4000 DoubleHiResImage
  // $06 BIN $1FFF eof $1FF8/$1FFF/$2000/$4000 OriginalHiResImage
  // $06 BIN $2000 eof $1FF8/$1FFF/$2000/$4000 OriginalHiResImage
  // $06 BIN $4000 eof $1FF8/$1FFF/$2000/$4000 OriginalHiResImage (?)
  // $06 BIN $4000 eof $4000 DoubleHiResImage (?)
  // $06 BIN .3200 SHRPictureFile2
  // $06 BIN .3201 SHRPictureFile2 packed
  // ---- ---- ------ -------------------------------------- ------------------------
  // $08 FOT <$4000 Apple II Graphics File OriginalHiResImage
  // $08 FOT $4000 Packed Hi-Res file ???
  // $08 FOT $4001 Packed Double Hi-Res file ???
  // $08 FOT $8066 Fadden Hi-res FaddenHiResImage
  // ---- ---- ------ -------------------------------------- ------------------------
  // * $C0 PNT $0000 Paintworks Packed Super Hi-Res SHRPictureFile2
  // * $C0 PNT $0001 Packed IIGS Super Hi-Res Image SHRPictureFile2
  // * $C0 PNT $0002 IIGS Super Hi-Res Picture File (APF) SHRPictureFile1
  // $C0 PNT $0003 Packed IIGS QuickDraw II PICT File SHRPictureFile2 *
  // * $C0 PNT $0004 Packed Super Hi-Res 3200 (Brooks) SHRPictureFile2 .3201
  // $C0 PNT $1000
  // $C0 PNT $8000 Drawplus? Paintworks Gold?
  // $C0 PNT $8001 GTv background picture
  // $C0 PNT $8005 DreamGraphix document SHRPictureFile2
  // $C0 PNT $8006 GIF
  // ---- ---- ------ -------------------------------------- ------------------------
  // * $C1 PIC $0000 IIGS Super Hi-Res Image SHRPictureFile2
  // $C1 PIC $0001 IIGS QuickDraw II PICT File SHRPictureFile2 *
  // * $C1 PIC $0002 Super Hi-Res 3200 (Brooks) SHRPictureFile2 .3200
  // $C1 PIC $2000 = $C1/0000
  // $C1 PIC $4100 = $C1/0000
  // $C1 PIC $4950 = $C1/0000
  // $C1 PIC $8001 Allison raw image
  // $C1 PIC $8002 Thunderscan
  // $C1 PIC $8003 DreamGraphix
  // ---- ---- ------ -------------------------------------- ------------------------
  // $C2 ANI Paintworks animation
  // $C3 PAL Paintworks palette
  // ---- ---- ------ -------------------------------------- ------------------------

  // packed unpacked
  // $06.3200 1
  // $06.3201 .
  // $08 0000 .
  // $08 4000 .
  // $08 4001 .
  // $08 8066 3
  // $C0 0000 1
  // $C0 0001 $C1 0000 2 1
  // $C0 0002 1,5
  // $C0 0003 $C1 0001 . .
  // $C0 0004 $C1 0002 . 1
  // $C0 1000 .
  // $C0 8000 .
  // $C0 8001 .
  // $C0 8005 6
  // $C0 8006 .
  // $C1 0042 4
  // $C1 0043 4
  // $C1 2000 .
  // $C1 4100 1
  // $C1 4950 .
  // $C1 8001 .
  // $C1 8002 .
  // $C1 8003 .

  // 1 Graphics & Animation.2mg
  // 2 0603 Katie's Farm - Disk 2.po
  // 3 CompressedSlides.do
  // 4 System Addons.hdv
  // 5 gfx.po
  // 6 Dream Grafix v1.02.po

  // see also - https://docs.google.com/spreadsheets/d
  // . /1rKR6A_bVniSCtIP_rrv8QLWJdj4h6jEU1jJj0AebWwg/edit#gid=0
  // also - http://lukazi.blogspot.com/2017/03/double-high-resolution-graphics-dhgr.html

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

  // ---------------------------------------------------------------------------------//
  public HiResImage (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  public HiResImage (String name, byte[] buffer, int loadAddress)
  // ---------------------------------------------------------------------------------//
  {
    this (name, buffer, loadAddress, false);
  }

  // ---------------------------------------------------------------------------------//
  public HiResImage (String name, byte[] buffer, int loadAddress, boolean scrunched)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.loadAddress = loadAddress;         // for the disassembly listing

    if (scrunched)
      this.buffer = unscrunch (buffer);
  }

  // ---------------------------------------------------------------------------------//
  public HiResImage (String name, byte[] buffer, int fileType, int auxType, int eof)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.fileType = fileType;
    this.auxType = auxType;
    this.eof = eof;
  }

  // ---------------------------------------------------------------------------------//
  public boolean isAnimation ()
  // ---------------------------------------------------------------------------------//
  {
    return fileType == ProdosConstants.FILE_TYPE_ANI;
  }

  // ---------------------------------------------------------------------------------//
  protected void createImage ()
  // ---------------------------------------------------------------------------------//
  {
    if (!failureReason.isEmpty ())
      return;

    if (isGif (buffer) || isPng (buffer) || isBmp (buffer) || isTiff (buffer))
      makeImage ();
    else if (monochrome)
      createMonochromeImage ();
    else
      createColourImage ();
  }

  // ---------------------------------------------------------------------------------//
  abstract void createMonochromeImage ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  abstract void createColourImage ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  public void checkPalette ()
  // ---------------------------------------------------------------------------------//
  {
    if (!monochrome && paletteIndex != paletteFactory.getCurrentPaletteIndex ())
      createImage ();
  }

  // ---------------------------------------------------------------------------------//
  public void setPalette ()
  // ---------------------------------------------------------------------------------//
  {
    if (!monochrome)
      createImage ();
  }

  // ---------------------------------------------------------------------------------//
  public void setColourQuirks (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    if (colourQuirks == value)
      return;

    colourQuirks = value;

    if (!monochrome)
      createImage ();
  }

  // ---------------------------------------------------------------------------------//
  public void setMonochrome (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    if (monochrome == value)
      return;

    monochrome = value;
    createImage ();
  }

  // ---------------------------------------------------------------------------------//
  public static void setDefaultColourQuirks (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    colourQuirks = value;
  }

  // ---------------------------------------------------------------------------------//
  public static void setDefaultMonochrome (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    monochrome = value;
  }

  // byte +120 is the first screen hole
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

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
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
        else if (auxType == FADDEN_AUX)
          auxText = "Fadden Hi-Res File";
        else
          auxText = "Unknown aux: " + auxType;
        break;

      case ProdosConstants.FILE_TYPE_PNT:           // 0xC0
        if (auxType == 0x8005)
          auxText = auxTypes[5];
        else
          auxText = auxType > 4 ? "Unknown aux: " + auxType : auxTypes[auxType];
        break;

      case ProdosConstants.FILE_TYPE_PIC:           // 0xC1
        auxText = switch (auxType)
        {
          case 0, 0x2000, 0x0042, 0x0043 -> "Super Hi-res Screen Image";
          case 1 -> "QuickDraw PICT File";
          case 2 -> "Super Hi-Res 3200 color image";
          default -> "Unknown aux: " + auxType;
        };
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

  // ---------------------------------------------------------------------------------//
  int mode320Line (int ptr, int element, int dataWidth, ColorTable colorTable,
      DataBuffer dataBuffer, int imageWidth)
  // ---------------------------------------------------------------------------------//
  {
    if (colorTable == null)
      colorTable = defaultColorTable320;

    for (int i = 0; i < dataWidth; i++)
    {
      if (ptr >= buffer.length)
      {
        System.out.printf ("too big: %d  %d%n", ptr, buffer.length);
        return ptr;
      }
      // get two pixels from this byte
      int left = (buffer[ptr] & 0xF0) >>> 4;
      int right = buffer[ptr++] & 0x0F;

      // get pixel colors
      int rgbLeft = colorTable.entries[left].color.getRGB ();
      int rgbRight = colorTable.entries[right].color.getRGB ();

      // draw two pixels (twice each) on two lines
      draw (dataBuffer, element + imageWidth, rgbLeft, rgbLeft, rgbRight, rgbRight);
      element = draw (dataBuffer, element, rgbLeft, rgbLeft, rgbRight, rgbRight);
    }

    return ptr;
  }

  // ---------------------------------------------------------------------------------//
  int mode640Line (int ptr, int element, int dataWidth, ColorTable colorTable,
      DataBuffer dataBuffer, int imageWidth)
  // ---------------------------------------------------------------------------------//
  {
    if (colorTable == null)
      colorTable = defaultColorTable640;

    for (int i = 0; i < dataWidth; i++)
    {
      // get four pixels from this byte
      int p1 = (buffer[ptr] & 0xC0) >>> 6;
      int p2 = (buffer[ptr] & 0x30) >> 4;
      int p3 = (buffer[ptr] & 0x0C) >> 2;
      int p4 = (buffer[ptr++] & 0x03);

      // get pixel colors
      int rgb1 = colorTable.entries[p1 + 8].color.getRGB ();
      int rgb2 = colorTable.entries[p2 + 12].color.getRGB ();
      int rgb3 = colorTable.entries[p3].color.getRGB ();
      int rgb4 = colorTable.entries[p4 + 4].color.getRGB ();

      // draw four pixels on two lines
      draw (dataBuffer, element + imageWidth, rgb1, rgb2, rgb3, rgb4);    // 2nd line
      element = draw (dataBuffer, element, rgb1, rgb2, rgb3, rgb4);       // 1st line
    }

    return ptr;
  }

  // ---------------------------------------------------------------------------------//
  int draw (DataBuffer dataBuffer, int element, int... rgbList)
  // ---------------------------------------------------------------------------------//
  {
    if (dataBuffer.getSize () < rgbList.length + element)
    {
      System.out.printf ("Bollocks: %d %d %d%n", dataBuffer.getSize (), rgbList.length,
          element);
      return element;
    }

    for (int rgb : rgbList)
      dataBuffer.setElem (element++, rgb);

    return element;
  }

  // ---------------------------------------------------------------------------------//
  int unpack (byte[] buffer, int ptr, int max, byte[] newBuf, int newPtr)
  // ---------------------------------------------------------------------------------//
  {
    int savePtr = newPtr;

    while (ptr < max - 1)                 // minimum 2 bytes needed
    {
      int type = (buffer[ptr] & 0xC0) >>> 6;        // 0-3
      int count = (buffer[ptr++] & 0x3F) + 1;       // 1-64

      switch (type)
      {
        case 0:                                     // 2-65 bytes
          while (count-- != 0 && newPtr < newBuf.length && ptr < max)
            newBuf[newPtr++] = buffer[ptr++];
          break;

        case 1:                                     // 2 bytes
          byte b = buffer[ptr++];
          while (count-- != 0 && newPtr < newBuf.length)
            newBuf[newPtr++] = b;
          break;

        case 2:                                     // 5 bytes
          for (int i = 0; i < 4; i++)
            fourBuf[i] = ptr < max ? buffer[ptr++] : 0;

          while (count-- != 0)
            for (int i = 0; i < 4; i++)
              if (newPtr < newBuf.length)
                newBuf[newPtr++] = fourBuf[i];
          break;

        case 3:                                     // 2 bytes
          b = buffer[ptr++];
          count *= 4;
          while (count-- != 0 && newPtr < newBuf.length)
            newBuf[newPtr++] = b;
          break;
      }
    }

    return newPtr - savePtr;          // bytes unpacked
  }

  // ---------------------------------------------------------------------------------//
  String debug (byte[] buffer, int ptr, int length)
  // ---------------------------------------------------------------------------------//
  {
    int size = 0;
    int max = ptr + length;
    StringBuffer text = new StringBuffer ();

    while (ptr < max)
    {
      int type = (buffer[ptr] & 0xC0) >>> 6;        // 0-3
      int count = (buffer[ptr++] & 0x3F) + 1;       // 1-64

      text.append (String.format ("%04X/%04d: %02X  (%d,%2d)  ", ptr - 1, size,
          buffer[ptr - 1], type, count));

      if (type == 0)
      {
        text.append (
            String.format ("%s%n", HexFormatter.getHexString (buffer, ptr, count)));
        ptr += count;
        size += count;
      }
      else if (type == 1)
      {
        text.append (String.format ("%s%n", HexFormatter.getHexString (buffer, ptr, 1)));
        ptr++;
        size += count;
      }
      else if (type == 2)
      {
        text.append (String.format ("%s%n", HexFormatter.getHexString (buffer, ptr, 4)));
        ptr += 4;
        size += count * 4;
      }
      else
      {
        text.append (String.format ("%s%n", HexFormatter.getHexString (buffer, ptr, 1)));
        ptr++;
        size += count * 4;
      }
    }
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  int calculateBufferSize (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    // int ptr = 0;
    int size = 0;
    while (ptr < buffer.length)
    {
      int type = (buffer[ptr] & 0xC0) >>> 6;        // 0-3
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
  // ---------------------------------------------------------------------------------//
  private byte[] unscrunch (byte[] src)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  protected void makeImage ()
  // ---------------------------------------------------------------------------------//
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
  // ---------------------------------------------------------------------------------//
  public static boolean isGif (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    if (buffer.length < 6)
      return false;

    String text = new String (buffer, 0, 6);
    return text.equals ("GIF89a") || text.equals ("GIF87a");
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isPng (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    if (buffer.length < pngHeader.length)
      return false;

    for (int i = 0; i < pngHeader.length; i++)
      if (pngHeader[i] != buffer[i])
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isTiff (byte[] buffer)
  // ---------------------------------------------------------------------------------//
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
  // ---------------------------------------------------------------------------------//
  public static boolean isBmp (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    if (buffer.length < 26)
      return false;

    String text = new String (buffer, 0, 2);
    int size = Utility.getLong (buffer, 2);

    if (false)
    {
      int empty = Utility.getLong (buffer, 6);
      int offset = Utility.getLong (buffer, 10);
      int header = Utility.getLong (buffer, 14);
      int width = Utility.getLong (buffer, 18);
      int height = Utility.getLong (buffer, 22);

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

  // ---------------------------------------------------------------------------------//
  public static boolean isAPP (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    if (buffer.length < 4)
      return false;

    return buffer[0] == (byte) 0xC1 && buffer[1] == (byte) 0xD0
        && buffer[2] == (byte) 0xD0 && buffer[3] == 0;
  }

  // ---------------------------------------------------------------------------------//
  public static PaletteFactory getPaletteFactory ()
  // ---------------------------------------------------------------------------------//
  {
    return paletteFactory;
  }

  // ---------------------------------------------------------------------------------//
  public static List<Palette> getPalettes ()
  // ---------------------------------------------------------------------------------//
  {
    return paletteFactory.getPalettes ();
  }

  // ---------------------------------------------------------------------------------//
  class ColorTable
  // ---------------------------------------------------------------------------------//
  {
    private int id;
    ColorEntry[] entries = new ColorEntry[16];

    // -------------------------------------------------------------------------------//
    public ColorTable (int id, int mode)
    // -------------------------------------------------------------------------------//
    {
      // default empty table
      this.id = id;

      if ((mode & 0x80) == 0)
      {
        entries[0] = new ColorEntry (0x00, 0x00, 0x00);
        entries[1] = new ColorEntry (0x07, 0x07, 0x07);
        entries[2] = new ColorEntry (0x08, 0x04, 0x01);
        entries[3] = new ColorEntry (0x07, 0x02, 0x0C);
        entries[4] = new ColorEntry (0x00, 0x00, 0x0F);
        entries[5] = new ColorEntry (0x00, 0x08, 0x00);
        entries[6] = new ColorEntry (0x0F, 0x07, 0x00);
        entries[7] = new ColorEntry (0x0D, 0x00, 0x00);

        entries[8] = new ColorEntry (0x0F, 0x0A, 0x09);
        entries[9] = new ColorEntry (0x0F, 0x0F, 0x00);
        entries[10] = new ColorEntry (0x00, 0x0E, 0x00);
        entries[11] = new ColorEntry (0x04, 0x0D, 0x0F);
        entries[12] = new ColorEntry (0x0D, 0x0A, 0x0F);
        entries[13] = new ColorEntry (0x07, 0x08, 0x0F);
        entries[14] = new ColorEntry (0x0C, 0x0C, 0x0C);
        entries[15] = new ColorEntry (0x0F, 0x0F, 0x0F);
      }
      else
      {
        entries[0] = new ColorEntry (0x00, 0x00, 0x00);
        entries[1] = new ColorEntry (0x00, 0x00, 0x0F);
        entries[2] = new ColorEntry (0x0F, 0x0F, 0x00);
        entries[3] = new ColorEntry (0x0F, 0x0F, 0x0F);

        entries[4] = new ColorEntry (0x00, 0x00, 0x00);
        entries[5] = new ColorEntry (0x0D, 0x00, 0x00);
        entries[6] = new ColorEntry (0x00, 0x0E, 0x00);
        entries[7] = new ColorEntry (0x0F, 0x0F, 0x0F);

        entries[0] = new ColorEntry (0x00, 0x00, 0x00);
        entries[1] = new ColorEntry (0x00, 0x00, 0x0F);
        entries[2] = new ColorEntry (0x0F, 0x0F, 0x00);
        entries[3] = new ColorEntry (0x0F, 0x0F, 0x0F);

        entries[4] = new ColorEntry (0x00, 0x00, 0x00);
        entries[5] = new ColorEntry (0x0D, 0x00, 0x00);
        entries[6] = new ColorEntry (0x00, 0x0E, 0x00);
        entries[7] = new ColorEntry (0x0F, 0x0F, 0x0F);
      }
    }

    // -------------------------------------------------------------------------------//
    public ColorTable (int id, byte[] data, int offset)
    // -------------------------------------------------------------------------------//
    {
      this.id = id;
      for (int i = 0; i < 16; i++)
      {
        entries[i] = new ColorEntry (data, offset);
        offset += 2;
      }
    }

    // -------------------------------------------------------------------------------//
    String toLine ()
    // -------------------------------------------------------------------------------//
    {

      StringBuilder text = new StringBuilder ();

      text.append (String.format ("%02X", id));
      for (int i = 0; i < 16; i++)
        text.append (String.format ("  %04X", entries[i].value));

      return text.toString ();
    }

    // -------------------------------------------------------------------------------//
    void reverse ()
    // -------------------------------------------------------------------------------//
    {
      for (int i = 0; i < 8; i++)
      {
        ColorEntry temp = entries[i];
        entries[i] = entries[15 - i];
        entries[15 - i] = temp;
      }
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("%3d ColorTable%n", id));
      for (int i = 0; i < 8; i++)
        text.append (String.format ("  %2d: %04X", i, entries[i].value));
      text.append ("\n");
      for (int i = 8; i < 16; i++)
        text.append (String.format ("  %2d: %04X", i, entries[i].value));

      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  class ColorEntry
  // ---------------------------------------------------------------------------------//
  {
    int value;          // 0RGB
    Color color;

    // -------------------------------------------------------------------------------//
    public ColorEntry (int red, int green, int blue)
    // -------------------------------------------------------------------------------//
    {
      value = (red << 8) | (green << 4) | blue;
      color = new Color (red, green, blue);
    }

    // -------------------------------------------------------------------------------//
    public ColorEntry (byte[] data, int offset)
    // -------------------------------------------------------------------------------//
    {
      value = Utility.getShort (data, offset);

      int red = ((value >> 8) & 0x0f) * 17;
      int green = ((value >> 4) & 0x0f) * 17;
      int blue = (value & 0x0f) * 17;

      color = new Color (red, green, blue);
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      return String.format ("ColorEntry: %04X", value);
    }
  }

  // ---------------------------------------------------------------------------------//
  class DirEntry
  // ---------------------------------------------------------------------------------//
  {
    int numBytes;
    int mode;

    // -------------------------------------------------------------------------------//
    public DirEntry (byte[] data, int offset)
    // -------------------------------------------------------------------------------//
    {
      numBytes = Utility.getShort (data, offset);
      mode = Utility.getShort (data, offset + 2);
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      return String.format ("Bytes: %5d, mode: %02X", numBytes, mode);
    }
  }
}