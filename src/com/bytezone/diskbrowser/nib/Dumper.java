package com.bytezone.diskbrowser.nib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

  private final DiskReader13Sector diskReader13Sector = new DiskReader13Sector ();
  private final DiskReader16Sector diskReader16Sector = new DiskReader16Sector ();

  private int diskSectors;
  private int wozVersion;
  private byte[] addressPrologue;

  private final boolean debug = false;

  // ---------------------------------------------------------------------------------//
  public Dumper (File file) throws DiskNibbleException
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = readFile (file);
    String header = new String (buffer, 0, 4);
    if (!"WOZ1".equals (header) && !"WOZ2".equals (header))
    {
      System.out.println ("Not a WOZ disk");
      return;
    }

    List<Track> tracks = null;

    int ptr = 12;
    while (ptr < buffer.length)
    {
      String chunkId = new String (buffer, ptr, 4);
      int size = Utility.getLong (buffer, ptr + 4);
      if (debug)
        System.out.printf ("%n%s  %,9d%n", chunkId, size);

      switch (chunkId)
      {
        case "INFO":                            // 60 bytes
          info (buffer, ptr);
          break;
        case "TMAP":                            // 160 bytes
          tmap (buffer, ptr);
          break;
        case "TRKS":                            // starts at 248
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

    DiskReader diskReader = diskSectors == 13 ? diskReader13Sector : diskReader16Sector;
    byte[] diskBuffer = new byte[35 * diskSectors * 256];

    for (Track track : tracks)
      for (Sector sector : track)
        if (sector.dataOffset > 0)
          sector.pack (diskReader, diskBuffer,
              256 * (sector.trackNo * diskSectors + sector.sector));

    int tr = 0x11;
    int sc = 15;
    System.out
        .println (HexFormatter.format (diskBuffer, 256 * (tr * diskSectors + sc), 256));
  }

  // ---------------------------------------------------------------------------------//
  private void info (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    wozVersion = val8 (buffer, ptr + 8);
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

    diskSectors = bootSectorFormat == 2 ? 13 : 16;
    addressPrologue = diskSectors == 13 ? address13prologue : address16prologue;

    if (debug)
    {
      String bootSectorFormatText =
          bootSectorFormat == 0 ? "Unknown" : bootSectorFormat == 1 ? "16 sector"
              : bootSectorFormat == 2 ? "13 sector" : "Hybrid";
      String diskTypeText = diskType == 1 ? "5.25" : "3.5";

      System.out.printf ("Version ............. %d%n", wozVersion);
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

    if (debug)
    {
      String metaData = new String (buffer, ptr, length);
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
      if (debug)
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
    try (BufferedInputStream in = new BufferedInputStream (new FileInputStream (file)))
    {
      return in.readAllBytes ();
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
    //    File file = new File ("/Users/denismolony/code/python/wozardry-2.0/bill.woz");
    File file = new File ("/Users/denismolony/Dropbox/Examples/woz test images/WOZ 2.0/"
        + "DOS 3.3 System Master.woz");
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
  class Track implements Iterable<Sector>
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

      while (sectors.size () < diskSectors)
      {
        offset = findNext (addressPrologue, offset + 1);
        if (offset < 0)
          break;

        Sector sector = new Sector (this, offset);
        if (sectors.size () > 0)
          checkDuplicates (sector);
        sectors.add (sector);
      }
    }

    // ---------------------------------------------------------------------------------//
    private void checkDuplicates (Sector sector1)
    // ---------------------------------------------------------------------------------//
    {
      for (Sector sector : sectors)
        if (sector1.sector == sector.sector)
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
      byte b = 0;
      while ((b & 0x80) == 0)
      {
        b <<= 1;
        if (nextBit ())
          b |= 0x01;
      }

      return b;
    }

    // ---------------------------------------------------------------------------------//
    void readTrack ()
    // ---------------------------------------------------------------------------------//
    {
      if (newBuffer != null)
        return;

      int max = (bitCount - 1) / 8 + 1;
      max += 520;
      newBuffer = new byte[max];

      for (int i = 0; i < max; i++)
        newBuffer[i] = (byte) nextByte ();
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
    void packSector (int sector)
    // ---------------------------------------------------------------------------------//
    {

    }

    // ---------------------------------------------------------------------------------//
    void dump ()
    // ---------------------------------------------------------------------------------//
    {
      System.out.println (HexFormatter.format (newBuffer));
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

    // ---------------------------------------------------------------------------------//
    @Override
    public Iterator<Sector> iterator ()
    // ---------------------------------------------------------------------------------//
    {
      return sectors.iterator ();
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
    void pack (DiskReader diskReader, byte[] buffer, int ptr) throws DiskNibbleException
    // ---------------------------------------------------------------------------------//
    {
      byte[] decodedBuffer = diskReader.decodeSector (track.newBuffer, dataOffset + 3);
      System.arraycopy (decodedBuffer, 0, buffer, ptr, decodedBuffer.length);
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
