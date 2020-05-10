package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class Selector extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  int numRunListEntries;
  int numOtherRunListEntries;
  List<Entry> entries = new ArrayList<> ();
  List<Path> paths = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public Selector (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    numRunListEntries = buffer[0] & 0xFF;
    numOtherRunListEntries = buffer[1] & 0xFF;

    int ptr = 2;
    for (int i = 0; i < 24; i++)
    {
      entries.add (new Entry (buffer, ptr));
      ptr += 16;
    }

    ptr = 386;
    for (int i = 0; i < 24; i++)
    {
      paths.add (new Path (buffer, ptr));
      ptr += 64;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Name    : " + name + "\n");
    text.append (String.format ("Length  : $%04X (%<,d)%n%n", buffer.length));
    text.append (String.format ("NumRunListEntries ....... %d%n", buffer[0]));
    text.append (String.format ("NumOtherRunListEntries .. %d%n%n", buffer[1]));
    text.append ("Name             Copy         Path\n");
    text.append ("--------------   ----------   ------------------------------------\n");

    for (int i = 0; i < entries.size (); i++)
    {
      Entry entry = entries.get (i);
      Path path = paths.get (i);

      if (entry.labelLength > 0)
        text.append (String.format ("%-14s   %-10s   %s%n", entry.label, entry.copyText,
            path.pathName));
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  class Entry
  // ---------------------------------------------------------------------------------//
  {
    int labelLength;
    String label;
    byte copyFlags;
    String copyText;

    public Entry (byte[] buffer, int ptr)
    {
      labelLength = buffer[ptr] & 0xFF;
      label = new String (buffer, ptr + 1, labelLength);
      copyFlags = buffer[ptr + 15];
      switch (copyFlags & 0xFF)
      {
        case 0:
          copyText = "First boot";
          break;
        case 0x80:
          copyText = "First use";
          break;
        case 0xC0:
          copyText = "Never";
          break;
        default:
          copyText = "Unknown";
          break;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  class Path
  // ---------------------------------------------------------------------------------//
  {
    int length;
    String pathName;

    public Path (byte[] buffer, int ptr)
    {
      length = buffer[ptr] & 0xFF;
      pathName = new String (buffer, ptr + 1, length);
    }
  }
}
