package com.bytezone.diskbrowser.nufx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
abstract class LZW
// -----------------------------------------------------------------------------------//
{
  static final String[] st = new String[0x1000];
  static final int TRACK_LENGTH = 0x1000;

  final List<byte[]> chunks = new ArrayList<> ();
  int volume;
  byte runLengthChar;

  int crc;
  int crcBase;
  int v3eof;                     // LZW/2 calculates the crc without padding

  private int byteBuffer;        // one character buffer
  private int bitsLeft;          // unused bits left in buffer

  private int ptr;
  private int startPtr;
  byte[] buffer;

  boolean unpacked;

  // ---------------------------------------------------------------------------------//
  static
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < 256; i++)
      st[i] = "" + (char) i;
  }

  // ---------------------------------------------------------------------------------//
  LZW (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = Objects.requireNonNull (buffer);
  }

  // ---------------------------------------------------------------------------------//
  abstract void unpack ();
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  void setBuffer (int ptr)
  // ---------------------------------------------------------------------------------//
  {
    startPtr = this.ptr = ptr;
    bitsLeft = 0;
  }

  // ---------------------------------------------------------------------------------//
  int bytesRead ()
  // ---------------------------------------------------------------------------------//
  {
    return ptr - startPtr;
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
  private boolean readBoolean ()
  // ---------------------------------------------------------------------------------//
  {
    if (bitsLeft == 0)
    {
      byteBuffer = buffer[ptr++] & 0xFF;
      bitsLeft = 8;
    }

    bitsLeft--;
    boolean bit = ((byteBuffer << bitsLeft) & 0x80) == 0x80;

    return bit;
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
  int getSize ()
  // ---------------------------------------------------------------------------------//
  {
    if (!unpacked)
    {
      unpack ();
      unpacked = true;
    }
    return chunks.size () * TRACK_LENGTH;
  }

  // ---------------------------------------------------------------------------------//
  byte[] getData ()
  // ---------------------------------------------------------------------------------//
  {
    if (!unpacked)
    {
      unpack ();
      unpacked = true;
    }

    byte[] buffer = new byte[getSize ()];
    int trackNumber = 0;

    for (byte[] track : chunks)
      System.arraycopy (track, 0, buffer, trackNumber++ * TRACK_LENGTH, TRACK_LENGTH);

    int length = v3eof != 0 ? v3eof : buffer.length;

    int calculatedCrc = Utility.getCRC (buffer, length, crcBase);
    if (crc != calculatedCrc)
    {
      System.out.printf ("%n*** Thread CRC failed ***  %04X  %04X%n", crc, calculatedCrc);
      //      throw new FileFormatException ("Thread CRC failed");
    }

    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  int width (int maximumValue)
  // ---------------------------------------------------------------------------------//
  {
    return 32 - Integer.numberOfLeadingZeros (maximumValue);
  }
}