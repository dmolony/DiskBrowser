package com.bytezone.diskbrowser.prodos.write;

import static com.bytezone.diskbrowser.prodos.write.ProdosDisk.ENTRY_SIZE;
import static com.bytezone.diskbrowser.prodos.write.ProdosDisk.UNDERLINE;
import static com.bytezone.diskbrowser.utilities.Utility.readShort;
import static com.bytezone.diskbrowser.utilities.Utility.writeShort;

// -----------------------------------------------------------------------------------//
public class SubdirectoryHeader extends DirectoryHeader
// -----------------------------------------------------------------------------------//
{
  int parentPointer;
  byte parentEntry;
  byte parentEntryLength = ENTRY_SIZE;

  // ---------------------------------------------------------------------------------//
  public SubdirectoryHeader (ProdosDisk disk, byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    super (disk, buffer, ptr);

    storageType = (byte) 0x0E;
    access = (byte) 0xC3;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void read ()
  // ---------------------------------------------------------------------------------//
  {
    super.read ();

    parentPointer = readShort (buffer, ptr + 0x23);
    parentEntry = buffer[ptr + 0x25];
    parentEntryLength = buffer[ptr + 0x26];
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void write ()
  // ---------------------------------------------------------------------------------//
  {
    super.write ();

    buffer[ptr + 0x10] = 0x75;                  // subdirectory header must be 0x75

    // these are supposed to be unused, but prodos fills them in
    //    buffer[ptr + 0x11] = version;
    //    buffer[ptr + 0x13] = access;
    //    buffer[ptr + 0x14] = parentEntryLength;
    //    buffer[ptr + 0x15] = entriesPerBlock;

    // fields specific to subdirectory headers
    writeShort (buffer, ptr + 0x23, parentPointer);
    buffer[ptr + 0x25] = parentEntry;
    buffer[ptr + 0x26] = parentEntryLength;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (UNDERLINE);
    text.append ("Subdirectory Header\n");
    text.append (UNDERLINE);
    text.append (super.toString ());
    text.append (String.format ("Parent pointer ... %d%n", parentPointer));
    text.append (String.format ("Parent entry ..... %02X%n", parentEntry));
    text.append (String.format ("PE length ........ %02X%n", parentEntryLength));
    text.append (UNDERLINE);

    return text.toString ();
  }
}
