package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class CharacterV4 extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  int id;
  int nextCharacterId;
  CharacterParty party;

  // ---------------------------------------------------------------------------------//
  CharacterV4 (String name, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.id = id;
    nextCharacterId = Utility.getShort (buffer, 0x7D);
  }

  // ---------------------------------------------------------------------------------//
  void setParty (CharacterParty party)
  // ---------------------------------------------------------------------------------//
  {
    this.party = party;
  }

  // ---------------------------------------------------------------------------------//
  boolean isInParty ()
  // ---------------------------------------------------------------------------------//
  {
    return party != null;
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
    //        text.append (String.format ("Slogan ......... %s%n", slogan));
    text.append (String.format ("Next ........... %d%n%n", nextCharacterId));

    text.append (HexFormatter.format (buffer, 1, buffer[0] & 0xFF));
    text.append ("\n\n");

    if (!party.slogan.isEmpty () || party.characters.size () > 1)
      text.append (party);

    return text.toString ();
  }
}
