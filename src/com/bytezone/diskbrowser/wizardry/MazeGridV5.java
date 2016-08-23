package com.bytezone.diskbrowser.wizardry;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class MazeGridV5 extends AbstractFile
{
  List<MazeGrid> grids = new ArrayList<MazeGrid> ();

  public MazeGridV5 (String name, byte[] buffer)
  {
    super (name, buffer);

    for (int i = 0; i < 16; i++)
    {
      MazeCell[][] grid = new MazeCell[8][8];
      for (int row = 0; row < 8; row++)
        for (int col = 0; col < 8; col++)
          grid[row][col] = getLayout (i, row, col);

      MazeGrid mazeGrid = new MazeGrid ();
      mazeGrid.grid = grid;
      grids.add (mazeGrid);
      mazeGrid.yOffset = buffer[512 + i] & 0xFF;
      mazeGrid.xOffset = buffer[528 + i] & 0xFF;
    }
  }

  @Override
  public BufferedImage getImage ()
  {
    Dimension cellSize = new Dimension (22, 22);
    int gridWidth = 8 * cellSize.width + 1;
    int gridHeight = 8 * cellSize.height + 1;
    image = new BufferedImage (6 * gridWidth, 6 * gridHeight,
        BufferedImage.TYPE_USHORT_555_RGB);
    Graphics2D g = image.createGraphics ();
    g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    for (int i = 0; i < 16; i++)
    {
      MazeGrid mazeGrid = grids.get (i);
      for (int row = 0; row < 8; row++)
        for (int column = 0; column < 8; column++)
        {
          MazeCell cell = mazeGrid.grid[row][column];
          int x = column * cellSize.width;
          int y = image.getHeight () - (row + 1) * cellSize.height - 1;
          x += (mazeGrid.xOffset - 0x80) * cellSize.width + 10 * cellSize.width;
          y -= (mazeGrid.yOffset - 0x80) * cellSize.height + 10 * cellSize.height;
          cell.draw (g, x, y);
        }
    }
    return image;
  }

  private MazeCell getLayout (int gridNo, int row, int column)
  {
    MazeAddress address = new MazeAddress (0, row, column);
    MazeCell cell = new MazeCell (address);

    int offset = gridNo * 16 + column * 2 + row / 4;
    int value;

    if (false)
    {
      value = HexFormatter.intValue (buffer[offset]);
      value >>>= (row % 4) * 2;
      cell.westWall = ((value & 1) == 1);
      value >>>= 1;
      cell.westDoor = ((value & 1) == 1);
    }

    if (false)
    {
      value = HexFormatter.intValue (buffer[offset + 256]);
      value >>>= (row % 4) * 2;
      cell.southWall = ((value & 1) == 1);
      value >>>= 1;
      cell.southDoor = ((value & 1) == 1);
    }

    if (true)
    {
      value = HexFormatter.intValue (buffer[offset + 0]);
      value >>>= (row % 4) * 2;
      cell.eastWall = ((value & 1) == 1);
      value >>>= 1;
      cell.eastDoor = ((value & 1) == 1);
    }

    if (true)
    {
      value = HexFormatter.intValue (buffer[offset + 256]);
      value >>>= (row % 4) * 2;
      cell.northWall = ((value & 1) == 1);
      value >>>= 1;
      cell.northDoor = ((value & 1) == 1);
    }

    return cell;
  }

  private class MazeGrid
  {
    MazeCell[][] grid = new MazeCell[8][8];
    int xOffset;
    int yOffset;
  }
}