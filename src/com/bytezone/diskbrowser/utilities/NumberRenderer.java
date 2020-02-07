package com.bytezone.diskbrowser.utilities;

import java.text.NumberFormat;

import javax.swing.SwingConstants;

// -----------------------------------------------------------------------------------//
public class NumberRenderer extends FormatRenderer
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public NumberRenderer (NumberFormat formatter)
  // ---------------------------------------------------------------------------------//
  {
    super (formatter);
    setHorizontalAlignment (SwingConstants.RIGHT);
  }

  // ---------------------------------------------------------------------------------//
  public static NumberRenderer getCurrencyRenderer ()
  // ---------------------------------------------------------------------------------//
  {
    return new NumberRenderer (NumberFormat.getCurrencyInstance ());
  }

  // ---------------------------------------------------------------------------------//
  public static NumberRenderer getIntegerRenderer ()
  // ---------------------------------------------------------------------------------//
  {
    return new NumberRenderer (NumberFormat.getIntegerInstance ());
  }

  // ---------------------------------------------------------------------------------//
  public static NumberRenderer getPercentRenderer ()
  // ---------------------------------------------------------------------------------//
  {
    return new NumberRenderer (NumberFormat.getPercentInstance ());
  }
}