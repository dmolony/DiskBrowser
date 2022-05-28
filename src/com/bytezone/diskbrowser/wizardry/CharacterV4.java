package com.bytezone.diskbrowser.wizardry;

import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class CharacterV4 extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  int id;
  int nextCharacter;
  String slogan = "";

  // ---------------------------------------------------------------------------------//
  CharacterV4 (String name, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.id = id;
    nextCharacter = Utility.getShort (buffer, 0x7D);
  }

  // ---------------------------------------------------------------------------------//
  void link (List<CharacterV4> characters)
  // ---------------------------------------------------------------------------------//
  {
    String text = getPartialSlogan ();
    int nextCharacterId = this.nextCharacter;

    while (nextCharacterId > 0 && nextCharacterId != id)
    {
      CharacterV4 next = characters.get (nextCharacterId);
      if (!next.slogan.isEmpty ())
        return;
      text += next.getPartialSlogan ();
      nextCharacterId = next.nextCharacter;
    }

    slogan = text.replace ("\\", " - ");
  }

  // ---------------------------------------------------------------------------------//
  String getPartialSlogan ()
  // ---------------------------------------------------------------------------------//
  {
    return buffer[17] == 0 ? "" : HexFormatter.getPascalString (buffer, 17);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Id ............. %3d%n", id));
    text.append (String.format ("Name ........... %s%n", name));
    text.append (String.format ("Slogan ......... %s%n", slogan));
    text.append (String.format ("Next ........... %d%n%n", nextCharacter));

    text.append (HexFormatter.format (buffer, 1, buffer[0] & 0xFF));

    return text.toString ();
  }
}
