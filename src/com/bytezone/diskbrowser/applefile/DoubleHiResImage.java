package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

public class DoubleHiResImage extends HiResImage
{
  private static final int BLACK = 0x000000;
  private static final int MAGENTA = 0xFF00FF;
  private static final int BROWN = 0x994C00;
  private static final int ORANGE = 0xFF9933;
  private static final int DARK_GREEN = 0x006600;
  private static final int GRAY = 0xA0A0A0;
  private static final int GREEN = 0x00CC00;
  private static final int YELLOW = 0xFFFF33;
  private static final int DARK_BLUE = 0x0066CC;
  private static final int PURPLE = 0xCC00CC;
  private static final int PINK = 0xFFCCE5;
  private static final int MEDIUM_BLUE = 0x3399FF;
  private static final int LIGHT_BLUE = 0x99CCFF;
  private static final int AQUA = 0x99FFFF;
  private static final int WHITE = 0xFFFFFF;

  private static int[] palette =
      { BLACK, MAGENTA, BROWN, ORANGE, DARK_GREEN, GRAY, GREEN, YELLOW, DARK_BLUE, PURPLE,
        GRAY, PINK, MEDIUM_BLUE, LIGHT_BLUE, AQUA, WHITE };

  private final byte[] auxBuffer;

  public DoubleHiResImage (String name, byte[] buffer, byte[] auxBuffer)
  {
    super (name, buffer);

    this.auxBuffer = auxBuffer;

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
}