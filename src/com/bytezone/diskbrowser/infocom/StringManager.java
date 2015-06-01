package com.bytezone.diskbrowser.infocom;

import java.util.Map;
import java.util.TreeMap;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.applefile.AbstractFile;

class StringManager extends AbstractFile
{
  Header header;
  Map<Integer, ZString> strings = new TreeMap<Integer, ZString> ();

  public StringManager (String name, byte[] buffer, Header header)
  {
    super (name, buffer);
    this.header = header;

    int ptr = header.stringPointer;
    int max = header.fileLength;
    while (ptr < max)
    {
      ZString zs = new ZString (buffer, ptr, header);
      if (zs.value == null)
        break; // used when eof not known or correct - fix!!
      strings.put (ptr, zs);
      ptr += zs.length;
    }
  }

  public boolean containsStringAt (int address)
  {
    return strings.containsKey (address);
  }

  public String stringAt (int address)
  {
    if (strings.containsKey (address))
      return strings.get (address).value;
    return "String not found at : " + address;
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();
    int count = 0;
    text.append ("  #  Start  String\n");
    text.append ("---  -----  --------------------------------------------------------"
          + "-------------------\n");

    for (ZString s : strings.values ())
    {
      String s2 = s.value.replace ("\n", "\n             ");
      text.append (String.format ("%3d  %05X  \"%s\"%n", ++count, s.startPtr, s2));
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  @Override
  public String getHexDump ()
  {
    int size = header.fileLength - header.stringPointer;
    return HexFormatter.format (buffer, header.stringPointer, size);
  }
}