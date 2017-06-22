package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class BootSector extends AbstractSector
{
  private static final byte[] skew = { 0x00, 0x0D, 0x0B, 0x09, 0x07, 0x05, 0x03, 0x01,
                                       0x0E, 0x0C, 0x0A, 0x08, 0x06, 0x04, 0x02, 0x0F };
  private static final int SKEW_OFFSET = 0x4D;

  AssemblerProgram assembler1;
  AssemblerProgram assembler2;
  String name;                                        // DOS or Prodos

  public BootSector (Disk disk, byte[] buffer, String name, DiskAddress diskAddress)
  {
    super (disk, buffer, diskAddress);
    this.name = name;
  }

  public BootSector (Disk disk, byte[] buffer, String name)
  {
    super (disk, buffer);
    this.name = name;
  }

  @Override
  public String createText ()
  {
    StringBuilder text = new StringBuilder ();

    if (assembler1 == null)
    {
      int flag = buffer[0] & 0xFF;
      if (flag == 1)                                // apple II
      {
        if (matches (buffer, SKEW_OFFSET, skew))
        {
          int newLen = 0x100 - (SKEW_OFFSET + skew.length);
          byte[] buf1 = new byte[0x4D];
          byte[] buf2 = new byte[newLen];
          System.arraycopy (buffer, 0, buf1, 0, SKEW_OFFSET);
          System.arraycopy (buffer, SKEW_OFFSET + skew.length, buf2, 0, newLen);
          assembler1 = new AssemblerProgram (name + " (first)", buf1, 0x800, 1);
          assembler2 = new AssemblerProgram (name + " (second)", buf2,
              SKEW_OFFSET + skew.length, 0);
        }
        else
          assembler1 = new AssemblerProgram (name + " Boot Loader", buffer, 0x00, 1);
      }
      else                                          // apple III (SOS)
      {
        byte[] newBuffer = new byte[buffer.length * 2];
        System.arraycopy (buffer, 0, newBuffer, 0, buffer.length);

        //        byte[] buf = disk.readSector (1);
        //        System.arraycopy (buf, 0, newBuffer, buf.length, buf.length);

        // this was doubling the size of Pascal boot blocks - 06/08/2016
        //        buffer = newBuffer;
        assembler1 = new AssemblerProgram (name + " Boot Loader", buffer, 0x00, 0);
      }
    }

    text.append (assembler1.getText ());
    if (assembler2 != null)
    {
      text.append ("\n\n");
      text.append (HexFormatter.formatNoHeader (buffer, SKEW_OFFSET, skew.length,
          0x800 + SKEW_OFFSET));
      text.append ("\n\n");
      text.append (assembler2.getText ());
    }

    return text.toString ();
  }

  private boolean matches (byte[] buffer, int offset, byte[] test)
  {
    if (test.length == 0 || test.length > buffer.length - offset)
      return false;

    for (int i = 0; i < test.length; i++)
      if (test[i] != buffer[offset++])
        return false;

    return true;
  }
}