package com.bytezone.diskbrowser.applefile;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bytezone.diskbrowser.gui.AssemblerPreferences;
import com.bytezone.diskbrowser.gui.DiskBrowser;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class AssemblerProgram extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  static AssemblerPreferences assemblerPreferences;     // set by MenuHandler

  private static Map<Integer, String> equates;

  private final int loadAddress;
  private int executeOffset;

  private byte[] extraBuffer = new byte[0];

  private List<Integer> entryPoints;
  private List<StringLocation> stringLocations;

  // ---------------------------------------------------------------------------------//
  public static void setAssemblerPreferences (AssemblerPreferences assemblerPreferences)
  // ---------------------------------------------------------------------------------//
  {
    AssemblerProgram.assemblerPreferences = assemblerPreferences;
  }

  // ---------------------------------------------------------------------------------//
  public AssemblerProgram (String name, byte[] buffer, int address)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
    this.loadAddress = address;

    if (equates == null)
      getEquates ();

    //    AssemblerBlocks assemblerBlocks = new AssemblerBlocks (buffer, address);
  }

  // ---------------------------------------------------------------------------------//
  public AssemblerProgram (String name, byte[] buffer, int address, int executeOffset)
  // ---------------------------------------------------------------------------------//
  {
    this (name, buffer, address);
    this.executeOffset = executeOffset;
  }

  // ---------------------------------------------------------------------------------//
  public void setExtraBuffer (byte[] fullBuffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    if (length >= 0)
    {
      this.extraBuffer = new byte[length];
      System.arraycopy (fullBuffer, offset, extraBuffer, 0, length);
    }
    else
      System.out.println ("Invalid length in setExtraBuffer() : " + length);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getHexDump ()
  // ---------------------------------------------------------------------------------//
  {
    // It might be useful to add opt-O to change the offset. Sometimes it's useful
    // to see the hex dump offset from zero, other times it's better to use the
    // load address.
    String text = HexFormatter.format (buffer, 0, buffer.length, loadAddress);

    if (extraBuffer.length == 0)
      return text;

    return text + "\n\nData outside actual buffer:\n\n" + HexFormatter
        .format (extraBuffer, 0, extraBuffer.length, loadAddress + buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getAssembler ()
  // ---------------------------------------------------------------------------------//
  {
    if (buffer == null)
      return "No buffer";

    if (assembler == null)
      this.assembler = new AssemblerProgram (name, buffer, loadAddress);

    if (extraBuffer.length == 0)
      return assembler.getText ();

    String extraName = String.format ("%s (extra)", name);
    AssemblerProgram assemblerProgram =
        new AssemblerProgram (extraName, extraBuffer, loadAddress + buffer.length);

    return assembler.getText () + "\n\n" + assemblerProgram.getText ();
  }

  // ---------------------------------------------------------------------------------//
  private void addHeader (StringBuilder pgm)
  // ---------------------------------------------------------------------------------//
  {
    pgm.append (String.format ("Name    : %s%n", name));
    pgm.append (String.format ("Length  : $%04X (%,d)%n", buffer.length, buffer.length));
    pgm.append (String.format ("Load at : $%04X (%,d)%n", loadAddress, loadAddress));

    if (executeOffset > 0)
      pgm.append (String.format ("Entry   : $%04X%n", (loadAddress + executeOffset)));
    pgm.append ("\n");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder pgm = new StringBuilder ();

    if (assemblerPreferences.showHeader)
      addHeader (pgm);

    pgm.append (getListing ());

    if (assemblerPreferences.showStrings)
      pgm.append (getStringsText ());

    return pgm.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String getListing ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder pgm = new StringBuilder ();

    List<AssemblerStatement> lines = getLines ();

    if (stringLocations == null)
      getStrings ();

    // if the assembly doesn't start at the beginning, just dump the bytes that
    // are skipped
    for (int i = 0; i < executeOffset; i++)
      pgm.append (String.format ("    %04X: %02X%n", (loadAddress + i), buffer[i]));

    for (AssemblerStatement cmd : lines)
    {
      StringBuilder line = new StringBuilder ();

      String arrowText = assemblerPreferences.showTargets ? getArrow (cmd) : "";
      line.append (
          String.format ("%3.3s %04X: %02X ", arrowText, cmd.address, cmd.value));

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
      else if (cmd.target > 0
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

    return pgm.toString ();
  }

  //  private int showString (AssemblerStatement cmd, StringBuilder line)
  //  {
  //    int key = cmd.address - loadAddress;
  //    if (strings.containsKey (key))
  //    {
  //      while (line.length () < 40)
  //        line.append (" ");
  //      String s = strings.get (key);
  //      line.append ("# " + s);
  //      return s.length () - cmd.size;
  //    }
  //    return 0;
  //  }

  // ---------------------------------------------------------------------------------//
  private List<AssemblerStatement> getLines ()
  // ---------------------------------------------------------------------------------//
  {
    List<AssemblerStatement> lines = new ArrayList<> ();
    Map<Integer, AssemblerStatement> linesMap = new HashMap<> ();
    List<Integer> targets = new ArrayList<> ();

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

      // JMP, JMP, JSR
      if (cmd.target >= loadAddress && cmd.target < (loadAddress + buffer.length)
          && (cmd.value == 0x4C || cmd.value == 0x6C || cmd.value == 0x20))
        targets.add (cmd.target);

      // branch relative
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

  // ---------------------------------------------------------------------------------//
  private String getStringsText ()
  // ---------------------------------------------------------------------------------//
  {
    if (stringLocations.size () == 0)
      return "";

    StringBuilder text = new StringBuilder ("\n\nPossible strings:\n\n");
    for (StringLocation stringLocation : stringLocations)
    {
      int address = stringLocation.offset + loadAddress;
      text.append (String.format ("%s %04X - %04X  %s %n",
          entryPoints.contains (stringLocation.offset) ? "*" : " ", address,
          address + stringLocation.length, stringLocation));
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private void getStrings ()
  // ---------------------------------------------------------------------------------//
  {
    entryPoints = new ArrayList<> ();
    stringLocations = new ArrayList<> ();

    int start = 0;
    for (int ptr = 0; ptr < buffer.length; ptr++)
    {
      if ((buffer[ptr] & 0x80) != 0)                    // hi bit set
        continue;

      if (buffer[ptr] == 0x0D)                          // CR
        continue;

      if (ptr - start > 3)
        stringLocations.add (new StringLocation (start, ptr - 1));

      start = ptr + 1;
    }

    if (buffer.length - start > 3)
      stringLocations.add (new StringLocation (start, buffer.length - 1));

    int max = buffer.length - 2;
    for (StringLocation stringLocation : stringLocations)
      for (int ptr = 0; ptr < max; ptr++)
        if (stringLocation.matches (buffer, ptr))
        {
          entryPoints.add (stringLocation.offset);
          break;
        }
  }

  // ---------------------------------------------------------------------------------//
  private String getArrow (AssemblerStatement cmd)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  private boolean isLocal (int target)
  // ---------------------------------------------------------------------------------//
  {
    return target >= loadAddress
        && target < loadAddress + buffer.length + extraBuffer.length;
  }

  // ---------------------------------------------------------------------------------//
  private void getEquates ()
  // ---------------------------------------------------------------------------------//
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
            System.out.printf ("Duplicate equate entry : %04X%n" + address);
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

  // ---------------------------------------------------------------------------------//
  class StringLocation
  // ---------------------------------------------------------------------------------//
  {
    int offset;
    byte hi, lo;
    int length;
    boolean zeroTerminated;
    boolean lowTerminated;
    boolean hasLengthByte;
    int digits;
    int letters;
    int punctuation;
    int controlChars;
    int spaces;

    public StringLocation (int first, int last)
    {
      offset = first;
      length = last - offset + 1;
      int end = last + 1;

      zeroTerminated = end < buffer.length && buffer[end] == 0;
      lowTerminated = end < buffer.length && buffer[end] >= 32 && buffer[end] < 127;

      if (first > 0 && (buffer[first] & 0xFF) == length + 1)
      {
        hasLengthByte = true;
        --offset;
        ++length;
      }

      hi = (byte) ((offset + loadAddress) >>> 8);
      lo = (byte) ((offset + loadAddress) & 0x00FF);

      for (int i = offset; i < offset + length; i++)
      {
        int val = buffer[i] & 0x7F;
        if (val < 32 || val == 127)
          ++controlChars;
        else if (val == 32)
          ++spaces;
        else if (val >= 48 && val <= 57)
          ++digits;
        else if (val >= 65 && val <= 90)
          ++letters;
        else if (val >= 97 && val <= 122)
          ++letters;
        else
          ++punctuation;
      }
    }

    boolean matches (byte[] buffer, int ptr)
    {
      return lo == buffer[ptr] && hi == buffer[ptr + 1];
    }

    boolean likelyString ()
    {
      return spaces > 0 || letters > punctuation;
    }

    public String address ()
    {
      return String.format ("%04X  %02X %02X", offset, hi, lo);
    }

    public String toStatisticsString ()
    {
      return String.format ("%2d, %2d, %2d, %2d, %2d", digits, letters, punctuation,
          controlChars, spaces);
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      if (hasLengthByte)
        text.append ("<length>");

      for (int i = offset; i < offset + length; i++)
      {
        int val = buffer[i] & 0x7F;
        if (val == 4)
          text.append ("<ctrl-D>");
        else if (val == 10)
          text.append ("<LF>");
        else if (val == 13)
          text.append ("<CR>");
        else
          text.append ((char) val);
      }
      if (lowTerminated)
        text.append ((char) buffer[offset + length]);

      return text.toString ();
    }
  }
}