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

  // ---------------------------------------------------------------------------------//
  public MasterHeader (byte[] buffer) throws FileFormatException
  // ---------------------------------------------------------------------------------//
  {
    int ptr = 0;

    while (true)
    {
      if (Utility.isMagic (buffer, ptr, NuFile))
        break;

      if (isBin2 (buffer, ptr))
      {
        ptr += 128;
        bin2 = true;
        continue;
      }

      throw new FileFormatException ("NuFile not found");
    }

    crc = Utility.getWord (buffer, ptr + 6);
    totalRecords = Utility.getLong (buffer, ptr + 8);
    created = new DateTime (buffer, ptr + 12);
    modified = new DateTime (buffer, ptr + 20);
    version = Utility.getWord (buffer, ptr + 28);
    reserved = Utility.getWord (buffer, ptr + 30);
    eof = Utility.getLong (buffer, ptr + 38);

    assert reserved == 0;

    byte[] crcBuffer = new byte[40];
    System.arraycopy (buffer, ptr + 8, crcBuffer, 0, crcBuffer.length);
    if (crc != Utility.getCRC (crcBuffer, 0))
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
    text.append (String.format ("Master EOF ..... %,d", eof));

    return text.toString ();
  }
}
