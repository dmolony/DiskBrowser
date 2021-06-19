package com.bytezone.diskbrowser.disk;

import java.awt.AWTEventMulticaster;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.bytezone.diskbrowser.nib.NibFile;
import com.bytezone.diskbrowser.nib.V2dFile;
import com.bytezone.diskbrowser.nib.WozFile;
import com.bytezone.diskbrowser.nufx.Binary2;
import com.bytezone.diskbrowser.nufx.NuFX;
import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class AppleDisk implements Disk
// -----------------------------------------------------------------------------------//
{
  private static final int MAX_INTERLEAVE = 3;
  private static final int SECTOR_SIZE = 256;
  private static final int BLOCK_SIZE = 512;

  public final File file;
  private final byte[] diskBuffer;        // contains the disk contents in memory

  private final int tracks;               // usually 35 for floppy disks
  private int sectors;                    // 8 or 16 (or 32 for unidos)
  private int blocks;                     // 280 or 560 for floppy disks, higher for HD
  private int dosVersion;

  private final int trackSize;            // 4096
  public int sectorSize;                  // 256 or 512

  private NuFX nuFX;
  private Binary2 bin2;
  private WozFile wozFile;
  private PrefixDiskCopy prefixDiskCopy;
  private Prefix2mg prefix2mg;

  private int interleave = 0;
  private static int[][] interleaveSector = //
      { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
          22, 23, 24, 25, 26, 27, 28, 29, 30, 31 },                     // None
        { 0, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 15 },       // Prodos/Pascal
        { 0, 13, 11, 9, 7, 5, 3, 1, 14, 12, 10, 8, 6, 4, 2, 15 },       // Infocom
        { 0, 6, 12, 3, 9, 15, 14, 5, 11, 2, 8, 7, 13, 4, 10, 1 } };     // CPM

  // Physical disk interleave:
  // Info from http://www.applelogic.org/TheAppleIIEGettingStarted.html
  // Block:    0 1 2 3 4 5 6 7 8 9 A B C D E F
  // Position: 0 8 1 9 2 A 3 B 4 C 5 D 6 E 7 F    - Prodos (.PO disks)
  // Position: 0 7 E 6 D 5 C 4 B 3 A 2 9 1 8 F    - Dos    (.DO disks)

  // https://github.com/AppleWin/AppleWin/blob/master/source/DiskImageHelper.cpp
  // DO logical order  0 1 2 3 4 5 6 7 8 9 A B C D E F */
  //    physical order 0 D B 9 7 5 3 1 E C A 8 6 4 2 F */

  // PO logical order  0 E D C B A 9 8 7 6 5 4 3 2 1 F */
  //    physical order 0 2 4 6 8 A C E 1 3 5 7 9 B D F */

  //BYTE CImageBase::ms_SectorNumber[NUM_SECTOR_ORDERS][0x10] =
  //{
  //  {0x00,0x08,0x01,0x09,0x02,0x0A,0x03,0x0B, 0x04,0x0C,0x05,0x0D,0x06,0x0E,0x07,0x0F},
  //  {0x00,0x07,0x0E,0x06,0x0D,0x05,0x0C,0x04, 0x0B,0x03,0x0A,0x02,0x09,0x01,0x08,0x0F},
  //  {0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00}
  //};

  // * TABLE OF PHYSICAL BSECTR NUMBERS
  // * WHICH CORRESPOND TO THE LOGICAL
  // * BSECTRS 0-F ON TRACK ZERO...
  // BHERE2  EQU     >*
  // TABLE   EQU     $800+BHERE2
  //         DFB     $00,13,11       ;00->00,01->13,02->11
  //         DFB     09,07,05        ;03->09,04->07;05->05
  //         DFB     03,01,14        ;06->03,07->01,08->14
  //         DFB     12,10,08        ;09->12,10->10,11->08
  //         DFB     06,04,02,15     ;12->06,13->04,14->02,15->15

  private boolean[] hasData;
  private byte emptyByte = 0;

  private ActionListener actionListenerList;
  private List<DiskAddress> blockList;

  private final boolean debug = false;

  // ---------------------------------------------------------------------------------//
  public AppleDisk (File file, int tracks, int sectors) throws FileFormatException
  // ---------------------------------------------------------------------------------//
  {
    this (file, tracks, sectors, 0);
  }

  // ---------------------------------------------------------------------------------//
  public AppleDisk (File file, int tracks, int sectors, int skip)
      throws FileFormatException
  // ---------------------------------------------------------------------------------//
  {
    assert (file.exists ()) : "No such path :" + file.getAbsolutePath ();
    assert (!file.isDirectory ()) : "File is directory :" + file.getAbsolutePath ();
    assert (file.length () <= Integer.MAX_VALUE) : "File too large";
    assert (file.length () != 0) : "File empty";

    String fileName = file.getName ();

    int pos = fileName.lastIndexOf ('.');
    String suffix = pos > 0 ? fileName.substring (pos + 1) : "";

    byte[] buffer = getPrefix (file);         // HDV could be a 2mg
    String prefix = new String (buffer, 0, 4);

    if ("2mg".equalsIgnoreCase (suffix) || "2IMG".equals (prefix))
    {
      if ("2IMG".equals (prefix))
      {
        prefix2mg = new Prefix2mg (buffer);
        if (debug)
          System.out.println (prefix2mg);

        if (prefix2mg.length > 0)
          this.blocks = prefix2mg.length / 4096 * 8;    // reduce blocks to a multiple of 8

        this.sectorSize = 512;
        this.trackSize = 8 * sectorSize;
        skip = prefix2mg.headerSize;

        tracks = blocks / 8;          // change parameter!
        sectors = 8;                  // change parameter!
      }
      else
      {
        System.out.println ("Not a 2mg file");
        this.blocks = (int) file.length () / 4096 * 8; // reduce blocks to a multiple of 8
        tracks = blocks / 8;          // change parameter!
        sectors = 8;                  // change parameter!
        this.sectorSize = 512;
        this.trackSize = sectors * sectorSize;
      }
    }
    else if ("img".equals (suffix) || "dimg".equals (suffix))
    {
      prefixDiskCopy = new PrefixDiskCopy (buffer);

      blocks = prefixDiskCopy.getBlocks ();
      this.sectorSize = 512;
      this.trackSize = 8 * sectorSize;
      skip = 0x54;

      tracks = blocks / 8;          // change parameter!
      sectors = 8;                  // change parameter!
    }
    else if (suffix.equalsIgnoreCase ("HDV")
        || (suffix.equalsIgnoreCase ("po") && tracks > 50)) // ULTIMATE APPLE1 CFFA 3.5.po
    {
      //this.blocks = (int) file.length () / 4096 * 8; // reduce blocks to a multiple of 8
      this.blocks = tracks * sectors;
      this.sectorSize = 512;
      this.trackSize = sectors * sectorSize;
    }
    else if (file.length () == 143360 && tracks == 256 && sectors == 8)    // wiz4
    {
      this.blocks = tracks * sectors;
      this.sectorSize = 512;
      this.trackSize = sectors * sectorSize;
    }
    else if (file.length () == 819200 && tracks == 50 && sectors == 32)    // unidisk
    {
      this.blocks = tracks * sectors;
      this.sectorSize = 256;
      this.trackSize = sectors * sectorSize;
    }
    else
    {
      this.blocks = tracks * sectors;
      this.sectorSize = (int) file.length () / blocks;
      this.trackSize = sectors * sectorSize;
    }

    if (sectorSize != 256 && sectorSize != 512)
      throw new FileFormatException ("Invalid sector size : " + sectorSize);

    this.file = file;
    this.tracks = tracks;
    this.sectors = sectors;

    if (debug)
    {
      System.out.printf ("Track size  : %d%n", trackSize);
      System.out.printf ("Sector size : %d%n", sectorSize);
      System.out.printf ("Tracks      : %d%n", tracks);
      System.out.printf ("Sectors     : %d%n", sectors);
      System.out.printf ("Blocks      : %d%n", blocks);
    }

    diskBuffer = new byte[blocks * sectorSize];

    hasData = new boolean[blocks];

    if (debug)
    {
      System.out.printf ("DiskBuffer size : %,d%n", diskBuffer.length);
      System.out.printf ("Skip size       : %,d%n", skip);
    }

    try (BufferedInputStream in = new BufferedInputStream (new FileInputStream (file)))
    {

      if (skip > 0)
        in.skip (skip);
      in.read (diskBuffer);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      System.exit (1);
    }

    checkSectorsForData ();
  }

  // ---------------------------------------------------------------------------------//
  void switchToDos ()
  // ---------------------------------------------------------------------------------//
  {
    sectorSize = 256;
    sectors = 16;
    blocks = 560;
    hasData = new boolean[blocks];
    checkSectorsForData ();
  }

  // ---------------------------------------------------------------------------------//
  void setNuFX (NuFX nufx)
  // ---------------------------------------------------------------------------------//
  {
    this.nuFX = nufx;
  }

  // ---------------------------------------------------------------------------------//
  void setBinary2 (Binary2 bin2)
  // ---------------------------------------------------------------------------------//
  {
    this.bin2 = bin2;
  }

  // ---------------------------------------------------------------------------------//
  public AppleDisk (V2dFile disk, int tracks, int sectors)
  // ---------------------------------------------------------------------------------//
  {
    this.tracks = tracks;
    this.sectors = sectors;

    file = disk.file;
    diskBuffer = disk.getDiskBuffer ();

    trackSize = 4096;
    sectorSize = trackSize / sectors;
    blocks = tracks * sectors;
    hasData = new boolean[blocks];

    checkSectorsForData ();
  }

  // ---------------------------------------------------------------------------------//
  public AppleDisk (NibFile disk)       // not used yet
  // ---------------------------------------------------------------------------------//
  {
    tracks = 35;
    trackSize = 4096;
    file = disk.file;
    diskBuffer = disk.getDiskBuffer ();
  }

  // ---------------------------------------------------------------------------------//
  public AppleDisk (WozFile wozFile, int tracks, int sectors)
  // ---------------------------------------------------------------------------------//
  {
    this.wozFile = wozFile;
    this.tracks = tracks;
    this.sectors = sectors;
    file = wozFile.file;
    diskBuffer = wozFile.getDiskBuffer ();

    if (sectors == 13)
    {
      trackSize = 0xD00;
      sectorSize = 256;
    }
    else
    {
      trackSize = 0x1000;
      sectorSize = trackSize / sectors;
    }

    blocks = tracks * sectors;
    hasData = new boolean[blocks];

    checkSectorsForData ();
  }

  // ---------------------------------------------------------------------------------//
  private byte[] getPrefix (File path)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = new byte[0x54];
    try (BufferedInputStream file = new BufferedInputStream (new FileInputStream (path)))
    {
      file.read (buffer);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      System.exit (1);
    }

    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  private void checkSectorsForData ()
  // ---------------------------------------------------------------------------------//
  {
    // force blockList to be rebuilt with the correct number/size of blocks
    blockList = null;

    for (DiskAddress da : this)
    {
      if (sectorSize == SECTOR_SIZE)                    // 256 byte sectors
      {
        int diskOffset = getBufferOffset (da);
        hasData[da.getBlockNo ()] = check (diskOffset);
      }
      else                                              // 512 byte blocks
      {
        int diskOffset1 = getBufferOffset (da, 0);
        int diskOffset2 = getBufferOffset (da, 1);
        hasData[da.getBlockNo ()] = check (diskOffset1) || check (diskOffset2);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private boolean check (int diskOffset)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = diskOffset, max = diskOffset + SECTOR_SIZE; i < max; i++)
      if (diskBuffer[i] != emptyByte)
        return true;
    return false;
  }

  // ---------------------------------------------------------------------------------//
  public void setDosVersion (int version)
  // ---------------------------------------------------------------------------------//
  {
    this.dosVersion = version;
  }

  /*
   * Routines that implement the Disk interface
   */

  // ---------------------------------------------------------------------------------//
  @Override
  public int getBlocksPerTrack ()
  // ---------------------------------------------------------------------------------//
  {
    return trackSize / sectorSize;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int getTrackSize ()
  // ---------------------------------------------------------------------------------//
  {
    return trackSize;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int getBlockSize ()
  // ---------------------------------------------------------------------------------//
  {
    return sectorSize;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int getTotalBlocks ()
  // ---------------------------------------------------------------------------------//
  {
    return blocks;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int getTotalTracks ()
  // ---------------------------------------------------------------------------------//
  {
    return tracks;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isBlockEmpty (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return !hasData[da.getBlockNo ()];
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isBlockEmpty (int block)
  // ---------------------------------------------------------------------------------//
  {
    return !hasData[block];
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isBlockEmpty (int track, int sector)
  // ---------------------------------------------------------------------------------//
  {
    return !hasData[getDiskAddress (track, sector).getBlockNo ()];
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public File getFile ()
  // ---------------------------------------------------------------------------------//
  {
    return file;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] readBlock (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = new byte[sectorSize];
    if (da == null)
      System.out.println ("Disk address is null");
    else
      readBuffer (da, buffer, 0);
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] readBlocks (List<DiskAddress> daList)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = new byte[daList.size () * sectorSize];
    int ptr = 0;
    for (DiskAddress da : daList)
    {
      // sparse text/PNT/PIC files may have gaps
      if (da != null && (da.getBlockNo () > 0 || ((AppleDiskAddress) da).zeroFlag ()))
        readBuffer (da, buffer, ptr);
      ptr += sectorSize;
    }
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] readBlock (int track, int sector)
  // ---------------------------------------------------------------------------------//
  {
    return readBlock (getDiskAddress (track, sector));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte[] readBlock (int block)
  // ---------------------------------------------------------------------------------//
  {
    return readBlock (getDiskAddress (block));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void writeBlock (DiskAddress da, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    writeBuffer (da, buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setInterleave (int interleave)
  // ---------------------------------------------------------------------------------//
  {
    assert (interleave >= 0 && interleave <= MAX_INTERLEAVE) : "Invalid interleave";
    this.interleave = interleave;
    checkSectorsForData ();
    if (actionListenerList != null)
      notifyListeners ("Interleave changed");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int getInterleave ()
  // ---------------------------------------------------------------------------------//
  {
    return interleave;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setBlockSize (int size)
  // ---------------------------------------------------------------------------------//
  {
    assert (size == SECTOR_SIZE || size == BLOCK_SIZE) : "Invalid sector size : " + size;
    if (sectorSize == size)
      return;

    sectorSize = size;
    sectors = trackSize / sectorSize;
    blocks = tracks * sectors;

    hasData = new boolean[blocks];
    checkSectorsForData ();

    if (actionListenerList != null)
      notifyListeners ("Sector size changed");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DiskAddress getDiskAddress (int track, int sector)
  // ---------------------------------------------------------------------------------//
  {
    if (!isValidAddress (track, sector))
    {
      System.out.println ("Invalid block : " + track + "/" + sector);
      return null;
      //      return new AppleDiskAddress (this, 0);    this was looping 26/07/2016
    }
    return new AppleDiskAddress (this, track, sector);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DiskAddress getDiskAddress (int block)
  // ---------------------------------------------------------------------------------//
  {
    if (!isValidAddress (block))
    {
      System.out.printf ("getDiskAddress: Invalid block : %d of %d%n", block,
          this.blocks);
      return null;
      //      return new AppleDiskAddress (this, 0);    // this was looping 26/07/2016
    }
    return new AppleDiskAddress (this, block);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getDiskAddressList (int... blocks)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> addressList = new ArrayList<> ();

    for (int block : blocks)
    {
      assert (isValidAddress (block)) : "Invalid block : " + block;
      addressList.add (new AppleDiskAddress (this, block));
    }
    return addressList;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isValidAddress (int block)
  // ---------------------------------------------------------------------------------//
  {
    return block >= 0 && block < this.blocks;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isValidAddress (int track, int sector)
  // ---------------------------------------------------------------------------------//
  {
    if (dosVersion >= 0x41)
    {
      track &= 0x3F;
      sector &= 0x1F;
    }
    //    else
    //      System.out.println ("normal dos");

    if (track < 0 || track >= this.tracks)
      return false;
    if (sector < 0 || sector >= this.sectors)
      return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean isValidAddress (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    return da != null && isValidAddress (da.getTrackNo (), da.getSectorNo ());
  }

  // This is the only method that transfers data from the disk buffer to an output buffer.
  // It handles sectors of 256 or 512 bytes, and both linear and interleaved sectors.
  // ---------------------------------------------------------------------------------//
  private void readBuffer (DiskAddress da, byte[] buffer, int bufferOffset)
  // ---------------------------------------------------------------------------------//
  {
    assert da.getDisk () == this : "Disk address not applicable to this disk";
    assert sectorSize == SECTOR_SIZE
        || sectorSize == BLOCK_SIZE : "Invalid sector size : " + sectorSize;
    assert interleave >= 0 && interleave <= MAX_INTERLEAVE : "Invalid interleave : "
        + interleave;

    if (sectorSize == SECTOR_SIZE)
    {
      int diskOffset = getBufferOffset (da);
      System.arraycopy (diskBuffer, diskOffset, buffer, bufferOffset, SECTOR_SIZE);
    }
    else
    {
      int diskOffset = getBufferOffset (da, 0);
      System.arraycopy (diskBuffer, diskOffset, buffer, bufferOffset, SECTOR_SIZE);

      diskOffset = getBufferOffset (da, 1);
      System.arraycopy (diskBuffer, diskOffset, buffer, bufferOffset + SECTOR_SIZE,
          SECTOR_SIZE);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void writeBuffer (DiskAddress da, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    assert da.getDisk () == this : "Disk address not applicable to this disk";
    assert sectorSize == SECTOR_SIZE
        || sectorSize == BLOCK_SIZE : "Invalid sector size : " + sectorSize;
    assert interleave >= 0 && interleave <= MAX_INTERLEAVE : "Invalid interleave : "
        + interleave;

    if (sectorSize == SECTOR_SIZE)
    {
      int diskOffset = getBufferOffset (da);
      System.arraycopy (buffer, 0, diskBuffer, diskOffset, SECTOR_SIZE);
    }
    else
    {
      int diskOffset = getBufferOffset (da, 0);
      System.arraycopy (buffer, 0, diskBuffer, diskOffset, SECTOR_SIZE);

      diskOffset = getBufferOffset (da, 1);
      System.arraycopy (buffer, SECTOR_SIZE, diskBuffer, diskOffset, SECTOR_SIZE);
    }
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    return diskBuffer;
  }

  // ---------------------------------------------------------------------------------//
  private int getBufferOffset (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    assert sectorSize == SECTOR_SIZE;

    return da.getTrackNo () * trackSize
        + interleaveSector[interleave][da.getSectorNo ()] * SECTOR_SIZE;
  }

  // ---------------------------------------------------------------------------------//
  private int getBufferOffset (DiskAddress da, int seq)
  // ---------------------------------------------------------------------------------//
  {
    assert sectorSize == BLOCK_SIZE;

    assert seq == 0 || seq == 1;

    return da.getTrackNo () * trackSize
        + interleaveSector[interleave][da.getSectorNo () * 2 + seq] * SECTOR_SIZE;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void addActionListener (ActionListener actionListener)
  // ---------------------------------------------------------------------------------//
  {
    actionListenerList = AWTEventMulticaster.add (actionListenerList, actionListener);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void removeActionListener (ActionListener actionListener)
  // ---------------------------------------------------------------------------------//
  {
    actionListenerList = AWTEventMulticaster.remove (actionListenerList, actionListener);
  }

  // ---------------------------------------------------------------------------------//
  public void notifyListeners (String text)
  // ---------------------------------------------------------------------------------//
  {
    if (actionListenerList != null)
      actionListenerList
          .actionPerformed (new ActionEvent (this, ActionEvent.ACTION_PERFORMED, text));
  }

  // ---------------------------------------------------------------------------------//
  //  private AppleFileSource getDetails ()
  //  // ---------------------------------------------------------------------------------//
  //  {
  //    return new DefaultAppleFileSource (toString (), file.getName (), null);
  //  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    String path = file.getAbsolutePath ();
    String home = System.getProperty ("user.home");
    if (path.startsWith (home))
      path = "~" + path.substring (home.length ());

    text.append (String.format ("Path ......... %s%n", path));
    text.append (String.format ("File name .... %s%n", file.getName ()));
    text.append (String.format ("File size .... %,d%n", file.length ()));
    text.append (String.format ("Tracks ....... %d%n", tracks));
    text.append (String.format ("Sectors ...... %d%n", sectors));
    text.append (String.format ("Blocks ....... %,d%n", blocks));
    text.append (String.format ("Track size ... %,d%n", trackSize));
    text.append (String.format ("Sector size .. %d%n", sectorSize));
    text.append (String.format ("Interleave ... %d%n%n", interleave));

    if (wozFile != null)
      text.append (wozFile);
    else if (nuFX != null)
      text.append (nuFX);
    else if (bin2 != null)
      text.append (bin2);
    else if (prefixDiskCopy != null)
      text.append (prefixDiskCopy);
    else if (prefix2mg != null)
      text.append (prefix2mg);

    return Utility.rtrim (text).toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public Iterator<DiskAddress> iterator ()
  // ---------------------------------------------------------------------------------//
  {
    if (blockList == null)
    {
      blockList = new ArrayList<> (blocks);
      for (int block = 0; block < blocks; block++)
        blockList.add (new AppleDiskAddress (this, block));
    }

    return blockList.iterator ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public long getBootChecksum ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = readBlock (0, 0);
    Checksum checksum = new CRC32 ();
    checksum.update (buffer, 0, buffer.length);
    return checksum.getValue ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setEmptyByte (byte value)
  // ---------------------------------------------------------------------------------//
  {
    emptyByte = value;
    checkSectorsForData ();
  }
}