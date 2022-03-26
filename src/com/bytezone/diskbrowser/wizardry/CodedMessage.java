package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class CodedMessage extends Message
// -----------------------------------------------------------------------------------//
{
  public static int codeOffset = 185;

  // ---------------------------------------------------------------------------------//
  CodedMessage (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected String getLine (int offset)
  // ---------------------------------------------------------------------------------//
  {
    int length = buffer[offset++] & 0xFF;
    byte[] translation = new byte[length];
    codeOffset--;
    for (int j = 0; j < length; j++)
    {
      translation[j] = buffer[offset + j];
      translation[j] -= codeOffset - j * 3;
    }
    return HexFormatter.getString (translation, 0, length);
  }
}