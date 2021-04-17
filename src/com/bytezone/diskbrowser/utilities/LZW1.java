package com.bytezone.diskbrowser.utilities;

import java.util.Objects;

// -----------------------------------------------------------------------------------//
class LZW1 extends LZW
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public LZW1 (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    bytes = Objects.requireNonNull (buffer);

    crc = Utility.getWord (buffer, 0);
    crcBase = 0;

    volume = buffer[2] & 0xFF;
    runLengthChar = (byte) (buffer[3] & 0xFF);
    int ptr = 4;

    while (ptr < buffer.length - 1)          // what is in the last byte?
    {
      int rleLength = Utility.getWord (buffer, ptr);
      int lzwPerformed = buffer[ptr + 2] & 0xFF;
      ptr += 3;

      if (lzwPerformed != 0)
      {
        setBuffer (buffer, ptr);            // prepare to read n-bit integers
        byte[] lzwBuffer = undoLZW (rleLength);

        if (rleLength == TRACK_LENGTH)      // no run length encoding
          chunks.add (lzwBuffer);
        else
          chunks.add (undoRLE (lzwBuffer, 0, lzwBuffer.length));

        ptr += bytesRead ();                // since the setBuffer()
      }
      else
      {
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
  protected byte[] undoLZW (int rleLength)
  // ---------------------------------------------------------------------------------//
  {
    byte[] lzwBuffer = new byte[rleLength];       // must fill this array from input
    int ptr = 0;
    int nextEntry = 0x100;                        // always start with a fresh table
    String prev = "";

    while (ptr < rleLength)
    {
      int codeWord = readInt (width (nextEntry + 1));
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

    text.append (String.format ("  crc ............... %,d  (%04X)%n", crc, crc));
    text.append (String.format ("  volume ............ %,d%n", volume));
    text.append (String.format ("  RLE char .......... $%02X", runLengthChar));

    return text.toString ();
  }
}