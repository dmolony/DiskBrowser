package com.bytezone.diskbrowser.applefile;

// -----------------------------------------------------------------------------------//
public interface AssemblerConstants
// -----------------------------------------------------------------------------------//
{
  // 1A = INC A, 3A = DEC A
  String[] mnemonics = { "BRK", "ORA", "???", "???", "TSB", "ORA", "ASL", "???", // 00
                         "PHP", "ORA", "ASL", "???", "TSB", "ORA", "ASL", "???", // 08
                         "BPL", "ORA", "ORA", "???", "TRB", "ORA", "ASL", "???", // 10
                         "CLC", "ORA", "INC", "???", "TRB", "ORA", "ASL", "???", // 18
                         "JSR", "AND", "???", "???", "BIT", "AND", "ROL", "???", // 20
                         "PLP", "AND", "ROL", "???", "BIT", "AND", "ROL", "???", // 28
                         "BMI", "AND", "AND", "???", "BIT", "AND", "ROL", "???", // 30
                         "SEC", "AND", "DEC", "???", "BIT", "AND", "ROL", "???", // 38
                         "RTI", "EOR", "???", "???", "???", "EOR", "LSR", "???", // 40
                         "PHA", "EOR", "LSR", "???", "JMP", "EOR", "LSR", "???", // 48
                         "BVC", "EOR", "EOR", "???", "???", "EOR", "LSR", "???", // 50
                         "CLI", "EOR", "PHY", "???", "???", "EOR", "LSR", "???", // 58
                         "RTS", "ADC", "???", "???", "STZ", "ADC", "ROR", "???", // 60
                         "PLA", "ADC", "ROR", "???", "JMP", "ADC", "ROR", "???", // 68
                         "BVS", "ADC", "ADC", "???", "STZ", "ADC", "ROR", "???", // 70
                         "SEI", "ADC", "PLY", "???", "JMP", "ADC", "ROR", "???", // 78
                         "BRA", "STA", "???", "???", "STY", "STA", "STX", "???", // 80
                         "DEY", "BIT", "TXA", "???", "STY", "STA", "STX", "???", // 88
                         "BCC", "STA", "STA", "???", "STY", "STA", "STX", "???", // 90
                         "TYA", "STA", "TXS", "???", "STZ", "STA", "STZ", "???", // 98
                         "LDY", "LDA", "LDX", "???", "LDY", "LDA", "LDX", "???", // A0
                         "TAY", "LDA", "TAX", "???", "LDY", "LDA", "LDX", "???", // A8
                         "BCS", "LDA", "LDA", "???", "LDY", "LDA", "LDX", "???", // B0
                         "CLV", "LDA", "TSX", "???", "LDY", "LDA", "LDX", "???", // B8
                         "CPY", "CMP", "???", "???", "CPY", "CMP", "DEC", "???", // C0
                         "INY", "CMP", "DEX", "???", "CPY", "CMP", "DEC", "???", // C8
                         "BNE", "CMP", "CMP", "???", "???", "CMP", "DEC", "???", // D0
                         "CLD", "CMP", "PHX", "???", "???", "CMP", "DEC", "???", // D8
                         "CPX", "SBC", "???", "???", "CPX", "SBC", "INC", "???", // E0
                         "INX", "SBC", "NOP", "???", "CPX", "SBC", "INC", "???", // E8
                         "BEQ", "SBC", "SBC", "???", "???", "SBC", "INC", "???", // F0
                         "SED", "SBC", "PLX", "???", "???", "SBC", "INC", "???" }; // F8

  byte[] sizes2 = { 1, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0, // 00 - 0F
                    2, 2, 2, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0, // 10 - 1F 
                    3, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0, // 20 - 2F
                    2, 2, 2, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0, // 30 - 3F
                    1, 2, 0, 0, 0, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0, // 40 - 4F
                    2, 2, 2, 0, 0, 2, 2, 0, 1, 3, 1, 0, 0, 3, 3, 0, // 50 - 5F
                    1, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0, // 60 - 6F
                    2, 2, 2, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0, // 70 - 7F
                    2, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0, // 80 - 8F
                    2, 2, 2, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0, // 90 - 9F
                    2, 2, 2, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0, // A0 - AF
                    2, 2, 2, 0, 2, 2, 2, 0, 1, 3, 1, 0, 3, 3, 3, 0, // B0 - BF
                    2, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0, // C0 - CF
                    2, 2, 2, 0, 0, 2, 2, 0, 1, 3, 1, 0, 0, 3, 3, 0, // D0 - DF
                    2, 2, 0, 0, 2, 2, 2, 0, 1, 2, 1, 0, 3, 3, 3, 0, // E0 - EF
                    2, 2, 2, 0, 0, 2, 2, 0, 1, 3, 1, 0, 0, 3, 3, 0 }; // F0 - FF

  byte[] sizes = { 1, 1, 2, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2 };

  String[] mode =
      { "Implied", "Accumulator", "Immediate", "Absolute", "Absolute, X", "Absolute, Y",
        "(Absolute, X)", "(Absolute)", "Zero page", "Zero page, X", "Zero page, Y",
        "(Zero page, X)", "(Zero page), Y", "(Zero page)", "Relative" };

  byte[] chip65c02 =
      { 0x04, 0x0C, 0x12, 0x14, 0x1A, 0x1C, 0x32, 0x34, 0x3A, 0x3C, 0x52, 0x5A, 0x64,
        0x72, 0x74, 0x7A, 0x7C, (byte) 0x80, (byte) 0x89, (byte) 0x92, (byte) 0x9C,
        (byte) 0x9E, (byte) 0xB2, (byte) 0xD2, (byte) 0xDA, (byte) 0xF2, (byte) 0xFA, };
}