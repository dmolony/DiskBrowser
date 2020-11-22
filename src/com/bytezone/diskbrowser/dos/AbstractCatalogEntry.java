package com.bytezone.diskbrowser.dos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.applefile.ApplesoftBasicProgram;
import com.bytezone.diskbrowser.applefile.AssemblerProgram;
import com.bytezone.diskbrowser.applefile.BasicTextFile;
import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.applefile.DoubleHiResImage;
import com.bytezone.diskbrowser.applefile.ErrorMessageFile;
import com.bytezone.diskbrowser.applefile.FontFile;
import com.bytezone.diskbrowser.applefile.HiResImage;
import com.bytezone.diskbrowser.applefile.IntegerBasicProgram;
import com.bytezone.diskbrowser.applefile.MerlinSource;
import com.bytezone.diskbrowser.applefile.OriginalHiResImage;
import com.bytezone.diskbrowser.applefile.PrintShopGraphic;
import com.bytezone.diskbrowser.applefile.ShapeTable;
import com.bytezone.diskbrowser.applefile.VisicalcFile;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.dos.DosDisk.FileType;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
abstract class AbstractCatalogEntry implements AppleFileSource
// -----------------------------------------------------------------------------------//
{
  protected Disk disk;
  protected DosDisk dosDisk;
  protected String name;
  protected String catalogName;
  protected String displayName;

  protected FileType fileType;
  protected int reportedSize;
  protected boolean locked;
  protected DataSource appleFile;
  protected LocalDateTime lastModified;

  protected DiskAddress catalogSectorDA;
  protected final List<DiskAddress> dataSectors = new ArrayList<> ();
  protected final List<DiskAddress> tsSectors = new ArrayList<> ();

  private CatalogEntry link;

  // ---------------------------------------------------------------------------------//
  AbstractCatalogEntry (DosDisk dosDisk, DiskAddress catalogSector, byte[] entryBuffer)
  // ---------------------------------------------------------------------------------//
  {
    this.dosDisk = dosDisk;
    this.disk = dosDisk.getDisk ();
    this.catalogSectorDA = catalogSector;

    name = getName ("", entryBuffer);
    reportedSize = Utility.unsignedShort (entryBuffer, 33);
    //    if (reportedSize == 0)
    //      System.out.printf ("%s size 0%n", name);

    int type = entryBuffer[2] & 0x7F;
    locked = (entryBuffer[2] & 0x80) != 0;

    if (type == 0)
      fileType = FileType.Text;
    else if ((type == 0x01))
      fileType = FileType.IntegerBasic;
    else if ((type == 0x02))
      fileType = FileType.ApplesoftBasic;
    else if ((type < 0x08))
      fileType = FileType.Binary;
    else if ((type < 0x10))
      fileType = FileType.SS;
    else if ((type < 0x20))
      fileType = FileType.Relocatable;
    else if ((type < 0x40))
      fileType = FileType.AA;
    //    else if ((type == 0x40))          // Lisa
    else
      fileType = FileType.Binary;
    //    else
    //    {
    //      System.out.println ("Unknown file type : " + type);
    //    }

    if (dosDisk.getVersion () >= 0x41)
      lastModified = Utility.getDateTime (entryBuffer, 0x1B);

    // CATALOG command only formats the LO byte - see Beneath Apple DOS pp4-6
    String base =
        String.format ("%s%s %03d ", (locked) ? "*" : " ", getFileType (), reportedSize);
    catalogName = getName (base, entryBuffer).replace ("^", "");
    displayName = getDisplayName (entryBuffer);
  }

  // ---------------------------------------------------------------------------------//
  private String getName (String base, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (base);

    int max = buffer[0] == (byte) 0xFF ? 32 : 33;
    if (dosDisk.getVersion () >= 0x41)
      max = 27;

    for (int i = 3; i < max; i++)
    {
      int c = buffer[i] & 0xFF;
      if (c == 136 && !base.isEmpty ())             // allow backspaces
      {
        if (text.length () > 0)
          text.deleteCharAt (text.length () - 1);
        continue;
      }
      if (c > 127)
        c -= c < 160 ? 64 : 128;
      if (c < 32)
        text.append ("^" + (char) (c + 64));        // non-printable ascii
      else
        text.append ((char) c);                     // standard ascii
    }

    while (text.length () > 0 && text.charAt (text.length () - 1) == ' ')
      text.deleteCharAt (text.length () - 1);       // rtrim()

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String getDisplayName (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int max = buffer[0] == (byte) 0xFF ? 32 : 33;
    if (dosDisk.getVersion () >= 0x41)
      max = 27;

    for (int i = 3; i < max; i++)
    {
      int c = buffer[i] & 0xFF;
      if (c == 136)             // don't allow backspaces
        continue;

      if (c > 127)
        c -= c < 160 ? 64 : 128;
      if (c < 32)
        text.append ((char) (c + 64));              // non-printable ascii
      else
        text.append ((char) c);                     // standard ascii
    }

    while (text.length () > 0 && text.charAt (text.length () - 1) == ' ')
      text.deleteCharAt (text.length () - 1);       // rtrim()

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  protected String getFileType ()
  // ---------------------------------------------------------------------------------//
  {
    if (fileType == null)
    {
      return "?";
    }
    switch (fileType)
    {
      case Text:
        return "T";
      case IntegerBasic:
        return "I";
      case ApplesoftBasic:
        return "A";
      case Binary:
        return "B";
      case SS:                    // what is this?
        return "S";
      case Relocatable:
        return "R";
      case AA:                    // what is this?
        return "A";
      case BB:                    // Lisa file
        return "L";
      default:
        System.out.println ("Unknown file type : " + fileType);
        return "?";
    }
  }

  // maybe this should be in the FormattedDisk
  // maybe DiskAddress should have a 'valid' flag
  // ---------------------------------------------------------------------------------//
  protected DiskAddress getValidAddress (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    if (disk.isValidAddress (buffer[offset], buffer[offset + 1]))
      return disk.getDiskAddress (buffer[offset], buffer[offset + 1]);
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public DataSource getDataSource ()
  // ---------------------------------------------------------------------------------//
  {
    if (appleFile != null)
      return appleFile;

    byte[] buffer = disk.readBlocks (dataSectors);
    int reportedLength;
    if (buffer.length == 0)
    {
      appleFile = new DefaultAppleFile (name, buffer);
      return appleFile;
    }

    try
    {
      byte[] exactBuffer;

      switch (this.fileType)
      {
        case Text:
          if (VisicalcFile.isVisicalcFile (buffer))
            appleFile = new VisicalcFile (name, buffer);
          else
            appleFile = new BasicTextFile (name, buffer);
          break;

        case IntegerBasic:
          reportedLength = Utility.unsignedShort (buffer, 0);
          exactBuffer = new byte[reportedLength];
          System.arraycopy (buffer, 2, exactBuffer, 0, reportedLength);
          appleFile = new IntegerBasicProgram (name, exactBuffer);
          break;

        case ApplesoftBasic:
          reportedLength = Utility.unsignedShort (buffer, 0);
          exactBuffer = new byte[reportedLength];
          if (reportedLength > buffer.length)
            reportedLength = buffer.length - 2;
          System.arraycopy (buffer, 2, exactBuffer, 0, reportedLength);
          appleFile = new ApplesoftBasicProgram (name, exactBuffer);
          break;

        case Binary:                        // binary file
        case Relocatable:                   // relocatable binary file
        case BB:
          int loadAddress = Utility.unsignedShort (buffer, 0);
          reportedLength = Utility.unsignedShort (buffer, 2);
          if (reportedLength == 0)
          {
            System.out.println (name.trim () + " reported length : 0 - reverting to "
                + (buffer.length - 4));
            reportedLength = buffer.length - 4;
          }

          // buffer is a multiple of the block size, so it usually needs to be reduced
          if ((reportedLength + 4) <= buffer.length)
            exactBuffer = new byte[reportedLength];
          else
            exactBuffer = new byte[buffer.length - 4];  // reported length is too long
          System.arraycopy (buffer, 4, exactBuffer, 0, exactBuffer.length);

          if ((name.endsWith (".FONT") || name.endsWith (" FONT")
              || name.endsWith (".SET") || name.startsWith ("ASCII."))
              && FontFile.isFont (exactBuffer))
            appleFile = new FontFile (name, exactBuffer, loadAddress);
          else if (ShapeTable.isShapeTable (exactBuffer))
            appleFile = new ShapeTable (name, exactBuffer);
          else if (name.endsWith (".S"))
            appleFile = new MerlinSource (name, exactBuffer, loadAddress);
          else if (HiResImage.isGif (exactBuffer))    // buffer?
            appleFile = new OriginalHiResImage (name, exactBuffer, loadAddress);
          else if (HiResImage.isPng (exactBuffer))    // buffer?
            appleFile = new OriginalHiResImage (name, exactBuffer, loadAddress);
          else if (name.endsWith (".BMP") && HiResImage.isBmp (buffer))
            appleFile = new OriginalHiResImage (name, buffer, loadAddress);
          else if (name.endsWith (".PAC"))
            appleFile = new DoubleHiResImage (name, exactBuffer);
          else if (link != null)
          {
            byte[] auxBuffer = link.disk.readBlocks (link.dataSectors);
            byte[] exactAuxBuffer = getExactBuffer (auxBuffer);
            if (name.endsWith (".AUX"))
              appleFile = new DoubleHiResImage (name, exactAuxBuffer, exactBuffer);
            else
              appleFile = new DoubleHiResImage (name, exactBuffer, exactAuxBuffer);
          }
          else if (loadAddress == 0x2000 || loadAddress == 0x4000)
          {
            if (reportedLength > 0x1F00 && reportedLength <= 0x4000)
              appleFile = new OriginalHiResImage (name, exactBuffer, loadAddress);
            else if (isScrunched (reportedLength))
              appleFile = new OriginalHiResImage (name, exactBuffer, loadAddress, true);
            else
              appleFile = new AssemblerProgram (name, exactBuffer, loadAddress);
          }
          else if (reportedLength == 0x240          //
              && (loadAddress == 0x5800 || loadAddress == 0x6000
                  || loadAddress == 0x7800))
            appleFile = new PrintShopGraphic (name, exactBuffer);
          else if (isRunCommand (exactBuffer))
          {
            byte[] buf = new byte[exactBuffer.length - 4];
            System.arraycopy (exactBuffer, 4, buf, 0, buf.length);
            appleFile = new ApplesoftBasicProgram (name, buf);
            System.out.printf ("Possible basic binary: %s%n", name);
          }
          else
          {
            appleFile = new AssemblerProgram (name, exactBuffer, loadAddress);
            if ((exactBuffer.length + 4) < buffer.length)
              ((AssemblerProgram) appleFile).setExtraBuffer (buffer,
                  exactBuffer.length + 4, buffer.length - (exactBuffer.length + 4));
          }
          break;

        case SS:                                          // what is this?
          System.out.println ("SS file");
          appleFile = new DefaultAppleFile (name, buffer);
          break;

        case AA:                                          // what is this?
          System.out.println ("AA file");
          appleFile = new DefaultAppleFile (name, buffer);
          break;

        //        case BB:                                          // Lisa
        //          loadAddress = Utility.intValue (buffer[0], buffer[1]);
        //          appleFile = new SimpleText2 (name, getExactBuffer (buffer), loadAddress);
        //          break;

        default:
          System.out.println ("Unknown file type : " + fileType);
          appleFile = new DefaultAppleFile (name, buffer);
          break;
      }
    }
    catch (Exception e)
    {
      appleFile = new ErrorMessageFile (name, buffer, e);
      e.printStackTrace ();
    }
    return appleFile;
  }

  // ---------------------------------------------------------------------------------//
  private byte[] getExactBuffer (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    byte[] exactBuffer;

    int reportedLength = Utility.unsignedShort (buffer, 2);
    if (reportedLength == 0)
    {
      System.out.println (
          name.trim () + " reported length : 0 - reverting to " + (buffer.length - 4));
      reportedLength = buffer.length - 4;
    }

    // buffer is a multiple of the block size, so it usually needs to be reduced
    if ((reportedLength + 4) <= buffer.length)
      exactBuffer = new byte[reportedLength];
    else
      exactBuffer = new byte[buffer.length - 4];  // reported length is too long
    System.arraycopy (buffer, 4, exactBuffer, 0, exactBuffer.length);

    return exactBuffer;
  }

  // ---------------------------------------------------------------------------------//
  private boolean isRunCommand (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    // see Stargate - Disk 1, Side A.woz
    return buffer[0] == 0x4C && buffer[1] == (byte) 0xFC && buffer[2] == (byte) 0xA4
        && buffer[3] == 0x00;
  }

  // ---------------------------------------------------------------------------------//
  private boolean isScrunched (int reportedLength)
  // ---------------------------------------------------------------------------------//
  {
    if ((name.equals ("FLY LOGO") || name.equals ("FLY LOGO SCRUNCHED"))
        && reportedLength == 0x14FA)
      return true;

    if (name.equals ("BBROS LOGO SCRUNCHED") && reportedLength == 0x0FED)
      return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public boolean contains (DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    for (DiskAddress sector : tsSectors)
      if (sector.matches (da))
        return true;

    for (DiskAddress sector : dataSectors)
      // random access files may have gaps, and thus null sectors
      //      is this still true? I thought I was using sector zero objects??
      if (sector != null && sector.matches (da))
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getUniqueName ()
  // ---------------------------------------------------------------------------------//
  {
    // this might not be unique if the file has been deleted
    return name;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public FormattedDisk getFormattedDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return dosDisk;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getSectors ()
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> sectors = new ArrayList<> ();
    sectors.add (catalogSectorDA);
    sectors.addAll (tsSectors);
    sectors.addAll (dataSectors);
    return sectors;
  }

  // ---------------------------------------------------------------------------------//
  void link (CatalogEntry catalogEntry)
  // ---------------------------------------------------------------------------------//
  {
    this.link = catalogEntry;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return catalogName;
  }
}