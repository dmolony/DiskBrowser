package com.bytezone.diskbrowser.utilities;

// -----------------------------------------------------------------------------------//
class Thread
// -----------------------------------------------------------------------------------//
{
  private static String[] threadClassText = { "Message", "Control", "Data", "Filename" };
  private static String[] formatText =
      { "Uncompressed", "Huffman squeeze", "LZW/1", "LZW/2", "Unix 12-bit Compress",
        "Unix 16-bit Compress" };
  private static String[][] threadKindText =
      { { "ASCII text", "predefined EOF", "IIgs icon" },
        { "create directory", "undefined", "undefined" },
        { "data fork", "disk image", "resource fork" },
        { "filename", "undefined", "undefined" } };

  private static final int DATA_FORK = 0;
  private static final int DISK_IMAGE = 1;
  private static final int RESOURCEFORK = 2;

  final int threadClass;
  final int threadFormat;
  final int threadKind;

  final int threadCrc;
  final int uncompressedEOF;
  final int compressedEOF;

  private final byte[] data;
  private String fileName;
  private String message;
  private LZW lzw;

  private boolean hasDisk;
  private boolean hasFile;
  private boolean hasResource;
  private boolean hasFileName;

  // ---------------------------------------------------------------------------------//
  public Thread (byte[] buffer, int offset, int dataOffset)
  // ---------------------------------------------------------------------------------//
  {
    threadClass = Utility.getShort (buffer, offset);
    threadFormat = Utility.getShort (buffer, offset + 2);
    threadKind = Utility.getShort (buffer, offset + 4);

    threadCrc = Utility.getShort (buffer, offset + 6);
    uncompressedEOF = Utility.getLong (buffer, offset + 8);
    compressedEOF = Utility.getLong (buffer, offset + 12);

    data = new byte[compressedEOF];
    System.arraycopy (buffer, dataOffset, data, 0, data.length);

    switch (threadFormat)
    {
      case 0:                         // uncompressed
        break;
      case 1:                         // Huffman Squeeze - see archivers folder
        System.out.printf ("Huffman squeeze");
        break;
      case 2:                         // Dynamic LZW/1
        lzw = new LZW1 (data);
        break;
      case 3:                         // Dynamic LZW/2
        int crcLength = threadKind == 1 ? 0 : uncompressedEOF;
        lzw = new LZW2 (data, threadCrc, crcLength);
        break;
      case 4:                         // Unix 12-bit compress
        break;
      case 5:                         // Unix 16-bit compress
        break;
    }

    switch (threadClass)
    {
      case 0:
        switch (threadKind)
        {
          case 0:                     // ASCII text (obsolete)
            break;
          case 1:                     // comp/uncomp eof may change
            message = new String (data, 0, uncompressedEOF);
            break;
          case 2:                     // Apple IIgs icon
            break;
        }
        break;

      case 1:
        switch (threadKind)
        {
          case 0:                     // create directory
            break;
          case 1:                     // undefined
          case 2:                     // undefined
            break;
        }
        break;

      case 2:
        switch (threadKind)
        {
          case DATA_FORK:
            hasFile = true;
            break;
          case DISK_IMAGE:
            hasDisk = true;
            break;
          case RESOURCEFORK:
            hasResource = true;
            break;
        }

        break;

      case 3:
        switch (threadKind)
        {
          case 0:                     // filename
            hasFileName = true;
            fileName = new String (data, 0, uncompressedEOF);
            break;

          case 1:                     // undefined
            break;
          case 2:                     // undefined
            break;
        }
        break;

      default:
        System.out.println ("Unknown threadClass: " + threadClass);
    }
  }

  // ---------------------------------------------------------------------------------//
  boolean hasFile (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    return threadClass == 3 && this.fileName != null && this.fileName.equals (fileName);
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getData ()
  // ---------------------------------------------------------------------------------//
  {
    if (threadFormat == 0)       // uncompressed
      return data;

    return lzw.getData ();
  }

  // ---------------------------------------------------------------------------------//
  int getCompressedEOF ()
  // ---------------------------------------------------------------------------------//
  {
    return compressedEOF;
  }

  // ---------------------------------------------------------------------------------//
  int getUncompressedEOF ()
  // ---------------------------------------------------------------------------------//
  {
    return uncompressedEOF;
  }

  // ---------------------------------------------------------------------------------//
  public boolean hasDisk ()
  // ---------------------------------------------------------------------------------//
  {
    return hasDisk;
  }

  // ---------------------------------------------------------------------------------//
  public boolean hasFile ()
  // ---------------------------------------------------------------------------------//
  {
    return hasFile;
  }

  // ---------------------------------------------------------------------------------//
  public boolean hasResource ()
  // ---------------------------------------------------------------------------------//
  {
    return hasResource;
  }

  // ---------------------------------------------------------------------------------//
  public boolean hasFileName ()
  // ---------------------------------------------------------------------------------//
  {
    return hasFileName;
  }

  // ---------------------------------------------------------------------------------//
  String getFileName ()
  // ---------------------------------------------------------------------------------//
  {
    return fileName;
  }

  // Called by Record.getFileSize()
  // ---------------------------------------------------------------------------------//
  int getFileSize ()
  // ---------------------------------------------------------------------------------//
  {
    //    if (lzw != null)
    //      System.out.printf ("%04X v %04X v %04X%n", compressedEOF, uncompressedEOF,
    //          lzw.getSize ());
    return lzw != null ? lzw.getSize () : uncompressedEOF;
    //    return uncompressedEOF;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("  threadClass ....... %d  %s%n", threadClass,
        threadClassText[threadClass]));
    text.append (String.format ("  format ............ %d  %s%n", threadFormat,
        formatText[threadFormat]));
    text.append (String.format ("  kind .............. %d  %s%n", threadKind,
        threadKindText[threadClass][threadKind]));
    text.append (String.format ("  crc ............... %,d  (%<04X)%n", threadCrc));
    text.append (String.format ("  uncompressedEOF ... %,d  (%<08X)%n", uncompressedEOF));
    text.append (String.format ("  compressedEOF ..... %,d  (%<08X)", compressedEOF));

    if (fileName != null)
      text.append ("\n  filename .......... " + fileName);
    else if (message != null)
      text.append ("\n  message ........... " + message);
    else if (lzw != null)
    {
      text.append ("\n");
      text.append (lzw);
    }

    return text.toString ();
  }
}