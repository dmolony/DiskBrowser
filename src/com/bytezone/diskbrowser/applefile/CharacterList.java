package com.bytezone.diskbrowser.applefile;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
abstract class CharacterList extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  static final int borderX = 3;
  static final int borderY = 3;
  static final int gapX = 3;
  static final int gapY = 3;

  static final int sizeX = 7;
  static final int sizeY = 8;

  List<Character> characters = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public CharacterList (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  void buildImage (int borderX, int borderY, int gapX, int gapY, int sizeX, int sizeY,
      int charsX)
  // ---------------------------------------------------------------------------------//
  {
    int charsY = (characters.size () - 1) / charsX + 1;
    image = new BufferedImage (                                //
        Utility.dimension (charsX, borderX, sizeX, gapX),      //
        Utility.dimension (charsY, borderY, sizeY, gapY),      //
        BufferedImage.TYPE_BYTE_GRAY);

    Graphics2D g2d = image.createGraphics ();
    g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) 1.0));

    int count = 0;
    int x = borderX;
    int y = borderY;

    for (Character character : characters)
    {
      g2d.drawImage (character.image, x, y, null);
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

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");

    for (Character character : characters)
    {
      text.append (character);
      text.append ("\n");
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  class Character
  // ---------------------------------------------------------------------------------//
  {
    BufferedImage image;

    // -------------------------------------------------------------------------------//
    public Character (int sizeX, int sizeY)
    // -------------------------------------------------------------------------------//
    {
      image = new BufferedImage (sizeX, sizeY, BufferedImage.TYPE_BYTE_GRAY);
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();
      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
      int element = 0;

      for (int i = 0; i < sizeY; i++)
      {
        for (int j = 0; j < sizeX; j++)
          text.append (dataBuffer.getElem (element++) == 0 ? "." : "X");
        text.append ("\n");
      }

      return text.toString ();
    }
  }
}
