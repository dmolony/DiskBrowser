package com.bytezone.diskbrowser.applefile;

// -----------------------------------------------------------------------------------//
public class DoubleScrunch
// -----------------------------------------------------------------------------------//
{
  final byte[][] memory = new byte[2][0x2000];
  private byte[] packedBuffer;

  private final int[] rows = new int[192];
  private int ptr;
  private int bank;
  private int outPtr;
  private int column;

  // ---------------------------------------------------------------------------------//
  public DoubleScrunch (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    int row = 0;

    for (int a = 0x400; a >= 0; a -= 0x400)           // reversed!
      for (int b = 0; b < 0x78; b += 0x28)
        for (int c = 0; c < 0x400; c += 0x80)
          for (int d = 0; d < 0x2000; d += 0x800)
            rows[row++] = a + b + c + d;

    packedBuffer = buffer;

    while (true)
    {
      int repeat = packedBuffer[ptr++] & 0xFF;

      if ((repeat & 0x80) == 0)            // repeat same byte
      {
        byte val = packedBuffer[ptr++];
        while (repeat-- > 0)
          if (move (val))
            return;
      }
      else                              // copy bytes
      {
        repeat &= 0x7F;
        while (repeat-- > 0)
          if (move (packedBuffer[ptr++]))
            return;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private boolean move (byte val)
  // ---------------------------------------------------------------------------------//
  {
    memory[bank][rows[outPtr++] + column] = val;

    if (outPtr % 192 == 0)        // screen column for this bank is finished
    {
      outPtr = 0;
      if (++bank > 1)             // both banks are finished
      {
        bank = 0;
        if (++column >= 40)       // whole screen is finished
          return true;
      }
    }
    return false;
  }
}