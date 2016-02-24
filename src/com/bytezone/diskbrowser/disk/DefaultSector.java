package com.bytezone.diskbrowser.disk;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class DefaultSector extends AbstractSector
{
  String name;

  public DefaultSector (String name, Disk disk, byte[] buffer)
  {
    super (disk, buffer);
    this.name = name;
  }

  @Override
  public String createText ()
  {
    return name + "\n\n" + HexFormatter.format (buffer, 0, buffer.length);
  }

  @Override
  public String toString ()
  {
    return name;
  }
}