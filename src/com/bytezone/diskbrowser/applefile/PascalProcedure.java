package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.applefile.PascalCodeStatement.Jump;

public class PascalProcedure
{
	// all procedures have these fields
	byte[] buffer;
	int procOffset;
	int offset;
	int slot;
	boolean valid;

	// only valid procedures have these fields
	int procedureNo;
	int procLevel;
	int codeStart;
	int codeEnd;
	int parmSize;
	int dataSize;
	List<PascalCodeStatement> statements = new ArrayList<PascalCodeStatement> ();
	AssemblerProgram assembler;
	int jumpTable = -8;

	public PascalProcedure (byte[] buffer, int slot)
	{
		this.buffer = buffer;
		this.slot = slot;
		int p = buffer.length - 2 - slot * 2;
		offset = HexFormatter.intValue (buffer[p], buffer[p + 1]);
		procOffset = p - offset;
		valid = procOffset > 0;

		if (valid)
		{
			procedureNo = buffer[procOffset] & 0xFF;
			procLevel = buffer[procOffset + 1];
			codeStart = HexFormatter.intValue (buffer[procOffset - 2], buffer[procOffset - 1]);
			codeEnd = HexFormatter.intValue (buffer[procOffset - 4], buffer[procOffset - 3]);
			parmSize = HexFormatter.intValue (buffer[procOffset - 6], buffer[procOffset - 5]);
			dataSize = HexFormatter.intValue (buffer[procOffset - 8], buffer[procOffset - 7]);
		}
	}

	private void decode ()
	{
		if (statements.size () > 0 || assembler != null)
			return;
		int ptr = procOffset - codeStart - 2;
		int max = procOffset + jumpTable;

		if (codeEnd == 0)
		{
			int len = codeStart + jumpTable + 2;
			if (len > 0)
			{
				byte[] asmBuf = new byte[len];
				System.arraycopy (buffer, ptr, asmBuf, 0, len);
				assembler = new AssemblerProgram ("Proc", asmBuf, ptr);
			}
			return;
		}

		while (ptr < max)
		{
			PascalCodeStatement cs = new PascalCodeStatement (buffer, ptr, procOffset);
			if (cs.length <= 0)
			{
				System.out.println ("error - length <= 0 : " + cs);
				break;
			}
			statements.add (cs);
			if (cs.val == 185 || cs.val == 161)
				if (cs.p1 < jumpTable)
				{
					jumpTable = cs.p1;
					max = procOffset + jumpTable;
				}
			ptr += cs.length;
		}

		// Tidy up left-over bytes at the end
		if (statements.size () > 1)
		{
			PascalCodeStatement lastStatement = statements.get (statements.size () - 1);
			PascalCodeStatement secondLastStatement = statements.get (statements.size () - 2);
			if (lastStatement.val == 0
									&& (secondLastStatement.val == 0xD6 || secondLastStatement.val == 0xC1 || secondLastStatement.val == 0xAD))
				statements.remove (statements.size () - 1);
		}

		// Mark statements that are jump targets
		int actualEnd = procOffset - codeEnd - 4;
		for (PascalCodeStatement cs : statements)
		{
			if (cs.ptr == actualEnd)
			{
				cs.jumpTarget = true;
				continue;
			}
			for (Jump cj : cs.jumps)
				for (PascalCodeStatement cs2 : statements)
					if (cs2.ptr == cj.addressTo)
					{
						cs2.jumpTarget = true;
						break;
					}
		}
	}

	public List<PascalCodeStatement> extractStrings ()
	{
		decode ();
		List<PascalCodeStatement> strings = new ArrayList<PascalCodeStatement> ();
		for (PascalCodeStatement cs : statements)
			if (cs.val == 166)
				strings.add (cs);
		return strings;
	}

	public String toString ()
	{
		if (!valid)
			return "";
		decode ();

		StringBuilder text = new StringBuilder ("\nProcedure Header\n================\n\n");

		if (false)
			text.append (HexFormatter.format (buffer, procOffset + jumpTable, 2 - jumpTable) + "\n\n");

		text.append (String.format ("Level.......%5d     %02X%n", procLevel, procLevel & 0xFF));
		text.append (String.format ("Proc no.....%5d     %02X%n", procedureNo, procedureNo));
		text.append (String.format ("Code entry..%5d   %04X  (%04X - %04X = %04X)%n", codeStart,
								codeStart, (procOffset - 2), codeStart, (procOffset - codeStart - 2)));
		text.append (String.format ("Code exit...%5d   %04X", codeEnd, codeEnd));
		if (codeEnd > 0)
			text.append (String.format ("  (%04X - %04X = %04X)%n", (procOffset - 4), codeEnd,
									(procOffset - codeEnd - 4)));
		else
			text.append (String.format ("%n"));
		text.append (String.format ("Parm size...%5d   %04X%n", parmSize, parmSize));
		text.append (String.format ("Data size...%5d   %04X%n%n", dataSize, dataSize));

		text.append ("Procedure Code\n==============\n\n");

		int ptr = procOffset - codeStart - 2;
		if (false)
			text.append (HexFormatter.format (buffer, ptr, codeStart + jumpTable + 2) + "\n\n");

		if (codeEnd == 0)
			text.append (assembler.getAssembler () + "\n");
		else
		{
			for (PascalCodeStatement cs : statements)
				text.append (cs);

			if (jumpTable < -8 && false)
			{
				text.append ("\nJump table:\n");
				for (int i = procOffset + jumpTable; i < procOffset - 8; i += 2)
				{
					ptr = i - ((buffer[i + 1] & 0xFF) * 256 + (buffer[i] & 0xFF));
					text.append (String.format ("%05X : %02X %02X  --> %04X%n", i, buffer[i], buffer[i + 1],
											ptr));
				}
			}
		}
		return text.toString ();
	}
}