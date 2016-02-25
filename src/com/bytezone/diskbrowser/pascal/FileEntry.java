package com.bytezone.diskbrowser.pascal;

import com.bytezone.diskbrowser.applefile.*;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class FileEntry extends CatalogEntry
{
  int bytesUsedInLastBlock;

  public FileEntry (PascalDisk parent, byte[] buffer)
  {
    super (parent, buffer);

    bytesUsedInLastBlock = HexFormatter.intValue (buffer[22], buffer[23]);
    date = HexFormatter.getPascalDate (buffer, 24);

    for (int i = firstBlock; i < lastBlock; i++)
      switch (fileType)
      {
        case 2:
          parent.sectorTypes[i] = parent.codeSector;
          break;
        case 3:
          parent.sectorTypes[i] = parent.textSector;
          break;
        case 4:
          parent.sectorTypes[i] = parent.infoSector;
          break;
        case 5:
          parent.sectorTypes[i] = parent.dataSector;
          break;
        case 6:
          parent.sectorTypes[i] = parent.grafSector;
          break;
        case 7:
          parent.sectorTypes[i] = parent.fotoSector;
          break;
        default:
          System.out.println ("Unknown pascal file type : " + fileType);
          parent.sectorTypes[i] = parent.dataSector;
          break;
      }
  }

  @Override
  public AbstractFile getDataSource ()
  {
    if (file != null)
      return file;

    byte[] buffer = getExactBuffer ();

    //      try
    {
      switch (fileType)
      {
        case 3:
          file = new PascalText (name, buffer);
          break;
        case 2:
          file = new PascalCode (name, buffer);
          break;
        case 4:
          file = new PascalInfo (name, buffer);
          break;
        case 0:
          // volume
          break;
        case 5:
          // data
          if (name.equals ("SYSTEM.CHARSET"))
          {
            file = new Charset (name, buffer);
            break;
          }
          if (name.equals ("WT")) // only testing
          {
            file = new WizardryTitle (name, buffer);
            break;
          }
          // intentional fall-through
        default:
          // unknown
          file = new DefaultAppleFile (name, buffer);
      }
    }
    //      catch (Exception e)
    //      {
    //        file = new ErrorMessageFile (name, buffer, e);
    //        e.printStackTrace ();
    //      }
    return file;
  }

  private byte[] getExactBuffer ()
  {
    byte[] buffer = parent.getDisk ().readSectors (blocks);
    byte[] exactBuffer;
    if (bytesUsedInLastBlock < 512)
    {
      int exactLength = buffer.length - 512 + bytesUsedInLastBlock;
      exactBuffer = new byte[exactLength];
      System.arraycopy (buffer, 0, exactBuffer, 0, exactLength);
    }
    else
      exactBuffer = buffer;
    return exactBuffer;
  }
}