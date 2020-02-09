package com.bytezone.diskbrowser.pascal;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.AssemblerProgram;
import com.bytezone.diskbrowser.applefile.Charset;
import com.bytezone.diskbrowser.applefile.DefaultAppleFile;
import com.bytezone.diskbrowser.applefile.PascalCode;
import com.bytezone.diskbrowser.applefile.PascalInfo;
import com.bytezone.diskbrowser.applefile.PascalSegment;
import com.bytezone.diskbrowser.applefile.PascalText;
import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class FileEntry extends CatalogEntry
// -----------------------------------------------------------------------------------//
{
  private DefaultMutableTreeNode node;

  // ---------------------------------------------------------------------------------//
  public FileEntry (PascalDisk parent, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (parent, buffer);

    bytesUsedInLastBlock = HexFormatter.intValue (buffer[22], buffer[23]);
    date = HexFormatter.getPascalDate (buffer, 24);

    int max = Math.min (lastBlock, parent.getDisk ().getTotalBlocks ());
    for (int i = firstBlock; i < max; i++)
    {
      if (fileType < parent.sectors.length)
        parent.sectorTypes[i] = parent.sectors[fileType];
      else
      {
        System.out.println ("Unknown pascal file type : " + fileType);
        parent.sectorTypes[i] = parent.dataSector;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  void setNode (DefaultMutableTreeNode node)
  // ---------------------------------------------------------------------------------//
  {
    this.node = node;
  }

  // ---------------------------------------------------------------------------------//
  public void setFile (AbstractFile file)
  // ---------------------------------------------------------------------------------//
  {
    this.file = file;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AbstractFile getDataSource ()
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  private byte[] getExactBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = parent.getDisk ().readSectors (blocks);
    byte[] exactBuffer;

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