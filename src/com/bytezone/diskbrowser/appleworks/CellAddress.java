package com.bytezone.diskbrowser.appleworks;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class CellAddress
// -----------------------------------------------------------------------------------//
{
  int colRef;
  int rowRef;

  // ---------------------------------------------------------------------------------//
  CellAddress (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    colRef = buffer[offset];
    rowRef = HexFormatter.intValue (buffer[offset + 1], buffer[offset + 2]);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("[Row=%04d, Col=%04d]", rowRef, colRef);
  }
}