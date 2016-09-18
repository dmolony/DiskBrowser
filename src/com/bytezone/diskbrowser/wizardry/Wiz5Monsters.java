package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;
import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class Wiz5Monsters extends AbstractFile
{
  List<Monster> monsters = new ArrayList<Monster> ();

  public Wiz5Monsters (String name, byte[] buffer)
  {
    super (name, buffer);

    int ptr = 0;
    Monster monster = null;

    while (buffer[ptr] != 0)
    {
      int val1 = buffer[ptr] & 0xFF;
      int val2 = Utility.getWord (buffer, ptr * 2 + 256);
      int offset = val1 * 512 + val2;

      if (monster != null)
        fillMonster (offset, monster, ptr);

      monster = new Monster ();
      monster.offset = offset;        // don't know the length yet
      monsters.add (monster);

      ptr++;
    }

    fillMonster (buffer.length, monster, ptr);
  }

  private void fillMonster (int offset, Monster monster, int ptr)
  {
    int len = offset - monster.offset;
    monster.buffer = new byte[len];
    System.arraycopy (buffer, monster.offset, monster.buffer, 0, len);
    monster.image = new Wiz4Image ("Image " + ptr, monster.buffer, 3, 10, 6);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    int count = 0;
    for (Monster monster : monsters)
      text.append (String.format ("%02X : %02X %04X : %s%n", ++count,
          monster.offset / 512, monster.offset % 512,
          HexFormatter.getHexString (buffer, monster.offset, monster.buffer.length)));

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  class Monster
  {
    int offset;
    Wiz4Image image;
    byte[] buffer;
  }
}