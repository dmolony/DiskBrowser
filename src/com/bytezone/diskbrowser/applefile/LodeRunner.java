package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class LodeRunner extends AbstractFile
{
  public LodeRunner (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();
    text.append ("Lode Runner Level\n\n");

    for (int level = 0; level < 150; level++)
    {
      int ptr = level * 256 + 226;
      String levelName = "";
      if (buffer[ptr] != 0 && buffer[ptr] != (byte) 0xFF)
        levelName = HexFormatter.sanitiseString (buffer, ptr, 15);
      text.append (String.format ("Level %d  %s%n%n", level + 1, levelName));

      ptr = 0;
      for (int i = level * 256, max = i + 224; i < max; i++)
      {
        String val = String.format ("%02X", buffer[i]);
        text = addPosition (text, val.charAt (0));
        text.append (' ');
        text = addPosition (text, val.charAt (1));
        text.append (' ');
        if (++ptr % 14 == 0)
          text.append ("\n");
      }
      text.append ("\n\n");
    }

    return text.toString ();
  }

  private StringBuilder addPosition (StringBuilder text, char c)
  {
    switch (c)
    {
      case '0':
        text.append (' ');          // space
        break;

      case '1':
        text.append ('-');          // diggable floor
        break;

      case '2':
        text.append ('=');          // undiggable floor
        break;

      case '3':
        text.append ('+');          // ladder
        break;

      case '4':
        text.append ('^');          // hand over hand bar
        break;

      case '5':
        text.append ('~');          // trap door
        break;

      case '6':
        text.append ('#');          // hidden ladder
        break;

      case '7':
        text.append ('$');          // gold
        break;

      case '8':
        text.append ('*');          // enemy
        break;

      case '9':
        text.append ('x');          // player
        break;

      default:
        text.append (c);
    }

    return text;
  }
}