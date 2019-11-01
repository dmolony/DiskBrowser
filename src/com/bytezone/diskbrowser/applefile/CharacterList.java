package com.bytezone.diskbrowser.applefile;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

abstract class CharacterList extends AbstractFile
{
  static final int borderX = 3;
  static final int borderY = 3;
  static final int gapX = 3;
  static final int gapY = 3;

  static final int sizeX = 7;
  static final int sizeY = 8;

  List<Character> characters = new ArrayList<> ();

  public CharacterList (String name, byte[] buffer, int charsX, int charsY)
  {
    this (name, buffer, charsX, charsY, 0);
  }

  public CharacterList (String name, byte[] buffer, int charsX, int charsY, int offset)
  {
    super (name, buffer);

    image = new BufferedImage (                         //
        dimension (charsX, borderX, sizeX, gapX),       //
        dimension (charsY, borderY, sizeY, gapY),       //
        BufferedImage.TYPE_BYTE_GRAY);

    Graphics2D g2d = image.createGraphics ();
    g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) 1.0));

    if (false)        // show gaps around the glyphs
    {
      g2d.setColor (new Color (245, 245, 245));   // match background
      g2d.fillRect (0, 0, image.getWidth (), image.getHeight ());
    }

    int x = borderX;
    int y = borderY;
    int count = 0;
    int ptr = offset;

    while (ptr < buffer.length)
    {
      Character c = createCharacter (buffer, ptr);
      characters.add (c);
      ptr += sizeY;

      g2d.drawImage (c.image, x, y, null);
      if (++count % charsX == 0)
      {
        x = borderX;
        y += sizeY + gapY;
      }
      else
        x += sizeX + gapX;
    }

    g2d.dispose ();
  }

  abstract Character createCharacter (byte[] buffer, int ptr);

  int dimension (int chars, int border, int size, int gap)
  {
    return border * 2 + chars * (size + gap) - gap;
  }

  class Character
  {
    BufferedImage image;

    public Character (byte[] buffer, int ptr)
    {
      image = new BufferedImage (sizeX, sizeY, BufferedImage.TYPE_BYTE_GRAY);
    }
  }
}
