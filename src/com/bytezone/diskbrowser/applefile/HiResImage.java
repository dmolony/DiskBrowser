package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
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

  public HiResImage (String name, byte[] buffer, int fileType, int auxType)
  {
    super (name, buffer);

    this.fileType = fileType;
    this.auxType = auxType;

    if (fileType == ProdosConstants.FILE_TYPE_PNT)
    {
      if (auxType == 1)
      {
        unpackedBuffer = unpackBytes (buffer);
        makeScreen (unpackedBuffer);
        System.out.println ("aux 1 - " + name);
      }

      //      if (auxType == 2)
      //      {
      //        System.out.println ("aux 2 - " + name);
      //      }
    }
  }

  protected void createImage ()
  {
    if (isGif (buffer) || isPng (buffer))
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
    text.append (String.format ("%nFile type  : $%02X", fileType));

    switch (fileType)
    {
      case ProdosConstants.FILE_TYPE_PICT:
        if (auxType < 0x4000)
        {
          auxText = "Graphics File";
          byte mode = buffer[0x78];                // 0-7
          System.out.println ("Prodos PICT, mode=" + mode);
        }
        else if (auxType == 0x4000)
          auxText = "Packed Hi-Res File";
        else if (auxType == 0x4001)
          auxText = "Packed Double Hi-Res File";
        break;

      case ProdosConstants.FILE_TYPE_PNT:
        if (auxType == 1)
        {
          if (unpackedBuffer == null)
            unpackedBuffer = unpackBytes (buffer);
          auxText = "Packed Super Hi-Res Image";
        }
        else if (auxType == 2)
          auxText = "Super Hi-Res Image";
        else if (auxType == 3)
          auxText = "Packed QuickDraw II PICT File";
        break;

      case ProdosConstants.FILE_TYPE_PIC:
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
    if (unpackedBuffer != null)
    {
      text.append (String.format ("%nUnpacked   : %,d%n%n", unpackedBuffer.length));
      text.append (HexFormatter.format (unpackedBuffer));
    }

    return text.toString ();
  }

  protected void makeScreen (byte[] buffer)
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

  // Super Hi-res IIGS
  protected byte[] unpackBytes (byte[] buffer)
  {
    // routine found here - http://kpreid.livejournal.com/4319.html

    byte[] newBuf = new byte[32768];
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

  public static PaletteFactory getPaletteFactory ()
  {
    return paletteFactory;
  }

  public static List<Palette> getPalettes ()
  {
    return paletteFactory.getPalettes ();
  }
}