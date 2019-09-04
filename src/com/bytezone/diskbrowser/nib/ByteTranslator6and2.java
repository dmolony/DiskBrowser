package com.bytezone.diskbrowser.nib;

// -----------------------------------------------------------------------------------//
class ByteTranslator6and2 implements ByteTranslator
// -----------------------------------------------------------------------------------//
{
  // 64 valid bytes that can be stored on a disk (plus 0xAA and 0xD5)
  private static byte[] writeTranslateTable6and2 =
      { (byte) 0x96, (byte) 0x97, (byte) 0x9A, (byte) 0x9B, (byte) 0x9D, (byte) 0x9E,
        (byte) 0x9F, (byte) 0xA6, (byte) 0xA7, (byte) 0xAB, (byte) 0xAC, (byte) 0xAD,
        (byte) 0xAE, (byte) 0xAF, (byte) 0xB2, (byte) 0xB3, //
        (byte) 0xB4, (byte) 0xB5, (byte) 0xB6, (byte) 0xB7, (byte) 0xB9, (byte) 0xBA,
        (byte) 0xBB, (byte) 0xBC, (byte) 0xBD, (byte) 0xBE, (byte) 0xBF, (byte) 0xCB,
        (byte) 0xCD, (byte) 0xCE, (byte) 0xCF, (byte) 0xD3, //
        (byte) 0xD6, (byte) 0xD7, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB, (byte) 0xDC,
        (byte) 0xDD, (byte) 0xDE, (byte) 0xDF, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7,
        (byte) 0xE9, (byte) 0xEA, (byte) 0xEB, (byte) 0xEC, //
        (byte) 0xED, (byte) 0xEE, (byte) 0xEF, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4,
        (byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xF9, (byte) 0xFA, (byte) 0xFB,
        (byte) 0xFC, (byte) 0xFD, (byte) 0xFE, (byte) 0xFF };

  private static final int SKIP = 0x96;
  private static byte[] readTranslateTable6and2 = new byte[256 - SKIP];

  static
  {
    for (int i = 0; i < writeTranslateTable6and2.length; i++)
    {
      int j = (writeTranslateTable6and2[i] & 0xFF) - SKIP;   // skip first 150 blanks
      readTranslateTable6and2[j] = (byte) (i + 1);           // offset by 1 to avoid zero
    }

    if (false)
      for (int i = 0; i < readTranslateTable6and2.length; i++)
        System.out.printf ("%02X  %02X%n", i + SKIP, readTranslateTable6and2[i] - 1);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte encode (byte b)
  // ---------------------------------------------------------------------------------//
  {
    return writeTranslateTable6and2[(b & 0xFC)];
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public byte decode (byte b) throws DiskNibbleException
  // ---------------------------------------------------------------------------------//
  {
    int val = (b & 0xFF) - SKIP;                              // 0 - 105
    if (val < 0 || val > 105)
      throw new DiskNibbleException ("6&2 val: " + val);
    byte trans = (byte) (readTranslateTable6and2[val] - 1);   // 0 - 63  (6 bits)
    if (trans < 0 || trans > 63)
      throw new DiskNibbleException ("6&2 trans: " + trans);
    return trans;
  }
}
