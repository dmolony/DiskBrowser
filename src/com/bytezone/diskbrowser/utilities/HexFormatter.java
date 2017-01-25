package com.bytezone.diskbrowser.utilities;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.GregorianCalendar;

public class HexFormatter
{
  private static String[] hex =
      { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
  private static MathContext mathContext = new MathContext (9);

  public static String format (byte[] buffer)
  {
    return format (buffer, 0, buffer.length);
  }

  public static String formatNoHeader (byte[] buffer)
  {
    return formatNoHeader (buffer, 0, buffer.length);
  }

  public static String format (byte[] buffer, int offset, int length)
  {
    return format (buffer, offset, length, true, 0);
  }

  public static String format (byte[] buffer, int offset, int length, int startingAddress)
  {
    return format (buffer, offset, length, true, startingAddress);
  }

  public static String formatNoHeader (byte[] buffer, int offset, int length)
  {
    return format (buffer, offset, length, false, 0);
  }

  public static String formatNoHeader (byte[] buffer, int offset, int length,
      int startingAddress)
  {
    return format (buffer, offset, length, false, startingAddress);
  }

  public static String format (byte[] buffer, int offset, int length, boolean header,
      int startingAddress)
  {
    StringBuffer line = new StringBuffer ();
    int[] freq = new int[256];
    boolean startedOnBoundary = offset % 0x100 == 0;

    if (header)
    {
      line.append ("      ");
      for (int i = 0; i < 16; i++)
        line.append ("  " + hex[i]);
      if (offset == 0)
        line.append ("\n");
    }

    for (int i = offset; i < offset + length; i += 16)
    {
      if (line.length () > 0 && i > 0)
        line.append ("\n");
      if (i > offset && startedOnBoundary && (i % 0x200) == 0)
        line.append ("\n");

      // print offset
      line.append (String.format ("%05X : ", (startingAddress + i - offset)));

      // print hex values
      StringBuffer trans = new StringBuffer ();
      StringBuffer hexLine = new StringBuffer ();

      int max = Math.min (i + 16, offset + length);
      max = Math.min (max, buffer.length);
      for (int j = i; j < max; j++)
      {
        int c = buffer[j] & 0xFF;
        freq[c]++;
        hexLine.append (String.format ("%02X ", c));

        if (c > 127)
        {
          if (c < 160)
            c -= 64;
          else
            c -= 128;
        }
        if (c < 32 || c == 127)         // non-printable
          trans.append (".");
        else                            // standard ascii
          trans.append ((char) c);
      }
      while (hexLine.length () < 48)
        hexLine.append (" ");

      line.append (hexLine.toString () + ": " + trans.toString ());
    }

    if (false)
    {
      line.append ("\n\n");
      int totalBits = 0;
      for (int i = 0; i < freq.length; i++)
        if (freq[i] > 0)
        {
          totalBits += (Integer.bitCount (i) * freq[i]);
          line.append (
              String.format ("%02X  %3d   %d%n", i, freq[i], Integer.bitCount (i)));
        }
      line.append (String.format ("%nTotal bits : %d%n", totalBits));
    }
    return line.toString ();
  }

  public static String sanitiseString (byte[] buffer, int offset, int length)
  {
    StringBuilder trans = new StringBuilder ();
    for (int j = offset; j < offset + length; j++)
    {
      int c = buffer[j] & 0xFF;

      if (c > 127)
      {
        if (c < 160)
          c -= 64;
        else
          c -= 128;
      }

      if (c < 32 || c == 127)       // non-printable
        trans.append (".");
      else
        trans.append ((char) c);    // standard ascii
    }
    return trans.toString ();
  }

  public static String getString (byte[] buffer)
  {
    return getString (buffer, 0, buffer.length);
  }

  public static String getString (byte[] buffer, int offset, int length)
  {
    StringBuffer text = new StringBuffer ();

    for (int i = offset; i < offset + length; i++)
    {
      int c = buffer[i] & 0xFF;
      if (c > 127)
      {
        if (c < 160)
          c -= 64;
        else
          c -= 128;
      }
      if (c == 13)
        text.append ("\n");
      else if (c < 32)          // non-printable
        text.append (".");
      else                      // standard ascii
        text.append ((char) c);
    }
    return text.toString ();
  }

  public static String getString2 (byte[] buffer, int offset, int length)
  {
    StringBuffer text = new StringBuffer ();

    for (int i = offset; i < offset + length; i++)
    {
      int c = buffer[i] & 0xFF;
      if (c == 136 && text.length () > 0)
      {
        System.out.println (text.toString ());
        text.deleteCharAt (text.length () - 1);
        System.out.println ("deleted");
        continue;
      }
      if (c > 127)
      {
        if (c < 160)
          c -= 64;
        else
          c -= 128;
      }
      if (c < 32)                           // non-printable
        text.append ((char) (c + 64));
      else                                  // standard ascii
        text.append ((char) c);
    }
    return text.toString ();
  }

  public static String getHexString (byte[] buffer, int offset, int length)
  {
    return getHexString (buffer, offset, length, true);
  }

  public static String getHexString (byte[] buffer)
  {
    return getHexString (buffer, 0, buffer.length);
  }

  public static String getHexString (byte[] buffer, int offset, int length, boolean space)
  {
    StringBuilder hex = new StringBuilder ();
    int max = Math.min (offset + length, buffer.length);
    for (int i = offset; i < max; i++)
    {
      hex.append (String.format ("%02X", buffer[i]));
      if (space)
        hex.append (' ');
    }
    if (length > 0 && space)
      hex.deleteCharAt (hex.length () - 1);
    return hex.toString ();
  }

  public static String getHexStringReversed (byte[] buffer, int offset, int length,
      boolean space)
  {
    StringBuilder hex = new StringBuilder ();
    for (int i = length - 1; i >= 0; i--)
    {
      hex.append (String.format ("%02X", buffer[offset + i]));
      if (space)
        hex.append (' ');
    }
    if (length > 0 && space)
      hex.deleteCharAt (hex.length () - 1);
    return hex.toString ();
  }

  public static String getBitString (byte b)
  {
    String s = "0000000" + Integer.toBinaryString (b & 0xFF);
    s = s.replaceAll ("0", ".");
    return s.substring (s.length () - 8);
  }

  public static char byteValue (byte b)
  {
    int c = b & 0xFF;
    if (c > 127)
      c -= 128;
    if (c > 95)
      c -= 64;
    if (c < 32)             // non-printable
      return '.';
    return (char) c;        // standard ascii

  }

  public static String format4 (int value)
  {
    if (value < 0)
      return "***err**";
    StringBuffer text = new StringBuffer ();
    for (int i = 0, weight = 4096; i < 4; i++)
    {
      int digit = value / weight;
      if (digit < 0 || digit > 15)
        return "***err**";
      text.append (hex[digit]);
      value %= weight;
      weight /= 16;
    }
    return text.toString ();
  }

  public static String format3 (int value)
  {
    return format4 (value).substring (1);
  }

  public static String format2 (int value)
  {
    if (value < 0)
      value += 256;
    String text = hex[value / 16] + hex[value % 16];
    return text;
  }

  public static String format1 (int value)
  {
    String text = hex[value];
    return text;
  }

  public static int intValue (byte b1, byte b2)
  {
    return (b1 & 0xFF) + (b2 & 0xFF) * 256;
  }

  public static int intValue (byte b1, byte b2, byte b3)
  {
    return (b1 & 0xFF) + (b2 & 0xFF) * 256 + (b3 & 0xFF) * 65536;
  }

  public static int unsignedLong (byte[] buffer, int ptr)
  {
    int val = 0;
    for (int i = 3; i >= 0; i--)
    {
      val <<= 8;
      val += buffer[ptr + i] & 0xFF;
    }
    return val;
  }

  public static int signedLong (byte[] buffer, int ptr)
  {
    return (((buffer[ptr] & 0xFF) << 24) | ((buffer[ptr] & 0xFF) << 16)
        | ((buffer[ptr] & 0xFF) << 8) | (buffer[ptr + 1] & 0xFF));
  }

  public static int getLongBigEndian (byte[] buffer, int ptr)
  {
    int val = 0;
    for (int i = 0; i < 4; i++)
    {
      val <<= 8;
      val += buffer[ptr + i] & 0xFF;
    }
    return val;
  }

  public static int unsignedShort (byte[] buffer, int ptr)
  {
    int val = 0;
    for (int i = 1; i >= 0; i--)
    {
      val <<= 8;
      val += buffer[ptr + i] & 0xFF;
    }
    return val;
  }

  //  public static int signedShort (byte[] buffer, int ptr)
  //  {
  //    return (short) (((buffer[ptr] & 0xFF) << 8) | (buffer[ptr + 1] & 0xFF));
  //  }

  public static int signedShort (byte[] buffer, int ptr)
  {
    return (short) ((buffer[ptr] & 0xFF) | ((buffer[ptr + 1] & 0xFF) << 8));
  }

  public static int getShortBigEndian (byte[] buffer, int ptr)
  {
    int val = 0;
    for (int i = 0; i < 2; i++)
    {
      val <<= 8;
      val |= buffer[ptr + i] & 0xFF;
    }
    return val;
  }

  public static double getSANEDouble (byte[] buffer, int offset)
  {
    long bits = 0;
    for (int i = 7; i >= 0; i--)
    {
      bits <<= 8;
      bits |= buffer[offset + i] & 0xFF;
    }

    return Double.longBitsToDouble (bits);
  }

  public static double floatValueOld (byte[] buffer, int offset)
  {
    double val = 0;

    int exponent = (buffer[offset] & 0xFF) - 0x80;

    int mantissa =
        (buffer[offset + 1] & 0x7F) * 0x1000000 + (buffer[offset + 2] & 0xFF) * 0x10000
            + (buffer[offset + 3] & 0xFF) * 0x100 + (buffer[offset + 4] & 0xFF);

    int weight1 = 1;
    long weight2 = 2147483648L;
    double multiplier = 0;

    for (int i = 0; i < 32; i++)
    {
      if ((mantissa & weight2) > 0)
        multiplier += (1.0 / weight1);
      weight2 /= 2;
      weight1 *= 2;
    }
    val = Math.pow (2, exponent - 1) * (multiplier + 1);

    return val;
  }

  public static double floatValue (byte[] buffer, int ptr)
  {
    int exponent = buffer[ptr] & 0x7F;                      // biased 128
    if (exponent == 0)
      return 0.0;

    int mantissa = (buffer[ptr + 1] & 0x7F) << 24 | (buffer[ptr + 2] & 0xFF) << 16
        | (buffer[ptr + 3] & 0xFF) << 8 | (buffer[ptr + 4] & 0xFF);
    boolean negative = (buffer[ptr + 1] & 0x80) > 0;
    double value = 0.5;
    for (int i = 2, weight = 0x40000000; i <= 32; i++, weight >>>= 1)
      if ((mantissa & weight) > 0)
        value += Math.pow (0.5, i);
    value *= Math.pow (2, exponent);
    BigDecimal bd = new BigDecimal (value);
    double rounded = bd.round (mathContext).doubleValue ();
    return negative ? rounded * -1 : rounded;
  }

  public static GregorianCalendar getAppleDate (byte[] buffer, int offset)
  {
    int date = HexFormatter.intValue (buffer[offset], buffer[offset + 1]);
    if (date > 0)
    {
      int year = (date & 0xFE00) >> 9;
      int month = (date & 0x01E0) >> 5;
      int day = date & 0x001F;
      int hour = buffer[offset + 3] & 0x1F;
      int minute = buffer[offset + 2] & 0x3F;
      if (year < 70)
        year += 2000;
      else
        year += 1900;
      return new GregorianCalendar (year, month - 1, day, hour, minute);
    }
    return null;
  }

  public static GregorianCalendar getPascalDate (byte[] buffer, int offset)
  {
    int year = (buffer[offset + 1] & 0xFF);
    int day = (buffer[offset] & 0xF0) >> 4;
    int month = buffer[offset] & 0x0F;
    if (day == 0 || month == 0)
      return null;
    if (year % 2 > 0)
      day += 16;
    year /= 2;
    if (year < 70)
      year += 2000;
    else
      year += 1900;
    return new GregorianCalendar (year, month - 1, day);
  }

  public static String getPascalString (byte[] buffer, int offset)
  {
    int length = buffer[offset] & 0xFF;
    return HexFormatter.getString (buffer, offset + 1, length);
  }
}
