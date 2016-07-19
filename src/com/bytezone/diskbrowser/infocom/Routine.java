package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Routine extends InfocomAbstractFile
    implements Iterable<Instruction>, Comparable<Routine>
{
  private static final String padding = "                             ";

  int startPtr, length, strings, locals;
  private final Header header;

  List<Parameter> parameters = new ArrayList<Parameter> ();
  List<Instruction> instructions = new ArrayList<Instruction> ();
  List<Integer> calls = new ArrayList<Integer> ();
  List<Integer> calledBy = new ArrayList<Integer> ();
  List<Integer> actions = new ArrayList<Integer> ();          // not used yet

  public Routine (int ptr, Header header, int caller)
  {
    super (String.format ("Routine %05X", ptr), header.buffer);
    this.header = header;

    locals = buffer[ptr] & 0xFF;
    if (locals > 15)
      return;

    startPtr = ptr++;                             // also used to flag a valid routine
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

      ptr += instruction.length ();

      // is it a backwards jump?
      if (instruction.isJump () && instruction.target () < ptr && !moreCode (ptr))
        break;

      // is it an unconditional return?
      if (instruction.isReturn () && !moreCode (ptr))
        break;
    }
    length = ptr - startPtr;

    hexBlocks.add (new HexBlock (startPtr, length, null));

    // check for branches outside this routine
    if (true)
    {
      int endPtr = startPtr + length;
      for (Instruction ins : instructions)
      {
        int target = ins.target () > 256 ? ins.target ()
            : ins.opcode.jumpTarget > 256 ? ins.opcode.jumpTarget : 0;
        if (target == 0)
          continue;
        if (ins.isBranch () && (target > endPtr || target < startPtr))
          System.out.println (ins);
        if (ins.isJump () && (target > endPtr || target < startPtr))
          System.out.println (ins);
      }
    }
  }

  // test whether the routine contains any instructions pointing to this address
  private boolean moreCode (int ptr)
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
    text.append (String.format ("Calls     : %3d%n%n", calls.size ()));
    text.append (String.format ("%s%05X : %d%n", padding, startPtr, locals));
    for (Parameter parameter : parameters)
      text.append (padding + parameter.toString () + "\n");
    text.append ("\n");
    for (Instruction instruction : instructions)
      text.append (instruction + "\n");
    return text.toString ();
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