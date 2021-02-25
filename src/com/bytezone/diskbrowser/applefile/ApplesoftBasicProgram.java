package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.getIndent;
import static com.bytezone.diskbrowser.utilities.Utility.isDigit;
import static com.bytezone.diskbrowser.utilities.Utility.isHighBitSet;
import static com.bytezone.diskbrowser.utilities.Utility.isLetter;
import static com.bytezone.diskbrowser.utilities.Utility.isPossibleNumber;
import static com.bytezone.diskbrowser.utilities.Utility.unsignedShort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class ApplesoftBasicProgram extends BasicProgram implements ApplesoftConstants
// -----------------------------------------------------------------------------------//
{
  private static final String underline =
      "----------------------------------------------------"
          + "----------------------------------------------";
  private static final Pattern dimPattern =
      Pattern.compile ("[A-Z][A-Z0-9]*[$%]?\\([0-9]+(,[0-9]+)*\\)[,:]?");
  private static final String NEWLINE = "\n";

  private static final int LEFT_MARGIN = 5;
  private static final int RIGHT_MARGIN = 33;

  private final List<SourceLine> sourceLines = new ArrayList<> ();
  private final int endPtr;
  private final int longestVarName;

  private final Map<Integer, List<Integer>> gotoLines = new TreeMap<> ();
  private final Map<Integer, List<Integer>> gosubLines = new TreeMap<> ();
  private final Map<Integer, List<Integer>> constantsInt = new TreeMap<> ();
  private final Map<Float, List<Integer>> constantsFloat = new TreeMap<> ();

  private final Map<String, List<Integer>> callLines = new TreeMap<> ();
  private final Map<String, List<Integer>> symbolLines = new TreeMap<> ();
  private final Map<String, List<Integer>> functionLines = new TreeMap<> ();
  private final Map<String, List<Integer>> arrayLines = new TreeMap<> ();

  private final Map<String, List<String>> uniqueSymbols = new TreeMap<> ();
  private final Map<String, List<String>> uniqueArrays = new TreeMap<> ();

  private final List<Integer> stringsLine = new ArrayList<> ();
  private final List<String> stringsText = new ArrayList<> ();

  private final String formatLeft;
  private final String formatLineNumber;
  private final String formatRight;

  private final int maxDigits;

  // ---------------------------------------------------------------------------------//
  public ApplesoftBasicProgram (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    int ptr = 0;

    while (buffer[ptr + 1] != 0)    // msb of link field
    {
      SourceLine line = new SourceLine (this, buffer, ptr);
      sourceLines.add (line);
      checkXref (line);
      ptr += line.length;           // assumes lines are contiguous
    }

    endPtr = ptr;                   // record where the end-of-program marker is

    longestVarName = getLongestName ();
    maxDigits = getMaxDigits ();

    // build format strings based on existing line numbers and variable names
    formatLeft = longestVarName > 7 ? "%-" + longestVarName + "." + longestVarName + "s  "
        : "%-7.7s  ";
    formatRight = formatLeft.replace ("-", "");
    formatLineNumber = "%" + maxDigits + "d ";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    if (basicPreferences.showHeader)
      addHeader (text);

    if (showDebugText)
      return getDebugText (text);

    if (sourceLines.size () == 0)
    {
      text.append ("\n\nThis page intentionally left blank");
      return text.toString ();
    }

    if (basicPreferences.formatApplesoft)
      getUserFormat (text);
    else
      getAppleFormat (text);

    if (basicPreferences.showAllXref)
      addXref (text);

    return Utility.rtrim (text);
  }

  // ---------------------------------------------------------------------------------//
  private void getAppleFormat (StringBuilder fullText)
  // ---------------------------------------------------------------------------------//
  {
    int loadAddress = getLoadAddress ();
    int ptr = 0;
    int linkField;

    StringBuilder currentLine = new StringBuilder ();

    while ((linkField = unsignedShort (buffer, ptr)) != 0)
    {
      int lineNumber = unsignedShort (buffer, ptr + 2);
      currentLine.append (String.format (" %d ", lineNumber));
      ptr += 4;

      if (basicPreferences.appleLineWrap)
        ptr = appendWithWrap (currentLine, ptr);
      else
        ptr = appendWithOutWrap (currentLine, ptr);

      if (ptr != (linkField - loadAddress))
      {
        System.out.printf ("%s: ptr: %04X, nextLine: %04X%n", name, ptr + loadAddress,
            linkField);
        //        ptr = linkField - loadAddress;      // use this when tested
      }

      currentLine.append (NEWLINE);

      fullText.append (currentLine);
      currentLine.setLength (0);
    }
  }

  // ---------------------------------------------------------------------------------//
  private int appendWithOutWrap (StringBuilder currentLine, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    byte b;

    while ((b = buffer[ptr++]) != 0)
      if (isHighBitSet (b))
      {
        String token = String.format (" %s ", ApplesoftConstants.tokens[b & 0x7F]);
        currentLine.append (token);
      }
      else
        switch (b)
        {
          case Utility.ASCII_CR:
            currentLine.append (NEWLINE);
            break;

          case Utility.ASCII_BACKSPACE:
            if (currentLine.length () > 0)
              currentLine.deleteCharAt (currentLine.length () - 1);
            break;

          case Utility.ASCII_LF:
            int indent = getIndent (currentLine);
            currentLine.append ("\n");
            for (int i = 0; i < indent; i++)
              currentLine.append (" ");
            break;

          default:
            currentLine.append ((char) b);
        }

    return ptr;
  }

  // ---------------------------------------------------------------------------------//
  private int appendWithWrap (StringBuilder currentLine, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    byte b;
    int cursor = currentLine.length ();       // controls when to wrap

    while ((b = buffer[ptr++]) != 0)
      if (isHighBitSet (b))
      {
        String token = String.format (" %s ", ApplesoftConstants.tokens[b & 0x7F]);
        currentLine.append (token);
        cursor = incrementCursor (currentLine, cursor, token.length ());
      }
      else
        switch (b)
        {
          case Utility.ASCII_CR:
            currentLine.append (NEWLINE);
            cursor = 0;
            break;

          case Utility.ASCII_BACKSPACE:
            if (cursor > 0)
            {
              currentLine.deleteCharAt (currentLine.length () - 1);
              --cursor;
            }
            break;

          case Utility.ASCII_LF:
            currentLine.append ("\n");
            for (int i = 0; i < cursor; i++)
              currentLine.append (" ");
            break;

          default:
            currentLine.append ((char) b);
            cursor = incrementCursor (currentLine, cursor, 1);
        }

    return ptr;
  }

  // ---------------------------------------------------------------------------------//
  private int incrementCursor (StringBuilder currentLine, int cursor, int size)
  // ---------------------------------------------------------------------------------//
  {
    assert size <= 9;           // longest token possible (7 plus 2 spaces)
    cursor += size;

    if ((cursor) >= RIGHT_MARGIN)
    {
      cursor = cursor >= 40 ? cursor - 40 : LEFT_MARGIN;
      currentLine.append ("\n     ".substring (0, cursor + 1));
    }

    return cursor;
  }

  // ---------------------------------------------------------------------------------//
  private void getUserFormat (StringBuilder fullText)
  // ---------------------------------------------------------------------------------//
  {
    int indentSize = 2;
    boolean insertBlankLine = false;

    Stack<String> loopVariables = new Stack<> ();

    int alignEqualsPos = 0;
    int baseOffset = 7;       // 5 digit line number + 2 spaces

    for (SourceLine line : sourceLines)
    {
      StringBuilder text = new StringBuilder (String.format ("%5d", (line.lineNumber)));

      int indent = loopVariables.size ();   // each full line starts at the loop indent
      int ifIndent = 0;                     // IF statement(s) limit back indentation by NEXT

      for (SubLine subline : line.sublines)
      {
        // Allow empty statements (caused by a single colon)
        if (subline.isEmpty ())
          continue;

        // A REM statement might conceal an assembler routine
        // - see P.CREATE on Diags2E.DSK
        if (subline.is (TOKEN_REM) && subline.containsToken ())
        {
          int address = getLoadAddress () + subline.startPtr + 1;  // skip the REM token
          fullText.append (text + String.format ("REM - Inline assembler @ $%02X (%d)%n",
              address, address));
          String padding = "                         ".substring (0, text.length () + 2);
          for (String asm : getRemAssembler (subline))
            fullText.append (padding + asm + NEWLINE);
          continue;
        }

        // Beagle Bros often have multiline REM statements
        if (subline.is (TOKEN_REM) && basicPreferences.formatRem
            && subline.containsControlChars ())
        {
          subline.addFormattedRem (text);
          fullText.append (text + NEWLINE);
          continue;
        }

        // Reduce the indent by each NEXT, but only as far as the IF indent allows
        if (subline.is (TOKEN_NEXT))
        {
          popLoopVariables (loopVariables, subline);
          indent = Math.max (ifIndent, loopVariables.size ());
        }

        // Are we joining REM lines with the previous subline?
        if (!basicPreferences.splitRem && subline.isJoinableRem ())
        {
          // Join this REM statement to the previous line, so no indenting
          fullText.deleteCharAt (fullText.length () - 1);         // remove newline
          fullText.append (" ");
        }
        else    // ... otherwise do all the indenting
        {
          // Align assign statements if required
          if (basicPreferences.alignAssign)
            alignEqualsPos = alignEqualsPosition (subline, alignEqualsPos);

          int column = indent * indentSize + baseOffset;
          while (text.length () < column)
            text.append (" ");
        }

        // Add the current text, then reset it
        String lineText = subline.getAlignedText (alignEqualsPos);

        if (subline.is (TOKEN_DATA) && basicPreferences.deleteExtraDataSpace)
          lineText = lineText.replaceFirst ("DATA  ", "DATA ");

        // Check for a wrappable REM/DATA/DIM statement
        // (see SEA BATTLE on DISK283.DSK)
        int inset = Math.max (text.length (), getIndent (fullText)) + 1;
        if (subline.is (TOKEN_REM) && lineText.length () > basicPreferences.wrapRemAt)
        {
          List<String> lines = splitLine (lineText, basicPreferences.wrapRemAt, ' ');
          addSplitLines (lines, text, inset);
        }
        else if (subline.is (TOKEN_DATA)
            && lineText.length () > basicPreferences.wrapDataAt)
        {
          List<String> lines = splitLine (lineText, basicPreferences.wrapDataAt, ',');
          addSplitLines (lines, text, inset);
        }
        else if (subline.is (TOKEN_DIM) && basicPreferences.splitDim)
        {
          List<String> lines = splitDim (lineText);
          addSplitLines (lines, text, inset);
        }
        else
          text.append (lineText);

        fullText.append (text);
        fullText.append (NEWLINE);
        text.setLength (0);

        // Calculate indent changes that take effect after the current subline
        if (subline.is (TOKEN_IF))
          ifIndent = ++indent;
        else if (subline.is (TOKEN_FOR))
        {
          String latestLoopVar = loopVariables.size () > 0 ? loopVariables.peek () : "";
          if (!subline.forVariable.equals (latestLoopVar))    // don't add repeated loop
          {
            loopVariables.push (subline.forVariable);
            ++indent;
          }
        }
        else if (basicPreferences.blankAfterReturn && subline.is (TOKEN_RETURN)
            && subline.isFirst ())
          insertBlankLine = true;
      }

      if (insertBlankLine)
      {
        fullText.append (NEWLINE);
        insertBlankLine = false;
      }

      // Reset alignment value if we just left an IF 
      //     - the indentation will be different now
      if (ifIndent > 0)
        alignEqualsPos = 0;
    }
  }

  // ---------------------------------------------------------------------------------//
  private void addXref (StringBuilder fullText)
  // ---------------------------------------------------------------------------------//
  {
    if (basicPreferences.showSymbols)
    {
      if (!symbolLines.isEmpty ())
        showSymbolsLeft (fullText, symbolLines, "Var");

      if (!arrayLines.isEmpty ())
        showSymbolsLeft (fullText, arrayLines, "Array");
    }

    if (basicPreferences.showDuplicateSymbols)
    {
      if (!uniqueSymbols.isEmpty ())
        showDuplicates (fullText, uniqueSymbols, "Var");

      if (!uniqueArrays.isEmpty ())
        showDuplicates (fullText, uniqueArrays, "Array");
    }

    if (basicPreferences.showFunctions && !functionLines.isEmpty ())
      showSymbolsLeft (fullText, functionLines, "Fnction");

    if (basicPreferences.showConstants)
    {
      if (!constantsInt.isEmpty ())
        showSymbolsRightInt (fullText, constantsInt, "Integer");

      if (!constantsFloat.isEmpty ())
        showSymbolsRightFloat (fullText, constantsFloat, "Float");

      if (stringsLine.size () > 0)
      {
        heading (fullText, formatRight, "Line", "String");
        for (int i = 0; i < stringsLine.size (); i++)
          fullText.append (String.format (formatRight + "%s%n", stringsLine.get (i),
              stringsText.get (i)));
      }
    }

    if (basicPreferences.showXref)
    {
      if (!gosubLines.isEmpty ())
        showSymbolsRight (fullText, gosubLines, "GOSUB");

      if (!gotoLines.isEmpty ())
        showSymbolsRight (fullText, gotoLines, "GOTO");
    }

    if (basicPreferences.showCalls && !callLines.isEmpty ())
      showSymbolsLeftRight (fullText, callLines, "   CALL");
  }

  // ---------------------------------------------------------------------------------//
  private int getMaxDigits ()
  // ---------------------------------------------------------------------------------//
  {
    if (sourceLines.size () == 0)
      return 4;                           // anything non-zero

    SourceLine lastLine = sourceLines.get (sourceLines.size () - 1);
    return (lastLine.lineNumber + "").length ();
  }

  // ---------------------------------------------------------------------------------//
  private int getLongestName ()
  // ---------------------------------------------------------------------------------//
  {
    int longestName = getLongestName (symbolLines, 0);
    longestName = getLongestName (arrayLines, longestName);
    longestName = getLongestName (functionLines, longestName);

    return longestName;
  }

  // ---------------------------------------------------------------------------------//
  private void heading (StringBuilder fullText, String format, String... heading)
  // ---------------------------------------------------------------------------------//
  {
    if (fullText.charAt (fullText.length () - 2) != '\n')
      fullText.append (NEWLINE);

    fullText.append (String.format (format, underline));
    fullText.append (underline);
    fullText.append (NEWLINE);

    fullText.append (String.format (format, heading[0]));
    if (heading.length == 1)
      fullText.append ("Line numbers");
    else
      fullText.append (heading[1]);

    fullText.append (NEWLINE);
    fullText.append (String.format (format, underline));
    fullText.append (underline);
    fullText.append (NEWLINE);
  }

  // ---------------------------------------------------------------------------------//
  private void showDuplicates (StringBuilder fullText, Map<String, List<String>> map,
      String heading)
  // ---------------------------------------------------------------------------------//
  {
    boolean headingShown = false;
    for (String key : map.keySet ())
    {
      List<String> usage = map.get (key);
      if (usage.size () > 1)
      {
        if (!headingShown)
        {
          headingShown = true;
          heading (fullText, formatLeft, heading, "Duplicate Names");
        }

        String line = usage.toString ();
        line = line.substring (1, line.length () - 1);
        fullText.append (String.format ("%-6s   %s%n", key, line));
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void showSymbolsLeft (StringBuilder fullText, Map<String, List<Integer>> map,
      String heading)
  // ---------------------------------------------------------------------------------//
  {
    heading (fullText, formatLeft, heading);

    for (String symbol : map.keySet ())                   // left-justify strings
    {
      if (symbol.length () <= 7)
        appendLineNumbers (fullText, String.format (formatLeft, symbol),
            map.get (symbol));
      else
        appendLineNumbers (fullText, symbol + " ", map.get (symbol));
    }
  }

  // ---------------------------------------------------------------------------------//
  private void showSymbolsLeftRight (StringBuilder fullText,
      Map<String, List<Integer>> map, String heading)
  // ---------------------------------------------------------------------------------//
  {
    heading (fullText, formatLeft, heading);

    for (String symbol : map.keySet ())                   // left-justify strings
    {
      if (isNumeric (symbol))
        appendLineNumbers (fullText, String.format (formatRight, symbol),
            map.get (symbol));
      else if (symbol.length () <= 7)
        appendLineNumbers (fullText, String.format (formatLeft, symbol),
            map.get (symbol));
      else
        appendLineNumbers (fullText, symbol + " ", map.get (symbol));
    }
  }

  // ---------------------------------------------------------------------------------//
  private boolean isNumeric (String value)
  // ---------------------------------------------------------------------------------//
  {
    byte[] bytes = value.getBytes ();
    int start = value.charAt (0) == Utility.ASCII_MINUS ? 1 : 0;
    for (int i = start; i < bytes.length; i++)
      if (!isPossibleNumber (bytes[i]))
        return false;
    return true;
  }

  // ---------------------------------------------------------------------------------//
  private void showSymbolsRight (StringBuilder fullText, Map<Integer, List<Integer>> map,
      String heading)
  // ---------------------------------------------------------------------------------//
  {
    heading (fullText, formatRight, heading);

    for (Integer symbol : map.keySet ())                  // right-justify integers
      appendLineNumbers (fullText, String.format (formatRight, symbol), map.get (symbol));
  }

  // ---------------------------------------------------------------------------------//
  private void showSymbolsRightInt (StringBuilder fullText,
      Map<Integer, List<Integer>> map, String heading)
  // ---------------------------------------------------------------------------------//
  {
    heading (fullText, formatRight, heading);

    for (int symbol : map.keySet ())                  // right-justify integers
      appendLineNumbers (fullText, String.format (formatRight, symbol), map.get (symbol));
  }

  // ---------------------------------------------------------------------------------//
  private void showSymbolsRightFloat (StringBuilder fullText,
      Map<Float, List<Integer>> map, String heading)
  // ---------------------------------------------------------------------------------//
  {
    heading (fullText, formatRight, heading);

    for (float symbol : map.keySet ())                  // right-justify integers
      appendLineNumbers (fullText, String.format (formatRight, symbol), map.get (symbol));
  }

  // ---------------------------------------------------------------------------------//
  private void appendLineNumbers (StringBuilder fullText, String symbol,
      List<Integer> lineNumbers)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    text.append (symbol);

    for (int lineNo : lineNumbers)
    {
      if (text.length () > underline.length () - maxDigits + longestVarName)
      {
        fullText.append (text);
        fullText.append (NEWLINE);
        text.setLength (0);
        text.append (String.format (formatRight, ""));
      }
      text.append (String.format (formatLineNumber, lineNo));
    }

    if (text.length () > longestVarName + 3)
      fullText.append (text + "\n");
  }

  // ---------------------------------------------------------------------------------//
  private int getLongestName (Map<String, List<Integer>> map, int longestName)
  // ---------------------------------------------------------------------------------//
  {
    for (String symbol : map.keySet ())
      if (symbol.length () > longestName)
        longestName = symbol.length ();

    return longestName;
  }

  // ---------------------------------------------------------------------------------//
  private void wrapPrint (StringBuilder fullText, StringBuilder text, String lineText)
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = splitPrint (lineText);
    if (lines != null)
    {
      int offset = text.indexOf ("PRINT");
      if (offset < 0)
        offset = text.indexOf ("INPUT");
      String fmt = "%-" + offset + "." + offset + "s%s%n";
      String padding = text.substring (0, offset);
      for (String s : lines)
      {
        fullText.append (String.format (fmt, padding, s));
        padding = "";
      }
    }
    else
      fullText.append (text + "\n");
  }

  // ---------------------------------------------------------------------------------//
  private int countChars (StringBuilder text, byte ch)
  // ---------------------------------------------------------------------------------//
  {
    int total = 0;
    for (int i = 0; i < text.length (); i++)
      if (text.charAt (i) == ch)
        total++;
    return total;
  }

  // ---------------------------------------------------------------------------------//
  private List<String> splitPrint (String line)
  // ---------------------------------------------------------------------------------//
  {
    int first = line.indexOf ("\"") + 1;
    int last = line.indexOf ("\"", first + 1) - 1;

    if (first != 7 || (last - first) <= basicPreferences.wrapPrintAt)
      return null;

    int charsLeft = last - first + 1;

    List<String> lines = new ArrayList<> ();
    String padding = line.substring (0, 7);
    line = line.substring (7);
    String sub;
    while (true)
    {
      if (line.length () >= basicPreferences.wrapPrintAt)
      {
        sub = line.substring (0, basicPreferences.wrapPrintAt);
        line = line.substring (basicPreferences.wrapPrintAt);
      }
      else
      {
        sub = line;
        line = "";
      }

      String subline = padding + sub;
      charsLeft -= basicPreferences.wrapPrintAt;

      if (charsLeft > 0)
        lines.add (subline);
      else
      {
        lines.add (subline + line);
        break;
      }
      padding = "       ";
    }

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  private List<String> splitLine (String line, int wrapLength, char breakChar)
  // ---------------------------------------------------------------------------------//
  {
    int spaceAt = 0;
    while (spaceAt < line.length () && line.charAt (spaceAt) != ' ')
      ++spaceAt;
    String indent = spaceAt < 8 ? "        ".substring (0, spaceAt + 1) : "        ";

    List<String> lines = new ArrayList<> ();

    while (line.length () > wrapLength)
    {
      int breakAt = wrapLength - 1;
      while (breakAt > spaceAt && line.charAt (breakAt) != breakChar)
        --breakAt;

      if (breakAt <= spaceAt)
        break;

      lines.add (line.substring (0, breakAt + 1));      // keep breakChar at end
      line = indent + line.substring (breakAt + 1);
    }

    while (line.length () > wrapLength)                 // no breakChars found
    {
      lines.add (line.substring (0, wrapLength));
      line = indent + line.substring (wrapLength);
    }

    lines.add (line);
    return lines;
  }

  // ---------------------------------------------------------------------------------//
  private List<String> splitDim (String line)
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();

    Matcher m = dimPattern.matcher (line);

    while (m.find ())
      lines.add ("    " + m.group ());

    if (lines.size () > 0)
      lines.set (0, "DIM " + lines.get (0).trim ());

    return lines;
  }

  // ---------------------------------------------------------------------------------//
  private void addSplitLines (List<String> lines, StringBuilder text, int indent)
  // ---------------------------------------------------------------------------------//
  {
    boolean first = true;

    for (String line : lines)
    {
      if (first)
      {
        first = false;
        text.append (line);
      }
      else
        text.append (
            "\n                                           ".substring (0, indent) + line);
    }
  }

  // Decide whether the current subline needs to be aligned on its equals sign. If so,
  // and the column hasn't been calculated, read ahead to find the highest position.
  // ---------------------------------------------------------------------------------//
  private int alignEqualsPosition (SubLine subline, int currentAlignPosition)
  // ---------------------------------------------------------------------------------//
  {
    if (subline.equalsPosition == 0)             // if the line has no equals sign
      return 0;                                  // reset it

    if (currentAlignPosition == 0)
      currentAlignPosition = findHighest (subline);     // examine following sublines

    return currentAlignPosition;
  }

  // The IF processing is so that any assignment that is being aligned doesn't continue
  // to the next full line (because the indentation has changed).
  // ---------------------------------------------------------------------------------//
  private int findHighest (SubLine startSubline)
  // ---------------------------------------------------------------------------------//
  {
    boolean started = false;
    int highestAssign = startSubline.equalsPosition;

    outerLoop: for (int i = sourceLines.indexOf (startSubline.parent); i < sourceLines
        .size (); i++)
    {
      boolean precededByIf = false;
      for (SubLine subline : sourceLines.get (i).sublines)
      {
        if (started)
        {
          // Stop when we come to a subline without an equals sign (joinable REMs
          // can be ignored)
          if (subline.equalsPosition == 0 && !joinableRem (subline))
            break outerLoop;

          if (subline.equalsPosition > highestAssign)
            highestAssign = subline.equalsPosition;
        }
        else if (subline == startSubline)
          started = true;
        else if (subline.is (TOKEN_IF))
          precededByIf = true;
      }

      if (started && precededByIf)     // sublines of IF have now finished
        break;                         // don't continue with following SourceLine
    }

    return highestAssign;
  }

  // ---------------------------------------------------------------------------------//
  private boolean joinableRem (SubLine subline)
  // ---------------------------------------------------------------------------------//
  {
    return subline.isJoinableRem () && !basicPreferences.splitRem;
  }

  // ---------------------------------------------------------------------------------//
  private String getDebugText (StringBuilder text)
  // ---------------------------------------------------------------------------------//
  {
    int linkField = unsignedShort (buffer, 0);
    int programLoadAddress = linkField - getLineLength (0);

    for (SourceLine sourceLine : sourceLines)
    {
      text.append (String.format ("%5d            %s%n", sourceLine.lineNumber,
          HexFormatter.formatNoHeader (buffer, sourceLine.linePtr, 4,
              programLoadAddress + sourceLine.linePtr)));
      for (SubLine subline : sourceLine.sublines)
      {
        String token = getDisplayToken (buffer[subline.startPtr]);
        String formattedHex = HexFormatter.formatNoHeader (buffer, subline.startPtr,
            subline.length, programLoadAddress + subline.startPtr);

        for (String bytes : formattedHex.split (NEWLINE))
        {
          text.append (String.format ("        %-8s %s%n", token, bytes));
          token = "";
        }
      }
      text.append (NEWLINE);
    }

    // check for assembler routines after the basic code
    if (endPtr < buffer.length)
    {
      int length = buffer.length - endPtr;
      int ptr = endPtr;

      if (length >= 2)
      {
        text.append ("                 ");
        text.append (
            HexFormatter.formatNoHeader (buffer, endPtr, 2, programLoadAddress + ptr));
        text.append ("\n\n");
        ptr += 2;
        length -= 2;
      }

      if (length > 0)
      {
        // show the extra bytes as a hex dump
        String formattedHex = HexFormatter.formatNoHeader (buffer, ptr,
            buffer.length - ptr, programLoadAddress + ptr);
        for (String bytes : formattedHex.split (NEWLINE))
          text.append (String.format ("                 %s%n", bytes));
      }

      if (length > 1)
      {
        // show the extra bytes as a disassembly
        byte[] extraBuffer = new byte[length];
        System.arraycopy (buffer, ptr, extraBuffer, 0, extraBuffer.length);
        AssemblerProgram assemblerProgram =
            new AssemblerProgram ("extra", extraBuffer, programLoadAddress + ptr);
        text.append ("\n");
        text.append (assemblerProgram.getText ());
      }
    }

    return Utility.rtrim (text);
  }

  // ---------------------------------------------------------------------------------//
  private String getDisplayToken (byte b)
  // ---------------------------------------------------------------------------------//
  {
    if (isHighBitSet (b))
      return ApplesoftConstants.tokens[b & 0x7F];

    if (isDigit (b) || isLetter (b))
      return "";

    return "*******";
  }

  // A REM statement might conceal an assembler routine
  // ---------------------------------------------------------------------------------//
  private String[] getRemAssembler (SubLine subline)
  // ---------------------------------------------------------------------------------//
  {
    AssemblerProgram program = new AssemblerProgram ("REM assembler",
        subline.getBuffer (), getLoadAddress () + subline.startPtr + 1);

    return program.getAssembler ().split ("\n");
  }

  // ---------------------------------------------------------------------------------//
  private void addHeader (StringBuilder pgm)
  // ---------------------------------------------------------------------------------//
  {
    pgm.append ("Name    : " + name + "\n");
    pgm.append (String.format ("Length  : $%04X (%<,d)%n", buffer.length));
    pgm.append (String.format ("Load at : $%04X (%<,d)%n%n", getLoadAddress ()));
  }

  // ---------------------------------------------------------------------------------//
  private int getLoadAddress ()
  // ---------------------------------------------------------------------------------//
  {
    return (buffer.length > 1) ? unsignedShort (buffer, 0) - getLineLength (0) : 0;
  }

  // ---------------------------------------------------------------------------------//
  private int getLineLength (int ptr)
  // ---------------------------------------------------------------------------------//
  {
    int linkField = unsignedShort (buffer, ptr);
    if (linkField == 0)
      return 2;

    ptr += 4;               // skip link field and line number
    int length = 5;

    while (ptr < buffer.length && buffer[ptr++] != 0)
      length++;

    //    System.out.printf ("Length: %4d, Ptr: %4d%n", length, ptr);
    assert length == ptr;
    return length;
  }

  // ---------------------------------------------------------------------------------//
  private void popLoopVariables (Stack<String> loopVariables, SubLine subline)
  // ---------------------------------------------------------------------------------//
  {
    if (subline.nextVariables.length == 0)                    // naked NEXT
    {
      if (loopVariables.size () > 0)
        loopVariables.pop ();
    }
    else
      for (String variable : subline.nextVariables)           // e.g. NEXT X,Y,Z
        while (loopVariables.size () > 0)
          if (sameVariable (variable, loopVariables.pop ()))
            break;
  }

  // ---------------------------------------------------------------------------------//
  private boolean sameVariable (String v1, String v2)
  // ---------------------------------------------------------------------------------//
  {
    return getUniqueName (v1).equals (getUniqueName (v2));
  }

  // ---------------------------------------------------------------------------------//
  private void checkUniqueName (String symbol, Map<String, List<String>> map)
  // ---------------------------------------------------------------------------------//
  {
    String uniqueName = getUniqueName (symbol);

    List<String> usage = map.get (uniqueName);
    if (usage == null)
    {
      usage = new ArrayList<> ();
      map.put (uniqueName, usage);
    }

    if (!usage.contains (symbol))
      usage.add (symbol);
  }

  // ---------------------------------------------------------------------------------//
  private String getUniqueName (String symbol)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = symbol.length () - 1;

    if (symbol.charAt (ptr) == Utility.ASCII_DOLLAR             // string
        || symbol.charAt (ptr) == Utility.ASCII_PERCENT)        // integer
      ptr--;

    return (ptr <= 1) ? symbol : symbol.substring (0, 2) + symbol.substring (ptr + 1);
  }

  // ---------------------------------------------------------------------------------//
  private void checkXref (SourceLine line)
  // ---------------------------------------------------------------------------------//
  {
    for (SubLine subline : line.sublines)
    {
      for (String symbol : subline.getVariables ())
        checkVar (symbol, line.lineNumber, symbolLines, uniqueSymbols);
      for (String symbol : subline.getArrays ())
        checkVar (symbol, line.lineNumber, arrayLines, uniqueArrays);
      for (String symbol : subline.getFunctions ())
        checkFunction (line.lineNumber, symbol);
      for (int targetLine : subline.getGosubLines ())
        addNumberInt (line.lineNumber, targetLine, gosubLines);
      for (int targetLine : subline.getGotoLines ())
        addNumberInt (line.lineNumber, targetLine, gotoLines);
      for (int num : subline.getConstantsInt ())
        addNumberInt (line.lineNumber, num, constantsInt);
      for (float num : subline.getConstantsFloat ())
        addNumberFloat (line.lineNumber, num, constantsFloat);
      if (subline.callTarget != null)
        addString (line.lineNumber, subline.callTarget, callLines);
      for (String s : subline.getStringsText ())
      {
        stringsText.add (s);
        stringsLine.add (line.lineNumber);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void checkVar (String var, int lineNumber, Map<String, List<Integer>> map,
      Map<String, List<String>> unique)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lines = map.get (var);
    if (lines == null)
    {
      lines = new ArrayList<> ();
      map.put (var, lines);
    }

    if (lines.size () == 0)
      lines.add (lineNumber);
    else
    {
      int lastLine = lines.get (lines.size () - 1);
      if (lastLine != lineNumber)
        lines.add (lineNumber);
    }

    checkUniqueName (var, unique);
  }

  // ---------------------------------------------------------------------------------//
  private void checkFunction (int sourceLine, String var)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lines = functionLines.get (var);
    if (lines == null)
    {
      lines = new ArrayList<> ();
      functionLines.put (var, lines);
    }

    addLine (lines, sourceLine);
  }

  // ---------------------------------------------------------------------------------//
  private void addNumberInt (int sourceLine, Integer key, Map<Integer, List<Integer>> map)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lines = map.get (key);
    if (lines == null)
    {
      lines = new ArrayList<> ();
      map.put (key, lines);
    }

    addLine (lines, sourceLine);
  }

  // ---------------------------------------------------------------------------------//
  private void addNumberFloat (int sourceLine, Float key, Map<Float, List<Integer>> map)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lines = map.get (key);
    if (lines == null)
    {
      lines = new ArrayList<> ();
      map.put (key, lines);
    }

    addLine (lines, sourceLine);
  }

  // ---------------------------------------------------------------------------------//
  private void addString (int sourceLine, String key, Map<String, List<Integer>> map)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lines = map.get (key);
    if (lines == null)
    {
      lines = new ArrayList<> ();
      map.put (key, lines);
    }

    addLine (lines, sourceLine);
  }

  // ---------------------------------------------------------------------------------//
  private void addLine (List<Integer> lines, int lineNumber)
  // ---------------------------------------------------------------------------------//
  {
    if (lines.size () == 0)
      lines.add (lineNumber);
    else
    {
      int lastLine = lines.get (lines.size () - 1);
      if (lastLine != lineNumber)
        lines.add (lineNumber);
    }
  }
}