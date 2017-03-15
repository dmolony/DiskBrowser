package com.bytezone.diskbrowser.visicalc;

import java.util.Arrays;

class Address implements Comparable<Address>
{
  private static final int MAX_ROWS = 255;
  private static final int MAX_COLUMNS = 64;

  private int row, column;
  private int rowKey;
  private int columnKey;
  private String text;

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
    assert address.length () >= 2;
    if (address.charAt (1) < 'A')
      set (address.substring (0, 1), address.substring (1));
    else
      set (address.substring (0, 2), address.substring (2));
  }

  public boolean matches (String addressText)
  {
    Address address = new Address (addressText);
    return this.rowMatches (address) && this.columnMatches (address);
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
      System.out.printf ("sCol:%s,sRow:%s%n", sCol, sRow);
      System.out.printf ("NFE: %s%n", sRow);
      System.out.println (Arrays.toString (Thread.currentThread ().getStackTrace ()));
    }
  }

  boolean rowMatches (Address other)
  {
    return row == other.row;
  }

  boolean columnMatches (Address other)
  {
    return column == other.column;
  }

  int getRow ()
  {
    return row;
  }

  int getColumn ()
  {
    return column;
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

  public String getText ()
  {
    return text;
  }

  int getRowKey ()
  {
    return rowKey;
  }

  int getColumnKey ()
  {
    return columnKey;
  }

  public String getDetails ()
  {
    return String.format ("Row:%3d  Col:%3d  rKey:%5d  cKey:%5d", row, column, rowKey,
        columnKey);
  }

  @Override
  public String toString ()
  {
    return String.format ("%-6s Row:%3d Col:%3d Key:%4d", text, row, column, rowKey);
  }

  @Override
  public int compareTo (Address o)
  {
    return rowKey - o.rowKey;
  }
}