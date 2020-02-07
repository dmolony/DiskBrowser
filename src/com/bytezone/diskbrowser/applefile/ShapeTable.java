package com.bytezone.diskbrowser.applefile;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
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

// -----------------------------------------------------------------------------------//
public class ShapeTable extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private final List<Shape> shapes = new ArrayList<> ();
  private static final int SIZE = 400;
  int maxWidth = 0;
  int maxHeight = 0;

  // ---------------------------------------------------------------------------------//
  public ShapeTable (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    int minRow = 200;
    int minCol = 200;
    int maxRow = 200;
    int maxCol = 200;

    int totalShapes = buffer[0] & 0xFF;
    for (int i = 0; i < totalShapes; i++)
    {
      Shape shape = new Shape (buffer, i);
      if (!shape.valid)
        continue;                   // shape table should be abandoned
      shapes.add (shape);

      minRow = Math.min (minRow, shape.minRow);
      minCol = Math.min (minCol, shape.minCol);
      maxRow = Math.max (maxRow, shape.maxRow);
      maxCol = Math.max (maxCol, shape.maxCol);
    }

    maxHeight = maxRow - minRow + 1;
    maxWidth = maxCol - minCol + 1;
    for (Shape shape : shapes)
      shape.convertGrid (minRow, minCol, maxHeight, maxWidth);

    int cols = (int) Math.sqrt (shapes.size ());
    int rows = (shapes.size () - 1) / cols + 1;

    image = new BufferedImage ((cols + 1) * (maxWidth + 5), (rows + 1) * (maxHeight + 5),
        BufferedImage.TYPE_BYTE_GRAY);

    int x = 10;
    int y = 10;
    int count = 0;
    Graphics2D g2d = image.createGraphics ();
    g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) 1.0));

    for (Shape shape : shapes)
    {
      g2d.drawImage (shape.image, x, y, null);
      x += maxWidth + 5;
      if (++count % cols == 0)
      {
        x = 10;
        y += maxHeight + 5;
      }
    }
    g2d.dispose ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("File Name      : %s%n", name));
    text.append (String.format ("File size      : %,d%n", buffer.length));
    text.append (String.format ("Total shapes   : %d%n", shapes.size ()));
    text.append (String.format ("Max dimensions : %d x %d%n%n", maxWidth, maxHeight));

    for (Shape shape : shapes)
    {
      shape.drawText (text);
      text.append ("\n");
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isShapeTable (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    if (buffer.length == 0)
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
      int offset = HexFormatter.unsignedShort (buffer, ptr);
      if (offset == 0 || offset >= buffer.length)
        return false;

      // check if previous shape ended with zero
      //      if (i > 0 && buffer[offset - 1] > 0)
      //        return false;
    }

    return true;
  }

  // ---------------------------------------------------------------------------------//
  class Shape
  // ---------------------------------------------------------------------------------//
  {
    private final byte[] buffer;
    private final int index;

    int offset;
    int actualLength;
    int minRow, maxRow;
    int minCol, maxCol;
    int startRow = SIZE / 2;
    int startCol = SIZE / 2;
    int[][] grid = new int[SIZE][SIZE];
    int[][] displayGrid;
    boolean valid;

    private BufferedImage image;

    public Shape (byte[] buffer, int index)
    {
      this.index = index;
      this.buffer = buffer;

      int row = startRow;
      int col = startCol;

      offset = HexFormatter.unsignedShort (buffer, index * 2 + 2);

      int ptr = offset;
      while (ptr < buffer.length)
      {
        int value = buffer[ptr++] & 0xFF;

        if (value == 0)
          break;

        // P  = plot
        // DD = direction to move
        int v1 = value >>> 6;                     //  DD......
        int v2 = (value & 0x38) >>> 3;            //  ..PDD...
        int v3 = value & 0x07;                    //  .....PDD

        // rightmost 3 bits
        if (v3 >= 4)
          if (!plot (grid, row, col))
            return;

        if (v3 == 0 || v3 == 4)
          row--;
        else if (v3 == 1 || v3 == 5)
          col++;
        else if (v3 == 2 || v3 == 6)
          row++;
        else
          col--;

        // middle 3 bits
        if (v2 >= 4)
          if (!plot (grid, row, col))
            return;

        // cannot move up without plotting if v1 is zero
        if ((v2 == 0 && v1 != 0) || v2 == 4)
          row--;
        else if (v2 == 1 || v2 == 5)
          col++;
        else if (v2 == 2 || v2 == 6)
          row++;
        else if (v2 == 3 || v2 == 7)
          col--;

        // leftmost 2 bits (cannot plot or move up)
        if (v1 == 1)
          col++;
        else if (v1 == 2)
          row++;
        else if (v1 == 3)
          col--;
      }

      actualLength = ptr - offset;

      //      endRow = row;
      //      endCol = col;

      // find min and max rows with pixels
      minRow = startRow;
      maxRow = startRow;
      //      minRow = Math.min (minRow, endRow);
      //      maxRow = Math.max (maxRow, endRow);
      for (row = 1; row < grid.length; row++)
      {
        if (grid[row][0] > 0)
        {
          minRow = Math.min (minRow, row);
          maxRow = Math.max (maxRow, row);
        }
      }

      // find min and max columns with pixels
      minCol = startCol;
      maxCol = startCol;
      //      minCol = Math.min (minCol, endCol);
      //      maxCol = Math.max (maxCol, endCol);
      for (col = 1; col < grid[0].length; col++)
      {
        if (grid[0][col] > 0)
        {
          minCol = Math.min (minCol, col);
          maxCol = Math.max (maxCol, col);
        }
      }
      valid = true;
    }

    void convertGrid (int offsetRows, int offsetColumns, int rows, int columns)
    {
      //      System.out.printf ("Converting shape # %d%n", index);
      //      System.out.printf ("offsetRows %d offsetCols %d%n", offsetRows,
      // offsetColumns);
      //      System.out.printf ("rows %d cols %d%n", rows, columns);

      displayGrid = new int[rows][columns];
      for (int row = 0; row < rows; row++)
        for (int col = 0; col < columns; col++)
          displayGrid[row][col] = grid[offsetRows + row][offsetColumns + col];
      grid = null;

      // draw the image
      image = new BufferedImage (columns, rows, BufferedImage.TYPE_BYTE_GRAY);
      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();
      int element = 0;
      for (int row = 0; row < rows; row++)
        for (int col = 0; col < columns; col++)
          dataBuffer.setElem (element++, displayGrid[row][col] == 0 ? 0 : 255);

      startRow -= offsetRows;
      startCol -= offsetColumns;
      //      endRow -= offsetRows;
      //      endCol -= offsetColumns;
    }

    private boolean plot (int[][] grid, int row, int col)
    {
      if (row < 0 || row >= SIZE || col < 0 || col >= SIZE)
      {
        System.out.printf ("Shape table out of range: %d, %d%n", row, col);
        return false;
      }
      grid[row][col] = 1;       // plot
      grid[0][col]++;           // increment total column dots
      grid[row][0]++;           // increment total row dots

      return true;
    }

    public void drawText (StringBuilder text)
    {
      text.append (String.format ("Shape  : %d%n", index));
      text.append (String.format ("Size   : %d%n", actualLength));
      //      text.append (String.format ("Width  : %d%n", width));
      //      text.append (String.format ("Height : %d%n", height));

      // append the shape's data
      String bytes = HexFormatter.getHexString (buffer, offset, actualLength);
      int ptr = offset;
      for (String s : split (bytes))
      {
        text.append (String.format ("  %04X : %s%n", ptr, s));
        ptr += 16;
      }
      text.append ("\n");

      for (int row = 0; row < displayGrid.length; row++)
      {
        for (int col = 0; col < displayGrid[0].length; col++)
          if (col == startCol && row == startRow)
            text.append (displayGrid[row][col] > 0 ? " @" : " .");
          //          else if (col == endCol && row == endRow)
          //            text.append (displayGrid[row][col] > 0 ? " #" : " .");
          else if (displayGrid[row][col] == 0)
            text.append ("  ");
          else
            text.append (" X");

        text.append ("\n");
      }

      text.append ("\n");
    }

    private List<String> split (String line)
    {
      List<String> list = new ArrayList<> ();
      while (line.length () > 48)
      {
        list.add (line.substring (0, 47));
        line = line.substring (48);
      }
      list.add (line);
      return list;
    }

    @Override
    public String toString ()
    {
      return String.format ("%3d  %3d  %3d  %3d  %3d", index, minRow, maxRow, minCol,
          maxCol);
    }
  }
}