package com.bytezone.diskbrowser.utilities;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

public class FontUtility
{
  public static final String OS = System.getProperty ("os.name").toLowerCase ();
  public static final String userHome = System.getProperty ("user.home");
  public static final boolean MAC = OS.startsWith ("mac os");
  public static final boolean MAC_OS_X = OS.startsWith ("mac os x");
  public static final boolean LINUX = OS.equals ("linux");
  public static final boolean WINDOWS = OS.startsWith ("windows");
  public static final String USER = System.getProperty ("user.name");

  private static GraphicsEnvironment ge =
      GraphicsEnvironment.getLocalGraphicsEnvironment ();
  public static String[] fontNames = ge.getAvailableFontFamilyNames ();

  public static enum FontType
  {
    PLAIN, SANS_SERIF, SERIF, MONOSPACED
  };

  public static enum FontSize
  {
    BASE_MINUS_2, BASE_MINUS_1, BASE, BASE_PLUS_1, BASE_PLUS_2
  };

  public static String fontName =
      MAC_OS_X ? "Monaco" : WINDOWS ? "Lucida Sans Typewriter" : "Lucida Sans Typewriter";

  // ---------------------------------------------------------------------------------//
  public static boolean isFontAvailable (String name)
  // ---------------------------------------------------------------------------------//
  {
    for (String s : fontNames)
      if (s.equals (name))
        return true;
    return false;
  }

  // ---------------------------------------------------------------------------------//
  public static Font getFont (FontType fontType, FontSize fontSize)
  // ---------------------------------------------------------------------------------//
  {
    switch (fontSize)
    {
      case BASE:
        return getFont (fontType, 12);
      case BASE_PLUS_1:
        return getFont (fontType, 14);
      case BASE_PLUS_2:
        return getFont (fontType, 16);
      case BASE_MINUS_1:
        return getFont (fontType, 10);
      case BASE_MINUS_2:
        return getFont (fontType, 8);
    }
    return getFont (fontType, 12);
  }

  // ---------------------------------------------------------------------------------//
  public static Font getFont (FontType fontType, int type, int fontSize)
  // ---------------------------------------------------------------------------------//
  {
    assert isFontAvailable (fontName);

    switch (fontType)
    {
      case PLAIN:
        return new Font (fontName, type, fontSize);
      case SANS_SERIF:
        return new Font (fontName, type, fontSize);
      case SERIF:
        return new Font ("Serif", type, fontSize);
      case MONOSPACED:
        return new Font ("Monospaced", type, fontSize);
      default:
        return new Font (fontName, type, fontSize);
    }
  }

  // ---------------------------------------------------------------------------------//
  public static Font getFont (FontType fontType, int fontSize)
  // ---------------------------------------------------------------------------------//
  {
    return getFont (fontType, Font.PLAIN, fontSize);
  }
}
