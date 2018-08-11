package com.bytezone.diskbrowser.disk;

class DiskAddressField
{
  int track, sector, volume, checksum;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  DiskAddressField (byte[] buffer, int offset)
  {
    volume = decode4and4 (buffer, offset);
    track = decode4and4 (buffer, offset + 2);
    sector = decode4and4 (buffer, offset + 4);
    checksum = decode4and4 (buffer, offset + 6);
  }

  // ---------------------------------------------------------------------------------//
  // decode4and4
  // ---------------------------------------------------------------------------------//

  int decode4and4 (byte[] buffer, int offset)
  {
    int odds = ((buffer[offset] & 0xFF) << 1) + 1;
    int evens = buffer[offset + 1] & 0xFF;
    return odds & evens;
  }

  // ---------------------------------------------------------------------------------//
  // toString
  // ---------------------------------------------------------------------------------//

  @Override
  public String toString ()
  {
    return String.format ("[volume: %02X, track: %02X, sector: %02X, checksum: %02X]",
        volume, track, sector, checksum);
  }
}
