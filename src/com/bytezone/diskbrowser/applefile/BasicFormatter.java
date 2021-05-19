package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.getShort;

import java.util.List;

import com.bytezone.diskbrowser.gui.BasicPreferences;

// -----------------------------------------------------------------------------------//
public abstract class BasicFormatter implements ApplesoftConstants
// -----------------------------------------------------------------------------------//
{
  static final String NEWLINE = "\n";

  ApplesoftBasicProgram program;
  BasicPreferences basicPreferences;
  byte[] buffer;
  List<SourceLine> sourceLines;

  // ---------------------------------------------------------------------------------//
  public BasicFormatter (ApplesoftBasicProgram program, BasicPreferences basicPreferences)
  // ---------------------------------------------------------------------------------//
  {
    this.program = program;
    this.basicPreferences = basicPreferences;
    this.buffer = program.getBuffer ();
    this.sourceLines = program.getSourceLines ();
  }

  // ---------------------------------------------------------------------------------//
  public abstract void append (StringBuilder fullText);
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  int getLoadAddress ()
  // ---------------------------------------------------------------------------------//
  {
    return (buffer.length > 3) ? getShort (buffer, 0) - getFirstLineLength () : 0;
  }

  // ---------------------------------------------------------------------------------//
  private int getFirstLineLength ()
  // ---------------------------------------------------------------------------------//
  {
    int linkField = getShort (buffer, 0);
    if (linkField == 0)
      return 2;

    int ptr = 4;               // skip link field and line number

    while (ptr < buffer.length && buffer[ptr++] != 0)
      ;

    return ptr;
  }
}
