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
public class ApplesoftBasicProgram extends BasicProgram
// -----------------------------------------------------------------------------------//
{
  private final List<SourceLine> sourceLines = new ArrayList<> ();
  private final int endPtr;

  final Map<Integer, List<Integer>> gotoLines = new TreeMap<> ();
  final Map<Integer, List<Integer>> gosubLines = new TreeMap<> ();
  final Map<String, List<Integer>> symbolLines = new TreeMap<> ();
  final Map<String, List<String>> uniqueSymbols = new TreeMap<> ();

  final List<Integer> stringsLine = new ArrayList<> ();
  final List<String> stringsText = new ArrayList<> ();

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
    }
    endPtr = ptr;
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
    int indentSize = 2;
    boolean insertBlankLine = false;

    StringBuilder fullText = new StringBuilder ();
    Stack<String> loopVariables = new Stack<> ();
    if (basicPreferences.showHeader)
      addHeader (fullText);
    int alignPos = 0;
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
        if (subline.is (ApplesoftConstants.TOKEN_REM) && subline.containsToken ())
        {
          int address = subline.getAddress () + 1;              // skip the REM token
          fullText.append (text + String.format ("REM - Inline assembler @ $%02X (%d)%n",
              address, address));
          String padding = "                         ".substring (0, text.length () + 2);
          for (String asm : subline.getAssembler ())
            fullText.append (padding + asm + "\n");
          continue;
        }

        // Beagle Bros often have multiline REM statements
        if (subline.is (ApplesoftConstants.TOKEN_REM) && subline.containsControlChars ())
        {
          subline.addFormattedRem (text);
          fullText.append (text + "\n");
          continue;
        }

        // Reduce the indent by each NEXT, but only as far as the IF indent allows
        if (subline.is (ApplesoftConstants.TOKEN_NEXT))
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
            if (subline.is (ApplesoftConstants.TOKEN_GOSUB)
                || (subline.is (ApplesoftConstants.TOKEN_ON)
                    && subline.has (ApplesoftConstants.TOKEN_GOSUB)))
              text.append ("<<--");
            else if (subline.is (ApplesoftConstants.TOKEN_GOTO)
                || subline.isImpliedGoto () || (subline.is (ApplesoftConstants.TOKEN_ON)
                    && subline.has (ApplesoftConstants.TOKEN_GOTO)))
              text.append (" <--");

          // Align assign statements if required
          if (basicPreferences.alignAssign)
            alignPos = alignEqualsPosition (subline, alignPos);

          int column = indent * indentSize + baseOffset;
          while (text.length () < column)
            text.append (" ");
        }

        // Add the current text, then reset it
        int pos = subline.is (ApplesoftConstants.TOKEN_REM) ? 0 : alignPos;
        String lineText = subline.getAlignedText (pos);

        if (subline.is (ApplesoftConstants.TOKEN_REM)
            && basicPreferences.deleteExtraRemSpace)
          lineText = lineText.replaceFirst ("REM  ", "REM ");

        if (subline.is (ApplesoftConstants.TOKEN_DATA)
            && basicPreferences.deleteExtraDataSpace)
          lineText = lineText.replaceFirst ("DATA  ", "DATA ");

        // Check for a wrappable REM statement
        // (see SEA BATTLE on DISK283.DSK)
        int inset = Math.max (text.length (), getIndent (fullText)) + 1;
        if (subline.is (ApplesoftConstants.TOKEN_REM)
            && lineText.length () > basicPreferences.wrapRemAt)
        {
          List<String> lines = splitLine (lineText, basicPreferences.wrapRemAt, ' ');
          addSplitLines (lines, text, inset);
        }
        else if (subline.is (ApplesoftConstants.TOKEN_DATA)
            && lineText.length () > basicPreferences.wrapDataAt)
        {
          List<String> lines = splitLine (lineText, basicPreferences.wrapDataAt, ',');
          addSplitLines (lines, text, inset);
        }
        else if (subline.is (ApplesoftConstants.TOKEN_DIM) && basicPreferences.splitDim)
        {
          List<String> lines = splitDim (lineText);
          addSplitLines (lines, text, inset);
        }
        else
          text.append (lineText);

        // Check for a wrappable PRINT statement
        // (see FROM MACHINE LANGUAGE TO BASIC on DOSToolkit2eB.dsk)
        if (basicPreferences.wrapPrintAt > 0           //
            && (subline.is (ApplesoftConstants.TOKEN_PRINT)
                || subline.is (ApplesoftConstants.TOKEN_INPUT))
            && countChars (text, Utility.ASCII_QUOTE) == 2      // just start and end quotes
            && countChars (text, Utility.ASCII_CARET) == 0)     // no control characters
        //    && countChars (text, ASCII_SEMI_COLON) == 0)
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
        else
          fullText.append (text + "\n");

        text.setLength (0);

        // Calculate indent changes that take effect after the current subline
        if (subline.is (ApplesoftConstants.TOKEN_IF))
          ifIndent = ++indent;
        else if (subline.is (ApplesoftConstants.TOKEN_FOR))
        {
          loopVariables.push (subline.forVariable);
          ++indent;
        }
        else if (basicPreferences.blankAfterReturn
            && subline.is (ApplesoftConstants.TOKEN_RETURN))
          insertBlankLine = true;
      }

      if (insertBlankLine)
      {
        fullText.append ("\n");
        insertBlankLine = false;
      }

      // Reset alignment value if we just left an IF - the indentation will be different now
      if (ifIndent > 0)
        alignPos = 0;
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

    if (basicPreferences.showXref && !gosubLines.isEmpty ())
      showLines (fullText, gosubLines, "GOSUB:\n");

    if (basicPreferences.showXref && !gotoLines.isEmpty ())
      showLines (fullText, gotoLines, "GOTO:\n");

    if (basicPreferences.showSymbols && !symbolLines.isEmpty ())
    {
      if (fullText.charAt (fullText.length () - 2) != '\n')
        fullText.append ("\n");

      fullText.append ("Variables:\n");

      int longestVarName = getLongestVarName ();
      String format = longestVarName > 6 ? "%" + longestVarName + "s  %s%n" : "%6s  %s%n";

      for (String symbol : symbolLines.keySet ())
      {
        String line = symbolLines.get (symbol).toString ();
        line = line.substring (1, line.length () - 2);
        for (String s : splitXref (line, 90, ' '))
        {
          fullText.append (String.format (format, symbol, s));
          symbol = "";
        }
      }
    }

    if (basicPreferences.showDuplicateSymbols && !uniqueSymbols.isEmpty ())
    {
      if (fullText.charAt (fullText.length () - 2) != '\n')
        fullText.append ("\n");

      boolean headingShown = false;
      for (String key : uniqueSymbols.keySet ())
      {
        List<String> usage = uniqueSymbols.get (key);
        if (usage.size () > 1)
        {
          if (!headingShown)
          {
            headingShown = true;
            fullText.append ("Duplicate Variable Names:\n");
          }
          fullText.append (String.format ("%6s  %s%n", key, usage));
        }
      }
    }

    if (basicPreferences.listStrings && stringsLine.size () > 0)
    {
      if (fullText.charAt (fullText.length () - 2) != '\n')
        fullText.append ("\n");
      fullText.append ("Strings:\n");
      for (int i = 0; i < stringsLine.size (); i++)
      {
        fullText.append (
            String.format (" %5s  %s%n", stringsLine.get (i), stringsText.get (i)));
      }
    }

    if (fullText.length () > 0)
      while (fullText.charAt (fullText.length () - 1) == '\n')
        fullText.deleteCharAt (fullText.length () - 1);     // remove trailing newlines

    return fullText.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private int getLongestVarName ()
  // ---------------------------------------------------------------------------------//
  {
    int longestName = 0;
    for (String symbol : symbolLines.keySet ())
      if (symbol.length () > longestName)
        longestName = symbol.length ();
    return longestName;
  }

  // ---------------------------------------------------------------------------------//
  private void showLines (StringBuilder fullText, Map<Integer, List<Integer>> lines,
      String heading)
  // ---------------------------------------------------------------------------------//
  {
    if (fullText.charAt (fullText.length () - 2) != '\n')
      fullText.append ("\n");
    fullText.append (heading);
    for (Integer line : lines.keySet ())
      fullText.append (String.format (" %5s  %s%n", line, lines.get (line)));
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
    System.out.println (line);

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
  private List<String> splitXref (String line, int wrapLength, char breakChar)
  // ---------------------------------------------------------------------------------//
  {
    List<String> lines = new ArrayList<> ();
    while (line.length () > wrapLength)
    {
      int max = Math.min (wrapLength, line.length () - 1);
      while (max > 0 && line.charAt (max) != breakChar)
        --max;
      if (max == 0)
        break;
      lines.add (line.substring (0, max + 1));
      line = line.substring (max + 1);
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

    if (subline.is (ApplesoftConstants.TOKEN_GOSUB)
        || (subline.is (ApplesoftConstants.TOKEN_ON)
            && subline.has (ApplesoftConstants.TOKEN_GOSUB)))
      c1 = "<<";
    else if (subline.is (ApplesoftConstants.TOKEN_GOTO)
        || (subline.is (ApplesoftConstants.TOKEN_ON)
            && subline.has (ApplesoftConstants.TOKEN_GOTO)))
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
    if (subline.assignEqualPos > 0)                   // does the line have an equals sign?
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
    int highestAssign = startSubline.assignEqualPos;

    fast: for (SourceLine line : sourceLines)
    {
      boolean inIf = false;
      for (SubLine subline : line.sublines)
      {
        if (started)
        {
          // Stop when we come to a line without an equals sign (except for non-split REMs).
          // Lines that start with a REM always break.
          if (subline.assignEqualPos == 0
              // && (splitRem || !subline.is (TOKEN_REM) || subline.isFirst ()))
              && (basicPreferences.splitRem || !subline.isJoinableRem ()))
            break fast; // of champions

          if (subline.assignEqualPos > highestAssign)
            highestAssign = subline.assignEqualPos;
        }
        else if (subline == startSubline)
          started = true;
        else if (subline.is (ApplesoftConstants.TOKEN_IF))
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
    if (v1.equals (v2))
      return true;
    if (v1.length () >= 2 && v2.length () >= 2 && v1.charAt (0) == v2.charAt (0)
        && v1.charAt (1) == v2.charAt (1))
      return true;
    return false;
  }
}