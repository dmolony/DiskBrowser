package com.bytezone.diskbrowser.appleworks;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class CellConstant extends Cell
// -----------------------------------------------------------------------------------//
{
  double saneDouble;
  CellFormat format;

  // ---------------------------------------------------------------------------------//
  CellConstant (byte[] buffer, int row, int column, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    super (row, column, offset, length);

    type = "Const";

    //    assert length == 10;

    if (length != 10)
    {
      System.out.println ("Spreadsheet CellConstant with length != 10");
      System.out.printf ("Row %d, Col %d, Length %d %n", row, column, length);
      System.out.println (HexFormatter.format (buffer, offset, length));
      type = "*** Invalid Constant ***";
      value = "";
    }
    else
    {
      long bits = 0;
      for (int i = 9; i >= 2; i--)
      {
        bits <<= 8;
        bits |= buffer[offset + i] & 0xFF;
      }

      saneDouble = Double.longBitsToDouble (bits);
      format = new CellFormat (buffer[offset], buffer[offset + 1]);
      value = String.format (format.mask (), saneDouble).trim ();
    }
  }
}