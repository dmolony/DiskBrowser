package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// -----------------------------------------------------------------------------------//
class Spell extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private final SpellType spellType;
  private SpellThrown whenCast;
  private final int level;
  private String translation;
  private SpellTarget target;
  private String description;

  public enum SpellType
  {
    MAGE, PRIEST
  };

  public enum SpellTarget
  {
    PERSON, PARTY, MONSTER, MONSTER_GROUP, ALL_MONSTERS, VARIABLE, NONE, CASTER
  };

  public enum SpellThrown
  {
    COMBAT, ANY_TIME, LOOTING, CAMP, COMBAT_OR_CAMP
  };

  private static int lastSpellFound = -1;

  // ---------------------------------------------------------------------------------//
  private Spell (String spellName, SpellType type, int level, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (spellName, buffer);
    this.spellType = type;
    this.level = level;

    if (lastSpellFound + 1 < spellNames.length
        && spellName.equals (spellNames[lastSpellFound + 1]))
      setSpell (++lastSpellFound);
    else
    {
      for (int i = 0; i < spellNames.length; i++)
        if (spellName.equals (spellNames[i]))
        {
          setSpell (i);
          lastSpellFound = i;
          break;
        }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void setSpell (int spellNo)
  // ---------------------------------------------------------------------------------//
  {
    this.translation = translations[spellNo];
    this.description = descriptions[spellNo];
    this.whenCast = when[spellNo];
    this.target = affects[spellNo];
  }

  // ---------------------------------------------------------------------------------//
  public static Spell getSpell (String spellName, SpellType type, int level,
      byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    return new Spell (spellName, type, level, buffer);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getName ()
  // ---------------------------------------------------------------------------------//
  {
    return getName ();
  }

  // ---------------------------------------------------------------------------------//
  public SpellType getType ()
  // ---------------------------------------------------------------------------------//
  {
    return spellType;
  }

  // ---------------------------------------------------------------------------------//
  public int getLevel ()
  // ---------------------------------------------------------------------------------//
  {
    return level;
  }

  // ---------------------------------------------------------------------------------//
  public String getTranslation ()
  // ---------------------------------------------------------------------------------//
  {
    return translation;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return description;
  }

  // ---------------------------------------------------------------------------------//
  public String getWhenCast ()
  // ---------------------------------------------------------------------------------//
  {
    switch (whenCast)
    {
      case COMBAT:
        return "Combat";
      case LOOTING:
        return "Looting";
      case ANY_TIME:
        return "Any time";
      case CAMP:
        return "Camp";
      case COMBAT_OR_CAMP:
        return "Combat or camp";
      default:
        return "?";
    }

  }

  // ---------------------------------------------------------------------------------//
  public String getArea ()
  // ---------------------------------------------------------------------------------//
  {
    switch (target)
    {
      case PERSON:
        return "1 Person";
      case PARTY:
        return "Entire party";
      case MONSTER:
        return "1 Monster";
      case MONSTER_GROUP:
        return "1 Monster group";
      case ALL_MONSTERS:
        return "All monsters";
      case VARIABLE:
        return "Variable";
      case NONE:
        return "None";
      case CASTER:
        return "Caster";
      default:
        return "?";
    }
  }

  // ---------------------------------------------------------------------------------//
  public String toHTMLTable ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("<table border=\"1\"");
    text.append (" width=\"100%\">\n");

    text.append ("  <tr>\n    <td width=\"110\">Spell name</td>\n");
    text.append ("    <td>" + getName () + "</td>\n  </tr>\n");

    text.append ("  <tr>\n    <td>Translation</td>\n");
    text.append ("    <td>" + translation + "</td>\n  </tr>\n");

    text.append ("  <tr>\n    <td>Spell level</td>\n");
    text.append ("    <td>" + level + "</td>\n  </tr>\n");

    text.append ("  <tr>\n    <td>Spell type</td>\n");
    text.append ("    <td>" + getWhenCast () + "</td>\n  </tr>\n");

    text.append ("  <tr>\n    <td>Area of effect</td>\n");
    text.append ("    <td>" + getArea () + "</td>\n  </tr>\n");

    text.append ("  <tr>\n    <td>Description</td>\n");
    text.append ("    <td>" + getText () + "</td>\n  </tr>\n");

    text.append ("</table>");
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (getName ());
    while (text.length () < 14)
      text.append (" ");
    if (spellType == SpellType.PRIEST)
      text.append ("P");
    else
      text.append ("M");
    text.append (level);
    while (text.length () < 20)
      text.append (" ");
    text.append (translation);
    while (text.length () < 40)
      text.append (" ");
    text.append (getArea ());
    while (text.length () < 60)
      text.append (" ");
    text.append (getWhenCast ());
    return text.toString ();
  }

  private static String[] spellNames = { "KALKI", "DIOS", "BADIOS", "MILWA", "PORFIC",
      "MATU", "CALFO", "MANIFO", "MONTINO", "LOMILWA", "DIALKO", "LATUMAPIC", "BAMATU",
      "DIAL", "BADIAL", "LATUMOFIS", "MAPORFIC", "DIALMA", "BADIALMA", "LITOKAN", "KANDI",
      "DI", "BADI", "LORTO", "MADI", "MABADI", "LOKTOFEIT", "MALIKTO", "KADORTO",

      "HALITO", "MOGREF", "KATINO", "DUMAPIC", "DILTO", "SOPIC", "MAHALITO", "MOLITO",
      "MORLIS", "DALTO", "LAHALITO", "MAMORLIS", "MAKANITO", "MADALTO", "LAKANITO",
      "ZILWAN", "MASOPIC", "HAMAN", "MALOR", "MAHAMAN", "TILTOWAIT" };

  private static String[] translations = { "Blessings", "Heal", "Harm", "Light", "Shield",
      "Blessing & zeal", "X-ray vision", "Statue", "Still air", "More light",
      "Softness/supple", "Identification", "Prayer", "Heal (more)", "Hurt (more)",
      "Cure poison", "Shield (big)", "Heal (greatly)", "Hurt (greatly)", "Flame tower",
      "Location", "Life", "Death", "Blades", "Healing", "Harm (incredibly)", "Recall",
      "The Word of Death", "Resurrection",

      "Little Fire", "Body Iron", "Bad Air", "Clarity", "Darkness", "Glass", "Big fire",
      "Spark storm", "Fear", "Blizzard blast", "Flame storm", "Terror", "Deadly air",
      "Frost", "Suffocation", "Dispell", "Big glass", "Change", "Apport", "Great change",
      "(untranslatable)" };

  private static SpellThrown[] when = { SpellThrown.COMBAT, SpellThrown.ANY_TIME,
      SpellThrown.COMBAT, SpellThrown.ANY_TIME, SpellThrown.COMBAT, SpellThrown.COMBAT,
      SpellThrown.LOOTING, SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.ANY_TIME,
      SpellThrown.ANY_TIME, SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.ANY_TIME,
      SpellThrown.COMBAT, SpellThrown.ANY_TIME, SpellThrown.ANY_TIME,
      SpellThrown.ANY_TIME, SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.CAMP,
      SpellThrown.CAMP, SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.ANY_TIME,
      SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.ANY_TIME,

      SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.CAMP,
      SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.COMBAT,
      SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.COMBAT,
      SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.COMBAT,
      SpellThrown.COMBAT, SpellThrown.COMBAT, SpellThrown.COMBAT_OR_CAMP,
      SpellThrown.COMBAT, SpellThrown.COMBAT, };

  private static SpellTarget[] affects = { SpellTarget.PARTY, SpellTarget.PERSON,
      SpellTarget.MONSTER, SpellTarget.PARTY, SpellTarget.CASTER, SpellTarget.PARTY,
      SpellTarget.CASTER, SpellTarget.MONSTER_GROUP, SpellTarget.MONSTER_GROUP,
      SpellTarget.PARTY, SpellTarget.PERSON, SpellTarget.PARTY, SpellTarget.PARTY,
      SpellTarget.PERSON, SpellTarget.MONSTER, SpellTarget.PERSON, SpellTarget.PARTY,
      SpellTarget.PERSON, SpellTarget.MONSTER, SpellTarget.PARTY, SpellTarget.PERSON,
      SpellTarget.PERSON, SpellTarget.MONSTER, SpellTarget.MONSTER_GROUP,
      SpellTarget.PERSON, SpellTarget.MONSTER, SpellTarget.PARTY,
      SpellTarget.MONSTER_GROUP, SpellTarget.PERSON,

      SpellTarget.MONSTER, SpellTarget.CASTER, SpellTarget.MONSTER_GROUP,
      SpellTarget.NONE, SpellTarget.MONSTER_GROUP, SpellTarget.CASTER,
      SpellTarget.MONSTER_GROUP, SpellTarget.MONSTER_GROUP, SpellTarget.MONSTER_GROUP,
      SpellTarget.MONSTER_GROUP, SpellTarget.MONSTER_GROUP, SpellTarget.ALL_MONSTERS,
      SpellTarget.ALL_MONSTERS, SpellTarget.MONSTER_GROUP, SpellTarget.MONSTER_GROUP,
      SpellTarget.MONSTER, SpellTarget.PARTY, SpellTarget.VARIABLE, SpellTarget.PARTY,
      SpellTarget.PARTY, SpellTarget.ALL_MONSTERS };

  private static String[] descriptions = {
      "KALKI reduces the AC of all party members by one, and thus makes"
          + " them harder to hit.",
      "DIOS restores from one to eight hit points of damage from a party"
          + "member. It will not bring dead back to life.",
      "BADIOS causes one to eight hit points of damage to a monster, and"
          + " may kill it. It is the reverse of dios. Note the BA prefix which"
          + " means 'not'.",
      "MILWA causes a softly glowing light to follow the party, allowing"
          + " them to see further into the maze, and also revealing all secret"
          + " doors. See also LOMILWA. This spell lasts only a short while.",
      "PORFIC lowers the AC of the caster considerably. The effects last"
          + " for the duration of combat.",
      "MATU has the same effects as KALKI, but at double the strength.",
      "CALFO allows the caster to determine the exact nature of a trap"
          + " on a chest 95% of the time.",
      "MANIFO causes some of the monsters in a group to become stiff as"
          + " statues for one or more melee rounds. The chance of success,"
          + " and the duration of the effects, depend on the power of the"
          + " target monsters.",
      "MONTINO causes the air around a group of monsters to stop"
          + " transmitting sound. Like MANIFO, only some of the monsters will"
          + " be affected, and for varying lengths of time. Monsters and"
          + " Party members under the influence of this spell cannot cast"
          + " spells, as they cannot utter the spell words!",
      "LOMILWA is a MILWA spell with a much longer life span. Note that"
          + " when this spell, or MILWA are active, the Q option while"
          + " moving through the maze is active. If Q)UICK PLOTTING is on,"
          + " only the square you are in, and the next two squares, will"
          + " plot. Normally you might see five or six squares ahead with"
          + " LOMILWA on. Quick Plotting lets you move fast through known"
          + " areas. Note that it will be turned off when you enter camp or"
          + " combat mode.",
      "DIALKO cures paralysis, and removes the effects of MANIFO and"
          + " KATINO from one member of the party.",
      "LATUMAPIC makes it readily apparent exactly what the opposing"
          + " monsters really are.",
      "BAMATU has the effects of MATU at twice the effectiveness.",
      "DIAL restores two to 16 hit points of damage, and is similar to" + " DIOS.",
      "BADIAL causes two to 16 hit points of damage in the same way as" + " BADIOS.",
      "LATUMOFIS makes a poisoned person whole and fit again. Note that"
          + " poison causes a person to lose hit points steadily during"
          + " movement and combat.",
      "MAPORFIC is an improved PORFIC, with effects that last for the"
          + " entire expedition.",
      "DIALMA restores three to 24 hit points.",
      "BADIALMA causes three to 24 hit points of damage.",
      "LITOKAN causes a pillar of flame to strike a group of monsters,"
          + " doing three to 24 hits of damage to each. However, as with"
          + " many spells that affect entire groups, there is a chance that"
          + " individual monsters will be able to avoid or minimise its"
          + " effects. And some monsters will be resistant to it.",
      "KANDI allows the user to locate characters in the maze. It tells on"
          + " which level, and in which rough area the dead one can be found.",
      "DI causes a dead person to be resurrected. However, the renewed"
          + " character has but one hit point. Also, this spell is not as"
          + " effective or as safe as using the Temple.",
      "BADI gives the affected monster a coronary attack. It may or may"
          + " not cause death to occur.",
      "LORTO causes sharp blades to slice through a group, causing six to"
          + " 36 points of damage.",
      "MADI causes all hit points to be restored and cures any condition" + " but death.",
      "MABADI causes all but one to eight hit points to be removed from" + " the target.",
      "LOKTOFEIT causes all party members to be teleported back to the"
          + " castle, minus all their equipment and most of their gold. There"
          + " is also a good chance this spell will not function.",
      "MALIKTO causes 12 to 72 hit points of damage to all monsters. None"
          + " can escape or minimise its effects.",
      "KADORTO restores the dead to life as does DI, but also restores all"
          + " hit points. However, it has the same drawbacks as the DI spell."
          + " KADORTO can be used to resurrect people even if they are ashes.",

      "HALITO causes a flame ball the size of a baseball to hit a monster,"
          + " doing from one to eight points of damage.",
      "MOGREF reduces the caster's AC by two. The effect lasts the entire"
          + " encounter.",
      "KATINO causes most of the monsters in a group to fall asleep."
          + " Katino only effects normal, animal or humanoid monsters. The"
          + " chance of the spell affecting an individual monster, and the"
          + " duration of the effect, is inversely proportional to the power"
          + " of the monster. While asleep, monsters are easier to hit and"
          + " successful strikes do double damage.",
      "DUMAPIC informs you of the party's exact displacement from the"
          + " stairs to the castle, vertically, and North and East, and also"
          + " tells you what direction you are facing.",

      "DILTO causes one group of monsters to be enveloped in darkness,"
          + " which reduces their ability to defend against your attacks.",
      "SOPIC causes the caster to become transparent. This means that"
          + " he is harder to see, and thus his AC is reduced by four.",

      "MAHALITO causes a fiery explosion in a monster group, doing four"
          + " to 24 hit points of damage. As with other similar spells,"
          + " monsters may be able to minimise the damage done.",
      "MOLITO causes sparks to fly out and damage about half of the"
          + " monsters in a group. Three to 18 hit points of damage are done"
          + " with no chance of avoiding the sparks.",
      "MORLIS causes one group of monsters to fear the party greatly. The"
          + " effects are the same as a double strength DILTO spell.",
      "DALTO is similar to MAHALITO except that cold replaces flames."
          + " Also, six to 36 hit points of damage are done.",
      "LAHALITO is an improved MAHALITO, doing the same damage as DALTO.",
      "MAMORLIS is similar to MORLIS, except that all monster groups are" + " affected.",
      "Any monsters of less than eigth level (i.e. about 35-40 hit points)"
          + " are killed by this spell outright.",
      "An improved DALTO causing eight to 64 hit points of damage.",
      "All monsters in the group affected by this spell die. Of course,"
          + " there is a chance that some of the monsters will not be affected.",
      "This spell will destroy any one monster that is of the Undead" + " variety",
      "This spell duplicates the effects of SOPIC for the entire party.",
      "This spell is indeed terrible, and may backfire on the caster."
          + " First, to even cast it, you must be of the thirteenth level or"
          + " higher, and casting it will cost you one level of experience."
          + " The effects of HAMAN are random, and usually help the party.",
      "This spell's effects depend on the situation the party is in when it"
          + " is cast.Basically, MALOR will teleport the entire party from one"
          + " location to another. When used in melee, the teleport is random,"
          + " but when used in camp, where there is more chance for concentration"
          + ", it can be used to move the party anywhere in the maze. Be warned,"
          + " however, that if you teleport outside of the maze, or into an"
          + " area that is solid rock, you will be lost forever, so this spell"
          + " is to be used with the greatest of care. Combat use of MALOR will"
          + " never put you outside of the maze, but it may move you deeper in,"
          + " so it should be used only in panic situations.",
      "The same restrictions and qualifications apply to this spell as do"
          + " to HAMAN. However, the effects are even greater. Generally these"
          + " spells are only used when there is no other hope for survival.",
      "The effect of this spell can be described as similar to that of a"
          + " nuclear fusion explosion. Luckily the party is shielded from its"
          + " effects. Unluckily (for them) the monsters are not. This spell"
          + " will do from 10-100 hit points of damage." };
}