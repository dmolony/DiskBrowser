package com.bytezone.diskbrowser.wizardry;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class MazeGridV5 extends AbstractFile
{
  MazeCell[][] grid = new MazeCell[8][8];

  public MazeGridV5 (String name, byte[] buffer)
  {
    super (name, buffer);

    for (int row = 0; row < 8; row++)
      for (int col = 0; col < 8; col++)
        grid[row][col] = getLayout (row, col);
  }

  @Override
  public BufferedImage getImage ()
  {
    Dimension cellSize = new Dimension (22, 22);
    image = new BufferedImage (8 * cellSize.width + 1, 8 * cellSize.height + 1,
        BufferedImage.TYPE_USHORT_555_RGB);
    Graphics2D g = image.createGraphics ();
    g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    for (int row = 0; row < 8; row++)
      for (int column = 0; column < 8; column++)
      {
        MazeCell cell = grid[row][column];
        int x = column * cellSize.width;
        int y = image.getHeight () - (row + 1) * cellSize.height - 1;
        cell.draw (g, x, y);
      }
    return image;
  }

  private MazeCell getLayout (int row, int column)
  {
    MazeAddress address = new MazeAddress (0, row, column);
    MazeCell cell = new MazeCell (address);

    int offset = column * 2 + row / 4;

    int value = HexFormatter.intValue (buffer[offset]);
    value >>>= (row % 4) * 2;
    cell.westWall = ((value & 1) == 1);
    value >>>= 1;
    cell.westDoor = ((value & 1) == 1);

    value = HexFormatter.intValue (buffer[offset + 16]);
    value >>>= (row % 4) * 2;
    cell.southWall = ((value & 1) == 1);
    value >>>= 1;
    cell.southDoor = ((value & 1) == 1);

    value = HexFormatter.intValue (buffer[offset + 32]);
    value >>>= (row % 4) * 2;
    cell.eastWall = ((value & 1) == 1);
    value >>>= 1;
    cell.eastDoor = ((value & 1) == 1);

    value = HexFormatter.intValue (buffer[offset + 48]);
    value >>>= (row % 4) * 2;
    cell.northWall = ((value & 1) == 1);
    value >>>= 1;
    cell.northDoor = ((value & 1) == 1);

    return cell;
  }
}