package com.bytezone.diskbrowser.prodos.write;

import static com.bytezone.diskbrowser.prodos.ProdosConstants.BLOCK_SIZE;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.ENTRY_SIZE;
import static com.bytezone.diskbrowser.utilities.Utility.unsignedShort;
import static com.bytezone.diskbrowser.utilities.Utility.writeShort;

import java.time.LocalDateTime;

// -----------------------------------------------------------------------------------//
public class SubdirectoryHeader extends DirectoryHeader
// -----------------------------------------------------------------------------------//
{
  private int parentPointer;
  private byte parentEntry;
  private byte parentEntryLength;

  // ---------------------------------------------------------------------------------//
  public SubdirectoryHeader (ProdosDisk disk, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    super (disk, ptr);

    storageType = (byte) 0x0E;
    access = (byte) 0xC3;
  }

  // ---------------------------------------------------------------------------------//
  void setParentDetails (FileEntry fileEntry)
  // ---------------------------------------------------------------------------------//
  {
    parentPointer = fileEntry.getBlockNo ();
    parentEntry = (byte) fileEntry.getEntryNo ();
    parentEntryLength = ENTRY_SIZE;
  }

  // ---------------------------------------------------------------------------------//
  FileEntry getParentFileEntry ()
  // ---------------------------------------------------------------------------------//
  {
    FileEntry fileEntry = new FileEntry (disk,
        parentPointer * BLOCK_SIZE + (parentEntry - 1) * ENTRY_SIZE + 4);
    fileEntry.read ();

    return fileEntry;
  }

  // ---------------------------------------------------------------------------------//
  void updateParentFileEntry ()
  // ---------------------------------------------------------------------------------//
  {
    FileEntry fileEntry = getParentFileEntry ();
    fileEntry.blocksUsed++;
    fileEntry.eof += BLOCK_SIZE;
    fileEntry.modifiedDate = LocalDateTime.now ();
    fileEntry.write ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void read ()
  // ---------------------------------------------------------------------------------//
  {
    super.read ();

    parentPointer = unsignedShort (buffer, ptr + 0x23);
    parentEntry = buffer[ptr + 0x25];
    parentEntryLength = buffer[ptr + 0x26];

    assert parentPointer > 0;
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
  String toText ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%s %04X:%02X", super.toText (), parentPointer, parentEntry);
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
    text.append (String.format ("Parent pointer ... %04X%n", parentPointer));
    text.append (String.format ("Parent entry ..... %02X%n", parentEntry));
    text.append (String.format ("PE length ........ %02X%n", parentEntryLength));
    text.append (UNDERLINE);

    return text.toString ();
  }
}
