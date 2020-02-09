package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
class Abbreviations extends InfocomAbstractFile
// -----------------------------------------------------------------------------------//
{
  List<ZString> list;
  Header header;
  int dataPtr;
  int dataSize;
  int tablePtr;
  int tableSize;

  // ---------------------------------------------------------------------------------//
  Abbreviations (Header header)
  // ---------------------------------------------------------------------------------//
  {
    super ("Abbreviations", header.buffer);
    this.header = header;

    dataPtr = header.getWord (header.abbreviationsTable) * 2;
    dataSize = header.abbreviationsTable - dataPtr;
    tablePtr = header.abbreviationsTable;
    tableSize = header.objectTableOffset - header.abbreviationsTable;

    // prepare hex dump
    hexBlocks.add (new HexBlock (dataPtr, dataSize, "Abbreviations data:"));
    hexBlocks.add (new HexBlock (tablePtr, tableSize, "Abbreviations table:"));
  }

  // ---------------------------------------------------------------------------------//
  private void populate ()
  // ---------------------------------------------------------------------------------//
  {
    list = new ArrayList<> ();

    for (int i = header.abbreviationsTable; i < header.objectTableOffset; i += 2)
      list.add (new ZString (header, header.getWord (i) * 2));
  }

  // ---------------------------------------------------------------------------------//
  String getAbbreviation (int abbreviationNumber)
  // ---------------------------------------------------------------------------------//
  {
    if (list == null)
      populate ();

    return list.get (abbreviationNumber).value;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    if (list == null)
      populate ();

    StringBuilder text = new StringBuilder ();

    //		text.append (String.format ("Data address....%04X  %d%n", dataPtr, dataPtr));
    //		text.append (String.format ("Data size.......%04X  %d%n", dataSize, dataSize));
    //		text.append (String.format ("Table address...%04X  %d%n", tablePtr, tablePtr));
    //		text.append (String.format ("Table size......%04X  %d  (%d words)%n%n", 
    //            tableSize, tableSize, (tableSize / 2)));

    int count = 0;
    for (ZString word : list)
      text.append (String.format ("%3d  %s%n", count++, word.value));
    if (list.size () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}