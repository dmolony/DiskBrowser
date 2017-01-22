package com.bytezone.diskbrowser.applefile;

public class SimpleText extends AbstractFile
{

  public SimpleText (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Name : " + name + "\n");
    text.append (String.format ("End of file   : %,8d%n%n", buffer.length));

    int ptr = 0;
    while (ptr < buffer.length && buffer[ptr] != 0x00)
    {
      String line = getLine (ptr);
      text.append (line + "\n");
      ptr += line.length () + 1;
      if (ptr < buffer.length && buffer[ptr] == 0x0A)
        ptr++;
    }
    return text.toString ();
  }

  private String getLine (int ptr)
  {
    StringBuilder line = new StringBuilder ();

    // added check for 0x00 eol 17/01/17
    while (ptr < buffer.length && buffer[ptr] != 0x0D && buffer[ptr] != 0x00)
      line.append ((char) buffer[ptr++]);

    return line.toString ();
  }

  public static boolean isHTML (byte[] buffer)
  {
    String text = new String (buffer, 0, buffer.length);
    if (text.indexOf ("HTML") > 0 || text.indexOf ("html") > 0)
      return true;
    return false;
  }
}