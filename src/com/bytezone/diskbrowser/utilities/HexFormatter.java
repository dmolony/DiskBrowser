package com.bytezone.diskbrowser.utilities;

// -----------------------------------------------------------------------------------//
public class HexFormatter
// -----------------------------------------------------------------------------------//
{
  private static String[] hex =
      { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

  // ---------------------------------------------------------------------------------//
  public static String format (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return format (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  public static String formatNoHeader (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return formatNoHeader (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  public static String format (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    return format (buffer, offset, length, true, 0);
  }

  // ---------------------------------------------------------------------------------//
  public static String format (byte[] buffer, int offset, int length, int startingAddress)
  // ---------------------------------------------------------------------------------//
  {
    return format (buffer, offset, length, true, startingAddress);
  }

  // ---------------------------------------------------------------------------------//
  public static String formatNoHeader (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    return format (buffer, offset, length, false, 0);
  }

  // ---------------------------------------------------------------------------------//
  public static String formatNoHeader (byte[] buffer, int offset, int length,
      int startingAddress)
  // ---------------------------------------------------------------------------------//
  {
    return format (buffer, offset, length, false, startingAddress);
  }

  // ---------------------------------------------------------------------------------//
  public static String format (byte[] buffer, int offset, int length, boolean header,
      int startingAddress)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = new StringBuilder ();
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

  // ---------------------------------------------------------------------------------//
  public static String sanitiseString (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  public static String getString (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return getString (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  public static String getString (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

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

  // ---------------------------------------------------------------------------------//
  public static String getString2 (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    for (int i = offset; i < offset + length; i++)
    {
      int c = buffer[i] & 0xFF;
      if (c == 136)
      {
        if (text.length () > 0)
          text.deleteCharAt (text.length () - 1);
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

  // ---------------------------------------------------------------------------------//
  public static String getHexString (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    return getHexString (buffer, offset, length, true);
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexString (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return getHexString (buffer, 0, buffer.length);
  }

  // ---------------------------------------------------------------------------------//
  public static String getHexString (byte[] buffer, int offset, int length, boolean space)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  public static String getHexStringReversed (byte[] buffer, int offset, int length,
      boolean space)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  public static String getBitString (byte b)
  // ---------------------------------------------------------------------------------//
  {
    String s = "0000000" + Integer.toBinaryString (b & 0xFF);
    s = s.replaceAll ("0", ".");
    return s.substring (s.length () - 8);
  }

  // ---------------------------------------------------------------------------------//
  public static char byteValue (byte b)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  public static String format4 (int value)
  // ---------------------------------------------------------------------------------//
  {
    if (value < 0)
      return "***err**";

    StringBuilder text = new StringBuilder ();
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

  // ---------------------------------------------------------------------------------//
  public static String format3 (int value)
  // ---------------------------------------------------------------------------------//
  {
    return format4 (value).substring (1);
  }

  // ---------------------------------------------------------------------------------//
  public static String format2 (int value)
  // ---------------------------------------------------------------------------------//
  {
    if (value < 0)
      value += 256;

    return hex[value / 16] + hex[value % 16];
  }

  // ---------------------------------------------------------------------------------//
  public static String format1 (int value)
  // ---------------------------------------------------------------------------------//
  {
    return hex[value];
  }

  // ---------------------------------------------------------------------------------//
  public static String getPascalString (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    int length = buffer[offset] & 0xFF;
    return HexFormatter.getString (buffer, offset + 1, length);
  }

  // ---------------------------------------------------------------------------------//
  public static String getCString (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    int end = offset;
    while (buffer[end] != 0)
      end++;

    return HexFormatter.getString (buffer, offset, end - offset);
  }
}
