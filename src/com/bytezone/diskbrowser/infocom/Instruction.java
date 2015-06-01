package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.HexFormatter;

class Instruction
{
	Opcode opcode;
	int startPtr;
	byte[] buffer;
	//	List<ZString> abbreviations;
	Header header;

	static final String[] name2OP =
				{ "*bad*", "je", "jl", "jg", "dec_chk", "inc_chk", "jin", "test", "or", "and", "test_attr",
					"set_attr", "clear_attr", "store", "insert_obj", "loadw", "loadb", "get_prop",
					"get_prop_addr", "get_next_prop", "add", "sub", "mul", "div", "mod", "call_2s",
					"call_2n", "set_colour", "throw", "*bad*", "*bad*", "*bad*" };
	static final String[] name1OP =
				{ "jz", "get_sibling", "get_child", "get_parent", "get_prop_len", "inc", "dec",
					"print_addr", "call_ls", "remove_obj", "print_obj", "ret", "jump", "print_paddr", "load",
					"not" };
	static final String[] name0OP =
				{ "rtrue", "rfalse", "print", "print_ret", "nop", "save", "restore", "restart",
					"ret_popped", "pop", "quit", "new_line", "show_status", "verify", "", "piracy" };
	static final String[] nameVAR =
				{ "call", "storew", "storeb", "put_prop", "sread", "print_char", "print_num", "random",
					"push", "pull", "split_window", "set_window", "call_vs2", "erase_window", "erase_line",
					"set_cursor", "get_cursor", "set_text_style", "buffer_mode", "output_stream",
					"input_stream", "sound_effect", "read_char", "scan_table", "not", "call_vn", "call_vn2",
					"tokenise", "encode_text", "copy_table", "print_table", "check_arg" };

	public Instruction (byte[] buffer, int ptr, Header header)
	{
		this.buffer = buffer;
		this.startPtr = ptr;
		this.header = header;
		byte b1 = buffer[ptr];

		// long
		if ((b1 & 0x80) == 0)
			opcode = new Opcode2OPLong (buffer, ptr);
		// short
		else if ((b1 & 0x40) == 0)
		{
			if ((b1 & 0x30) == 0x30)
				opcode = new Opcode0OP (buffer, ptr);
			else
				opcode = new Opcode1OP (buffer, ptr);
		}
		// variable
		else
		{
			if ((b1 & 0x20) == 0)
				opcode = new Opcode2OPVar (buffer, ptr);
			else
				opcode = new OpcodeVar (buffer, ptr);
		}
	}

	public int length ()
	{
		return opcode.length ();
	}

	public boolean isReturn ()
	{
		return opcode.isReturn;
	}

	public boolean isPrint ()
	{
		return opcode.string != null;
	}

	public boolean isCall ()
	{
		return opcode.isCall;
	}

	public boolean isJump ()
	{
		// could use jumpTarget != 0
		return (opcode instanceof Opcode1OP && opcode.opcodeNumber == 12);
	}

	public boolean isBranch ()
	{
		return opcode.branch != null;
	}

	public boolean isStore ()
	{
		return opcode.store != null;
	}

	public int target ()
	{
		return isBranch () ? opcode.branch.target : 0;
	}

	@Override
	public String toString ()
	{
		int max = opcode.length ();
		String extra = "";
		if (max > 9)
		{
			max = 9;
			extra = "..";
		}
		String hex = HexFormatter.getHexString (buffer, startPtr, max);
		return String.format ("%-26s%2s %s", hex, extra, opcode.toString ());
	}

	abstract class Opcode
	{
		int opcodeNumber;
		int opcodeLength;
		List<Operand> operands;
		int totalOperandLength;
		ArgumentBranch branch;
		ArgumentString string;
		OperandVariable store;
		boolean isReturn, isCall, isExit;
		int jumpTarget;
		int callTarget;

		public Opcode ()
		{
			operands = new ArrayList<Operand> ();
		}

		@Override
		public String toString ()
		{
			StringBuilder text = new StringBuilder ();
			if (false)
				text.append (HexFormatter.formatNoHeader (buffer, startPtr, length ()) + "\n");
			text.append (String.format ("%05X : %-12s", startPtr, opcodeName ()));
			if (jumpTarget != 0)
				text.append (String.format (" L:%05X", jumpTarget));
			else if (isCall)
			{
				text.append (String.format (" R:%05X (", callTarget));
				int count = 0;
				for (Operand op : operands)
					if (count++ > 0)
						text.append (op + ", ");
				if (operands.size () > 1)
					text.delete (text.length () - 2, text.length ());
				text.append (") --> " + store);
			}
			else
			{
				for (Operand op : operands)
					text.append (" " + op);
				if (branch != null)
					text.append (branch);
				if (store != null)
					text.append (" --> " + store);
				if (string != null)
					text.append (" \"" + string + "\"");
			}
			return text.toString ();
		}

		public int length ()
		{
			int length = totalOperandLength + opcodeLength;
			if (branch != null)
				length += branch.length;
			if (store != null)
				length += store.length;
			if (string != null)
				length += string.length;
			return length;
		}

		public abstract String opcodeName ();

		private void addOperand (Operand operand)
		{
			operands.add (operand);
			totalOperandLength += operand.length;
		}

		protected void addOperand (byte[] buffer, int ptr, boolean bit1, boolean bit2)
		{
			int offset = ptr + totalOperandLength;
			if (bit1)
			{
				if (!bit2)
					addOperand (new OperandVariable (buffer[offset])); // %10
			}
			else if (bit2)
				addOperand (new OperandByte (buffer[offset])); // %01
			else
				addOperand (new OperandWord (header.getWord (offset))); // %00
		}

		protected void addOperand (byte[] buffer, int ptr, boolean bit)
		{
			int address = ptr + totalOperandLength;
			if (address >= buffer.length)
			{
				System.out.println ("Illegal byte address : " + address);
				return;
			}
			if (bit)
				addOperand (new OperandVariable (buffer[address]));
			else
				addOperand (new OperandByte (buffer[address]));
		}

		protected void setVariableOperands (int ptr)
		{
			int value = buffer[ptr + 1] & 0xFF;
			for (int i = 0; i < 4; i++)
			{
				boolean bit1 = ((value & 0x80) == 0x80);
				boolean bit2 = ((value & 0x40) == 0x40);
				if (bit1 && bit2)
					break;
				addOperand (buffer, ptr + 2, bit1, bit2);
				value <<= 2;
			}
		}

		protected void setStore (byte[] buffer)
		{
			store = new OperandVariable (buffer[startPtr + totalOperandLength + opcodeLength]);
		}

		protected void setBranch (byte[] buffer)
		{
			int offset = startPtr + totalOperandLength + (store == null ? 0 : 1) + opcodeLength;
			if ((buffer[offset] & 0x40) == 0x40)
				branch = new ArgumentBranch (buffer[offset], offset);
			else
				branch = new ArgumentBranch (header.getWord (offset), offset);
		}

		protected void setZString (byte[] buffer)
		{
			int offset = startPtr + totalOperandLength + opcodeLength;
			string = new ArgumentString (buffer, offset);
		}
	}

	class Opcode0OP extends Opcode
	{
		public Opcode0OP (byte[] buffer, int ptr)
		{
			opcodeNumber = buffer[ptr] & 0x0F;
			opcodeLength = 1;

			if (opcodeNumber == 5 || opcodeNumber == 6 || opcodeNumber == 13)
				setBranch (buffer);

			if (opcodeNumber == 0 || opcodeNumber == 1 || opcodeNumber == 3 || opcodeNumber == 8)
				isReturn = true;

			if (opcodeNumber == 2 || opcodeNumber == 3)
				setZString (buffer);

			if (opcodeNumber == 7 || opcodeNumber == 10)
				isExit = true;
		}

		@Override
		public String opcodeName ()
		{
			return name0OP[opcodeNumber];
		}
	}

	class Opcode1OP extends Opcode
	{
		public Opcode1OP (byte[] buffer, int ptr)
		{
			opcodeNumber = buffer[ptr] & 0x0F;
			opcodeLength = 1;

			boolean bit1 = ((buffer[ptr] & 0x20) == 0x20);
			boolean bit2 = ((buffer[ptr] & 0x10) == 0x10);
			addOperand (buffer, ptr + 1, bit1, bit2);

			if ((opcodeNumber >= 1 && opcodeNumber <= 4) || opcodeNumber == 8 || opcodeNumber == 14
						|| opcodeNumber == 15)
				setStore (buffer);
			if (opcodeNumber <= 2)
				setBranch (buffer);
			if (opcodeNumber == 12)
				jumpTarget = (short) operands.get (0).value + startPtr - 2 + length ();
			if (opcodeNumber == 11)
				isReturn = true;
		}

		@Override
		public String opcodeName ()
		{
			return name1OP[opcodeNumber];
		}
	}

	abstract class Opcode2OP extends Opcode
	{
		public Opcode2OP ()
		{
			opcodeLength = 1;
		}

		public void setArguments (byte[] buffer)
		{
			if ((opcodeNumber >= 1 && opcodeNumber <= 7) || opcodeNumber == 10)
				setBranch (buffer);
			else if ((opcodeNumber >= 15 && opcodeNumber <= 25) || opcodeNumber == 8 || opcodeNumber == 9)
				setStore (buffer);
		}

		@Override
		public String opcodeName ()
		{
			return name2OP[opcodeNumber];
		}
	}

	class Opcode2OPLong extends Opcode2OP
	{
		public Opcode2OPLong (byte[] buffer, int ptr)
		{
			opcodeNumber = buffer[ptr] & 0x1F;
			boolean bit1 = ((buffer[ptr] & 0x40) == 0x40);
			boolean bit2 = ((buffer[ptr] & 0x20) == 0x20);
			addOperand (buffer, ptr + 1, bit1);
			addOperand (buffer, ptr + 1, bit2);

			setArguments (buffer);
		}
	}

	class Opcode2OPVar extends Opcode2OP
	{
		public Opcode2OPVar (byte[] buffer, int ptr)
		{
			opcodeNumber = buffer[ptr] & 0x1F;
			opcodeLength = 2;
			setVariableOperands (ptr);
			setArguments (buffer);
		}
	}

	class OpcodeVar extends Opcode
	{
		public OpcodeVar (byte[] buffer, int ptr)
		{
			opcodeNumber = buffer[ptr] & 0x1F;
			opcodeLength = 2;
			setVariableOperands (ptr);

			if (opcodeNumber == 0 || opcodeNumber == 7)
				setStore (buffer);
			if (opcodeNumber == 0)
			{
				isCall = true;
				callTarget = operands.get (0).value * 2;
			}
		}

		@Override
		public String opcodeName ()
		{
			return nameVAR[opcodeNumber];
		}
	}

	abstract class Operand
	{
		int length;
		int value;
	}

	class OperandWord extends Operand
	{
		public OperandWord (int value)
		{
			this.value = value;
			length = 2;
		}

		@Override
		public String toString ()
		{
			return String.format ("#%05d", value);
		}
	}

	class OperandByte extends Operand
	{
		public OperandByte (byte value)
		{
			this.value = value & 0xFF;
			length = 1;
		}

		@Override
		public String toString ()
		{
			return String.format ("#%03d", value);
		}
	}

	class OperandVariable extends Operand
	{
		public OperandVariable (byte value)
		{
			this.value = value & 0xFF;
			length = 1;
		}

		@Override
		public String toString ()
		{
			if (value == 0)
				return ("ToS");
			if (value <= 15)
				return (String.format ("L%02d", value));
			return String.format ("G%03d", (value - 15));
		}
	}

	class ArgumentBranch extends Operand
	{
		int target;
		boolean branchOnTrue;

		public ArgumentBranch (byte value, int offset)
		{
			branchOnTrue = (value & 0x80) == 0x80;
			int val = value & 0x3F; // unsigned
			if (val <= 1)
				target = val;
			else
				target = val + offset - 1;
			length = 1;
		}

		public ArgumentBranch (int value, int offset)
		{
			branchOnTrue = (value & 0x8000) == 0x8000;
			int val = value & 0x3FFF; // signed
			if (val > 8191)
				val -= 16384;
			target = val + offset;
			length = 2;
		}

		@Override
		public String toString ()
		{
			StringBuilder text = new StringBuilder ();
			text.append (" [" + (branchOnTrue ? "true" : "false") + "] ");
			if (target == 0 || target == 1)
				text.append (target == 0 ? "RFALSE" : "RTRUE");
			else
				text.append (String.format ("%05X", target));
			return text.toString ();
		}
	}

	class ArgumentString extends Operand
	{
		ZString text;
		int startPtr;
		byte[] buffer;

		public ArgumentString (byte[] buffer, int offset)
		{
			this.buffer = buffer;
			text = new ZString (buffer, offset, header);
			length = text.length;
		}

		@Override
		public String toString ()
		{
			return text.value;
		}
	}
}