package com.bytezone.diskbrowser.utilities;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

public class Utility
{
  // not used - it doesn't work with Oracle's JDK
  public static boolean hasRetinaDisplay ()
  {
    Object obj =
        Toolkit.getDefaultToolkit ().getDesktopProperty ("apple.awt.contentScaleFactor");
    if (obj instanceof Float)
    {
      Float f = (Float) obj;
      int scale = f.intValue ();
      return (scale == 2);            // 1 indicates a regular mac display.
    }
    return false;
  }

  public static boolean test (Graphics2D g)
  {
    return g.getFontRenderContext ().getTransform ()
        .equals (AffineTransform.getScaleInstance (2.0, 2.0));
  }

  public static void find (byte[] buffer, byte[] key)
  {
    System.out.println ("*********** searching ************");
    //    System.out.println (HexFormatter.format (buffer));
    for (int i = 0; i < buffer.length; i++)
    {
      //      System.out.printf ("%02X  %02X%n", buffer[i], key[0]);
      if (buffer[i] == key[0])
      {
        //        System.out.println ("**** checking first ****");
        if (matches (buffer, i, key))
          System.out.printf ("Matches at %04X%n", i);
      }
    }
  }

  private static boolean matches (byte[] buffer, int offset, byte[] key)
  {
    int ptr = 0;
    while (offset < buffer.length && ptr < key.length)
    {
      if (buffer[offset++] != key[ptr++])
      {
        //        System.out.printf ("%04X: %02X != %02X%n", offset, buffer[offset], key[ptr]);
        return false;
      }
      //      System.out.printf ("%04X: %02X == %02X%n", offset, buffer[offset - 1],
      //                         key[ptr - 1]);
    }
    return true;
  }
}