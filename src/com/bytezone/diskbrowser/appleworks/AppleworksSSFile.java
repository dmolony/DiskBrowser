package com.bytezone.diskbrowser.appleworks;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class AppleworksSSFile extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  Header header;
  List<Row> rows = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public AppleworksSSFile (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    header = new Header ();

    int ptr = header.ssMinVers == 0 ? 300 : 302;
    while (ptr < buffer.length)
    {
      int length = Utility.getShort (buffer, ptr);

      if (length == 0xFFFF)
        break;

      ptr += 2;
      Row row = new Row (ptr);
      rows.add (row);
      ptr += length;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (header.toString ());

    for (Row row : rows)
    {
      text.append ("\n");
      for (Cell cell : row.cells)
        text.append (cell);
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  static String getCellName (int row, int column)
  // ---------------------------------------------------------------------------------//
  {
    char c1 = (char) ('A' + column / 26 - 1);
    char c2 = (char) ('A' + column % 26);
    return "" + (c1 == '@' ? "" : c1) + c2 + row;
  }

  // ---------------------------------------------------------------------------------//
  private class Header
  // ---------------------------------------------------------------------------------//
  {
    private final int[] columnWidths = new int[127];
    private final char calcOrder;
    private final char calcFrequency;
    private final int lastRow;
    private final int lastColumn;
    private final char windowLayout;
    private final boolean windowSynch;
    private final Window currentWindow;
    private final Window secondWindow;
    private final boolean cellProtection;
    private final int platenWidth;
    private final int leftMargin;
    private final int rightMargin;
    private final int charsPerInch;

    private final int paperLength;
    private final int topMargin;
    private final int bottomMargin;
    private final int linesPerInch;
    private final char spacing;

    private final byte[] printerCodes = new byte[14];
    private final boolean printDash;
    private final boolean printHeader;
    private final boolean zoomed;

    private final int ssMinVers;

    public Header ()
    {
      int ptr = 4;
      for (int i = 0; i < columnWidths.length; i++)
        columnWidths[i] = buffer[ptr++] & 0xFF;

      calcOrder = (char) buffer[131];
      calcFrequency = (char) buffer[132];
      lastRow = Utility.getShort (buffer, 133);
      lastColumn = buffer[135] & 0xFF;
      windowLayout = (char) buffer[136];
      windowSynch = buffer[137] != 0;

      currentWindow = new Window (138);
      secondWindow = new Window (162);

      cellProtection = buffer[213] != 0;
      platenWidth = buffer[215] & 0xFF;
      leftMargin = buffer[216] & 0xFF;
      rightMargin = buffer[217] & 0xFF;
      charsPerInch = buffer[218] & 0xFF;

      paperLength = buffer[219] & 0xFF;
      topMargin = buffer[220] & 0xFF;
      bottomMargin = buffer[221] & 0xFF;
      linesPerInch = buffer[222] & 0xFF;
      spacing = (char) buffer[223];

      System.arraycopy (buffer, 224, printerCodes, 0, printerCodes.length);

      printDash = buffer[238] != 0;
      printHeader = buffer[239] != 0;
      zoomed = buffer[240] != 0;

      ssMinVers = buffer[242];
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Calc order ..... %s %n", calcOrder));
      text.append (String.format ("Calc freq ...... %s %n", calcFrequency));
      text.append (String.format ("Last row ....... %d %n", lastRow));
      text.append (String.format ("Last column .... %d %n", lastColumn));
      text.append (String.format ("Window layout .. %s %n", windowLayout));
      text.append (String.format ("Window synch ... %s %n", windowSynch));
      text.append (String.format ("Min version .... %s %n%n", ssMinVers));

      String[] s1 = currentWindow.toString ().split ("\n");
      String[] s2 = secondWindow.toString ().split ("\n");
      for (int i = 0; i < s1.length; i++)
        text.append (String.format ("%-30s %-30s%n", s1[i], s2[i]));
      text.append ("\n");

      text.append (String.format ("Cell protect ... %s %n", cellProtection));
      text.append (String.format ("Platen width ... %d %n", platenWidth));
      text.append (String.format ("Left margin .... %d %n", leftMargin));
      text.append (String.format ("Right margin ... %d %n", rightMargin));
      text.append (String.format ("Chars per inch . %d %n", charsPerInch));

      text.append (String.format ("Paper length ... %d %n", paperLength));
      text.append (String.format ("Top margin ..... %d %n", topMargin));
      text.append (String.format ("Bottom margin .. %d %n", bottomMargin));
      text.append (String.format ("Lines per inch . %d %n", linesPerInch));
      text.append (String.format ("Spacing ........ %s %n", spacing));

      String prC = HexFormatter.getHexString (printerCodes);
      text.append (String.format ("Printer codes .. %s %n", prC));
      text.append (String.format ("Print dash ..... %s %n", printDash));
      text.append (String.format ("Print header ... %s %n", printHeader));
      text.append (String.format ("Zoomed ......... %s %n", zoomed));

      return text.toString ();
    }
  }

  private class Window
  {
    private final int justification;
    private final CellFormat format;
    private final int r1;
    private final int c1;
    private final int r2;
    private final int c2;
    private final int r3;
    private final int c3;
    private final int r4;
    private final int c4;
    private final int r5;
    private final int c5;
    private final int r6;
    private final int c6;
    private final int r7;
    private final int c7;
    private final int bodyRows;
    private final boolean rightColumnNotDisplayed;
    private final boolean topTitleSwitch;
    private final boolean sideTitleSwitch;

    public Window (int offset)
    {
      justification = buffer[offset] & 0xFF;

      format = new CellFormat (buffer[offset + 1], buffer[offset + 2]);

      r1 = buffer[offset + 3] & 0xFF;
      c1 = buffer[offset + 4] & 0xFF;
      r2 = Utility.getShort (buffer, offset + 5);
      c2 = buffer[offset + 7] & 0xFF;
      r3 = Utility.getShort (buffer, offset + 8);
      c3 = buffer[offset + 10] & 0xFF;
      r4 = Utility.getShort (buffer, offset + 11);
      c4 = buffer[offset + 13] & 0xFF;
      r5 = buffer[offset + 14] & 0xFF;
      c5 = buffer[offset + 15] & 0xFF;
      r6 = Utility.getShort (buffer, offset + 16);
      c6 = buffer[offset + 18] & 0xFF;
      r7 = buffer[offset + 19] & 0xFF;
      c7 = buffer[offset + 20] & 0xFF;

      bodyRows = buffer[offset + 21] & 0xFF;
      rightColumnNotDisplayed = buffer[offset + 21] != 0;

      int flags = buffer[offset + 23] & 0xFF;
      topTitleSwitch = (flags & 0x80) != 0;
      sideTitleSwitch = (flags & 0x40) != 0;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Justification .. %s %n", justification));
      text.append (String.format ("Format ......... %s %n", format.mask ()));
      text.append (String.format ("Decimals ....... %s %n", format.decimals));
      text.append (String.format ("Top line ....... %d %n", r1));
      text.append (String.format ("Left column .... %d %n", c1));
      text.append (String.format ("Title top line . %d %n", r2));
      text.append (String.format ("Title left col . %d %n", c2));
      text.append (String.format ("Top line ....... %d %n", r3));
      text.append (String.format ("Left column .... %d %n", c3));
      text.append (String.format ("Title top line . %d %n", r4));
      text.append (String.format ("Title left col . %d %n", c4));
      text.append (String.format ("Title top line . %d %n", r5));
      text.append (String.format ("Title left col . %d %n", c5));
      text.append (String.format ("Top line ....... %d %n", r6));
      text.append (String.format ("Left column .... %d %n", c6));
      text.append (String.format ("Title top line . %d %n", r7));
      text.append (String.format ("Title left col . %d %n", c7));
      text.append (String.format ("Body rows ...... %d %n", bodyRows));
      text.append (String.format ("Right col hidden %s %n", rightColumnNotDisplayed));
      text.append (String.format ("Top title sw ... %s %n", topTitleSwitch));
      text.append (String.format ("Left title sw .. %s %n", sideTitleSwitch));

      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  private class Row
  // ---------------------------------------------------------------------------------//
  {
    private final int rowNumber;
    private final List<Cell> cells = new ArrayList<> ();

    public Row (int ptr)
    {
      rowNumber = Utility.getShort (buffer, ptr);
      ptr += 2;                                   // first control byte

      int column = 0;
      int val;

      while ((val = buffer[ptr++] & 0xFF) != 0xFF)
      {
        if (val > 0x80)
        {
          column += (val - 0x80);                 // skip columns
          continue;
        }

        if (ptr >= buffer.length)
        {
          System.out.println ("too long for buffer");
          break;
        }

        int b1 = buffer[ptr] & 0xFF;

        if ((b1 & 0xE0) == 0 || (b1 & 0xA0) == 0x20)                // Label - 000 or 0.1
          cells.add (new CellLabel (buffer, rowNumber, column++, ptr, val));
        else if ((b1 & 0xA0) == 0xA0)                               // Constant - 1.1
        {
          if (val > 0)
            cells.add (new CellConstant (buffer, rowNumber, column++, ptr, val));
        }
        else if ((b1 & 0xA0) == 0x80)                               // Value - 1.0
          cells.add (new CellValue (buffer, rowNumber, column++, ptr, val));
        else
          System.out.println ("Unknown Cell value : " + val);

        ptr += val;
      }
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Row number ..... %s %n", rowNumber));
      for (Cell cell : cells)
      {
        text.append (cell);
        text.append ("\n");
      }

      return text.toString ();
    }
  }
}