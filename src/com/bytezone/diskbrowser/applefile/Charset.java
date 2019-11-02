package com.bytezone.diskbrowser.applefile;

import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

// Found on Pascal disks
// -----------------------------------------------------------------------------------//
public class Charset extends CharacterList
// -----------------------------------------------------------------------------------//
{
  private static final int charsX = 16;
  private static final int charsY = 8;

  List<Character> characters = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public Charset (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer, charsX, charsY);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  Character createCharacter (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return new CharsetCharacter (buffer, ptr);
  }

  // ---------------------------------------------------------------------------------//
  class CharsetCharacter extends Character
  // ---------------------------------------------------------------------------------//
  {
    // -------------------------------------------------------------------------------//
    public CharsetCharacter (byte[] buffer, int ptr)
    // -------------------------------------------------------------------------------//
    {
      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
      int element = 0;

      for (int i = sizeY - 1; i >= 0; i--)
      {
        int value = buffer[ptr + i] & 0xFF;
        for (int j = 0; j < sizeX; j++)
        {
          dataBuffer.setElem (element++, (value & 0x01) == 0 ? 0 : 0xFF);
          value >>>= 1;
        }
      }
    }
  }
}