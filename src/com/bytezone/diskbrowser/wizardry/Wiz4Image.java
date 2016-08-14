package com.bytezone.diskbrowser.wizardry;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

public class Wiz4Image extends AbstractImage
{

  public Wiz4Image (String name, byte[] buffer)
  {
    super (name, buffer);

    image = new BufferedImage (42, 40, BufferedImage.TYPE_BYTE_GRAY); // width/height
    DataBuffer db = image.getRaster ().getDataBuffer ();
    int element = 0;

    //    System.out.println (HexFormatter.format (buffer));

    for (int row = 0; row < 5; row++)
    {
      for (int line = 0; line < 8; line++)
      {
        for (int col = 0; col < 6; col++)
        {
          int ptr = row * 48 + col * 8 + line;
          {
            byte b = buffer[ptr];
            for (int bit = 0; bit < 7; bit++)
            {
              if ((b & 0x01) == 0x01)
              {
                db.setElem (element, 255);
                //                System.out.print ("X");
              }
              //              else
              //                System.out.print (".");
              b >>>= 1;
              element++;
            }
          }
        }
        //        System.out.println ();
      }
    }
  }
}
