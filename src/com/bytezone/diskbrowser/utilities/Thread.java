package com.bytezone.diskbrowser.utilities;

// -----------------------------------------------------------------------------------//
class Thread
// -----------------------------------------------------------------------------------//
{
  private final ThreadHeader header;
  private final byte[] data;
  private String fileName;
  private String message;
  private LZW lzw;
  private boolean hasDisk;
  private boolean hasFile;
  private boolean hasFileName;
  private int fileSize;

  // ---------------------------------------------------------------------------------//
  public Thread (byte[] buffer, int offset, int dataOffset)
  // ---------------------------------------------------------------------------------//
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
        lzw = switch (header.threadFormat)
        {
          case 2 -> new LZW1 (data);
          case 3 -> new LZW2 (data, header.threadCrc, header.uncompressedEOF);
          default -> null;                        // 1 = Huffman Squeeze
        };

        if (header.threadKind == 0)               // file
        {
          hasFile = true;
          if (lzw != null)
            fileSize = lzw.getSize ();
          else
            fileSize = header.uncompressedEOF;
        }
        else if (header.threadKind == 1)          // disk image
          hasDisk = true;

        break;

      case 3:
        if (header.threadKind == 0)
        {
          hasFileName = true;
          fileName = new String (data, 0, header.uncompressedEOF);
        }
        break;

      default:
        System.out.println ("Unknown threadClass: " + header.threadClass);
    }
  }

  // ---------------------------------------------------------------------------------//
  boolean hasFile (String fileName)
  // ---------------------------------------------------------------------------------//
  {
    return header.threadClass == 3 && this.fileName != null
        && this.fileName.equals (fileName);
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getData ()
  // ---------------------------------------------------------------------------------//
  {
    if (header.threadFormat == 0)       // uncompressed
      return data;

    return lzw.getData ();
  }

  // ---------------------------------------------------------------------------------//
  int getCompressedEOF ()
  // ---------------------------------------------------------------------------------//
  {
    return header.compressedEOF;
  }

  // ---------------------------------------------------------------------------------//
  int getUncompressedEOF ()
  // ---------------------------------------------------------------------------------//
  {
    return header.uncompressedEOF;
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

  // ---------------------------------------------------------------------------------//
  int getFileSize ()
  // ---------------------------------------------------------------------------------//
  {
    return fileSize;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (header.toString ());

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