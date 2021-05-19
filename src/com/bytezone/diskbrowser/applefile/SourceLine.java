package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.ASCII_COLON;
import static com.bytezone.diskbrowser.utilities.Utility.ASCII_QUOTE;
import static com.bytezone.diskbrowser.utilities.Utility.getShort;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class SourceLine implements ApplesoftConstants
// -----------------------------------------------------------------------------------//
{
  ApplesoftBasicProgram program;
  int linkField;
  int lineNumber;
  int linePtr;
  int length;
  byte[] buffer;

  List<SubLine> sublines = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  SourceLine (ApplesoftBasicProgram program, byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    this.program = program;
    this.buffer = buffer;

    linePtr = ptr;
    linkField = getShort (buffer, ptr);
    lineNumber = getShort (buffer, ptr + 2);

    int startPtr = ptr += 4;              // skip link field and lineNumber
    boolean inString = false;             // can toggle
    boolean inRemark = false;             // can only go false -> true
    byte b;

    while (ptr < buffer.length && (b = buffer[ptr++]) != 0)
    {
      if (inRemark)                       // cannot terminate a REM
        continue;

      if (inString)
      {
        if (b == ASCII_QUOTE)             // terminate string
          inString = false;
        continue;
      }

      switch (b)
      {
        // break IF statements into two sublines (allows for easier line indenting)
        case TOKEN_IF:
          while (buffer[ptr] != TOKEN_THEN && buffer[ptr] != TOKEN_GOTO
              && buffer[ptr] != 0)
            ptr++;

          if (buffer[ptr] == TOKEN_THEN)          // keep THEN with the IF
            ++ptr;

          startPtr = addSubLine (startPtr, ptr);  // create subline from the condition 
          break;

        case ASCII_COLON:                         // end of subline
          startPtr = addSubLine (startPtr, ptr);
          break;

        case TOKEN_REM:
          if (ptr == startPtr + 1)                // at start of line
            inRemark = true;
          else                                    //  mid-line - should be illegal
          {
            System.out.printf ("%s : %5d mid-line REM token%n", program.name, lineNumber);
            startPtr = addSubLine (startPtr, --ptr);    // point back to the REM
          }
          break;

        case Utility.ASCII_QUOTE:
          inString = true;
          break;
      }
    }

    length = ptr - linePtr;

    addSubLine (startPtr, ptr);
  }

  // ---------------------------------------------------------------------------------//
  private int addSubLine (int startPtr, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    sublines.add (new SubLine (this, startPtr, ptr - startPtr));
    return ptr;
  }
}
