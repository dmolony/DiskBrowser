package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class FileSystemTranslator extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private final String text1;
  private final String text2;
  private final String text3;
  private final String text4;

  // ---------------------------------------------------------------------------------//
  public FileSystemTranslator (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    text1 = HexFormatter.getPascalString (buffer, 0x36);
    text2 = HexFormatter.getPascalString (buffer, 0xFC);

    int len1 = buffer[0xFC] & 0xFF;
    text3 = HexFormatter.getPascalString (buffer, 0xFC + len1 + 1);
    int len2 = buffer[0xFC + len1 + 1] & 0xFF;
    text4 = HexFormatter.getPascalString (buffer, 0xFC + len1 + len2 + 4);

    SegmentHeader segmentHeader = new SegmentHeader (buffer, 0);
    System.out.println (segmentHeader);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");

    text.append ("File System Translator\n\n");
    text.append (String.format ("Text 1 ....... %s%n", text1));
    text.append (String.format ("Text 2 ....... %s%n", text2));
    text.append (String.format ("Text 3 ....... %s%n", text3));
    text.append (String.format ("Text 4 ....... %s%n", text4));
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}