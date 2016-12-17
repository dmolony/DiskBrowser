package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

public class FontFile extends AbstractFile
{
  List<Character> characters = new ArrayList<Character> ();

  public FontFile (String name, byte[] buffer)
  {
    super (name, buffer);

    int ptr = 0;
    while (ptr < buffer.length)
    {
      Character c = new Character (buffer, ptr);
      ptr += 8;
      characters.add (c);
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");

    for (int i = 0; i < characters.size (); i += 8)
    {
      StringBuilder line = new StringBuilder ();
      for (int j = 0; j < 8; j++)
      {
        for (int k = 0; k < 8; k++)
        {
          line.append (characters.get (i + k).lines[j]);
          line.append ("    ");
        }
        line.append ("\n");
      }

      text.append (line.toString ());
      text.append ("\n");
    }

    return text.toString ();
  }

  class Character
  {
    String[] lines = new String[8];

    public Character (byte[] buffer, int ptr)
    {
      for (int i = 0; i < 8; i++)
      {
        int b = buffer[ptr + i] & 0xFF;
        String s = "0000000" + Integer.toString (b, 2);
        s = s.substring (s.length () - 7);
        s = s.replace ('0', ' ');
        s = s.replace ('1', 'O');
        s = new StringBuilder (s).reverse ().toString ();
        lines[i] = s;
      }
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      for (String s : lines)
        text.append (s + "\n");
      text.deleteCharAt (text.length () - 1);

      return text.toString ();
    }
  }
}