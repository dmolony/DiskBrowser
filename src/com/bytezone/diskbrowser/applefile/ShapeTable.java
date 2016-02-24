package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class ShapeTable extends AbstractFile
{
  private static final int SIZE = 400;

  public ShapeTable (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  @Override
  public String getText ()
  {
    StringBuffer text = new StringBuffer ();
    int totalShapes = buffer[0] & 0xFF;
    int startPos = SIZE / 2;

    for (int i = 0; i < totalShapes; i++)
    {
      int offset = HexFormatter.intValue (buffer[i * 2 + 2], buffer[i * 2 + 3]);
      int[][] grid = new int[SIZE][SIZE];
      int row = startPos;
      int col = row;
      if (i > 0)
        text.append ("\n");
      text.append ("Shape " + i + " :\n");

      while (buffer[offset] != 0)
      {
        int value = buffer[offset++] & 0xFF;
        int v1 = value >> 6;
        int v2 = (value & 0x38) >> 3;
        int v3 = value & 0x07;
//        System.out.printf ("%02X  %02X  %02X  %02X%n", value, v1, v2, v3);

        if (v3 >= 4)
        {
          grid[row][col] = 1;
          grid[0][col]++;
          grid[row][0]++;
        }
        if (v3 == 0 || v3 == 4)
          row--;
        else if (v3 == 1 || v3 == 5)
          col++;
        else if (v3 == 2 || v3 == 6)
          row++;
        else
          col--;

        if (v2 >= 4)
        {
          grid[row][col] = 1;
          grid[0][col]++;
          grid[row][0]++;
        }
        if (v2 == 0 && v1 != 0)
          row--;
        else if (v2 == 4)
          row--;
        else if (v2 == 1 || v2 == 5)
          col++;
        else if (v2 == 2 || v2 == 6)
          row++;
        else if (v2 == 3 || v2 == 7)
          col--;

        if (v1 == 1)
          col++;
        else if (v1 == 2)
          row++;
        else if (v1 == 3)
          col--;

      }
      text.append ("\n");

      int minRow = startPos, maxRow = startPos;
      int minCol = startPos, maxCol = startPos;
      for (row = 1; row < grid.length; row++)
      {
        if (grid[row][0] > 0)
        {
          if (row < minRow)
            minRow = row;
          if (row > maxRow)
            maxRow = row;
        }
      }

      for (col = 1; col < grid[0].length; col++)
      {
        if (grid[0][col] > 0)
        {
          if (col < minCol)
            minCol = col;
          if (col > maxCol)
            maxCol = col;
        }
      }

      for (row = minRow; row <= maxRow; row++)
      {
        for (col = minCol; col <= maxCol; col++)
        {
          if (col == startPos && row == startPos)
            text.append (grid[row][col] > 0 ? " @" : " .");
          else if (grid[row][col] == 0)
            text.append ("  ");
          else
            text.append (" X");
        }
        text.append ("\n");
      }
    }

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  public static boolean isShapeTable (byte[] buffer)
  {
    if (buffer.length == 0 || buffer[buffer.length - 1] != 0)
      return false;
    int totalShapes = buffer[0] & 0xFF;
    if (totalShapes == 0)
      return false;

    int lastOffset = 0;
    for (int i = 0; i < totalShapes; i++)
    {
      // check index table entry is inside the file
      int ptr = i * 2 + 2;
      if (ptr >= buffer.length - 1)
        return false;

      // check index points inside the file
      int offset = HexFormatter.intValue (buffer[ptr], buffer[ptr + 1]);
      if (offset == 0 || offset < lastOffset || offset >= buffer.length)
        return false;

      lastOffset = offset;
    }

    return true;
  }
}