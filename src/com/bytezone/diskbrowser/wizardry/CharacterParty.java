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
    text.append ("\n");

    for (CharacterV4 character : characters)
      text.append (String.format ("  %3d  %s%n", character.id, character.getName ()));

    return text.toString ();
  }
}
