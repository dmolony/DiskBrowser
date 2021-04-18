package com.bytezone.diskbrowser.utilities;

// -----------------------------------------------------------------------------------//
class ThreadHeader
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

  final int threadClass;
  final int threadFormat;
  final int threadKind;

  final int threadCrc;
  final int uncompressedEOF;
  final int compressedEOF;

  // ---------------------------------------------------------------------------------//
  public ThreadHeader (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    threadClass = Utility.getWord (buffer, offset);
    threadFormat = Utility.getWord (buffer, offset + 2);
    threadKind = Utility.getWord (buffer, offset + 4);

    threadCrc = Utility.getWord (buffer, offset + 6);
    uncompressedEOF = Utility.getLong (buffer, offset + 8);
    compressedEOF = Utility.getLong (buffer, offset + 12);
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
    return text.toString ();
  }
}
