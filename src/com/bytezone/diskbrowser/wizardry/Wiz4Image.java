package com.bytezone.diskbrowser.wizardry;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

// -----------------------------------------------------------------------------------//
public class Wiz4Image extends AbstractImage
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public Wiz4Image (String name, byte[] buffer, int rows, int cols)   // 5, 6
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    image = new BufferedImage (cols * 7, rows * 8, BufferedImage.TYPE_BYTE_GRAY);
    DataBuffer db = image.getRaster ().getDataBuffer ();
    int element = 0;

    int rowSize = cols * 8;
    for (int row = 0; row < rows; row++)
      for (int line = 0; line < 8; line++)
        for (int col = 0; col < cols; col++)
        {
          byte b = buffer[row * rowSize + col * 8 + line];
          for (int bit = 0; bit < 7; bit++)
          {
            if ((b & 0x01) == 0x01)
              db.setElem (element, 255);
            b >>>= 1;
            element++;
          }
        }
  }
}
