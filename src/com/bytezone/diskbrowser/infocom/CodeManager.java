package com.bytezone.diskbrowser.infocom;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.disk.DefaultAppleFileSource;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class CodeManager extends AbstractFile
{
	Header header;
	int codeSize;
	Map<Integer, Routine> routines = new TreeMap<Integer, Routine> ();

	public CodeManager (Header header)
	{
		super ("Code", header.buffer);
		this.header = header;
	}

	public void addNodes (DefaultMutableTreeNode root, FormattedDisk disk)
	{
		root.setAllowsChildren (true);

		codeSize = header.stringPointer - header.highMemory; // should be set by now - do this better!

		int count = 0;
		for (Routine routine : routines.values ())
		{
			DefaultMutableTreeNode node =
						new DefaultMutableTreeNode (new DefaultAppleFileSource (String.format ("%3d %s (%04X)",
									++count, routine.name, routine.startPtr / 2), routine, disk));
			node.setAllowsChildren (false);
			root.add (node);
		}
	}

	public void addMissingRoutines ()
	{
		System.out.printf ("%nWalking the code block%n%n");
		int total = 0;
		int ptr = header.highMemory;
		while (ptr < header.stringPointer)
		{
			if (ptr % 2 == 1) // routine must start on a word boundary
				ptr++;

			if (containsRoutineAt (ptr))
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
				total++;
				ptr += routine.length;
			}
		}
		System.out.printf ("%n%d new routines found by walking the code block%n%n", total);
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
						r.startPtr, r.length, r.instructions.size (), r.strings, r.calledBy.size (), r.calls
									.size (), gap, r.startPtr / 2));

			nextAddress = r.startPtr + r.length;
		}
		text.deleteCharAt (text.length () - 1);
		return text.toString ();
	}

	public boolean containsRoutineAt (int address)
	{
		return (routines.containsKey (address));
	}

	public void addCodeRoutines ()
	{
		List<Integer> routines = header.objectManager.getCodeRoutines ();
		System.out.println ("Adding " + routines.size () + " code routines");
		for (Integer address : routines)
			addRoutine (address, 0);
	}

	public void addActionRoutines ()
	{
		List<Integer> routines = header.grammar.getActionRoutines ();
		System.out.println ("Adding " + routines.size () + " action routines");
		for (Integer address : routines)
			addRoutine (address, 0);
	}

	public Routine addRoutine (int address, int caller)
	{
		if (address == 0) // stack-based call
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
		Routine r = new Routine (address, header, caller);
		if (r.length == 0) // invalid routine
			return null;

		// recursively add all routines called by this one
		routines.put (address, r);
		for (int ptr : r.calls)
			addRoutine (ptr, address);

		return r;
	}

	public Routine getRoutine (int address)
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