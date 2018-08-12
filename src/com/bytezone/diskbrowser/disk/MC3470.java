package com.bytezone.diskbrowser.disk;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

class MC3470
{
  private static final int EMPTY = 999;
  private final boolean debug = false;
  private final boolean dump = false;

  private final List<DiskSector> diskSectors = new ArrayList<> ();

  private State currentState;
  private DiskSector currentDiskSector;
  private int expectedDataSize;
  private boolean finished;
  private boolean restarted;

  private DiskReader diskReader;
  private final DiskReader diskReader16 = new DiskReader16Sector ();
  private final DiskReader diskReader13 = new DiskReader13Sector ();

  private final byte[] dataBuffer = new byte[EMPTY];
  private int dataPtr = 0;

  private enum State
  {
    ADDRESS, DATA, OTHER
  }

  // ---------------------------------------------------------------------------------//
  // readTrack
  // ---------------------------------------------------------------------------------//

  void readTrack (byte[] buffer, int offset, int bytesUsed, int bitCount)
      throws DiskNibbleException
  {
    final int max = offset + bytesUsed;
    int totalBits = 0;

    diskSectors.clear ();
    currentDiskSector = null;
    currentState = State.OTHER;
    expectedDataSize = EMPTY;
    finished = false;
    restarted = false;
    int value = 0;
    dataPtr = 0;

    if (debug)
    {
      System.out.printf ("%nOffset    : %06X%n", offset);
      System.out.printf ("Bytes used: %06X%n", bytesUsed);
      System.out.printf ("Bit count : %06X%n", bitCount);
      System.out.printf ("remaining : %06X%n", bitCount % 8);
    }

    int inPtr = offset;         // keep offset in case we have to loop around
    while (!finished && inPtr < max)
    {
      int b = buffer[inPtr++] & 0xFF;
      for (int mask = 0x80; mask != 0; mask >>>= 1)
      {
        value <<= 1;
        if ((b & mask) != 0)
          value |= 1;

        if ((value & 0x80) != 0)                        // is hi-bit set?
        {
          if (dump)
          {
            if (dataPtr % 16 == 0)
              System.out.printf ("%n%04X: ", dataPtr);
            System.out.printf ("%02X ", value);
          }

          dataBuffer[dataPtr++] = (byte) value;
          checkState (value);
          value = 0;
        }

        if (++totalBits == bitCount)
          break;
      }

      if (inPtr == max && currentState == State.DATA && !restarted)
      {
        inPtr = offset;
        restarted = true;
      }
    }

    if (debug)
    {
      System.out.printf ("total bits : %d%n", bitCount);
      System.out.printf ("bits used  : %d%n", totalBits);
    }
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
  // checkState
  // ---------------------------------------------------------------------------------//

  private void checkState (int value) throws DiskNibbleException
  {
    switch (value)
    {
      case 0xB5:
        if (isPrologue ())
        {
          diskReader = diskReader13;
          setState (State.ADDRESS);
        }
        break;

      case 0x96:
        if (isPrologue ())
        {
          diskReader = diskReader16;
          setState (State.ADDRESS);
        }
        break;

      case 0xAD:
        if (isPrologue ())
          setState (State.DATA);
        break;

      case 0xEB:
        if (isEpilogue ())
          setState (State.OTHER);
        break;
    }

    if (dataPtr == expectedDataSize)
    {
      if (currentState == State.OTHER)
        throw new DiskNibbleException ("No address or data blocks found");
      setState (State.OTHER);
    }
  }

  // ---------------------------------------------------------------------------------//
  // setState
  // ---------------------------------------------------------------------------------//

  private void setState (State newState)
  {
    if (currentState == newState && currentState == State.OTHER)
      return;
    assert currentState != newState : currentState + " -> " + newState;

    switch (currentState)           // this state is now finished
    {
      case ADDRESS:
        currentDiskSector = new DiskSector (new DiskAddressField (dataBuffer));
        if (dump)
          System.out.println (currentDiskSector);
        break;

      case DATA:
        if (currentDiskSector != null)
        {
          currentDiskSector.setBuffer (diskReader.decodeSector (dataBuffer));
          diskSectors.add (currentDiskSector);
          currentDiskSector = null;
          if (diskSectors.size () == diskReader.sectorsPerTrack)
            finished = true;
        }
        else
        {
          if (debug)
          {
            System.out.printf ("cannot store %d DATA no ADDRESS", dataPtr);
            System.out.println (HexFormatter.format (dataBuffer, 0, dataPtr));
          }
        }
        break;

      case OTHER:
        break;
    }

    switch (newState)               // this state is now starting
    {
      case ADDRESS:
        if (dump)
          System.out.print ("ADDRESS  ");
        expectedDataSize = 8;
        break;

      case DATA:
        if (dump)
          System.out.println ("DATA");
        if (debug && currentDiskSector == null)
        {
          System.out.println ("starting DATA with no ADDRESS");
          System.out.println (HexFormatter.format (dataBuffer, 0, dataPtr));
        }
        expectedDataSize = diskReader.expectedDataSize ();
        break;

      case OTHER:
        if (dump)
          System.out.println ("OTHER");
        expectedDataSize = EMPTY;      // what is the maximum filler?
        break;
    }

    currentState = newState;
    dataPtr = 0;                    // start collecting new buffer
  }

  // ---------------------------------------------------------------------------------//
  // isPrologue
  // ---------------------------------------------------------------------------------//

  private boolean isPrologue ()
  {
    return dataPtr >= 3
        && (dataBuffer[dataPtr - 3] == (byte) 0xD5
            || dataBuffer[dataPtr - 3] == (byte) 0xD4)      // non-standard
        && dataBuffer[dataPtr - 2] == (byte) 0xAA;
  }

  // ---------------------------------------------------------------------------------//
  // isEpilogue
  // ---------------------------------------------------------------------------------//

  private boolean isEpilogue ()
  {
    return dataPtr >= 3
        && (dataBuffer[dataPtr - 3] == (byte) 0xDE
            || dataBuffer[dataPtr - 3] == (byte) 0xDA)      // non-standard
        && dataBuffer[dataPtr - 2] == (byte) 0xAA;
  }

  // ---------------------------------------------------------------------------------//
  // DiskSector
  // ---------------------------------------------------------------------------------//

  class DiskSector
  {
    final DiskAddressField addressField;
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
