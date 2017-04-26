package com.bytezone.diskbrowser.utilities;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.List;

public class Utility
{
  public static final List<String> suffixes =
      Arrays.asList ("po", "dsk", "do", "hdv", "2mg", "v2d", "d13", "sdk");

  // not used - it doesn't work with Oracle's JDK
  private static boolean hasRetinaDisplay ()
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

  static int getLong (byte[] buffer, int ptr)
  {
    return getWord (buffer, ptr) + getWord (buffer, ptr + 2) * 0x10000;
  }

  static int getWord (byte[] buffer, int ptr)
  {
    int a = (buffer[ptr + 1] & 0xFF) << 8;
    int b = buffer[ptr] & 0xFF;
    return a + b;
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

  public static int getSuffixNo (String filename)
  {
    return suffixes.indexOf (getSuffix (filename));
  }

  public static String getSuffix (String filename)
  {
    String lcFilename = filename.toLowerCase ();

    if (lcFilename.endsWith (".gz"))
      lcFilename = lcFilename.substring (0, lcFilename.length () - 3);
    else if (lcFilename.endsWith (".zip"))
      lcFilename = lcFilename.substring (0, lcFilename.length () - 4);

    int dotPos = lcFilename.lastIndexOf ('.');
    if (dotPos < 0)
      return "";

    return lcFilename.substring (dotPos + 1);
  }

  public static boolean validFileType (String filename)
  {
    return suffixes.contains (getSuffix (filename));
  }
}