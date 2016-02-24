package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class PascalCodeStatement implements PascalConstants
{
	private static final String[] compValue =
							{ "invalid", "", "REAL", "", "STR", "", "BOOL", "", "POWR", "", "BYT", "", "WORD" };

	int length;
	int val;
	int p1, p2, p3;
	String mnemonic;
	String extras = "";
	String description;
	String text;
	int ptr; // temp
	byte[] buffer;
	boolean jumpTarget;
	List<Jump> jumps = new ArrayList<Jump> ();

	public PascalCodeStatement (byte[] buffer, int ptr, int procPtr)
	{
		this.ptr = ptr;
		this.buffer = buffer;
		length = 1;
		val = buffer[ptr] & 0xFF;
		if (val <= 127)
		{
			mnemonic = "SLDC";
			extras = "#" + val;
			description = "Short load constant - push #" + val;
		}
		else if (val >= 248)
		{
			mnemonic = "SIND";
			extras = "#" + (val - 248);
			description = "Short index load - push word *ToS + #" + (val - 248);
		}
		else if (val >= 232)
		{
			mnemonic = "SLDO";
			extras = "#" + (val - 231);
			description = "Short load global - push BASE + #" + (val - 231);
		}
		else if (val >= 216)
		{
			mnemonic = "SLDL";
			extras = "#" + (val - 215);
			description = "Short load local - push MP + #" + (val - 215);
		}
		else
		{
			mnemonic = mnemonics[val - 128];
			description = descriptions[val - 128];

			length = mnemonicSize[val - 128];
			if (length != 1)
			{
				switch (val)
				{
					// W1, W2, W3, <table> - word aligned case jump
					case 172: //XJP
						int padding = (ptr % 2) == 0 ? 1 : 0;
						p1 = getWord (buffer, ptr + padding + 1);
						p2 = getWord (buffer, ptr + padding + 3);
						p3 = getWord (buffer, ptr + padding + 5);
						length = (p2 - p1 + 1) * 2 + 7 + padding;
						setParameters (p1, p2, String.format ("%04X", p3));
						int v = p1;
						int min = ptr + padding + 7;
						int max = min + (p2 - p1) * 2;
						for (int i = min; i <= max; i += 2)
						{
							jumps.add (new Jump (i, i - HexFormatter.intValue (buffer[i], buffer[i + 1]), v++));
						}
						break;

					// UB, <block> - word aligned
					case 179: //LDC
						p1 = buffer[ptr + 1] & 0xFF;
						padding = ptr % 2 == 0 ? 0 : 1;
						length = p1 * 2 + padding + 2;
						setParameters (p1);
						break;

					// UB, <chars>
					case 166: // LSA
					case 208: // LPA
						p1 = buffer[ptr + 1] & 0xFF;
						length = p1 + 2;
						if (val == 166)
						{
							text = HexFormatter.getPascalString (buffer, ptr + 1);
							description += ": " + text;
						}
						break;

					// W
					case 199: // LDCI
						p1 = getWord (buffer, ptr + 1);
						setParameters (p1);
						break;

					// B
					case 162: // INC
					case 163: // IND
					case 164: // IXA
					case 165: // LAO
					case 168: // MOV
					case 169: // LDO
					case 171: // SRO
					case 198: // LLA
					case 202: // LDL
					case 204: // STL
					case 213: // BPT
						length = getLengthOfB (buffer[ptr + 1]) + 1;
						p1 = getValueOfB (buffer, ptr + 1, length - 1);
						setParameters (p1);
						break;

					// DB, B or UB, B
					case 157: // LDE
					case 167: // LAE
					case 178: // LDA
					case 182: // LOD
					case 184: // STR
					case 209: // STE
						length = getLengthOfB (buffer[ptr + 2]) + 2;
						p1 = buffer[ptr + 1] & 0xFF;
						p2 = getValueOfB (buffer, ptr + 2, length - 2);
						setParameters (p1, p2);
						break;

					// UB1, UB2
					case 192: // IXP
					case 205: // CXP
						p1 = buffer[ptr + 1] & 0xFF;
						p2 = buffer[ptr + 2] & 0xFF;
						setParameters (p1, p2);
						break;

					// SB or DB
					case 161: // FJP
					case 173: // RNP
					case 185: // UJP
					case 193: // RBP
					case 211: // EFJ
					case 212: // NFJ
						p1 = buffer[ptr + 1];
						if (val == 173 || val == 193) // return from procedure
							setParameters (p1);
						else if (p1 < 0)
						{
							// look up jump table entry
							int address = procPtr + p1;
							int ptr2 = address - ((buffer[address + 1] & 0xFF) * 256 + (buffer[address] & 0xFF));
							extras = String.format ("$%04X", ptr2);
							jumps.add (new Jump (ptr, ptr2));
						}
						else
						{
							int address = ptr + length + p1;
							extras = String.format ("$%04X", address);
							jumps.add (new Jump (ptr, address));
						}
						break;

					// UB
					case 160: // AOJ
					case 170: // SAS
					case 174: // CIP
					case 188: // LDM
					case 189: // STM
					case 194: // CBP
					case 206: // CLP
					case 207: // CGP
						p1 = buffer[ptr + 1] & 0xFF;
						setParameters (p1);
						break;

					// CSP
					case 158:
						p1 = buffer[ptr + 1];
						description = "Call standard procedure - " + CSP[p1];
						break;

					// Non-integer comparisons
					case 175:
					case 176:
					case 177:
					case 180:
					case 181:
					case 183:
						p1 = buffer[ptr + 1]; // 2/4/6/8/10/12
						if (p1 < 0 || p1 >= compValue.length)
						{
							System.out.printf ("%d  %d  %d%n", val, p1, ptr);
							mnemonic += "******************************";
							break;
						}
						mnemonic += compValue[p1];
						if (p1 == 10 || p1 == 12)
						{
							length = getLengthOfB (buffer[ptr + 2]) + 2;
							p2 = getValueOfB (buffer, ptr + 2, length - 2);
							setParameters (p2);
						}
						break;

					default:
						System.out.println ("Forgot : " + val);
				}
			}
		}
	}

	private int getWord (byte[] buffer, int ptr)
	{
		return (buffer[ptr + 1] & 0xFF) * 256 + (buffer[ptr] & 0xFF);
	}

	private int getLengthOfB (byte b)
	{
		return (b & 0x80) == 0x80 ? 2 : 1;
	}

	private int getValueOfB (byte[] buffer, int ptr, int length)
	{
		if (length == 2)
			return (buffer[ptr] & 0x7F) * 256 + (buffer[ptr + 1] & 0xFF);
		return buffer[ptr] & 0xFF;
	}

	private void setParameters (int p1)
	{
		description = description.replaceFirst (":1", p1 + "");
		extras = "#" + p1;
	}

	private void setParameters (int p1, int p2)
	{
		setParameters (p1);
		extras += ", #" + p2;
		description = description.replaceFirst (":2", p2 + "");
	}

	private void setParameters (int p1, int p2, String p3)
	{
		setParameters (p1, p2);
		description = description.replaceFirst (":3", p3);
	}

	public String toString ()
	{
		String hex = getHex (buffer, ptr, length > 4 ? 4 : length);
		StringBuilder text = new StringBuilder ();
		text.append (String.format ("%2s%05X: %-11s  %-6s %-8s   %s%n", jumpTarget ? "->" : "", ptr,
								hex, mnemonic, extras, description));
		if (length > 4)
		{
			int bytesLeft = length - 4;
			int jmp = 0;
			int p = ptr + 4;
			while (bytesLeft > 0)
			{
				String line = getHex (buffer, p, (bytesLeft > 4) ? 4 : bytesLeft);
				text.append ("         " + line);
				if (jumps.size () > 0)
				{
					if (jmp < jumps.size ())
						text.append ("                     " + jumps.get (jmp++));
					if (jmp < jumps.size ())
						text.append ("   " + jumps.get (jmp++));
				}
				text.append ("\n");
				bytesLeft -= 4;
				p += 4;
			}
		}
		return text.toString ();
	}

	private String getHex (byte[] buffer, int offset, int length)
	{
		if ((offset + length) >= buffer.length)
		{
			System.out.println ("too many");
			return "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
		}
		StringBuilder text = new StringBuilder ();
		for (int i = 0; i < length; i++)
			text.append (String.format ("%02X ", buffer[offset + i]));
		if (text.length () > 0)
			text.deleteCharAt (text.length () - 1);
		return text.toString ();
	}

	class Jump
	{
		int addressFrom;
		int addressTo;
		boolean caseJump;
		int caseValue;

		public Jump (int addressFrom, int addressTo)
		{
			this.addressFrom = addressFrom;
			this.addressTo = addressTo;
		}

		public Jump (int addressFrom, int addressTo, int value)
		{
			this (addressFrom, addressTo);
			this.caseValue = value;
			this.caseJump = true;
		}

		public String toString ()
		{
			if (caseJump)
				return String.format ("%3d: %04X", caseValue, addressTo);
			return String.format ("%04X", addressTo);
		}
	}
}