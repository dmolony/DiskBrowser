package com.bytezone.diskbrowser.utilities;

// -----------------------------------------------------------------------------------//
class MasterHeader
// -----------------------------------------------------------------------------------//
{
  private static final byte[] NuFile =
      { 0x4E, (byte) 0xF5, 0x46, (byte) 0xE9, 0x6C, (byte) 0xE5 };
  private static final byte[] BIN2 = { 0x0A, 0x47, 0x4C };

  private final int crc;
  private final int totalRecords;
  private final DateTime created;
  private final DateTime modified;
  private final int version;
  private final int reserved;
  private final int eof;

  boolean bin2;
  Binary2Header binary2Header;

  // ---------------------------------------------------------------------------------//
  public MasterHeader (byte[] buffer) throws FileFormatException
  // ---------------------------------------------------------------------------------//
  {
    int ptr = 0;

    while (true)
    {
      if (Utility.isMagic (buffer, ptr, NuFile))
        break;

      // internet.shk has 0x2000 bytes of text at the start
      //      if (Utility.isMagic (buffer, 0x2000, NuFile))
      //      {
      //        System.out.println ("found it");
      //        ptr = 0x2000;
      //        bin2 = true;
      //        break;
      //      }

      if (isBin2 (buffer, ptr))
      {
        binary2Header = new Binary2Header (buffer, 0);
        if (binary2Header.fileType == (byte) 0xE0
            && (binary2Header.auxType == 0x8000 || binary2Header.auxType == 0x8002))
        {
          ptr += 128;
          bin2 = true;
          continue;
        }
        else
        {
          System.out.printf ("Not NuFX: %02X  %04X%n", binary2Header.fileType,
              binary2Header.auxType);
          System.out.println (binary2Header);
        }
      }

      System.out.println (HexFormatter.format (buffer, 0, 256));
      throw new FileFormatException ("NuFile not found");
    }

    crc = Utility.getShort (buffer, ptr + 6);
    totalRecords = Utility.getLong (buffer, ptr + 8);
    created = new DateTime (buffer, ptr + 12);
    modified = new DateTime (buffer, ptr + 20);
    version = Utility.getShort (buffer, ptr + 28);
    reserved = Utility.getShort (buffer, ptr + 30);
    eof = Utility.getLong (buffer, ptr + 38);

    //    assert reserved == 0;
    //    if (reserved != 0)
    //      System.out.printf ("Reserved for zero, actual: %02X%n", reserved);

    byte[] crcBuffer = new byte[40];
    System.arraycopy (buffer, ptr + 8, crcBuffer, 0, crcBuffer.length);
    if (crc != Utility.getCRC (crcBuffer, crcBuffer.length, 0))
    {
      System.out.println ("***** Master CRC mismatch *****");
      throw new FileFormatException ("Master CRC failed");
    }
  }

  // ---------------------------------------------------------------------------------//
  int getTotalRecords ()
  // ---------------------------------------------------------------------------------//
  {
    return totalRecords;
  }

  // ---------------------------------------------------------------------------------//
  String getCreated ()
  // ---------------------------------------------------------------------------------//
  {
    return created.format ();
  }

  // ---------------------------------------------------------------------------------//
  String getModified ()
  // ---------------------------------------------------------------------------------//
  {
    return modified.format ();
  }

  // ---------------------------------------------------------------------------------//
  String getCreated2 ()
  // ---------------------------------------------------------------------------------//
  {
    return created.format2 ();
  }

  // ---------------------------------------------------------------------------------//
  String getModified2 ()
  // ---------------------------------------------------------------------------------//
  {
    return modified.format2 ();
  }

  // ---------------------------------------------------------------------------------//
  long getEof ()
  // ---------------------------------------------------------------------------------//
  {
    return eof;
  }

  // ---------------------------------------------------------------------------------//
  private boolean isBin2 (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    if (Utility.isMagic (buffer, ptr, BIN2) && buffer[ptr + 18] == (byte) 0x02)
      return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Master CRC ..... %,d  (%04X)%n", crc, crc));
    text.append (String.format ("Records ........ %,d%n", totalRecords));
    text.append (String.format ("Created ........ %s%n", created.format ()));
    text.append (String.format ("Modified ....... %s%n", modified.format ()));
    text.append (String.format ("Version ........ %,d%n", version));
    text.append (String.format ("Reserved ....... %016X%n", reserved));
    text.append (String.format ("Master EOF ..... %,d", eof));

    return text.toString ();
  }
}
