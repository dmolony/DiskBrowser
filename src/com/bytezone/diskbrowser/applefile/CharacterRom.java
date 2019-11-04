package com.bytezone.diskbrowser.applefile;

import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// see graffidisk.v1.0.2mg
// -----------------------------------------------------------------------------------//
public class CharacterRom extends CharacterList
// -----------------------------------------------------------------------------------//
{
  private static final int charsX = 16;
  private static final int charsY = 6;
  private static final int HEADER_LENGTH = 0x100;

  String description;
  List<Character> characters = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public CharacterRom (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer, charsX, charsY, HEADER_LENGTH);

    description = HexFormatter.getCString (buffer, 16);
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isRom (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    if (buffer.length != 0x400)
      return false;

    // see CHARROM.S on graffidisk
    // BD 41 53 10 A0 07 08
    return buffer[0] == (byte) 0xBD && buffer[1] == (byte) 0x41
        && buffer[2] == (byte) 0x53 && buffer[4] == (byte) 0xA0
        && buffer[5] == (byte) 0x07 && buffer[6] == (byte) 0x08;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  Character createCharacter (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return new CharacterRomCharacter (buffer, ptr);
  }

  // ---------------------------------------------------------------------------------//
  class CharacterRomCharacter extends Character
  // ---------------------------------------------------------------------------------//
  {
    // -------------------------------------------------------------------------------//
    public CharacterRomCharacter (byte[] buffer, int ptr)
    // -------------------------------------------------------------------------------//
    {
      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
      int element = 0;

      for (int i = 0; i < sizeY; i++)
      {
        int value = buffer[ptr++] & 0xFF;
        for (int j = 0; j < sizeX; j++)
        {
          dataBuffer.setElem (element++, (value & 0x80) == 0 ? 0 : 0xFF);
          value <<= 1;
        }
      }
    }
  }
}