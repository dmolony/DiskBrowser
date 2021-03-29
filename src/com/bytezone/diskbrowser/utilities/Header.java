package com.bytezone.diskbrowser.utilities;

// -----------------------------------------------------------------------------------//
class Header
// -----------------------------------------------------------------------------------//
{
  private final int totalRecords;
  private final int version;
  private final int eof;
  private final int crc;
  private final DateTime created;
  private final DateTime modified;
  boolean bin2;

  // ---------------------------------------------------------------------------------//
  public Header (byte[] buffer) throws FileFormatException
  // ---------------------------------------------------------------------------------//
  {
    int ptr = 0;

    while (true)
    {
      if (isNuFile (buffer, ptr))
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
    eof = Utility.getLong (buffer, ptr + 38);

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
  private boolean isNuFile (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    if (buffer[ptr] == 0x4E && buffer[ptr + 1] == (byte) 0xF5 && buffer[ptr + 2] == 0x46
        && buffer[ptr + 3] == (byte) 0xE9 && buffer[ptr + 4] == 0x6C
        && buffer[ptr + 5] == (byte) 0xE5)
      return true;
    return false;
  }

  // ---------------------------------------------------------------------------------//
  private boolean isBin2 (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    if (buffer[ptr] == 0x0A && buffer[ptr + 1] == 0x47 && buffer[ptr + 2] == 0x4C
        && buffer[ptr + 18] == (byte) 0x02)
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
