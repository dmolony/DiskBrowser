package com.bytezone.diskbrowser.applefile;

public interface PascalConstants
{
  static String[] mnemonics =
      { "ABI", "ABR", "ADI", "ADR", "LAND", "DIF", "DVI", "DVR", "CHK", "FLO", "FLT",
        "INN", "INT", "LOR", "MODI", "MPI", "MPR", "NGI", "NGR", "LNOT", "SRS", "SBI",
        "SBR", "SGS", "SQI", "SQR", "STO", "IXS", "UNI", "LDE", "CSP", "LDCN", "ADJ",
        "FJP", "INC", "IND", "IXA", "LAO", "LSA", "LAE", "MOV", "LDO", "SAS", "SRO",
        "XJP", "RNP", "CIP", "EQU", "GEQ", "GRT", "LDA", "LDC", "LEQ", "LES", "LOD",
        "NEQ", "STR", "UJP", "LDP", "STP", "LDM", "STM", "LDB", "STB", "IXP", "RBP",
        "CBP", "EQUI", "GEQI", "GRTI", "LLA", "LDCI", "LEQI", "LESI", "LDL", "NEQI",
        "STL", "CXP", "CLP", "CGP", "LPA", "STE", "???", "EFJ", "NFJ", "BPT", "XIT",
        "NOP" };

  static int[] mnemonicSize =
      //
      // 128 - 155
      // 156 - 183
      // 184 - 211
      // 212 - 239
      // 240 - 255

      { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 3, 2, 1, 2, 2, 2, 2, 2, 2, 0, 3, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 3, 0, 2, 2,
        3, 2, 3, 2, 1, 1, 2, 2, 1, 1, 3, 2, 2, 1, 1, 1, 2, 3, 1, 1, 2, 1, 2, 3, 2, 2, 0,
        3, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

  static String[] descriptions =
      { "Absolute value of integer - push ABS(ToS)",
        "Absolute value of real - push abs((real)ToS)", "Add integers (tos + tos-1)",
        "Add reals - push ToS + ToS-1", "Logical AND",
        "Set difference - push difference of sets ToS-1 and ToS",
        "Divide integers - push ToS-1 / ToS", "Divide reals - push ToS-1 / ToS",
        "Check subrange bounds - assert ToS-1 <= ToS-2 <= ToS, pop ToS, pop ToS-1",
        "Float next-to-ToS - push integer ToS-1 after converting to a real",
        "Float ToS - push integer ToS after converting to a float",
        "Set Membership - if int ToS-1 is in set ToS, push true, else push false",
        "Set Intersection - push TOS AND TOS-1", "Logical OR",
        "Modulo integers - push ToS-1 % ToS", "Multiply TOS by TOS-1",
        "Multiply reals - push ToS-1 * ToS",
        "Negate Integer - push two's complement of ToS",
        "Negate real - push -((real)ToS)", "Logical Not - push one's complement of ToS",
        "Build a subrange set", "Subtract Integers push ToS-1 - ToS",
        "Subtract reals - push ToS-1 - ToS", "Build a singleton set",
        "Square integer - push ToS ^ 2", "Square real - push ToS ^ 2",
        "Store indirect word - store ToS into word pointed to by ToS-1",
        "Index string array - push &(*ToS-1 + ToS)",
        "Set union - push union of sets ToS OR ToS-1",
        "Load extended word - push word at segment :1+:2",
        "Call Standard Procedure #:1 - ", "Load Constant NIL", "Adjust set",
        "Jump if ToS false", "Increment field ptr - push ToS+:1",
        "Static index and load word", "Compute word pointer from ToS-1 + ToS * :1 words",
        "Load Global - push (BASE+:1)", "Load constant string address",
        "Load extended address - push address of word at segment :1+:2",
        "Move words - transfer :1 words from *ToS to *ToS-1",
        "Load Global Word - push BASE+:1", "String Assign", "Store TOS into BASE+:1",
        "Case Jump - :1::2, Error: :3", "Return from non-base procedure (pass :1 words)",
        "Call intermediate procedure #:1", "ToS-1 == ToS", "ToS-1 >= ToS", "ToS-1 > ToS",
        "Load Intermediate Address - push :1th activation record +:2 bytes",
        "Load multi-word constant - :1 words", "ToS-1 <= ToS", "ToS-1 < ToS",
        "Load Intermediate Word - push :1th activation record +:2 bytes", "ToS-1 <> ToS",
        "Store intermediate word - store TOS into :2, traverse :1", "Unconditional jump",
        "Load Packed Field - push *ToS", "Store into packed field",
        "Load multiple words - push block of unsigned bytes at *ToS",
        "Store multiple words - store block of UB at ToS to *ToS-1",
        "Load Byte - index the byte pointer ToS-1 by integer index ToS and push that byte",
        "Store Byte - index the byte pointer ToS-2 by integer index ToS-1 and move ToS to that location",
        "Index packed array - do complicated stuff with :1 and :2",
        "Return from base procedure (pass :1 words)",
        "Call Base Procedure :1 at lex level -1 or 0", "Compare Integer : ToS-1 = ToS",
        "Compare Integer : TOS-1 >= TOS", "Compare Integer : TOS-1 > ToS",
        "Load Local Address - push MP+:1", "Load Word - push #:1",
        "Compare Integer : TOS-1 <= TOS", "Compare Integer : TOS-1 < ToS",
        "Load Local Word - push MP+:1", "Compare Integer : TOS-1 <> TOS",
        "Store Local Word - store ToS into MP+:1",
        "Call external procedure #:2 in segment #:1", "Call local procedure #:1",
        "Call global procedure #:1", "Load a packed array - use :1 and :2",
        "Store extended word - store ToS into word at segment :1+:2", "210        ",
        "Equal false jump - jump :1 if ToS-1 <> ToS",
        "Not equal false jump - jump :1 if ToS-1 == ToS",
        "Breakpoint - not used (does NOP)", "Exit OS - cold boot", "No-op" };

  static String[] CSP =
      { "000", "NEW", "MVL", "MVR", "EXIT", "", "", "IDS", "TRS", "TIM", "FLC", "SCN", "",
        "", "", "", "", "", "", "", "", "021", "TNC", "RND", "", "", "", "", "", "", "",
        "MRK", "RLS", "33", "34", "POT", "36", "37", "38", "39", "40" };

  static String[] SegmentKind = { "Linked", "HostSeg", "SegProc", "UnitSeg", "SeprtSeg",
                                  "UnlinkedIntrins", "LinkedIntrins", "DataSeg" };
}