package com.bytezone.diskbrowser.nib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class Dumper
// -----------------------------------------------------------------------------------//
{
  private static final int[] weights = { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };

  private static final byte[] address16prologue =
      { (byte) 0xD5, (byte) 0xAA, (byte) 0x96 };
  private static final byte[] address13prologue =
      { (byte) 0xD5, (byte) 0xAA, (byte) 0xB5 };
  private static final byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };
  private static final byte[] epilogue = { (byte) 0xDE, (byte) 0xAA, (byte) 0xEB };

  List<Track> tracks;

  // ---------------------------------------------------------------------------------//
  public Dumper (File file)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = readFile (file);
    String header = new String (buffer, 0, 4);
    System.out.println (header);

    int ptr = 12;
    while (ptr < buffer.length)
    {
      String chunkId = new String (buffer, ptr, 4);
      int size = Utility.getLong (buffer, ptr + 4);
      System.out.printf ("%n%s  %,9d%n", chunkId, size);
      switch (chunkId)
      {
        case "INFO":
          info (buffer, ptr);
          break;
        case "TMAP":
          tmap (buffer, ptr);
          break;
        case "TRKS":
          tracks = trks (buffer, ptr);
          break;
        case "META":
          meta (buffer, ptr, size);
          break;
        case "WRIT":
          break;
        default:
          break;
      }
      ptr += size + 8;
    }

    Track track = tracks.get (0x22);
    for (Sector sector : track.sectors)
      sector.dump ();
  }

  // ---------------------------------------------------------------------------------//
  private void info (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    int version = val8 (buffer, ptr + 8);
    int diskType = val8 (buffer, ptr + 9);
    int writeProtected = val8 (buffer, ptr + 10);
    int synchronised = val8 (buffer, ptr + 11);
    int cleaned = val8 (buffer, ptr + 12);
    String creator = new String (buffer, ptr + 13, 32);
    int sides = val8 (buffer, ptr + 45);
    int bootSectorFormat = val8 (buffer, ptr + 46);
    int optimalBitTiming = val8 (buffer, ptr + 47);
    int compatibleHardware = val16 (buffer, ptr + 48);
    int requiredRam = val16 (buffer, ptr + 50);
    int largestTrack = val16 (buffer, ptr + 52);

    String bootSectorFormatText =
        bootSectorFormat == 0 ? "Unknown" : bootSectorFormat == 1 ? "16 sector"
            : bootSectorFormat == 2 ? "13 sector" : "Hybrid";
    String diskTypeText = diskType == 1 ? "5.25" : "3.5";

    System.out.printf ("Version ............. %d%n", version);
    System.out.printf ("Disk type ........... %d  (%s\")%n", diskType, diskTypeText);
    System.out.printf ("Write protected ..... %d%n", writeProtected);
    System.out.printf ("Synchronized ........ %d%n", synchronised);
    System.out.printf ("Cleaned ............. %d%n", cleaned);
    System.out.printf ("Creator ............. %s%n", creator);
    System.out.printf ("Sides ............... %d%n", sides);
    System.out.printf ("Boot sector format .. %d  (%s)%n", bootSectorFormat,
        bootSectorFormatText);
    System.out.printf ("Optimal bit timing .. %d%n", optimalBitTiming);
    System.out.printf ("Compatible hardware . %d%n", compatibleHardware);
    System.out.printf ("Required RAM ........ %d%n", requiredRam);
    System.out.printf ("Largest track ....... %d%n", largestTrack);
  }

  // ---------------------------------------------------------------------------------//
  private void tmap (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    ptr += 8;
  }

  // ---------------------------------------------------------------------------------//
  private void meta (byte[] buffer, int ptr, int length)
  // ---------------------------------------------------------------------------------//
  {
    ptr += 8;
    String metaData = new String (buffer, ptr, length);
    //    System.out.println (metaData);
    String[] chunks = metaData.split ("\n");
    for (String chunk : chunks)
    {
      String[] parts = chunk.split ("\t");
      if (parts.length >= 2)
        System.out.printf ("%-20s %s%n", parts[0], parts[1]);
      else
        System.out.printf ("%-20s%n", parts[0]);
    }
  }

  // ---------------------------------------------------------------------------------//
  private List<Track> trks (byte[] rawBuffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    List<Track> tracks = new ArrayList<> ();
    ptr += 8;
    for (int i = 0; i < 160; i++)
    {
      Track trk = new Track (i, rawBuffer, ptr);
      if (trk.bitCount == 0)
        break;
      tracks.add (trk);
      ptr += 8;
      System.out.printf ("%n$%02X  %s%n", i, trk);
    }
    return tracks;
  }

  // ---------------------------------------------------------------------------------//
  private int val8 (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return (buffer[ptr] & 0xFF);
  }

  // ---------------------------------------------------------------------------------//
  private int val16 (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return (buffer[ptr++] & 0xFF) + ((buffer[ptr] & 0xFF) << 8);
  }

  // ---------------------------------------------------------------------------------//
  private int val32 (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return (buffer[ptr++] & 0xFF) + ((buffer[ptr++] & 0xFF) << 8)
        + ((buffer[ptr++] & 0xFF) << 16) + ((buffer[ptr] & 0xFF) << 24);
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
  private byte[] readFile (File file)
  // ---------------------------------------------------------------------------------//
  {
    try
    {
      BufferedInputStream in = new BufferedInputStream (new FileInputStream (file));
      byte[] buffer = in.readAllBytes ();
      in.close ();
      return buffer;
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      return null;
    }
  }

  // ---------------------------------------------------------------------------------//
  public static void main (String[] args)
  // ---------------------------------------------------------------------------------//
  {
    File file = new File ("/Users/denismolony/code/python/wozardry-2.0/bill.woz");
    try
    {
      Dumper dumper = new Dumper (file);
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }

  // ---------------------------------------------------------------------------------//
  class Track
  // ---------------------------------------------------------------------------------//
  {
    int trackNo;
    int startingBlock;
    int blockCount;
    int bitCount;

    byte[] rawBuffer;
    byte[] newBuffer;

    int bitIndex;
    int byteIndex;
    int trackIndex;
    int revolutions;

    List<Sector> sectors = new ArrayList<> ();

    // ---------------------------------------------------------------------------------//
    public Track (int trackNo, byte[] rawBuffer, int ptr)
    // ---------------------------------------------------------------------------------//
    {
      this.rawBuffer = rawBuffer;
      this.trackNo = trackNo;

      startingBlock = val16 (rawBuffer, ptr);
      blockCount = val16 (rawBuffer, ptr + 2);
      bitCount = val32 (rawBuffer, ptr + 4);

      if (bitCount == 0)
        return;

      byteIndex = startingBlock * 512;

      int offset = -1;

      while (sectors.size () < 13)        // hard-coded!!
      {
        offset = findNext (address13prologue, offset + 1);
        if (offset < 0)
          break;

        Sector sector = new Sector (this, offset);
        if (sectors.size () > 0)
          checkDuplicates (sector, sectors.get (sectors.size () - 1));
        sectors.add (sector);
      }
    }

    // ---------------------------------------------------------------------------------//
    private void checkDuplicates (Sector sector1, Sector sector2)
    // ---------------------------------------------------------------------------------//
    {
      if (sector1.sector == sector2.sector)
        System.out.println ("\n*** duplicate ***\n");
    }

    // ---------------------------------------------------------------------------------//
    boolean nextBit ()
    // ---------------------------------------------------------------------------------//
    {
      boolean bit = ((rawBuffer[byteIndex] & 0xFF) & weights[bitIndex]) != 0;

      if (++trackIndex >= bitCount)
      {
        ++revolutions;
        trackIndex = 0;
        bitIndex = 0;
        byteIndex = startingBlock * 512;
      }
      else if (++bitIndex >= 8)
      {
        ++byteIndex;
        bitIndex = 0;
      }

      return bit;
    }

    // ---------------------------------------------------------------------------------//
    int nextByte ()
    // ---------------------------------------------------------------------------------//
    {
      while (!nextBit ())
        if (revolutions >= 2)
        {
          System.out.println ("looping");
          return 0;
        }

      int b = 0x80;
      for (int i = 6; i >= 0; i--)
        if (nextBit ())
          b |= (1 << i);

      return b;
    }

    // ---------------------------------------------------------------------------------//
    byte[] readTrack ()
    // ---------------------------------------------------------------------------------//
    {
      if (newBuffer != null)
        return newBuffer;

      int max = (bitCount - 1) / 8 + 1;
      max += 520;
      newBuffer = new byte[max];

      for (int i = 0; i < max; i++)
        newBuffer[i] = (byte) nextByte ();

      return newBuffer;
    }

    // ---------------------------------------------------------------------------------//
    int findNext (byte[] key, int start)
    // ---------------------------------------------------------------------------------//
    {
      readTrack ();

      int max = newBuffer.length - key.length;
      outer: for (int ptr = start; ptr < max; ptr++)
      {
        for (int keyPtr = 0; keyPtr < key.length; keyPtr++)
          if (newBuffer[ptr + keyPtr] != key[keyPtr])
            continue outer;
        return ptr;
      }

      return -1;
    }

    // ---------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // ---------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();
      text.append (String.format ("Start: %4d,  Blocks: %2d,  Bits: %,8d%n%n",
          startingBlock, blockCount, bitCount));
      int count = 0;
      for (Sector sector : sectors)
        text.append (String.format ("%2d  %s%n", count++, sector));
      text.deleteCharAt (text.length () - 1);
      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  class Sector
  // ---------------------------------------------------------------------------------//
  {
    Track track;
    int trackNo, sector, volume, checksum;
    int addressOffset, dataOffset;

    // ---------------------------------------------------------------------------------//
    Sector (Track track, int addressOffset)
    // ---------------------------------------------------------------------------------//
    {
      this.track = track;
      byte[] buffer = track.newBuffer;
      volume = decode4and4 (buffer, addressOffset + 3);
      trackNo = decode4and4 (buffer, addressOffset + 5);
      sector = decode4and4 (buffer, addressOffset + 7);
      checksum = decode4and4 (buffer, addressOffset + 9);

      this.addressOffset = addressOffset;
      dataOffset = track.findNext (dataPrologue, addressOffset + 11);
      if (dataOffset > addressOffset + 200)
        dataOffset = -1;
    }

    // ---------------------------------------------------------------------------------//
    void dump ()
    // ---------------------------------------------------------------------------------//
    {
      System.out.println ();
      System.out.println (this);
      System.out.println (
          HexFormatter.format (track.newBuffer, addressOffset, 512, addressOffset));
    }

    // ---------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // ---------------------------------------------------------------------------------//
    {
      String dataOffsetText = dataOffset < 0 ? "" : String.format ("%04X", dataOffset);
      return String.format (
          "Vol: %02X  Trk: %02X  Sct: %02X  Chk: %02X  Add: %04X  Dat: %s", volume,
          trackNo, sector, checksum, addressOffset, dataOffsetText);
    }
  }
}
