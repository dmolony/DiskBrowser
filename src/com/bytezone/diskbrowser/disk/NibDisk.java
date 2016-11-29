package com.bytezone.diskbrowser.disk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class NibDisk
{
  private final Nibblizer nibbler = new Nibblizer ();

  final File file;
  int tracks;
  int actualTracks;

  final byte[] buffer = new byte[4096 * 35];

  // .nib files are 232,960 bytes
  // 6,656 bytes x 35 tracks (0x1A00)

  public NibDisk (File file)
  {
    this.file = file;
    byte[] buffer = new byte[6656];
    try
    {
      byte[] diskBuffer = new byte[10];
      BufferedInputStream in = new BufferedInputStream (new FileInputStream (file));

      for (int i = 0; i < 35; i++)
      {
        in.read (buffer);
        //        System.out.println (HexFormatter.format (buffer));
      }

      in.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }
}