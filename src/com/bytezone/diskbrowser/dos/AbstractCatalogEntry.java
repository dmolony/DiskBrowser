package com.bytezone.diskbrowser.dos;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.*;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.dos.DosDisk.FileType;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

abstract class AbstractCatalogEntry implements AppleFileSource
{
  protected Disk disk;
  protected DosDisk dosDisk;
  protected String name;
  protected String catalogName;

  protected FileType fileType;
  protected int reportedSize;
  protected boolean locked;
  protected DataSource appleFile;

  protected DiskAddress catalogSectorDA;
  protected final List<DiskAddress> dataSectors = new ArrayList<DiskAddress> ();
  protected final List<DiskAddress> tsSectors = new ArrayList<DiskAddress> ();

  public AbstractCatalogEntry (DosDisk dosDisk, DiskAddress catalogSector,
      byte[] entryBuffer)
  {
    this.dosDisk = dosDisk;
    this.disk = dosDisk.getDisk ();
    this.catalogSectorDA = catalogSector;
    reportedSize = HexFormatter.intValue (entryBuffer[33], entryBuffer[34]);
    int type = entryBuffer[2] & 0xFF;
    locked = (type & 0x80) > 0;
    this.disk = dosDisk.getDisk ();

    if ((type & 0x7F) == 0)
      fileType = FileType.Text;
    else if ((type & 0x01) > 0)
      fileType = FileType.IntegerBasic;
    else if ((type & 0x02) > 0)
      fileType = FileType.ApplesoftBasic;
    else if ((type & 0x04) > 0)
      fileType = FileType.Binary;
    else if ((type & 0x08) > 0)
      fileType = FileType.SS;
    else if ((type & 0x10) > 0)
      fileType = FileType.Relocatable;
    else if ((type & 0x20) > 0)
      fileType = FileType.AA;
    else if ((type & 0x40) > 0)
      fileType = FileType.BB;
    else
      System.out.println ("Unknown file type : " + (type & 0x7F));

    name = getName ("", entryBuffer);
    // CATALOG command only formats the LO byte - see Beneath Apple DOS pp4-6
    String base = String.format ("%s%s %03d ", (locked) ? "*" : " ", getFileType (),
        (entryBuffer[33] & 0xFF));
    catalogName = getName (base, entryBuffer);
  }

  private String getName (String base, byte[] buffer)
  {
    StringBuilder text = new StringBuilder (base);
    int max = buffer[0] == (byte) 0xFF ? 32 : 33;
    for (int i = 3; i < max; i++)
    {
      int c = buffer[i] & 0xFF;
      if (c == 136 && !base.isEmpty ()) // allow backspaces
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

  protected String getFileType ()
  {
    switch (this.fileType)
    {
      case Text:
        return "T";
      case IntegerBasic:
        return "I";
      case ApplesoftBasic:
        return "A";
      case Binary:
        return "B";
      case SS: // what is this?
        return "S";
      case Relocatable:
        return "R";
      case AA: // what is this?
        return "A";
      case BB: // what is this?
        return "B";
      default:
        System.out.println ("Unknown file type : " + fileType);
        return "?";
    }
  }

  // maybe this should be in the FormattedDisk
  // maybe DiskAddress should have a 'valid' flag
  protected DiskAddress getValidAddress (byte[] buffer, int offset)
  {
    if (disk.isValidAddress (buffer[offset], buffer[offset + 1]))
      return disk.getDiskAddress (buffer[offset], buffer[offset + 1]);
    return null;
  }

  @Override
  public DataSource getDataSource ()
  {
    if (appleFile != null)
      return appleFile;

    byte[] buffer = disk.readSectors (dataSectors);
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
            appleFile = new TextFile (name, buffer);
          break;

        case IntegerBasic:
          reportedLength = HexFormatter.intValue (buffer[0], buffer[1]);
          exactBuffer = new byte[reportedLength];
          System.arraycopy (buffer, 2, exactBuffer, 0, reportedLength);
          appleFile = new IntegerBasicProgram (name, exactBuffer);
          break;

        case ApplesoftBasic:
          reportedLength = HexFormatter.intValue (buffer[0], buffer[1]);
          exactBuffer = new byte[reportedLength];
          if (reportedLength > buffer.length)
            reportedLength = buffer.length - 2;
          System.arraycopy (buffer, 2, exactBuffer, 0, reportedLength);
          //          appleFile = new ApplesoftBasicProgram (name, exactBuffer);
          appleFile = new BasicProgram (name, exactBuffer);
          break;

        case Binary:                        // binary file
        case Relocatable:                   // relocatable binary file
          //          if (buffer.length == 0)
          //            appleFile = new AssemblerProgram (name, buffer, 0);
          //          else
          //          {
          int loadAddress = HexFormatter.intValue (buffer[0], buffer[1]);
          reportedLength = HexFormatter.intValue (buffer[2], buffer[3]);
          if (reportedLength == 0)
          {
            System.out.println (name.trim () + " reported length : 0 - reverting to "
                + (buffer.length - 4));
            reportedLength = buffer.length - 4;
          }

          // buffer is a multiple of the block size, so it usually needs to be reduced
          if ((reportedLength + 4) <= buffer.length)
          {
            exactBuffer = new byte[reportedLength];
            //              extraBuffer = new byte[buffer.length - reportedLength - 4];
            //              System.arraycopy (buffer, reportedLength + 4, extraBuffer, 0,
            //                                extraBuffer.length);
          }
          else
            exactBuffer = new byte[buffer.length - 4];  // reported length is too long

          System.arraycopy (buffer, 4, exactBuffer, 0, exactBuffer.length);

          if (name.endsWith (".FONT") || name.endsWith (".SET"))
            appleFile = new FontFile (name, exactBuffer);
          else if (ShapeTable.isShapeTable (exactBuffer))
            appleFile = new ShapeTable (name, exactBuffer);
          else if (name.endsWith (".S"))
            appleFile = new MerlinSource (name, exactBuffer);
          else if (HiResImage.isGif (exactBuffer))
            appleFile = new HiResImage (name, exactBuffer);
          else if (loadAddress == 0x2000 || loadAddress == 0x4000)
          {
            if (reportedLength > 0x1F00 && reportedLength <= 0x4000)
              appleFile = new HiResImage (name, exactBuffer);
            else if (isScrunched (reportedLength))
              appleFile = new HiResImage (name, exactBuffer, true);
            else
              appleFile = new AssemblerProgram (name, exactBuffer, loadAddress);
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

        case BB:                                          // what is this?
          loadAddress = HexFormatter.intValue (buffer[0], buffer[1]);
          reportedLength = HexFormatter.intValue (buffer[2], buffer[3]);
          exactBuffer = new byte[reportedLength];
          System.arraycopy (buffer, 4, exactBuffer, 0, reportedLength);
          appleFile = new SimpleText2 (name, exactBuffer, loadAddress);
          break;

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

  private boolean isScrunched (int reportedLength)
  {
    if ((name.equals ("FLY LOGO") || name.equals ("FLY LOGO SCRUNCHED"))
        && reportedLength == 0x14FA)
      return true;

    //    if (name.endsWith (".PAC"))
    //      return true;

    if (name.equals ("BBROS LOGO SCRUNCHED") && reportedLength == 0x0FED)
      return true;

    return false;
  }

  @Override
  public boolean contains (DiskAddress da)
  {
    for (DiskAddress sector : tsSectors)
      if (sector.matches (da))
        return true;
    for (DiskAddress sector : dataSectors)
      // random access files may have gaps, and thus null sectors
      if (sector != null && sector.matches (da))
        return true;
    return false;
  }

  @Override
  public String getUniqueName ()
  {
    // this might not be unique if the file has been deleted
    return name;
  }

  @Override
  public FormattedDisk getFormattedDisk ()
  {
    return dosDisk;
  }

  @Override
  public List<DiskAddress> getSectors ()
  {
    List<DiskAddress> sectors = new ArrayList<DiskAddress> ();
    sectors.add (catalogSectorDA);
    sectors.addAll (tsSectors);
    sectors.addAll (dataSectors);
    return sectors;
  }

  @Override
  public String toString ()
  {
    return catalogName;
  }
}