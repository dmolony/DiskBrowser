package com.bytezone.diskbrowser.applefile;

public class PascalText extends AbstractFile
{
  public PascalText (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder (getHeader ());

    int ptr = 0x400;
    while (ptr < buffer.length)
    {
      if (buffer[ptr] == 0x00)
      {
        ++ptr;
        continue;
      }
      if (buffer[ptr] == 0x10)
      {
        int tab = buffer[ptr + 1] - 0x20;
        while (tab-- > 0)
          text.append (" ");
        ptr += 2;
      }
      String line = getLine (ptr);
      text.append (line + "\n");
      ptr += line.length () + 1;
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  private String getHeader ()
  {
    return "Name : " + name + "\n\n";
  }

  private String getLine (int ptr)
  {
    StringBuilder line = new StringBuilder ();
    while (buffer[ptr] != 0x0D)
      line.append ((char) buffer[ptr++]);
    return line.toString ();
  }
}