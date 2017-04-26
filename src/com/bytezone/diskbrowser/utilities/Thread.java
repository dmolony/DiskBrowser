package com.bytezone.diskbrowser.utilities;

class Thread
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

  private final ThreadHeader header;
  private final byte[] data;
  private String filename;
  private String message;
  private LZW lzw;

  public Thread (byte[] buffer, int offset, int dataOffset)
  {
    header = new ThreadHeader (buffer, offset);

    data = new byte[header.compressedEOF];
    System.arraycopy (buffer, dataOffset, data, 0, data.length);

    switch (header.threadClass)
    {
      case 0:
        if (header.threadKind == 1)
          message = new String (data, 0, header.uncompressedEOF);
        break;

      case 1:
        break;

      case 2:
        if (header.threadKind == 1)
        {
          if (header.format == 2)
            lzw = new LZW1 (data);
          else if (header.format == 3)
            lzw = new LZW2 (data, header.crc);
          else if (header.format == 1)
          {
            // Huffman Squeeze
            System.out.println ("Huffman Squeeze format - not written yet");
          }
        }
        break;

      case 3:
        if (header.threadKind == 0)
          filename = new String (data, 0, header.uncompressedEOF);
        break;

      default:
        System.out.println ("Unknown threadClass: " + header.threadClass);
    }
  }

  public byte[] getData ()
  {
    return hasDisk () ? lzw.getData () : null;
  }

  int getCompressedEOF ()
  {
    return header.compressedEOF;
  }

  public boolean hasDisk ()
  {
    return lzw != null;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (header.toString ());

    if (filename != null)
      text.append ("\n  filename .......... " + filename);
    else if (message != null)
      text.append ("\n  message ........... " + message);
    else if (lzw != null)
    {
      text.append ("\n");
      text.append (lzw);
    }

    return text.toString ();
  }

  class ThreadHeader
  {
    private final int threadClass;
    private final int format;
    private final int threadKind;
    private final int crc;
    private final int uncompressedEOF;
    private final int compressedEOF;

    public ThreadHeader (byte[] buffer, int offset)
    {
      threadClass = Utility.getWord (buffer, offset);
      format = Utility.getWord (buffer, offset + 2);
      threadKind = Utility.getWord (buffer, offset + 4);
      crc = Utility.getWord (buffer, offset + 6);
      uncompressedEOF = Utility.getLong (buffer, offset + 8);
      compressedEOF = Utility.getLong (buffer, offset + 12);
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("  threadClass ....... %d  %s%n", threadClass,
          threadClassText[threadClass]));
      text.append (
          String.format ("  format ............ %d  %s%n", format, formatText[format]));
      text.append (String.format ("  kind .............. %d  %s%n", threadKind,
          threadKindText[threadClass][threadKind]));
      text.append (String.format ("  crc ............... %,d%n", crc));
      text.append (String.format ("  uncompressedEOF ... %,d%n", uncompressedEOF));
      text.append (String.format ("  compressedEOF ..... %,d  (%08X)", compressedEOF,
          compressedEOF));
      return text.toString ();
    }
  }
}