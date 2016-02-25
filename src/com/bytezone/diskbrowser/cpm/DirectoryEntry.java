package com.bytezone.diskbrowser.cpm;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.AppleDiskAddress;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class DirectoryEntry implements AppleFileSource
{
  private final CPMDisk parent;
  private final int userNumber;
  private final String name;
  private final String type;
  private final int ex;
  private final int s1;
  private final int s2;
  private final int rc;
  private final byte[] blockList = new byte[16];
  private final List<DirectoryEntry> entries = new ArrayList<DirectoryEntry> ();
  private final List<DiskAddress> blocks = new ArrayList<DiskAddress> ();

  public DirectoryEntry (CPMDisk parent, byte[] buffer, int offset)
  {
    this.parent = parent;
    userNumber = buffer[offset] & 0xFF;
    name = new String (buffer, offset + 1, 8).trim ();
    type = new String (buffer, offset + 9, 3).trim ();
    ex = buffer[offset + 12] & 0xFF;
    s2 = buffer[offset + 13] & 0xFF;
    s1 = buffer[offset + 14] & 0xFF;
    rc = buffer[offset + 15] & 0xFF;
    System.arraycopy (buffer, offset + 16, blockList, 0, 16);

    Disk disk = parent.getDisk ();
    for (byte b : blockList)
    {
      if (b == 0)
        break;

      int blockNumber = b * 4 + 48;
      for (int i = 0; i < 4; i++)
        blocks.add (new AppleDiskAddress (blockNumber + i, disk));
    }
  }

  public String getType ()
  {
    return type;
  }

  public boolean matches (DirectoryEntry directoryEntry)
  {
    return userNumber == directoryEntry.userNumber && name.equals (directoryEntry.name)
        && type.equals (directoryEntry.type);
  }

  public void add (DirectoryEntry entry)
  {
    entries.add (entry);

    Disk disk = parent.getDisk ();
    for (byte b : entry.blockList)
    {
      if (b == 0)
        break;

      int blockNumber = b * 4 + 48;
      for (int i = 0; i < 4; i++)
        blocks.add (new AppleDiskAddress (blockNumber + i, disk));
    }
  }

  public String line ()
  {
    int blocks = ((rc & 0xF0) >> 3) + (((rc & 0x0F) + 7) / 8);
    String bytes = HexFormatter.getHexString (blockList, 0, 16);
    bytes = bytes.replaceAll ("00", "  ");
    String text = String.format ("%3d   %-8s   %-3s   %3d   %3d    %s", userNumber, name,
                                 type, entries.size () + 1, blocks, bytes);
    for (DirectoryEntry entry : entries)
    {
      bytes = HexFormatter.getHexString (entry.blockList, 0, 16);
      bytes = bytes.replaceAll ("00", "  ");
      text = text + String.format ("%n%-36.36s%s", "", bytes);
    }
    return text;
  }

  public String toDetailedString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("User number .... %d%n", userNumber));
    text.append (String.format ("File name ...... %s%n", name + "." + type));
    text.append (String.format ("Extents lo ..... %d%n", ex));
    text.append (String.format ("Extents hi ..... %d%n", s2));
    text.append (String.format ("Reserved ....... %d%n", s1));

    int blocks = ((rc & 0xF0) >> 3) + (((rc & 0x0F) + 7) / 8);
    text.append (String.format ("Records ........ %02X  (%d)%n", rc, blocks));

    String bytes = HexFormatter.getHexString (blockList, 0, 16);
    text.append (String.format ("Allocation ..... %s%n", bytes));

    for (DirectoryEntry entry : entries)
    {
      bytes = HexFormatter.getHexString (entry.blockList, 0, 16);
      text.append (String.format ("                 %s%n", bytes));
    }

    return text.toString ();
  }

  @Override
  public String getUniqueName ()
  {
    return name + "." + type;
  }

  @Override
  public DataSource getDataSource ()
  {
    return null;
  }

  @Override
  public List<DiskAddress> getSectors ()
  {
    return blocks;
  }

  @Override
  public FormattedDisk getFormattedDisk ()
  {
    return parent;
  }

  @Override
  public String toString ()
  {
    return name + "." + type;
  }
}