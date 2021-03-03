package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class ApplesoftBasicProgram extends BasicProgram implements ApplesoftConstants
// -----------------------------------------------------------------------------------//
{
  private final List<SourceLine> sourceLines = new ArrayList<> ();
  private final int endPtr;

  private UserBasicFormatter userBasicFormatter;
  private AppleBasicFormatter appleBasicFormatter;
  private DebugBasicFormatter debugBasicFormatter;
  private XrefFormatter xrefFormatter;

  // ---------------------------------------------------------------------------------//
  public ApplesoftBasicProgram (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    int ptr = 0;

    while (buffer[ptr + 1] != 0)    // msb of link field
    {
      SourceLine line = new SourceLine (this, buffer, ptr);
      sourceLines.add (line);
      ptr += line.length;           // assumes lines are contiguous
    }

    endPtr = ptr;                   // record where the end-of-program marker is

    userBasicFormatter = new UserBasicFormatter (this, basicPreferences);
    appleBasicFormatter = new AppleBasicFormatter (this, basicPreferences);
    debugBasicFormatter = new DebugBasicFormatter (this, basicPreferences);
    xrefFormatter = new XrefFormatter (this, basicPreferences);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    if (basicPreferences.showHeader)
      addHeader (text);

    if (showDebugText)
    {
      debugBasicFormatter.format (text);
      return Utility.rtrim (text);
    }

    if (sourceLines.size () == 0)
    {
      text.append ("\n\nThis page intentionally left blank");
      return text.toString ();
    }

    if (basicPreferences.formatApplesoft)
      userBasicFormatter.format (text);
    else
      appleBasicFormatter.format (text);

    if (basicPreferences.showAllXref)
      xrefFormatter.format (text);

    return Utility.rtrim (text);
  }

  // ---------------------------------------------------------------------------------//
  List<SourceLine> getSourceLines ()
  // ---------------------------------------------------------------------------------//
  {
    return sourceLines;
  }

  // ---------------------------------------------------------------------------------//
  byte[] getBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  int getEndPtr ()
  // ---------------------------------------------------------------------------------//
  {
    return endPtr;
  }

  // ---------------------------------------------------------------------------------//
  private void addHeader (StringBuilder pgm)
  // ---------------------------------------------------------------------------------//
  {
    pgm.append ("Name    : " + name + "\n");
    pgm.append (String.format ("Length  : $%04X (%<,d)%n", buffer.length));
    pgm.append (String.format ("Load at : $%04X (%<,d)%n%n",
        BasicFormatter.getLoadAddress (buffer)));
  }
}