package com.bytezone.diskbrowser.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
public class NuFX
// -----------------------------------------------------------------------------------//
{
  private Header header;
  private final byte[] buffer;
  private final boolean debug = false;

  private final List<Record> records = new ArrayList<> ();
  private final List<Thread> threads = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public NuFX (Path path) throws FileFormatException, IOException
  // ---------------------------------------------------------------------------------//
  {
    buffer = Files.readAllBytes (path);
    readBuffer ();
  }

  // ---------------------------------------------------------------------------------//
  private void readBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    header = new Header (buffer);

    int dataPtr = 48;
    if (header.bin2)
      dataPtr += 128;

    if (debug)
      System.out.printf ("%s%n%n", header);

    for (int rec = 0; rec < header.getTotalRecords (); rec++)
    {
      Record record = new Record (buffer, dataPtr);
      records.add (record);

      if (debug)
        System.out.printf ("Record: %d%n%n%s%n%n", rec, record);

      dataPtr += record.getAttributes () + record.getFileNameLength ();
      int threadsPtr = dataPtr;
      dataPtr += record.getTotalThreads () * 16;

      for (int i = 0; i < record.getTotalThreads (); i++)
      {
        Thread thread = new Thread (buffer, threadsPtr + i * 16, dataPtr);
        threads.add (thread);
        dataPtr += thread.getCompressedEOF ();

        if (debug)
          System.out.printf ("Thread: %d%n%n%s%n%n", i, thread);
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasDisk ())
        return thread.getData ();
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    for (Thread thread : threads)
      if (thread.hasDisk ())
        return thread.toString ();
    return "no disk";
  }
}