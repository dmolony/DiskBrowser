package com.bytezone.diskbrowser.infocom;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.AbstractFormattedDisk;
import com.bytezone.diskbrowser.disk.AppleDisk;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.SectorType;
import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// https://mud.co.uk/richard/htflpism.htm
// https://inform-fiction.org/zmachine/standards/
// https://github.com/historicalsource?tab=repositories

// -----------------------------------------------------------------------------------//
public class InfocomDisk extends AbstractFormattedDisk
// -----------------------------------------------------------------------------------//
{
  private static final int BLOCK_SIZE = 256;
  private static final boolean TYPE_NODE = true;
  private static final boolean TYPE_LEAF = false;
  private byte[] data;
  private final Header header;

  Color green = new Color (0, 200, 0);

  SectorType bootSector = new SectorType ("ZIP code", Color.lightGray);
  SectorType stringsSector = new SectorType ("Strings", Color.magenta);
  SectorType objectsSector = new SectorType ("Objects", green);
  SectorType dictionarySector = new SectorType ("Dictionary", Color.blue);
  SectorType abbreviationsSector = new SectorType ("Abbreviations", Color.red);
  SectorType codeSector = new SectorType ("Code", Color.orange);
  SectorType headerSector = new SectorType ("Header", Color.cyan);
  SectorType globalsSector = new SectorType ("Globals", Color.darkGray);
  SectorType grammarSector = new SectorType ("Grammar", Color.gray);

  // ---------------------------------------------------------------------------------//
  public InfocomDisk (Disk disk)
  // ---------------------------------------------------------------------------------//
  {
    super (disk);

    setInfocomSectorTypes ();

    data = disk.readSector (3, 0);          // read first sector to get file size
    data = getBuffer (getWord (26) * 2);    // read entire file into data buffer

    if (false)
      createStoryFile ("Zork1.sf");

    DefaultMutableTreeNode root = getCatalogTreeRoot ();
    DefaultMutableTreeNode headerNode = null;
    DefaultMutableTreeNode abbreviationsNode = null;
    DefaultMutableTreeNode codeNode = null;
    DefaultMutableTreeNode objectNode = null;
    DefaultMutableTreeNode globalsNode = null;
    DefaultMutableTreeNode grammarNode = null;
    DefaultMutableTreeNode dictionaryNode = null;
    DefaultMutableTreeNode stringsNode = null;

    header = new Header ("Header", data, disk);

    headerNode = addToTree (root, "Header", header, TYPE_LEAF);
    DefaultAppleFileSource dafs = (DefaultAppleFileSource) headerNode.getUserObject ();
    List<DiskAddress> blocks = new ArrayList<> ();
    blocks.add (disk.getDiskAddress (3, 0));
    dafs.setSectors (blocks);

    abbreviationsNode =
        addToTree (root, "Abbreviations", header.abbreviations, TYPE_LEAF);

    objectNode = addToTree (root, "Objects", header.objectManager, TYPE_NODE);
    header.objectManager.addNodes (objectNode, this);

    globalsNode = addToTree (root, "Globals", header.globals, TYPE_LEAF);
    grammarNode = addToTree (root, "Grammar", header.grammar, TYPE_LEAF);
    dictionaryNode = addToTree (root, "Dictionary", header.dictionary, TYPE_LEAF);

    codeNode = addToTree (root, "Code", header.codeManager, TYPE_NODE);
    header.codeManager.addNodes (codeNode, this);

    stringsNode = addToTree (root, "Strings", header.stringManager, TYPE_LEAF);

    PropertyManager pm = new PropertyManager ("Properties", data, header);
    pm.addNodes (addToTree (root, "Properties", pm, TYPE_NODE), this);

    AttributeManager am = new AttributeManager ("Attributes", data, header);
    am.addNodes (addToTree (root, "Attributes", am, TYPE_NODE), this);

    sectorTypes[48] = headerSector;

    setSectorTypes (header.abbreviationsTable, header.objectTableOffset,
        abbreviationsSector, abbreviationsNode);
    setSectorTypes (header.objectTableOffset, header.globalsOffset, objectsSector,
        objectNode);
    setSectorTypes (header.globalsOffset, header.staticMemory, globalsSector,
        globalsNode);
    setSectorTypes (header.staticMemory, header.dictionaryOffset, grammarSector,
        grammarNode);
    setSectorTypes (header.dictionaryOffset, header.highMemory, dictionarySector,
        dictionaryNode);
    setSectorTypes (header.highMemory, header.stringPointer, codeSector, codeNode);
    setSectorTypes (header.stringPointer, header.fileLength, stringsSector, stringsNode);
  }

  // ---------------------------------------------------------------------------------//
  protected void setInfocomSectorTypes ()
  // ---------------------------------------------------------------------------------//
  {
    sectorTypesList.add (bootSector);
    sectorTypesList.add (headerSector);
    sectorTypesList.add (abbreviationsSector);
    sectorTypesList.add (objectsSector);
    sectorTypesList.add (globalsSector);
    sectorTypesList.add (grammarSector);
    sectorTypesList.add (dictionarySector);
    sectorTypesList.add (codeSector);
    sectorTypesList.add (stringsSector);

    for (int track = 0; track < 3; track++)
      for (int sector = 0; sector < 16; sector++)
        if (!disk.isSectorEmpty (track, sector))
          sectorTypes[track * 16 + sector] = bootSector;
  }

  // ---------------------------------------------------------------------------------//
  private void setSectorTypes (int sectorFrom, int sectorTo, SectorType type,
      DefaultMutableTreeNode node)
  // ---------------------------------------------------------------------------------//
  {
    DefaultAppleFileSource dafs = (DefaultAppleFileSource) node.getUserObject ();
    List<DiskAddress> blocks = new ArrayList<> ();

    int blockNo = sectorFrom / disk.getBlockSize () + 48;
    int blockTo = sectorTo / disk.getBlockSize () + 48;
    while (blockNo <= blockTo)
    {
      blocks.add (disk.getDiskAddress (blockNo));
      if (!disk.isSectorEmpty (blockNo))
        sectorTypes[blockNo] = type;
      blockNo++;
    }
    dafs.setSectors (blocks);
  }

  // ---------------------------------------------------------------------------------//
  private int getFileSize ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = null;
    int startBlock = getWord (4) / 256 + 48;
    int fileSize = 0;
    for (DiskAddress da : disk)
    {
      if (da.getBlock () > startBlock && disk.isSectorEmpty (da))
      {
        System.out.println ("Empty : " + da);
        buffer = disk.readSector (da.getBlock () - 1);
        fileSize = (da.getBlock () - 48) * disk.getBlockSize ();
        break;
      }
    }

    if (buffer != null)
    {
      int ptr = 255;
      while (buffer[ptr--] == 0)
        fileSize--;
    }
    return fileSize;
  }

  // ---------------------------------------------------------------------------------//
  private byte[] getBuffer (int fileSize)
  // ---------------------------------------------------------------------------------//
  {
    if (fileSize == 0)
      fileSize = getFileSize ();
    data = new byte[fileSize];

    for (int track = 3, ptr = 0; track < 35; track++)
      for (int sector = 0; sector < 16; sector++, ptr += BLOCK_SIZE)
      {
        byte[] temp = disk.readSector (track, sector);
        int spaceLeft = fileSize - ptr;
        if (spaceLeft <= BLOCK_SIZE)
        {
          System.arraycopy (temp, 0, data, ptr, spaceLeft);
          return data;
        }
        System.arraycopy (temp, 0, data, ptr, BLOCK_SIZE);
      }
    return data;
  }

  // ---------------------------------------------------------------------------------//
  private DefaultMutableTreeNode addToTree (DefaultMutableTreeNode root, String title,
      DataSource af, boolean allowsChildren)
  // ---------------------------------------------------------------------------------//
  {
    DefaultAppleFileSource dafs = new DefaultAppleFileSource (title, af, this);

    //    dafs.setSectors (blocks);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode (dafs);
    node.setAllowsChildren (allowsChildren);
    root.add (node);
    return node;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public List<DiskAddress> getFileSectors (int fileNo)
  // ---------------------------------------------------------------------------------//
  {
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public AppleFileSource getCatalog ()
  // ---------------------------------------------------------------------------------//
  {
    return new DefaultAppleFileSource (header.getText (), this);
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isCorrectFormat (AppleDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    disk.setInterleave (2);
    return checkFormat (disk);
  }

  // ---------------------------------------------------------------------------------//
  public static boolean checkFormat (AppleDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = disk.readSector (3, 0);

    int version = buffer[0] & 0xFF;
    int highMemory = HexFormatter.intValue (buffer[5], buffer[4]);
    int programCounter = HexFormatter.intValue (buffer[7], buffer[6]);
    int dictionary = HexFormatter.intValue (buffer[9], buffer[8]);
    int objectTable = HexFormatter.intValue (buffer[11], buffer[10]);
    int globals = HexFormatter.intValue (buffer[13], buffer[12]);
    int staticMemory = HexFormatter.intValue (buffer[15], buffer[14]);
    int abbreviationsTable = HexFormatter.intValue (buffer[25], buffer[24]);
    int fileLength = HexFormatter.intValue (buffer[27], buffer[26]);

    if (false)
    {
      System.out.printf ("Version         %,6d%n", version);
      System.out.printf ("Abbreviations   %,6d%n", abbreviationsTable);
      System.out.printf ("Objects         %,6d%n", objectTable);
      System.out.printf ("Globals         %,6d%n", globals);
      System.out.printf ("Static memory   %,6d%n", staticMemory);
      System.out.printf ("Dictionary      %,6d%n", dictionary);
      System.out.printf ("High memory     %,6d%n", highMemory);
      System.out.printf ("Program counter %,6d%n", programCounter);
      System.out.printf ("File length     %,6d%n", fileLength);
    }

    if (abbreviationsTable >= objectTable)
      return false;
    //    if (objectTable >= globals)
    //      return false;
    if (globals >= staticMemory)
      return false;
    if (staticMemory >= dictionary)
      return false;
    if (dictionary >= highMemory)
      return false;
    //		if (highMemory > programCounter)
    //			return false;

    if (version < 2 || version > 3)
    {
      System.out.println ("Incorrect format : " + version);
      JOptionPane.showMessageDialog (null,
          "This appears to be an Infocom disk," + " but version " + version
              + " is not supported",
          "Unknown disk format", JOptionPane.INFORMATION_MESSAGE);
      return false;
    }

    return true;
  }

  // ---------------------------------------------------------------------------------//
  private int getWord (int offset)
  // ---------------------------------------------------------------------------------//
  {
    return (((data[offset] << 8) & 0xFF00) | ((data[offset + 1]) & 0xFF));
  }

  // ---------------------------------------------------------------------------------//
  private void createStoryFile (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    File f = new File (fileName);
    try
    {
      FileOutputStream fos = new FileOutputStream (f);
      fos.write (data);
      fos.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }
}