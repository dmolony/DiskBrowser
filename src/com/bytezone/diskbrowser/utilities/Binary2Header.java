package com.bytezone.diskbrowser.utilities;

import java.time.LocalDateTime;

// -----------------------------------------------------------------------------------//
public class Binary2Header
// -----------------------------------------------------------------------------------//
{
  static String[] osTypes = { "Prodos", "DOS 3.3", "Pascal", "CPM", "MS-DOS" };

  int accessCode;
  int fileType;
  int auxType;
  int storageType;
  int totalBlocks;
  LocalDateTime modified;
  LocalDateTime created;
  int id;                        // always 0x02
  int eof;
  String fileName;
  String nativeFileName;
  int prodos16accessCode;
  int prodos16fileType;
  int prodos16storageType;
  int prodos16totalBlocks;
  int prodos16eof;
  long diskSpaceRequired;
  int osType;
  int nativeFileType;
  int phantomFileFlag;
  int dataFlags;
  int version;
  int filesToFollow;

  boolean compressed;
  boolean encrypted;
  boolean sparsePacked;

  // ---------------------------------------------------------------------------------//
  public Binary2Header (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    accessCode = buffer[3] & 0xFF;
    fileType = buffer[4] & 0xFF;
    auxType = Utility.readShort (buffer, 5);
    storageType = buffer[7] & 0xFF;
    totalBlocks = Utility.readShort (buffer, 8);
    modified = Utility.getAppleDate (buffer, 10);
    created = Utility.getAppleDate (buffer, 14);
    id = buffer[18] & 0xFF;
    eof = Utility.readTriple (buffer, 20);
    fileName = HexFormatter.getPascalString (buffer, 23);
    prodos16accessCode = buffer[111] & 0xFF;
    prodos16fileType = buffer[112] & 0xFF;
    prodos16storageType = buffer[113] & 0xFF;
    prodos16totalBlocks = Utility.readShort (buffer, 114);
    prodos16eof = buffer[116] & 0xFF;
    diskSpaceRequired = Utility.getLong (buffer, 117);
    osType = buffer[121] & 0xFF;
    nativeFileType = Utility.readShort (buffer, 122);
    phantomFileFlag = buffer[124] & 0xFF;
    dataFlags = buffer[125] & 0xFF;
    version = buffer[126] & 0xFF;
    filesToFollow = buffer[127] & 0xFF;

    compressed = (dataFlags & 0x80) != 0;
    encrypted = (dataFlags & 0x40) != 0;
    sparsePacked = (dataFlags & 0x01) != 0;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Access ................ %02X%n", accessCode));
    text.append (String.format ("File type ............. %02X%n", fileType));
    text.append (String.format ("Aux type .............. %04X%n", auxType));
    text.append (String.format ("Storage type .......... %02X%n", storageType));
    text.append (String.format ("Total blocks .......... %04X  %<,d%n", totalBlocks));
    text.append (String.format ("Modified .............. %s%n", modified));
    text.append (String.format ("Created ............... %s%n", created));
    text.append (String.format ("ID (0x02) ............. %02X%n", id));
    text.append (String.format ("End of file ........... %06X  %<,d%n", eof));
    text.append (String.format ("File name ............. %s%n", fileName));
    text.append (String.format ("Prodos access ......... %02X%n", prodos16accessCode));
    text.append (String.format ("Prodos file type ...... %02X%n", prodos16fileType));
    text.append (String.format ("Prodos storage type ... %02X%n", prodos16storageType));
    text.append (String.format ("Prodos total blocks ... %02X%n", prodos16totalBlocks));
    text.append (String.format ("Prodos eof ............ %06X  %<,d%n", prodos16eof));
    text.append (
        String.format ("Disk space needed ..... %08X  %<,d%n", diskSpaceRequired));
    text.append (
        String.format ("OS type ............... %02X  %s%n", osType, osTypes[osType]));
    text.append (String.format ("Native file type ...... %02X%n", nativeFileType));
    text.append (String.format ("Data flags ............ %02X%n", dataFlags));
    text.append (String.format ("Version ............... %02X%n", version));
    text.append (String.format ("Following files ....... %02X%n", filesToFollow));

    return text.toString ();
  }
}