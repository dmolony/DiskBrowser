package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class DoubleHiResImage extends HiResImage
{

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
    Palette palette = paletteFactory.getCurrentPalette ();
    int[] colours = palette.getColours ();

    // image will be doubled horizontally
    image = new BufferedImage (140 * 2, 192, BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
    int ndx = 0;
    int[] swap = { 0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15 };

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
              int val2 = swap[val];
              dataBuffer.setElem (ndx++, colours[val2]);
              dataBuffer.setElem (ndx++, colours[val2]);  // repeat pixel
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