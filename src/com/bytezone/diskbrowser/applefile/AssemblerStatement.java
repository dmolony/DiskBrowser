package com.bytezone.diskbrowser.applefile;

import java.util.Arrays;
import java.util.Comparator;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class AssemblerStatement
{
  public byte value;
  public String mnemonic;
  public String operand;
  public int size;
  public int mode;
  public int opcode;
  public int target;
  public int offset;
  public String comment;
  public int address;
  public boolean isTarget;
  public byte operand1, operand2;
  String ascii = "";

  public static void print ()
  {
    AssemblerStatement[] statements = new AssemblerStatement[256];
    System.out.println ();
    for (int i = 0; i < 256; i++)
    {
      if (i % 16 == 0 && i > 0)
        System.out.println ();
      AssemblerStatement as = new AssemblerStatement ((byte) i);
      statements[i] = as;
      if (as.size == 1)
        as.addData ();
      else if (as.size == 2)
        as.addData ((byte) 1);
      else if (as.size == 3)
        as.addData ((byte) 1, (byte) 1);
      if ((i / 8) % 2 == 0)
        System.out.printf ("%02X %15.15s   ", i, as);
    }

    Arrays.sort (statements, new Comparator<AssemblerStatement> ()
    {
      @Override
      public int compare (AssemblerStatement o1, AssemblerStatement o2)
      {
        if (o1.mnemonic.equals (o2.mnemonic))
          return o1.mode == o2.mode ? 0 : o1.mode < o2.mode ? -1 : 1;
        return o1.mnemonic.compareTo (o2.mnemonic);
      }
    });

    System.out.println ();
    String lastMnemonic = "";
    for (AssemblerStatement as : statements)
      if (as.size > 0)
      {
        if (!as.mnemonic.equals (lastMnemonic))
        {
          lastMnemonic = as.mnemonic;
          System.out.println ();
        }
        System.out.printf ("%3s  %-15s  %s%n", as.mnemonic,
            AssemblerConstants.mode[as.mode], as);
      }
  }

  public AssemblerStatement (byte opcode)
  {
    this.value = opcode;
    this.opcode = opcode & 0xFF;
    this.mnemonic = AssemblerConstants.mnemonics[this.opcode];
    this.size = AssemblerConstants.sizes2[this.opcode];
    this.operand = "";
    ascii = getChar (opcode);
  }

  String getChar (byte val)
  {
    int c = val & 0xFF;
    if (c > 127)
    {
      if (c < 160)
        c -= 64;
      else
        c -= 128;
    }
    if (c < 32 || c == 127)         // non-printable
      return ".";
    else                            // standard ascii
      return (char) c + "";
  }

  public void addData ()
  {
    switch (this.opcode)
    {
      case 0x00:  // BRK
      case 0x08:  // PHP
      case 0x18:  // CLC
      case 0x28:  // PLP
      case 0x38:  // SEC
      case 0x40:  // RTI
      case 0x48:  // PHA
      case 0x58:  // CLI
      case 0x5A:  // NOP
      case 0x60:  // RTS
      case 0x68:  // PLA
      case 0x78:  // SEI
      case 0x7A:  // NOP
      case 0x88:  // DEY
      case 0x8A:  // TXA
      case 0x98:  // TYA
      case 0x9A:  // TXS
      case 0xA8:  // TAY
      case 0xAA:  // TAX
      case 0xB8:  // CLV
      case 0xBA:  // TSX
      case 0xC8:  // INY
      case 0xCA:  // DEX
      case 0xD8:  // CLD
      case 0xDA:  // NOP
      case 0xE8:  // INX
      case 0xEA:  // NOP
      case 0xF8:  // SED
      case 0xFA:  // NOP
        mode = 0; // Implied
        break;

      case 0x0A:  // ASL
      case 0x1A:  // NOP
      case 0x2A:  // ROL
      case 0x3A:  // NOP
      case 0x4A:  // LSR
      case 0x6A:  // ROR
        mode = 1; // Accumulator
        break;

      default:
        System.out.println ("Not found (0) : " + opcode);
    }
  }

  public void addData (byte b)
  {
    operand1 = b;
    String address = "$" + HexFormatter.format2 (b);
    //    if (this.mnemonic.equals ("JSR"))
    //      this.target = HexFormatter.intValue (b);
    ascii += getChar (b);

    switch (this.opcode)
    {
      case 0x09:  // ORA
      case 0x29:  // AND
      case 0x49:  // EOR
      case 0x69:  // ADC
      case 0x89:  // NOP - 65c02
      case 0xA0:  // LDY
      case 0xA2:  // LDX
      case 0xA9:  // LDA
      case 0xC0:  // CPY
      case 0xC9:  // CMP
      case 0xE0:  // CPX
      case 0xE9:  // SBC
        operand = "#" + address;
        mode = 2; // Immediate
        break;

      case 0x04:  // NOP - 65c02
      case 0x05:  // ORA
      case 0x06:  // ASL
      case 0x14:  // NOP - 65c02
      case 0x24:  // BIT
      case 0x25:  // AND
      case 0x26:  // ROL
      case 0x45:  // EOR
      case 0x46:  // LSR
      case 0x64:  // NOP - 65c02
      case 0x65:  // ADC
      case 0x66:  // ROR
      case 0x84:  // STY
      case 0x85:  // STA
      case 0x86:  // STX
      case 0xA4:  // LDY
      case 0xA5:  // LDA
      case 0xA6:  // LDX
      case 0xC4:  // CPY
      case 0xC5:  // CMP
      case 0xC6:  // DEC
      case 0xE4:  // CPX
      case 0xE5:  // SBC
      case 0xE6:  // INC
        target = b & 0xFF;
        operand = address;
        mode = 8; // Zero page
        break;

      case 0x15:  // ORA
      case 0x16:  // ASL
      case 0x34:  // NOP - 65c02
      case 0x35:  // AND
      case 0x36:  // ROL
      case 0x55:  // EOR
      case 0x56:  // LSR
      case 0x74:  // NOP - 65c02
      case 0x75:  // ADC
      case 0x76:  // ROR
      case 0x94:  // STY
      case 0x95:  // STA
      case 0xB4:  // LDY
      case 0xB5:  // LDA
      case 0xD5:  // CMP
      case 0xD6:  // DEC
      case 0xF5:  // SBC
      case 0xF6:  // INC
        operand = address + ",X";
        mode = 9; // Zero page, X
        break;

      case 0x96:  // STX
      case 0xB6:  // LDX
        operand = address + ",Y";
        mode = 10;  // Zero page, Y
        break;

      case 0x01:  // ORA
      case 0x21:  // AND
      case 0x41:  // EOR
      case 0x61:  // ADC
      case 0x81:  // STA
      case 0xA1:  // LDA
      case 0xC1:  // CMP
      case 0xE1:  // SEC
        operand = "(" + address + ",X)";
        mode = 11;  // (Indirect, X)
        break;

      case 0x11:  // ORA
      case 0x31:  // AND
      case 0x51:  // EOR
      case 0x71:  // ADC
      case 0x91:  // STA
      case 0xB1:  // LDA
      case 0xD1:  // CMP
      case 0xF1:  // SBC
        operand = "(" + address + "),Y";
        mode = 12;  // (Indirect), Y
        break;

      case 0x12:  // NOP
      case 0x32:  // NOP
      case 0x52:  // NOP
      case 0x72:  // NOP
      case 0x92:  // NOP
      case 0xB2:  // NOP
      case 0xD2:  // NOP
      case 0xF2:  // NOP
        operand = "(" + address + ")"; // all 65c02
        mode = 13; // (zero page)
        break;

      case 0x10:  // BPL
      case 0x30:  // BMI
      case 0x50:  // BVC
      case 0x70:  // BVS
      case 0x80:  // NOP - 65c02
      case 0x90:  // BCC
      case 0xB0:  // BCS
      case 0xD0:  // BNE
      case 0xF0:  // BEQ
        offset = b;
        mode = 14; // relative
        this.target = b & 0xFF;
        break;

      default:
        System.out.println ("Not found (1) : " + opcode);
    }
  }

  public void addData (byte b1, byte b2)
  {
    operand1 = b1;
    operand2 = b2;
    String address = "$" + HexFormatter.format2 (b2) + HexFormatter.format2 (b1);
    //    if (this.mnemonic.equals ("JSR") || this.mnemonic.equals ("JMP")
    //          || this.mnemonic.equals ("BIT") || this.mnemonic.equals ("STA")
    //          || this.mnemonic.equals ("LDA"))
    //      this.target = HexFormatter.intValue (b1, b2);
    ascii += getChar (b1) + getChar (b2);

    switch (this.opcode)
    {
      case 0x0C:  // NOP - 65c02
      case 0x0D:  // ORA
      case 0x0E:  // ASL
      case 0x1C:  // NOP - 65c02
      case 0x20:  // JSR
      case 0x2C:  // BIT
      case 0x2D:  // AND
      case 0x2E:  // ROL
      case 0x4C:  // JMP
      case 0x4D:  // EOR
      case 0x4E:  // LSR
      case 0x6D:  // ADC
      case 0x6E:  // ROR
      case 0x8C:  // STY
      case 0x8D:  // STA
      case 0x8E:  // STX
      case 0x9C:  // NOP - 65c02
      case 0xAC:  // LDY
      case 0xAD:  // LDA
      case 0xAE:  // LDX
      case 0xCC:  // CPY
      case 0xCD:  // CMP
      case 0xCE:  // DEC
      case 0xEC:  // CPX
      case 0xED:  // SBC
      case 0xEE:  // INC
        operand = address;
        mode = 3; // absolute
        this.target = HexFormatter.intValue (b1, b2);
        break;

      case 0x1D:  // ORA
      case 0x1E:  // ASL
      case 0x3C:  // NOP - 65c02
      case 0x3D:  // AND
      case 0x3E:  // ROL
      case 0x5D:  // EOR
      case 0x5E:  // LSR
      case 0x7D:  // ADC
      case 0x7E:  // ROR
      case 0x9D:  // STA
      case 0x9E:  // NOP - 65c02
      case 0xBC:  // LDY
      case 0xBD:  // LDA
      case 0xDD:  // CMP
      case 0xDE:  // DEC
      case 0xFD:  // SBC
      case 0xFE:  // INC
        operand = address + ",X";
        mode = 4; // absolute, X
        break;

      case 0x19:  // ORA
      case 0x39:  // AND
      case 0x59:  // EOR
      case 0x79:  // ADC
      case 0x99:  // STA
      case 0xB9:  // LDA
      case 0xBE:  // LDX
      case 0xD9:  // CMP
      case 0xF9:  // SBC
        operand = address + ",Y";
        mode = 5; // absolute, Y
        break;

      case 0x7C:  // NOP - 65c02
        operand = "(" + address + ",X)";
        mode = 6; // (absolute, X)
        break;

      case 0x6C:  // JMP
        operand = "(" + address + ")";
        mode = 7; // (absolute)
        break;

      default:
        System.out.println ("Not found (2) : " + opcode);
    }
  }

  @Override
  public String toString ()
  {
    if (offset == 0)
      return String.format ("%d  %3s %-10s %02X", size, mnemonic, operand, value);
    return String.format ("%d  %3s %-10s %02X", size, mnemonic, operand + "+" + offset,
        value);
  }
}