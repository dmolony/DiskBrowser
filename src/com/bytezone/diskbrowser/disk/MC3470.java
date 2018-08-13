package com.bytezone.diskbrowser.disk;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

class MC3470
{
  private static final int MAX_DATA = 999;
  private final boolean debug = false;
  private final boolean dump = false;

  private List<RawDiskSector> diskSectors;

  private State currentState;
  private RawDiskSector currentDiskSector;

  private int expectedDataSize;
  private boolean finished;
  private boolean restarted;

  private DiskReader diskReader;
  private final DiskReader diskReader16 = new DiskReader16Sector ();
  private final DiskReader diskReader13 = new DiskReader13Sector ();

  private final byte[] dataBuffer = new byte[MAX_DATA];
  private int dataPtr = 0;

  private enum State
  {
    ADDRESS, DATA, OTHER
  }

  // ---------------------------------------------------------------------------------//
  // readTrack
  // ---------------------------------------------------------------------------------//

  List<RawDiskSector> readTrack (byte[] buffer, int offset, int bytesUsed, int bitCount)
      throws DiskNibbleException
  {
    final int max = offset + bytesUsed;
    int totalBits = 0;
    int totalBytes = 0;

    diskSectors = new ArrayList<> ();
    diskReader = null;
    currentDiskSector = null;
    currentState = State.OTHER;
    finished = false;
    restarted = false;

    byte value = 0;                     // value to be stored
    dataPtr = 0;
    expectedDataSize = MAX_DATA;

    if (debug)
    {
      System.out.printf ("%nOffset    : %06X%n", offset);
      System.out.printf ("Bytes used: %06X%n", bytesUsed);
      System.out.printf ("Bit count : %06X%n", bitCount);
    }

    int inPtr = offset;               // keep offset in case we have to loop around
    while (inPtr < max && !finished)
    {
      byte b = buffer[inPtr++];

      if (!restarted)
        totalBytes++;

      for (int mask = 0x80; mask != 0; mask >>>= 1)
      {
        value <<= 1;
        if ((b & mask) != 0)
          value |= 0x01;

        if ((value & 0x80) != 0)     // value is not valid until the hi-bit is set
        {
          if (dump)
          {
            if (dataPtr % 16 == 0)
              System.out.printf ("%n%04X: ", dataPtr);
            System.out.printf ("%02X ", value);
          }

          dataBuffer[dataPtr++] = value;
          checkState (value);
          value = 0;
        }

        if (++totalBits == bitCount)
          break;
      }

      // check for unfinished data block, we may need to restart the track
      if (inPtr == max && currentState == State.DATA && !restarted)
      {
        inPtr = offset;
        restarted = true;
      }
    }

    if (debug)
    {
      System.out.println ("**************************************");
      System.out.printf ("*  total bits  : %,5d  *%n", bitCount);
      System.out.printf ("*  bits used   : %,5d  *%n", totalBits);
      System.out.printf ("*  total bytes : %,5d  *%n", bytesUsed);
      System.out.printf ("*  bytes used  : %,5d  *%n", totalBytes);
      System.out.println ("**************************************");
    }

    return diskSectors;
  }

  // ---------------------------------------------------------------------------------//
  // storeSectors
  // ---------------------------------------------------------------------------------//

  void storeSectors (List<RawDiskSector> diskSectors, byte[] diskBuffer)
      throws DiskNibbleException
  {
    if (diskReader == null)
      throw new DiskNibbleException ("No DiskReader");

    for (RawDiskSector diskSector : diskSectors)
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

  private void checkState (byte value) throws DiskNibbleException
  {
    switch (value)
    {
      case (byte) 0xB5:
        if (isPrologue ())
        {
          diskReader = diskReader13;
          setState (State.ADDRESS);
        }
        break;

      case (byte) 0x96:
        if (isPrologue ())
        {
          diskReader = diskReader16;
          setState (State.ADDRESS);
        }
        break;

      case (byte) 0xAD:
        if (isPrologue ())
          setState (State.DATA);
        break;

      case (byte) 0xEB:
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

  private void setState (State newState) throws DiskNibbleException
  {
    if (currentState == newState && currentState == State.OTHER)
      return;
    assert currentState != newState : currentState + " -> " + newState;

    switch (currentState)           // this state is now finished
    {
      case ADDRESS:
        if (currentDiskSector != null)
          System.out.printf ("unused ADDRESS: %s%n", currentDiskSector);

        currentDiskSector = new RawDiskSector (new DiskAddressField (dataBuffer));
        if (dump)
          System.out.println (currentDiskSector);
        break;

      case DATA:
        if (currentDiskSector == null)
        {
          System.out.printf ("cannot store %d DATA no ADDRESS", dataPtr);
          if (debug)
            System.out.println (HexFormatter.format (dataBuffer, 0, dataPtr));
        }
        else
        {
          currentDiskSector.setBuffer (diskReader.decodeSector (dataBuffer));
          diskSectors.add (currentDiskSector);
          currentDiskSector = null;
          if (diskSectors.size () == diskReader.sectorsPerTrack)
            finished = true;
        }
        break;

      case OTHER:       // triggered by an epilogue or full address/data buffer
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
        expectedDataSize = MAX_DATA;      // what is the maximum filler?
        break;
    }

    currentState = newState;
    dataPtr = 0;                          // start collecting new buffer
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
}
