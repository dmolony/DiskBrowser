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

    int p = 0;
    int nextBlock = buffer[0] & 0xFF;
    int nextOffset = Utility.getWord (buffer, 256);
    Monster monster = new Monster (1, nextBlock * 512 + nextOffset);
    monsters.add (monster);
    boolean createMonster = false;

    while (nextBlock > 0)
    {
      int firstBlock = nextBlock;
      int firstOffset = nextOffset;

      int ndx = nextBlock * 512 + nextOffset;

      if (buffer[ndx] == (byte) 0)
      {
        nextBlock = buffer[++p] & 0xFF;
        nextOffset = Utility.getWord (buffer, p * 2 + 256);

        createMonster = true;
      }
      else
      {
        nextBlock = buffer[ndx] & 0xFF;
        nextOffset = Utility.getWord (buffer, ndx + 1);
      }

      int length = nextOffset > 0 ? nextOffset : 512 - firstOffset;

      Buffer monsterBuffer = new Buffer (firstBlock, firstOffset, length);
      monster.buffers.add (monsterBuffer);
      //      System.out.println (monsterBuffer);

      if (createMonster && nextBlock > 0)
      {
        createMonster = false;
        monster = new Monster (p + 1, nextBlock * 512 + nextOffset);
        monsters.add (monster);
      }
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    for (Monster monster : monsters)
    {
      text.append (
          String.format ("%02X : %02X %04X : %s%n", monster.id, monster.offset / 512,
              monster.offset % 512, monster.buffers.get (0).toHexString ()));
      for (int i = 1; i < monster.buffers.size (); i++)
      {
        Buffer monsterBuffer = monster.buffers.get (i);
        text.append (String.format ("             : %s%n", monsterBuffer.toHexString ()));
      }
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  class Monster
  {
    int id;
    int offset;
    int length;
    Wiz4Image image;
    byte[] data;

    List<Buffer> buffers = new ArrayList<Buffer> ();

    public Monster (int id, int offset)
    {
      this.id = id;
      this.offset = offset;
    }

    public Wiz4Image getImage ()
    {
      if (image == null)
      {
        length = 0;
        for (Buffer monsterBuffer : buffers)
          length += monsterBuffer.length - 3;

        data = new byte[length];

        int ptr = 0;
        for (Buffer monsterBuffer : buffers)
        {
          int offset = monsterBuffer.block * 512 + monsterBuffer.offset + 3;
          System.arraycopy (buffer, offset, data, ptr, monsterBuffer.length - 3);
          ptr += monsterBuffer.length - 3;
        }
        image = new Wiz4Image ("Image " + id, data, 8, 8);
      }
      return image;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      for (Buffer monsterBuffer : buffers)
      {
        text.append (monsterBuffer);
        text.append ("\n");
      }

      return text.toString ();
    }
  }

  class Buffer
  {
    int block;
    int offset;
    int length;

    public Buffer (int block, int offset, int length)
    {
      this.block = block;
      this.offset = offset;
      this.length = length;
    }

    public String toHexString ()
    {
      return HexFormatter.getHexString (buffer, block * 512 + offset, length);
    }

    @Override
    public String toString ()
    {
      return (HexFormatter.format (buffer, block * 512 + offset, length));
    }
  }
}