package com.bytezone.diskbrowser.wizardry;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

// -----------------------------------------------------------------------------------//
class Image extends AbstractImage
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  Image (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    //    if (buffer[0] == -61 && buffer[1] == -115)
    //      fixSlime (buffer);

    image = new BufferedImage (70, 50, BufferedImage.TYPE_BYTE_GRAY); // width/height
    DataBuffer db = image.getRaster ().getDataBuffer ();
    int element = 0;

    for (int j = 0; j < 500; j++)
    {
      int bits = buffer[j] & 0xFF;
      for (int m = 0; m < 7; m++)
      {
        if (bits == 0)
        {
          element += 7 - m;
          break;
        }

        if ((bits & 1) == 1)
          db.setElem (element, 255);

        bits >>= 1;
        element++;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void fixSlime (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < 208; i++)
      buffer[i] = 0;

    buffer[124] = -108;
    buffer[134] = -43;
    buffer[135] = -128;
    buffer[144] = -44;
    buffer[145] = -126;
    buffer[154] = -48;
    buffer[155] = -118;
    buffer[164] = -64;
    buffer[165] = -86;
    buffer[174] = -64;
    buffer[175] = -86;
    buffer[184] = -63;
    buffer[185] = -86;
    buffer[194] = -44;
    buffer[195] = -86;
    buffer[204] = -44;
    buffer[205] = -126;
  }
}