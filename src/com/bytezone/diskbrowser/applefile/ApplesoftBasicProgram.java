package com.bytezone.diskbrowser.applefile;

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
  static final String underline = "----------------------------------------------------"
      + "----------------------------------------------";

  private final List<SourceLine> sourceLines = new ArrayList<> ();
  private final int endPtr;
  private final int longestVarName;

  private final Map<Integer, List<Integer>> gotoLines = new TreeMap<> ();
  private final Map<Integer, List<Integer>> gosubLines = new TreeMap<> ();
  private final Map<Integer, List<Integer>> constants = new TreeMap<> ();

  private final Map<String, List<Integer>> callLines = new TreeMap<> ();
  private final Map<String, List<Integer>> symbolLines = new TreeMap<> ();
  private final Map<String, List<Integer>> functionLines = new TreeMap<> ();
  private final Map<String, List<Integer>> arrayLines = new TreeMap<> ();

  private final Map<String, List<String>> uniqueSymbols = new TreeMap<> ();
  private final Map<String, List<String>> uniqueArrays = new TreeMap<> ();

  final List<Integer> stringsLine = new ArrayList<> ();
  final List<String> stringsText = new ArrayList<> ();

  String formatLeft;
  String formatLineNumber;
  String formatRight;
  int maxDigits;

  // ---------------------------------------------------------------------------------//
  public ApplesoftBasicProgram (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    int ptr = 0;
    int currentAddress = 0;

    int max = buffer.length - 6;          // need at least 6 bytes to make a SourceLine
    while (ptr <= max)
    {
      int nextAddress = Utility.unsignedShort (buffer, ptr);
      if (nextAddress <= currentAddress)           // usually zero when finished
        break;

      SourceLine line = new SourceLine (this, buffer, ptr);
      sourceLines.add (line);
      ptr += line.length;
      currentAddress = nextAddress;

      for (SubLine subline : line.sublines)
      {
        for (String symbol : subline.getSymbols ())
          checkVar (symbol, line.lineNumber, symbolLines, uniqueSymbols);
        for (String symbol : subline.getArrays ())
          checkVar (symbol, line.lineNumber, arrayLines, uniqueArrays);
        for (String symbol : subline.getFunctions ())
          checkFunction (symbol, line.lineNumber);
        for (int targetLine : subline.getGosubLines ())
          addXref (line.lineNumber, targetLine, gosubLines);
        for (int targetLine : subline.getGotoLines ())
          addXref (line.lineNumber, targetLine, gotoLines);
        for (int targetLine : subline.getConstants ())
          addXref (line.lineNumber, targetLine, constants);
        if (subline.callTarget != null)
          addXref (line.lineNumber, subline.callTarget, callLines);
      }
    }
    endPtr = ptr;

    longestVarName = getLongestName ();
    formatLeft = longestVarName > 7 ? "%-" + longestVarName + "." + longestVarName + "s  "
        : "%-7.7s  ";
    formatRight = formatLeft.replace ("-", "");

    maxDigits = getMaxDigits ();
    formatLineNumber = "%" + maxDigits + "d ";
  }

  // ---------------------------------------------------------------------------------//
  void checkVar (String var, int lineNumber, Map<String, List<Integer>> map,
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
  void checkFunction (String var, int lineNumber)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lines = functionLines.get (var);
    if (lines == null)
    {
      lines = new ArrayList<> ();
      functionLines.put (var, lines);
    }

    if (lines.size () == 0)
      lines.add (lineNumber);
    else
    {
      int lastLine = lines.get (lines.size () - 1);
      if (lastLine != lineNumber)
        lines.add (lineNumber);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void addXref (int sourceLine, int targetLine, Map<Integer, List<Integer>> map)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lines = map.get (targetLine);
    if (lines == null)
    {
      lines = new ArrayList<> ();
      map.put (targetLine, lines);
    }
    lines.add (sourceLine);
  }

  // ---------------------------------------------------------------------------------//
  private void addXref (int sourceLine, String target, Map<String, List<Integer>> map)
  // ---------------------------------------------------------------------------------//
  {
    List<Integer> lines = map.get (target);
    if (lines == null)
    {
      lines = new ArrayList<> ();
      map.put (target, lines);
    }
    lines.add (sourceLine);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return showDebugText ? getHexText () : getProgramText ();
  }

  // ---------------------------------------------------------------------------------//
  private String getProgramText ()
  // ---------------------------------------------------------------------------------//
  {
    if (!basicPreferences.formatApplesoft)
      return originalFormat ();

    int indentSize = 2;
    boolean insertBlankLine = false;

    StringBuilder fullText = new StringBuilder ();
    Stack<String> loopVariables = new Stack<> ();

    if (basicPreferences.showHeader)
      addHeader (fullText);
    int alignEqualsPos = 0;
    StringBuilder text;
    int baseOffset = basicPreferences.showTargets ? 12 : 8;

    for (SourceLine line : sourceLines)
    {
      text = new StringBuilder (getBase (line) + "  ");

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
            fullText.append (padding + asm + "\n");
          continue;
        }

        // Beagle Bros often have multiline REM statements
        if (subline.is (TOKEN_REM) && subline.containsControlChars ())
        {
          subline.addFormattedRem (text);
          fullText.append (text + "\n");
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
        else    // ... otherwise do all the indenting and showing of targets etc.
        {
          // Prepare target indicators for subsequent sublines (ie no line number)
          if (basicPreferences.showTargets && !subline.isFirst ())
            if (subline.is (TOKEN_GOSUB)
                || (subline.is (TOKEN_ON) && subline.has (TOKEN_GOSUB)))
              text.append ("<<--");
            else if (subline.is (TOKEN_GOTO) || subline.isImpliedGoto ()
                || (subline.is (TOKEN_ON) && subline.has (TOKEN_GOTO)))
              text.append (" <--");

          // Align assign statements if required
          if (basicPreferences.alignAssign)
            alignEqualsPos = alignEqualsPosition (subline, alignEqualsPos);

          int column = indent * indentSize + baseOffset;
          while (text.length () < column)
            text.append (" ");
        }

        // Add the current text, then reset it
        String lineText = subline.getAlignedText (alignEqualsPos);

        if (subline.is (TOKEN_REM) && basicPreferences.deleteExtraRemSpace)
          lineText = lineText.replaceFirst ("REM  ", "REM ");

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

        // Check for a wrappable PRINT or INPUT statement
        // (see FROM MACHINE LANGUAGE TO BASIC on DOSToolkit2eB.dsk)
        if (basicPreferences.wrapPrintAt > 0
            && (subline.is (TOKEN_PRINT) || subline.is (TOKEN_INPUT))
            && countChars (text, Utility.ASCII_QUOTE) == 2      // just start and end quotes
            && countChars (text, Utility.ASCII_CARET) == 0)     // no control characters
          wrapPrint (fullText, text, lineText);
        else
          fullText.append (text + "\n");

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
        fullText.append ("\n");
        insertBlankLine = false;
      }

      // Reset alignment value if we just left an IF - the indentation will be different now
      if (ifIndent > 0)
        alignEqualsPos = 0;
    }

    int ptr = endPtr + 2;
    if (ptr < buffer.length - 1)    // sometimes there's an extra byte on the end
    {
      int offset = Utility.unsignedShort (buffer, 0);
      int programLoadAddress = offset - getLineLength (0);
      fullText.append ("\nExtra data:\n\n");
      fullText.append (HexFormatter.formatNoHeader (buffer, ptr, buffer.length - ptr,
          programLoadAddress + ptr));
      fullText.append ("\n");
    }

    if (basicPreferences.showSymbols && !symbolLines.isEmpty ())
      showSymbolsLeft (fullText, symbolLines, "Var");

    if (basicPreferences.showSymbols && !arrayLines.isEmpty ())
      showSymbolsLeft (fullText, arrayLines, "Array");

    if (basicPreferences.showDuplicateSymbols && !uniqueSymbols.isEmpty ())
      showDuplicates (fullText, uniqueSymbols, "Var");

    if (basicPreferences.showDuplicateSymbols && !uniqueArrays.isEmpty ())
      showDuplicates (fullText, uniqueArrays, "Array");

    if (basicPreferences.showFunctions && !functionLines.isEmpty ())
      showSymbolsLeft (fullText, functionLines, "Fnction");

    if (basicPreferences.showConstants && !constants.isEmpty ())
      showSymbolsRight (fullText, constants, "Literal");

    if (basicPreferences.listStrings && stringsLine.size () > 0)
    {
      heading (fullText, formatRight, "Line", "String");
      for (int i = 0; i < stringsLine.size (); i++)
        fullText.append (String.format (formatRight + "%s%n", stringsLine.get (i),
            stringsText.get (i)));
    }

    if (basicPreferences.showXref && !gosubLines.isEmpty ())
      showSymbolsRight (fullText, gosubLines, "GOSUB");

    if (basicPreferences.showXref && !gotoLines.isEmpty ())
      showSymbolsRight (fullText, gotoLines, "GOTO");

    if (basicPreferences.showCalls && !callLines.isEmpty ())
      showSymbolsLeft (fullText, callLines, "CALL");

    if (fullText.length () > 0)
      while (fullText.charAt (fullText.length () - 1) == '\n')
        fullText.deleteCharAt (fullText.length () - 1);     // remove trailing newlines

    return fullText.toString ();
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
  private void heading (StringBuilder fullText, String format, String... heading)
  // ---------------------------------------------------------------------------------//
  {
    if (fullText.charAt (fullText.length () - 2) != '\n')
      fullText.append ("\n");

    fullText.append (String.format (format, underline));
    fullText.append (underline);
    fullText.append ("\n");

    fullText.append (String.format (format, heading[0]));
    if (heading.length == 1)
      fullText.append ("Line numbers");
    else
      fullText.append (heading[1]);

    fullText.append ("\n");
    fullText.append (String.format (format, underline));
    fullText.append (underline);
    fullText.append ("\n");
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
      appendLineNumbers (fullText, String.format (formatLeft, symbol), map.get (symbol));
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
        fullText.append ("\n");
        text.setLength (0);
        text.append (String.format (formatRight, ""));
      }
      text.append (String.format (formatLineNumber, lineNo));
    }

    if (text.length () > longestVarName + 3)
      fullText.append (text + "\n");
  }

  // ---------------------------------------------------------------------------------//
  private int getLongestName ()
  // ---------------------------------------------------------------------------------//
  {
    int longestName = 0;

    longestName = getLongestName (symbolLines, longestName);
    longestName = getLongestName (arrayLines, longestName);
    longestName = getLongestName (functionLines, longestName);

    return longestName;
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
    if (true)       // new method
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
    else            // old method
    {
      int first = text.indexOf ("\"") + 1;
      int last = text.indexOf ("\"", first + 1) - 1;
      if ((last - first) > basicPreferences.wrapPrintAt)
      {
        int ptr = first + basicPreferences.wrapPrintAt;
        do
        {
          fullText.append (text.substring (0, ptr)
              + "\n                                 ".substring (0, first + 1));
          text.delete (0, ptr);
          ptr = basicPreferences.wrapPrintAt;
        } while (text.length () > basicPreferences.wrapPrintAt);
      }
      fullText.append (text + "\n");
    }
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
    int firstSpace = 0;
    while (firstSpace < line.length () && line.charAt (firstSpace) != ' ')
      ++firstSpace;

    List<String> lines = new ArrayList<> ();
    while (line.length () > wrapLength)
    {
      int max = Math.min (wrapLength, line.length () - 1);
      while (max > 0 && line.charAt (max) != breakChar)
        --max;
      if (max == 0)
        break;
      lines.add (line.substring (0, max + 1));
      line = "       ".substring (0, firstSpace + 1) + line.substring (max + 1);
    }

    lines.add (line);
    return lines;
  }

  // ---------------------------------------------------------------------------------//
  private List<String> splitDim (String line)
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();

    Pattern p = Pattern.compile ("[A-Z][A-Z0-9]*[$%]?\\([0-9,]*\\)[,:]?");
    Matcher m = p.matcher (line);

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

  // ---------------------------------------------------------------------------------//
  private int getIndent (StringBuilder fullText)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = fullText.length () - 1;
    int indent = 0;
    while (ptr >= 0 && fullText.charAt (ptr) != '\n')
    {
      --ptr;
      ++indent;
    }
    return indent;
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
  private String getBase (SourceLine line)
  // ---------------------------------------------------------------------------------//
  {
    boolean isTarget = gotoLines.containsKey (line.lineNumber)
        || gosubLines.containsKey (line.lineNumber);

    if (!basicPreferences.showTargets)
    {
      if (!isTarget && basicPreferences.onlyShowTargetLineNumbers)
        return "      ";
      return String.format (" %5d", line.lineNumber);
    }

    String lineNumberText = String.format ("%5d", line.lineNumber);
    SubLine subline = line.sublines.get (0);
    String c1 = "  ", c2 = "  ";

    if (subline.is (TOKEN_GOSUB) || (subline.is (TOKEN_ON) && subline.has (TOKEN_GOSUB)))
      c1 = "<<";
    else if (subline.is (TOKEN_GOTO)
        || (subline.is (TOKEN_ON) && subline.has (TOKEN_GOTO)))
      c1 = " <";

    if (gotoLines.containsKey (line.lineNumber))
      c2 = "> ";
    if (gosubLines.containsKey (line.lineNumber))
      c2 = ">>";
    if (c1.equals ("  ") && !c2.equals ("  "))
      c1 = "--";
    if (!c1.equals ("  ") && c2.equals ("  "))
      c2 = "--";

    if (!isTarget && basicPreferences.onlyShowTargetLineNumbers)
      lineNumberText = "";

    return String.format ("%s%s %s", c1, c2, lineNumberText);
  }

  // Decide whether the current subline needs to be aligned on its equals sign. If so,
  // and the column hasn't been calculated, read ahead to find the highest position.
  // ---------------------------------------------------------------------------------//
  private int alignEqualsPosition (SubLine subline, int currentAlignPosition)
  // ---------------------------------------------------------------------------------//
  {
    if (subline.equalsPosition > 0)                   // does the line have an equals sign?
    {
      if (currentAlignPosition == 0)
        currentAlignPosition = findHighest (subline); // examine following sublines
      return currentAlignPosition;
    }
    return 0;                                         // reset it
  }

  // The IF processing is so that any assignment that is being aligned doesn't continue
  // to the next full line (because the indentation has changed).
  // ---------------------------------------------------------------------------------//
  private int findHighest (SubLine startSubline)
  // ---------------------------------------------------------------------------------//
  {
    boolean started = false;
    int highestAssign = startSubline.equalsPosition;

    fast: for (SourceLine line : sourceLines)
    {
      boolean inIf = false;
      for (SubLine subline : line.sublines)
      {
        if (started)
        {
          // Stop when we come to a line without an equals sign (except for non-split REMs).
          // Lines that start with a REM always break.
          if (subline.equalsPosition == 0
              // && (splitRem || !subline.is (TOKEN_REM) || subline.isFirst ()))
              && (basicPreferences.splitRem || !subline.isJoinableRem ()))
            break fast; // of champions

          if (subline.equalsPosition > highestAssign)
            highestAssign = subline.equalsPosition;
        }
        else if (subline == startSubline)
          started = true;
        else if (subline.is (TOKEN_IF))
          inIf = true;
      }
      if (started && inIf)
        break;
    }
    return highestAssign;
  }

  // ---------------------------------------------------------------------------------//
  private String getHexText ()
  // ---------------------------------------------------------------------------------//
  {
    if (buffer.length < 2)
      return super.getHexDump ();

    StringBuilder pgm = new StringBuilder ();
    if (basicPreferences.showHeader)
      addHeader (pgm);

    int ptr = 0;
    int offset = Utility.unsignedShort (buffer, 0);
    int programLoadAddress = offset - getLineLength (0);

    while (ptr <= endPtr)             // stop at the same place as the source listing
    {
      int length = getLineLength (ptr);
      if (length == 0)
      {
        pgm.append (
            HexFormatter.formatNoHeader (buffer, ptr, 2, programLoadAddress + ptr));
        ptr += 2;
        break;
      }

      if (ptr + length < buffer.length)
        pgm.append (
            HexFormatter.formatNoHeader (buffer, ptr, length, programLoadAddress + ptr)
                + "\n\n");
      ptr += length;
    }

    if (ptr < buffer.length)
    {
      int length = buffer.length - ptr;
      pgm.append ("\n\n");
      pgm.append (
          HexFormatter.formatNoHeader (buffer, ptr, length, programLoadAddress + ptr));
    }

    return pgm.toString ();
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
  int getLoadAddress ()
  // ---------------------------------------------------------------------------------//
  {
    int programLoadAddress = 0;
    if (buffer.length > 1)
    {
      int offset = Utility.unsignedShort (buffer, 0);
      programLoadAddress = offset - getLineLength (0);
    }
    return programLoadAddress;
  }

  // ---------------------------------------------------------------------------------//
  private int getLineLength (int ptr)
  // ---------------------------------------------------------------------------------//
  {
    int offset = Utility.unsignedShort (buffer, ptr);
    if (offset == 0)
      return 0;
    ptr += 4;               // skip offset and line number
    int length = 5;

    while (ptr < buffer.length && buffer[ptr++] != 0)
      length++;

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
  private String originalFormat ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    if (basicPreferences.showHeader)
      addHeader (text);

    int loadAddress = getLoadAddress ();
    int ptr = 0;
    int nextLine;
    byte b;

    while ((nextLine = Utility.unsignedShort (buffer, ptr)) != 0)
    {
      int lineNumber = Utility.unsignedShort (buffer, ptr + 2);
      text.append (String.format (" %5d ", lineNumber));
      ptr += 4;

      while ((b = buffer[ptr++]) != 0)
        if (Utility.isHighBitSet (b))
          text.append (
              String.format (" %s ", ApplesoftConstants.tokens[b & 0x7F].trim ()));
        else
          text.append ((char) b);

      assert ptr == nextLine - loadAddress;
      //      ptr = nextLine - loadAddress;
      text.append ("\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}