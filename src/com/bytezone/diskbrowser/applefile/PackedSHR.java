package com.bytezone.diskbrowser.applefile;

public class PackedSHR extends HiResImage
{

  public PackedSHR (String name, byte[] buffer, int fileType, int auxType)
  {
    super (name, buffer, fileType, auxType);

    System.out.println ("SHR aux=1");
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