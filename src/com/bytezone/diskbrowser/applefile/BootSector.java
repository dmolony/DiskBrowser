package com.bytezone.diskbrowser.applefile;

import java.util.List;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;

public class BootSector extends AbstractSector
{
  AssemblerProgram assembler;
  String name;                                        // DOS or Prodos

  public BootSector (Disk disk, byte[] buffer, String name, DiskAddress diskAddress)
  {
    super (disk, buffer, diskAddress);
    this.name = name;
  }

  public BootSector (Disk disk, byte[] buffer, String name,
      List<DiskAddress> diskAddressList)
  {
    super (disk, buffer, diskAddressList);
    this.name = name;
  }

  @Override
  public String createText ()
  {
    StringBuilder text = new StringBuilder ();

    if (assembler == null)
    {
      int flag = buffer[0] & 0xFF;
      if (flag == 1)                                // apple II
        assembler = new AssemblerProgram (name + " Boot Loader", buffer, 0x00, 1);
      else                                          // apple III (SOS)
      {
        byte[] newBuffer = new byte[buffer.length * 2];
        System.arraycopy (buffer, 0, newBuffer, 0, buffer.length);

        byte[] buf = disk.readSector (1);
        System.arraycopy (buf, 0, newBuffer, buf.length, buf.length);

        buffer = newBuffer;
        assembler = new AssemblerProgram (name + " Boot Loader", buffer, 0x00, 0);
      }
    }

    text.append (assembler.getText ());

    return text.toString ();
  }
}