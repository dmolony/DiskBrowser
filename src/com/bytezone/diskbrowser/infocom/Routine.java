package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.infocom.Instruction.Operand;
import com.bytezone.diskbrowser.infocom.Instruction.OperandType;
import com.bytezone.diskbrowser.utilities.HexFormatter;

class Routine extends InfocomAbstractFile
    implements Iterable<Instruction>, Comparable<Routine>
{
  int startPtr, length, strings, locals;

  List<Parameter> parameters = new ArrayList<> ();
  List<Instruction> instructions = new ArrayList<> ();
  List<Integer> calls = new ArrayList<> ();
  List<Integer> calledBy = new ArrayList<> ();
  List<Integer> actions = new ArrayList<> ();          // not used yet
  List<Integer> targets = new ArrayList<> ();

  public Routine (int ptr, Header header, int caller)
  {
    super (String.format ("Routine %05X", ptr), header.buffer);

    locals = buffer[ptr] & 0xFF;
    if (locals > 15)
    {
      System.out.println ("Too many locals: " + locals);
      return;                                     // startPtr will be zero
    }

    startPtr = ptr++;                             // also used to flag a valid routine

    if (!calledBy.contains (caller))
      calledBy.add (caller);

    for (int i = 1; i <= locals; i++)
    {
      parameters.add (new Parameter (i, header.getWord (ptr)));     // default values
      ptr += 2;
    }

    while (true)
    {
      if (buffer[ptr] == 0 || buffer[ptr] == 0x20 || buffer[ptr] == 0x40)
      {
        System.out.println ("Bad instruction found : " + ptr);
        return;
      }

      Instruction instruction = new Instruction (buffer, ptr, header);
      instructions.add (instruction);

      if (instruction.isCall () && instruction.opcode.callTarget > 0) // not stack-based
        calls.add (instruction.opcode.callTarget);

      if (instruction.isPrint ())
        strings++;

      if (instruction.isBranch () && !targets.contains (instruction.target ()))
        targets.add (instruction.target ());

      if (instruction.isJump () && !targets.contains (instruction.target ()))
        targets.add (instruction.target ());

      for (Operand operand : instruction.opcode.operands)
        if (operand.operandType == OperandType.VAR_GLOBAL)
          header.globals.addRoutine (this, operand);

      ptr += instruction.length ();       // point to next instruction
      if (isTarget (ptr))
        continue;

      // is it a backwards jump?
      if (instruction.isJump () && instruction.target () < ptr)
        break;

      // is it an unconditional return?
      if (instruction.isReturn ())
        break;
    }

    length = ptr - startPtr;

    hexBlocks.add (new HexBlock (startPtr, length, null));

    // check for branches outside this routine
    if (true)
    {
      int endPtr = startPtr + length;
      for (Instruction instruction : instructions)
      {
        int target = instruction.target () > 256 ? instruction.target ()
            : instruction.opcode.jumpTarget > 256 ? instruction.opcode.jumpTarget : 0;
        if (target == 0)
          continue;
        if (instruction.isBranch () && (target > endPtr || target < startPtr))
          System.out.println (instruction);
        if (instruction.isJump () && (target > endPtr || target < startPtr))
          System.out.println (instruction);
      }
    }
  }

  String dump ()
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("%05X : %s", startPtr,
        HexFormatter.getHexString (buffer, startPtr, 1 + locals * 2)));
    text.append ("\n");
    for (Instruction instruction : instructions)
    {
      text.append (instruction.dump ());
      text.append ("\n");
    }
    return text.toString ();
  }

  boolean isValid ()
  {
    return startPtr > 0;
  }

  // test whether the routine contains any instructions pointing to this address
  private boolean isTarget (int ptr)
  {
    for (Instruction ins : instructions)
    {
      if (ins.isBranch () && ins.target () == ptr)
        return true;
      // should this be calling ins.target () ?
      if (ins.isJump () && ins.opcode.jumpTarget == ptr)
        return true;
    }
    return false;
  }

  public void addCaller (int caller)
  {
    calledBy.add (caller);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Called by : %3d%n", calledBy.size ()));
    text.append (String.format ("Calls     : %3d%n", calls.size ()));
    text.append (String.format ("Length    : %3d%n%n", length));

    text.append (String.format ("%05X : %d%n", startPtr, locals));

    for (Parameter parameter : parameters)
      text.append (parameter.toString () + "\n");

    text.append ("\n");

    for (Instruction instruction : instructions)
    {
      text.append (instruction.getHex ());
      int offset = instruction.startPtr;
      if (targets.contains (offset))
        text.append ("  L000 ");
      else
        text.append ("       ");
      text.append (instruction + "\n");
    }

    if (calledBy.size () > 0)
    {
      text.append ("\n\nCalled by\n\n");
      for (int i : calledBy)
        text.append (String.format ("%05X%n", i));
    }

    if (calls.size () > 0)
    {
      text.append ("\n\nCalls\n\n");
      for (int i : calls)
        text.append (String.format ("%05X%n", i));
    }

    if (targets.size () > 0)
    {
      text.append ("\n\nTargets\n\n");
      for (int i : targets)
        text.append (String.format ("%05X%n", i));
    }

    return text.toString ();
  }

  @Override
  public String toString ()
  {
    return String.format ("[Start: %05X, Len: %4d, Strings: %2d, Locals: %2d]", startPtr,
        length, strings, locals);
  }

  class Parameter
  {
    int value;
    int sequence;

    public Parameter (int sequence, int value)
    {
      this.value = value;
      this.sequence = sequence;
    }

    @Override
    public String toString ()
    {
      return String.format ("%05X : L%02d : %d", (startPtr + (sequence - 1) * 2 + 1),
          sequence, value);
    }
  }

  @Override
  public Iterator<Instruction> iterator ()
  {
    return instructions.iterator ();
  }

  @Override
  public int compareTo (Routine o)
  {
    return startPtr - o.startPtr;
  }
}