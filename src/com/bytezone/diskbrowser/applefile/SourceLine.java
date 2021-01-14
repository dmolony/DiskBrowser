package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class SourceLine implements ApplesoftConstants
// -----------------------------------------------------------------------------------//
{
  ApplesoftBasicProgram parent;
  int lineNumber;
  int linePtr;
  int length;
  byte[] buffer;

  List<SubLine> sublines = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  SourceLine (ApplesoftBasicProgram parent, byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    this.parent = parent;
    this.buffer = buffer;

    linePtr = ptr;
    lineNumber = Utility.unsignedShort (buffer, ptr + 2);

    int startPtr = ptr += 4;            // skip link to next line and lineNumber
    boolean inString = false;           // can toggle
    boolean inRemark = false;           // can only go false -> true
    byte b;

    while (ptr < buffer.length && (b = buffer[ptr++]) != 0)
    {
      if (inRemark)                     // cannot terminate a REM
        continue;

      if (inString)
      {
        if (b == Utility.ASCII_QUOTE)           // terminate string
          inString = false;
        continue;
      }

      switch (b)
      {
        // break IF statements into two sublines (allows for easier line indenting)
        case ApplesoftConstants.TOKEN_IF:
          // skip to THEN or GOTO - if not found then it's an error
          while (buffer[ptr] != TOKEN_THEN && buffer[ptr] != TOKEN_GOTO
              && buffer[ptr] != 0)
            ptr++;

          // keep THEN with the IF
          if (buffer[ptr] == TOKEN_THEN)
            ++ptr;

          // create subline from the condition (and THEN if it exists)
          sublines.add (new SubLine (this, startPtr, ptr - startPtr));
          startPtr = ptr;

          break;

        // end of subline, so add it, advance startPtr and continue
        case Utility.ASCII_COLON:
          sublines.add (new SubLine (this, startPtr, ptr - startPtr));
          startPtr = ptr;
          break;

        case TOKEN_REM:
          if (ptr != startPtr + 1)      // REM appears mid-line (should follow a colon)
          {
            System.out.println ("mid-line REM token");
            sublines.add (new SubLine (this, startPtr, (ptr - startPtr) - 1));
            startPtr = ptr - 1;
          }
          else
            inRemark = true;

          break;

        case Utility.ASCII_QUOTE:
          inString = true;
          break;
      }
    }

    // add whatever is left
    sublines.add (new SubLine (this, startPtr, ptr - startPtr));
    this.length = ptr - linePtr;
  }
}
