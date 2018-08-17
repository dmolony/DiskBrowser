package com.bytezone.diskbrowser.nib;

class DiskAddressField
{
  int track, sector, volume, checksum;

  // ---------------------------------------------------------------------------------//
  // constructor
  // ---------------------------------------------------------------------------------//

  DiskAddressField (byte[] buffer)
  {
    volume = decode4and4 (buffer, 0);
    track = decode4and4 (buffer, 2);
    sector = decode4and4 (buffer, 4);
    checksum = decode4and4 (buffer, 6);

    //    if (track == 0)
    //      for (int i = 0; i < 8; i++)
    //        System.out.printf ("%02X ", buffer[i]);
  }

  // ---------------------------------------------------------------------------------//
  // decode4and4
  // ---------------------------------------------------------------------------------//

  private int decode4and4 (byte[] buffer, int offset)
  {
    int odds = ((buffer[offset] & 0xFF) << 1) | 0x01;
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
