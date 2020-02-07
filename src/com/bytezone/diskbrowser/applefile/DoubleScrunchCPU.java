package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.CPU;

// -----------------------------------------------------------------------------------//
public class DoubleScrunchCPU extends CPU
// -----------------------------------------------------------------------------------//
{
  private byte mem_71D0;        // varies 1/0/1/0 ...
  private byte mem_71D2;        // column index * 2 (00-4F) increments every C0 bytes
  private byte mem_71D3;        // byte to repeat
  private byte mem_71D4;        // repetition counter

  private int src;              // base address of packed buffer
  private int dst;              // base address of main/aux memory

  private byte zp_00;           // input buffer offset
  private byte zp_01;

  private byte zp_26;           // output buffer offset
  private byte zp_27;

  private byte zp_E6;           // hi-res page ($20 or $40)

  final byte[][] memory = new byte[2][0x2000];
  private byte[] packedBuffer;

  private int count;

  // ---------------------------------------------------------------------------------//
  void unscrunch (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    packedBuffer = buffer;

    src = 0x4000;           // packed picture data
    dst = 0x2000;           // main/aux picture data

    zp_00 = 0x00;           // load address of packed buffer
    zp_01 = 0x40;

    zp_E6 = 0x20;           // hi-res page 1

    lda (zp_E6);
    ora ((byte) 0x04);
    zp_27 = sta ();

    ldx ((byte) 0x01);
    mem_71D0 = stx ();      // X and 71D0 are both set to 1

    ldy ((byte) 0x00);
    mem_71D2 = sty ();      // column index = 0
    zp_26 = sty ();         // output index = 0

    while (true)
    {
      // get the repetition counter (hi-bit is a flag to indicate copy/repeat)
      lda (packedBuffer, indirectY (src, zp_00, zp_01));        // LDA ($00),Y)

      php ();

      // increment address to get next transfer byte (done in copy/repeat routine)
      zp_00 = inc (zp_00);
      if (zero)
        zp_01 = inc (zp_01);

      and ((byte) 0x7F);            // remove copy/repeat flag
      mem_71D4 = sta ();            // save repetition counter (RC)
      plp ();

      // copy or repeat RC bytes
      if (negative ? copyBytes () : repeatBytes ())
        break;

      // increment address to get next repetition counter
      zp_00 = inc (zp_00);
      if (zero)
        zp_01 = inc (zp_01);
    }
  }

  // copy a single byte RC times
  // ---------------------------------------------------------------------------------//
  private boolean repeatBytes ()
  // ---------------------------------------------------------------------------------//
  {
    // get the byte to store
    lda (packedBuffer, indirectY (src, zp_00, zp_01));        // LDA ($00),Y)
    mem_71D3 = sta ();

    while (true)
    {
      lda (mem_71D3);               // get the byte to store

      storeByte ();                 // store it and decrement repetition counter

      calculateScreenColumn ();
      if (carry)
        return true;                // completely finished

      calculateNextScreenAddress ();

      ldy (mem_71D4);               // check the repetition counter
      if (zero)
        return false;               // not finished
    }
  }

  // copy the next RC bytes
  // ---------------------------------------------------------------------------------//
  private boolean copyBytes ()
  // ---------------------------------------------------------------------------------//
  {
    while (true)
    {
      // get the byte to store
      ldy ((byte) 0x00);
      lda (packedBuffer, indirectY (src, zp_00, zp_01));        // LDA ($00),Y)

      storeByte ();                 // store it and decrement repetition counter

      calculateScreenColumn ();
      if (carry)
        return true;                // completely finished

      calculateNextScreenAddress ();

      ldy (mem_71D4);               // check the repetition counter
      if (zero)
        return false;               // not finished

      // prepare address for next read
      zp_00 = inc (zp_00);
      if (zero)
        zp_01 = inc (zp_01);
    }
  }

  // store the byte currently in Acc
  // ---------------------------------------------------------------------------------//
  private void storeByte ()
  // ---------------------------------------------------------------------------------//
  {
    mem_71D4 = dec (mem_71D4);      // decrement repetition counter (RC)
    ldy (mem_71D2);                 // column index (times 2)

    php ();
    //    sei ();
    pha ();
    tya ();
    lsr ();                         // divide by 2, put odd/even value in carry (bank)
    tay ();
    pla ();

    if (false)
      System.out.printf (
          "%04X  D0: %02X  D2: %02X  xReg: %02X  bank: %d  %02X -> %02X %02X + %02X%n",
          count++, mem_71D0, mem_71D2, stx (), carry ? 0 : 1, sta (), zp_27, zp_26,
          sty ());

    // store byte
    sta (memory[carry ? 1 : 0], indirectY (dst, zp_26, zp_27));
    plp ();
  }

  // $71B1
  // ---------------------------------------------------------------------------------//
  private void calculateScreenColumn ()
  // ---------------------------------------------------------------------------------//
  {
    // increment X by 2 - X varies 01, 03, 05 -> BF, then 00, 02, 04 -> BE
    inx ();
    inx ();
    cpx ((byte) 0xC0);              // # screen lines
    if (!carry)                     // X < $C0
      return;                       // continue current phase

    // carry is now set, current phase is finished

    // change phase
    mem_71D0 = dec (mem_71D0);      // either 1 -> 0, or 0 -> -1
    if (!negative)                  //          
    {
      assert mem_71D0 == 0;
      // start second phase of screen column
      ldx ((byte) 0x00);            // start X at 0
      //      mem_71D0 = stx ();            // phase 2 (not necessary, was already 0)
      clc ();
      return;                       // start phase 2
    }

    // start a new screen column (and start phase 1)
    mem_71D2 = inc (mem_71D2);      // increment column index
    ldy (mem_71D2);
    cpy ((byte) 0x50);              // test for end of line (2 x 0x28)
    if (carry)                      // Y >= $50
      return;                       // program finished

    ldx ((byte) 0x01);              // start X at 1
    mem_71D0 = stx ();              // start phase 1
    clc ();
  }

  // set locations $26-27 (address of first location in screen line)
  // ---------------------------------------------------------------------------------//
  private void calculateNextScreenAddress ()
  // ---------------------------------------------------------------------------------//
  {
    txa ();
    and ((byte) 0xC0);              // get 2 hi bits - 00/40/80
    zp_26 = sta ();

    lsr ();
    lsr ();
    ora (zp_26);                    // graphics page ($20 or $40)
    zp_26 = sta ();

    txa ();
    zp_27 = sta ();

    asl ();
    asl ();
    asl ();
    zp_27 = rol (zp_27);

    asl ();
    zp_27 = rol (zp_27);

    asl ();
    zp_26 = ror (zp_26);

    lda (zp_27);
    and ((byte) 0x1F);              // clear bits 7-5
    ora (zp_E6);                    // graphics page ($20 or $40)
    zp_27 = sta ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected String debugString ()
  // ---------------------------------------------------------------------------------//
  {
    return String.format ("0: %02X %02X  26: %02X %02X  %02X %02X %02X %02X", zp_00,
        zp_01, zp_26, zp_27, mem_71D0, mem_71D2, mem_71D3, mem_71D4);
  }
}
