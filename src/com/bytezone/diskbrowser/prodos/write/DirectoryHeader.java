package com.bytezone.diskbrowser.prodos.write;

import static com.bytezone.diskbrowser.prodos.ProdosConstants.BLOCK_SIZE;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.ENTRIES_PER_BLOCK;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.ENTRY_SIZE;
import static com.bytezone.diskbrowser.utilities.Utility.getAppleDate;
import static com.bytezone.diskbrowser.utilities.Utility.getShort;
import static com.bytezone.diskbrowser.utilities.Utility.putAppleDate;
import static com.bytezone.diskbrowser.utilities.Utility.writeShort;

import java.time.LocalDateTime;

// -----------------------------------------------------------------------------------//
public class DirectoryHeader
// -----------------------------------------------------------------------------------//
{
  static final String UNDERLINE = "--------------------------------------------";
  ProdosDisk disk;
  byte[] buffer;
  int ptr;

  String fileName;
  byte storageType;
  LocalDateTime creationDate;
  byte version = 0x00;
  byte minVersion = 0x00;
  byte access = (byte) 0xE3;
  byte entryLength = ENTRY_SIZE;
  byte entriesPerBlock = ENTRIES_PER_BLOCK;
  int fileCount;

  // ---------------------------------------------------------------------------------//
  public DirectoryHeader (ProdosDisk disk, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    this.disk = disk;
    this.buffer = disk.getBuffer ();
    this.ptr = ptr;
  }

  // ---------------------------------------------------------------------------------//
  void read ()
  // ---------------------------------------------------------------------------------//
  {
    storageType = (byte) ((buffer[ptr] & 0xF0) >>> 4);
    int nameLength = buffer[ptr] & 0x0F;
    fileName = new String (buffer, ptr + 1, nameLength);

    creationDate = getAppleDate (buffer, ptr + 0x18);
    version = buffer[ptr + 0x1C];
    minVersion = buffer[ptr + 0x1D];
    access = buffer[ptr + 0x1E];
    entryLength = buffer[ptr + 0x1F];
    entriesPerBlock = buffer[ptr + 0x20];
    fileCount = getShort (buffer, ptr + 0x21);
  }

  // ---------------------------------------------------------------------------------//
  void write ()
  // ---------------------------------------------------------------------------------//
  {
    buffer[ptr] = (byte) ((storageType << 4) | fileName.length ());
    System.arraycopy (fileName.getBytes (), 0, buffer, ptr + 1, fileName.length ());

    putAppleDate (buffer, ptr + 0x18, creationDate);
    buffer[ptr + 0x1C] = version;
    buffer[ptr + 0x1D] = minVersion;
    buffer[ptr + 0x1E] = access;
    buffer[ptr + 0x1F] = entryLength;
    buffer[ptr + 0x20] = entriesPerBlock;
    writeShort (buffer, ptr + 0x21, fileCount);
  }

  // ---------------------------------------------------------------------------------//
  void list ()
  // ---------------------------------------------------------------------------------//
  {
    System.out.println (UNDERLINE);
    System.out.println (toText ());
    System.out.println (UNDERLINE);

    int blockNo = ptr / BLOCK_SIZE;

    do
    {
      int offset = blockNo * BLOCK_SIZE;
      int ptr = offset + 4;
      for (int i = 0; i < ENTRIES_PER_BLOCK; i++)
      {
        int storageType = (buffer[ptr] & 0xF0) >>> 4;
        int nameLength = buffer[ptr] & 0x0F;
        if (nameLength != 0 && storageType < 0x0E)
        {
          FileEntry fileEntry = new FileEntry (disk, ptr);
          fileEntry.read ();
          System.out.println (fileEntry.toText ());
        }

        ptr += ENTRY_SIZE;
      }
      blockNo = getShort (buffer, offset + 2);
    } while (blockNo > 0);
    System.out.println ();
  }

  // ---------------------------------------------------------------------------------//
  String toText ()
  // ---------------------------------------------------------------------------------//
  {
    int block = ptr / BLOCK_SIZE;
    int entry = ((ptr % BLOCK_SIZE) - 4) / 39 + 1;

    return String.format ("%04X:%02X %-15s %02X %04X", block, entry, fileName,
        storageType, fileCount);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int blockNo = ptr / BLOCK_SIZE;
    text.append (String.format ("Block ............ %04X%n", blockNo));
    text.append (
        String.format ("Entry ............ %02X%n", ((ptr % BLOCK_SIZE) - 4) / 39 + 1));

    text.append (String.format ("Storage type ..... %02X  %s%n", storageType,
        ProdosDisk.storageTypes[storageType]));
    text.append (String.format ("Name length ...... %02X%n", fileName.length ()));
    text.append (String.format ("File name ........ %s%n", fileName));
    text.append (String.format ("Version .......... %02X%n", version));
    text.append (String.format ("Min version ...... %02X%n", minVersion));
    text.append (String.format ("Created .......... %s%n", creationDate));
    text.append (String.format ("Access ........... %02X%n", access));
    text.append (String.format ("Entry length ..... %02X%n", entryLength));
    text.append (String.format ("Entries per blk .. %02X%n", entriesPerBlock));
    text.append (String.format ("File count ....... %d%n", fileCount));

    return text.toString ();
  }
}
