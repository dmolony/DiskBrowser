package com.bytezone.diskbrowser.applefile;

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
    StringBuilder line = subline.toStringBuilder ();      // get line

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
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Equals position ..... %d%n", equalsPosition));
    text.append (String.format ("Target length ....... %d%n", targetLength));
    text.append (String.format ("First subline ....... %s%n", firstSubLine));
    text.append (String.format ("Last subline ........ %s", lastSubLine));

    return text.toString ();
  }
}
