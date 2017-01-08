package com.bytezone.diskbrowser.utilities;

public abstract class CPU
{
  // registers
  private byte xReg;
  private byte yReg;
  private byte aReg;

  // status register
  protected boolean carry;
  protected boolean zero;
  protected boolean interrupt;
  protected boolean decimal;
  protected boolean breakFlag;
  protected boolean overflow;       // Clancy
  protected boolean negative;

  // stack
  private final byte[] stack = new byte[0x100];
  private int sp = stack.length;

  private boolean debug = false;

  protected void setDebug (boolean value)
  {
    debug = value;
  }

  protected void and (byte mask)              // AND
  {
    aReg &= mask;
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("AND");
  }

  protected void asl ()                       // ASL
  {
    carry = (aReg & 0x80) != 0;       // move bit 7 into the carry flag
    aReg = (byte) (aReg << 1);        // shift left
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("ASL");
  }

  // unfinished
  protected void bit (byte value)             // BIT
  {
    byte b = (byte) (aReg & value);
    zero = b == 0;
    overflow = (value & 0x40) != 0;
    negative = (value & 0x80) != 0;
    debug ("BIT");
  }

  protected void clc ()                       // CLC
  {
    carry = false;
    debug ("CLC");
  }

  protected void cli ()                       // CLI
  {
    interrupt = false;
    debug ("CLI");
  }

  protected void clv ()                       // CLV
  {
    overflow = false;
    debug ("CLV");
  }

  protected void cmp (byte value)             // CMP
  {
    int tmp = (aReg & 0xFF) - (value & 0xFF);
    zero = tmp == 0;
    negative = (tmp & 0x80) != 0;
    carry = (aReg & 0xFF) >= (value & 0xFF);
    debug ("CMP");
    if (debug)
      System.out.printf ("  cmp a: %02X v: %02X%n", aReg, value);
  }

  protected void cpx (byte value)             // CPX
  {
    int tmp = (xReg & 0xFF) - (value & 0xFF);
    zero = tmp == 0;
    negative = (tmp & 0x80) != 0;
    carry = (xReg & 0xFF) >= (value & 0xFF);
    debug ("CPX");
    if (debug)
      System.out.printf ("  cpx x: %02X v: %02X%n", xReg, value);
  }

  protected void cpy (byte value)             // CPY
  {
    int tmp = (yReg & 0xFF) - (value & 0xFF);
    zero = tmp == 0;
    negative = (tmp & 0x80) != 0;
    carry = (yReg & 0xFF) >= (value & 0xFF);
    debug ("CPY");
    if (debug)
      System.out.printf ("  cpy y: %02X v: %02X%n", yReg, value);
  }

  protected byte dec (byte value)             // DEC
  {
    value = (byte) ((value & 0xFF) - 1);
    zero = value == 0;
    negative = (value & 0x80) != 0;
    debug ("DEC");
    return value;
  }

  protected byte inc (byte value)             // INC
  {
    value = (byte) ((value & 0xFF) + 1);
    zero = value == 0;
    negative = (value & 0x80) != 0;
    debug ("INC");
    return value;
  }

  protected void inx ()                       // INX
  {
    xReg = (byte) ((xReg & 0xFF) + 1);
    xReg &= 0xFF;
    zero = xReg == 0;
    negative = (xReg & 0x80) != 0;
    debug ("INX");
  }

  protected void lda (byte value)             // LDA
  {
    aReg = value;
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("LDA");
  }

  protected void lda (byte[] buffer, int offset)        // LDA
  {
    aReg = buffer[offset];
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("LDA");
  }

  protected void ldx (byte value)             // LDX
  {
    xReg = value;
    zero = xReg == 0;
    negative = (xReg & 0x80) != 0;
    debug ("LDX");
  }

  protected void ldy (byte value)             // LDY
  {
    yReg = value;
    zero = yReg == 0;
    negative = (yReg & 0x80) != 0;
    debug ("LDY");
  }

  protected void lsr ()                       // LSR
  {
    negative = false;
    carry = (aReg & 0x01) != 0;
    aReg = (byte) ((aReg & 0xFF) >>> 1);
    zero = aReg == 0;
    debug ("LSR");
  }

  protected void ora (byte mask)              // ORA
  {
    aReg |= mask;
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("ORA");
  }

  protected void php ()                       // PHP
  {
    byte flags = 0;
    if (negative)
      flags |= 0x80;
    if (overflow)
      flags |= 0x40;
    if (breakFlag)
      flags |= 0x10;
    if (decimal)
      flags |= 0x08;
    if (interrupt)
      flags |= 0x04;
    if (zero)
      flags |= 0x02;
    if (carry)
      flags |= 0x01;
    stack[--sp] = flags;
    debug ("PHP");
  }

  protected void plp ()                       // PLP
  {
    byte flags = stack[sp++];
    negative = (flags & 0x80) != 0;
    overflow = (flags & 0x40) != 0;
    breakFlag = (flags & 0x10) != 0;
    decimal = (flags & 0x08) != 0;
    interrupt = (flags & 0x04) != 0;
    zero = (flags & 0x02) != 0;
    carry = (flags & 0x01) != 0;
    debug ("PLP");
  }

  protected void pha ()                       // PHA
  {
    stack[--sp] = aReg;
    debug ("PHA");
  }

  protected void pla ()                       // PLA
  {
    aReg = stack[sp++];
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("PLA");
  }

  protected void rol ()                       // ROL
  {
    boolean tempCarry = carry;
    carry = (aReg & 0x80) != 0;       // move bit 7 into the carry flag
    aReg = (byte) (aReg << 1);        // shift left
    if (tempCarry)
      aReg |= 0x01;                   // move old carry into bit 0
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("ROL");
  }

  protected byte rol (byte value)             // ROL
  {
    boolean tempCarry = carry;
    carry = (value & 0x80) != 0;      // move bit 7 into the carry flag
    value = (byte) (value << 1);
    if (tempCarry)
      value |= 0x01;                  // move old carry into bit 0
    zero = value == 0;
    negative = (value & 0x80) != 0;
    debug ("ROL");

    return value;
  }

  protected byte ror (byte value)             // ROR
  {
    boolean tempCarry = carry;
    carry = (value & 0x01) != 0;      // move bit 0 into the carry flag
    value = (byte) ((value & 0xFF) >>> 1);
    if (tempCarry)
      value |= 0x80;                  // move old carry into bit 7
    zero = value == 0;
    negative = (value & 0x80) != 0;
    debug ("ROR");

    return value;
  }

  protected byte sta ()                       // STA
  {
    debug ("STA");
    return aReg;
  }

  protected void sta (byte[] buffer, int offset)        // STA
  {
    buffer[offset] = aReg;
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("STA");
  }

  protected byte stx ()                       // STX
  {
    debug ("STX");
    return xReg;
  }

  protected byte sty ()                       // STY
  {
    debug ("STY");
    return yReg;
  }

  protected void txa ()                       // TXA
  {
    aReg = xReg;
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("TXA");
  }

  protected void tya ()                       // TYA
  {
    aReg = yReg;
    zero = aReg == 0;
    negative = (aReg & 0x80) != 0;
    debug ("TYA");
  }

  protected void tax ()                       // TAX
  {
    xReg = aReg;
    zero = xReg == 0;
    negative = (xReg & 0x80) != 0;
    debug ("TAX");
  }

  protected void tay ()                       // TAY
  {
    yReg = aReg;
    zero = yReg == 0;
    negative = (yReg & 0x80) != 0;
    debug ("TAY");
  }

  protected void sei ()                       // SEI
  {
    interrupt = true;
    debug ("SEI");
  }

  protected abstract String debugString ();

  protected void debug (String cmd)
  {
    if (debug)
    {
      String flags = String.format ("%s %s - %s %s %s %s %s", negative ? "1" : ".",
          overflow ? "1" : ".", breakFlag ? "1" : ".", decimal ? "1" : ".",
          interrupt ? "1" : ".", zero ? "1" : ".", carry ? "1" : ".");
      System.out.printf ("%3s  A: %02X  X: %02X  Y: %02X  %s  %s%n", cmd, aReg, xReg,
          yReg, flags, debugString ());
    }
  }

  protected int indirectY (int base, byte offset, byte page)
  {
    if (debug)
      System.out.printf ("base: %,6d, page: %02X, offset: %02X, yReg: %02X%n", base, page,
          offset, yReg);

    return ((page & 0xFF) * 256 + (offset & 0xFF)) - base + (yReg & 0xFF);
  }
}