package com.bytezone.diskbrowser.applefile;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

public class PrintShopGraphic extends AbstractFile
{
  public PrintShopGraphic (String name, byte[] buffer)
  {
    super (name, buffer);

    image = new BufferedImage (88, 52, BufferedImage.TYPE_BYTE_GRAY);
    Graphics2D g2d = image.createGraphics ();
    g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) 1.0));
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
    int element = 0;

    for (int ptr = 0; ptr < 572; ptr++)
    {
        for (int px = 7; px >= 0; px--)
        {
          int val = (buffer[ptr] >> px) & 0x01;
          dataBuffer.setElem (element++, val == 0 ? 255 : 0);
        }
    }
    
    g2d.dispose ();
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("File Name      : %s%n", name));
    text.append (String.format ("File size      : %,d%n", buffer.length));

    return text.toString ();
  }

}