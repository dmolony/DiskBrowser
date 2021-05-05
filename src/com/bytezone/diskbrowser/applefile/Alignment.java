package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.isControlCharacter;

// ---------------------------------------------------------------------------------//
class Alignment implements ApplesoftConstants
// ---------------------------------------------------------------------------------//
{
  int equalsPosition;
  int targetLength;
  SubLine firstSubLine;
  SubLine lastSubLine;

  // ---------------------------------------------------------------------------------//
  void reset ()
  // ---------------------------------------------------------------------------------//
  {
    equalsPosition = 0;
    targetLength = 0;
    firstSubLine = null;
    lastSubLine = null;
  }

  // ---------------------------------------------------------------------------------//
  void setFirst (SubLine subline)
  // ---------------------------------------------------------------------------------//
  {
    reset ();
    firstSubLine = subline;
    check (subline);
  }

  // ---------------------------------------------------------------------------------//
  void check (SubLine subline)
  // ---------------------------------------------------------------------------------//
  {
    //      System.out.printf ("%-20s  %d %d%n", subline, subline.equalsPosition,
    //          subline.endPosition - subline.equalsPosition);
    if (equalsPosition < subline.equalsPosition)
      equalsPosition = subline.equalsPosition;

    int temp = subline.endPosition - subline.equalsPosition;
    if (targetLength < temp)
      targetLength = temp;

    lastSubLine = subline;
  }

  // ---------------------------------------------------------------------------------//
  public String getAlignedText (SubLine subline)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = toStringBuilder (subline);      // get line

    if (equalsPosition == 0 || subline.is (TOKEN_REM))
      return line.toString ();

    int alignEqualsPos = equalsPosition;
    int targetLength = subline.endPosition - equalsPosition;

    // insert spaces before '=' until it lines up with the other assignment lines
    while (alignEqualsPos-- > subline.equalsPosition)
      line.insert (subline.equalsPosition, ' ');

    if (line.charAt (line.length () - 1) == ':')
      while (targetLength++ <= this.targetLength)
        line.append (" ");

    return line.toString ();
  }

  // ---------------------------------------------------------------------------------//
  static StringBuilder toStringBuilder (SubLine subline)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = new StringBuilder ();

    // All sublines end with 0 or : except IF lines that are split into two
    int max = subline.startPtr + subline.length - 1;
    if (subline.buffer[max] == 0)
      --max;

    if (subline.isImpliedGoto () && !ApplesoftBasicProgram.basicPreferences.showThen)
      line.append ("GOTO ");

    for (int p = subline.startPtr; p <= max; p++)
    {
      byte b = subline.buffer[p];
      if (subline.isToken (b))
      {
        if (line.length () > 0 && line.charAt (line.length () - 1) != ' ')
          line.append (' ');
        int val = b & 0x7F;
        if (b != TOKEN_THEN || ApplesoftBasicProgram.basicPreferences.showThen)
          line.append (ApplesoftConstants.tokens[val] + " ");
      }
      //      else if (Utility.isControlCharacter (b))
      //        line.append (ApplesoftBasicProgram.basicPreferences.showCaret
      //            ? "^" + (char) (b + 64) : "?");
      else if (!isControlCharacter (b))
        line.append ((char) b);
    }

    return line;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Equals position ..... %d%n", equalsPosition));
    text.append (String.format ("Target length ....... %d%n", targetLength));
    text.append (
        String.format ("First subline ....... %s%n", toStringBuilder (firstSubLine)));
    text.append (
        String.format ("Last subline ........ %s", toStringBuilder (lastSubLine)));

    return text.toString ();
  }
}
