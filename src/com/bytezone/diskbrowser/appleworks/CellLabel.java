package com.bytezone.diskbrowser.appleworks;

import com.bytezone.diskbrowser.HexFormatter;

public class CellLabel extends Cell
{
  boolean propagated;
  String label;

  public CellLabel (byte[] buffer, int row, int column, int offset, int length)
  {
    super (row, column, offset, length);

    int b1 = buffer[offset] & 0xFF;

    //    label = new String (buffer, offset + 1, length - 1);

    // MOUSE.TEXT.SS/TAWUG.22/TAWUG 21 to 25.2mg has funny characters
    label = HexFormatter.sanitiseString (buffer, offset + 1, length - 1);

    //    int columnWidth = header.columnWidths[column];

    value = "[" + label + "]";
    type = "Label";
    propagated = (b1 & 0xA0) == 0x20;

    if (propagated)
      value += "+";
  }
}