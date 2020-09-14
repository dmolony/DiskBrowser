package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class AssemblerBlocks
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public AssemblerBlocks (byte[] buffer, int loadAddress)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = 0;
    boolean inCode = true;
    int address = loadAddress;
    List<Integer> loadTargets = new ArrayList<> ();
    List<Integer> branchTargets = new ArrayList<> ();

    while (ptr < buffer.length)
    {
      if (branchTargets.contains (address))
        inCode = true;

      if (inCode)
      {
        AssemblerStatement cmd = new AssemblerStatement (buffer[ptr]);

        if (cmd.size == 2 && ptr < buffer.length - 1)
          cmd.addData (buffer[ptr + 1]);
        else if (cmd.size == 3 && ptr < buffer.length - 2)
          cmd.addData (buffer[ptr + 1], buffer[ptr + 2]);
        else
          cmd.size = 1;

        cmd.address = address;

        if (branchTargets.contains (address))
          System.out.print ("> ");
        else
          System.out.print ("  ");
        System.out.printf ("%s%n", cmd);
        ptr += cmd.size;
        address += cmd.size;

        // RTS, JMP
        if (cmd.opcode == 0x60 || cmd.opcode == 0x4C)
          inCode = false;

        // JMP, JMP, JSR
        //        System.out.printf ("%02X %02X %02X%n", cmd.opcode, cmd.operand1, cmd.operand2);
        if (cmd.opcode == 0x4C || cmd.opcode == 0x6C || cmd.opcode == 0x20)
        {
          branchTargets.add (cmd.target);
        }

        if (cmd.opcode == 0xB9)
        {
          int target = ((cmd.operand2 & 0xFF) << 8) | (cmd.operand1 & 0xFF);
          loadTargets.add (target);
        }

        // branch relative
        if (cmd.offset != 0)
          branchTargets.add (cmd.address + cmd.offset + 2);
      }
      else
      {
        if (loadTargets.contains (address))
          System.out.print ("* ");
        else
          System.out.print ("  ");
        System.out.printf ("%06X  %02X  %s%n", address, buffer[ptr],
            (char) (buffer[ptr] & 0x7F));
        ptr += 1;
        address += 1;
      }
    }

    for (int loadTarget : loadTargets)
      System.out.printf ("load  : $%04X%n", loadTarget);
    for (int branchTarget : branchTargets)
      System.out.printf ("branch: $%04X%n", branchTarget);
  }
}
