package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class SegmentDictionary
// -----------------------------------------------------------------------------------//
{
  private final boolean isValid;
  private int[] codeAddress = new int[16];
  private int[] codeLength = new int[16];
  private String[] segName = new String[16];

  // ---------------------------------------------------------------------------------//
  public SegmentDictionary (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    isValid = !name.equals ("SYSTEM.INTERP");       // temporary

    int ptr = 0;
    for (int seg = 0; seg < 16; seg++)
    {
      codeAddress[seg] = Utility.getShort (buffer, ptr);
      ptr += 2;
      codeLength[seg] = Utility.getShort (buffer, ptr);
      ptr += 2;
    }

    ptr = 64;
    for (int seg = 0; seg < 16; seg++)
    {
      segName[seg] = new String (buffer, ptr, 8);
      ptr += 8;
    }

    //    for (int seg = 0; seg < 16; seg++)
    //      System.out.printf ("%04X  %04X  %s%n", codeAddress[seg], codeLength[seg], segName[seg]);
  }

  // ---------------------------------------------------------------------------------//
  public boolean isValid ()
  // ---------------------------------------------------------------------------------//
  {
    return isValid;
  }
}