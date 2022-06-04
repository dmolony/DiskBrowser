package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class MonsterV4 extends Monster
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  MonsterV4 (String[] names, byte[] buffer, int id)
  // ---------------------------------------------------------------------------------//
  {
    super (names[2], buffer);

    this.monsterID = id;

    genericName = names[0];
    genericNamePlural = names[1];
    namePlural = names[3];

    groupSize = new Dice (buffer, 1);
    hitPoints = new Dice (buffer, 7);
    type = Utility.getShort (buffer, 13);
    armourClass = Utility.signedShort (buffer, 15);

    recsn = buffer[17];                               // number of dice
    for (int i = 0, ptr = 19; i < 7; i++, ptr += 6)
    {
      if (buffer[ptr] == 0)
        break;
      damage.add (new Dice (buffer, ptr));
    }

    levelDrain = Utility.getShort (buffer, 61);
    healPts = Utility.getShort (buffer, 63);
    mageSpellLevel = Utility.getShort (buffer, 65);
    priestSpellLevel = Utility.getShort (buffer, 67);
    breathe = Utility.getShort (buffer, 69);
    unaffect = Utility.getShort (buffer, 71);

    resistance = Utility.getShort (buffer, 73);     // bit flags
    abilities = Utility.getShort (buffer, 75);      // bit flags
  }
}
