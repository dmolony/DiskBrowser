package com.bytezone.diskbrowser.appleworks;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class CellFormula
{
  private static String[] tokens = {//
        "@Deg", "@Rad", "@Pi", "@True", "@False", "@Not", "@IsBlank", "@IsNA", "@IsError",
            "@Exp", "@Ln", "@Log", "@Cos", "@Sin", "@Tan", "@ACos", "@ASin", "@ATan2",
            "@ATan", "@Mod", "@FV", "@PV", "@PMT", "@Term", "@Rate", "@Round", "@Or", "@And",
            "@Sum", "@Avg", "@Choose", "@Count", "@Error", "@IRR", "@If", "@Int", "@Lookup",
            "@Max", "@Min", "@NA", "@NPV", "@Sqrt", "@Abs", "", "<>", ">=", "<=", "=", ">",
            "<", ",", "^", ")", "-", "+", "/", "*", "(", "-", "+", "..." };
  String value;

  public CellFormula (Cell cell, byte[] buffer, int offset, int length)
  {
    StringBuilder text = new StringBuilder ();

    for (int i = 0; i < length; i++)
    {
      int value = buffer[offset + i] & 0xFF;
      if (value < 0xFD)
      {
        String token = tokens[value - 0xC0];
        text.append (token);
        if (value == 0xE0 || value == 0xE7)
          i += 3;
      }
      else if (value == 0xFD)
      {
        double d = HexFormatter.getSANEDouble (buffer, offset + i + 1);
        String num = String.format ("%f", d).trim ();
        while (num.endsWith ("0"))
          num = num.substring (0, num.length () - 1);
        if (num.endsWith ("."))
          num = num.substring (0, num.length () - 1);
        text.append (num);
        i += 8;
      }
      else if (value == 0xFE)
      {
        CellAddress address = new CellAddress (buffer, offset + i + 1);
        String cellName =
              Cell.getCellName (cell.row + address.rowRef, cell.column + address.colRef);
        i += 3;
        text.append (cellName);
      }
      else if (value == 0xFF)
      {
        int len = buffer[offset + i + 1] & 0xFF;
        String word = new String (buffer, offset + i + 2, len);
        i += len + 1;
        System.out.println ("Word: " + word);
      }
    }
    value = text.toString ();
  }
}