package com.bytezone.diskbrowser.wizardry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class MazeCell
// -----------------------------------------------------------------------------------//
{
  static final Dimension cellSize = new Dimension (22, 22);       // size in pixels
  static final Color SECRET_DOOR = Color.RED;
  static final Color DOOR = Color.GREEN;

  boolean northWall;
  boolean southWall;
  boolean eastWall;
  boolean westWall;

  boolean northDoor;
  boolean southDoor;
  boolean eastDoor;
  boolean westDoor;

  boolean darkness;

  boolean stairs;
  boolean pit;
  boolean spinner;
  boolean chute;
  boolean elevator;
  boolean monsterLair;
  boolean rock;
  boolean teleport;
  boolean spellsBlocked;

  int elevatorFrom;
  int elevatorTo;

  int messageType;
  int monsterID = -1;
  int itemID;

  int unknown;

  MazeAddress address;
  MazeAddress addressTo;                    // if teleport/stairs/chute

  public Message message;
  public List<Monster> monsters;
  public Item itemRequired;
  public Item itemObtained;

  // ---------------------------------------------------------------------------------//
  MazeCell (MazeAddress address)
  // ---------------------------------------------------------------------------------//
  {
    this.address = address;
  }

  // ---------------------------------------------------------------------------------//
  void draw (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.setColor (Color.BLACK);
    g.fillRect (x, y, cellSize.width, cellSize.height);

    g.setColor (Color.WHITE);

    if (westWall)
      drawWest (g, x, y);
    if (eastWall)
      drawEast (g, x, y);
    if (northWall)
      drawNorth (g, x, y);
    if (southWall)
      drawSouth (g, x, y);

    g.setColor (DOOR);

    if (westDoor)
      drawWest (g, x, y);
    if (eastDoor)
      drawEast (g, x, y);
    if (northDoor)
      drawNorth (g, x, y);
    if (southDoor)
      drawSouth (g, x, y);

    g.setColor (SECRET_DOOR);

    if (westDoor && westWall)
      drawWest (g, x, y);
    if (eastDoor && eastWall)
      drawEast (g, x, y);
    if (northDoor && northWall)
      drawNorth (g, x, y);
    if (southDoor && southWall)
      drawSouth (g, x, y);

    g.setColor (Color.WHITE);

    if (monsterLair)
      drawMonsterLair (g, x, y);

    if (stairs)
      if (address.level < addressTo.level)
        drawStairsDown (g, x, y);
      else
        drawStairsUp (g, x, y);
    else if (message != null)
      drawChar (g, x, y, "M", Color.RED);
    else if (pit)
      drawPit (g, x, y);
    else if (chute)
      drawChute (g, x, y);
    else if (spinner)
      g.drawString ("S", x + 8, y + 16);
    else if (teleport)
      drawTeleport (g, x, y);
    else if (darkness)
      drawDarkness (g, x, y);
    else if (rock)
      drawRock (g, x, y);
    else if (elevator)
      drawElevator (g, x, y, (elevatorTo - elevatorFrom + 1) / 2);
    else if (monsterID >= 0)
      drawMonster (g, x, y);
    else if (spellsBlocked)
      drawSpellsBlocked (g, x, y);
    else if (unknown != 0)
      drawChar (g, x, y, HexFormatter.format1 (unknown), Color.GRAY);
  }

  // ---------------------------------------------------------------------------------//
  void drawWest (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.drawLine (x + 1, y + 1, x + 1, y + cellSize.height - 1);
  }

  // ---------------------------------------------------------------------------------//
  void drawEast (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.drawLine (x + cellSize.width - 1, y + 1, x + cellSize.width - 1, y + cellSize.height - 1);
  }

  // ---------------------------------------------------------------------------------//
  void drawNorth (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.drawLine (x + 1, y + 1, x + cellSize.width - 1, y + 1);
  }

  // ---------------------------------------------------------------------------------//
  void drawSouth (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.drawLine (x + 1, y + cellSize.height - 1, x + cellSize.width - 1, y + cellSize.height - 1);
  }

  // ---------------------------------------------------------------------------------//
  void drawStairsUp (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.drawLine (x + 6, y + 18, x + 6, y + 14);
    g.drawLine (x + 6, y + 14, x + 10, y + 14);
    g.drawLine (x + 10, y + 14, x + 10, y + 10);
    g.drawLine (x + 10, y + 10, x + 14, y + 10);
    g.drawLine (x + 14, y + 10, x + 14, y + 6);
    g.drawLine (x + 14, y + 6, x + 18, y + 6);
  }

  // ---------------------------------------------------------------------------------//
  void drawStairsDown (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.drawLine (x + 4, y + 7, x + 8, y + 7);
    g.drawLine (x + 8, y + 7, x + 8, y + 11);
    g.drawLine (x + 8, y + 11, x + 12, y + 11);
    g.drawLine (x + 12, y + 11, x + 12, y + 15);
    g.drawLine (x + 12, y + 15, x + 16, y + 15);
    g.drawLine (x + 16, y + 15, x + 16, y + 19);
  }

  // ---------------------------------------------------------------------------------//
  void drawPit (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.drawLine (x + 5, y + 14, x + 5, y + 19);
    g.drawLine (x + 5, y + 19, x + 17, y + 19);
    g.drawLine (x + 17, y + 14, x + 17, y + 19);
  }

  // ---------------------------------------------------------------------------------//
  void drawChute (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.drawLine (x + 6, y + 6, x + 10, y + 6);
    g.drawLine (x + 10, y + 6, x + 18, y + 18);
  }

  // ---------------------------------------------------------------------------------//
  void drawElevator (Graphics2D g, int x, int y, int rows)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < rows; i++)
    {
      g.drawOval (x + 7, y + i * 5 + 5, 2, 2);
      g.drawOval (x + 14, y + i * 5 + 5, 2, 2);
    }
  }

  // ---------------------------------------------------------------------------------//
  void drawMonsterLair (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.setColor (Color.YELLOW);
    g.fillOval (x + 4, y + 4, 2, 2);
    g.setColor (Color.WHITE);
  }

  // ---------------------------------------------------------------------------------//
  void drawTeleport (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.setColor (Color.GREEN);
    g.fillOval (x + 8, y + 8, 8, 8);
    g.setColor (Color.WHITE);
  }

  // ---------------------------------------------------------------------------------//
  void drawSpellsBlocked (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.setColor (Color.YELLOW);
    g.fillOval (x + 8, y + 8, 8, 8);
    g.setColor (Color.WHITE);
  }

  // ---------------------------------------------------------------------------------//
  void drawMonster (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.setColor (Color.RED);
    g.fillOval (x + 8, y + 8, 8, 8);
    g.setColor (Color.WHITE);
  }

  // ---------------------------------------------------------------------------------//
  void drawDarkness (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.setColor (Color.gray);
    for (int h = 0; h < 15; h += 7)
      for (int offset = 0; offset < 15; offset += 7)
        g.drawOval (x + offset + 4, y + h + 4, 1, 1);
    g.setColor (Color.white);
  }

  // ---------------------------------------------------------------------------------//
  void drawRock (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    for (int h = 0; h < 15; h += 7)
      for (int offset = 0; offset < 15; offset += 7)
        g.drawOval (x + offset + 4, y + h + 4, 1, 1);
  }

  // ---------------------------------------------------------------------------------//
  void drawChar (Graphics2D g, int x, int y, String c, Color colour)
  // ---------------------------------------------------------------------------------//
  {
    g.setColor (colour);
    g.fillRect (x + 7, y + 6, 11, 11);
    g.setColor (Color.WHITE);
    g.drawString (c, x + 8, y + 16);
  }

  // ---------------------------------------------------------------------------------//
  void drawHotDogStand (Graphics2D g, int x, int y)
  // ---------------------------------------------------------------------------------//
  {
    g.drawRect (x + 5, y + 11, 12, 6);
    g.drawOval (x + 6, y + 18, 3, 3);
    g.drawOval (x + 13, y + 18, 3, 3);
    g.drawLine (x + 8, y + 6, x + 8, y + 10);
    g.drawLine (x + 14, y + 6, x + 14, y + 10);
    g.drawLine (x + 5, y + 5, x + 17, y + 5);
  }

  // ---------------------------------------------------------------------------------//
  String getTooltipText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder sign = new StringBuilder ("<html><pre>");
    sign.append ("&nbsp;<b>");
    sign.append (address.row + "N ");
    sign.append (address.column + "E</b>&nbsp;<br>");

    if (message != null)
      sign.append (message.toHTMLString ());

    if (elevator)
      sign.append ("&nbsp;Elevator: L" + elevatorFrom + "-L" + elevatorTo + "&nbsp;");
    if (stairs)
    {
      sign.append ("&nbsp;Stairs to ");
      if (addressTo.level == 0)
        sign.append ("castle&nbsp;");
      else
      {
        sign.append ("level " + addressTo.level + "&nbsp;");
      }
    }
    if (teleport)
    {
      sign.append ("&nbsp;Teleport to ");
      if (addressTo.level == 0)
        sign.append ("castle&nbsp;");
      else
      {
        sign.append (
            "L" + addressTo.level + " " + addressTo.row + "N " + addressTo.column + "E&nbsp;");
      }
    }
    if (pit)
      sign.append ("&nbsp;Pit");
    if (spinner)
      sign.append ("&nbsp;Spinner&nbsp;");
    if (chute)
      sign.append ("&nbsp;Chute");
    if (darkness)
      sign.append ("&nbsp;Darkness&nbsp;");
    if (rock)
      sign.append ("&nbsp;Rock&nbsp;");
    if (spellsBlocked)
      sign.append ("&nbsp;Spells fizzle out&nbsp;");
    if (monsterID >= 0)
      if (monsters == null || monsterID >= monsters.size ())
        sign.append ("&nbsp;Monster&nbsp;");
      else
      {
        Monster monster = monsters.get (monsterID);
        sign.append ("&nbsp;<b>" + monster.getRealName () + "&nbsp;</b>");
        while (monster.partnerOdds == 100)
        {
          monster = monsters.get (monster.partnerID);
          sign.append ("<br>&nbsp;<b>" + monster.getRealName () + "&nbsp;</b>");
        }
      }
    if (itemRequired != null)
    {
      sign.append ("&nbsp;<b>Requires: ");
      sign.append (itemRequired.getName () + "&nbsp;</b>");
    }

    if (itemObtained != null)
    {
      sign.append ("&nbsp;<b>Obtain: ");
      sign.append (itemObtained.getName () + "&nbsp;</b>");
    }
    sign.append ("</pre></html>");
    return sign.toString ();
  }
}