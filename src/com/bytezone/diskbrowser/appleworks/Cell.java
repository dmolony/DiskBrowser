package com.bytezone.diskbrowser.appleworks;

class Cell
{
  final String cellName;
  final int row;
  final int column;
  String value;
  String type;

  static String getCellName (int row, int column)
  {
    char c1 = (char) ('A' + column / 26 - 1);
    char c2 = (char) ('A' + column % 26);
    return "" + (c1 == '@' ? "" : c1) + c2 + row;
  }

  public Cell (int row, int column, int offset, int length)
  {
    this.row = row;
    this.column = column;

    cellName = getCellName (row, column);
  }

  @Override
  public String toString ()
  {
    return String.format ("%5s : %s %s%n", cellName, type, value);
  }
}