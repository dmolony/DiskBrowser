package com.bytezone.diskbrowser.applefile;

// -----------------------------------------------------------------------------------//
public class MagicWindowText extends AbstractFile
// -----------------------------------------------------------------------------------//
{

  // ---------------------------------------------------------------------------------//
  public MagicWindowText (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Name : " + name + "\n");
    text.append (String.format ("End of file   : %,8d%n%n", buffer.length));

    int ptr = 0x100;
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

  // ---------------------------------------------------------------------------------//
  private String getLine (int ptr)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = new StringBuilder ();

    // added check for 0x00 eol 17/01/17
    while (ptr < buffer.length && buffer[ptr] != (byte) 0x8D && buffer[ptr] != 0x00)
    {
      line.append ((char) (buffer[ptr++] & 0x7F));
    }

    return line.toString ();
  }
}
