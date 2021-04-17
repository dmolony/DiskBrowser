package com.bytezone.diskbrowser.utilities;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
class LZW
// -----------------------------------------------------------------------------------//
{
  static protected final String[] st = new String[0x1000];
  static protected final int TRACK_LENGTH = 0x1000;

  protected final List<byte[]> chunks = new ArrayList<> ();
  protected int volume;
  protected byte runLengthChar;
  protected int crc;
  protected int crcBase;
  int v3eof;

  private int buffer;            // one character buffer
  private int bitsLeft;          // unused bits left in buffer

  private int ptr;
  private int startPtr;
  protected byte[] bytes;

  // ---------------------------------------------------------------------------------//
  static
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < 256; i++)
      st[i] = "" + (char) i;
  }

  // ---------------------------------------------------------------------------------//
  public void setBuffer (byte[] buffer, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    bytes = buffer;
    startPtr = this.ptr = ptr;
    bitsLeft = 0;
  }

  // ---------------------------------------------------------------------------------//
  public int bytesRead ()
  // ---------------------------------------------------------------------------------//
  {
    return ptr - startPtr;
  }

  // ---------------------------------------------------------------------------------//
  private void fillBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    buffer = bytes[ptr++] & 0xFF;
    bitsLeft = 8;
  }

  // ---------------------------------------------------------------------------------//
  private boolean readBoolean ()
  // ---------------------------------------------------------------------------------//
  {
    if (bitsLeft == 0)
      fillBuffer ();

    bitsLeft--;
    boolean bit = ((buffer << bitsLeft) & 0x80) == 0x80;
    return bit;
  }

  // ---------------------------------------------------------------------------------//
  int readInt (int width)
  // ---------------------------------------------------------------------------------//
  {
    if (width < 8 || width > 12)
      throw new RuntimeException ("Illegal value of r = " + width);

    int x = 0;
    for (int i = 0, weight = 1; i < width; i++, weight <<= 1)
      if (readBoolean ())
        x |= weight;

    return x;
  }

  // ---------------------------------------------------------------------------------//
  byte[] undoRLE (byte[] inBuffer, int inPtr, int length)
  // ---------------------------------------------------------------------------------//
  {
    byte[] outBuffer = new byte[TRACK_LENGTH];
    int outPtr = 0;
    int max = inPtr + length;

    while (inPtr < max)
    {
      byte b = inBuffer[inPtr++];
      if (b == runLengthChar)
      {
        b = inBuffer[inPtr++];
        int rpt = inBuffer[inPtr++] & 0xFF;
        while (rpt-- >= 0)
          outBuffer[outPtr++] = b;
      }
      else
        outBuffer[outPtr++] = b;
    }

    assert outPtr == TRACK_LENGTH;
    return outBuffer;
  }

  // ---------------------------------------------------------------------------------//
  public int getSize ()
  // ---------------------------------------------------------------------------------//
  {
    return chunks.size () * TRACK_LENGTH;
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getData ()
  // ---------------------------------------------------------------------------------//
  {
    byte[] buffer = new byte[getSize ()];
    int trackNumber = 0;

    for (byte[] track : chunks)
      System.arraycopy (track, 0, buffer, trackNumber++ * TRACK_LENGTH, TRACK_LENGTH);

    int length = v3eof != 0 ? v3eof : buffer.length;

    int calculatedCrc = Utility.getCRC (buffer, length, crcBase);
    if (crc != calculatedCrc)
      System.out.printf ("%n*** LZW CRC mismatch ***  %04X  %04X%n", crc, calculatedCrc);

    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  int width (int maximumValue)
  // ---------------------------------------------------------------------------------//
  {
    return 32 - Integer.numberOfLeadingZeros (maximumValue);
  }
}