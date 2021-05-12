package com.bytezone.diskbrowser.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.prodos.write.DiskFullException;
import com.bytezone.diskbrowser.prodos.write.FileAlreadyExistsException;
import com.bytezone.diskbrowser.prodos.write.ProdosDisk;
import com.bytezone.diskbrowser.prodos.write.VolumeCatalogFullException;

// -----------------------------------------------------------------------------------//
public class Binary2
// -----------------------------------------------------------------------------------//
{
  private static final String UNDERLINE =
      "------------------------------------------------------"
          + "-----------------------";

  Binary2Header binary2Header;
  byte[] buffer;
  List<Binary2Header> headers = new ArrayList<> ();
  int totalBlocks;
  String fileName;

  // ---------------------------------------------------------------------------------//
  public Binary2 (Path path) throws IOException
  // ---------------------------------------------------------------------------------//
  {
    fileName = path.toFile ().getName ();
    buffer = Files.readAllBytes (path);
    int ptr = 0;

    do
    {
      binary2Header = new Binary2Header (buffer, ptr);
      System.out.println (binary2Header);
      headers.add (binary2Header);

      totalBlocks += binary2Header.totalBlocks;
      ptr += ((binary2Header.eof - 1) / 128 + 1) * 128 + 128;
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

    //    int totalUncompressedSize = 0;
    //    int totalCompressedSize = 0;

    for (Binary2Header header : headers)
    {
      text.append (String.format ("%s%n", header.getLine ()));
      //      totalUncompressedSize += record.getUncompressedSize ();
      //      totalCompressedSize += record.getCompressedSize ();
    }

    text.append (String.format ("%s%n", UNDERLINE));

    //    float pct = 0;
    //    if (totalUncompressedSize > 0)
    //      pct = totalCompressedSize * 100 / totalUncompressedSize;
    //    text.append (String.format (" Uncomp:%7d  Comp:%7d  %%of orig:%3.0f%%%n%n",
    //        totalUncompressedSize, totalCompressedSize, pct));

    return text.toString ();
  }
}
