package com.bytezone.diskbrowser.applefile;

import java.awt.image.DataBuffer;

// -----------------------------------------------------------------------------------//
public class FontFile extends CharacterList
// -----------------------------------------------------------------------------------//
{
  private static final int charsX = 16;

  // ---------------------------------------------------------------------------------//
  public FontFile (String name, byte[] buffer, int address)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    loadAddress = address;
    int ptr = 0;

    while (ptr < buffer.length)
    {
      characters.add (new FontFileCharacter (buffer, ptr));
      ptr += sizeY;
    }

    buildImage (borderX, borderY, gapX, gapY, sizeX, sizeY, charsX);
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isFont (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    if (buffer.length % 8 != 0)
      return false;

    for (int i = 0; i < 8; i++)
      if (buffer[i] != 0 && buffer[i] != 0x7F)
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  class FontFileCharacter extends Character
  // ---------------------------------------------------------------------------------//
  {
    // -------------------------------------------------------------------------------//
    public FontFileCharacter (byte[] buffer, int ptr)
    // -------------------------------------------------------------------------------//
    {
      super (sizeX, sizeY);

      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
      int element = 0;

      for (int i = 0; i < sizeY; i++)
      {
        int value = buffer[ptr++] & 0xFF;
        for (int j = 0; j < sizeX; j++)
        {
          dataBuffer.setElem (element++, (value & 0x01) == 0 ? 0 : 0xFF);
          value >>>= 1;
        }
      }
    }
  }
}