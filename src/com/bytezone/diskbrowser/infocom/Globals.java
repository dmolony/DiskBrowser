package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.infocom.Instruction.Operand;

// -----------------------------------------------------------------------------------//
class Globals extends InfocomAbstractFile
// -----------------------------------------------------------------------------------//
{
  private static final int TOTAL_GLOBALS = 240;
  private final Header header;
  private final int globalsPtr, globalsSize;
  private final int arrayPtr, arraySize;
  private final List<List<Routine>> globalRoutines;

  // ---------------------------------------------------------------------------------//
  Globals (Header header)
  // ---------------------------------------------------------------------------------//
  {
    super ("Globals", header.buffer);
    this.header = header;

    globalsPtr = header.globalsOffset;
    globalsSize = TOTAL_GLOBALS * 2;
    arrayPtr = globalsPtr + globalsSize;
    arraySize = header.staticMemory - arrayPtr;

    // add entries for AbstractFile.getHexDump ()
    hexBlocks.add (new HexBlock (globalsPtr, globalsSize, "Globals:"));
    hexBlocks.add (new HexBlock (arrayPtr, arraySize, "Arrays:"));

    globalRoutines = new ArrayList<> (TOTAL_GLOBALS);
    for (int i = 0; i < TOTAL_GLOBALS; i++)
      globalRoutines.add (new ArrayList<> ());
  }

  // ---------------------------------------------------------------------------------//
  void addRoutine (Routine routine, Operand operand)
  // ---------------------------------------------------------------------------------//
  {
    List<Routine> list = globalRoutines.get (operand.value - 16);
    if (!list.contains (routine))
      list.add (routine);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("GLB   Value   Routines\n");
    for (int i = 0; i < TOTAL_GLOBALS; i++)
    {
      int value = header.getWord (globalsPtr + i * 2);
      text.append (String.format ("G%03d    %04X      %03d     ", i, value,
          globalRoutines.get (i).size ()));
      int address = value * 2;
      if (address >= header.stringPointer && address < header.fileLength)
        text.append (header.stringManager.stringAt (address) + "\n");
      else
      {
        for (Routine routine : globalRoutines.get (i))
          text.append (String.format ("%05X  ", routine.startPtr));
        text.append ("\n");
      }
    }
    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }
}