package com.bytezone.diskbrowser.gui;

// -----------------------------------------------------------------------------------//
public class BasicPreferences
// -----------------------------------------------------------------------------------//
{
  public boolean showHeader = true;
  public boolean userFormat = true;
  public boolean showAllXref = true;

  public boolean appleLineWrap = false;

  public boolean splitRem = false;
  public boolean splitDim = false;
  public boolean alignAssign = true;
  public boolean showCaret = false;
  public boolean showThen = true;
  public boolean blankAfterReturn = false;
  public boolean formatRem = false;
  public boolean deleteExtraDataSpace = false;

  public boolean showGosubGoto = false;
  public boolean showCalls = false;
  public boolean showSymbols = false;
  public boolean showFunctions = false;
  public boolean showConstants = false;
  public boolean showDuplicateSymbols = false;

  public int wrapPrintAt = 80;
  public int wrapRemAt = 80;
  public int wrapDataAt = 80;

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Split REM ................ %s%n", splitRem));
    text.append (String.format ("Align assign ............. %s%n", alignAssign));
    text.append (String.format ("Show header .............. %s%n", showHeader));
    text.append (String.format ("User format .............. %s%n", userFormat));
    text.append (String.format ("Show All Xref ............ %s%n", showAllXref));
    text.append (String.format ("Apple line wrap .......... %s%n", appleLineWrap));
    text.append (String.format ("Show caret ............... %s%n", showCaret));
    text.append (String.format ("Show THEN ................ %s%n", showThen));
    text.append (String.format ("Show GOTO/GOSUB .......... %s%n", showGosubGoto));
    text.append (String.format ("Show CALL ................ %s%n", showCalls));
    text.append (String.format ("Show symbols ............. %s%n", showSymbols));
    text.append (String.format ("Show constants ........... %s%n", showConstants));
    text.append (String.format ("Show functions ........... %s%n", showFunctions));
    text.append (String.format ("Show duplicate symbols ... %s%n", showDuplicateSymbols));
    text.append (String.format ("Blank after RETURN ....... %s%n", blankAfterReturn));
    text.append (String.format ("Format REM ............... %s%n", formatRem));
    text.append (String.format ("Delete extra DATA space .. %s%n", deleteExtraDataSpace));
    text.append (String.format ("Wrap PRINT at ............ %d%n", wrapPrintAt));
    text.append (String.format ("Wrap REM at .............. %d%n", wrapRemAt));
    text.append (String.format ("Wrap DATA at ............. %d%n", wrapDataAt));
    text.append (String.format ("Split DIM ................ %d", splitDim));

    return text.toString ();
  }
}
