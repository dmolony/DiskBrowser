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
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class MazeGridV5 extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private final MessageBlock messageBlock;
  List<MazeGrid> grids = new ArrayList<> ();
  int minX = 9999;
  int minY = 9999;
  int maxX = 0;
  int maxY = 0;

  // ---------------------------------------------------------------------------------//
  MazeGridV5 (String name, byte[] buffer, MessageBlock messageBlock)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.messageBlock = messageBlock;

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

  // ---------------------------------------------------------------------------------//
  @Override
  public BufferedImage getImage ()
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  private MazeCell getLayout (int gridNo, int row, int column)
  // ---------------------------------------------------------------------------------//
  {
    MazeAddress address = new MazeAddress (0, row, column);
    MazeCell cell = new MazeCell (address);

    int offset = gridNo * 16 + column * 2 + row / 4;
    int value;

    value = buffer[offset + 0] & 0xFF;
    value >>>= (row % 4) * 2;
    cell.eastWall = ((value & 1) == 1);
    value >>>= 1;
    cell.eastDoor = ((value & 1) == 1);

    value = buffer[offset + 256] & 0xFF;
    value >>>= (row % 4) * 2;
    cell.northWall = ((value & 1) == 1);
    value >>>= 1;
    cell.northDoor = ((value & 1) == 1);

    return cell;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getHexDump ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (super.getHexDump ());

    text.append ("\n\n");

    int offset = 0x220;
    for (int i = 0; i < 16; i++)
    {
      text.append (String.format ("%05X : ", offset + i * 4));
      for (int j = 0; j < 4; j++)
      {
        text.append (HexFormatter.getHexString (buffer, offset + j * 64 + i * 4, 4));
        text.append ("  ");
      }
      text.append ("\n");
    }

    text.append ("\n");

    offset = 0x320;
    for (int i = 0; i < 15; i++)
    {
      text.append (HexFormatter.format (buffer, offset + i * 10, 10));
      text.append ("\n");
    }
    text.append ("\n");

    offset = 0x400;
    for (int i = 0; i < 5; i++)
    {
      text.append (HexFormatter.format (buffer, offset + i * 64, 64));
      text.append ("\n\n");
    }

    text.append ("\n");
    for (int i = 0; i < 176; i += 2)
    {
      int msg = Utility.getWord (buffer, 0x540 + i);
      text.append (String.format ("%05X  %04X  %04X", 0x540 + i, i / 2, msg));
      if (msg >= 700)
      {
        List<String> messages = messageBlock.getMessageLines (msg);
        if (messages.size () > 0)
          text.append (String.format ("  %s%n", messages.get (0)));
        else
          text.append (String.format (" Message not found: %04X%n", msg));
      }
      else
        text.append ("\n");
    }

    text.append ("\n");
    offset = 0x5F0;
    for (int i = 0; i < 7; i++)
    {
      text.append (HexFormatter.format (buffer, offset + i * 64, 64));
      text.append ("\n\n");
    }

    if (false)
    {
      for (int i = 0; i < 176; i += 2)
      {
        int msg = Utility.getWord (buffer, 0x540 + i);
        if (msg >= 15000)
        {
          List<String> messages = messageBlock.getMessageLines (msg);
          if (messages.size () > 0)
            text.append (
                String.format ("%n%4d  %02X  %04X  %s%n", i, i, msg, messages.get (0)));
          else
            text.append (String.format ("Message not found: %04X%n", msg));

          for (int j = 1; j < messages.size (); j++)
            text.append (String.format ("                %s%n", messages.get (j)));
        }
      }
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private class MazeGrid
  // ---------------------------------------------------------------------------------//
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