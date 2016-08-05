package com.bytezone.diskbrowser.pascal;

import com.bytezone.diskbrowser.applefile.*;
import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class FileEntry extends CatalogEntry
{
  int bytesUsedInLastBlock;
  private final Relocator relocator;

  public FileEntry (PascalDisk parent, byte[] buffer, Relocator relocator)
  {
    super (parent, buffer);

    this.relocator = relocator;
    bytesUsedInLastBlock = HexFormatter.intValue (buffer[22], buffer[23]);
    date = HexFormatter.getPascalDate (buffer, 24);

    for (int i = firstBlock; i < lastBlock; i++)
    {
      if (i >= 280)
        break;

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
  }

  @Override
  public AbstractFile getDataSource ()
  {
    if (file != null)
      return file;

    byte[] buffer = getExactBuffer ();

    switch (fileType)
    {
      case 2:                                         // code (6502 or Pascal)
        try
        {
          file = new PascalCode (name, buffer, firstBlock, relocator);
        }
        catch (FileFormatException e)
        {
          if (name.equals ("SYSTEM.INTERP"))
            file = new AssemblerProgram (name, buffer, 0xD000);
          else
            file = new AssemblerProgram (name, buffer, 0);
        }
        break;

      case 3:
        file = new PascalText (name, buffer);
        break;

      case 4:
        file = new PascalInfo (name, buffer);
        break;

      case 5:                                           // data
        if (name.equals ("SYSTEM.CHARSET"))
          file = new Charset (name, buffer);
        //        else if (name.equals ("WT"))               // only testing
        //          file = new WizardryTitle (name, buffer);
        else if (name.equals ("SYSTEM.RELOC"))
          file = new Relocator (name, buffer);
        else
          file = new DefaultAppleFile (name, buffer);
        break;

      case 0:                                           // volume
        break;

      default:                                          // unknown
        file = new DefaultAppleFile (name, buffer);
    }

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