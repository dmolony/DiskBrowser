package com.bytezone.diskbrowser.applefile;

public class SHRPictureFile2 extends HiResImage
{

  public SHRPictureFile2 (String name, byte[] buffer, int fileType, int auxType, int eof)
  {
    super (name, buffer, fileType, auxType, eof);

    // type $C0.0001 - packed SHR
    // type $C1.0001 - unpacked SHR
    // type $C1.0002 - 
    //    System.out.println (buffer.length);
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