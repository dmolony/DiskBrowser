package com.bytezone.diskbrowser.applefile;

import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class FontFile extends CharacterList
// -----------------------------------------------------------------------------------//
{
  private static final int charsX = 16;
  private static final int charsY = 6;

  List<Character> characters = new ArrayList<Character> ();

  // ---------------------------------------------------------------------------------//
  public FontFile (String name, byte[] buffer, int address)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer, charsX, charsY);

    loadAddress = address;
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
  @Override
  Character createCharacter (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return new FontFileCharacter (buffer, ptr);
  }

  // ---------------------------------------------------------------------------------//
  class FontFileCharacter extends Character
  // ---------------------------------------------------------------------------------//
  {
    // -------------------------------------------------------------------------------//
    public FontFileCharacter (byte[] buffer, int ptr)
    // -------------------------------------------------------------------------------//
    {
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