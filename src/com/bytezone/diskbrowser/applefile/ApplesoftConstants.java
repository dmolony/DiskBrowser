package com.bytezone.diskbrowser.applefile;

public interface ApplesoftConstants
{
  String[] tokens = { "END", "FOR ", "NEXT ", "DATA ",           // 0x80 - 0x83
                      "INPUT ", "DEL", "DIM ", "READ ",          // 0x84 - 0x87
                      "GR", "TEXT", "PR#", "IN#",                // 0x88 - 0x8B
                      "CALL ", "PLOT ", "HLIN ", "VLIN ",        // 0x8C - 0x8F
                      "HGR2", "HGR", "HCOLOR=", "HPLOT ",        // 0x90
                      "DRAW ", "XDRAW ", "HTAB ", "HOME",        // 0x94
                      "ROT=", "SCALE=", "SHLOAD", "TRACE",       // 0x98
                      "NOTRACE", "NORMAL", "INVERSE", "FLASH",   // 0x9C
                      "COLOR=", "POP", "VTAB ", "HIMEM:",        // 0xA0
                      "LOMEM:", "ONERR ", "RESUME", "RECALL",    // 0xA4 - 0xA7
                      "STORE", "SPEED=", "LET ", "GOTO ",        // 0xA8
                      "RUN", "IF ", "RESTORE", "& ",             // 0xAC
                      "GOSUB ", "RETURN", "REM ", "STOP",        // 0xB0
                      "ON ", "WAIT", "LOAD", "SAVE",             // 0xB4
                      "DEF", "POKE ", "PRINT ", "CONT",          // 0xB8
                      "LIST", "CLEAR", "GET ", "NEW",            // 0xBC
                      "TAB(", "TO ", "FN", "SPC(",               // 0xC0
                      "THEN ", "AT ", "NOT ", "STEP ",           // 0xC4
                      "+ ", "- ", "* ", "/ ",                    // 0xC8
                      "^ ", "AND ", "OR ", "> ",                 // 0xCC
                      "= ", "< ", "SGN", "INT ",                 // 0xD0
                      "ABS", "USR", "FRE", "SCRN(",              // 0xD4 
                      "PDL", "POS ", "SQR", "RND",               // 0xD8
                      "LOG", "EXP", "COS", "SIN",                // 0xDC
                      "TAN", "ATN", "PEEK", "LEN",               // 0xE0 - 0xE3
                      "STR$", "VAL", "ASC", "CHR$",              // 0xE4 - 0xE7
                      "LEFT$", "RIGHT$", "MID$", "",             // 0xE8 - 0xEB
                      "", "", "", "",                            // 0xEC - 0xEF
                      "ELSE", "MOD", "INC", "DEC",               // 0xF0 - 0xF3
                      "DEEK", "DOKE", "REPEAT", "UNTIL",         // 0xF4 - 0xF7
                      "", "", "", "",                            // 0xF8 - 0xFB
                      "", "", "", "",                            // 0xFC - 0xFF
  };

  int[] tokenAddresses =
      { 0xD870, 0xD766, 0xDCF9, 0xD995, 0xDBB2, 0xF331, 0xDFD9, 0xDBE2, 0xF390, 0xF399,
        0xF1E5, 0xF1DE, 0xF1D5, 0xF225, 0xF232, 0xF241, 0xF3D8, 0xF3E2, 0xF6E9, 0xF6FE,
        0xF769, 0xF76F, 0xF7E7, 0xFC58, 0xF721, 0xF727, 0xF775, 0xF26D, 0xF26F, 0xF273,
        0xF277, 0xF280, 0xF24F, 0xD96B, 0xF256, 0xF286, 0xF2A6, 0xF2CB, 0xF318, 0xF3BC,
        0xF39F, 0xF262, 0xDA46, 0xD93E, 0xD912, 0xD9C9, 0xD849, 0x03F5, 0xD921, 0xD96B,
        0xD9DC, 0xD86E, 0xD9EC, 0xE784, 0xD8C9, 0xD8B0, 0xE313, 0xE77B, 0xFDAD5, 0xD896,
        0xD6A5, 0xD66A, 0xDBA0, 0xD649, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0xEB90, 0xEC23, 0xEBAF, 0x000A, 0xE2DE,
        0xD412, 0xDFCD, 0xE2FF, 0xEE8D, 0xEFAE, 0xE941, 0xEF09, 0xEFEA, 0xEFF1, 0xF03A,
        0xF09E, 0xE764, 0xE6D6, 0xE3C5, 0xE707, 0xE6E5, 0xE646, 0xE65A, 0xE686, 0xE691 };
}

/*
https://groups.google.com/forum/#!topic/comp.sys.apple2/_lQ2-l9i5cw
NEW Applesoft tokens
$F0 - ELSE
$F1 - MOD
$F2 - INC - as in Increment, so instead of A=A+1: use INC A
$F3 - DEC - opposite of INC
$F4 - DEEK - two-byte PEEK
$F5 - DOKE - two-byte POKE
$F6 - REPEAT
$F7 - UNTIL
*/
