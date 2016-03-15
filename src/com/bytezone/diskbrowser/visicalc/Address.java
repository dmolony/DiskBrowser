package com.bytezone.diskbrowser.visicalc;

class Address implements Comparable<Address>
{
  private static final int MAX_ROWS = 255;
  private static final int MAX_COLUMNS = 64;

  int row, column;
  int rowKey;
  int columnKey;
  String text;

  public Address (String column, String row)
  {
    set (column, row);
  }

  public Address (int column, int row)
  {
    assert column <= MAX_COLUMNS;
    assert row <= MAX_ROWS;
    this.row = row;
    this.column = column;
    rowKey = row * MAX_COLUMNS + column;
    columnKey = column * MAX_ROWS + row;

    int col1 = column / 26;
    int col2 = column % 26;
    String col =
        col1 > 0 ? (char) ('@' + col1) + ('A' + col2) + "" : (char) ('A' + col2) + "";
    text = col + (row + 1);
  }

  public Address (String address)
  {
    if (address.charAt (1) < 'A')
      set (address.substring (0, 1), address.substring (1));
    else
      set (address.substring (0, 2), address.substring (2));
  }

  private void set (String sCol, String sRow)
  {
    if (sCol.length () == 1)
      column = sCol.charAt (0) - 'A';
    else if (sCol.length () == 2)
      column = (sCol.charAt (0) - '@') * 26 + sCol.charAt (1) - 'A';
    else
      System.out.println ("Bollocks");

    try
    {
      row = Integer.parseInt (sRow) - 1;
      rowKey = row * MAX_COLUMNS + column;
      columnKey = column * MAX_ROWS + row;
      text = sCol + sRow;
    }
    catch (NumberFormatException e)
    {
      System.out.printf ("NFE: %s%n", sRow);
      //      for (StackTraceElement ste : Thread.currentThread ().getStackTrace ())
      //        System.out.println (ste);
    }
  }

  Address nextRow ()
  {
    Address next = new Address (column, row + 1);
    return next;
  }

  Address nextColumn ()
  {
    Address next = new Address (column + 1, row);
    return next;
  }

  // copied from Appleworks Cell
  static String getCellName (int row, int column)
  {
    char c1 = (char) ('A' + column / 26 - 1);
    char c2 = (char) ('A' + column % 26);
    return "" + (c1 == '@' ? "" : c1) + c2 + row;
  }

  @Override
  public String toString ()
  {
    return String.format ("%-4s %3d %3d %4d", text, row, column, rowKey);
  }

  @Override
  public int compareTo (Address o)
  {
    return rowKey - o.rowKey;
  }
}