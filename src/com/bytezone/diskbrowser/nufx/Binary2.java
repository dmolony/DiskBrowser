package com.bytezone.diskbrowser.nufx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.prodos.write.DiskFullException;
import com.bytezone.diskbrowser.prodos.write.FileAlreadyExistsException;
import com.bytezone.diskbrowser.prodos.write.ProdosDisk;
import com.bytezone.diskbrowser.prodos.write.VolumeCatalogFullException;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class Binary2
// -----------------------------------------------------------------------------------//
{
  private static final String UNDERLINE =
      "------------------------------------------------------"
          + "-----------------------";

  private Binary2Header binary2Header;
  private byte[] buffer;
  private List<Binary2Header> headers = new ArrayList<> ();
  private int totalBlocks;
  private String fileName;

  // ---------------------------------------------------------------------------------//
  public Binary2 (Path path) throws IOException
  // ---------------------------------------------------------------------------------//
  {
    fileName = path.toFile ().getName ();
    buffer = Files.readAllBytes (path);
    read (buffer);
  }

  // ---------------------------------------------------------------------------------//
  private void read (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    int ptr = 0;

    do
    {
      binary2Header = new Binary2Header (buffer, ptr);

      ptr += ((binary2Header.eof - 1) / 128 + 2) * 128;
      if (ptr > buffer.length)      // not enough blocks for this file
        break;

      totalBlocks += binary2Header.totalBlocks;
      headers.add (binary2Header);
    } while (binary2Header.filesToFollow > 0);
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getDiskBuffer () throws DiskFullException, VolumeCatalogFullException,
      FileAlreadyExistsException, IOException
  // ---------------------------------------------------------------------------------//
  {
    ProdosDisk disk = new ProdosDisk (800, "DiskBrowser");

    for (Binary2Header header : headers)
    {
      byte[] dataBuffer = new byte[header.eof];       // this sux
      System.arraycopy (buffer, header.ptr + 128, dataBuffer, 0, dataBuffer.length);

      if (header.compressed && dataBuffer[0] == 0x76 && dataBuffer[1] == (byte) 0xFF)
      {
        String name = Utility.getCString (dataBuffer, 4);

        Squeeze squeeze = new Squeeze ();
        byte[] tmp = squeeze.unSqueeze (dataBuffer);

        disk.addFile (name, header.fileType, header.auxType, header.created,
            header.modified, tmp, tmp.length);
      }
      else
        disk.addFile (header.fileName, header.fileType, header.auxType, header.created,
            header.modified, dataBuffer, header.eof);
    }
    disk.close ();

    return disk.getBuffer ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format (
        " %-15.15s                                                  Files:%5d%n%n",
        fileName, headers.size ()));

    text.append (" Name                              Type Auxtyp Modified"
        + "         Fmat   Length\n");

    text.append (String.format ("%s%n", UNDERLINE));

    for (Binary2Header header : headers)
      text.append (String.format ("%s%n", header.getLine ()));

    text.append (String.format ("%s%n", UNDERLINE));

    return text.toString ();
  }
}
