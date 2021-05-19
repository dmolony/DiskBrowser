package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
class ExperienceLevel extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private final long[] expLevels = new long[13];

  // ---------------------------------------------------------------------------------//
  ExperienceLevel (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    int seq = 0;

    for (int ptr = 0; ptr < buffer.length; ptr += 6)
    {
      if (buffer[ptr] == 0)
        break;

      long points =
          Utility.getShort (buffer, ptr) + Utility.getShort (buffer, ptr + 2) * 10000
              + Utility.getShort (buffer, ptr + 4) * 100000000L;
      expLevels[seq++] = points;
    }
  }

  // ---------------------------------------------------------------------------------//
  long getExperiencePoints (int level)
  // ---------------------------------------------------------------------------------//
  {
    if (level < 13)
      return expLevels[level];
    return (level - 12) * expLevels[0] + expLevels[12];
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder line = new StringBuilder ();
    for (long exp : expLevels)
      line.append (exp + "\n");
    return line.toString ();
  }
}