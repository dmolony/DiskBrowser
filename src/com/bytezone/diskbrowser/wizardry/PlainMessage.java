package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class PlainMessage extends Message
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  PlainMessage (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected String getLine (int offset)
  // ---------------------------------------------------------------------------------//
  {
    int length = buffer[offset] & 0xFF;
    return HexFormatter.getString (buffer, offset + 1, length);
  }
}