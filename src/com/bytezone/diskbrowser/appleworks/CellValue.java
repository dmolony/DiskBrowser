package com.bytezone.diskbrowser.appleworks;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class CellValue extends Cell
{
  CellFormat format;
  CellFormula formula;
  boolean lastEvalNA;
  boolean lastEvalError;
  double saneDouble;

  public CellValue (byte[] buffer, int row, int column, int offset, int length)
  {
    super (row, column, offset, length);

    type = "Value";

    //    if (header.ssMinVers != 0)
    //    {
    //      System.out.println ("AppleWorks v" + header.ssMinVers + " required!");
    //      value = HexFormatter.getHexString (buffer, offset, length);
    //    }
    //    else
    {
      format = new CellFormat (buffer[offset]);
      int b1 = buffer[offset + 1] & 0xFF;
      lastEvalNA = (b1 & 0x40) != 0;
      lastEvalError = (b1 & 0x20) != 0;
      saneDouble = HexFormatter.getSANEDouble (buffer, offset + 2);
      value = String.format (format.mask (), saneDouble).trim ();
      formula = new CellFormula (this, buffer, offset + 10, length - 10);
      value = String.format ("%-15s %s", value, formula.value);
    }
  }
}