package com.bytezone.diskbrowser.applefile;

public interface ApplesoftConstants
{
  String[] tokens = { "END", "FOR ", "NEXT ", "DATA ", "INPUT ", "DEL", "DIM ", "READ ", "GR",
                     "TEXT", "PR#", "IN#", "CALL ", "PLOT", "HLIN ", "VLIN ", "HGR2", "HGR",
                     "HCOLOR=", "HPLOT ", "DRAW ", "XDRAW ", "HTAB ", "HOME", "ROT=", "SCALE=",
                     "SHLOAD", "TRACE", "NOTRACE", "NORMAL", "INVERSE", "FLASH", "COLOR=", "POP",
                     "VTAB ", "HIMEM:", "LOMEM:", "ONERR ", "RESUME", "RECALL", "STORE", "SPEED=",
                     "LET ", "GOTO ", "RUN", "IF ", "RESTORE", "& ", "GOSUB ", "RETURN", "REM ",
                     "STOP", "ON ", "WAIT", "LOAD", "SAVE", "DEF", "POKE ", "PRINT ", "CONT",
                     "LIST", "CLEAR", "GET ", "NEW", "TAB(", "TO ", "FN", "SPC(", "THEN ", "AT ",
                     "NOT ", "STEP ", "+ ", "- ", "* ", "/ ", "^ ", "AND ", "OR ", "> ", "= ",
                     "< ", "SGN", "INT", "ABS", "USR", "FRE", "SCRN(", "PDL", "POS ", "SQR", "RND",
                     "LOG", "EXP", "COS", "SIN", "TAN", "ATN", "PEEK", "LEN", "STR$", "VAL", "ASC",
                     "CHR$", "LEFT$", "RIGHT$", "MID$" };

  int[] tokenAddresses = { 0xD870, 0xD766, 0xDCF9, 0xD995, 0xDBB2, 0xF331, 0xDFD9, 0xDBE2, 0xF390,
                          0xF399, 0xF1E5, 0xF1DE, 0xF1D5, 0xF225, 0xF232, 0xF241, 0xF3D8, 0xF3E2,
                          0xF6E9, 0xF6FE, 0xF769, 0xF76F, 0xF7E7, 0xFC58, 0xF721, 0xF727, 0xF775,
                          0xF26D, 0xF26F, 0xF273, 0xF277, 0xF280, 0xF24F, 0xD96B, 0xF256, 0xF286,
                          0xF2A6, 0xF2CB, 0xF318, 0xF3BC, 0xF39F, 0xF262, 0xDA46, 0xD93E, 0xD912,
                          0xD9C9, 0xD849, 0x03F5, 0xD921, 0xD96B, 0xD9DC, 0xD86E, 0xD9EC, 0xE784,
                          0xD8C9, 0xD8B0, 0xE313, 0xE77B, 0xFDAD5, 0xD896, 0xD6A5, 0xD66A, 0xDBA0,
                          0xD649, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0xEB90,
                          0xEC23, 0xEBAF, 0x000A, 0xE2DE, 0xD412, 0xDFCD, 0xE2FF, 0xEE8D, 0xEFAE,
                          0xE941, 0xEF09, 0xEFEA, 0xEFF1, 0xF03A, 0xF09E, 0xE764, 0xE6D6, 0xE3C5,
                          0xE707, 0xE6E5, 0xE646, 0xE65A, 0xE686, 0xE691 };
}