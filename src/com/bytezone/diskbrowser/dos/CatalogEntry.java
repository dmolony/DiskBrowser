package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.dos.DosDisk.FileType;

class CatalogEntry extends AbstractCatalogEntry
{
  int textFileGaps;
  int length;
  int address;

  public CatalogEntry (DosDisk dosDisk, DiskAddress catalogSector, byte[] entryBuffer)
  {
    super (dosDisk, catalogSector, entryBuffer); // build lists of ts and data sectors
    if (reportedSize > 0 && disk.isValidAddress (entryBuffer[0], entryBuffer[1]))
    {
      // Get address of first TS-list sector
      DiskAddress da = disk.getDiskAddress (entryBuffer[0], entryBuffer[1]);

      // Loop through all TS-list sectors
      loop: while (da.getBlock () > 0)
      {
        if (dosDisk.stillAvailable (da))
          dosDisk.sectorTypes[da.getBlock ()] = dosDisk.tsListSector;
        else
        {
          System.out.print ("Attempt to assign TS sector to occupied sector : " + da);
          System.out.println (" from " + name);
        }
        tsSectors.add (da);
        byte[] sectorBuffer = disk.readSector (da);

        int startPtr = 12;
        // the tsList *should* start at 0xC0, but some disks start in the unused bytes
        if (false)
          for (int i = 7; i < startPtr; i++)
            if (sectorBuffer[i] != 0)
            {
              startPtr = i;
              break;
            }

        for (int i = startPtr, max = disk.getBlockSize (); i < max; i += 2)
        {
          da = getValidAddress (sectorBuffer, i);
          if (da == null)
          {
            System.out.print ("T/S list in sector " + i);
            System.out.printf (" contains an invalid address : %02X, %02X (file %s)%n",
                               sectorBuffer[i], sectorBuffer[i + 1], name.trim ());
            break loop;
          }
          if (da.getBlock () == 0)
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
              dosDisk.sectorTypes[da.getBlock ()] = dosDisk.dataSector;
            else
            {
              System.out.print ("Attempt to assign Data sector to occupied sector : " + da);
              System.out.println (" from " + name);
            }
          }
        }

        da = getValidAddress (sectorBuffer, 1);
        if (da == null)
        {
          System.out.print ("Next T/S list in sector " + da);
          System.out.printf (" is invalid : %02X, %02X%n", sectorBuffer[1], sectorBuffer[2]);
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

    // get the file length
    if (dataSectors.size () > 0 && fileType != FileType.Text)
    {
      byte[] buffer = disk.readSector (dataSectors.get (0));
      switch (fileType)
      {
        case IntegerBasic:
          //          length = HexFormatter.intValue (buffer[0], buffer[1]);
          //          break;
        case ApplesoftBasic:
          length = HexFormatter.intValue (buffer[0], buffer[1]);
          break;
        default:
          address = HexFormatter.intValue (buffer[0], buffer[1]);
          length = HexFormatter.intValue (buffer[2], buffer[3]);
      }
    }
  }

  public String getDetails ()
  {
    int actualSize = dataSectors.size () + tsSectors.size () - textFileGaps;
    String addressText = address == 0 ? "" : String.format ("$%4X", address);
    String lengthText = length == 0 ? "" : String.format ("$%4X  %,6d", length, length);
    String message = "";
    String lockedFlag = (locked) ? "*" : " ";
    if (reportedSize != actualSize)
      message += "Bad size (" + reportedSize + ") ";
    if (dataSectors.size () == 0)
      message += "No data ";
    return String.format ("%1s  %1s  %03d  %-30.30s  %-5s  %-13s  %2d %3d   %s", lockedFlag,
                          getFileType (), actualSize, name, addressText, lengthText,
                          tsSectors.size (), (dataSectors.size () - textFileGaps),
                          message.trim ());
  }
}