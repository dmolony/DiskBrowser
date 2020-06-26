package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class BasicProgramGS extends BasicProgram
// -----------------------------------------------------------------------------------//
{
  private static String[] //
  tokens = { "AUTO", "DEL", "EDI", "HLIST ",                    // 0x80 - 0x83
             "LIST", "RENUM", "BREAK ", "FN ",                  // 0x84 - 0x87
             "PROC ", "GOSUB", " GOTO ", "FOR ",                // 0x88 - 0x8B
             " THEN ", "ELSE ", "NEXT ", "OFF ",                // 0x8C - 0x8F
             "ON ", "INPUT ", "OUTPUT ", "TEXT ",               // 0x90 - 0x93
             "TIMER ", "EXCEPTION ", "CAT ", " 97 ",            // 0x94 - 0x97
             "INIT ", "INVOKE ", "LIBRARY ", "PREFIX ",         // 0x98 - 0x9B
             "TYPE ", "LOAD ", "SAVE ", "DELETE ",              // 0x9C - 0x9F
             "RUN ", "RENAME ", "CREATE ", "LOCK ",             // 0xA0 - 0xA3
             "UNLOCK ", "EXEC ", "CHAIN ", "CATALOG ",          // 0xA4 - 0xA7
             "OPEN ", "QUIT ", "DIR ", "DIM ",                  // 0xA8 - 0xAB
             "READ ", "WRITE ", "CLOSE ", "TASKPOLL ",          // 0xAC - 0xAF
             "LOCATE ", "EVENTDEF ", "MENUDEF ", "VOLUMES ",    // 0xB0 - 0xB3
             "CALL% ", "CALL ", "_", "TEXTPORT ",               // 0xB4 - 0xB7
             "PERFORM ", "GRAF ", "DBUG ", "POP ",              // 0xB8 - 0xBB
             "HOME ", "SUB$(", "TRACE ", "NOTRACE ",            // 0xBC - 0xBF
             "NORMAL ", "INVERSE ", "RESUME ", "LET ",          // 0xC0 - 0xC3
             "IF ", "RESTORE ", "SWAP ", "RETURN ",             // 0xC4 - 0xC7
             "REM ", "STOP ", "DATA ", "IMAGE ",                // 0xC8 - 0xCB
             "LIBFIND ", "DEF ", "PRINT ", "CLEAR ",            // 0xCC - 0xCF
             "RANDOMIZE ", "NEW ", "POKE ", "ASSIGN ",          // 0xD0 - 0xD3
             "GET ", "PUT ", "SET ", "ERROR ",                  // 0xD4 - 0xD7
             "ERASE ", "LOCAL ", "WHILE ", "CONT ",             // 0xD8 - 0xDB
             "DO ", "UNTIL ", "END ", " DF ",                   // 0xDC - 0xDF
             " E0 ", " E1 ", " E2 ", " E3 ",                    // 0xE0 - 0xE3
             " E4 ", " E5 ", " E6 ", " E7 ",                    // 0xE4 - 0xE7
             " E8 ", " E9 ", " EA ", " EB ",                    // 0xE8 - 0xEB
             " EC ", " ED ", " EE ", " EF  ",                   // 0xEC - 0xEF
             " F0 ", " F1 ", " F2 ", " F3 ",                    // 0xF0 - 0xF3
             " F4 ", " F5 ", " F6 ", " F7 ",                    // 0xF4 - 0xF7
             " F8 ", " F9 ", " FA ", " FB ",                    // 0xF8 - 0xFB
             " FC ", " FD ", " FE ", " FF  ",                   // 0xFC - 0xFF
  };

  private static String[] //
  tokensDF = { " TAB(", " TO ", " SPC(", " USING ",                   // 0x80 - 0x83
               "APPEND ", " MOD ", " REMDR ", " STEP ",               // 0x84 - 0x87
               " AND ", " OR ", " XOR ", " DIV ",                     // 0x88 - 0x8B
               " SRC ", " NOT ", " 8E ", " UPDATE ",                  // 0x8C - 0x8F
               " TXT ", " BDF ", " FILTYPE= ", " AS ",                // 0x90 - 0x93
               " 94 ", " 95 ", " SGN(", " INT ",                      // 0x94 - 0x97
               " ABS(", " TYP(", " REC(", " JOYX(",                   // 0x98 - 0x9B
               " PDL(", " BTN(", " R.STACK% ", " R.STACK@ ",          // 0x9C - 0x9F
               " R.STACK&(", " SQR(", " RND(", " LOG(",               // 0xA0 - 0xA3
               " LOG1(", " LOG2(", " LOGB%(", " EXP(",                // 0xA4 - 0xA7
               " EXP1(", " EXP2(", " COS(", " SIN(",                  // 0xA8 - 0xAB
               " TAN(", " ATN(", " BASIC@(", " DATE(",                // 0xAC - 0xAF
               " EOFMARK(", " FILTYP(", " FIX(", " FREMEM(",          // 0XB0 - 0xB3
               " NEGATE(", " PEEK(", " ROUND(", " TASKREC%(",         // 0xB4 - 0xB7
               " TASKREC@(", " TIME(", " UIR(", " STR$(",             // 0xB8 - 0xBB
               " HEX$(", " PFX$(", " SPACE$(", " ERRTXT$(",           // 0xBC - 0xBF
               " CHR$(", " RELATION(", " ANU(", " COMPI(",            // 0xC0 - 0xC3
               " SCALB(", " SCALE(", " LEN(", " VAL(",                // 0xC4 - 0xC7
               " ASC(", " UCASE$(", "TEN(", " CONV#(",                // 0xC8 - 0xCB
               " CONV@(", " CONV(", " CONV&(", " CONV$",              // 0xCC - 0xCF
               " CONV%(", " LEFT$(", " RIGHT$(", " REP$(",            // 0xD0 - 0xD3
               " MID$(", " INSTR(", "VARPTR(", "VARPTR$(",            // 0xD4 - 0xD7
               "VAR$(", "VAR(", " UBOUND(", " FILE(",                 // 0xD8 - 0xDB
               " EXEVENT@(", " DD ", " DE ", " DF ",                  // 0xDC - 0xDF
               " HPOS ", " VPOS ", " TIME$ ", " DATE$ ",              // 0xE0 - 0xE3
               " PREFIX$ ", " E5 ", " OUTREC ", " INDENT ",           // 0xE4 - 0xE7
               " SHOWDIGITS ", " LISTTAB ", " AUXID@ ", " EXFN ",     // 0xE8 - 0xEB
               " SECONDS@ ", " FRE ", " ERRLIN ", "ERR ",             // 0xEC - 0xEF
               " KBD ", " EOF ", " JOYY ", " PDL9 ",                  // 0xF0 - 0xF3
               " PI ", " ERRTOOL ", " F6 ", " F7 ",                   // 0xF4 - 0xF7
               " F8 ", " F9 ", " FA ", " FB ",                        // 0xF8 - 0xFB
               " FC ", " FD ", " FE ", " FF ",                        // 0xFC - 0xFF
  };

  private final List<SourceLine> sourceLines = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public BasicProgramGS (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    // need to validate these files - seem to sometimes contain palette files
    // 0132 816-Paint.po

    int ptr = 5;
    while (ptr < buffer.length)
    {
      SourceLine sourceLine = new SourceLine (ptr);

      if (sourceLine.lineNumber == 0)
        break;

      sourceLines.add (sourceLine);
      ptr += sourceLine.length;
    }

  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    for (SourceLine sourceLine : sourceLines)
      text.append (sourceLine + "\n");
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private class SourceLine
  // ---------------------------------------------------------------------------------//
  {
    int lineNumber;
    int length;
    String label;
    String line;

    public SourceLine (int ptr)
    {
      int labelLength = buffer[ptr] & 0xFF;
      if (labelLength > 1)
        label = new String (buffer, ptr + 1, labelLength - 1);
      else
        label = "";
      ptr += labelLength;

      int lineLength = buffer[ptr] & 0xFF;
      lineNumber = Utility.intValue (buffer[ptr + 1], buffer[ptr + 2]);
      length = labelLength + lineLength;

      if (lineNumber == 0)
        return;

      ptr += 3;
      int max = ptr + lineLength - 4;
      StringBuilder text = new StringBuilder ();

      while (ptr < max)
      {
        byte b1 = buffer[ptr++];
        if (isHighBitSet (b1))
          ptr = tokenOrNumber (b1, text, ptr);
        else
          text.append ((b1 & 0xFF) < 32 ? '.' : (char) b1);
      }
      line = text.toString ();
    }

    private int tokenOrNumber (byte b1, StringBuilder text, int ptr)
    {
      if (b1 == (byte) 0xDF)
      {
        append (tokensDF[(buffer[ptr++] & 0x7F)], text);
        return ptr;
      }

      if (b1 < (byte) 0xE0)
      {
        append (tokens[(b1 & 0x7F)], text);
        return ptr;
      }

      if (b1 == (byte) 0xFC)             // 3 bytes
      {
        text.append (get (ptr, 3));
        return ptr + 4;                  // skip trailing zero
      }

      if (b1 == (byte) 0xFA)             // 2 bytes
      {
        text.append (get (ptr, 2));
        return ptr + 2;
      }

      if (b1 == (byte) 0xFB)
      {
        return ptr + 3;                  // ignore next 3 bytes (why?)
      }

      if ((b1 & 0xF0) == 0xF0)           // F0:F9 = 0:9
      {
        text.append (b1 & 0x0F);
        return ptr;
      }

      if ((b1 & 0xE0) == 0xE0)           // 3 nybbles
      {
        text.append (((b1 & 0x0F) << 8) | (buffer[ptr++] & 0xFF));
        return ptr;
      }

      System.out.printf ("not handled: %02X%n", b1);
      return ptr;
    }

    private void append (String word, StringBuilder text)
    {
      if (word.startsWith (" ") && text.length () > 0
          && text.charAt (text.length () - 1) == ' ')
        text.deleteCharAt (text.length () - 1);
      text.append (word);
    }

    private int get (int ptr, int size)
    {
      int val = 0;
      for (int i = 0; i < size; i++)
        val |= (buffer[ptr++] & 0xFF) << (i * 8);
      return val;
    }

    private void show (int ptr, int size)
    {
      for (int i = 0; i < size; i++)
        System.out.printf (" %02X ", buffer[ptr++]);
      System.out.println ();
    }

    @Override
    public String toString ()
    {
      return String.format ("%5d %-12s %s", lineNumber, label, line);
    }
  }
}
