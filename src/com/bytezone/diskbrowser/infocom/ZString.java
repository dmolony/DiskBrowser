package com.bytezone.diskbrowser.infocom;

// -----------------------------------------------------------------------------------//
class ZString
// -----------------------------------------------------------------------------------//
{
  private static String[] letters =
      { "      abcdefghijklmnopqrstuvwxyz", "      ABCDEFGHIJKLMNOPQRSTUVWXYZ",
        "        0123456789.,!?_#\'\"/\\-:()" };
  String value;
  Header header;
  int startPtr;
  int length;

  // ---------------------------------------------------------------------------------//
  ZString (Header header, int offset)
  // ---------------------------------------------------------------------------------//
  {
    ZStringBuilder text = new ZStringBuilder ();
    this.header = header;
    this.startPtr = offset;

    while (true)
    {
      if (offset >= header.buffer.length - 1)
      {
        System.out.println ("********" + text.toString ());
        break;
      }

      // get the next two bytes
      int val = header.getWord (offset);

      // process each zChar as a 5-bit value
      text.processZChar ((byte) ((val >>> 10) & 0x1F));
      text.processZChar ((byte) ((val >>> 5) & 0x1F));
      text.processZChar ((byte) (val & 0x1F));

      if ((val & 0x8000) != 0)               // bit 15 = finished flag
      {
        length = offset - startPtr + 2;
        value = text.toString ();
        break;
      }
      offset += 2;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return value;
  }

  // ---------------------------------------------------------------------------------//
  private class ZStringBuilder
  // ---------------------------------------------------------------------------------//
  {
    int alphabet;
    boolean shift;
    int shiftAlphabet;
    int synonym;
    int buildingLevel;
    int builtLetter;
    StringBuilder text = new StringBuilder ();

    private void processZChar (byte zchar)
    {
      // A flag to indicate that we are building a character not in the alphabet. The
      // value indicates which half of the character the current zchar goes into. Once
      // both halves are full, we use the ascii value in builtLetter.
      if (buildingLevel > 0)
      {
        builtLetter = (short) ((builtLetter << 5) | zchar);
        if (++buildingLevel == 3)
        {
          text.append ((char) builtLetter);
          buildingLevel = 0;
        }
        return;
      }

      // A flag to indicate that we need to insert an abbreviation. The synonym value
      // (1-3) indicates which abbreviation block to use, and the current zchar is the
      // offset within that block.
      if (synonym > 0)
      {
        text.append (header.getAbbreviation ((synonym - 1) * 32 + zchar));
        synonym = 0;
        return;
      }

      if ((shift && shiftAlphabet == 2) || (!shift && alphabet == 2))
      {
        if (zchar == 6)
        {
          buildingLevel = 1;
          builtLetter = 0;
          shift = false;
          return;
        }
        if (zchar == 7)
        {
          text.append ("\n");
          shift = false;
          return;
        }
      }

      // zChar values 0-5 have special meanings, and 6-7 are special only in alphabet #2.
      // Otherwise it's just a straight lookup into the current alphabet.
      switch (zchar)
      {
        case 0:
          text.append (" ");
          shift = false;
          return;

        case 1:
          synonym = zchar;
          return;

        case 2:
        case 3:
          if (header.version >= 3)
          {
            synonym = zchar;
            return;
          }
          // version 1 or 2
          shiftAlphabet = (alphabet + zchar - 1) % 3;
          shift = true;
          return;

        case 4:
        case 5:
          if (header.version >= 3)                    // shift key
          {
            shiftAlphabet = zchar - 3;
            shift = true;
          }
          else                                        // shift lock key
            alphabet = (alphabet + zchar - 3) % 3;
          return;

        default:
          if (shift)
          {
            text.append (letters[shiftAlphabet].charAt (zchar));
            shift = false;
          }
          else
            text.append (letters[alphabet].charAt (zchar));
          return;
      }
    }

    @Override
    public String toString ()
    {
      return text.toString ();
    }
  }
}