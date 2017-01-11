package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class DoubleHiResImage extends HiResImage
{
  //  private static final int BLACK = 0x000000;
  //  private static final int MAGENTA = 0xFF00FF;
  //  private static final int BROWN = 0x994C00;
  //  private static final int ORANGE = 0xFF9933;
  //  private static final int DARK_GREEN = 0x006600;
  //  private static final int GRAY = 0xA0A0A0;
  //  private static final int GREEN = 0x00CC00;
  //  private static final int YELLOW = 0xFFFF33;
  //  private static final int DARK_BLUE = 0x0066CC;
  //  private static final int PURPLE = 0xCC00CC;
  //  private static final int PINK = 0xFFCCE5;
  //  private static final int MEDIUM_BLUE = 0x3399FF;
  //  private static final int LIGHT_BLUE = 0x99CCFF;
  //  private static final int AQUA = 0x99FFFF;
  //  private static final int WHITE = 0xFFFFFF;
  //  private static int[] palette =
  //  private static int[] palette =
  //    { BLACK, MAGENTA, BROWN, ORANGE, DARK_GREEN, GRAY, GREEN, YELLOW, DARK_BLUE,
  //      PURPLE, GRAY, PINK, MEDIUM_BLUE, LIGHT_BLUE, AQUA, WHITE };

  private static final int BLACK = 0x000000;
  private static final int MAGENTA = 0xDD0033;
  private static final int BROWN = 0x885500;
  private static final int ORANGE = 0xFF6600;
  private static final int DARK_GREEN = 0x007722;
  private static final int GRAY1 = 0x555555;
  private static final int GREEN = 0x11DD00;
  private static final int YELLOW = 0xFFFF00;
  private static final int DARK_BLUE = 0x000099;
  private static final int PURPLE = 0xDD22DD;
  private static final int GRAY2 = 0xAAAAAA;
  private static final int PINK = 0xFF9988;
  private static final int MEDIUM_BLUE = 0x2222FF;
  private static final int LIGHT_BLUE = 0x66AAFF;
  private static final int AQUA = 0x44FF99;
  private static final int WHITE = 0xFFFFFF;

  //  private static int[] palette =
  //      { BLACK, MAGENTA, DARK_BLUE, PURPLE, DARK_GREEN, GRAY1, MEDIUM_BLUE, LIGHT_BLUE,
  //        BROWN, ORANGE, GRAY2, PINK, GREEN, YELLOW, AQUA, WHITE };

  //  private static final int BLACK = 0x000000;
  //  private static final int MAGENTA = 0x722640;
  //  private static final int DARK_BLUE = 0x40337F;
  //  private static final int PURPLE = 0xE434FE;
  //  private static final int DARK_GREEN = 0x0E5940;
  //  private static final int GRAY = 0x808080;
  //  private static final int MEDIUM_BLUE = 0x1B9AEF;
  //  private static final int LIGHT_BLUE = 0xBFB3FF;
  //  private static final int BROWN = 0x404C00;
  //  private static final int ORANGE = 0xE46501;
  //  private static final int PINK = 0xF1A6BF;
  //  private static final int GREEN = 0x1BCB01;
  //  private static final int YELLOW = 0xBFCC80;
  //  private static final int AQUA = 0x8DD9BF;
  //  private static final int WHITE = 0xFFFFFF;
  //
  //  private static int[] palette =
  //      { BLACK, MAGENTA, DARK_BLUE, PURPLE, DARK_GREEN, GRAY, MEDIUM_BLUE, LIGHT_BLUE,
  //        BROWN, ORANGE, GRAY, PINK, GREEN, YELLOW, AQUA, WHITE };

  private static int[][] palette2 =
      { { 0x000000, 0xDD0033, 0x885500, 0xFF6600, 0x007722, 0x555555, 0x11DD00, 0xFFFF00,
          0x000099, 0xDD22DD, 0xAAAAAA, 0xFF9988, 0x2222FF, 0x66AAFF, 0x44FF99,
          0xFFFFFF },
        {//
         rgb (0, 0, 0),         // black    
         rgb (157, 9, 102),     // red      
         rgb (42, 42, 229),     // dk blue  
         rgb (199, 52, 255),    // purple   
         rgb (0, 118, 26),      // dk green 
         rgb (128, 128, 128),   // gray     
         rgb (13, 161, 255),    // med blue 
         rgb (170, 170, 255),   // lt blue  
         rgb (85, 85, 0),       // brown    
         rgb (242, 94, 0),      // orange   
         rgb (192, 192, 192),   // grey     
         rgb (255, 137, 229),   // pink     
         rgb (56, 203, 0),      // lt green 
         rgb (213, 213, 26),    // yellow   
         rgb (98, 246, 153),    // aqua     
         rgb (255, 255, 255)    // white    
        }, //
        { 0x000000, 0x722640, 0x40337F, 0xE434FE, 0x0E5940, 0x808080, 0x1B9AEF, 0xBFB3FF,
          0x404C00, 0xE46501, 0x808080, 0xF1A6BF, 0x1BCB01, 0xBFCC80, 0x8DD9BF,
          0xFFFFFF } };

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

  private final byte[] auxBuffer;
  private DoubleScrunch doubleScrunch;
  byte[] packedBuffer;
  //  private final int paletteIndex = 2;

  public DoubleHiResImage (String name, byte[] buffer, byte[] auxBuffer)
  {
    super (name, buffer);

    this.auxBuffer = auxBuffer;
    createImage ();
  }

  public DoubleHiResImage (String name, byte[] buffer)
  {
    super (name, buffer);

    assert name.endsWith (".PAC") || name.endsWith ("A2FC");

    if (name.endsWith (".PAC"))
    {
      packedBuffer = buffer;
      doubleScrunch = new DoubleScrunch ();
      doubleScrunch.unscrunch (buffer);
      auxBuffer = doubleScrunch.memory[0];
      this.buffer = doubleScrunch.memory[1];
    }
    else if (name.endsWith (".A2FC"))
    {
      auxBuffer = new byte[0x2000];
      this.buffer = new byte[0x2000];
      System.arraycopy (buffer, 0, auxBuffer, 0, 0x2000);
      System.arraycopy (buffer, 0x2000, this.buffer, 0, 0x2000);
    }
    else
    {
      auxBuffer = null;
    }

    createImage ();
  }

  @Override
  protected void createMonochromeImage ()
  {
    // image will be doubled vertically
    image = new BufferedImage (560, 192 * 2, BufferedImage.TYPE_BYTE_GRAY);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
    int ndx = 0;

    for (int i = 0; i < 3; i++)
      for (int j = 0; j < 8; j++)
        for (int k = 0; k < 8; k++)
        {
          int base = i * 0x28 + j * 0x80 + k * 0x400;
          int max = Math.min (base + 40, buffer.length);
          for (int ptr = base; ptr < max; ptr += 2)
          {
            int value = auxBuffer[ptr] & 0x7F | ((buffer[ptr] & 0x7F) << 7)
                | ((auxBuffer[ptr + 1] & 0x7F) << 14) | ((buffer[ptr + 1] & 0x7F) << 21);
            for (int px = 0; px < 28; px++)
            {
              int val = (value >> px) & 0x01;
              int pixel = val == 0 ? 0 : 255;
              dataBuffer.setElem (ndx, pixel);
              dataBuffer.setElem (ndx + 560, pixel);  // repeat pixel one line on
              ++ndx;
            }
          }
          ndx += 560;                                 // skip past repeated line
        }
  }

  @Override
  protected void createColourImage ()
  {
    int paletteNdx = paletteIndex % palette2.length;

    // image will be doubled horizontally
    image = new BufferedImage (140 * 2, 192, BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
    int ndx = 0;

    for (int i = 0; i < 3; i++)
      for (int j = 0; j < 8; j++)
        for (int k = 0; k < 8; k++)
        {
          int base = i * 0x28 + j * 0x80 + k * 0x400;
          int max = Math.min (base + 40, buffer.length);

          for (int ptr = base; ptr < max; ptr += 2)
          {
            int value = auxBuffer[ptr] & 0x7F | ((buffer[ptr] & 0x7F) << 7)
                | ((auxBuffer[ptr + 1] & 0x7F) << 14) | ((buffer[ptr + 1] & 0x7F) << 21);
            for (int px = 0; px < 28; px += 4)
            {
              int val = (value >> px) & 0x0F;
              dataBuffer.setElem (ndx++, palette2[paletteNdx][val]);
              dataBuffer.setElem (ndx++, palette2[paletteNdx][val]);  // repeat pixel
            }
          }
        }
  }

  @Override
  public String getHexDump ()
  {
    StringBuilder text = new StringBuilder ();

    if (packedBuffer != null)
    {
      text.append ("Packed buffer:\n\n");
      text.append (HexFormatter.format (packedBuffer));
    }

    text.append ("\n\nAuxilliary buffer:\n\n");
    text.append (HexFormatter.format (auxBuffer));

    text.append ("\n\nPrimary buffer:\n\n");
    text.append (HexFormatter.format (buffer));

    return text.toString ();
  }

  private static int rgb (int red, int green, int blue)
  {
    return red << 16 | green << 8 | blue;
  }
}