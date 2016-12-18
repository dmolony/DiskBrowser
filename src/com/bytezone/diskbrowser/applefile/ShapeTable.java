package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

/*-
 *  Offset     Meaning
 *    0        # of shapes
 *    1        unused
 *    2-3      offset to shape #1 (S1)
 *    3-4      offset to shape #2 (S2)
 *    S1-S1+1  shape definition #1
 *    S1+n     last byte = 0
 *    S2-S2+1  shape definition #1
 *    S2+n     last byte = 0
 */

public class ShapeTable extends AbstractFile
{
  private static final int SIZE = 400;
  private final int totalShapes;
  private final int[] shapeLength;
  private final int[] offsets;
  private int minSize = 9999;
  private int maxSize = 0;

  public ShapeTable (String name, byte[] buffer)
  {
    super (name, buffer);

    totalShapes = buffer[0] & 0xFF;
    shapeLength = new int[totalShapes];
    offsets = new int[totalShapes];
    int lastPtr = buffer.length;

    for (int i = totalShapes - 1; i >= 0; i--)
    {
      int ptr = HexFormatter.getShort (buffer, i * 2 + 2);
      int length = lastPtr - ptr;
      lastPtr = ptr;
      shapeLength[i] = length;
      offsets[i] = ptr;
      minSize = Math.min (minSize, length);
      maxSize = Math.max (maxSize, length);
    }
  }

  @Override
  public String getText ()
  {
    StringBuffer text = new StringBuffer ();

    int startPos = SIZE / 2;
    int maxWidth = 0;
    int maxHeight = 0;

    for (int i = 0; i < totalShapes; i++)
    {
      int[][] grid = new int[SIZE][SIZE];
      int row = startPos;
      int col = startPos;       // start in the middle of the grid
      if (i > 0)
        text.append ("\n");

      text.append (String.format ("Shape : %d%n", i));
      text.append (String.format ("Size  : %d%n", shapeLength[i]));

      String bytes = HexFormatter.getHexString (buffer, offsets[i], shapeLength[i]);
      int ptr = offsets[i];
      for (String s : split (bytes))
      {
        text.append (String.format ("%04X  : %s%n", ptr, s));
        ptr += 16;
      }

      ptr = offsets[i];
      while (buffer[ptr] != 0)
      {
        int value = buffer[ptr++] & 0xFF;
        int v1 = value >> 6;
        int v2 = (value & 0x38) >> 3;
        int v3 = value & 0x07;

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

      int width = maxCol - minCol + 1;
      int height = maxRow - minRow + 1;
      maxWidth = Math.max (maxWidth, width);
      maxHeight = Math.max (maxHeight, height);

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

    StringBuilder header = new StringBuilder ();
    header.append (String.format ("File Name      : %s%n", name));
    header.append (String.format ("File size      : %,d%n", buffer.length));
    header.append (String.format ("Total shapes   : %d%n", totalShapes));
    header.append (String.format ("Smallest       : %d%n", minSize));
    header.append (String.format ("Largest        : %d%n", maxSize));
    header.append (String.format ("Max dimensions : %d x %d%n%n", maxWidth, maxHeight));

    return header.toString () + text.toString ();
  }

  private List<String> split (String line)
  {
    List<String> list = new ArrayList<String> ();
    while (line.length () > 48)
    {
      list.add (line.substring (0, 47));
      line = line.substring (48);
    }
    list.add (line);
    return list;
  }

  public static boolean isShapeTable (byte[] buffer)
  {
    if (buffer.length == 0 || buffer[buffer.length - 1] != 0)
      return false;

    int totalShapes = buffer[0] & 0xFF;
    if (totalShapes == 0)
      return false;

    // this flags large files that start with a very small value
    //    System.out.printf ("Average shape length: %d%n", buffer.length / totalShapes);
    if (totalShapes * 500 < buffer.length)
      return false;

    for (int i = 0; i < totalShapes; i++)
    {
      // check index table entry is inside the file
      int ptr = i * 2 + 2;
      if (ptr >= buffer.length - 1)
        return false;

      // check index points inside the file
      int offset = HexFormatter.getShort (buffer, ptr);
      if (offset == 0 || offset >= buffer.length)
        return false;
    }

    return true;
  }
}