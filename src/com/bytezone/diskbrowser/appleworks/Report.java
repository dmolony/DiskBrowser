package com.bytezone.diskbrowser.appleworks;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
abstract class Report
// -----------------------------------------------------------------------------------//
{
  static final String line = "-------------------------------------------------------"
      + "-----------------------------------\n";
  static final String gap = "                                                        "
      + "                                     ";
  static final String[] testText = { "", "=", ">", "<", "?4", "?5", "<>", "?7", "?8",
                                     "?9", "?10", "?11", "?12", "?13" };
  static final String[] continuationText = { "", "And", "Or", "Through" };

  protected final AppleworksADBFile parent;
  protected final String name;
  private final char reportFormat;
  private final char spacing;
  protected final int categoriesOnThisReport;
  protected final String titleLine;
  protected final boolean printHeader;
  private final int platenWidth;
  private final int leftMargin;
  private final int rightMargin;
  private final int charsPerInch;
  private final int paperLength;
  private final int topMargin;
  private final int bottomMargin;
  private final int linesPerInch;

  private final int[] selectionRules = new int[3];
  private final int[] testTypes = new int[3];
  private final int[] continuation = new int[3];
  private final String[] comparison = new String[3];

  private final String printerRules;
  private final boolean printDash;
  private String fudgeReason;

  // ---------------------------------------------------------------------------------//
  Report (AppleworksADBFile parent, byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    this.parent = parent;

    name = pascalString (buffer, offset);
    categoriesOnThisReport = buffer[offset + 200] & 0xFF;
    platenWidth = buffer[offset + 205] & 0xFF;
    leftMargin = buffer[offset + 206] & 0xFF;
    rightMargin = buffer[offset + 207] & 0xFF;
    charsPerInch = buffer[offset + 208] & 0xFF;
    paperLength = buffer[offset + 209] & 0xFF;
    topMargin = buffer[offset + 210] & 0xFF;
    bottomMargin = buffer[offset + 211] & 0xFF;
    linesPerInch = buffer[offset + 212] & 0xFF;

    reportFormat = (char) buffer[offset + 214];
    spacing = (char) buffer[offset + 215];
    printHeader = buffer[offset + 216] != 0;

    titleLine = pascalString (buffer, offset + 220);
    int printerLength = buffer[offset + 464] & 0xFF;
    if (printerLength > 13)
      System.out.println ("*** Dodgy printer rules ***");

    printerRules = pascalString (buffer, offset + 464);
    printDash = buffer[offset + 478] != 0;

    int fudge = 0;
    if (buffer[offset + 480] != 0)
    {
      fudge = 1;
      fudgeReason = "*** Report rules ***";
      if (buffer[offset + 481] != 0)
        fudgeReason = "*** Bollocksed ***";
    }
    else if (buffer[offset + 464] == 0 && buffer[offset + 465] != 0)
    {
      fudgeReason = "*** Printer codes ***";
      fudge = 1;
    }
    else if (buffer[offset + 479] != 0 && buffer[offset + 485] == 0)
    {
      fudgeReason = "*** Test codes ***";
      fudge = 1;
    }
    else
      fudgeReason = "";

    if (false)
    {
      System.out
          .println ("==============================================================");
      System.out.println ("Header");
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 213, 7));
      System.out.println ();
      System.out.println ("Title line:");
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 220, 81));
      System.out.println ();
      System.out.println ("Calculated categories:");
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 302, 22));
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 324, 32));
      System.out.println ();
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 356, 22));
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 378, 32));
      System.out.println ();
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 410, 22));
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 432, 32));
      System.out.println ();
      System.out.println ("Printer rules:");
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 464 + fudge, 14));
      System.out.println ();
      System.out.println ("Printer dash:");
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 478 + fudge, 1));
      System.out.println ();
      System.out.println ("Selection rules:");
      System.out.println ("  categories:");
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 479 + fudge, 6));
      System.out.println ("  tests:");
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 485 + fudge, 6));
      System.out.println ("  continuation:");
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 491 + fudge, 6));
      System.out.println ("  comparison:");
      System.out.println (HexFormatter.formatNoHeader (buffer, offset + 497 + fudge, 32));
      System.out.println ();
    }

    if (buffer[offset + 480 + fudge] == 0)      // test high byte
      for (int i = 0; i < 3; i++)
      {
        selectionRules[i] = Utility.unsignedShort (buffer, offset + 479 + i * 2 + fudge);
        testTypes[i] = Utility.unsignedShort (buffer, offset + 485 + i * 2 + fudge);
        continuation[i] = Utility.unsignedShort (buffer, offset + 491 + i * 2 + fudge);
        comparison[i] = pascalString (buffer, offset + 497 + i * 32 + fudge);
      }
    else
      System.out.println ("*** Invalid value in report rules ***");
  }

  // ---------------------------------------------------------------------------------//
  abstract String getText ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  protected String pascalString (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    return new String (buffer, ptr + 1, buffer[ptr] & 0xFF);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Report name ........ %s%n", name));
    text.append (String.format ("Report type ........ %s%n", reportFormat));
    text.append (
        String.format ("Spacing ............ %s  (Single/Double/Triple)%n", spacing));
    text.append (String.format ("Print header ....... %s%n", printHeader));
    text.append (String.format ("Title .............. %s%n", titleLine));
    text.append (String.format ("L/R/T/B margin ..... %d/%d/%d/%d%n", leftMargin,
        rightMargin, topMargin, bottomMargin));
    text.append (String.format ("Categories ......... %s%n", categoriesOnThisReport));
    text.append (String.format ("Platen width ....... %d%n", platenWidth));
    text.append (String.format ("Chars per inch ..... %d%n", charsPerInch));
    text.append (String.format ("Paper length ....... %d%n", paperLength));
    text.append (String.format ("Lines per inch ..... %d%n", linesPerInch));
    text.append (String.format ("Print dash ......... %s%n", printDash));
    text.append (String.format ("Printer rules ...... %s%n", printerRules));

    text.append ("Report rules ....... ");
    for (int i = 0; i < 3; i++)
    {
      if (selectionRules[i] == 0)
        break;
      int category = selectionRules[i] - 1;
      int test = testTypes[i];
      int cont = continuation[i];
      text.append (String.format ("[%s] %s [%s] %s ", parent.categoryNames[category],
          testText[test], comparison[i], continuationText[cont]));
    }
    text.append ("\n");

    if (!fudgeReason.isEmpty ())
      text.append ("Fudge .............. " + fudgeReason + "\n");

    return text.toString ();
  }
}