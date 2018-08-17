package com.bytezone.diskbrowser.nib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class NibFile
{
  //  private final Nibblizer nibbler;

  public final File file;
  int tracks;
  int actualTracks;

  final byte[] diskBuffer = new byte[4096 * 35];

  // .nib files are 232,960 bytes
  // 6,656 bytes x 35 tracks (0x1A00)

  // add 'nib' to Utility.suffixes to allow nib files to be selected

  public NibFile (File file)
  {
    this.file = file;
    byte[] trackBuffer = new byte[6656];
    //    nibbler = new Nibblizer (file);

    try
    {
      BufferedInputStream in = new BufferedInputStream (new FileInputStream (file));

      byte[] test = { (byte) 0xD5, (byte) 0xAA, (byte) 0xB5 };
      for (int i = 0; i < 35; i++)
      {
        in.read (trackBuffer);
        int offset = 0;
        while (true)
        {
          offset = Nibblizer.findBytes (trackBuffer, offset, test);
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

  // ---------------------------------------------------------------------------------//
  // getDiskBuffer
  // ---------------------------------------------------------------------------------//

  public byte[] getDiskBuffer ()
  {
    return diskBuffer;
  }
}