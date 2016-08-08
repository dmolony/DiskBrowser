package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.pascal.PascalDisk;
import com.bytezone.diskbrowser.utilities.Utility;

public class Wizardry4BootDisk extends PascalDisk
{
  List<AppleDisk> disks = new ArrayList<AppleDisk> ();

  public Wizardry4BootDisk (AppleDisk[] dataDisks)
  {
    super (dataDisks[0]);

    for (AppleDisk dataDisk : dataDisks)
      relocator.addDisk (dataDisk);
  }

  public static boolean isWizardryIV (Disk disk, boolean debug)
  {
    byte[] header = { 0x00, (byte) 0xEA, (byte) 0xA9, 0x60, (byte) 0x8D, 0x01, 0x08 };
    byte[] buffer = disk.readSector (0);

    if (!Utility.matches (buffer, 0, header))
      return false;
    buffer = disk.readSector (1);

    if (buffer[510] != 1)
      return false;

    return true;
  }
}