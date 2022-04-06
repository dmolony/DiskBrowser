package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// -----------------------------------------------------------------------------------//
class Reward extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  static String[] types = { "gold", "item" };
  static final int SEGMENT_LENGTH = 18;

  int id;
  int totalElements;

  List<RewardElement> elements;
  List<Item> items;
  List<Monster> goldMonsters = new ArrayList<> ();
  List<Monster> chestMonsters = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  Reward (String name, byte[] buffer, int id, List<Item> items)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.id = id;
    this.items = items;

    totalElements = buffer[4];
    elements = new ArrayList<> (totalElements);

    for (int i = 0; i < totalElements; i++)
    {
      byte[] buffer2 = new byte[SEGMENT_LENGTH];
      System.arraycopy (buffer, i * SEGMENT_LENGTH, buffer2, 0, SEGMENT_LENGTH);
      elements.add (new RewardElement (buffer2));
    }
  }

  // ---------------------------------------------------------------------------------//
  public void addMonster (Monster monster, int location)
  // ---------------------------------------------------------------------------------//
  {
    if (location == 0)
      goldMonsters.add (monster);
    else
      chestMonsters.add (monster);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return getText (true);
  }

  // ---------------------------------------------------------------------------------//
  public String getText (boolean showLinks)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    for (RewardElement re : elements)
      text.append (re.getDetail () + "\n");

    if (showLinks)
    {
      if (goldMonsters.size () > 0)
      {
        text.append ("Without chest:\n\n");
        for (Monster m : goldMonsters)
          text.append ("   " + m + "\n");
        text.append ("\n");
      }
      if (chestMonsters.size () > 0)
      {
        text.append ("With chest:\n\n");
        for (Monster m : chestMonsters)
          text.append ("   " + m + "\n");
      }
    }
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public String getDump ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    int seq = 0;
    for (RewardElement re : elements)
    {
      text.append (seq++ == 0 ? String.format ("%02X  :  ", id) : "       ");
      text.append (re + "\n");
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private class RewardElement
  // ---------------------------------------------------------------------------------//
  {
    int type;
    int odds;
    byte[] buffer;

    public RewardElement (byte[] buffer)
    {
      this.buffer = buffer;

      type = buffer[8];
      odds = buffer[6];
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();
      for (int i = 0; i < SEGMENT_LENGTH; i += 2)
        text.append (String.format ("%3d  ", buffer[i] & 0xFF));
      return text.toString ();
    }

    public String getDetail ()
    {
      StringBuilder text = new StringBuilder ();
      text.append ("Odds ............ " + odds + "%\n");

      switch (type)
      {
        case 0:
          text.append ("Gold ............ " + buffer[10] + "d" + buffer[12] + "\n");
          break;

        case 1:
          int lo = buffer[10] & 0xFF;
          int qty = buffer[16] & 0xFF;
          boolean title = true;
          String[] lineItem = new String[4];

          for (int i = lo, max = lo + qty; i <= max; i += lineItem.length)
          {
            String lineTitle = title ? "Items ..........." : "";
            title = false;
            for (int j = 0; j < lineItem.length; j++)
              lineItem[j] = i + j < items.size () ? items.get (i + j).getName () : "";
            text.append (String.format ("%-17s %-16s %-16s %-16s %-16s%n", lineTitle, lineItem[0],
                lineItem[1], lineItem[2], lineItem[3]));
          }
          break;

        default:
          System.out.println ("Unknown reward type " + type);
      }

      return text.toString ();
    }
  }
}