package com.bytezone.diskbrowser.applefile;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.gui.DiskBrowser;

public class AssemblerProgram extends AbstractFile
{
  private static Map<Integer, String> equates;

  private final int loadAddress;
  private int executeOffset;

  private byte[] extraBuffer = new byte[0];

  public AssemblerProgram (String name, byte[] buffer, int address)
  {
    super (name, buffer);
    this.loadAddress = address;

    if (equates == null)
      getEquates ();
  }

  public AssemblerProgram (String name, byte[] buffer, int address, int executeOffset)
  {
    this (name, buffer, address);
    this.executeOffset = executeOffset;
  }

  public void setExtraBuffer (byte[] fullBuffer, int offset, int length)
  {
    if (length >= 0)
    {
      this.extraBuffer = new byte[length];
      System.arraycopy (fullBuffer, offset, extraBuffer, 0, length);
    }
    else
      System.out.println ("Invalid length in setExtraBuffer() : " + length);
  }

  @Override
  public String getHexDump ()
  {
    // It might be useful to add opt-O to change the offset. Sometimes it's useful
    // to see the hex dump offset from zero, other times it's better to use the
    // load address.
    String text = HexFormatter.format (buffer, 0, buffer.length, loadAddress);

    if (extraBuffer.length == 0)
      return text;

    return text + "\n\n" + HexFormatter.format (extraBuffer, 0, extraBuffer.length,
                                                loadAddress + buffer.length);
  }

  @Override
  public String getAssembler ()
  {
    //    String text = super.getAssembler ();
    if (buffer == null)
      return "No buffer";
    if (assembler == null)
      this.assembler = new AssemblerProgram (name, buffer, loadAddress);
    //    return assembler.getText ();

    if (extraBuffer.length == 0)
      return assembler.getText ();

    String extraName = String.format ("%s (extra)", name);
    AssemblerProgram assemblerProgram =
        new AssemblerProgram (extraName, extraBuffer, loadAddress + buffer.length);

    return assembler.getText () + "\n\n" + assemblerProgram.getText ();
  }

  @Override
  public String getText ()
  {
    StringBuilder pgm = new StringBuilder ();

    pgm.append (String.format ("Name    : %s%n", name));
    pgm.append (String.format ("Length  : $%04X (%,d)%n", buffer.length, buffer.length));
    pgm.append (String.format ("Load at : $%04X (%,d)%n", loadAddress, loadAddress));

    if (executeOffset > 0)
      pgm.append (String.format ("Entry   : $%04X%n", (loadAddress + executeOffset)));
    pgm.append (String.format ("%n"));

    return pgm.append (getStringBuilder2 ()).toString ();
  }

  private StringBuilder getStringBuilder ()
  {
    if (true)
      return getStringBuilder2 ();

    StringBuilder pgm = new StringBuilder ();

    int ptr = executeOffset;
    int address = loadAddress + executeOffset;

    // if the assembly doesn't start at the beginning, just dump the bytes that 
    // are skipped
    for (int i = 0; i < executeOffset; i++)
      pgm.append (String.format ("%04X: %02X%n", (loadAddress + i), buffer[i]));

    while (ptr < buffer.length)
    {
      StringBuilder line = new StringBuilder ();

      AssemblerStatement cmd = new AssemblerStatement (buffer[ptr]);

      if (cmd.size == 2 && ptr < buffer.length - 1)
        cmd.addData (buffer[ptr + 1]);
      else if (cmd.size == 3 && ptr < buffer.length - 2)
        cmd.addData (buffer[ptr + 1], buffer[ptr + 2]);
      else
        cmd.size = 1;

      line.append (String.format ("%04X: ", address));
      for (int i = 0; i < cmd.size; i++)
        line.append (String.format ("%02X ", buffer[ptr + i]));
      while (line.length () < 20)
        line.append (" ");
      line.append (cmd.mnemonic + " " + cmd.operand);
      if (cmd.offset != 0)
      {
        int branch = address + cmd.offset + 2;
        line.append (String.format ("$%04X", branch < 0 ? branch += 0xFFFF : branch));
      }

      if (cmd.target > 0
          && (cmd.target < loadAddress - 1 || cmd.target > (loadAddress + buffer.length)))
      {
        while (line.length () < 40)
          line.append (" ");

        String text = equates.get (cmd.target);
        if (text != null)
          line.append ("; " + text);
        else
          for (int i = 0, max = ApplesoftConstants.tokenAddresses.length; i < max; i++)
            if (cmd.target == ApplesoftConstants.tokenAddresses[i])
            {
              line.append ("; Applesoft - " + ApplesoftConstants.tokens[i]);
              break;
            }
      }
      pgm.append (line.toString () + "\n");
      address += cmd.size;
      ptr += cmd.size;
    }

    if (pgm.length () > 0)
      pgm.deleteCharAt (pgm.length () - 1);

    return pgm;
  }

  private StringBuilder getStringBuilder2 ()
  {
    StringBuilder pgm = new StringBuilder ();
    List<AssemblerStatement> lines = getLines ();

    // if the assembly doesn't start at the beginning, just dump the bytes that 
    // are skipped
    for (int i = 0; i < executeOffset; i++)
      pgm.append (String.format ("    %04X: %02X%n", (loadAddress + i), buffer[i]));

    for (AssemblerStatement cmd : lines)
    {
      StringBuilder line = new StringBuilder ();

      line.append (String.format ("%3.3s %04X: %02X ", getArrow (cmd), cmd.address,
                                  cmd.value));

      if (cmd.size > 1)
        line.append (String.format ("%02X ", cmd.operand1));
      if (cmd.size > 2)
        line.append (String.format ("%02X ", cmd.operand2));

      while (line.length () < 23)
        line.append (" ");

      line.append (cmd.mnemonic + " " + cmd.operand);
      if (cmd.offset != 0)
      {
        int branch = cmd.address + cmd.offset + 2;
        line.append (String.format ("$%04X", branch < 0 ? branch += 0xFFFF : branch));
      }

      if (cmd.target > 0
          && (cmd.target < loadAddress - 1 || cmd.target > (loadAddress + buffer.length)))
      {
        while (line.length () < 40)
          line.append (" ");

        String text = equates.get (cmd.target);
        if (text != null)
          line.append ("; " + text);
        else
          for (int i = 0, max = ApplesoftConstants.tokenAddresses.length; i < max; i++)
            if (cmd.target == ApplesoftConstants.tokenAddresses[i])
            {
              line.append ("; Applesoft - " + ApplesoftConstants.tokens[i]);
              break;
            }
      }
      pgm.append (line.toString () + "\n");
    }

    if (pgm.length () > 0)
      pgm.deleteCharAt (pgm.length () - 1);

    return pgm;
  }

  private List<AssemblerStatement> getLines ()
  {
    List<AssemblerStatement> lines = new ArrayList<AssemblerStatement> ();
    Map<Integer, AssemblerStatement> linesMap =
        new HashMap<Integer, AssemblerStatement> ();
    List<Integer> targets = new ArrayList<Integer> ();

    int ptr = executeOffset;
    int address = loadAddress + executeOffset;

    while (ptr < buffer.length)
    {
      AssemblerStatement cmd = new AssemblerStatement (buffer[ptr]);
      lines.add (cmd);
      linesMap.put (address, cmd);
      cmd.address = address;

      if (cmd.size == 2 && ptr < buffer.length - 1)
        cmd.addData (buffer[ptr + 1]);
      else if (cmd.size == 3 && ptr < buffer.length - 2)
        cmd.addData (buffer[ptr + 1], buffer[ptr + 2]);
      else
        cmd.size = 1;

      if (cmd.target >= loadAddress && cmd.target < (loadAddress + buffer.length)
          && (cmd.value == 0x4C || cmd.value == 0x6C || cmd.value == 0x20))
        targets.add (cmd.target);
      if (cmd.offset != 0)
        targets.add (cmd.address + cmd.offset + 2);

      address += cmd.size;
      ptr += cmd.size;
    }

    for (Integer target : targets)
    {
      AssemblerStatement cmd = linesMap.get (target);
      if (cmd != null)
        cmd.isTarget = true;
    }

    return lines;
  }

  private String getArrow (AssemblerStatement cmd)
  {
    String arrow = "";
    if (cmd.value == 0x4C || cmd.value == 0x6C || cmd.value == 0x60 || cmd.offset != 0)
      arrow = "<--";
    if (cmd.value == 0x20 && isLocal (cmd.target))    // JSR
      arrow = "<--";
    if (cmd.isTarget)
      if (arrow.isEmpty ())
        arrow = "-->";
      else
        arrow = "<->";
    return arrow;
  }

  private boolean isLocal (int target)
  {
    return target >= loadAddress
        && target < loadAddress + buffer.length + extraBuffer.length;
  }

  private void getEquates ()
  {
    equates = new HashMap<Integer, String> ();
    DataInputStream inputEquates =
        new DataInputStream (DiskBrowser.class.getClassLoader ()
            .getResourceAsStream ("com/bytezone/diskbrowser/applefile/equates.txt"));
    BufferedReader in = new BufferedReader (new InputStreamReader (inputEquates));

    String line;
    try
    {
      while ((line = in.readLine ()) != null)
      {
        if (!line.isEmpty () && !line.startsWith ("*"))
        {
          int address = Integer.parseInt (line.substring (0, 4), 16);
          if (equates.containsKey (address))
            System.out.println ("Duplicate equate entry : " + address);
          else
            equates.put (address, line.substring (6));
        }
      }
      in.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }
}