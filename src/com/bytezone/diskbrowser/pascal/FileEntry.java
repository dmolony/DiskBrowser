package com.bytezone.diskbrowser.pascal;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.*;
import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class FileEntry extends CatalogEntry
{
  private DefaultMutableTreeNode node;

  public FileEntry (PascalDisk parent, byte[] buffer)
  {
    super (parent, buffer);

    bytesUsedInLastBlock = HexFormatter.intValue (buffer[22], buffer[23]);
    date = HexFormatter.getPascalDate (buffer, 24);

    int max = Math.min (lastBlock, parent.getDisk ().getTotalBlocks ());
    for (int i = firstBlock; i < max; i++)
    {
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

  void setNode (DefaultMutableTreeNode node)
  {
    this.node = node;
  }

  public void setFile (AbstractFile file)
  {
    this.file = file;
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
          file = new PascalCode (name, buffer, firstBlock);
          node.removeAllChildren ();

          for (PascalSegment pascalSegment : (PascalCode) file)
          {
            DefaultMutableTreeNode segmentNode = new DefaultMutableTreeNode (
                new PascalCodeObject (parent, pascalSegment, firstBlock));
            segmentNode.setAllowsChildren (false);
            node.add (segmentNode);
          }
        }
        catch (FileFormatException e)
        {
          if (name.equals ("SYSTEM.INTERP"))
            file = new AssemblerProgram (name, buffer, 0xD000);
          else
            file = new AssemblerProgram (name, buffer, 0);
          node.setAllowsChildren (false);
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
        //        else if (name.equals ("SYSTEM.RELOC"))
        //          file = new Relocator (name, buffer);
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
    //    System.out.println (HexFormatter.format (buffer));

    if (buffer.length > 0 && bytesUsedInLastBlock < 512)
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