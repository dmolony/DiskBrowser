package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class PlainMessage extends MessageV1
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