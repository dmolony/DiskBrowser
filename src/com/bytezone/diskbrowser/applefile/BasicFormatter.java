package com.bytezone.diskbrowser.applefile;

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
  int endPtr;

  // ---------------------------------------------------------------------------------//
  public BasicFormatter (ApplesoftBasicProgram program, BasicPreferences basicPreferences)
  // ---------------------------------------------------------------------------------//
  {
    this.program = program;
    this.basicPreferences = basicPreferences;
    this.buffer = program.getBuffer ();
    this.sourceLines = program.getSourceLines ();
    this.endPtr = program.getEndPtr ();
  }

  // ---------------------------------------------------------------------------------//
  public abstract void format (StringBuilder fullText);
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  int getLoadAddress ()
  // ---------------------------------------------------------------------------------//
  {
    return program.getLoadAddress ();
  }
}
