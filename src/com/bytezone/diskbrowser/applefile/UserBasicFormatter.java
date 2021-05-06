package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.ASCII_DOLLAR;
import static com.bytezone.diskbrowser.utilities.Utility.ASCII_PERCENT;
import static com.bytezone.diskbrowser.utilities.Utility.getIndent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.diskbrowser.gui.BasicPreferences;

// -----------------------------------------------------------------------------------//
public class UserBasicFormatter extends BasicFormatter
// -----------------------------------------------------------------------------------//
{
  private static final Pattern dimPattern =
      Pattern.compile ("[A-Z][A-Z0-9]*[$%]?\\([0-9]+(,[0-9]+)*\\)[,:]?");
  private static final int INDENT_SIZE = 2;
  private static final String EIGHT_SPACES = "        ";
  private static final String FOUR_SPACES = "    ";
  private static boolean FORCE = true;
  private static boolean NO_FORCE = false;

  // ---------------------------------------------------------------------------------//
  public UserBasicFormatter (ApplesoftBasicProgram program,
      BasicPreferences basicPreferences)
  // ---------------------------------------------------------------------------------//
  {
    super (program, basicPreferences);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void append (StringBuilder fullText)
  // ---------------------------------------------------------------------------------//
  {
    boolean insertBlankLine = false;
    int baseOffset = 7;       // 5 digit line number + 2 spaces

    Stack<String> loopVariables = new Stack<> ();
    Alignment alignment = new Alignment ();

    for (SourceLine line : sourceLines)
    {
      StringBuilder text = new StringBuilder (String.format ("%5d", (line.lineNumber)));

      int indentLevel = loopVariables.size ();   // each full line starts at the loop indent
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
          indentLevel = Math.max (ifIndent, loopVariables.size ());
        }

        // Are we joining REM lines with the previous subline?
        if (joinableRem (subline))
        {
          // Join this REM statement to the previous line, so no indenting
          fullText.deleteCharAt (fullText.length () - 1);         // remove newline
          fullText.append (" ");
        }
        else    // ... otherwise do all the indenting
        {
          // Align assign statements if required
          if (basicPreferences.alignAssign)
            alignEqualsPosition (subline, alignment);

          int column = indentLevel * INDENT_SIZE + baseOffset;
          while (text.length () < column)
            text.append (" ");
        }

        // Add the current text, then reset it
        String lineText = alignment.getAlignedText (subline);

        if (subline.is (TOKEN_DATA) && basicPreferences.deleteExtraDataSpace)
          lineText = lineText.replaceFirst ("DATA  ", "DATA ");

        // Check for a wrappable REM/DATA/DIM statement
        // (see SEA BATTLE on DISK283.DSK)
        int inset = Math.max (text.length (), getIndent (fullText)) + 1;
        if (subline.is (TOKEN_REM) && lineText.length () > basicPreferences.wrapRemAt)
        {
          List<String> lines =
              splitLine (lineText, basicPreferences.wrapRemAt, ' ', FORCE);
          addSplitLines (lines, text, inset);
        }
        else if (subline.is (TOKEN_DATA)
            && lineText.length () > basicPreferences.wrapDataAt)
        {
          List<String> lines =
              splitLine (lineText, basicPreferences.wrapDataAt, ',', FORCE);
          addSplitLines (lines, text, inset);
        }
        else if (subline.is (TOKEN_PRINT)
            && lineText.length () > basicPreferences.wrapPrintAt)
        {
          List<String> lines =
              splitLine (lineText, basicPreferences.wrapDataAt, ';', NO_FORCE);
          addSplitLines (lines, text, inset);
        }
        else if (subline.is (TOKEN_DIM) && basicPreferences.splitDim)
        {
          List<String> lines = splitDim (lineText);
          addSplitLines (lines, text, inset);
        }
        else
          text.append (lineText);

        if (subline == alignment.lastSubLine)
          alignment.reset ();

        fullText.append (text);
        fullText.append (NEWLINE);
        text.setLength (0);

        // Calculate indent changes that take effect after the current subline
        if (subline.is (TOKEN_IF))
          ifIndent = ++indentLevel;
        else if (subline.is (TOKEN_FOR))
        {
          String latestLoopVar = loopVariables.size () > 0 ? loopVariables.peek () : "";
          if (!subline.forVariable.equals (latestLoopVar))    // don't add repeated loop
          {
            loopVariables.push (subline.forVariable);
            ++indentLevel;
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
    }
  }

  // ---------------------------------------------------------------------------------//
  private List<String> splitLine (String line, int wrapLength, char breakChar,
      boolean force)
  // ---------------------------------------------------------------------------------//
  {
    int spaceAt = 0;
    while (spaceAt < line.length () && line.charAt (spaceAt) != ' ')
      ++spaceAt;
    String indent = spaceAt < 8 ? EIGHT_SPACES.substring (0, spaceAt + 1) : EIGHT_SPACES;

    List<String> lines = new ArrayList<> ();

    while (line.length () > wrapLength)
    {
      int breakAt = wrapLength - 1;
      while (breakAt > spaceAt && line.charAt (breakAt) != breakChar)
        --breakAt;

      if (breakAt <= spaceAt)
        break;

      lines.add (line.substring (0, breakAt + 1));      // keep breakChar at end
      line = indent + line.substring (breakAt + 1).trim ();
    }

    if (force)
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
      lines.add (FOUR_SPACES + m.group ());

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
      if (first)
      {
        first = false;
        text.append (line);
      }
      else
        text.append (
            "\n                                           ".substring (0, indent) + line);
  }

  // Decide whether the current subline needs to be aligned on its equals sign. If so,
  // and the column hasn't been calculated, read ahead to find the highest position.
  // ---------------------------------------------------------------------------------//
  private void alignEqualsPosition (SubLine subline, Alignment alignment)
  // ---------------------------------------------------------------------------------//
  {
    if (subline.equalsPosition == 0)
    {
      alignment.reset ();
      return;
    }

    if (alignment.equalsPosition == 0)
      findHighest (subline, alignment);
  }

  // The IF processing is so that any assignment that is being aligned doesn't continue
  // to the next full line (because the indentation has changed).
  // ---------------------------------------------------------------------------------//
  private void findHighest (SubLine startSubline, Alignment alignment)
  // ---------------------------------------------------------------------------------//
  {
    boolean started = false;
    alignment.setFirst (startSubline);

    outerLoop: for (int i = sourceLines.indexOf (startSubline.sourceLine); i < sourceLines
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

          if (subline.equalsPosition > 0)
            alignment.check (subline);
        }
        else if (subline == startSubline)
          started = true;
        else if (subline.is (TOKEN_IF))
          precededByIf = true;
      }

      if (started && precededByIf)     // sublines of IF have now finished
        break;                         // don't continue with following SourceLine
    }
  }

  // ---------------------------------------------------------------------------------//
  private boolean joinableRem (SubLine subline)
  // ---------------------------------------------------------------------------------//
  {
    return subline.isJoinableRem () && !basicPreferences.splitRem;
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
  private String getUniqueName (String symbol)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = symbol.length () - 1;

    if (symbol.charAt (ptr) == ASCII_DOLLAR             // string
        || symbol.charAt (ptr) == ASCII_PERCENT)        // integer
      ptr--;

    return (ptr <= 1) ? symbol : symbol.substring (0, 2) + symbol.substring (ptr + 1);
  }

  // A REM statement might conceal an assembler routine
  // ---------------------------------------------------------------------------------//
  private String[] getRemAssembler (SubLine subline)
  // ---------------------------------------------------------------------------------//
  {
    AssemblerProgram program = new AssemblerProgram ("REM assembler",
        subline.getBuffer (), getLoadAddress () + subline.startPtr + 1);

    return program.getAssembler ().split (NEWLINE);
  }
}
