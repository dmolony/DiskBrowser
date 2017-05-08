package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class IntegerBasicProgram extends AbstractFile
{
  private static String[] tokens =
      { "?", "?", "?", " : ", "?", "?", "?", "?", "?", "?", "?", "?", "CLR", "?", "?",
        "?", "HIMEM: ", "LOMEM: ", " + ", " - ", " * ", " / ", " = ", " # ", " >= ",
        " > ", " <= ", " <> ", " < ", " AND ", " OR ", " MOD ", "^", "+", "(", ",",
        " THEN ", " THEN ", ",", ",", "\"", "\"", "(", "!", "!", "(", "PEEK ", "RND ",
        "SGN", "ABS", "PDL", "RNDX", "(", "+", "-", "NOT ", "(", "=", "#", "LEN(", "ASC(",
        "SCRN(", ",", "(", "$", "$", "(", ", ", ",", ";", ";", ";", ",", ",", ",", "TEXT",
        "GR ", "CALL ", "DIM ", "DIM ", "TAB ", "END", "INPUT ", "INPUT ", "INPUT ",
        "FOR ", " = ", " TO ", " STEP ", "NEXT ", ",", "RETURN", "GOSUB ", "REM ", "LET ",
        "GOTO ", "IF ", "PRINT ", "PRINT ", "PRINT", "POKE ", ",", "COLOR=", "PLOT", ",",
        "HLIN", ",", " AT ", "VLIN ", ",", " AT ", "VTAB ", " = ", " = ", ")", ")",
        "LIST ", ",", "LIST ", "POP ", "NODSP ", "NODSP ", "NOTRACE ", "DSP ", "DSP ",
        "TRACE ", "PR#", "IN#", };

  public IntegerBasicProgram (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  @Override
  public String getText ()
  {
    StringBuilder pgm = new StringBuilder ();
    pgm.append ("Name    : " + name + "\n");
    pgm.append ("Length  : $" + HexFormatter.format4 (buffer.length) + " ("
        + buffer.length + ")\n\n");
    int ptr = 0;

    boolean looksLikeAssembler = checkForAssembler ();      // this can probably go
    boolean looksLikeSCAssembler = checkForSCAssembler ();

    while (ptr < buffer.length)
    {
      int lineLength = buffer[ptr] & 0xFF;
      /*
       * It appears that lines ending in 00 are S-C Assembler programs, and
       * lines ending in 01 are Integer Basic programs.
       */
      int p2 = ptr + lineLength - 1;
      if (p2 < 0 || p2 >= buffer.length || (buffer[p2] != 1 && buffer[p2] != 0))
      {
        pgm.append ("\nPossible assembler code follows\n");
        break;
      }
      if (lineLength <= 0)
        break;

      if (looksLikeSCAssembler)
        appendSCAssembler (pgm, ptr, lineLength);
      else if (looksLikeAssembler)
        appendAssembler (pgm, ptr, lineLength);
      else
        appendInteger (pgm, ptr, lineLength);

      pgm.append ("\n");
      ptr += lineLength;
    }

    if (ptr < buffer.length)
    {
      int address = HexFormatter.intValue (buffer[ptr + 2], buffer[ptr + 3]);
      int remainingBytes = buffer.length - ptr - 5;
      byte[] newBuffer = new byte[remainingBytes];
      System.arraycopy (buffer, ptr + 4, newBuffer, 0, remainingBytes);
      AssemblerProgram ap = new AssemblerProgram ("embedded", newBuffer, address);
      pgm.append ("\n" + ap.getText () + "\n");
    }

    pgm.deleteCharAt (pgm.length () - 1);
    return pgm.toString ();
  }

  private void appendAssembler (StringBuilder pgm, int ptr, int lineLength)
  {
    for (int i = ptr + 3; i < ptr + lineLength - 1; i++)
    {
      if ((buffer[i] & 0x80) == 0x80)
      {
        int spaces = buffer[i] & 0x0F;
        for (int j = 0; j < spaces; j++)
          pgm.append (' ');
        continue;
      }
      int b = buffer[i] & 0xFF;
      pgm.append ((char) b);
    }
  }

  private boolean checkForAssembler ()
  {
    int ptr = 0;

    while (ptr < buffer.length)
    {
      int lineLength = buffer[ptr] & 0xFF;
      if (lineLength == 255)
        System.out.printf ("Line length %d%n", lineLength);
      int p2 = ptr + lineLength - 1;

      if (p2 < 0 || p2 >= buffer.length || (buffer[p2] != 1 && buffer[p2] != 0))
        break;
      if (lineLength <= 0)                   // in case of looping bug
        break;
      // check for comments
      if (buffer[ptr + 3] == 0x3B || buffer[ptr + 3] == 0x2A)
        return true;
      ptr += lineLength;
    }

    return false;
  }

  private boolean checkForSCAssembler ()
  {
    if (buffer.length == 0)
    {
      System.out.println ("Empty buffer array");
      return false;
    }
    int lineLength = buffer[0] & 0xFF;
    if (lineLength <= 0)
      return false;
    return buffer[lineLength - 1] == 0;
  }

  private void appendSCAssembler (StringBuilder pgm, int ptr, int lineLength)
  {
    int lineNumber = (buffer[ptr + 2] & 0xFF) * 256 + (buffer[ptr + 1] & 0xFF);
    pgm.append (String.format ("%4d: ", lineNumber));
    int p2 = ptr + 3;
    while (buffer[p2] != 0)
    {
      if (buffer[p2] == (byte) 0xC0)
      {
        int repeat = buffer[p2 + 1];
        for (int i = 0; i < repeat; i++)
          pgm.append ((char) buffer[p2 + 2]);
        p2 += 2;
      }
      else if ((buffer[p2] & 0x80) != 0)
      {
        int spaces = buffer[p2] & 0x7F;
        for (int i = 0; i < spaces; i++)
          pgm.append (' ');
      }
      else
        pgm.append ((char) buffer[p2]);
      p2++;
    }
  }

  private void appendInteger (StringBuilder pgm, int ptr, int lineLength)
  {
    int lineNumber = HexFormatter.intValue (buffer[ptr + 1], buffer[ptr + 2]);

    boolean inString = false;
    boolean inRemark = false;

    String lineText = String.format ("%5d ", lineNumber);
    int lineTab = lineText.length ();
    pgm.append (lineText);

    for (int p = ptr + 3; p < ptr + lineLength - 1; p++)
    {
      int b = buffer[p] & 0xFF;

      if (b == 0x03 // token for colon (:)
          && !inString && !inRemark && buffer[p + 1] != 1)        // not end of line
      {
        pgm.append (":\n" + "         ".substring (0, lineTab));
        continue;
      }

      if (b >= 0xB0 && b <= 0xB9                        // numeric literal
          && (buffer[p - 1] & 0x80) == 0                // not a variable name
          && !inString && !inRemark)
      {
        pgm.append (HexFormatter.intValue (buffer[p + 1], buffer[p + 2]));
        p += 2;
        continue;
      }

      if (b >= 128)
      {
        b -= 128;
        if (b >= 32)
          pgm.append ((char) b);
        else
          pgm.append ("<ctrl-" + (char) (b + 64) + ">");
      }
      else if (!tokens[b].equals ("?"))
      {
        pgm.append (tokens[b]);
        if ((b == 40 || b == 41) && !inRemark) // double quotes
          inString = !inString;
        if (b == 0x5D)
          inRemark = true;
      }
      else
        pgm.append (" ." + HexFormatter.format2 (b) + ". ");
    }
  }

  @Override
  public String getHexDump ()
  {
    if (false)
      return super.getHexDump ();

    StringBuffer pgm = new StringBuffer ();

    pgm.append ("Name : " + name + "\n");
    pgm.append ("Length : $" + HexFormatter.format4 (buffer.length) + " (" + buffer.length
        + ")\n\n");

    int ptr = 0;

    while (ptr < buffer.length)
    {
      int lineLength = buffer[ptr] & 0xFF;
      int p2 = ptr + lineLength - 1;
      if (p2 < 0 || p2 >= buffer.length || buffer[p2] > 1)
      {
        System.out.println ("invalid line");
        break;
      }
      pgm.append (HexFormatter.formatNoHeader (buffer, ptr, lineLength));
      pgm.append ("\n");
      if (lineLength <= 0)
      {
        System.out.println ("looping");
        break;
      }
      ptr += lineLength;
      pgm.append ("\n");
    }

    if (pgm.length () > 0)
      pgm.deleteCharAt (pgm.length () - 1);

    return pgm.toString ();
  }

/*
 * https://groups.google.com/forum/#!topic/comp.sys.apple2/Baf36jyqwAM
 * To convert Integer Basic to Applesoft

INPUT comands - change comma to semi-colon
remove all DIM of a string variable (not needed)
change string variables to use MID$ - i.e.  A$(1,1)(in INT) is MID$(A$,1,1)(in AS basic)
change GOTO or GOSUB with a variable to ON GOTO
change IF statements to ON GOTO where possible and convert to multiple lines.  
All statements that follow an IF on the same line are executed whether the statement 
is true or not.
change MOD function to  X=Y-(INT(Y/Z)*Z)
change "#" to "<>"
change TAB to HTAB
change RND(X) to INT(RND(1)*X)
relocate ML programs and change CALL'S  and POKE'S.  Since INT programs go from 
HIMEM down, binary code is usually in low memory.

These few are not necessary but make for compact code.

change CALL -384 to INVERSE
change CALL -380 to NORMAL
change CALL -936 to HOME 
 */
}