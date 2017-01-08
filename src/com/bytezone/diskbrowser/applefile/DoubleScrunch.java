package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.CPU;

public class DoubleScrunch extends CPU
{
  private byte mem_71D0;
  private byte mem_71D2;
  private byte mem_71D3;
  private byte mem_71D4;

  private int src;              // base address of packed buffer
  private int dst;              // base address of main/aux memory

  private byte zp_00;
  private byte zp_01;
  private byte zp_26;
  private byte zp_27;
  private byte zp_E6;

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
    if (false)
    {
      ldx ((byte) 0x81);
      calculateScreenAddress ();

      return;
    }

    packedBuffer = buffer;

    src = 0x4000;           // packed picture data
    dst = 0x2000;           // main/aux picture data

    zp_00 = 0x00;
    zp_01 = 0x40;
    zp_E6 = 0x20;           // hi-res page 1

    lda (zp_E6);
    ora ((byte) 0x04);
    zp_27 = sta ();

    ldx ((byte) 0x01);
    mem_71D0 = stx ();
    ldy ((byte) 0x00);
    mem_71D2 = sty ();
    zp_26 = sty ();

    while (true)
    {
      lda (packedBuffer, indirectY (src, zp_00, zp_01));        // LDA ($00),Y)

      php ();
      zp_00 = inc (zp_00);
      if (zero)
        zp_01 = inc (zp_01);

      and ((byte) 0x7F);
      mem_71D4 = sta ();
      plp ();

      if (negative)
      {
        boolean finished = blockB ();
        if (finished)
          break;
      }
      else
      {
        boolean finished = blockA ();
        if (finished)
          break;
      }

      zp_00 = inc (zp_00);
      if (zero)
        zp_01 = inc (zp_01);
    }
  }

  // $7144
  // repeat a single byte X times
  private boolean blockA ()
  {
    lda (packedBuffer, indirectY (src, zp_00, zp_01));        // LDA ($00),Y)
    mem_71D3 = sta ();

    do
    {
      lda (mem_71D3);

      storeByte ();

      function_71B1 ();
      if (carry)
        return true;            // completely finished

      calculateScreenAddress ();
      ldy (mem_71D4);
    } while (!zero);

    return false;               // not finished
  }

  // $717D
  // copy X single bytes
  private boolean blockB ()
  {
    while (true)
    {
      ldy ((byte) 0x00);
      lda (packedBuffer, indirectY (src, zp_00, zp_01));        // LDA ($00),Y)

      storeByte ();

      function_71B1 ();
      if (carry)
        return true;            // completely finished

      calculateScreenAddress ();

      ldy (mem_71D4);
      if (zero)
        return false;           // not finished

      zp_00 = inc (zp_00);
      if (zero)
        zp_01 = inc (zp_01);

      assert !zero;
    }
  }

  private void storeByte ()
  {
    mem_71D4 = dec (mem_71D4);      // decrement counter
    ldy (mem_71D2);                 // load byte to store

    php ();
    sei ();
    pha ();
    tya ();
    lsr ();                         // divide by 2 ?
    tay ();
    pla ();

    // switch page
    byte[] target = carry ? primaryBuffer : auxBuffer;
    sta (target, indirectY (dst, zp_26, zp_27));

    plp ();
  }

  // $70E8
  private void calculateScreenAddress ()
  {
    txa ();
    and ((byte) 0xC0);              // clear bits 5-0
    zp_26 = sta ();

    lsr ();
    lsr ();
    ora (zp_26);
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
    ora (zp_E6);
    zp_27 = sta ();
  }

  // $71B1
  private void function_71B1 ()
  {
    inx ();
    inx ();
    cpx ((byte) 0xC0);
    if (!carry)                     // BCC $71CE (if X < 0xC0)
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

    mem_71D2 = inc (mem_71D2);
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