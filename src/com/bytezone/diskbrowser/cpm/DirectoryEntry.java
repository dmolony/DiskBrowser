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

// File Control Block (FCB)
// -----------------------------------------------------------------------------------//
class DirectoryEntry implements AppleFileSource
// -----------------------------------------------------------------------------------//
{
  private final Disk disk;
  private final CPMDisk parent;
  private DataSource appleFile;

  private final int userNumber;
  private final String name;
  private final String type;
  private final int extent;
  private final int s1;                               // reserved
  private final int s2;                               // reserved
  private final int recordsUsed;                      // records used in this extent
  private final byte[] blockList = new byte[16];      // allocation blocks used

  private final List<DirectoryEntry> entries = new ArrayList<> ();
  private final List<DiskAddress> blocks = new ArrayList<> ();
  private final boolean readOnly;
  private final boolean systemFile;

  // ---------------------------------------------------------------------------------//
  DirectoryEntry (CPMDisk parent, byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    this.parent = parent;
    disk = parent.getDisk ();

    // hi-bits of type are used for flags
    readOnly = (buffer[offset + 9] & 0x80) != 0;
    systemFile = (buffer[offset + 10] & 0x80) != 0;

    byte[] typeBuffer = new byte[3];
    typeBuffer[0] = (byte) (buffer[offset + 9] & 0x7F);
    typeBuffer[1] = (byte) (buffer[offset + 10] & 0x7F);
    typeBuffer[2] = (byte) (buffer[offset + 11] & 0x7F);
    type = new String (typeBuffer).trim ();

    userNumber = buffer[offset] & 0xFF;
    if (userNumber == 0xE5 && buffer[offset + 1] == 0)
      name = "";
    else
      name = new String (buffer, offset + 1, 8).trim ();
    extent = buffer[offset + 12] & 0xFF;
    s2 = buffer[offset + 13] & 0xFF;
    s1 = buffer[offset + 14] & 0xFF;
    recordsUsed = buffer[offset + 15] & 0xFF;
    System.arraycopy (buffer, offset + 16, blockList, 0, 16);

    Disk disk = parent.getDisk ();
    for (byte b : blockList)
    {
      if (b == 0)
        break;

      int blockNumber;

      if ((b & 0x80) == 0)
        blockNumber = (b * 4 + 48);
      else
        blockNumber = (b & 0x7F) * 4;

      for (int i = 0; i < 4; i++)
        blocks.add (new AppleDiskAddress (disk, blockNumber + i));
    }
  }

  // ---------------------------------------------------------------------------------//
  String getType ()
  // ---------------------------------------------------------------------------------//
  {
    return type;
  }

  // ---------------------------------------------------------------------------------//
  boolean matches (DirectoryEntry directoryEntry)
  // ---------------------------------------------------------------------------------//
  {
    return userNumber == directoryEntry.userNumber && name.equals (directoryEntry.name)
        && type.equals (directoryEntry.type);
  }

  // ---------------------------------------------------------------------------------//
  void add (DirectoryEntry entry)
  // ---------------------------------------------------------------------------------//
  {
    entries.add (entry);

    Disk disk = parent.getDisk ();
    for (byte b : entry.blockList)
    {
      if (b == 0)
        break;

      int blockNumber = b * 4 + 48;
      for (int i = 0; i < 4; i++)
        blocks.add (new AppleDiskAddress (disk, blockNumber + i));
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean contains (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    for (DiskAddress sector : blocks)
      if (sector.matches (da))
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  String line ()
  // ---------------------------------------------------------------------------------//
  {
    String bytes = HexFormatter.getHexString (blockList, 0, 16);
    bytes = bytes.replaceAll ("00", "  ");

    char ro = readOnly ? '*' : ' ';
    char sf = systemFile ? '*' : ' ';

    String text =
        String.format ("%3d   %-8s   %-3s %s %s  %02X   %02X   %02X   %02X   %s",
            userNumber, name, type, ro, sf, extent, s2, s1, recordsUsed, bytes);
    for (DirectoryEntry entry : entries)
      text = text + "\n" + entry.line ();

    if (extent != 0)
      text = "                    " + text.substring (20);

    return text;
  }

  // ---------------------------------------------------------------------------------//
  String toDetailedString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("User number .... %d%n", userNumber));
    text.append (String.format ("File name ...... %s%n", name + "." + type));
    text.append (String.format ("Extents lo ..... %d%n", extent));
    text.append (String.format ("Extents hi ..... %d%n", s2));
    text.append (String.format ("Reserved ....... %d%n", s1));

    int blocks = ((recordsUsed & 0xF0) >> 3) + (((recordsUsed & 0x0F) + 7) / 8);
    text.append (String.format ("Records ........ %02X  (%d)%n", recordsUsed, blocks));

    String bytes = HexFormatter.getHexString (blockList, 0, 16);
    text.append (String.format ("Allocation ..... %s%n", bytes));

    for (DirectoryEntry entry : entries)
    {
      bytes = HexFormatter.getHexString (entry.blockList, 0, 16);
      text.append (String.format ("                 %s%n", bytes));
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getUniqueName ()
  // ---------------------------------------------------------------------------------//
  {
    return name + "." + type;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getDataSource ()
  // ---------------------------------------------------------------------------------//
  {
    if (appleFile != null)
      return appleFile;

    byte[] buffer = disk.readBlocks (blocks);

    if (buffer.length == 0)
    {
      appleFile = new DefaultAppleFile (name, buffer);
      return appleFile;
    }

    DirectoryEntry entry = recordsUsed == 0x80 ? entries.get (entries.size () - 1) : this;
    int len = (entry.extent * 128 + entry.recordsUsed) * 128;
    if (len > buffer.length)
    {
      System.out.println ("too big");       // see tdbt12d1.sdk
      len = buffer.length;
    }

    byte[] exactBuffer = new byte[len];
    System.arraycopy (buffer, 0, exactBuffer, 0, len);

    int max = Math.min (256, exactBuffer.length);
    int count = 0;
    for (int i = 1; i < max; i++)
    {
      if (exactBuffer[i - 1] == 0x0D && exactBuffer[i] == 0x0A)
        ++count;
    }

    if ("COM".equals (type))
      appleFile = new DefaultAppleFile (name, exactBuffer, "COM File");
    else if ("DVR".equals (type))
      appleFile = new DefaultAppleFile (name, exactBuffer, "DVR File");
    else if ("ASM".equals (type) || "DOC".equals (type) || "TXT".equals (type)
        || count > 2)
      appleFile = new CPMTextFile (name, exactBuffer);
    else
      appleFile = new DefaultAppleFile (name, exactBuffer, "CPM File : " + type);

    return appleFile;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getSectors ()
  // ---------------------------------------------------------------------------------//
  {
    return blocks;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public FormattedDisk getFormattedDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return parent;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return name + "." + type;
  }
}