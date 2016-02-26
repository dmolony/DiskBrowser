package com.bytezone.diskbrowser.cpm;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.CPMTextFile;
import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.disk.AppleDiskAddress;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class DirectoryEntry implements AppleFileSource
{
  private final Disk disk;
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
  private DataSource appleFile;

  public DirectoryEntry (CPMDisk parent, byte[] buffer, int offset)
  {
    this.parent = parent;
    disk = parent.getDisk ();

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
      {
        AppleDiskAddress da = new AppleDiskAddress (blockNumber + i, disk);
        blocks.add (da);
      }
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

  @Override
  public boolean contains (DiskAddress da)
  {
    for (DiskAddress sector : blocks)
      if (sector.compareTo (da) == 0)
        return true;
    return false;
  }

  public String line ()
  {
    int blocks = ((rc & 0xF0) >> 3) + (((rc & 0x0F) + 7) / 8);
    String bytes = HexFormatter.getHexString (blockList, 0, 16);
    bytes = bytes.replaceAll ("00", "  ");

    String text = String.format ("%3d   %-8s   %-3s   %02X   %02X   %02X   %02X   %s",
                                 userNumber, name, type, ex, s2, s1, rc, bytes);
    for (DirectoryEntry entry : entries)
      text = text + "\n" + entry.line ();

    if (ex != 0)
      text = "                    " + text.substring (20);

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
    if (appleFile != null)
      return appleFile;

    byte[] buffer = disk.readSectors (blocks);

    if (buffer.length == 0)
    {
      appleFile = new DefaultAppleFile (name, buffer);
      return appleFile;
    }

    DirectoryEntry entry = rc == 0x80 ? entries.get (entries.size () - 1) : this;
    int len = (entry.ex * 128 + entry.rc) * 128;

    byte[] exactBuffer = new byte[len];
    System.arraycopy (buffer, 0, exactBuffer, 0, len);

    int max = Math.min (256, exactBuffer.length);
    int count = 0;
    for (int i = 1; i < max; i++)
    {
      if (exactBuffer[i - 1] == 0x0D && exactBuffer[i] == 0x0A)
        ++count;
    }

    if ("ASM".equals (type) || "DOC".equals (type) || "TXT".equals (type) || count > 0)
      appleFile = new CPMTextFile (name, exactBuffer);
    else
      appleFile = new DefaultAppleFile (name, exactBuffer, "CPM File : " + type);

    return appleFile;
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