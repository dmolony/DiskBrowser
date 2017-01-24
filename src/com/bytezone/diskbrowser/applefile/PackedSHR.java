package com.bytezone.diskbrowser.applefile;

public class PackedSHR extends HiResImage
{

  public PackedSHR (String name, byte[] buffer, int fileType, int auxType)
  {
    super (name, buffer, fileType, auxType);
  }

  @Override
  protected void createMonochromeImage ()
  {
  }

  @Override
  protected void createColourImage ()
  {
  }

}