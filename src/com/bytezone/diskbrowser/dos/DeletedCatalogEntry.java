package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.disk.AppleDiskAddress;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.gui.DataSource;

// -----------------------------------------------------------------------------------//
class DeletedCatalogEntry extends AbstractCatalogEntry
// -----------------------------------------------------------------------------------//
{
  boolean allSectorsAvailable = true;
  boolean debug = false;

  // ---------------------------------------------------------------------------------//
  DeletedCatalogEntry (DosDisk dosDisk, DiskAddress catalogSector, byte[] entryBuffer,
      int dosVersion)
  // ---------------------------------------------------------------------------------//
  {
    super (dosDisk, catalogSector, entryBuffer);

    if (debug)
    {
      System.out.println ("Deleted file  : " + name);
      System.out.printf ("Reported size : %d%n", reportedSize);
    }

    DiskAddress da = null;

    // Get address of first TS-list sector
    if (dosVersion >= 0x41)
    {
      int track = entryBuffer[0] & 0x3F;
      int sector = entryBuffer[1] & 0x1F;
      da = disk.getDiskAddress (track, sector);
    }
    else
    {
      int track = entryBuffer[32] & 0xFF;
      int sector = entryBuffer[1] & 0xFF;
      da = disk.getDiskAddress (track, sector);
    }
    int totalBlocks = 0;

    if (reportedSize <= 1 || !disk.isValidAddress (da.getTrack (), da.getSector ()))
    {
      if (debug)
        System.out.println ("invalid catalog entry");
      allSectorsAvailable = false;
      return;
    }

    // Loop through all TS-list sectors
    loop: while (da.getBlock () > 0 || ((AppleDiskAddress) da).zeroFlag ())
    {
      if (!dosDisk.stillAvailable (da))
      {
        allSectorsAvailable = false;
        break;
      }
      tsSectors.add (da);
      totalBlocks++;

      byte[] sectorBuffer = disk.readSector (da);
      for (int i = 12, max = disk.getBlockSize (); i < max; i += 2)
      {
        da = getValidAddress (sectorBuffer, i);
        if (da == null)
          break loop;
        if (da.getBlock () > 0 && debug)
          System.out.println (da);

        if (da.getBlock () > 0 || ((AppleDiskAddress) da).zeroFlag ())
        {
          if (!dosDisk.stillAvailable (da))
          {
            allSectorsAvailable = false;
            break loop;
          }
          dataSectors.add (da);
          totalBlocks++;
        }
      }

      da = getValidAddress (sectorBuffer, 1);

      if (da == null)
      {
        System.out.printf ("Next T/S list in sector %s is invalid : %02X, %02X%n", da,
            sectorBuffer[1], sectorBuffer[2]);
        break;
      }
    }
    if (debug)
      System.out.printf ("Total blocks recoverable : %d%n", totalBlocks);
    if (totalBlocks != reportedSize)
      allSectorsAvailable = false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getUniqueName ()
  // ---------------------------------------------------------------------------------//
  {
    // name might not be unique if the file has been deleted
    return "!" + name;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getDataSource ()
  // ---------------------------------------------------------------------------------//
  {
    if (!allSectorsAvailable && appleFile == null)
    {
      DefaultAppleFile daf = new DefaultAppleFile (name, null);
      daf.setText ("This file cannot be recovered");
      appleFile = daf;
    }
    return super.getDataSource ();
  }

  // ---------------------------------------------------------------------------------//
  String getDetails ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("%-30s  %s", name,
        allSectorsAvailable ? "Recoverable" : "Not recoverable");
  }
}