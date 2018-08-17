package com.bytezone.diskbrowser.nib;

public class ByteTranslator5and3 extends ByteTranslator
{
  // 32 valid bytes that can be stored on a disk (plus 0xAA and 0xD5)
  private static byte[] writeTranslateTable5and3 =
      { (byte) 0xAB, (byte) 0xAD, (byte) 0xAE, (byte) 0xAF, (byte) 0xB5, (byte) 0xB6,
        (byte) 0xB7, (byte) 0xBA, (byte) 0xBB, (byte) 0xBD, (byte) 0xBE, (byte) 0xBF,
        (byte) 0xD6, (byte) 0xD7, (byte) 0xDA, (byte) 0xDB, //
        (byte) 0xDD, (byte) 0xDE, (byte) 0xDF, (byte) 0xEA, (byte) 0xEB, (byte) 0xED,
        (byte) 0xEE, (byte) 0xEF, (byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xFA,
        (byte) 0xFB, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF };

  private static byte[] readTranslateTable5and3 = new byte[85];   // skip first 171 blanks
  private static boolean debug = false;

  static
  {
    for (int i = 0; i < writeTranslateTable5and3.length; i++)
    {
      int j = (writeTranslateTable5and3[i] & 0xFF) - 0xAB;   // skip first 171 blanks
      readTranslateTable5and3[j] = (byte) (i + 1);           // offset by 1 to avoid zero
      if (debug)
        System.out.printf ("%02X  %02X  %02X%n", i, writeTranslateTable5and3[i], j);
    }
    if (debug)
      for (int j = 0; j < readTranslateTable5and3.length; j++)
      {
        int target = readTranslateTable5and3[j] - 1;
        if (target >= 0)
        {
          int value = writeTranslateTable5and3[target] & 0xFF;
          System.out.printf ("%02X -> %02X%n", j, value);
        }
        else
          System.out.printf ("%02X%n", j);
      }
  }

  // ---------------------------------------------------------------------------------//
  // encode
  // ---------------------------------------------------------------------------------//

  @Override
  byte encode (byte b)
  {
    System.out.println ("encode() not written");
    return 0;
  }

  // ---------------------------------------------------------------------------------//
  // decode
  // ---------------------------------------------------------------------------------//

  @Override
  byte decode (byte b) throws DiskNibbleException
  {
    int val = (b & 0xFF) - 0xAB;                              // 0 - 84
    if (val < 0 || val > 84)
      throw new DiskNibbleException ("5&3 val: " + val);
    byte trans = (byte) (readTranslateTable5and3[val] - 1);   // 0 - 31  (5 bits)
    if (trans < 0 || trans > 31)
      throw new DiskNibbleException ("5&3 trans: " + trans);
    return trans;
  }
}
