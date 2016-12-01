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

  // add 'nib' to TreeBuilder to allow nib files to be selected

  public NibDisk (File file)
  {
    this.file = file;
    byte[] trackBuffer = new byte[6656];
    try
    {
      BufferedInputStream in = new BufferedInputStream (new FileInputStream (file));

      byte[] test = { (byte) 0xD5, (byte) 0xAA, (byte) 0xB5 };
      for (int i = 0; i < 35; i++)
      {
        in.read (trackBuffer);
        //        System.out.println (HexFormatter.format (trackBuffer));
        int offset = 0;
        while (true)
        {
          offset = nibbler.findBytes (trackBuffer, offset, test);
          if (offset < 0)
            break;
          System.out.printf ("found at %04X%n", offset);
          ++offset;
        }
        break;        // just examine the first track
      }

      in.close ();
    }
    catch (IOException e)
    {
      e.printStackTrace ();
    }
  }
}