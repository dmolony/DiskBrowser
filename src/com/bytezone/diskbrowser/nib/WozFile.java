package com.bytezone.diskbrowser.nib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class WozFile
{
  private static final byte[] address16prologue =
      { (byte) 0xD5, (byte) 0xAA, (byte) 0x96 };
  private static final byte[] address13prologue =
      { (byte) 0xD5, (byte) 0xAA, (byte) 0xB5 };
  private static final byte[] dataPrologue = { (byte) 0xD5, (byte) 0xAA, (byte) 0xAD };
  private static final byte[] epilogue = { (byte) 0xDE, (byte) 0xAA, (byte) 0xEB };

  private static final int BLOCK_SIZE = 512;
  private static final int SECTOR_SIZE = 256;

  private static final int TRK_SIZE = 0x1A00;
  private static final int DATA_SIZE = TRK_SIZE - 10;

  private final DiskReader13Sector diskReader13Sector = new DiskReader13Sector ();
  private final DiskReader16Sector diskReader16Sector = new DiskReader16Sector ();

  private static int[][] interleave =
      { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
        { 0, 7, 14, 6, 13, 5, 12, 4, 11, 3, 10, 2, 9, 1, 8, 15 } };

  public final File file;

  private int diskSectors;
  private int wozVersion;
  private byte[] addressPrologue;
  private final byte[] diskBuffer;

  private final boolean debug = false;

  // ---------------------------------------------------------------------------------//
  public WozFile (File file) throws DiskNibbleException
  // ---------------------------------------------------------------------------------//
  {
    this.file = file;

    byte[] buffer = readFile (file);
    String header = new String (buffer, 0, 4);
    if (!"WOZ1".equals (header) && !"WOZ2".equals (header))
      throw new DiskNibbleException ("Header error");

    int checksum1 = val32 (buffer, 8);
    int checksum2 = Utility.crc32 (buffer, 12, buffer.length - 12);
    if (checksum1 != checksum2)
    {
      System.out.printf ("Stored checksum     : %08X%n", checksum1);
      System.out.printf ("Calculated checksum : %08X%n", checksum2);
      throw new DiskNibbleException ("Checksum error");
    }

    List<Track> tracks = null;

    int ptr = 12;
    while (ptr < buffer.length)
    {
      String chunkId = new String (buffer, ptr, 4);
      int size = val32 (buffer, ptr + 4);
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
    diskBuffer = new byte[35 * diskSectors * 256];
    int ndx = diskSectors == 13 ? 0 : 1;

    for (Track track : tracks)
      for (Sector sector : track)
        if (sector.dataOffset > 0)
          sector.pack (diskReader, diskBuffer, SECTOR_SIZE
              * (sector.trackNo * diskSectors + interleave[ndx][sector.sectorNo]));
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getDiskBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    return diskBuffer;
  }

  // ---------------------------------------------------------------------------------//
  public int getSectorsPerTrack ()
  // ---------------------------------------------------------------------------------//
  {
    return diskSectors;
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

    if (debug)
    {
      String diskTypeText = diskType == 1 ? "5.25" : "3.5";

      System.out.printf ("Version ............. %d%n", wozVersion);
      System.out.printf ("Disk type ........... %d  (%s\")%n", diskType, diskTypeText);
      System.out.printf ("Write protected ..... %d%n", writeProtected);
      System.out.printf ("Synchronized ........ %d%n", synchronised);
      System.out.printf ("Cleaned ............. %d%n", cleaned);
      System.out.printf ("Creator ............. %s%n", creator);
    }

    if (wozVersion == 2)
    {
      int sides = val8 (buffer, ptr + 45);
      int bootSectorFormat = val8 (buffer, ptr + 46);
      int optimalBitTiming = val8 (buffer, ptr + 47);
      int compatibleHardware = val16 (buffer, ptr + 48);
      int requiredRam = val16 (buffer, ptr + 50);
      int largestTrack = val16 (buffer, ptr + 52);

      setGlobals (bootSectorFormat == 2 ? 13 : 16);

      if (debug)
      {
        String bootSectorFormatText =
            bootSectorFormat == 0 ? "Unknown" : bootSectorFormat == 1 ? "16 sector"
                : bootSectorFormat == 2 ? "13 sector" : "Hybrid";

        System.out.printf ("Sides ............... %d%n", sides);
        System.out.printf ("Boot sector format .. %d  (%s)%n", bootSectorFormat,
            bootSectorFormatText);
        System.out.printf ("Optimal bit timing .. %d%n", optimalBitTiming);
        System.out.printf ("Compatible hardware . %d%n", compatibleHardware);
        System.out.printf ("Required RAM ........ %d%n", requiredRam);
        System.out.printf ("Largest track ....... %d%n", largestTrack);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void setGlobals (int diskSectors)
  // ---------------------------------------------------------------------------------//
  {
    this.diskSectors = diskSectors;
    addressPrologue = diskSectors == 13 ? address13prologue : address16prologue;
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

    int reclen = wozVersion == 1 ? TRK_SIZE : 8;
    int max = wozVersion == 1 ? 35 : 160;
    for (int i = 0; i < max; i++)
    {
      try
      {
        Track trk = new Track (i, rawBuffer, ptr);
        if (trk.bitCount == 0)
          break;
        tracks.add (trk);
        if (debug)
          System.out.printf ("%n$%02X  %s%n", i, trk);
      }
      catch (DiskNibbleException e)
      {
        e.printStackTrace ();
      }
      ptr += reclen;
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
  private int decode4and4 (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
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
    String home = "/Users/denismolony/";
    String wozBase1 = home + "Dropbox/Examples/woz test images/WOZ 1.0/";
    String wozBase2 = home + "Dropbox/Examples/woz test images/WOZ 2.0/";
    File[] files = { new File (home + "code/python/wozardry-2.0/bill.woz"),
                     new File (wozBase2 + "DOS 3.3 System Master.woz"),
                     new File (wozBase1 + "DOS 3.3 System Master.woz") };
    try
    {
      new WozFile (files[2]);
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
    int bytesUsed;

    List<Sector> sectors = new ArrayList<> ();

    // ---------------------------------------------------------------------------------//
    public Track (int trackNo, byte[] rawBuffer, int ptr) throws DiskNibbleException
    // ---------------------------------------------------------------------------------//
    {
      this.rawBuffer = rawBuffer;
      this.trackNo = trackNo;

      if (debug)
        System.out.println (HexFormatter.format (rawBuffer, ptr, 512, ptr));

      if (wozVersion == 1)
      {
        bytesUsed = val16 (rawBuffer, ptr + DATA_SIZE);
        bitCount = val16 (rawBuffer, ptr + DATA_SIZE + 2);

        if (debug)
          System.out.println (
              (String.format ("Bytes: %2d,  Bits: %,8d%n%n", bytesUsed, bitCount)));
      }
      else
      {
        startingBlock = val16 (rawBuffer, ptr);
        blockCount = val16 (rawBuffer, ptr + 2);
        bitCount = val32 (rawBuffer, ptr + 4);

        if (debug)
          System.out.println ((String.format ("Start: %4d,  Blocks: %2d,  Bits: %,8d%n%n",
              startingBlock, blockCount, bitCount)));
      }

      if (bitCount == 0)
        return;

      resetIndex ();

      if (addressPrologue == null)                                 // WOZ1
        if (findNext (address16prologue, ptr) > 0)
          setGlobals (16);
        else if (findNext (address13prologue, ptr) > 0)
          setGlobals (13);
        else
          throw new DiskNibbleException ("No address prologue found");

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
    private void checkDuplicates (Sector newSector)
    // ---------------------------------------------------------------------------------//
    {
      for (Sector sector : sectors)
        if (sector.isDuplicate (newSector))
          System.out.println ("\n*** duplicate ***\n");
    }

    // ---------------------------------------------------------------------------------//
    private void resetIndex ()
    // ---------------------------------------------------------------------------------//
    {
      trackIndex = 0;
      bitIndex = 0;

      if (wozVersion == 1)
        byteIndex = 256 + trackNo * TRK_SIZE;
      else
        byteIndex = startingBlock * BLOCK_SIZE;
    }

    // ---------------------------------------------------------------------------------//
    boolean nextBit ()
    // ---------------------------------------------------------------------------------//
    {
      boolean bit = (rawBuffer[byteIndex] & (0x80 >>> bitIndex)) != 0;

      if (++trackIndex >= bitCount)
      {
        ++revolutions;
        resetIndex ();
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
      if (wozVersion == 1)
        text.append (String.format ("Bytes: %2d,  Bits: %,8d%n%n", bytesUsed, bitCount));
      else
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
    int trackNo, sectorNo, volume, checksum;
    int addressOffset, dataOffset;

    // ---------------------------------------------------------------------------------//
    Sector (Track track, int addressOffset)
    // ---------------------------------------------------------------------------------//
    {
      this.track = track;

      volume = decode4and4 (track.newBuffer, addressOffset + 3);
      trackNo = decode4and4 (track.newBuffer, addressOffset + 5);
      sectorNo = decode4and4 (track.newBuffer, addressOffset + 7);
      checksum = decode4and4 (track.newBuffer, addressOffset + 9);

      this.addressOffset = addressOffset;
      dataOffset = track.findNext (dataPrologue, addressOffset + 11);
      if (dataOffset > addressOffset + 200)
        dataOffset = -1;
    }

    // ---------------------------------------------------------------------------------//
    boolean isDuplicate (Sector sector)
    // ---------------------------------------------------------------------------------//
    {
      return this.sectorNo == sector.sectorNo;
    }

    // ---------------------------------------------------------------------------------//
    void dump ()
    // ---------------------------------------------------------------------------------//
    {
      System.out.println ();
      System.out.println (this);
      System.out.println (HexFormatter.format (track.newBuffer, addressOffset, BLOCK_SIZE,
          addressOffset));
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
          trackNo, sectorNo, checksum, addressOffset, dataOffsetText);
    }
  }
}
