package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class PascalArea extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  int size;
  int volumes;
  String ppmName;
  int start;
  int length;
  int defaultUnit;
  boolean writeProtected;
  int oldDriver;

  // ---------------------------------------------------------------------------------//
  public PascalArea (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    size = Utility.getShort (buffer, 0);
    volumes = Utility.getShort (buffer, 2);
    ppmName = HexFormatter.getPascalString (buffer, 4);
    start = Utility.getShort (buffer, 8);
    length = Utility.getShort (buffer, 11);
    defaultUnit = buffer[13] & 0xFF;
    oldDriver = Utility.getShort (buffer, 14);
    //    writeProtected = buffer[12] != 0;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (getHeader ());

    text.append (String.format ("Size ............. %04X  (%<,d)%n", size));
    text.append (String.format ("Volumes .......... %04X%n", volumes));
    text.append (String.format ("PPM .............. %s%n", ppmName));
    text.append (String.format ("Start ............ %04X  (%<,d)%n", start));
    text.append (String.format ("Length ........... %04X  (%<,d)%n", length));
    text.append (String.format ("Default Unit ..... %d%n", defaultUnit));
    text.append (
        String.format ("Write Protected .. %s%n", writeProtected ? "True" : "False"));
    text.append (String.format ("Old driver ....... %04X  (%<,d)%n", oldDriver));

    return text.toString ();
  }
}
