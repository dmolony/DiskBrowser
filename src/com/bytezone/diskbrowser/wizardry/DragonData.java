package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;

public class DragonData extends AbstractFile
{
  public DragonData (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  @Override
  public String getText ()
  {
    return "DragonData";
  }
}
