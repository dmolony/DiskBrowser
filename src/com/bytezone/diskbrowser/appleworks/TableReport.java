package com.bytezone.diskbrowser.appleworks;

class TableReport extends Report
{
  private final int[] columnWidths = new int[33];
  private final int[] spaces = new int[33];
  private final int[] footTotals = new int[33];
  private final int[] justification = new int[33];
  private final int[] reportCategoryNames = new int[33];

  private final int[] calculatedColumn = new int[3];
  private final String[] calculatedCategory = new String[3];
  private final String[] calculatedRules = new String[3];

  private final int groupTotalColumn;
  private final boolean printGroupTotals;

  public TableReport (AppleworksADBFile parent, byte[] buffer, int offset)
  {
    super (parent, buffer, offset);

    for (int i = 0; i < categoriesOnThisReport; i++)
    {
      columnWidths[i] = buffer[offset + 20 + i] & 0xFF;
      spaces[i] = buffer[offset + 56 + i] & 0xFF;
      reportCategoryNames[i] = buffer[offset + 92 + i] & 0xFF;
      footTotals[i] = buffer[offset + 128 + i] & 0xFF;
      justification[i] = buffer[offset + 164 + i] & 0xFF;
    }

    for (int i = 0; i < 3; i++)
    {
      calculatedColumn[i] = buffer[offset + 201 + i] & 0xFF;
      calculatedCategory[i] = pascalString (buffer, offset + 302 + i * 54);
      calculatedRules[i] = pascalString (buffer, offset + 324 + i * 54);
    }

    groupTotalColumn = buffer[offset + 204] & 0xFF;
    printGroupTotals = buffer[offset + 217] != 0;
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    if (printHeader && !titleLine.isEmpty ())
      text.append (titleLine);
    else
      text.append ("Report name: " + name);
    text.append ("\n\n");

    StringBuilder header = new StringBuilder ();
    StringBuilder underline = new StringBuilder ();

    for (int i = 0; i < categoriesOnThisReport; i++)
    {
      int category = reportCategoryNames[i];
      String categoryName;
      if (category < 0x7F)
        categoryName = parent.categoryNames[category - 1];
      else
      {
        int calcField = (category - 0x80);
        categoryName = calculatedCategory[calcField];
      }

      if (categoryName.length () > columnWidths[i])
        categoryName = categoryName.substring (0, columnWidths[i]);

      header.append (categoryName);
      header.append (gap.substring (0, columnWidths[i] + spaces[i] - categoryName.length ()));
      underline.append (line.substring (0, columnWidths[i]));
      underline.append (gap.substring (0, spaces[i]));
    }

    header = trimRight (header);
    text.append (header.toString ());
    text.append ("\n");

    underline = trimRight (underline);
    text.append (underline.toString ());
    text.append ("\n");

    float[] totals = new float[33];

    for (Record record : parent.records)
    {
      for (int i = 0; i < categoriesOnThisReport; i++)
      {
        int category = reportCategoryNames[i];
        String item;
        if (category < 0x7F)
          item = record.getItem (category - 1).trim ();
        else
        {
          int calcField = (category - 0x80);
          String cond = calculatedRules[calcField];
          int col = calculatedColumn[calcField] - 1;
          String format = "%12." + justification[col] + "f";
          item =
                String.format (format, record.calculateItem (calcField, i + 97, cond)).trim ();
          //          System.out.println (item);
        }

        if (item.length () > columnWidths[i])
          item = item.substring (0, columnWidths[i]);

        if (footTotals[i] != 0xFF && !item.isEmpty () && !item.equals (" "))
        {
          try
          {
            totals[i] += Float.parseFloat (item);
          }
          catch (NumberFormatException e)
          {
            // ignore this value
          }
        }

        if (justification[i] != 0xFF)
        {
          text.append (gap.substring (0, columnWidths[i] - item.length ()));
          text.append (item);
        }
        else
        {
          text.append (item);
          text.append (gap.substring (0, columnWidths[i] - item.length ()));
        }

        text.append (gap.substring (0, spaces[i]));
      }

      text = trimRight (text);
      text.append ("\n");
    }

    text.append (underline.toString ());
    text.append ("\n");

    StringBuilder totalLine = new StringBuilder ();
    underline = new StringBuilder ();
    boolean hasTotals = false;

    for (int i = 0; i < categoriesOnThisReport; i++)
    {
      if (footTotals[i] == 0xFF)          // not totalled
      {
        totalLine.append (gap.substring (0, columnWidths[i]));
        underline.append (gap.substring (0, columnWidths[i]));
      }
      else
      {
        hasTotals = true;
        String format = "%12." + footTotals[i] + "f";
        String value = String.format (format, totals[i]).trim ();
        if (value.length () > columnWidths[i])
          value = value.substring (0, columnWidths[i]);   // wrong end

        totalLine.append (gap.substring (0, columnWidths[i] - value.length ()));
        totalLine.append (value);
        underline.append (line.substring (0, columnWidths[i]));
      }

      totalLine.append (gap.substring (0, spaces[i]));
      underline.append (gap.substring (0, spaces[i]));
    }

    if (hasTotals)
    {
      text.append (totalLine.toString ());
      text.append ("\n");
      text.append (underline.toString ());
      text.append ("\n");
    }

    return text.toString ();
  }

  private StringBuilder trimRight (StringBuilder text)
  {
    while (text.length () > 0 && text.charAt (text.length () - 1) == ' ')
      text.deleteCharAt (text.length () - 1);
    return text;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());
    text.append (String.format ("Calculated ......... %d %d %d%n", calculatedColumn[0],
                                calculatedColumn[1], calculatedColumn[2]));
    text.append (String.format ("Group total ........ %d%n", groupTotalColumn));
    text.append (String.format ("Print gr totals .... %s%n", printGroupTotals));
    text.append (String.format ("Calc category1 ..... %s%n", calculatedCategory[0]));
    text.append (String.format ("Calc rules1 ........ %s%n", calculatedRules[0]));
    text.append (String.format ("Calc category2 ..... %s%n", calculatedCategory[1]));
    text.append (String.format ("Calc rules2 ........ %s%n", calculatedRules[1]));
    text.append (String.format ("Calc category3 ..... %s%n", calculatedCategory[2]));
    text.append (String.format ("Calc rules3 ........ %s%n", calculatedRules[2]));

    text.append (String.format ("%n  Width Space Name Foot Just%n"));
    for (int i = 0; i < categoriesOnThisReport; i++)
      text.append (String.format ("    %2d    %2d   %02X   %02X   %02X %n", columnWidths[i],
                                  spaces[i], reportCategoryNames[i], footTotals[i],
                                  justification[i]));

    return text.toString ();
  }
}