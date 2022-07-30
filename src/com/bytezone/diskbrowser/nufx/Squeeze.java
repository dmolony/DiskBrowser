package com.bytezone.diskbrowser.nufx;

import com.bytezone.diskbrowser.utilities.FileFormatException;
import com.bytezone.diskbrowser.utilities.Utility;

// see http://fileformats.archiveteam.org/wiki/Squeeze
// see http://fileformats.archiveteam.org/wiki/RLE90
// -----------------------------------------------------------------------------------//
public class Squeeze
// -----------------------------------------------------------------------------------//
{
  private static final byte[] Squeeze = { 0x76, (byte) 0xFF };
  private static int RLE_DELIMITER = 0x90;
  private static int EOF_TOKEN = 0x100;

  private int bits;
  private int bitPos = 7;         // trigger the first read
  private int ptr;
  private byte[] buffer;
  private Node[] nodes;

  // ---------------------------------------------------------------------------------//
  public byte[] unSqueeze (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    if (!Utility.isMagic (buffer, 0, Squeeze))
      throw new FileFormatException ("Not Squeeze format");

    byte[] uncompressed = new byte[buffer.length * 3];
    int uncPtr = 0;

    int fileChecksum = Utility.getShort (buffer, 2);
    String fileName = Utility.getCString (buffer, 4);

    ptr = fileName.length () + 5;
    int nodeCount = Utility.getShort (buffer, ptr);
    ptr += 2;

    nodes = new Node[nodeCount];
    this.buffer = buffer;

    for (int i = 0; i < nodes.length; i++)
    {
      int left = Utility.getSignedShort (buffer, ptr);
      int right = Utility.getSignedShort (buffer, ptr + 2);
      nodes[i] = new Node (left, right);
      ptr += 4;
    }

    boolean repeating = false;
    int lastVal = 0;
    int sum = 0;

    while (true)
    {
      int val = decodeSymbol ();
      if (val == EOF_TOKEN)
        break;

      if (repeating)
      {
        repeating = false;

        if (val == 0)         // flag indicating a single RLE_DELIMITER
        {
          lastVal = RLE_DELIMITER;
          val = 2;
        }

        while (--val != 0)
        {
          sum += lastVal;
          uncompressed[uncPtr++] = (byte) lastVal;
        }
      }
      else
      {
        if (val == RLE_DELIMITER)
          repeating = true;
        else
        {
          lastVal = val;
          sum += lastVal;
          uncompressed[uncPtr++] = (byte) lastVal;
        }
      }
    }

    if ((sum & 0xFFFF) != fileChecksum)
      System.out.printf ("Checksum mismatch : %04X  %04X%n", fileChecksum, sum & 0xFFFF);

    byte[] uncompressedBuffer = new byte[uncPtr];
    System.arraycopy (uncompressed, 0, uncompressedBuffer, 0, uncompressedBuffer.length);

    return uncompressedBuffer;
  }

  // ---------------------------------------------------------------------------------//
  private int decodeSymbol ()
  // ---------------------------------------------------------------------------------//
  {
    int val = 0;

    while (true)
    {
      if (++bitPos > 7)
      {
        bits = buffer[ptr++];
        bitPos = 0;
      }

      val = (bits & 1) == 0 ? nodes[val].left : nodes[val].right;
      bits >>>= 1;

      if (val < 0)
        return -++val;          // increment and make positive
    }
  }

  // ---------------------------------------------------------------------------------//
  record Node (int left, int right)
  // ---------------------------------------------------------------------------------//
  {
  };
}
