package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.disk.AppleDiskAddress;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.dos.DosDisk.FileType;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class CatalogEntry extends AbstractCatalogEntry
// -----------------------------------------------------------------------------------//
{
  private int textFileGaps;
  private int length;
  private int address;

  // ---------------------------------------------------------------------------------//
  CatalogEntry (DosDisk dosDisk, DiskAddress catalogSector, byte[] entryBuffer)
  // ---------------------------------------------------------------------------------//
  {
    super (dosDisk, catalogSector, entryBuffer);

    //    if (reportedSize > 0 && disk.isValidAddress (entryBuffer[0], entryBuffer[1]))
    if (disk.isValidAddress (entryBuffer[0], entryBuffer[1]))
    {
      // Get address of first TS-list sector
      DiskAddress da = disk.getDiskAddress (entryBuffer[0], entryBuffer[1]);

      // Loop through all TS-list sectors
      loop: while (!da.isZero () || ((AppleDiskAddress) da).zeroFlag ())
      {
        if (dosDisk.stillAvailable (da))
        {
          if (isValidCatalogSector (da))
            dosDisk.sectorTypes[da.getBlockNo ()] = dosDisk.tsListSector;
          else
          {
            System.out.printf ("Attempt to assign invalid TS sector " + ": %s from %s%n",
                da, name);
            break;
          }
        }
        else
        {
          System.out.printf (
              "Attempt to assign TS sector to occupied sector " + ": %s from %s%n", da,
              name);
          break;
        }
        tsSectors.add (da);
        byte[] sectorBuffer = disk.readBlock (da);

        int startPtr = 12;
        // the tsList *should* start at 0xC0, but some disks start in the unused bytes
        if (false)
          for (int i = 7; i < startPtr; i++)
            if (sectorBuffer[i] != 0)
            {
              startPtr = i;
              break;
            }

        DiskAddress thisDA = da;
        for (int i = startPtr, max = disk.getBlockSize (); i < max; i += 2)
        {
          da = getValidAddress (sectorBuffer, i);
          if (da == null)
          {
            System.out.printf (
                "T/S list at offset %02X contains an invalid address : %02X, %02X (file %s)%n",
                i, sectorBuffer[i], sectorBuffer[i + 1], name.trim ());
            break loop;
          }
          if (da.isZero () && !((AppleDiskAddress) da).zeroFlag ())
          {
            if (fileType != FileType.Text)
              break;
            ++textFileGaps;
            dataSectors.add (null);
          }
          else
          {
            dataSectors.add (da);
            if (dosDisk.stillAvailable (da))
              dosDisk.sectorTypes[da.getBlockNo ()] = dosDisk.dataSector;
            else
            {
              System.out
                  .print ("Attempt to assign Data sector to occupied sector : " + da);
              System.out.println (" from " + name);
            }
          }
        }

        da = getValidAddress (sectorBuffer, 1);
        if (da == null)
        {
          System.out.print ("Next T/S list in sector " + thisDA);
          System.out.printf (" is invalid : %02X, %02X%n", sectorBuffer[1],
              sectorBuffer[2]);
          break;
        }

        if (thisDA.matches (da) && ((AppleDiskAddress) thisDA)
            .zeroFlag () == ((AppleDiskAddress) da).zeroFlag ())
        {
          System.out.printf ("Next T/S list in sector %s points to itself%n", thisDA);
          break;
        }
      }
    }

    // remove trailing empty sectors
    if (fileType == FileType.Text)
    {
      while (dataSectors.size () > 0)
      {
        DiskAddress da = dataSectors.get (dataSectors.size () - 1);
        if (da == null)
        {
          dataSectors.remove (dataSectors.size () - 1);
          --textFileGaps;
        }
        else
          break;
      }
    }
    else if (dataSectors.size () > 0)       // get the file length
    {
      byte[] buffer = disk.readBlock (dataSectors.get (0));
      switch (fileType)
      {
        case IntegerBasic:
        case ApplesoftBasic:
          length = Utility.unsignedShort (buffer, 0);
          break;

        default:
          address = Utility.unsignedShort (buffer, 0);
          length = Utility.unsignedShort (buffer, 2);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private boolean isValidCatalogSector (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = da.readBlock ();

    if (!da.getDisk ().isValidAddress (buffer[1], buffer[2]))
      return false;
    if (buffer[3] != 0 || buffer[4] != 0)         // not supposed to be used
      // Diags2E.dsk stores its own sector address here
      if (da.getTrackNo () != (buffer[3] & 0xFF)
          && da.getSectorNo () != (buffer[4] & 0xFF))
        return false;

    return true;
  }

  // ---------------------------------------------------------------------------------//
  String getDetails ()
  // ---------------------------------------------------------------------------------//
  {
    int actualSize = dataSectors.size () + tsSectors.size () - textFileGaps;
    String addressText = address == 0 ? "" : String.format ("$%4X", address);
    String lengthText = length == 0 ? "" : String.format ("$%4X  %,6d", length, length);
    String message = "";
    String lockedFlag = (locked) ? "*" : " ";

    if (dosDisk.dosVTOCSector.dosVersion >= 0x41)
      message = lastModified.toString ().replace ('T', ' ');

    if (reportedSize != actualSize)
      message += "Actual size (" + actualSize + ") ";
    //    if (dataSectors.size () == 0)
    //      message += "No data ";
    if (reportedSize > 999)
      message += "Reported " + reportedSize;

    String text =
        String.format ("%1s  %1s  %03d  %-30.30s  %-5s  %-13s %3d %3d   %s", lockedFlag,
            getFileType (), reportedSize % 1000, displayName, addressText, lengthText,
            tsSectors.size (), (dataSectors.size () - textFileGaps), message.trim ());
    //    if (actualSize == 0)
    //      text = text.substring (0, 50);

    return text;
  }
}