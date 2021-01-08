package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;;

// -----------------------------------------------------------------------------------//
public class SubLine implements ApplesoftConstants
// -----------------------------------------------------------------------------------//
{
  SourceLine parent;

  byte[] buffer;
  int startPtr;
  int length;

  String[] nextVariables;
  String forVariable = "";

  int equalsPosition;               // used for aligning the equals sign

  String functionArgument;
  String functionName;

  String callTarget;

  private final List<Integer> gotoLines = new ArrayList<> ();
  private final List<Integer> gosubLines = new ArrayList<> ();
  private final List<String> symbols = new ArrayList<> ();
  private final List<String> functions = new ArrayList<> ();
  private final List<String> arrays = new ArrayList<> ();
  private final List<Integer> constants = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  SubLine (SourceLine parent, int startPtr, int length)
  // ---------------------------------------------------------------------------------//
  {
    this.parent = parent;
    this.startPtr = startPtr;
    this.length = length;

    this.buffer = parent.buffer;
    byte firstByte = buffer[startPtr];

    if (Utility.isHighBitSet (firstByte))
    {
      doToken (firstByte);
      if (is (TOKEN_REM) || is (TOKEN_DATA) || is (TOKEN_CALL))
        return;
    }
    else if (Utility.isDigit (firstByte))
    {
      doDigit ();
      return;
    }
    else
      doAlpha ();

    int ptr = startPtr;
    String var = "";
    boolean inQuote = false;
    boolean inFunction = false;
    boolean inDefine = false;

    int max = startPtr + length - 1;
    if (buffer[max] == 0)
      --max;
    if (buffer[max] == Utility.ASCII_COLON)
      --max;

    while (ptr <= max)
    {
      byte b = buffer[ptr++];

      if (b == TOKEN_DEF)
      {
        inDefine = true;
        continue;
      }

      if (inDefine)         // ignore the name and argument
      {
        if (b == TOKEN_EQUALS)
          inDefine = false;

        continue;
      }

      if (inQuote && b != Utility.ASCII_QUOTE)
        continue;

      if (inFunction && b == Utility.ASCII_RIGHT_BRACKET)
      {
        inFunction = false;
        continue;
      }

      if (b == TOKEN_FN)
      {
        inFunction = true;
        continue;
      }

      if (Utility.isPossibleVariable (b))
        var += (char) b;
      else
      {
        if (inFunction)
          checkFunction (var, b);
        else
          checkVar (var, b);
        var = "";

        if (b == Utility.ASCII_QUOTE)
          inQuote = !inQuote;
      }
    }

    checkVar (var, (byte) 0);
  }

  // ---------------------------------------------------------------------------------//
  private void checkFunction (String var, byte terminator)
  // ---------------------------------------------------------------------------------//
  {
    assert terminator == Utility.ASCII_LEFT_BRACKET;

    if (!functions.contains (var))
      functions.add (var);
  }

  // ---------------------------------------------------------------------------------//
  private void checkVar (String var, byte terminator)
  // ---------------------------------------------------------------------------------//
  {
    if (var.length () == 0)
      return;

    if (!Utility.isLetter ((byte) var.charAt (0)))
    {
      if (is (TOKEN_GOTO) || is (TOKEN_GOSUB) || is (TOKEN_ON))
        return;

      int varInt = Integer.parseInt (var);
      if (!constants.contains (varInt))
        constants.add (varInt);
      return;
    }

    if (is (TOKEN_DEF) && (var.equals (functionName) || var.equals (functionArgument)))
      return;

    if (terminator == Utility.ASCII_LEFT_BRACKET)
    {
      if (!arrays.contains (var))
        arrays.add (var);
    }
    else if (!symbols.contains (var))
      symbols.add (var);
  }

  // ---------------------------------------------------------------------------------//
  List<String> getSymbols ()
  // ---------------------------------------------------------------------------------//
  {
    return symbols;
  }

  // ---------------------------------------------------------------------------------//
  List<String> getFunctions ()
  // ---------------------------------------------------------------------------------//
  {
    return functions;
  }

  // ---------------------------------------------------------------------------------//
  List<String> getArrays ()
  // ---------------------------------------------------------------------------------//
  {
    return arrays;
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
  List<Integer> getConstants ()
  // ---------------------------------------------------------------------------------//
  {
    return constants;
  }

  // ---------------------------------------------------------------------------------//
  private void doToken (byte b)
  // ---------------------------------------------------------------------------------//
  {
    switch (b)
    {
      case TOKEN_FOR:
        int p = startPtr + 1;
        while (buffer[p] != TOKEN_EQUALS)
          forVariable += (char) buffer[p++];
        break;

      case TOKEN_NEXT:
        if (length == 2)                // no variables
          nextVariables = new String[0];
        else
        {
          String varList = new String (buffer, startPtr + 1, length - 2);
          nextVariables = varList.split (",");
        }
        break;

      case TOKEN_LET:
        recordEqualsPosition ();
        break;

      case TOKEN_GOTO:
        int targetLine = getLineNumber (buffer, startPtr + 1);
        addXref (targetLine, gotoLines);
        break;

      case TOKEN_GOSUB:
        targetLine = getLineNumber (buffer, startPtr + 1);
        addXref (targetLine, gosubLines);
        break;

      case TOKEN_ON:
        p = startPtr + 1;
        int max = startPtr + length - 1;
        while (p < max && buffer[p] != ApplesoftConstants.TOKEN_GOTO
            && buffer[p] != ApplesoftConstants.TOKEN_GOSUB)
          p++;

        switch (buffer[p++])
        {
          case TOKEN_GOSUB:
            for (int destLine : getLineNumbers (buffer, p))
              addXref (destLine, gosubLines);
            break;

          case TOKEN_GOTO:
            for (int destLine : getLineNumbers (buffer, p))
              addXref (destLine, gotoLines);
            break;

          default:
            System.out.println ("GOTO / GOSUB not found");
        }
        break;

      case TOKEN_ONERR:
        if (buffer[startPtr + 1] == ApplesoftConstants.TOKEN_GOTO)
        {
          targetLine = getLineNumber (buffer, startPtr + 2);
          addXref (targetLine, gotoLines);
        }
        break;

      case TOKEN_CALL:
        byte[] lineBuffer = getBuffer ();

        if (lineBuffer[0] == TOKEN_MINUS)
          callTarget = "-" + new String (lineBuffer, 1, lineBuffer.length - 1);
        else
          callTarget = new String (lineBuffer, 0, lineBuffer.length);
        break;

      case TOKEN_DEF:
        lineBuffer = getBuffer ();
        assert lineBuffer[0] == TOKEN_FN;

        int leftBracket = getPosition (lineBuffer, 1, Utility.ASCII_LEFT_BRACKET);
        int rightBracket =
            getPosition (lineBuffer, leftBracket + 1, Utility.ASCII_RIGHT_BRACKET);

        functionName = new String (lineBuffer, 1, leftBracket - 1);
        functionArgument =
            new String (lineBuffer, leftBracket + 1, rightBracket - leftBracket - 1);
        functions.add (functionName);

        break;
    }
  }

  // ---------------------------------------------------------------------------------//
  private int getPosition (byte[] buffer, int start, byte value)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = start; i < buffer.length; i++)
      if (buffer[i] == value)
        return i;
    return -1;
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

    while (ptr < buffer.length && Utility.isDigit (buffer[ptr]))
      lineNumber = lineNumber * 10 + (buffer[ptr++] & 0xFF) - 0x30;

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
    while (buffer[p] != TOKEN_EQUALS && p < max)
      p++;
    if (buffer[p] == TOKEN_EQUALS)
      equalsPosition = toString ().indexOf ('=');           // use expanded line
  }

  // ---------------------------------------------------------------------------------//
  boolean isJoinableRem ()
  // ---------------------------------------------------------------------------------//
  {
    return is (TOKEN_REM) && !isFirst ();
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
  public String getAlignedText (int alignEqualsPos)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = toStringBuilder ();      // get line

    // insert spaces before '=' until it lines up with the other assignment lines
    if (!is (TOKEN_REM))
      while (alignEqualsPos-- > equalsPosition)
        line.insert (equalsPosition, ' ');

    return line.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    int len = length - 1;
    if (buffer[startPtr + len] == Utility.ASCII_COLON || buffer[startPtr + len] == 0)
      len--;
    byte[] buffer2 = new byte[len];
    System.arraycopy (buffer, startPtr + 1, buffer2, 0, buffer2.length);
    return buffer2;
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
          if (b != TOKEN_THEN || ApplesoftBasicProgram.basicPreferences.showThen)
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
