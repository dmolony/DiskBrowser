package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;

public class BootSector extends AbstractSector
{
  AssemblerProgram assembler;
  String name;      // DOS or Prodos

  public BootSector (Disk disk, byte[] buffer, String name)
  {
    super (disk, buffer);
    this.name = name;
  }

  @Override
  public String createText ()
  {
    StringBuilder text = new StringBuilder ();

    if (assembler == null)
    {
      // The first byte in the buffer is the number of sectors to read in (minus 1)
      int sectors = buffer[0] & 0xFF;
      //      System.out.printf ("Sectors to read : %d%n", (sectors + 1));
      if (sectors > 0)
      {
        int bufferSize = buffer.length * (sectors + 1);
        byte[] newBuffer = new byte[bufferSize];
        System.arraycopy (buffer, 0, newBuffer, 0, buffer.length);

        for (int i = 0; i < sectors; i++)
        {
          byte[] buf = disk.readSector (i + 1);
          //          System.out.printf ("%d %d %d%n", buf.length, buffer.length, newBuffer.length);
          System.arraycopy (buf, 0, newBuffer, i * buf.length, buf.length);
        }
        buffer = newBuffer;
      }
      assembler = new AssemblerProgram (name + " Boot Loader", buffer, 0x800, 1);
    }

    text.append (assembler.getText ());

    return text.toString ();
  }
}