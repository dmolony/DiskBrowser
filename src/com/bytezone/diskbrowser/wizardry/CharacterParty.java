package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class CharacterParty
// -----------------------------------------------------------------------------------//
{
  List<CharacterV4> characters = new ArrayList<> ();
  String slogan = "";

  // ---------------------------------------------------------------------------------//
  void add (CharacterV4 character)
  // ---------------------------------------------------------------------------------//
  {
    characters.add (character);
    slogan += character.getPartialSlogan ();
    character.setParty (this);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (slogan.replace ("\\", " - "));
    text.append ("\n\n");

    for (CharacterV4 character : characters)
      text.append (
          String.format ("  %3d  %-15s %17s  %13s  %13s%n", character.id, character.getName (),
              character.getAttributeString (), character.getSpellsString (CharacterV4.MAGE_SPELLS),
              character.getSpellsString (CharacterV4.PRIEST_SPELLS)));

    return text.toString ();
  }
}
