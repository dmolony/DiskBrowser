package com.bytezone.diskbrowser.dos;

import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.gui.DataSource;

class DeletedCatalogEntry extends AbstractCatalogEntry
{
  boolean allSectorsAvailable = true;
  boolean debug = false;

  public DeletedCatalogEntry (DosDisk dosDisk, DiskAddress catalogSector, byte[] entryBuffer)
  {
    super (dosDisk, catalogSector, entryBuffer);
    //    reportedSize = HexFormatter.intValue (entryBuffer[33], entryBuffer[34]);
    if (debug)
    {
      System.out.println ("Deleted file  : " + name);
      System.out.printf ("Reported size : %d%n", reportedSize);
    }

    if (reportedSize <= 1 || !disk.isValidAddress (entryBuffer[32], entryBuffer[1]))
    {
      if (debug)
        System.out.println ("invalid catalog entry");
      allSectorsAvailable = false;
      return;
    }

    // Get address of first TS-list sector
    DiskAddress da = disk.getDiskAddress (entryBuffer[32], entryBuffer[1]);
    int totalBlocks = 0;

    // Loop through all TS-list sectors
    loop: while (da.getBlock () > 0)
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

        if (da.getBlock () > 0)
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

  @Override
  public String getUniqueName ()
  {
    // name might not be unique if the file has been deleted
    return "!" + name;
  }

  @Override
  public DataSource getDataSource ()
  {
    if (!allSectorsAvailable && appleFile == null)
    {
      DefaultAppleFile daf = new DefaultAppleFile (name, null);
      daf.setText ("This file cannot be recovered");
      appleFile = daf;
    }
    return super.getDataSource ();
  }

  public String getDetails ()
  {
    return String.format ("%-30s  %s", name, allSectorsAvailable ? "Recoverable"
          : "Not recoverable");
  }
}