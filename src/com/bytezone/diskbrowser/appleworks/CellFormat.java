package com.bytezone.diskbrowser.appleworks;

public class CellFormat
{
  boolean labelAllowed;
  boolean valueAllowed;
  boolean display;
  boolean standard;
  boolean fixed;
  boolean dollars;
  boolean commas;
  boolean percent;
  boolean appropriate;
  int decimals;

  public CellFormat (byte format)
  {
    display = (format & 0x40) == 0;
    labelAllowed = (format & 0x10) == 0;
    valueAllowed = (format & 0x08) == 0;

    int formatting = format & 0x07;

    standard = formatting == 1;
    fixed = formatting == 2;
    dollars = formatting == 3;
    commas = formatting == 4;
    percent = formatting == 5;
    appropriate = formatting == 6;
  }

  public CellFormat (byte format, byte decimals)
  {
    this (format);
    this.decimals = decimals & 0x07;
  }

  public String mask ()
  {
    String fmt = dollars ? "$%" : "%";
    if (commas)
      fmt += ",";
    fmt += "12." + decimals;
    fmt += "f";
    if (percent)
      fmt += "%%";

    return fmt;
  }
}
