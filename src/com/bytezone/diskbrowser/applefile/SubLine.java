package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class SubLine
// -----------------------------------------------------------------------------------//
{
  SourceLine parent;
  ApplesoftBasicProgram program;
  int startPtr;
  int length;
  String[] nextVariables;
  String forVariable = "";
  int equalsPosition;               // used for aligning the equals sign
  byte[] buffer;

  private final List<Integer> gotoLines = new ArrayList<> ();
  private final List<Integer> gosubLines = new ArrayList<> ();
  private final List<String> symbols = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  SubLine (SourceLine parent, int startPtr, int length)
  // ---------------------------------------------------------------------------------//
  {
    this.parent = parent;
    this.startPtr = startPtr;
    this.length = length;

    program = parent.parent;

    this.buffer = parent.buffer;
    byte firstByte = buffer[startPtr];

    if (Utility.isHighBitSet (firstByte))
      doToken (firstByte);
    else if (Utility.isDigit (firstByte))
      doDigit ();
    else
      doAlpha ();

    if (is (ApplesoftConstants.TOKEN_REM) || is (ApplesoftConstants.TOKEN_DATA))
      return;

    int ptr = startPtr;
    length--;
    String var = "";
    boolean inQuote = false;

    while (length-- > 0)
    {
      byte b = buffer[ptr++];

      if (inQuote && b != Utility.ASCII_QUOTE)
        continue;

      if (Utility.isPossibleVariable (b))
        var += (char) b;
      else
      {
        checkVar (var, b);
        var = "";

        if (b == Utility.ASCII_QUOTE)
          inQuote = !inQuote;
      }
    }
    checkVar (var, (byte) 0);
  }

  // ---------------------------------------------------------------------------------//
  private void checkVar (String var, byte terminator)
  // ---------------------------------------------------------------------------------//
  {
    if (var.length () == 0)
      return;

    if (terminator == Utility.ASCII_LEFT_BRACKET)
      var += "(";

    if (Utility.isLetter ((byte) var.charAt (0)) && !symbols.contains (var))
      symbols.add (var);
  }

  // ---------------------------------------------------------------------------------//
  List<String> getSymbols ()
  // ---------------------------------------------------------------------------------//
  {
    return symbols;
  }

  // ---------------------------------------------------------------------------------//
  List<Integer> getGotoLines ()
  // ---------------------------------------------------------------------------------//
  {
    return gotoLines;
  }

  // ---------------------------------------------------------------------------------//
  List<Integer> getGosubLines ()
  // ---------------------------------------------------------------------------------//
  {
    return gosubLines;
  }

  // ---------------------------------------------------------------------------------//
  private void doToken (byte b)
  // ---------------------------------------------------------------------------------//
  {
    switch (b)
    {
      case ApplesoftConstants.TOKEN_FOR:
        int p = startPtr + 1;
        while (buffer[p] != ApplesoftConstants.TOKEN_EQUALS)
          forVariable += (char) buffer[p++];
        break;

      case ApplesoftConstants.TOKEN_NEXT:
        if (length == 2)                // no variables
          nextVariables = new String[0];
        else
        {
          String varList = new String (buffer, startPtr + 1, length - 2);
          nextVariables = varList.split (",");
        }
        break;

      case ApplesoftConstants.TOKEN_LET:
        recordEqualsPosition ();
        break;

      case ApplesoftConstants.TOKEN_GOTO:
        int targetLine = getLineNumber (buffer, startPtr + 1);
        addXref (targetLine, gotoLines);
        break;

      case ApplesoftConstants.TOKEN_GOSUB:
        targetLine = getLineNumber (buffer, startPtr + 1);
        addXref (targetLine, gosubLines);
        break;

      case ApplesoftConstants.TOKEN_ON:
        p = startPtr + 1;
        int max = startPtr + length - 1;
        while (p < max && buffer[p] != ApplesoftConstants.TOKEN_GOTO
            && buffer[p] != ApplesoftConstants.TOKEN_GOSUB)
          p++;

        switch (buffer[p++])
        {
          case ApplesoftConstants.TOKEN_GOSUB:
            for (int destLine : getLineNumbers (buffer, p))
              addXref (destLine, gosubLines);
            break;

          case ApplesoftConstants.TOKEN_GOTO:
            for (int destLine : getLineNumbers (buffer, p))
              addXref (destLine, gotoLines);
            break;

          default:
            System.out.println ("GOTO / GOSUB not found");
        }
        break;

      case ApplesoftConstants.TOKEN_ONERR:
        if (buffer[startPtr + 1] == ApplesoftConstants.TOKEN_GOTO)
        {
          targetLine = getLineNumber (buffer, startPtr + 2);
          addXref (targetLine, gotoLines);
        }
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  private void doDigit ()
  // ---------------------------------------------------------------------------------//
  {
    int targetLine = getLineNumber (buffer, startPtr);
    addXref (targetLine, gotoLines);
  }

  // ---------------------------------------------------------------------------------//
  private void doAlpha ()
  // ---------------------------------------------------------------------------------//
  {
    recordEqualsPosition ();
  }

  // ---------------------------------------------------------------------------------//
  private void addXref (int targetLine, List<Integer> list)
  // ---------------------------------------------------------------------------------//
  {
    if (!list.contains (targetLine))
      list.add (targetLine);
  }

  // ---------------------------------------------------------------------------------//
  private List<Integer> getLineNumbers (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lineNumbers = new ArrayList<> ();
    int start = ptr;

    while (ptr < buffer.length && buffer[ptr] != 0 && buffer[ptr] != Utility.ASCII_COLON)
      ptr++;

    String s = new String (buffer, start, ptr - start);

    String[] chunks = s.split (",");

    try
    {
      for (String chunk : chunks)
        lineNumbers.add (Integer.parseInt (chunk));
    }
    catch (NumberFormatException e)
    {
      System.out.printf ("NFE: %s%n", s);
    }

    return lineNumbers;
  }

  // ---------------------------------------------------------------------------------//
  private int getLineNumber (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    int lineNumber = 0;
    while (ptr < buffer.length)
    {
      int b = (buffer[ptr++] & 0xFF) - 0x30;
      if (b < 0 || b > 9)
        break;
      lineNumber = lineNumber * 10 + b;
    }
    return lineNumber;
  }

  // ---------------------------------------------------------------------------------//
  boolean isImpliedGoto ()
  // ---------------------------------------------------------------------------------//
  {
    byte b = buffer[startPtr];
    if (Utility.isHighBitSet (b))
      return false;
    return (Utility.isDigit (b));
  }

  // Record the position of the equals sign so it can be aligned with adjacent lines.
  // ---------------------------------------------------------------------------------//
  private void recordEqualsPosition ()
  // ---------------------------------------------------------------------------------//
  {
    int p = startPtr + 1;
    int max = startPtr + length;
    while (buffer[p] != ApplesoftConstants.TOKEN_EQUALS && p < max)
      p++;
    if (buffer[p] == ApplesoftConstants.TOKEN_EQUALS)
      equalsPosition = toString ().indexOf ('=');           // use expanded line
  }

  // ---------------------------------------------------------------------------------//
  boolean isJoinableRem ()
  // ---------------------------------------------------------------------------------//
  {
    return is (ApplesoftConstants.TOKEN_REM) && !isFirst ();
  }

  // ---------------------------------------------------------------------------------//
  boolean isFirst ()
  // ---------------------------------------------------------------------------------//
  {
    return (parent.linePtr + 4) == startPtr;
  }

  // ---------------------------------------------------------------------------------//
  boolean is (byte token)
  // ---------------------------------------------------------------------------------//
  {
    return buffer[startPtr] == token;
  }

  // ---------------------------------------------------------------------------------//
  boolean has (byte token)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = startPtr + 1;
    int max = startPtr + length;

    while (ptr < max)
    {
      if (buffer[ptr++] == token)
        return true;
    }
    return false;
  }

  // ---------------------------------------------------------------------------------//
  boolean isEmpty ()
  // ---------------------------------------------------------------------------------//
  {
    return length == 1 && buffer[startPtr] == 0;
  }

  // ---------------------------------------------------------------------------------//
  boolean containsToken ()
  // ---------------------------------------------------------------------------------//
  {
    // ignore first byte, check the rest for tokens
    for (int p = startPtr + 1, max = startPtr + length; p < max; p++)
      if (Utility.isHighBitSet (buffer[p]))
        return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  boolean containsControlChars ()
  // ---------------------------------------------------------------------------------//
  {
    for (int p = startPtr + 1, max = startPtr + length; p < max; p++)
    {
      int c = buffer[p] & 0xFF;
      if (c == 0)
        break;

      if (c < 32)
        return true;
    }

    return false;
  }

  // ---------------------------------------------------------------------------------//
  void addFormattedRem (StringBuilder text)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = startPtr + 1;
    int max = startPtr + length - 2;

    while (ptr <= max)
    {
      switch (buffer[ptr])
      {
        case Utility.ASCII_BACKSPACE:
          if (text.length () > 0)
            text.deleteCharAt (text.length () - 1);
          break;

        case Utility.ASCII_CR:
          text.append ("\n");
          break;

        default:
          text.append ((char) buffer[ptr]);     // do not mask with 0xFF
      }

      ptr++;
    }
  }

  // ---------------------------------------------------------------------------------//
  public int getAddress ()
  // ---------------------------------------------------------------------------------//
  {
    return program.getLoadAddress () + startPtr;
  }

  // ---------------------------------------------------------------------------------//
  public String getAlignedText (int alignEqualsPos)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = toStringBuilder ();      // get line

    // insert spaces before '=' until it lines up with the other assignment lines
    if (!is (ApplesoftConstants.TOKEN_REM))
      while (alignEqualsPos-- > equalsPosition)
        line.insert (equalsPosition, ' ');

    return line.toString ();
  }

  // A REM statement might conceal an assembler routine
  // ---------------------------------------------------------------------------------//
  public String[] getAssembler ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer2 = new byte[length - 1];
    System.arraycopy (buffer, startPtr + 1, buffer2, 0, buffer2.length);
    AssemblerProgram program =
        new AssemblerProgram ("REM assembler", buffer2, getAddress () + 1);

    return program.getAssembler ().split ("\n");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return toStringBuilder ().toString ();
  }

  // ---------------------------------------------------------------------------------//
  public StringBuilder toStringBuilder ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = new StringBuilder ();

    // All sublines end with 0 or : except IF lines that are split into two
    int max = startPtr + length - 1;
    if (buffer[max] == 0)
      --max;

    if (isImpliedGoto () && !ApplesoftBasicProgram.basicPreferences.showThen)
      line.append ("GOTO ");

    for (int p = startPtr; p <= max; p++)
    {
      byte b = buffer[p];
      if (Utility.isHighBitSet (b))
      {
        if (line.length () > 0 && line.charAt (line.length () - 1) != ' ')
          line.append (' ');
        int val = b & 0x7F;
        if (val < ApplesoftConstants.tokens.length)
        {
          if (b != ApplesoftConstants.TOKEN_THEN
              || ApplesoftBasicProgram.basicPreferences.showThen)
            line.append (ApplesoftConstants.tokens[val]);
        }
      }
      else if (Utility.isControlCharacter (b))
        line.append (ApplesoftBasicProgram.basicPreferences.showCaret
            ? "^" + (char) (b + 64) : ".");
      else
        line.append ((char) b);
    }

    return line;
  }
}
