package com.bytezone.diskbrowser.applefile;

public class DeviceDriver extends AbstractFile
{
  private final int auxType;
  private final int classifications;
  private final int driverClass;
  private final boolean inactive;

  public DeviceDriver (String name, byte[] buffer, int auxType)
  {
    super (name, buffer);
    this.auxType = auxType;

    classifications = auxType & 0xFF;
    driverClass = (auxType & 0x7F00) >>> 8;
    inactive = (auxType & 0x8000) != 0;
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");

    text.append ("Apple IIGS Device Driver File\n\n");

    text.append (String.format ("Classifications ... %02X%n", classifications));
    text.append (String.format ("Driver Class ...... %02X%n", driverClass));
    text.append (String.format ("Aux type .......... %d%n", auxType));
    text.append (String.format ("Inactive .......... %s%n", inactive ? "True" : "False"));

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}