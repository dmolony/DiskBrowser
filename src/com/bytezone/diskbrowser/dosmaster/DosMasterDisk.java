package com.bytezone.diskbrowser.dosmaster;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class DosMasterDisk
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public static boolean isDos33 (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    int slots = 3 * 16 + 8;
    int v0 = slots + 8;
    int size = v0 + 16;
    int vsiz = size + 8;
    //    int adrs = vsiz + 8;

    System.out.println (
        "Slots  v0   size  vsiz    d    d0   slot  ptr    st      sz      v      num");

    StringBuilder text = new StringBuilder ();

    for (int d = 0; d < 8; d++)
    {
      int d0 = d / 2 * 2;
      int s = buffer[slots + d] & 0xFF;

      if (s == 0)
        continue;

      System.out.printf (" %02X    %02X    %02X    %02X    %02X    %02X    %02X", slots,
          v0, size, vsiz, d, d0, s);

      int dr = 0;
      if (s > 127)
      {
        s -= 128;
        dr = 1;
      }

      text.append (String.format ("Slot %d, Drive %d has", s / 16, dr + 1));

      int ptr = v0 + 2 * d0 + 2 * dr;
      int st = Utility.unsignedShort (buffer, ptr);
      int sz = Utility.unsignedShort (buffer, vsiz + d0);
      int v = Utility.unsignedShort (buffer, size + d0);

      if (st > v)
        st -= 16 * 4096;

      int num = (v - st) / sz - 1;

      text.append (String.format (" %d volumes of %d sectors%n", num, sz * 2));

      System.out.printf ("   %02X    %04X    %04X    %04X    %04X%n", ptr, st, sz, v,
          num);
    }

    System.out.println ();
    System.out.println (text.toString ());

    return false;
  }
}
