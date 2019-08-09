package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class BasicProgramGS extends BasicProgram
{
  private final List<SourceLine> sourceLines = new ArrayList<> ();

  public BasicProgramGS (String name, byte[] buffer)
  {
    super (name, buffer);

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

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();
    for (SourceLine sourceLine : sourceLines)
      text.append (sourceLine + "\n");
    return text.toString ();
  }

  private class SourceLine
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
      lineNumber = HexFormatter.intValue (buffer[ptr + 1], buffer[ptr + 2]);
      length = labelLength + lineLength;

      if (lineNumber == 0)
        return;

      ptr += 3;
      int max = ptr + lineLength - 4;
      StringBuilder text = new StringBuilder (String.format ("%-12s :", label));

      while (ptr < max)
      {
        byte b = buffer[ptr++];
        if (isToken (b))
        {
          switch (b & 0xFF)
          {
            case 0x80:
              text.append (" AUTO ");
              break;
            case 0x81:
              text.append (" DEL ");
              break;
            case 0x82:
              text.append (" EDIT ");
              break;
            case 0x83:
              text.append (" HLIST ");
              break;
            case 0x84:
              text.append (" LIST ");
              break;
            case 0x85:
              text.append (" RENUM ");
              break;
            case 0x86:
              text.append (" BREAK ");
              break;
            case 0x87:
              text.append (" FN ");
              break;
            case 0x88:
              text.append (" PROC ");
              break;
            case 0x89:
              text.append (" GOSUB ");
              break;
            case 0x8A:
              text.append (" GOTO ");
              break;
            case 0x8B:
              text.append (" FOR ");
              break;
            case 0x8C:
              text.append (" THEN ");
              break;
            case 0x8D:
              text.append (" ELSE ");
              break;
            case 0x8E:
              text.append (" NEXT ");
              break;
            case 0x8F:
              text.append (" OFF ");
              break;
            case 0x90:
              text.append (" ON ");
              break;
            case 0x91:
              text.append (" INPUT ");
              break;
            case 0x92:
              text.append (" OUTPUT ");
              break;
            case 0x93:
              text.append (" TEXT ");
              break;
            case 0x94:
              text.append (" TIMER ");
              break;
            case 0x95:
              text.append (" EXCEPTION ");
              break;
            case 0x96:
              text.append (" CAT ");
              break;
            case 0x98:
              text.append (" INIT ");
              break;
            case 0x99:
              text.append (" INVOKE ");
              break;
            case 0x9A:
              text.append (" LIBRARY ");
              break;
            case 0x9B:
              text.append (" PREFIX ");
              break;
            case 0x9C:
              text.append (" TYPE ");
              break;
            case 0x9D:
              text.append (" LOAD ");
              break;
            case 0x9E:
              text.append (" SAVE ");
              break;
            case 0x9F:
              text.append (" DELETE ");
              break;
            case 0xA0:
              text.append (" RUN ");
              break;
            case 0xA1:
              text.append (" RENAME ");
              break;
            case 0xA2:
              text.append (" CREATE ");
              break;
            case 0xA3:
              text.append (" LOCK ");
              break;
            case 0xA4:
              text.append (" UNLOCK ");
              break;
            case 0xA5:
              text.append (" EXEC ");
              break;
            case 0xA6:
              text.append (" CHAIN ");
              break;
            case 0xA7:
              text.append (" CATALOG ");
              break;
            case 0xA8:
              text.append (" OPEN ");
              break;
            case 0xA9:
              text.append (" QUIT ");
              break;
            case 0xAA:
              text.append (" DIR ");
              break;
            case 0xAB:
              text.append (" DIM ");
              break;
            case 0xAC:
              text.append (" READ ");
              break;
            case 0xAD:
              text.append (" WRITE ");
              break;
            case 0xAE:
              text.append (" CLOSE ");
              break;
            case 0xAF:
              text.append (" TASKPOLL ");
              break;
            case 0xB0:
              text.append (" LOCATE ");
              break;
            case 0xB1:
              text.append (" EVENTDEF ");
              break;
            case 0xB2:
              text.append (" MENUDEF ");
              break;
            case 0xB3:
              text.append (" VOLUMES ");
              break;
            case 0xB4:
              text.append (" CALL% ");
              break;
            case 0xB5:
              text.append (" CALL ");
              break;
            case 0xB6:
              text.append (" _ ");
              break;
            case 0xB7:
              text.append (" TEXTPORT ");
              break;
            case 0xB8:
              text.append (" PERFORM ");
              break;
            case 0xB9:
              text.append (" GRAF ");
              break;
            case 0xBA:
              text.append (" DBUG ");
              break;
            case 0xBB:
              text.append (" POP ");
              break;
            case 0xBC:
              text.append (" HOME ");
              break;
            case 0xBD:
              text.append (" SUB$( ");
              break;
            case 0xBE:
              text.append (" TRACE ");
              break;
            case 0xBF:
              text.append (" NOTRACE ");
              break;
            case 0xC0:
              text.append (" NORMAL ");
              break;
            case 0xC1:
              text.append (" INVERSE ");
              break;
            case 0xC2:
              text.append (" RESUME ");
              break;
            case 0xC3:
              text.append (" LET ");
              break;
            case 0xC4:
              text.append (" IF ");
              break;
            case 0xC5:
              text.append (" RESTORE ");
              break;
            case 0xC6:
              text.append (" SWAP ");
              break;
            case 0xC7:
              text.append (" RETURN ");
              break;
            case 0xC8:
              text.append (" REM ");
              break;
            case 0xC9:
              text.append (" STOP ");
              break;
            case 0xCA:
              text.append (" DATA ");
              break;
            case 0xCB:
              text.append (" IMAGE ");
              break;
            case 0xCC:
              text.append (" LIBFIND ");
              break;
            case 0xCD:                        // gimme some skin
              text.append (" DEF ");
              break;
            case 0xCE:
              text.append (" PRINT ");
              break;
            case 0xCF:
              text.append (" CLEAR ");
              break;
            case 0xD0:
              text.append (" RANDOMIZE ");
              break;
            case 0xD1:
              text.append (" NEW ");
              break;
            case 0xD2:
              text.append (" POKE ");
              break;
            case 0xD3:
              text.append (" ASSIGN ");
              break;
            case 0xD4:
              text.append (" GET ");
              break;
            case 0xD5:
              text.append (" PUT ");
              break;
            case 0xD6:
              text.append (" SET ");
              break;
            case 0xD7:
              text.append (" ERROR ");
              break;
            case 0xD8:
              text.append (" ERASE ");
              break;
            case 0xD9:
              text.append (" LOCAL ");
              break;
            case 0xDA:
              text.append (" WHILE ");
              break;
            case 0xDB:
              text.append (" CONT ");
              break;
            case 0xDC:
              text.append (" DO ");
              break;
            case 0xDD:
              text.append (" UNTIL ");
              break;
            case 0xDE:
              text.append (" END ");
              break;
            case 0xDF:
              switch (buffer[ptr] & 0xFF)
              {
                case 0x80:
                  text.append (" TAB( ");
                  break;
                case 0x81:
                  text.append (" TO ");
                  break;
                case 0x82:
                  text.append (" SPC( ");
                  break;
                case 0x83:
                  text.append (" USING ");
                  break;
                case 0x84:
                  text.append (" APPEND ");
                  break;
                case 0x85:
                  text.append (" MOD ");
                  break;
                case 0x86:
                  text.append (" REMDR ");
                  break;
                case 0x87:
                  text.append (" STEP ");
                  break;
                case 0x88:
                  text.append (" AND ");
                  break;
                case 0x89:
                  text.append (" OR ");
                  break;
                case 0x8A:
                  text.append (" XOR ");
                  break;
                case 0x8B:
                  text.append (" DIV ");
                  break;
                case 0x8C:
                  text.append (" SRC ");
                  break;
                case 0x8D:
                  text.append (" NOT ");
                  break;
                case 0x8F:
                  text.append (" UPDATE ");
                  break;
                case 0x90:
                  text.append (" TXT ");
                  break;
                case 0x91:
                  text.append (" BDF ");
                  break;
                case 0x92:
                  text.append (" FILTYPE= ");
                  break;
                case 0x93:
                  text.append (" AS ");
                  break;
                case 0x96:
                  text.append (" SGN( ");
                  break;
                case 0x97:
                  text.append (" INT ");
                  break;
                case 0x98:
                  text.append (" ABS( ");
                  break;
                case 0x99:
                  text.append (" TYP( ");
                  break;
                case 0x9A:
                  text.append (" REC( ");
                  break;
                case 0x9B:
                  text.append (" JOYX( ");
                  break;
                case 0x9C:
                  text.append (" PDL( ");
                  break;
                case 0x9D:
                  text.append (" BTN( ");
                  break;
                case 0x9E:
                  text.append (" R.STACK%( ");
                  break;
                case 0x9F:
                  text.append (" R.STACK@( ");
                  break;
                case 0xA0:
                  text.append (" R.STACK&( ");
                  break;
                case 0xA1:
                  text.append (" SQR( ");
                  break;
                case 0xA2:
                  text.append (" RND( ");
                  break;
                case 0xA3:
                  text.append (" LOG( ");
                  break;
                case 0xA4:
                  text.append (" LOG1( ");
                  break;
                case 0xA5:
                  text.append (" LOG2( ");
                  break;
                case 0xA6:
                  text.append (" LOGB%( ");
                  break;
                case 0xA7:
                  text.append (" EXP( ");
                  break;
                case 0xA8:
                  text.append (" EXP1( ");
                  break;
                case 0xA9:
                  text.append (" EXP2( ");
                  break;
                case 0xAA:
                  text.append (" COS( ");
                  break;
                case 0xAB:
                  text.append (" SIN( ");
                  break;
                case 0xAC:
                  text.append (" TAN( ");
                  break;
                case 0xAD:
                  text.append (" ATN( ");
                  break;
                case 0xAE:
                  text.append (" BASIC@( ");
                  break;
                case 0xAF:
                  text.append (" DATE( ");
                  break;
                case 0xB0:
                  text.append (" EOFMARK( ");
                  break;
                case 0xB1:
                  text.append (" FILTYP( ");
                  break;
                case 0xB2:
                  text.append (" FIX( ");
                  break;
                case 0xB3:
                  text.append (" FREMEM( ");
                  break;
                case 0xB4:
                  text.append (" NEGATE( ");
                  break;
                case 0xB5:
                  text.append (" PEEK( ");
                  break;
                case 0xB6:
                  text.append (" ROUND( ");
                  break;
                case 0xB7:
                  text.append (" TASKREC%( ");
                  break;
                case 0xB8:
                  text.append (" TASKREC@( ");
                  break;
                case 0xB9:
                  text.append (" TIME( ");
                  break;
                case 0xBA:
                  text.append (" UIR( ");
                  break;
                case 0xBB:
                  text.append (" STR$( ");
                  break;
                case 0xBC:
                  text.append (" HEX$( ");
                  break;
                case 0xBD:
                  text.append (" PFX$( ");
                  break;
                case 0xBE:
                  text.append (" SPACE$( ");
                  break;
                case 0xBF:
                  text.append (" ERRTXT$( ");
                  break;
                case 0xC0:
                  text.append (" CHR$( ");
                  break;
                case 0xC1:
                  text.append (" RELATION( ");
                  break;
                case 0xC2:
                  text.append (" ANU( ");
                  break;
                case 0xC3:
                  text.append (" COMPI( ");
                  break;
                case 0xC4:
                  text.append (" SCALB( ");
                  break;
                case 0xC5:
                  text.append (" SCALE( ");
                  break;
                case 0xC6:
                  text.append (" LEN( ");
                  break;
                case 0xC7:
                  text.append (" VAL( ");
                  break;
                case 0xC8:
                  text.append (" ASC( ");
                  break;
                case 0xC9:
                  text.append (" UCASE$( ");
                  break;
                case 0xCA:
                  text.append (" TEN( ");
                  break;
                case 0xCB:
                  text.append (" CONV#( ");
                  break;
                case 0xCC:
                  text.append (" CONV@( ");
                  break;
                case 0xCD:
                  text.append (" CONV( ");
                  break;
                case 0xCE:
                  text.append (" CONV&( ");
                  break;
                case 0xCF:
                  text.append (" CONV$ ");
                  break;
                case 0xD0:
                  text.append (" CONV%( ");
                  break;
                case 0xD1:
                  text.append (" LEFT$( ");
                  break;
                case 0xD2:
                  text.append (" RIGHT$( ");
                  break;
                case 0xD3:
                  text.append (" REP$( ");
                  break;
                case 0xD4:
                  text.append (" MID$( ");
                  break;
                case 0xD5:
                  text.append (" INSTR( ");
                  break;
                case 0xD6:
                  text.append (" VARPTR( ");
                  break;
                case 0xD7:
                  text.append (" VARPTR$( ");
                  break;
                case 0xD8:
                  text.append (" VAR$( ");
                  break;
                case 0xD9:
                  text.append (" VAR( ");
                  break;
                case 0xDA:
                  text.append (" UBOUND( ");
                  break;
                case 0xDB:
                  text.append (" FILE( ");
                  break;
                case 0xDC:
                  text.append (" EXEVENT@( ");
                  break;
                case 0xE0:
                  text.append (" HPOS ");
                  break;
                case 0xE1:
                  text.append (" VPOS ");
                  break;
                case 0xE2:
                  text.append (" TIME$ ");
                  break;
                case 0xE3:
                  text.append (" DATE$ ");
                  break;
                case 0xE4:
                  text.append (" PREFIX$ ");
                  break;
                case 0xE6:
                  text.append (" OUTREC ");
                  break;
                case 0xE7:
                  text.append (" INDENT ");
                  break;
                case 0xE8:
                  text.append (" SHOWDIGITS ");
                  break;
                case 0xE9:
                  text.append (" LISTTAB ");
                  break;
                case 0xEA:
                  text.append (" AUXID@ ");
                  break;
                case 0xEB:
                  text.append (" EXFN ");
                  break;
                case 0xEC:
                  text.append (" SECONDS@ ");
                  break;
                case 0xED:
                  text.append (" FRE ");
                  break;
                case 0xEE:
                  text.append (" ERRLIN ");
                  break;
                case 0xEF:
                  text.append (" ERR ");
                  break;
                case 0xF0:
                  text.append (" KBD ");
                  break;
                case 0xF1:
                  text.append (" EOF ");
                  break;
                case 0xF2:
                  text.append (" JOYY ");
                  break;
                case 0xF3:
                  text.append (" PDL9 ");
                  break;
                case 0xF4:
                  text.append (" PI ");
                  break;
                case 0xF5:
                  text.append (" ERRTOOL ");
                  break;
              }
              break;
            default:
              text.append (String.format (" %02X ", b));
          }
        }
        else
        {
          if ((b & 0xFF) < 32)
            text.append ('.');
          else
            text.append ((char) b);
        }
      }
      line = text.toString ();
    }

    @Override
    public String toString ()
    {
      return String.format ("%5d %s", lineNumber, line);
    }
  }
}
