package com.bytezone.diskbrowser.disk;

import java.util.ArrayList;
import java.util.List;

class MC3470
{
  private final boolean debug = false;

  private final byte[] dataBuffer = new byte[411];
  int dataPtr = 0;

  private final List<DiskSector> diskSectors = new ArrayList<> ();
  DiskReader diskReader;

  State currentState;
  DiskSector currentDiskSector;
  int expectedDataSize;

  DiskReader diskReader16 = new DiskReader16Sector ();
  DiskReader diskReader13 = new DiskReader13Sector ();

  enum State
  {
    ADDRESS, DATA, OTHER
  }

  // ---------------------------------------------------------------------------------//
  // readTrack
  // ---------------------------------------------------------------------------------//

  void readTrack (byte[] buffer, int offset, int bytesUsed, int bitCount)
  {
    int value = 0;

    final int max = offset + bytesUsed;
    int totalBits = 0;

    diskSectors.clear ();
    currentDiskSector = null;
    currentState = State.OTHER;

    if (debug)
    {
      System.out.printf ("%nOffset    : %06X%n", offset);
      System.out.printf ("Bytes used: %06X%n", bytesUsed);
    }

    int inPtr = offset;
    while (inPtr < max)
    {
      int b = buffer[inPtr++] & 0xFF;
      for (int mask = 0x80; mask != 0; mask >>>= 1)
      {
        value <<= 1;
        if ((b & mask) != 0)
          value |= 1;

        ++totalBits;

        if ((value & 0x80) != 0)                        // is hi-bit set?
        {
          dataBuffer[dataPtr++] = (byte) value;
          checkState (value);
          value = 0;
        }
      }
      if (inPtr == max && currentState == State.DATA)
      {
        System.out.println ("Unfinished business");
      }
    }

    //    if (bytesUsed > outputBuffer.length)
    //    {
    //      System.out.printf ("Bytes used  %,5d%n", bytesUsed);
    //      System.out.printf ("Buffer size %,5d%n", outPtr);
    //    }

    if (value != 0)
      System.out.printf ("********** Value not used: %01X%n", value);
  }

  // ---------------------------------------------------------------------------------//
  // storeSectors
  // ---------------------------------------------------------------------------------//

  void storeSectors (byte[] diskBuffer)
  {
    for (DiskSector diskSector : diskSectors)
      diskReader.storeBuffer (diskSector, diskBuffer);
  }

  // ---------------------------------------------------------------------------------//
  // checkState
  // ---------------------------------------------------------------------------------//

  private void checkState (int value)
  {
    if (value == 0xB5 && isPrologue ())
    {
      diskReader = diskReader13;
      setState (State.ADDRESS);
    }

    if (value == 0x96 && isPrologue ())
    {
      diskReader = diskReader16;
      setState (State.ADDRESS);
    }

    if (value == 0xAD && isPrologue ())
      setState (State.DATA);

    if (value == 0xEB && isEpilogue ())
      setState (State.OTHER);

    if (dataPtr == expectedDataSize)
      setState (State.OTHER);
  }

  // ---------------------------------------------------------------------------------//
  // setState
  // ---------------------------------------------------------------------------------//

  private void setState (State newState)
  {
    if (currentState == newState && currentState == State.OTHER)
      return;
    assert currentState != newState;

    switch (currentState)           // this state is now finished
    {
      case ADDRESS:
        currentDiskSector = new DiskSector (new DiskAddressField (dataBuffer, 0));
        break;

      case DATA:
        currentDiskSector.setBuffer (diskReader.decodeSector (dataBuffer, 0));
        diskSectors.add (currentDiskSector);
        break;

      case OTHER:
        break;
    }

    currentState = newState;
    dataPtr = 0;                    // start collecting new buffer

    switch (currentState)           // this state is now starting
    {
      case ADDRESS:
        expectedDataSize = 8;
        break;

      case DATA:
        expectedDataSize = diskReader.expectedDataSize ();
        break;

      case OTHER:
        expectedDataSize = -1;      // unspecified length
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  // is13Sector
  // ---------------------------------------------------------------------------------//

  boolean is13Sector ()
  {
    return diskSectors.size () == 13 && diskReader.sectorsPerTrack == 13;
  }

  // ---------------------------------------------------------------------------------//
  // is16Sector
  // ---------------------------------------------------------------------------------//

  boolean is16Sector ()
  {
    return diskSectors.size () == 16 && diskReader.sectorsPerTrack == 16;
  }

  // ---------------------------------------------------------------------------------//
  // isPrologue
  // ---------------------------------------------------------------------------------//

  private boolean isPrologue ()
  {
    return dataPtr >= 3 && dataBuffer[dataPtr - 3] == (byte) 0xD5
        && dataBuffer[dataPtr - 2] == (byte) 0xAA;
  }

  // ---------------------------------------------------------------------------------//
  // isEpilogue
  // ---------------------------------------------------------------------------------//

  private boolean isEpilogue ()
  {
    return dataPtr >= 3 && dataBuffer[dataPtr - 3] == (byte) 0xDE
        && dataBuffer[dataPtr - 2] == (byte) 0xAA;
  }

  // ---------------------------------------------------------------------------------//
  // DiskSector
  // ---------------------------------------------------------------------------------//

  class DiskSector
  {
    DiskAddressField addressField;
    byte[] buffer;

    DiskSector (DiskAddressField addressField)
    {
      this.addressField = addressField;
    }

    void setBuffer (byte[] buffer)
    {
      this.buffer = buffer;
    }

    @Override
    public String toString ()
    {
      return addressField.toString ();
    }
  }
}
