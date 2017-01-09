package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.CPU;

public class DoubleScrunch extends CPU
{
  private byte mem_71D0;        // screen row index (X)
  private byte mem_71D2;        // screen column index
  private byte mem_71D3;        // byte to repeat
  private byte mem_71D4;        // # of bytes to store

  private int src;              // base address of packed buffer
  private int dst;              // base address of main/aux memory

  private byte zp_00;           // input buffer offset
  private byte zp_01;

  private byte zp_26;           // output buffer offset
  private byte zp_27;

  private byte zp_E6;           // hi-res page ($20 or $40)

  final byte[] auxBuffer = new byte[0x2000];
  final byte[] primaryBuffer = new byte[0x2000];
  private byte[] packedBuffer;

  private final boolean debug = false;

  public DoubleScrunch ()
  {
    setDebug (debug);
  }

  void unscrunch (byte[] buffer)
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
    mem_71D0 = stx ();
    ldy ((byte) 0x00);
    mem_71D2 = sty ();      // line offset = 0
    zp_26 = sty ();         // output index = 0

    while (true)
    {
      // get the repetition counter (hi-bit is a flag to indicate copy/repeat)
      lda (packedBuffer, indirectY (src, zp_00, zp_01));        // LDA ($00),Y)

      php ();

      // prepare address to get next transfer byte (done in copy/repeat routine)
      zp_00 = inc (zp_00);
      if (zero)
        zp_01 = inc (zp_01);

      and ((byte) 0x7F);            // remove copy/repeat flag
      mem_71D4 = sta ();            // save repetition counter
      plp ();

      if (negative ? copyBytes () : repeatBytes ())
        break;

      // prepare address to get next repetition counter
      zp_00 = inc (zp_00);
      if (zero)
        zp_01 = inc (zp_01);
    }
  }

  // $7144
  // copy a single byte $71D4 times
  private boolean repeatBytes ()
  {
    // get the byte to store
    lda (packedBuffer, indirectY (src, zp_00, zp_01));        // LDA ($00),Y)
    mem_71D3 = sta ();

    while (true)
    {
      lda (mem_71D3);               // get the byte to store

      storeByte ();                 // store it and decrement repetition counter

      calculateScreenLine ();
      if (carry)
        return true;                // completely finished

      calculateNextScreenAddress ();

      ldy (mem_71D4);               // check the repetition counter
      if (zero)
        return false;               // not finished
    }
  }

  // $717D
  // copy the next $71D4 bytes
  private boolean copyBytes ()
  {
    while (true)
    {
      // get the byte to store
      ldy ((byte) 0x00);
      lda (packedBuffer, indirectY (src, zp_00, zp_01));        // LDA ($00),Y)

      storeByte ();                 // store it and decrement repetition counter

      calculateScreenLine ();
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

  private void storeByte ()
  {
    mem_71D4 = dec (mem_71D4);      // decrement counter
    ldy (mem_71D2);                 // line offset

    php ();
    sei ();
    pha ();
    tya ();
    lsr ();                         // divide by 2 ?
    tay ();
    pla ();

    // switch page
    byte[] target = carry ? primaryBuffer : auxBuffer;

    // store byte
    sta (target, indirectY (dst, zp_26, zp_27));

    plp ();
  }

  // $70E8
  private void calculateNextScreenAddress ()
  {
    txa ();
    and ((byte) 0xC0);              // clear bits 5-0
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

  // $71B1
  private void calculateScreenLine ()
  {
    inx ();
    inx ();
    cpx ((byte) 0xC0);
    if (!carry)                     // BCC $71CE 
    {
      clc ();
      return;
    }

    mem_71D0 = dec (mem_71D0);
    if (!negative)                 // BPL $71C9         
    {
      ldx ((byte) 0);
      mem_71D0 = stx ();
      clc ();
      return;
    }

    mem_71D2 = inc (mem_71D2);      // increment line offset
    ldy (mem_71D2);
    cpy ((byte) 0x50);
    if (carry)                      // BCS $71CF
      return;                       // program finished

    ldx ((byte) 0x01);
    //    bit ((byte) 0xA2);              // ??
    mem_71D0 = stx ();
    clc ();
  }

  @Override
  protected String debugString ()
  {
    return String.format ("0: %02X %02X  26: %02X %02X  %02X %02X %02X %02X", zp_00,
        zp_01, zp_26, zp_27, mem_71D0, mem_71D2, mem_71D3, mem_71D4);
  }
}