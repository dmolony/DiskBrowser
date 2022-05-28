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
public class MazeLevel extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private static final int AUX0 = 768;
  private static final int AUX1 = 800;
  private static final int AUX2 = 832;

  private static final String[] squareType =
      { "Normal", "Stairs", "Pit", "Chute", "Spinner", "Darkness", "Teleport", "Ouch", "Elevator",
          "Rock/Water", "Fizzle", "Message/Item", "Monster" };

  public final int level;
  private List<MessageV1> messages;
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
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("West walls/doors\n\n");
    text.append (HexFormatter.format (buffer, 0, 120, true, 0));
    addWalls (text, 0);

    text.append ("\nSouth walls/doors\n\n");
    text.append (HexFormatter.format (buffer, 120, 120, true, 120));
    addWalls (text, 120);

    text.append ("\nEast walls/doors\n\n");
    text.append (HexFormatter.format (buffer, 240, 120, true, 240));
    addWalls (text, 240);

    text.append ("\nNorth walls/doors\n\n");
    text.append (HexFormatter.format (buffer, 360, 120, true, 360));
    addWalls (text, 360);

    text.append ("\nFIGHTS\n\n");
    text.append (HexFormatter.format (buffer, 480, 80, true, 480));
    addEncounters (text, 480);

    text.append ("\nSQREXTRA\n\n");
    text.append (HexFormatter.format (buffer, 560, 200, true, 560));
    addExtras (text, 560);

    text.append ("\nSQRTYPE\n\n");
    text.append (String.format ("%04X: %s%n", 760, HexFormatter.getHexString (buffer, 760, 8)));

    text.append ("\nAUX0\n\n");
    text.append (HexFormatter.format (buffer, AUX0, 32, true, AUX0));
    text.append ("\n");

    text.append ("\nAUX1\n\n");
    text.append (HexFormatter.format (buffer, AUX1, 32, true, AUX1));
    text.append ("\n");

    text.append ("\nAUX2\n\n");
    text.append (HexFormatter.format (buffer, AUX2, 32, true, AUX2));
    text.append ("\n");

    List<MazeAddress> messageList = new ArrayList<> ();
    List<MazeAddress> monsterList = new ArrayList<> ();

    addTable (text, messageList, monsterList);

    text.append ("\n\nENMYCALC\n\n");
    text.append (HexFormatter.format (buffer, 864, buffer.length - 864, true, 864));
    addEnmyCalc (text, 864);

    text.append ("\n\n");
    for (MazeAddress address : messageList)
    {
      MessageV1 message = getMessage (address.row);
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

    for (int col = 0; col < 20; col++)
    {
      text.append (
          String.format ("   Col %2d : %s : ", col, HexFormatter.getHexString (buffer, ptr, 6)));

      for (int i = 0; i < 5; i++)
      {
        int val = buffer[ptr++] & 0xFF;
        for (int j = 0; j < 4; j++)
        {
          int wall = (val & 0x03);                   // right to left ordering
          text.append (String.format ("%d ", wall));
          val >>>= 2;
        }
      }

      //      assert buffer[ptr] == 0;
      ptr++;                        // skip last byte
      text.append ("\n");
    }
  }

  // ---------------------------------------------------------------------------------//
  private void addEncounters (StringBuilder text, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    text.append ("\n\n");
    for (int i = 0; i < 20; i++)
    {
      text.append (
          String.format ("  Col %2d: %s  ", i, HexFormatter.getHexString (buffer, ptr + i * 4, 4)));
      int val = Utility.readTriple (buffer, ptr + i * 4);
      for (int j = 0; j < 20; j++)
      {
        text.append ((val & 0x01) == 0 ? "  " : " 1");
        val >>>= 1;
      }

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
        int val = buffer[ptr++] & 0xFF;
        int left = (val & 0xF0) >> 4;
        int right = (val & 0x0F);
        String sLeft = left == 0 ? " " : String.format ("%X", left);
        String sRight = right == 0 ? " " : String.format ("%X", right);
        text.append (String.format ("%s %s ", sRight, sLeft));
      }
      text.append ("\n");
    }
  }

  // ---------------------------------------------------------------------------------//
  private void addTable (StringBuilder text, List<MazeAddress> messageList,
      List<MazeAddress> monsterList)
  // ---------------------------------------------------------------------------------//
  {
    if (monsters == null)
    {
      System.out.println ("monsters is null");
      return;
    }

    text.append ("\n\nSQREXTRA  SQRTYPE   TSQUARE           AUX0  AUX1  AUX2\n");
    for (int j = 0; j < 16; j++)
    {
      String extraText = "";
      int val = buffer[760 + j / 2] & 0xFF;
      String extra = (j % 2) == 0 ? squareType[val % 16] : squareType[val / 16];

      MazeAddress address = getAddress (j);
      int cellFlag = (j % 2) == 0 ? val % 16 : val / 16;
      if (cellFlag == 11)
      {
        extraText = "Msg:" + String.format ("%04X  ", address.row);
        messageList.add (address);          // to print at the end

        int messageType = address.column;       // AUX3
        if (messageType == 2)
        {
          extraText += "Obtained: ";
          if (items != null)
            extraText += items.get (address.level).getName ();
        }

        if (messageType == 4)
        {
          if (address.level < monsters.size ())
            extraText += monsters.get (address.level).realName;
          else
            extraText += "Obtained: " + items.get ((address.level - 64536) * -1).getName ();
        }

        if (messageType == 5)
        {
          extraText += "Requires: ";
          if (items != null)
            extraText += items.get (address.level).getName ();
        }
      }

      if (cellFlag == 12)
      {
        monsterList.add (address);
        extraText = "Encounter: ";
        if (monsters != null)
          extraText += monsters.get (address.column).realName;
      }

      text.append (String.format ("    %X   -->   %X     %-15s   %04X  %04X  %04X  %s%n", j,
          cellFlag, extra, address.level, address.row, address.column, extraText));
    }
  }

  // ---------------------------------------------------------------------------------//
  private void addEnmyCalc (StringBuilder text, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    text.append ("\n\n");
    int savePtr = ptr;

    text.append (String.format ("MINENEMY   %04X  %04X  %04X%n", Utility.getShort (buffer, ptr),
        Utility.getShort (buffer, ptr + 10), Utility.getShort (buffer, ptr + 20)));
    ptr += 2;
    text.append (String.format ("MULTWORS   %04X  %04X  %04X%n", Utility.getShort (buffer, ptr),
        Utility.getShort (buffer, ptr + 10), Utility.getShort (buffer, ptr + 20)));
    ptr += 2;
    text.append (String.format ("WORSE01    %04X  %04X  %04X%n", Utility.getShort (buffer, ptr),
        Utility.getShort (buffer, ptr + 10), Utility.getShort (buffer, ptr + 20)));
    ptr += 2;
    text.append (String.format ("RANGE0N    %04X  %04X  %04X%n", Utility.getShort (buffer, ptr),
        Utility.getShort (buffer, ptr + 10), Utility.getShort (buffer, ptr + 20)));
    ptr += 2;
    text.append (String.format ("PERCWORS   %04X  %04X  %04X%n", Utility.getShort (buffer, ptr),
        Utility.getShort (buffer, ptr + 10), Utility.getShort (buffer, ptr + 20)));

    ptr = savePtr;
    for (int i = 0; i < 3; i++)
    {
      String pct = i == 0 ? "75" : i == 1 ? "18.75" : "6.25";
      text.append (String.format ("%nEnemy #%d  %s%%%n%n", (i + 1), pct));
      int minenemy = Utility.getShort (buffer, ptr);
      int multwors = Utility.getShort (buffer, ptr + 2);
      int worse01 = Utility.getShort (buffer, ptr + 4);
      int range0n = Utility.getShort (buffer, ptr + 6);
      int percwors = Utility.getShort (buffer, ptr + 8);
      ptr += 10;

      int max = multwors * worse01;

      for (int id = minenemy; id < minenemy + range0n + max; id++)
      {
        if (id == minenemy + range0n)
          text.append ("\n");
        Monster monster = monsters == null ? null : monsters.get (id);
        text.append (String.format ("%3d  %-16s  %n", id, monster));
      }
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
    g.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
  public void setMessages (List<MessageV1> messages)
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
    value >>>= (row % CELLS_PER_BYTE) * BITS_PER_ROW;   // shift 0, 2, 4, 6 bits
    cell.westWall = ((value & 1) == 1);                 // use rightmost bit
    value >>>= 1;                                       // shift 1 bit
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

      case 1:         // stairs
        cell.stairs = true;
        cell.addressTo = getAddress (b);
        break;

      case 2:         // pit
        cell.pit = true;
        break;

      case 3:         // chute
        cell.chute = true;
        cell.addressTo = getAddress (b);
        break;

      case 4:         // spinner
        cell.spinner = true;
        break;

      case 5:         // dark
        cell.darkness = true;
        break;

      case 6:         // transfer
        cell.teleport = true;
        cell.addressTo = getAddress (b);
        break;

      case 7:         // ouchy
        cell.unknown = cellFlag;
        break;

      case 8:         // buttonz
        cell.elevator = true;
        MazeAddress elevatorAddress = getAddress (b);
        cell.elevatorTo = elevatorAddress.row;
        cell.elevatorFrom = elevatorAddress.column;
        break;

      case 9:         // rock/water
        cell.rock = true;
        break;

      case 10:        // fizzle
        cell.spellsBlocked = true;
        break;

      case 11:        // screen message
        MazeAddress messageAddress = getAddress (b);
        MessageV1 m = getMessage (messageAddress.row);
        if (m != null)
          cell.message = m;

        cell.messageType = messageAddress.column;

        switch (cell.messageType)
        {
          case 2:                 // obtain Item
            if (items != null)
              cell.itemObtained = items.get (messageAddress.level);
            break;

          case 4:
            int itemID = messageAddress.level;
            if (itemID <= 100)
            {
              cell.monsterID = itemID;
              cell.monsters = monsters;
            }
            else
            {
              int val = (itemID - 64536) * -1;
              // this gives Index error: 20410, Size 104 in Wizardry_III/legacy2.dsk
              if (items != null && val < items.size ())
                cell.itemObtained = items.get (val);          // check this
              if (cell.itemObtained == null)
                System.out.printf ("Item %d (%d) not found on level %d%n", val, value, level);
            }
            break;

          case 5:                // requires Item
            if (items != null)
              cell.itemRequired = items.get (messageAddress.level);
            break;
        }

        break;

      case 12:      // encounter
        MazeAddress monsterAddress = getAddress (b);
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
    // converts AUX1, AUX2, AUX3 into a MazeAddress (level, row, column)
    b *= 2;
    return new MazeAddress (Utility.getShort (buffer, 768 + b), Utility.getShort (buffer, 800 + b),
        Utility.getShort (buffer, 832 + b));
  }

  // ---------------------------------------------------------------------------------//
  private MessageV1 getMessage (int messageNo)
  // ---------------------------------------------------------------------------------//
  {
    if (messages == null)
      return null;

    for (MessageV1 m : messages)
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