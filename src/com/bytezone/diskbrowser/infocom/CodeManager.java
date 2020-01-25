package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.infocom.Grammar.Sentence;
import com.bytezone.diskbrowser.infocom.Grammar.SentenceGroup;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class CodeManager extends AbstractFile
{
  private final Header header;
  private int codeSize;
  private final Map<Integer, Routine> routines = new TreeMap<> ();

  public CodeManager (Header header)
  {
    super ("Code", header.buffer);
    this.header = header;
  }

  void addNodes (DefaultMutableTreeNode root, InfocomDisk disk)
  {
    root.setAllowsChildren (true);

    // should be set by now - do this better!
    codeSize = header.stringPointer - header.highMemory;

    int count = 0;
    for (Routine routine : routines.values ())
    {
      String name = String.format ("%3d %s (%04X)", ++count, routine.getName (),
          routine.startPtr / 2);
      DefaultAppleFileSource dafs = new DefaultAppleFileSource (name, routine, disk);
      dafs.setSectors (getSectors (routine, disk.getDisk ()));

      DefaultMutableTreeNode node = new DefaultMutableTreeNode (dafs);
      node.setAllowsChildren (false);
      root.add (node);
    }
  }

  private List<DiskAddress> getSectors (Routine routine, Disk disk)
  {
    int blockNo = routine.startPtr / 256 + 48;
    int size = routine.length;
    List<DiskAddress> blocks = new ArrayList<> ();

    while (size > 0)
    {
      blocks.add (disk.getDiskAddress (blockNo++));
      size -= 256;
    }
    return blocks;
  }

  void addRoutines (int programCounter)
  {
    addRoutine (programCounter - 1, -1);
    addActionRoutines ();                 // obtained from Grammar
    addCodeRoutines ();                   // obtained from Object properties
    addGlobalRoutines ();
    addMissingRoutines ();                // requires stringPtr to be set
    //    checkThreeByteProperties ();

    if (false)
    {
      int routineNo = 0;
      int ptr = header.highMemory;
      for (Routine routine : routines.values ())
      {
        if (ptr < routine.startPtr)
        {
          int extraBytes = routine.startPtr - ptr;
          if (extraBytes > 1)
            System.out.println ("Orphan bytes\n------------");
          if (extraBytes == 1)
            System.out.println (String.format ("%05X : %s%n", ptr,
                HexFormatter.getHexString (buffer, ptr, extraBytes)));
          else
            System.out
                .println (HexFormatter.format (buffer, ptr, extraBytes, ptr) + "\n");
        }
        System.out.printf ("Routine #%3d%n", ++routineNo);
        System.out.println ("------------");
        System.out.println (routine.dump ());
        ptr = routine.startPtr + routine.length;
      }
    }

    if (false)
    {
      int ptr = header.highMemory;
      for (int key : routines.keySet ())
      {
        ptr = checkAlignment (ptr);
        Routine routine = routines.get (key);
        if (routine.startPtr > ptr)
          System.out.printf ("skipped %d bytes%n", routine.startPtr - ptr);
        System.out.println (routine);
        ptr = routine.startPtr + routine.length;
      }
    }
  }

  private int checkAlignment (int ptr)
  {
    if (ptr % 2 == 1)         // routine must start on a word boundary
      ++ptr;
    return ptr;
  }

  private void addGlobalRoutines ()
  {

  }

  private void addMissingRoutines ()
  {
    System.out.printf ("%nWalking the code block%n%n");
    int total = routines.size ();
    int ptr = header.highMemory;

    while (ptr < header.stringPointer)
    {
      ptr = checkAlignment (ptr);

      if (routines.containsKey (ptr))
      {
        ptr += getRoutine (ptr).length;
        continue;
      }

      Routine routine = addRoutine (ptr, 0);
      if (routine == null)
      {
        System.out.printf ("Invalid routine found : %05X%n", ptr);
        ptr = findNextRoutine (ptr + 1);
        System.out.printf ("skipping to %05X%n", ptr);
        if (ptr == 0)
          break;
      }
      else
      {
        ptr += routine.length;
        System.out.printf ("Routine found: %05X%n", routine.startPtr);
      }
    }
    System.out.printf ("%n%d new routines found by walking the code block%n%n",
        routines.size () - total);
  }

  private int findNextRoutine (int address)
  {
    for (Routine routine : routines.values ())
      if (routine.startPtr > address)
        return routine.startPtr;
    return 0;
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();
    int count = 0;
    int nextAddress = header.highMemory;
    text.append ("  #   Address   Size   Lines  Strings   Called   Calls   Gap   Pack\n");
    text.append ("---   -------   ----   -----  -------   ------   -----   ---   ----\n");
    for (Routine r : routines.values ())
    {
      int gap = r.startPtr - nextAddress;
      text.append (String.format (
          "%3d    %05X   %5d     %3d      %2d      %3d     %3d   %4d   %04X%n", ++count,
          r.startPtr, r.length, r.instructions.size (), r.strings, r.calledBy.size (),
          r.calls.size (), gap, r.startPtr / 2));

      nextAddress = r.startPtr + r.length;
    }
    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  private void addCodeRoutines ()
  {
    List<Integer> routines = header.objectManager.getCodeRoutines ();
    System.out.println ("Adding " + routines.size () + " code routines");
    for (Integer address : routines)
      addRoutine (address, 0);
  }

  private void addActionRoutines ()
  {
    int total = routines.size ();

    for (SentenceGroup sentenceGroup : header.grammar.getSentenceGroups ())
      for (Sentence sentence : sentenceGroup)
      {
        if (sentence.preActionRoutine > 0)
          addRoutine (sentence.preActionRoutine, sentence.startPtr);
        addRoutine (sentence.actionRoutine, sentence.startPtr);
      }

    System.out.printf ("Added %d action routines%n", routines.size () - total);
  }

  Routine addRoutine (int address, int caller)
  {
    if (address == 0)                                       // stack-based call
      return null;

    if (address > header.fileLength)
      return null;

    // check whether we already have this routine
    if (routines.containsKey (address))
    {
      Routine routine = routines.get (address);
      routine.addCaller (caller);
      return routine;
    }

    // try to create a new Routine
    Routine routine = new Routine (address, header, caller);
    if (!routine.isValid ())
      return null;

    // recursively add all routines called by this one
    routines.put (address, routine);
    for (int ptr : routine.calls)
      addRoutine (ptr, address);

    return routine;
  }

  Routine getRoutine (int address)
  {
    return routines.get (address);
  }

  @Override
  public String getHexDump ()
  {
    // this depends on codeSize being set after the strings have been processed
    return HexFormatter.format (buffer, header.highMemory, codeSize);
  }
}