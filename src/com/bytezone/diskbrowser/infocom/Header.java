package com.bytezone.diskbrowser.infocom;

import java.io.File;

import com.bytezone.diskbrowser.disk.Disk;

class Header extends InfocomAbstractFile
{
  final String[] propertyNames = new String[32];

  private final File file;
  private final Disk disk;
  int version;
  int highMemory;
  int programCounter;
  int dictionaryOffset;
  int objectTable;
  int globalsOffset;
  int staticMemory;
  int abbreviationsTable;
  int fileLength;
  int checksum;
  int stringPointer;

  Abbreviations abbreviations;
  Dictionary dictionary;
  ObjectManager objectManager;
  StringManager stringManager;
  CodeManager codeManager;
  Globals globals;
  Grammar grammar;

  public Header (String name, byte[] buffer, Disk disk)
  {
    super (name, buffer);
    this.disk = disk;
    this.file = disk.getFile ();

    version = getByte (0);
    highMemory = getWord (4);
    programCounter = getWord (6);
    dictionaryOffset = getWord (8);
    objectTable = getWord (10);
    globalsOffset = getWord (12);
    staticMemory = getWord (14);
    abbreviationsTable = getWord (24);
    checksum = getWord (28);
    fileLength = getWord (26) * 2;

    if (fileLength == 0)
      fileLength = buffer.length;

    // do the basic managers
    abbreviations = new Abbreviations (this);
    dictionary = new Dictionary (this);
    globals = new Globals (this);                     // may display ZStrings

    // set up an empty object to store Routines in
    codeManager = new CodeManager (this);

    grammar = new Grammar ("Grammar", buffer, this);

    // add all the ZObjects, and analyse them to find stringPtr, DICT etc.
    objectManager = new ObjectManager (this);

    // add all the ZStrings
    stringManager = new StringManager ("Strings", buffer, this);

    codeManager.addRoutine (programCounter - 1, 0);
    codeManager.addActionRoutines ();                 // obtained from Grammar
    codeManager.addCodeRoutines ();                   // obtained from Object properties
    codeManager.addMissingRoutines ();                // requires stringPtr to be set

    // add entries for AbstractFile.getHexDump ()
    hexBlocks.add (new HexBlock (0, 64, "Header data:"));
  }

  public String getAbbreviation (int index)
  {
    return abbreviations.getAbbreviation (index);
  }

  public boolean containsWordAt (int address)
  {
    return dictionary.containsWordAt (address);
  }

  public String wordAt (int address)
  {
    return dictionary.wordAt (address);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Disk name                %s%n", file.getName ()));
    text.append (String.format ("Version                  %d%n", version));
    text.append ("\nDynamic memory:\n");
    text.append (String.format ("  Abbreviation table     %04X  %,6d%n",
                                abbreviationsTable, abbreviationsTable));
    text.append (String.format ("  Objects table          %04X  %,6d%n", objectTable,
                                objectTable));
    text.append (String.format ("  Global variables       %04X  %,6d%n", globalsOffset,
                                globalsOffset));

    text.append ("\nStatic memory:\n");
    text.append (String.format ("  Grammar table etc      %04X  %,6d%n", staticMemory,
                                staticMemory));
    text.append (String.format ("  Dictionary             %04X  %,6d%n", dictionaryOffset,
                                dictionaryOffset));
    text.append ("\nHigh memory:\n");
    text.append (String.format ("  ZCode                  %04X  %,6d%n", highMemory,
                                highMemory));
    text.append (String.format ("  Program counter        %04X  %,6d%n", programCounter,
                                programCounter));
    text.append (String.format ("\nFile length             %05X  %,6d%n", fileLength,
                                fileLength));
    text.append (String.format ("Checksum                 %04X  %,6d%n", checksum,
                                checksum));
    text.append (String.format ("%nZString offset          %05X  %,6d%n", stringPointer,
                                stringPointer));

    text.append (String.format ("Total strings                     %d%n",
                                stringManager.strings.size ()));
    text.append (String.format ("Total objects                     %d%n",
                                objectManager.list.size ()));

    return text.toString ();
  }

  int getByte (int offset)
  {
    return buffer[offset] & 0xFF;
  }

  int getWord (int offset)
  {
    return ((buffer[offset] << 8) & 0xFF00) | ((buffer[offset + 1]) & 0xFF);
  }
}