package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public abstract class HiResImage extends AbstractFile
{
  protected static int[][] //
  palette = {
              // my preferences
              { 0x000000, // 0 black        A
                0xFF0000, // 1 red          C
                0xA52A2A, // 2 brown        E  (8)
                0xFFA500, // 3 orange       G  (9)
                0x008000, // 4 dark green   I
                0x808080, // 5 grey1        K
                0x90EE90, // 6 light green  M  (C)
                0xFFFF00, // 7 yellow       O  (D)
                0x00008B, // 8 dark blue    B  (2)
                0x800080, // 9 purple       D  (3)
                0xC0C0C0, // A grey2        F
                0xFFC0CB, // B pink         H
                0x00BFFF, // C med blue     J  (6)
                0x87CEFA, // D light blue   L  (7)
                0x00FFFF, // E aqua         N
                0xFFFFFF  // F white        P
              },
              // Virtual II
              { 0x000000, // 0 black
                0xDD0033, // 1 magenta
                0x885500, // 2 brown         (8)
                0xFF6600, // 3 orange        (9)
                0x007722, // 4 dark green
                0x555555, // 5 grey1
                0x11DD00, // 6 light green   (C)
                0xFFFF00, // 7 yellow        (D)
                0x000099, // 8 dark blue     (2)
                0xDD22DD, // 9 purple        (3)
                0xAAAAAA, // A grey2
                0xFF9988, // B pink
                0x2222FF, // C med blue      (6)
                0x66AAFF, // D light blue    (7)
                0x44FF99, // E aqua
                0xFFFFFF  // F white
              },
              // Bill Buckels
              { 0x000000, // 0 black    
                0x9D0966, // 1 red    
                0x555500, // 2 brown    
                0xF25E00, // 3 orange     
                0x00761A, // 4 dk green 
                0x808080, // 5 gray     
                0x38CB00, // 6 lt green 
                0xD5D51A, // 7 yellow   
                0x2A2AE5, // 8 dk blue  
                0xC734FF, // 9 purple   
                0xC0C0C0, // A grey     
                0xFF89E5, // B pink     
                0x0DA1FF, // C med blue 
                0xAAAAFF, // D lt blue  
                0x62F699, // E aqua     
                0xFFFFFF  // F white    
              },
              // Kegs (BB)
              { rgb (0, 0, 0),        // black    */
                rgb (221, 0, 51),     // red      */
                rgb (136, 85, 34),    // brown    */
                rgb (255, 102, 0),    // orange   */
                rgb (0, 119, 0),      // dk green */
                rgb (85, 85, 85),     // gray     */
                rgb (0, 221, 0),      // lt green */
                rgb (255, 255, 0),    // yellow   */
                rgb (0, 0, 153),      // dk blue  */
                rgb (221, 0, 221),    // purple   */
                rgb (170, 170, 170),  // grey     */
                rgb (255, 153, 136),  // pink     */
                rgb (34, 34, 255),    // med blue */
                rgb (102, 170, 255),  // lt blue  */
                rgb (0, 255, 153),    // aqua     */
                rgb (255, 255, 255)   // white
              },
              // Authentic (MP)
              { 0x000000, // black
                0xD00030, // magenta
                0x805000, // brown
                0xF06000, // orange
                0x007020, // dark green
                0x505050, // grey1
                0x10D000, // light green
                0xF0F000, // yellow
                0x000090, // dark blue
                0xD020D0, // purple
                0xA0A0A0, // grey2
                0xF09080, // pink
                0x2020F0, // med blue
                0x60A0F0, // light blue
                0x40F090, // aqua
                0xFFFFFF  // white
              },
              // Tweaked (MP)
              { 0x000000, // black
                0xD00030, // magenta
                0x805000, // brown
                0xFF8000, // orange
                0x008000, // dark green
                0x808080, // grey1
                0x00FF00, // light green
                0xFFFF00, // yellow
                0x000080, // dark blue
                0xFF00FF, // purple
                0xC0C0C0, // grey2
                0xFF9080, // pink
                0x0000FF, // med blue
                0x60A0FF, // light blue
                0x40FF90, // aqua
                0xFFFFFF  // white
              },
              // NTSC Corrected (MP)
              { 0x000000, // black
                0x901740, // magenta
                0x405400, // brown
                0xD06A1A, // orange
                0x006940, // dark green
                0x808080, // grey1
                0x2FBC1A, // light green
                0xBFD35A, // yellow
                0x402CA5, // dark blue
                0xD043E5, // purple
                0x808080, // grey2
                0xFF96BF, // pink
                0x2F95E5, // med blue
                0xBFABFF, // light blue
                0x6FE8BF, // aqua
                0xFFFFFF  // white
              }, };

  /*-
   *  Michael Pohoreski - The Apple II Forever Anthology
  
  @reference: Technote tn-iigs-063 “Master Color Values”
  Color Register Values
            Color      Reg  LR HR  DHR  Master  Authentic   Tweaked    NTSC     
            Name            #  #   #    Value                          Corrected
  -----------------------------------------------------------------------------
            Black       0   0  0,4 0    $0000   (00,00,00)  (00,00,00) 00,00,00 
  (Magenta) Deep Red    1   1      1    $0D03   (D0,00,30)  (D0,00,30) 90,17,40 
            Dark Blue   2   2      8    $0009   (00,00,90)  (00,00,80) 40,2C,A5 
   (Violet) Purple      3   3  2   9    $0D2D   (D0,20,D0)  (FF,00,FF) D0,43,E5 
            Dark Green  4   4      4    $0072   (00,70,20)  (00,80,00) 00,69,40 
   (Gray 1) Dark Gray   5   5      5    $0555   (50,50,50)  (80,80,80) 80,80,80 
     (Blue) Medium Blue 6   6  6   C    $022F   (20,20,F0)  (00,00,FF) 2F,95,E5 
     (Cyan) Light Blue  7   7      D    $06AF   (60,A0,F0)  (60,A0,FF) BF,AB,FF 
            Brown       8   8      2    $0850   (80,50,00)  (80,50,00) 40,54,00 
            Orange      9   9  5   3    $0F60   (F0,60,00)  (FF,80,00) D0,6A,1A 
   (Gray 2) Light Gray  A   A      A    $0AAA   (A0,A0,A0)  (C0,C0,C0) 80,80,80 
            Pink        B   B      B    $0F98   (F0,90,80)  (FF,90,80) FF,96,BF 
    (Green) Light Green C   C  1   6    $01D0   (10,D0,00)  (00,FF,00) 2F,BC,1A 
            Yellow      D   D      7    $0FF0   (F0,F0,00)  (FF,FF,00) BF,D3,5A 
     (Aqua) Aquamarine  E   E      E    $04F9   (40,F0,90)  (40,FF,90) 6F,E8,BF 
            White       F   F  3,7 F    $0FFF   (F0,F0,F0)  (FF,FF,FF) FF,FF,FF 
  Legend:
   LR: Lo-Res   HR: Hi-Res   DHR: Double Hi-Res 
   */
  private static final byte[] pngHeader =
      { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

  protected static boolean colourQuirks;
  protected static boolean monochrome;
  protected static int paletteIndex;

  protected int fileType;
  protected int auxType;
  protected byte[] unpackedBuffer;

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
        makeScreen2 (unpackedBuffer);
        System.out.println ("aux 1 - " + name);
      }

      if (auxType == 2)
      {
        System.out.println ("aux 2 - " + name);
      }
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

  public void cyclePalette ()
  {
    ++paletteIndex;
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

  protected void makeScreen2 (byte[] buffer)
  {
    //    System.out.println (HexFormatter.format (buffer, 32000, 640));
    //    for (int table = 0; table < 200; table++)
    //    {
    //      System.out.println (HexFormatter.format (buffer, ptr, 32));
    //      for (int color = 0; color < 16; color++)
    //      {
    //        int red = buffer[ptr++] & 0x0F;
    //        int green = (buffer[ptr] & 0xF0) >> 4;
    //        int blue = buffer[ptr++] & 0x0F;
    //        Color c = new Color (red, green, blue);
    //      }
    //    }

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
      int type = (buffer[ptr] & 0xC0) >> 6;
      int count = (buffer[ptr++] & 0x3F) + 1;

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

  // switch between lo-res and DHR indexing
  private void switchColours ()
  {
    int[] tbl = { 2, 3, 6, 7 };
    for (int i = 0; i < palette.length; i++)
      for (int j = 0; j < tbl.length; j++)
      {
        int c = tbl[j];
        int temp = palette[i][c];
        palette[i][c] = palette[i][c + 6];
        palette[i][c + 6] = temp;
      }
  }

  private static int rgb (int red, int green, int blue)
  {
    System.out.printf ("%3d %3d %3d = 0x%06X%n", red, green, blue,
        (red << 16 | green << 8 | blue));
    return red << 16 | green << 8 | blue;
  }
}