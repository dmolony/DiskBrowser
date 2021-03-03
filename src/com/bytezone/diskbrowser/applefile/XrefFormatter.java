package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.isPossibleNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.bytezone.diskbrowser.gui.BasicPreferences;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class XrefFormatter extends BasicFormatter
// -----------------------------------------------------------------------------------//
{
  private static final String underline =
      "----------------------------------------------------"
          + "----------------------------------------------";

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

  private final int longestVarName;

  private final int maxDigits;

  // ---------------------------------------------------------------------------------//
  public XrefFormatter (ApplesoftBasicProgram program, BasicPreferences basicPreferences)
  // ---------------------------------------------------------------------------------//
  {
    super (program, basicPreferences);

    for (SourceLine sourceLine : program.getSourceLines ())
      checkXref (sourceLine);

    longestVarName = getLongestName ();
    maxDigits = getMaxDigits ();

    // build format strings based on existing line numbers and variable names
    formatLeft = longestVarName > 7 ? "%-" + longestVarName + "." + longestVarName + "s  "
        : "%-7.7s  ";
    formatRight = formatLeft.replace ("-", "");
    formatLineNumber = "%" + maxDigits + "d ";
  }

  // ---------------------------------------------------------------------------------//
  void checkXref (SourceLine line)
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
  @Override
  public void format (StringBuilder fullText)
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
  private int getLongestName (Map<String, List<Integer>> map, int longestName)
  // ---------------------------------------------------------------------------------//
  {
    for (String symbol : map.keySet ())
      if (symbol.length () > longestName)
        longestName = symbol.length ();

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
        appendLineNumbers (fullText, symbol + "  ", map.get (symbol));
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
  private String getUniqueName (String symbolName)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = symbolName.length () - 1;

    if (symbolName.charAt (ptr) == Utility.ASCII_DOLLAR             // string
        || symbolName.charAt (ptr) == Utility.ASCII_PERCENT)        // integer
      ptr--;

    return (ptr <= 1) ? symbolName
        : symbolName.substring (0, 2) + symbolName.substring (ptr + 1);
  }
}
