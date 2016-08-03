package com.bytezone.diskbrowser.disk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import com.bytezone.diskbrowser.cpm.CPMDisk;
import com.bytezone.diskbrowser.dos.DosDisk;
import com.bytezone.diskbrowser.infocom.InfocomDisk;
import com.bytezone.diskbrowser.pascal.PascalDisk;
import com.bytezone.diskbrowser.prodos.ProdosDisk;
import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.NuFX;
import com.bytezone.diskbrowser.wizardry.WizardryScenarioDisk;

public class DiskFactory
{
  private static boolean debug = false;

  private DiskFactory ()
  {
  }

  public static FormattedDisk createDisk (File file)
  {
    return createDisk (file.getAbsolutePath ());
  }

  public static FormattedDisk createDisk (String path)
  {
    if (debug)
      System.out.println ("Factory : " + path);

    File file = new File (path);
    if (!file.exists ())
      return null;

    String suffix = path.substring (path.lastIndexOf (".") + 1).toLowerCase ();
    Boolean compressed = false;
    Path p = Paths.get (path);

    if (suffix.equals ("sdk"))
    {
      try
      {
        NuFX nuFX = new NuFX (p);
        File tmp = File.createTempFile ("sdk", null);
        FileOutputStream fos = new FileOutputStream (tmp);
        fos.write (nuFX.getBuffer ());
        fos.close ();
        tmp.deleteOnExit ();
        file = tmp;
        suffix = "dsk";
        compressed = true;
      }
      catch (IOException e)
      {
        e.printStackTrace ();
        return null;
      }
      catch (FileFormatException e)
      {
        return null;
      }
    }
    else if (suffix.equals ("gz"))    // will be .dsk.gz
    {
      try
      {
        InputStream in = new GZIPInputStream (new FileInputStream (path));
        File tmp = File.createTempFile ("gzip", null);
        FileOutputStream fos = new FileOutputStream (tmp);

        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = in.read (buffer)) > 0)
          fos.write (buffer, 0, bytesRead);

        fos.close ();
        in.close ();
        tmp.deleteOnExit ();
        file = tmp;
        suffix = "dsk";
        compressed = true;
      }
      catch (IOException e)
      {
        e.printStackTrace ();
        return null;
      }
    }

    FormattedDisk disk = null;
    FormattedDisk disk2 = null;

    if (suffix.equals ("hdv"))
    {
      ProdosDisk prodosDisk = checkHardDisk (file);
      if (prodosDisk != null)
        return prodosDisk;

      disk2 = check2mgDisk (file);
      if (disk2 != null)
        return disk2;

      AppleDisk appleDisk = new AppleDisk (file, (int) file.length () / 4096, 8);
      return new DataDisk (appleDisk);
    }

    if (suffix.equals ("2mg"))
    {
      disk2 = check2mgDisk (file);
      if (disk2 != null)
        return disk2;

      AppleDisk appleDisk = new AppleDisk (file, (int) file.length () / 4096, 8);
      return new DataDisk (appleDisk);
    }

    if (((suffix.equals ("po") || suffix.equals ("dsk")) && file.length () > 143360))
    {
      if (debug)
        System.out.println ("Checking po or dsk hard drive: " + file.length ());

      disk = checkHardDisk (file);
      if (disk != null)
      {
        if (compressed)
          disk.setOriginalPath (p);
        return disk;
      }

      if (debug)
        System.out.println ("Creating a data disk from bad length");
      try
      {
        AppleDisk appleDisk = new AppleDisk (file, (int) file.length () / 4096, 8);
        if (debug)
          System.out.println ("created");
        return new DataDisk (appleDisk);
      }
      catch (FileFormatException e)
      {
        if (debug)
          System.out.println ("Creating AppleDisk failed");
        return null;
      }
    }

    long length = file.length ();
    if (length != 143360 && length != 116480)
    {
      System.out.printf ("%s: invalid file length : %,d%n", file.getName (),
                         file.length ());
      return null;
    }

    int sectors = file.length () == 143360 ? 16 : 13;
    if (true)
    {
      AppleDisk appleDisk = new AppleDisk (file, 35, sectors);
      long checksum = appleDisk.getBootChecksum ();

      if (checksum == 3176296590L || checksum == 108825457L || checksum == 1439356606L
          || checksum == 1550012074L || checksum == 1614602459L || checksum == 940889336L
          || checksum == 990032697 || checksum == 2936955085L || checksum == 1348415927L
          || checksum == 3340889101L || checksum == 18315788L || checksum == 993895235L)
      {
        disk = checkDos (file);
        disk2 = checkProdos (file);
        if (disk2 != null && disk != null)
          disk = new DualDosDisk (disk, disk2);
      }

      else if (checksum == 1737448647L || checksum == 170399908L)
      {
        disk = checkProdos (file);
        disk2 = checkDos (file);
        if (disk2 != null && disk != null)
          disk = new DualDosDisk (disk, disk2);
      }

      else if (checksum == 2803644711L || checksum == 3317783349L
          || checksum == 1728863694L || checksum == 198094178L)
        disk = checkPascalDisk (file);

      else if (checksum == 3028642627L || checksum == 2070151659L)
        disk = checkInfocomDisk (file);

      //      else if (checksum == 1212926910L || checksum == 1365043894L
      //          || checksum == 2128073918L)
      //        disk = checkCPMDisk (file);

      //      System.out.println (checksum);

      if (disk != null)
      {
        if (compressed)
          disk.setOriginalPath (p);
        return disk;
      }

      // empty boot sector
      if (checksum != 227968344L && false)
        System.out.println ("Unknown checksum : " + checksum + " : " + path);
    }

    if (suffix.equals ("dsk") || suffix.equals ("do") || suffix.equals ("d13"))
    {
      disk = checkDos (file);
      if (disk == null)
        disk = checkProdos (file);
      else if (sectors == 16)
      {
        if (debug)
          System.out.println ("Checking DualDos disk");
        disk2 = checkProdos (file);
        if (disk2 != null)
          disk = new DualDosDisk (disk, disk2);
      }
    }
    else if (suffix.equals ("po"))
    {
      disk = checkProdos (file);
      if (disk == null)
        disk = checkDos (file);
    }

    if (disk == null)
      disk = checkPascalDisk (file);

    if (disk == null)
      disk = checkCPMDisk (file);

    if (disk == null)
    {
      disk2 = checkInfocomDisk (file);
      if (disk2 != null)
        disk = disk2;
    }

    if (disk == null)
      disk = new DataDisk (new AppleDisk (file, 35, 16));

    if (debug)
      System.out.println ("Factory creating disk : "
          + disk.getDisk ().getFile ().getAbsolutePath ());

    if (disk != null && compressed)
      disk.setOriginalPath (p);

    return disk;
  }

  private static DosDisk checkDos (File file)
  {
    if (debug)
      System.out.println ("Checking DOS disk");
    try
    {
      int sectors = file.length () == 143360 ? 16 : 13;
      AppleDisk disk = new AppleDisk (file, 35, sectors);
      if (DosDisk.isCorrectFormat (disk))
        return new DosDisk (disk);
    }
    catch (Exception e)
    {
    }
    if (debug)
      System.out.println ("Not a DOS disk");
    return null;
  }

  private static ProdosDisk checkProdos (File file)
  {
    if (debug)
      System.out.println ("Checking Prodos disk");

    try
    {
      AppleDisk disk = new AppleDisk (file, 35, 8);
      if (ProdosDisk.isCorrectFormat (disk))
        return new ProdosDisk (disk);
    }
    catch (Exception e)
    {
    }
    if (debug)
      System.out.println ("Not a Prodos disk");
    return null;
  }

  private static ProdosDisk checkHardDisk (File file)
  {
    if (debug)
    {
      System.out.println ("\nChecking Prodos hard disk");
      System.out.printf ("Total blocks : %f%n", (float) file.length () / 512);
      System.out.printf ("Total tracks : %f%n", (float) file.length () / 4096);
      System.out.printf ("File length  : %d%n", file.length ());
      System.out.println ();
    }

    // assumes a sector is 512 bytes
    if ((file.length () % 512) != 0)
    {
      if (debug)
        System.out.printf ("file length not divisible by 512 : %,d%n%n", file.length ());
      return null;
    }

    // assumes a track is 4096 bytes
    //    if ((file.length () % 4096) != 0)
    //    {
    //      if (debug)
    //   System.out.printf ("file length not divisible by 4096 : %d%n%n", file.length ());
    //      return null;
    //    }

    try
    {
      // truncate the file if necessary
      AppleDisk disk = new AppleDisk (file, (int) file.length () / 4096, 8);
      if (ProdosDisk.isCorrectFormat (disk))
      {
        if (debug)
        {
          System.out.println ("Yay, it's a prodos hard disk");
          System.out.println (disk);
        }
        return new ProdosDisk (disk);
      }
    }
    catch (Exception e)
    {
      //      e.printStackTrace ();
      System.out.println (e);
    }

    if (debug)
      System.out.println ("Not a Prodos hard disk\n");

    return null;
  }

  private static FormattedDisk check2mgDisk (File file)
  {
    if (debug)
      System.out.println ("Checking Prodos 2mg disk");

    try
    {
      AppleDisk disk = new AppleDisk (file, 0, 0);
      if (ProdosDisk.isCorrectFormat (disk))
        return new ProdosDisk (disk);
    }
    catch (Exception e)
    {
    }
    if (debug)
      System.out.println ("Not a Prodos 2mg disk");

    return null;
  }

  private static FormattedDisk checkPascalDisk (File file)
  {
    if (debug)
      System.out.println ("Checking Pascal disk");
    AppleDisk disk = new AppleDisk (file, 35, 8);
    if (!PascalDisk.isCorrectFormat (disk, debug))
      return null;
    if (debug)
      System.out.println ("Pascal disk OK - Checking Wizardry disk");
    if (WizardryScenarioDisk.isWizardryFormat (disk, debug))
      return new WizardryScenarioDisk (disk);
    if (debug)
      System.out.println ("Not a Wizardry disk");
    return new PascalDisk (disk);
  }

  private static InfocomDisk checkInfocomDisk (File file)
  {
    if (debug)
      System.out.println ("Checking Infocom disk");
    AppleDisk disk = new AppleDisk (file, 35, 16);
    if (InfocomDisk.isCorrectFormat (disk))
      return new InfocomDisk (disk);
    if (debug)
      System.out.println ("Not an InfocomDisk disk");
    return null;
  }

  private static CPMDisk checkCPMDisk (File file)
  {
    if (debug)
      System.out.println ("Checking CPM disk");
    AppleDisk disk = new AppleDisk (file, 35, 16);
    if (CPMDisk.isCorrectFormat (disk))
      return new CPMDisk (disk);
    if (debug)
      System.out.println ("Not a CPM disk");
    return null;
  }
}