package com.bytezone.diskbrowser.applefile;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Platform;
import com.bytezone.common.Platform.FontType;

// see graffidisk.v1.0.2mg
public class CharacterRom extends AbstractFile
{
  String description;
  List<Character> characters = new ArrayList<> ();

  public CharacterRom (String name, byte[] buffer)
  {
    super (name, buffer);

    description = new String (buffer, 16, 16);

    int sizeX = buffer[5] & 0xFF;
    int sizeY = buffer[6] & 0xFF;

    int gapX = 2;
    int gapY = 2;
    int marginX = 2;
    int marginY = 2;
    int heading = 15;

    image = new BufferedImage (16 * (sizeX + gapX) + 2 * marginX - gapX,
        6 * (sizeY + gapY) + 2 * marginY - gapY + heading, BufferedImage.TYPE_BYTE_GRAY);
    Graphics2D g2d = image.createGraphics ();
    g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) 1.0));

    int x = marginX;
    int y = marginY + heading;
    int count = 0;
    int ptr = 256;

    while (ptr < buffer.length)
    {
      Character character = new Character (buffer, ptr);
      characters.add (character);
      ptr += 8;

      g2d.drawImage (character.image, x, y, null);
      x += sizeX + gapX;
      if (++count % 16 == 0)
      {
        x = marginX;
        y += sizeY + gapY;
      }
    }

    y = marginY;
    x = marginX;
    Font font = Platform.getFont (FontType.SANS_SERIF, 10);
    g2d.setFont (font);

    for (int i = 0; i < description.length (); i++)
    {
      int pos = description.charAt (i);
      if (pos == 0)
        break;
      Character character = characters.get (pos - 32);
      g2d.drawImage (character.image, x, y, null);
      x += sizeX;
    }

    g2d.dispose ();
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder (description + "\n\n");
    for (int i = 256; i < buffer.length; i += 8)
    {
      for (int line = 0; line < 8; line++)
      {
        int value = buffer[i + line] & 0xFF;
        for (int bit = 0; bit < 8; bit++)
        {
          text.append ((value & 0x80) != 0 ? "X" : ".");
          value <<= 1;
        }
        text.append ("\n");
      }
      text.append ("\n");
    }
    return text.toString ();
  }

  public static boolean isRom (byte[] buffer)
  {
    if (buffer.length != 0x400)
      return false;

    // no idea what these mean
    // BD 41 53 10 A0 07 08
    return buffer[0] == (byte) 0xBD && buffer[1] == (byte) 0x41
        && buffer[2] == (byte) 0x53 && buffer[3] == (byte) 0x10;
  }

  class Character
  {
    private final BufferedImage image;

    public Character (byte[] buffer, int ptr)
    {
      // draw the image
      image = new BufferedImage (7, 8, BufferedImage.TYPE_BYTE_GRAY);
      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
      int element = 0;

      for (int line = 0; line < 8; line++)
      {
        int value = buffer[ptr++] & 0xFF;
        for (int bit = 0; bit < 7; bit++)
        {
          dataBuffer.setElem (element++, (value & 0x80) != 0 ? 255 : 0);
          value <<= 1;
        }
      }
    }
  }
}