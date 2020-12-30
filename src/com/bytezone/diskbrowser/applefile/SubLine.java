package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
  String onExpression = "";
  int assignEqualPos;               // used for aligning the equals sign
  byte[] buffer;

  private final Map<Integer, List<Integer>> gotoLines;
  private final Map<Integer, List<Integer>> gosubLines;
  private final Map<String, List<Integer>> symbolLines;
  private final Map<String, List<String>> uniqueSymbols;

  // ---------------------------------------------------------------------------------//
  SubLine (SourceLine parent, int startPtr, int length)
  // ---------------------------------------------------------------------------------//
  {
    this.parent = parent;
    this.startPtr = startPtr;
    this.length = length;

    program = parent.parent;
    this.gotoLines = program.gotoLines;
    this.gosubLines = program.gosubLines;
    this.symbolLines = program.symbolLines;
    this.uniqueSymbols = program.uniqueSymbols;

    this.buffer = parent.buffer;
    byte firstByte = parent.buffer[startPtr];

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
      byte b = parent.buffer[ptr++];

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
  private void checkVar (String var, byte term)
  // ---------------------------------------------------------------------------------//
  {
    if (var.length () == 0)
      return;

    if (term == Utility.ASCII_LEFT_BRACKET)
      var += "(";

    if (Utility.isLetter ((byte) var.charAt (0)))
    {
      List<Integer> lines = symbolLines.get (var);
      if (lines == null)
      {
        lines = new ArrayList<> ();
        symbolLines.put (var, lines);
      }
      if (lines.size () == 0)
        lines.add (parent.lineNumber);
      else
      {
        int lastLine = lines.get (lines.size () - 1);
        if (lastLine != parent.lineNumber)
          lines.add (parent.lineNumber);
      }
      checkUniqueName (var);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void doToken (byte b)
  // ---------------------------------------------------------------------------------//
  {
    switch (b)
    {
      case ApplesoftConstants.TOKEN_FOR:
        int p = startPtr + 1;
        while (parent.buffer[p] != ApplesoftConstants.TOKEN_EQUALS)
          forVariable += (char) parent.buffer[p++];
        break;

      case ApplesoftConstants.TOKEN_NEXT:
        if (length == 2)                // no variables
          nextVariables = new String[0];
        else
        {
          String varList = new String (parent.buffer, startPtr + 1, length - 2);
          nextVariables = varList.split (",");
        }
        break;

      case ApplesoftConstants.TOKEN_LET:
        recordEqualsPosition ();
        break;

      case ApplesoftConstants.TOKEN_GOTO:
        int targetLine = getLineNumber (parent.buffer, startPtr + 1);
        addXref (targetLine, gotoLines);
        break;

      case ApplesoftConstants.TOKEN_GOSUB:
        targetLine = getLineNumber (parent.buffer, startPtr + 1);
        addXref (targetLine, gosubLines);
        break;

      case ApplesoftConstants.TOKEN_ON:
        p = startPtr + 1;
        int max = startPtr + length - 1;
        while (p < max && parent.buffer[p] != ApplesoftConstants.TOKEN_GOTO
            && parent.buffer[p] != ApplesoftConstants.TOKEN_GOSUB)
        {
          if (Utility.isHighBitSet (parent.buffer[p]))
          {
            int val = parent.buffer[p] & 0x7F;
            if (val < ApplesoftConstants.tokens.length)
              onExpression += " " + ApplesoftConstants.tokens[val];
          }
          else
            onExpression += (char) (parent.buffer[p]);
          p++;
        }

        switch (parent.buffer[p++])
        {
          case ApplesoftConstants.TOKEN_GOSUB:
            for (int destLine : getLineNumbers (parent.buffer, p))
              addXref (destLine, gosubLines);
            break;

          case ApplesoftConstants.TOKEN_GOTO:
            for (int destLine : getLineNumbers (parent.buffer, p))
              addXref (destLine, gotoLines);
            break;

          default:
            System.out.println ("GOTO / GOSUB not found");
        }
        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  private void checkUniqueName (String symbol)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = symbol.length () - 1;
    if (symbol.charAt (ptr) == Utility.ASCII_LEFT_BRACKET)      // array
      ptr--;
    if (symbol.charAt (ptr) == Utility.ASCII_DOLLAR
        || symbol.charAt (ptr) == Utility.ASCII_PERCENT)
      ptr--;

    String unique =
        (ptr <= 1) ? symbol : symbol.substring (0, 2) + symbol.substring (ptr + 1);

    List<String> usage = uniqueSymbols.get (unique);
    if (usage == null)
    {
      usage = new ArrayList<> ();
      uniqueSymbols.put (unique, usage);
    }

    if (!usage.contains (symbol))
      usage.add (symbol);
  }

  // ---------------------------------------------------------------------------------//
  private void doDigit ()
  // ---------------------------------------------------------------------------------//
  {
    int targetLine = getLineNumber (parent.buffer, startPtr);
    addXref (targetLine, gotoLines);
  }

  // ---------------------------------------------------------------------------------//
  private void doAlpha ()
  // ---------------------------------------------------------------------------------//
  {
    recordEqualsPosition ();
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
  private void addXref (int targetLine, Map<Integer, List<Integer>> map)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lines = map.get (targetLine);
    if (lines == null)
    {
      lines = new ArrayList<> ();
      map.put (targetLine, lines);
    }
    lines.add (parent.lineNumber);
  }

  // ---------------------------------------------------------------------------------//
  boolean isImpliedGoto ()
  // ---------------------------------------------------------------------------------//
  {
    byte b = parent.buffer[startPtr];
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
    while (parent.buffer[p] != ApplesoftConstants.TOKEN_EQUALS && p < max)
      p++;
    if (buffer[p] == ApplesoftConstants.TOKEN_EQUALS)
      assignEqualPos = toString ().indexOf ('=');           // use expanded line
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
    return parent.buffer[startPtr] == token;
  }

  // ---------------------------------------------------------------------------------//
  boolean has (byte token)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = startPtr + 1;
    int max = startPtr + length;
    while (ptr < max)
    {
      if (parent.buffer[ptr++] == token)
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
      int c = buffer[ptr] & 0xFF;
      //        System.out.printf ("%02X  %s%n", c, (char) c);
      if (c == 0x08 && text.length () > 0)
        text.deleteCharAt (text.length () - 1);
      else if (c == 0x0D)
        text.append ("\n");
      else
        text.append ((char) c);
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
  public String getAlignedText (int alignPosition)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = toStringBuilder ();

    while (alignPosition-- > assignEqualPos)
      line.insert (assignEqualPos, ' ');

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
            ? "^" + (char) (b + 64) : "");
      else
        line.append ((char) b);
    }

    return line;
  }
}
