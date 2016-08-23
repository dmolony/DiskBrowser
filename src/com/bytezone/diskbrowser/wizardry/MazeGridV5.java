package com.bytezone.diskbrowser.wizardry;

import java.awt.Color;
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
  int minX = 9999;
  int minY = 9999;
  int maxX = 0;
  int maxY = 0;

  public MazeGridV5 (String name, byte[] buffer)
  {
    super (name, buffer);

    for (int i = 0; i < 16; i++)
    {
      MazeCell[][] grid = new MazeCell[8][8];
      for (int row = 0; row < 8; row++)
        for (int col = 0; col < 8; col++)
          grid[row][col] = getLayout (i, row, col);

      MazeGrid mazeGrid =
          new MazeGrid (grid, buffer[528 + i] & 0xFF, buffer[512 + i] & 0xFF);
      grids.add (mazeGrid);

      minX = Math.min (minX, mazeGrid.xOffset);
      minY = Math.min (minY, mazeGrid.yOffset);
      maxX = Math.max (maxX, mazeGrid.xOffset);
      maxY = Math.max (maxY, mazeGrid.yOffset);
    }
  }

  @Override
  public BufferedImage getImage ()
  {
    Dimension cellSize = new Dimension (22, 22);
    int fudge = 30;

    int gridWidth = (maxX - minX + 8) * cellSize.width;
    int gridHeight = (maxY - minY + 7) * cellSize.height;

    image = new BufferedImage (gridWidth + 1, gridHeight + fudge,
        BufferedImage.TYPE_USHORT_555_RGB);
    Graphics2D g = image.createGraphics ();
    g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    g.setColor (Color.LIGHT_GRAY);
    g.fillRect (0, 0, gridWidth + 1, gridHeight + fudge);

    for (int i = 0; i < 16; i++)
    {
      MazeGrid mazeGrid = grids.get (i);
      for (int row = 0; row < 8; row++)
        for (int column = 0; column < 8; column++)
        {
          MazeCell cell = mazeGrid.grid[row][column];
          int x = column * cellSize.width;
          int y = image.getHeight () - (row) * cellSize.height;
          x += (mazeGrid.xOffset - minX) * cellSize.width;
          y -= (mazeGrid.yOffset - minY) * cellSize.height + fudge;
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
    MazeCell[][] grid;
    int xOffset;
    int yOffset;

    public MazeGrid (MazeCell[][] grid, int x, int y)
    {
      this.grid = grid;
      this.xOffset = x;
      this.yOffset = y;
    }
  }
}