package com.bytezone.diskbrowser.applefile;

import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

// Found on Pascal disks
public class Charset extends CharacterList
{
  private static final int charsX = 16;
  private static final int charsY = 8;

  List<Character> characters = new ArrayList<> ();

  public Charset (String name, byte[] buffer)
  {
    super (name, buffer, charsX, charsY);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < buffer.length; i += sizeY)
    {
      for (int line = sizeY - 1; line >= 0; line--)
      {
        int value = buffer[line + i] & 0xFF;
        for (int bit = 0; bit < sizeX; bit++)
        {
          text.append ((value & 0x01) == 0 ? "." : "X");
          value >>>= 1;
        }
        text.append ("\n");
      }
      text.append ("\n");
    }

    return text.toString ();
  }

  @Override
  Character createCharacter (byte[] buffer, int ptr)
  {
    return new CharsetCharacter (buffer, ptr);
  }

  class CharsetCharacter extends Character
  {
    public CharsetCharacter (byte[] buffer, int ptr)
    {
      super (buffer, ptr);

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