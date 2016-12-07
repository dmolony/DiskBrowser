package com.bytezone.diskbrowser.utilities;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.List;

public class Utility
{
  public static final List<String> suffixes =
      Arrays.asList ("po", "dsk", "do", "hdv", "2mg", "v2d", "nib", "d13", "sdk", "gz");

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

  public static boolean find (byte[] buffer, byte[] key)
  {
    for (int i = 0; i < buffer.length; i++)
    {
      if (buffer[i] == key[0])
      {
        if (matches (buffer, i, key))
        {
          System.out.printf ("Matches at %04X%n", i);
          return true;
        }
      }
    }
    return false;
  }

  public static boolean matches (byte[] buffer, int offset, byte[] key)
  {
    int ptr = 0;
    while (offset < buffer.length && ptr < key.length)
      if (buffer[offset++] != key[ptr++])
        return false;

    return true;
  }

  public static boolean validFileType (String filename)
  {
    int dotPos = filename.lastIndexOf ('.');
    if (dotPos < 0)
      return false;

    String suffix = filename.substring (dotPos + 1).toLowerCase ();

    int dotPos2 = filename.lastIndexOf ('.', dotPos - 1);
    if (dotPos2 > 0)
    {
      String suffix2 = filename.substring (dotPos2 + 1, dotPos).toLowerCase ();
      if (suffix.equals ("gz") && (suffix2.equals ("bxy") || suffix2.equals ("bny")))
        return false;
    }

    return suffixes.contains (suffix);
  }
}