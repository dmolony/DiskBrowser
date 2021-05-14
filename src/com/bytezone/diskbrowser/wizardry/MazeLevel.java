package com.bytezone.diskbrowser.wizardry;

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
class MazeLevel extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  public final int level;
  private List<Message> messages;
  private List<Monster> monsters;
  private List<Item> items;

  // ---------------------------------------------------------------------------------//
  public MazeLevel (byte[] buffer, int level)
  // ---------------------------------------------------------------------------------//
  {
    super ("Level " + level, buffer);
    this.level = level;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getHexDump ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("West walls/doors\n\n");
    text.append (HexFormatter.format (buffer, 0, 120));
    addWalls (text, 0);

    text.append ("\nSouth walls/doors\n\n");
    text.append (HexFormatter.format (buffer, 120, 120));
    addWalls (text, 120);

    text.append ("\nEast walls/doors\n\n");
    text.append (HexFormatter.format (buffer, 240, 120));
    addWalls (text, 240);

    text.append ("\nNorth walls/doors\n\n");
    text.append (HexFormatter.format (buffer, 360, 120));
    addWalls (text, 360);

    text.append ("\nEncounters\n\n");
    text.append (HexFormatter.format (buffer, 480, 80));
    addEncounters (text, 480);

    text.append ("\nExtras\n\n");
    text.append (HexFormatter.format (buffer, 560, 200));
    addExtras (text, 560);

    text.append ("\nIndex\n\n");
    text.append (
        String.format ("%04X: %s%n", 760, HexFormatter.getHexString (buffer, 760, 8)));

    text.append ("\nTable\n\n");
    text.append (HexFormatter.format (buffer, 768, 96));

    text.append ("\n\n      0 1  2 3  4 5  6 7  8 9  A B  C D  E F\n");
    text.append (String.format ("%04X: ", 760));
    for (int i = 0; i < 8; i++)
    {
      int val = buffer[760 + i] & 0xFF;
      text.append (String.format ("%X:%X  ", val % 16, val / 16));
    }

    String[] extras =
        { "", "Stairs", "Pit", "Chute", "Spinner", "Darkness", "Teleport", "Ouch",
          "Elevator", "Rock/Water", "Fizzle", "Message/Item", "Monster" };

    List<MazeAddress> messageList = new ArrayList<> ();
    List<MazeAddress> monsterList = new ArrayList<> ();

    text.append ("\n\nValue   Index   Contains          Table\n");
    for (int j = 0; j < 16; j++)
    {
      String extraText = "";
      int val = buffer[760 + j / 2] & 0xFF;
      String extra = (j % 2) == 0 ? extras[val % 16] : extras[val / 16];
      MazeAddress address = getAddress (j);
      int cellFlag = (j % 2) == 0 ? val % 16 : val / 16;
      if (cellFlag == 11)
      {
        extraText = "Msg:" + String.format ("%04X  ", address.row);
        messageList.add (address);          // to print at the end

        int messageType = address.column;
        if (messageType == 2)
        {
          extraText += "Obtained: ";
          if (items != null)
            extraText += items.get (address.level).getName ();
        }

        if (messageType == 5)
        {
          extraText += "Requires: ";
          if (items != null)
            extraText += items.get (address.level).getName ();
        }

        if (messageType == 4)
        {
          extraText += "Unknown";
        }
      }

      if (cellFlag == 12)
      {
        monsterList.add (address);
        extraText = "Encounter: ";
        if (monsters != null)
          extraText += monsters.get (address.column).realName;
      }

      text.append (String.format ("  %X  -->  %X     %-15s   %04X  %04X  %04X  %s%n", j,
          cellFlag, extra, address.level, address.row, address.column, extraText));
    }

    text.append ("\n\nRest\n\n");
    text.append (HexFormatter.format (buffer, 864, buffer.length - 864));

    text.append ("\n");
    for (MazeAddress address : messageList)
    {
      Message message = getMessage (address.row);
      if (message != null)
      {
        text.append (String.format ("%nMessage: %04X  (%d)%n", address.row, address.row));
        text.append (message.getText ());
        text.append ("\n");
      }
    }

    for (MazeAddress address : monsterList)
    {
      Monster monster = getMonster (address.column);
      if (monster != null)
      {
        text.append (String.format ("%nMonster: %04X%n", address.column));
        text.append (monster.getText ());
        text.append ("\n");
      }
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private void addWalls (StringBuilder text, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    text.append ("\n\n");
    for (int i = 0; i < 20; i++)
      text.append (String.format ("  Col %2d: %s%n", i,
          HexFormatter.getHexString (buffer, ptr + i * 6, 6)));
  }

  // ---------------------------------------------------------------------------------//
  private void addEncounters (StringBuilder text, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    text.append ("\n\n");
    for (int i = 0; i < 20; i++)
    {
      text.append (String.format ("  Col %2d: %s  ", i,
          HexFormatter.getHexString (buffer, ptr + i * 4, 4)));
      StringBuilder bitString = new StringBuilder ();
      for (int j = 2; j >= 0; j--)
      {
        byte b = buffer[ptr + i * 4 + j];
        String s = ("0000000" + Integer.toBinaryString (0xFF & b))
            .replaceAll (".*(.{8})$", "$1");
        bitString.append (s);
        //        text.append (s);
        //        text.append ("  ");
      }

      String bitsReversed = bitString.reverse ().toString ();
      bitsReversed = bitsReversed.replace ("1", " 1");
      bitsReversed = bitsReversed.replace ("0", "  ");
      text.append (bitsReversed.substring (0, 40));
      text.append ("  : ");
      text.append (bitsReversed.substring (40));
      text.append ("\n");
    }
  }

  // ---------------------------------------------------------------------------------//
  private void addExtras (StringBuilder text, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    text.append ("\n\n");
    for (int i = 0; i < 20; i++)
    {
      text.append (String.format ("  Col %2d:  ", i));
      for (int j = 0; j < 10; j++)
      {
        int val = buffer[ptr + i * 10 + j] & 0xFF;
        int left = val / 16;          // 0:F
        int right = val % 16;         // 0:F
        text.append (String.format ("%X:%X  ", right, left));
      }
      text.append ("\n");
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public BufferedImage getImage ()
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  public void setMessages (List<Message> messages)
  // ---------------------------------------------------------------------------------//
  {
    this.messages = messages;
  }

  // ---------------------------------------------------------------------------------//
  public void setMonsters (List<Monster> monsters)
  // ---------------------------------------------------------------------------------//
  {
    this.monsters = monsters;
  }

  // ---------------------------------------------------------------------------------//
  public void setItems (List<Item> items)
  // ---------------------------------------------------------------------------------//
  {
    this.items = items;
  }

  // ---------------------------------------------------------------------------------//
  public MazeCell getLocation (int row, int column)
  // ---------------------------------------------------------------------------------//
  {
    MazeAddress address = new MazeAddress (level, row, column);
    MazeCell cell = new MazeCell (address);

    // doors and walls
    int BYTES_PER_COL = 6;        // 20 / 4 = 5 bytes + 1 wasted
    int CELLS_PER_BYTE = 4;       // 2 bits each
    int BITS_PER_ROW = 2;

    int offset = column * BYTES_PER_COL + row / CELLS_PER_BYTE;

    int value = buffer[offset] & 0xFF;
    value >>>= (row % CELLS_PER_BYTE) * BITS_PER_ROW;   // push 0, 2, 4, 6 bits
    cell.westWall = ((value & 1) == 1);                 // use rightmost bit
    value >>>= 1;                                       // push 1 bit
    cell.westDoor = ((value & 1) == 1);                 // use rightmost bit

    value = buffer[offset + 120] & 0xFF;
    value >>>= (row % CELLS_PER_BYTE) * BITS_PER_ROW;
    cell.southWall = ((value & 1) == 1);
    value >>>= 1;
    cell.southDoor = ((value & 1) == 1);

    value = buffer[offset + 240] & 0xFF;
    value >>>= (row % CELLS_PER_BYTE) * BITS_PER_ROW;
    cell.eastWall = ((value & 1) == 1);
    value >>>= 1;
    cell.eastDoor = ((value & 1) == 1);

    value = buffer[offset + 360] & 0xFF;
    value >>>= (row % CELLS_PER_BYTE) * BITS_PER_ROW;
    cell.northWall = ((value & 1) == 1);
    value >>>= 1;
    cell.northDoor = ((value & 1) == 1);

    // monster table
    BYTES_PER_COL = 4;        // 3 bytes + 1 wasted
    CELLS_PER_BYTE = 8;       // 1 bit each
    BITS_PER_ROW = 1;

    offset = column * BYTES_PER_COL + row / CELLS_PER_BYTE;  // 4 bytes/column, 1 bit/row
    value = buffer[offset + 480] & 0xFF;
    value >>>= row % 8;
    cell.monsterLair = ((value & 1) == 1);

    // square extra

    offset = column * 10 + row / 2;                       // 10 bytes/column, 4 bits/row
    value = buffer[offset + 560] & 0xFF;
    int b = (row % 2 == 0) ? value % 16 : value / 16;     // 0:F

    int c = buffer[760 + b / 2] & 0xFF;                   // 760:767
    int cellFlag = (b % 2 == 0) ? c % 16 : c / 16;

    switch (cellFlag)
    {
      case 0:         // normal
        break;

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

      case 7:       // ouchy
        cell.unknown = cellFlag;
        break;

      case 8:       // buttonz
        cell.elevator = true;
        MazeAddress elevatorAddress = getAddress (b);
        cell.elevatorTo = elevatorAddress.row;
        //            HexFormatter.intValue (buffer[800 + b * 2], buffer[801 + b * 2]);
        cell.elevatorFrom = elevatorAddress.column;
        //            HexFormatter.intValue (buffer[832 + b * 2], buffer[833 + b * 2]);
        break;

      case 9:       // rock/water
        cell.rock = true;
        break;

      case 10:      // fizzle
        cell.spellsBlocked = true;
        break;

      case 11:      // screen message
        MazeAddress messageAddress = getAddress (b);
        //   int messageNum = HexFormatter.intValue (buffer[800 + b * 2], buffer[801 + b * 2]);

        Message m = getMessage (messageAddress.row);
        if (m != null)
          cell.message = m;

        cell.messageType = messageAddress.column;
        //     HexFormatter.intValue (buffer[832 + b * 2], buffer[833 + b * 2]);

        int itemID = -1;

        if (cell.messageType == 2 && items != null)                 // obtain Item
        {
          //   itemID = HexFormatter.intValue (buffer[768 + b * 2], buffer[769 + b * 2]);
          itemID = messageAddress.level;
          cell.itemObtained = items.get (itemID);
        }

        if (cell.messageType == 5 && items != null)                 // requires Item
        {
          //  itemID = HexFormatter.intValue (buffer[768 + b * 2], buffer[769 + b * 2]);
          itemID = messageAddress.level;
          cell.itemRequired = items.get (itemID);
        }

        if (cell.messageType == 4)
        {
          //   value = HexFormatter.intValue (buffer[768 + b * 2], buffer[769 + b * 2]);
          itemID = messageAddress.level;
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
              cell.itemObtained = items.get (val);          // check this
            if (cell.itemObtained == null)
              System.out.printf ("Item %d not found%n", val);
          }
        }
        break;

      case 12:      // encounter
        MazeAddress monsterAddress = getAddress (b);
        //  cell.monsterID = HexFormatter.intValue (buffer[832 + b * 2], buffer[833 + b * 2]);
        cell.monsterID = monsterAddress.column;
        cell.monsters = monsters;
        break;

      default:
        System.out.println ("Unknown extra: " + cellFlag);
        cell.unknown = cellFlag;
        break;
    }

    return cell;
  }

  // ---------------------------------------------------------------------------------//
  private MazeAddress getAddress (int b)      // 0:F
  // ---------------------------------------------------------------------------------//
  {
    int x = b * 2;
    return new MazeAddress (Utility.unsignedShort (buffer, 768 + x),
        Utility.unsignedShort (buffer, 800 + x), Utility.unsignedShort (buffer, 832 + x));
  }

  // ---------------------------------------------------------------------------------//
  private Message getMessage (int messageNo)
  // ---------------------------------------------------------------------------------//
  {
    if (messages == null)
      return null;

    for (Message m : messages)
      if (m.match (messageNo))
        return m;

    return null;
  }

  // ---------------------------------------------------------------------------------//
  private Monster getMonster (int monsterNo)
  // ---------------------------------------------------------------------------------//
  {
    if (monsters == null)
      return null;

    for (Monster m : monsters)
      if (m.match (monsterNo))
        return m;

    return null;
  }

  // ---------------------------------------------------------------------------------//
  public int getRows ()
  // ---------------------------------------------------------------------------------//
  {
    return 20;
  }

  // ---------------------------------------------------------------------------------//
  public int getColumns ()
  // ---------------------------------------------------------------------------------//
  {
    return 20;
  }
  /*
   * Pascal code decompiled by Tom Ewers
   * 
   *         TWALL = (OPEN, WALL, DOOR, HIDEDOOR);
             
             TSQUARE = (NORMAL, STAIRS, PIT, CHUTE, SPINNER, DARK, TRANSFER,
                       OUCHY, BUTTONZ, ROCKWATE, FIZZLE, SCNMSG, ENCOUNTE);
              
             TMAZE = RECORD
               W : PACKED ARRAY[ 0..19] OF PACKED ARRAY[ 0..19] OF TWALL;
               S : PACKED ARRAY[ 0..19] OF PACKED ARRAY[ 0..19] OF TWALL;
               E : PACKED ARRAY[ 0..19] OF PACKED ARRAY[ 0..19] OF TWALL;
               N : PACKED ARRAY[ 0..19] OF PACKED ARRAY[ 0..19] OF TWALL;
               
               FIGHTS : PACKED ARRAY[ 0..19] OF PACKED ARRAY[ 0..19] OF 0..1;
               
               SQREXTRA : PACKED ARRAY[ 0..19] OF PACKED ARRAY[ 0..19] OF 0..15;
               
               SQRETYPE : PACKED ARRAY[ 0..15] OF TSQUARE;
               
               AUX0 : PACKED ARRAY[ 0..15] OF INTEGER;
               AUX1 : PACKED ARRAY[ 0..15] OF INTEGER;
               AUX2 : PACKED ARRAY[ 0..15] OF INTEGER;
               
               ENMYCALC : PACKED ARRAY[ 1..3] OF RECORD
                            MINENEMY : INTEGER;
                            MULTWORS : INTEGER;
                            WORSE01  : INTEGER;
                            RANGE0N  : INTEGER;
                            PERCWORS : INTEGER;
                          END;
            END;
   */
}