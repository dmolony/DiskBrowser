package com.bytezone.diskbrowser.applefile;

public class CPMTextFile extends AbstractFile
{

  public CPMTextFile (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Name : " + name + "\n\n");

    int ptr = 0;
    while (ptr < buffer.length && buffer[ptr] != (byte) 0x1A)
    {
      String line = getLine (ptr);
      text.append (line + "\n");
      ptr += line.length () + 2;
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  private String getLine (int ptr)
  {
    StringBuilder line = new StringBuilder ();

    int max = buffer.length - 1;
    while (ptr < max && buffer[ptr] != 0x0D && buffer[ptr + 1] != 0x0A)
      line.append ((char) buffer[ptr++]);

    return line.toString ();
  }
}