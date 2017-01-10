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

  private static int[] palette =
      { BLACK, MAGENTA, DARK_BLUE, PURPLE, DARK_GREEN, GRAY1, MEDIUM_BLUE, LIGHT_BLUE,
        BROWN, ORANGE, GRAY2, PINK, GREEN, YELLOW, AQUA, WHITE };

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

  private final byte[] auxBuffer;
  private DoubleScrunch doubleScrunch;
  byte[] packedBuffer;

  public DoubleHiResImage (String name, byte[] buffer, byte[] auxBuffer)
  {
    super (name, buffer);

    this.auxBuffer = auxBuffer;
    createImage ();
  }

  public DoubleHiResImage (String name, byte[] buffer)
  {
    super (name, buffer);

    assert name.endsWith (".PAC");

    packedBuffer = buffer;
    doubleScrunch = new DoubleScrunch ();
    doubleScrunch.unscrunch (buffer);
    auxBuffer = doubleScrunch.memory[0];
    this.buffer = doubleScrunch.memory[1];

    createImage ();
  }

  @Override
  protected void createMonochromeImage ()
  {
    // image will be doubled vertically
    image = new BufferedImage (560, 192 * 2, BufferedImage.TYPE_BYTE_GRAY);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
    int element = 0;

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
              dataBuffer.setElem (element, pixel);
              dataBuffer.setElem (element + 560, pixel);  // repeat pixel one line on
              ++element;
            }
          }
          element += 560;                                 // skip past repeated line
        }
  }

  @Override
  protected void createColourImage ()
  {
    // image will be doubled horizontally
    image = new BufferedImage (140 * 2, 192, BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
    int element = 0;

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
              dataBuffer.setElem (element++, palette[val]);
              dataBuffer.setElem (element++, palette[val]);     // repeat pixel
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
}