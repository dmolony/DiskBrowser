package com.bytezone.diskbrowser.applefile;

import java.awt.image.DataBuffer;

// Found on Pascal disks
// -----------------------------------------------------------------------------------//
public class Charset extends CharacterList
// -----------------------------------------------------------------------------------//
{
  private static final int charsX = 16;

  // ---------------------------------------------------------------------------------//
  public Charset (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    int ptr = 0;

    while (ptr < buffer.length)
    {
      characters.add (new CharsetCharacter (buffer, ptr));
      ptr += sizeY;
    }

    buildImage (borderX, borderY, gapX, gapY, sizeX, sizeY, charsX);
  }

  // ---------------------------------------------------------------------------------//
  class CharsetCharacter extends Character
  // ---------------------------------------------------------------------------------//
  {
    // -------------------------------------------------------------------------------//
    public CharsetCharacter (byte[] buffer, int ptr)
    // -------------------------------------------------------------------------------//
    {
      super (sizeX, sizeY);

      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
      int element = 0;
      ptr += sizeY;         // start at the end and move backwards

      for (int i = 0; i < sizeY; i++)
      {
        int value = buffer[--ptr] & 0xFF;
        for (int j = 0; j < sizeX; j++)
        {
          dataBuffer.setElem (element++, (value & 0x01) == 0 ? 0 : 0xFF);
          value >>>= 1;
        }
      }
    }
  }
}