package com.bytezone.diskbrowser.wizardry;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class MazeLevel extends AbstractFile
{
  public final int level;
  private List<Message> messages;
  private List<Monster> monsters;
  private List<Item> items;

  public MazeLevel (byte[] buffer, int level)
  {
    super ("Level " + level, buffer);
    this.level = level;
  }

  @Override
  public BufferedImage getImage ()
  {
    Dimension cellSize = new Dimension (22, 22);
    image = new BufferedImage (20 * cellSize.width + 1, 20 * cellSize.height + 1,
        BufferedImage.TYPE_USHORT_555_RGB);
    Graphics2D g = image.createGraphics ();
    g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    for (int row = 0; row < 20; row++)
      for (int column = 0; column < 20; column++)
      {
        MazeCell cell = getLocation (row, column);
        int x = column * cellSize.width;
        int y = image.getHeight () - (row + 1) * cellSize.height - 1;
        cell.draw (g, x, y);
      }
    return image;
  }

  public void setMessages (List<Message> messages)
  {
    this.messages = messages;
  }

  public void setMonsters (List<Monster> monsters)
  {
    this.monsters = monsters;
  }

  public void setItems (List<Item> items)
  {
    this.items = items;
  }

  public MazeCell getLocation (int row, int column)
  {
    MazeAddress address = new MazeAddress (level, row, column);
    MazeCell cell = new MazeCell (address);

    // doors and walls

    int offset = column * 6 + row / 4;                        // 6 bytes/column

    int value = HexFormatter.intValue (buffer[offset]);
    value >>>= (row % 4) * 2;
    cell.westWall = ((value & 1) == 1);
    value >>>= 1;
    cell.westDoor = ((value & 1) == 1);

    value = HexFormatter.intValue (buffer[offset + 120]);
    value >>>= (row % 4) * 2;
    cell.southWall = ((value & 1) == 1);
    value >>>= 1;
    cell.southDoor = ((value & 1) == 1);

    value = HexFormatter.intValue (buffer[offset + 240]);
    value >>>= (row % 4) * 2;
    cell.eastWall = ((value & 1) == 1);
    value >>>= 1;
    cell.eastDoor = ((value & 1) == 1);

    value = HexFormatter.intValue (buffer[offset + 360]);
    value >>>= (row % 4) * 2;
    cell.northWall = ((value & 1) == 1);
    value >>>= 1;
    cell.northDoor = ((value & 1) == 1);

    // monster table

    offset = column * 4 + row / 8;                          // 4 bytes/column, 1 bit/row
    value = HexFormatter.intValue (buffer[offset + 480]);
    value >>>= row % 8;
    cell.monsterLair = ((value & 1) == 1);

    // stairs, pits, darkness etc.

    offset = column * 10 + row / 2;                       // 10 bytes/column, 4 bits/row
    value = HexFormatter.intValue (buffer[offset + 560]);
    int b = (row % 2 == 0) ? value % 16 : value / 16;
    int c = HexFormatter.intValue (buffer[760 + b / 2]);
    int d = (b % 2 == 0) ? c % 16 : c / 16;

    switch (d)
    {
      case 1:
        cell.stairs = true;
        cell.addressTo = getAddress (b);
        break;

      case 2:
        cell.pit = true;
        break;

      case 3:
        cell.chute = true;
        cell.addressTo = getAddress (b);
        break;

      case 4:
        cell.spinner = true;
        break;

      case 5:
        cell.darkness = true;
        break;

      case 6:
        cell.teleport = true;
        cell.addressTo = getAddress (b);
        break;

      case 8:
        cell.elevator = true;
        cell.elevatorTo =
            HexFormatter.intValue (buffer[800 + b * 2], buffer[801 + b * 2]);
        cell.elevatorFrom =
            HexFormatter.intValue (buffer[832 + b * 2], buffer[833 + b * 2]);
        break;

      case 9:
        cell.rock = true;
        break;

      case 10:
        cell.spellsBlocked = true;
        break;

      case 11:
        int messageNum = HexFormatter.intValue (buffer[800 + b * 2], buffer[801 + b * 2]);
        if (messages != null)
        {
          for (Message m : messages)
            if (m.match (messageNum))
            {
              cell.message = m;
              break;
            }
          if (cell.message == null)
            System.out.println ("message not found : " + messageNum);
        }
        cell.messageType =
            HexFormatter.intValue (buffer[832 + b * 2], buffer[833 + b * 2]);

        int itemID = -1;

        if (cell.messageType == 2 && items != null)                 // obtain Item
        {
          itemID = HexFormatter.intValue (buffer[768 + b * 2], buffer[769 + b * 2]);
          cell.itemObtained = items.get (itemID);
        }

        if (cell.messageType == 5 && items != null)                  // requires Item
        {
          itemID = HexFormatter.intValue (buffer[768 + b * 2], buffer[769 + b * 2]);
          cell.itemRequired = items.get (itemID);
        }

        if (cell.messageType == 4)
        {
          value = HexFormatter.intValue (buffer[768 + b * 2], buffer[769 + b * 2]);
          if (value <= 100)
          {
            cell.monsterID = value;
            cell.monsters = monsters;
          }
          else
          {
            int val = (value - 64536) * -1;
            System.out.println ("Value : " + val);
            // this gives Index error: 20410, Size 104 in Wizardry_III/legacy2.dsk
            if (items != null && val < items.size ())
              cell.itemObtained = items.get (val); // check this
            if (cell.itemObtained == null)
              System.out.printf ("Item %d not found%n", val);
          }
        }
        break;

      case 12:
        cell.monsterID = HexFormatter.intValue (buffer[832 + b * 2], buffer[833 + b * 2]);
        cell.monsters = monsters;
        break;

      default:
        cell.unknown = d;
        break;
    }

    return cell;
  }

  private MazeAddress getAddress (int a)
  {
    int b = a * 2;
    return new MazeAddress (HexFormatter.intValue (buffer[768 + b], buffer[769 + b]),
        HexFormatter.intValue (buffer[800 + b], buffer[801 + b]),
        HexFormatter.intValue (buffer[832 + b], buffer[833 + b]));
  }

  public int getRows ()
  {
    return 20;
  }

  public int getColumns ()
  {
    return 20;
  }
}