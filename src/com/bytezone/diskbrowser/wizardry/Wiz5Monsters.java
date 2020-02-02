package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.common.Utility;
import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class Wiz5Monsters extends AbstractFile implements Iterable<Wiz5Monsters.Monster>
{
  private static final int BLOCK_SIZE = 512;
  private final List<Monster> monsters = new ArrayList<> ();

  public Wiz5Monsters (String name, byte[] buffer)
  {
    super (name, buffer);

    int p = 0;
    int nextBlock = buffer[p] & 0xFF;
    int nextOffset = Utility.getWord (buffer, 256);

    Monster monster = new Monster (p + 1);
    monsters.add (monster);
    boolean createMonster = false;

    while (nextBlock > 0)
    {
      int firstBlock = nextBlock;
      int firstOffset = nextOffset;

      int ndx = nextBlock * BLOCK_SIZE + nextOffset;

      if (buffer[ndx] != (byte) 0)
      {
        nextBlock = buffer[ndx] & 0xFF;
        nextOffset = Utility.getWord (buffer, ndx + 1);
      }
      else
      {
        nextBlock = buffer[++p] & 0xFF;
        nextOffset = Utility.getWord (buffer, p * 2 + 256);
        createMonster = true;
      }

      int length = nextOffset > 0 ? nextOffset : BLOCK_SIZE - firstOffset;
      monster.dataBuffers.add (new DataBuffer (firstBlock, firstOffset, length));

      if (createMonster && nextBlock > 0)
      {
        monster = new Monster (p + 1);
        monsters.add (monster);
        createMonster = false;
      }
    }
  }

  @Override
  public Iterator<Monster> iterator ()
  {
    return monsters.iterator ();
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    for (Monster monster : monsters)
    {
      DataBuffer dataBuffer = monster.dataBuffers.get (0);
      text.append (String.format ("%02X : %02X %04X : %s%n", monster.id, dataBuffer.block,
          dataBuffer.offset, dataBuffer.toHexString ()));
      for (int i = 1; i < monster.dataBuffers.size (); i++)
      {
        dataBuffer = monster.dataBuffers.get (i);
        text.append (String.format ("     %02X %04X : %s%n", dataBuffer.block,
            dataBuffer.offset, dataBuffer.toHexString ()));
      }
      text.append ("\n");
    }

    if (text.length () > 1)
    {
      text.deleteCharAt (text.length () - 1);
      text.deleteCharAt (text.length () - 1);
    }

    return text.toString ();
  }

  class Monster
  {
    private final int id;
    private final List<DataBuffer> dataBuffers = new ArrayList<> ();

    private Wiz4Image image;
    private byte[] data;

    public Monster (int id)
    {
      this.id = id;
    }

    public Wiz4Image getImage ()
    {
      if (image == null)
      {
        int length = 0;
        for (DataBuffer dataBuffer : dataBuffers)
          length += dataBuffer.length - 3;

        data = new byte[length];

        int ptr = 0;
        for (DataBuffer dataBuffer : dataBuffers)
        {
          int offset = dataBuffer.block * BLOCK_SIZE + dataBuffer.offset + 3;
          System.arraycopy (buffer, offset, data, ptr, dataBuffer.length - 3);
          ptr += dataBuffer.length - 3;
        }
        image = new Wiz4Image ("Image " + id, data, 10, 6);
        //        System.out.printf ("Monster# %d%n", id);
        //        System.out.println (Utility.toHex (data));
        //        System.out.println ();
      }
      return image;
    }

    List<Integer> getBlocks ()
    {
      List<Integer> blocks = new ArrayList<> ();
      for (DataBuffer dataBuffer : dataBuffers)
        blocks.add (dataBuffer.block);
      return blocks;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      for (DataBuffer dataBuffer : dataBuffers)
      {
        text.append (dataBuffer);
        text.append ("\n");
      }

      return text.toString ();
    }
  }

  class DataBuffer
  {
    private final int block;
    private final int offset;
    private final int length;

    public DataBuffer (int block, int offset, int length)
    {
      this.block = block;
      this.offset = offset;
      this.length = length;
    }

    public String toHexString ()
    {
      int p1 = block * BLOCK_SIZE + offset;
      String s1 = HexFormatter.getHexString (buffer, p1, 3);
      String s2 = HexFormatter.getHexString (buffer, p1 + 3, length - 3);
      //      split (p1 + 3, length - 3);
      return s1 + " : " + s2;
    }

    //    private void split (int offset, int length)
    //    {
    //      for (int p = offset; length > 0; p += 16, length -= 16)
    //      {
    //        int len = length > 15 ? 16 : length;
    //        String s = HexFormatter.getHexString (buffer, p, len);
    //        System.out.println (s);
    //      }
    //      System.out.println ();
    //    }

    @Override
    public String toString ()
    {
      return (HexFormatter.format (buffer, block * BLOCK_SIZE + offset, length));
    }
  }
}