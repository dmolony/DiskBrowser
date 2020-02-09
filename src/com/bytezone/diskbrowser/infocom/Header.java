package com.bytezone.diskbrowser.infocom;

import java.io.File;

import com.bytezone.diskbrowser.disk.Disk;

// -----------------------------------------------------------------------------------//
class Header extends InfocomAbstractFile
// -----------------------------------------------------------------------------------//
{
  final String[] propertyNames = new String[32];

  private final File file;
  int version;
  int highMemory;
  int programCounter;
  int dictionaryOffset;
  int objectTableOffset;
  int globalsOffset;
  int staticMemory;
  int abbreviationsTable;
  int fileLength;
  int checksum;
  int stringPointer;

  final Abbreviations abbreviations;
  final ObjectManager objectManager;
  final Globals globals;
  final Grammar grammar;
  final Dictionary dictionary;
  final CodeManager codeManager;
  final StringManager stringManager;

  // ---------------------------------------------------------------------------------//
  Header (String name, byte[] buffer, Disk disk)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
    this.file = disk.getFile ();

    version = getByte (00);
    highMemory = getWord (0x04);
    programCounter = getWord (0x06);

    dictionaryOffset = getWord (0x08);
    objectTableOffset = getWord (0x0A);
    globalsOffset = getWord (0x0C);
    staticMemory = getWord (0x0E);
    abbreviationsTable = getWord (0x18);

    fileLength = getWord (0x1A) * 2;            // 2 for versions 1-3
    checksum = getWord (0x1C);
    int interpreterNumber = getByte (0x1E);
    int interpreterVersion = getByte (0x1F);
    int revision = getWord (0x30);

    System.out.printf ("Version    : %d%n", version);
    System.out.printf ("Interpreter: %d.%d%n", interpreterNumber, interpreterVersion);
    System.out.printf ("Revision   : %d%n", revision);

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

    codeManager.addRoutines (programCounter);

    // add entries for AbstractFile.getHexDump ()
    hexBlocks.add (new HexBlock (0, 64, "Header data:"));
  }

  // ---------------------------------------------------------------------------------//
  String getPropertyName (int id)
  // ---------------------------------------------------------------------------------//
  {
    return propertyNames[id];
  }

  // ---------------------------------------------------------------------------------//
  public String getAbbreviation (int index)
  // ---------------------------------------------------------------------------------//
  {
    return abbreviations.getAbbreviation (index);
  }

  // ---------------------------------------------------------------------------------//
  public boolean containsWordAt (int address)
  // ---------------------------------------------------------------------------------//
  {
    return dictionary.containsWordAt (address);
  }

  // ---------------------------------------------------------------------------------//
  public String wordAt (int address)
  // ---------------------------------------------------------------------------------//
  {
    return dictionary.wordAt (address);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Disk name                %s%n", file.getName ()));
    text.append (String.format ("Version                  %d%n", version));
    text.append ("\nDynamic memory:\n");
    text.append (String.format ("  Abbreviation table     %04X  %,6d%n",
        abbreviationsTable, abbreviationsTable));
    text.append (String.format ("  Objects table          %04X  %,6d%n",
        objectTableOffset, objectTableOffset));
    text.append (String.format ("  Global variables       %04X  %,6d%n", globalsOffset,
        globalsOffset));

    text.append ("\nStatic memory:\n");
    text.append (String.format ("  Grammar table etc      %04X  %,6d%n", staticMemory,
        staticMemory));
    text.append (String.format ("  Dictionary             %04X  %,6d%n", dictionaryOffset,
        dictionaryOffset));
    text.append ("\nHigh memory:\n");
    text.append (
        String.format ("  ZCode                  %04X  %,6d%n", highMemory, highMemory));
    text.append (String.format ("  Program counter        %04X  %,6d%n", programCounter,
        programCounter));
    text.append (
        String.format ("\nFile length             %05X %,7d%n", fileLength, fileLength));
    text.append (
        String.format ("Checksum                 %04X  %,6d%n", checksum, checksum));
    text.append (String.format ("%nZString offset          %05X %,7d%n", stringPointer,
        stringPointer));

    text.append (String.format ("Total strings                     %d%n",
        stringManager.strings.size ()));
    text.append (String.format ("Total objects                     %d%n",
        objectManager.getObjects ().size ()));

    text.append (getAlternate ());

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String getAlternate ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("\n\n");

    text.append (getLine (0, 1, "version"));
    text.append (getLine (1, 3, "flags 1"));
    text.append (getLine (4, 2, "high memory"));
    text.append (getLine (6, 2, "program counter"));
    text.append (getLine (8, 2, "dictionary"));
    text.append (getLine (10, 2, "object table"));
    text.append (getLine (12, 2, "global variables"));
    text.append (getLine (14, 2, "static memory"));
    text.append (getLine (16, 2, "flags 2"));
    text.append (getLine (24, 2, "abbreviations table"));
    text.append (getLine (26, 2, "length of file (x2 = " + fileLength + ")"));
    text.append (getLine (28, 2, "checksum"));
    text.append (getLine (50, 1, "revision number"));
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String getLine (int offset, int size, String description)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("%04X - %04X  ", offset, offset + size - 1));
    for (int i = 0; i < size; i++)
      text.append (String.format ("%02X ", buffer[offset + i]));
    while (text.length () < 24)
      text.append (" ");
    text.append (description);
    text.append ("\n");
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  ZObject getObject (int index)
  // ---------------------------------------------------------------------------------//
  {
    return objectManager.getObject (index);
  }

  // ---------------------------------------------------------------------------------//
  int getByte (int offset)
  // ---------------------------------------------------------------------------------//
  {
    return buffer[offset] & 0xFF;
  }

  // ---------------------------------------------------------------------------------//
  int getWord (int offset)
  // ---------------------------------------------------------------------------------//
  {
    return ((buffer[offset] << 8) & 0xFF00) | ((buffer[offset + 1]) & 0xFF);
  }
}