package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.unsignedShort;

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
  public abstract void format (StringBuilder fullText);
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  int getLoadAddress ()
  // ---------------------------------------------------------------------------------//
  {
    return (buffer.length > 1) ? unsignedShort (buffer, 0) - getLineLength (0) : 0;
  }

  // ---------------------------------------------------------------------------------//
  private int getLineLength (int ptr)
  // ---------------------------------------------------------------------------------//
  {
    int linkField = unsignedShort (buffer, ptr);
    if (linkField == 0)
      return 2;

    ptr += 4;               // skip link field and line number
    int length = 5;

    while (ptr < buffer.length && buffer[ptr++] != 0)
      length++;

    assert length == ptr;
    return length;
  }
}
