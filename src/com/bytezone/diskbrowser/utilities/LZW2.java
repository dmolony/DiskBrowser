package com.bytezone.diskbrowser.utilities;

// -----------------------------------------------------------------------------------//
class LZW2 extends LZW
// -----------------------------------------------------------------------------------//
{
  private int nextEntry = 0x100;
  private String prev = "";
  private int codeWord;

  // ---------------------------------------------------------------------------------//
  public LZW2 (byte[] buffer, int crc, int eof)
  // ---------------------------------------------------------------------------------//
  {
    super (buffer);

    this.crc = crc;
    this.v3eof = eof;

    //    unpack ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void unpack ()
  // ---------------------------------------------------------------------------------//
  {
    crcBase = 0xFFFF;
    codeWord = 0;

    volume = buffer[0] & 0xFF;
    runLengthChar = (byte) (buffer[1] & 0xFF);
    int ptr = 2;

    while (ptr < buffer.length - 1)
    {
      int rleLength = Utility.getWord (buffer, ptr);
      boolean lzwPerformed = (rleLength & 0x8000) != 0;
      ptr += 2;

      if (lzwPerformed)
      {
        rleLength &= 0x0FFF;                // remove the LZW flag
        if (rleLength == 0)
          rleLength = TRACK_LENGTH;

        int chunkLength = Utility.getWord (buffer, ptr);
        ptr += 2;

        setBuffer (ptr);                    // prepare to read n-bit integers
        byte[] lzwBuffer = undoLZW (rleLength);

        if ((chunkLength - 4) != bytesRead ())
          System.out.printf ("Invalid chunk length%n");

        if (rleLength == TRACK_LENGTH)      // no run length encoding
          chunks.add (lzwBuffer);
        else
          chunks.add (undoRLE (lzwBuffer, 0, lzwBuffer.length));

        ptr += bytesRead ();                // since the setBuffer()
      }
      else
      {
        nextEntry = 0x100;
        if (rleLength == 0)
          rleLength = TRACK_LENGTH;

        if (rleLength == TRACK_LENGTH)      // no run length encoding
        {
          byte[] originalBuffer = new byte[TRACK_LENGTH];
          System.arraycopy (buffer, ptr, originalBuffer, 0, originalBuffer.length);
          chunks.add (originalBuffer);
        }
        else
          chunks.add (undoRLE (buffer, ptr, rleLength));

        ptr += rleLength;
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  byte[] undoLZW (int rleLength)
  // ---------------------------------------------------------------------------------//
  {
    byte[] lzwBuffer = new byte[rleLength];      // must fill this array from buffer
    int ptr = 0;

    while (ptr < rleLength)
    {
      codeWord = readInt (width (nextEntry + 1));

      if (codeWord == 0x100)      // clear the table
      {
        nextEntry = 0x100;
        codeWord = readInt (9);
        prev = "";
      }

      String s = (nextEntry == codeWord) ? prev + prev.charAt (0) : st[codeWord];

      if (nextEntry < st.length)
        st[nextEntry++] = prev + s.charAt (0);

      for (int i = 0; i < s.length (); i++)
        lzwBuffer[ptr++] = (byte) s.charAt (i);

      prev = s;
    }

    return lzwBuffer;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("  volume ............ %,d%n", volume));
    text.append (String.format ("  RLE char .......... $%02X", runLengthChar));

    return text.toString ();
  }
}