package com.bytezone.diskbrowser.prodos.write;

import static com.bytezone.diskbrowser.prodos.ProdosConstants.BLOCK_SIZE;
import static com.bytezone.diskbrowser.prodos.ProdosConstants.ENTRY_SIZE;
import static com.bytezone.diskbrowser.prodos.write.ProdosDisk.UNDERLINE;
import static com.bytezone.diskbrowser.utilities.Utility.getAppleDate;
import static com.bytezone.diskbrowser.utilities.Utility.putAppleDate;
import static com.bytezone.diskbrowser.utilities.Utility.readTriple;
import static com.bytezone.diskbrowser.utilities.Utility.unsignedShort;
import static com.bytezone.diskbrowser.utilities.Utility.writeShort;
import static com.bytezone.diskbrowser.utilities.Utility.writeTriple;

import java.time.LocalDateTime;

// -----------------------------------------------------------------------------------//
public class FileEntry
// -----------------------------------------------------------------------------------//
{
  private final ProdosDisk disk;
  private final byte[] buffer;
  private final int ptr;

  String fileName;
  byte storageType;
  LocalDateTime creationDate;
  LocalDateTime modifiedDate;
  byte fileType;
  int keyPointer;
  int blocksUsed;
  int eof;
  byte version = 0x00;
  byte minVersion = 0x00;
  byte access = (byte) 0xE3;
  int auxType;
  int headerPointer;

  // ---------------------------------------------------------------------------------//
  public FileEntry (ProdosDisk disk, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    this.disk = disk;
    this.buffer = disk.getBuffer ();
    this.ptr = ptr;
  }

  // ---------------------------------------------------------------------------------//
  int getBlockNo ()
  // ---------------------------------------------------------------------------------//
  {
    return ptr / BLOCK_SIZE;
  }

  // ---------------------------------------------------------------------------------//
  int getEntryNo ()
  // ---------------------------------------------------------------------------------//
  {
    return (((ptr % BLOCK_SIZE) - 4) / ENTRY_SIZE + 1);
  }

  // ---------------------------------------------------------------------------------//
  void read ()
  // ---------------------------------------------------------------------------------//
  {
    storageType = (byte) ((buffer[ptr] & 0xF0) >>> 4);

    int nameLength = buffer[ptr] & 0x0F;
    if (nameLength > 0)
      fileName = new String (buffer, ptr + 1, nameLength);
    else
      fileName = "";

    fileType = buffer[ptr + 0x10];
    keyPointer = unsignedShort (buffer, ptr + 0x11);
    blocksUsed = unsignedShort (buffer, ptr + 0x13);
    eof = readTriple (buffer, ptr + 0x15);
    creationDate = getAppleDate (buffer, ptr + 0x18);

    version = buffer[ptr + 0x1C];
    minVersion = buffer[ptr + 0x1D];
    access = buffer[ptr + 0x1E];

    auxType = unsignedShort (buffer, ptr + 0x1F);
    modifiedDate = getAppleDate (buffer, ptr + 0x21);
    headerPointer = unsignedShort (buffer, ptr + 0x25);
  }

  // ---------------------------------------------------------------------------------//
  void write ()
  // ---------------------------------------------------------------------------------//
  {
    buffer[ptr] = (byte) ((storageType << 4) | fileName.length ());
    System.arraycopy (fileName.getBytes (), 0, buffer, ptr + 1, fileName.length ());

    buffer[ptr + 0x10] = fileType;
    writeShort (buffer, ptr + 0x11, keyPointer);
    writeShort (buffer, ptr + 0x13, blocksUsed);
    writeTriple (buffer, ptr + 0x15, eof);
    putAppleDate (buffer, ptr + 0x18, creationDate);

    buffer[ptr + 0x1C] = version;
    buffer[ptr + 0x1D] = minVersion;
    buffer[ptr + 0x1E] = access;

    writeShort (buffer, ptr + 0x1F, auxType);
    putAppleDate (buffer, ptr + 0x21, modifiedDate);
    writeShort (buffer, ptr + 0x25, headerPointer);
  }

  // ---------------------------------------------------------------------------------//
  String toText ()
  // ---------------------------------------------------------------------------------//
  {
    int block = ptr / BLOCK_SIZE;
    int entry = ((ptr % BLOCK_SIZE) - 4) / 39 + 1;

    return String.format ("%04X:%02X %-15s %02X %04X %02X %04X %04X", block, entry,
        fileName, storageType, blocksUsed, fileType, keyPointer, headerPointer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (UNDERLINE);
    text.append ("File Entry\n");
    text.append (UNDERLINE);
    int blockNo = ptr / BLOCK_SIZE;
    text.append (String.format ("Block ............ %04X%n", blockNo));
    text.append (
        String.format ("Entry ............ %02X%n", ((ptr % BLOCK_SIZE) - 4) / 39 + 1));
    text.append (String.format ("Storage type ..... %02X  %s%n", storageType,
        ProdosDisk.storageTypes[storageType]));
    text.append (String.format ("Name length ...... %02X%n", fileName.length ()));
    text.append (String.format ("File name ........ %s%n", fileName));
    text.append (String.format ("File type ........ %02X%n", fileType));
    text.append (String.format ("Key pointer ...... %04X%n", keyPointer));
    text.append (String.format ("Blocks used ...... %d%n", blocksUsed));
    text.append (String.format ("EOF .............. %d%n", eof));
    text.append (String.format ("Created .......... %s%n", creationDate));
    text.append (String.format ("Version .......... %02X%n", version));
    text.append (String.format ("Min version ...... %02X%n", minVersion));
    text.append (String.format ("Access ........... %02X%n", access));
    text.append (String.format ("Aux .............. %d%n", auxType));
    text.append (String.format ("Modified ......... %s%n", modifiedDate));
    text.append (String.format ("Header ptr ....... %04X%n", headerPointer));
    text.append (UNDERLINE);

    return text.toString ();
  }
}
