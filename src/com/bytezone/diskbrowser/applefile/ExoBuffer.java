package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.Utility;

// pack::: ~/exomizer-3.0.2/src/exomizer mem -q -P23 -lnone LODE148@0x4000 -o LODE148c
// unpack: ~/exomizer-3.0.2/src/exomizer raw -d -b -P23 LODE148c,0,-2 -o LODE148x

// -----------------------------------------------------------------------------------//
public class ExoBuffer
// -----------------------------------------------------------------------------------//
{
  private static final int PFLAG_BITS_ORDER_BE = 1;
  private static final int PFLAG_BITS_COPY_GT_7 = 2;
  private static final int PFLAG_IMPL_1LITERAL = 4;
  private static final int PFLAG_BITS_ALIGN_START = 8;
  private static final int PFLAG_4_OFFSET_TABLES = 16;

  private byte[] inBuffer;
  private byte[] outBuffer;

  private int inPos;
  private int outPos;

  private int bitBuffer;
  private int flags;

  private int tableBit[] = new int[8];
  private int tableOff[] = new int[8];
  private int tableBi[] = new int[100];
  private int tableLo[] = new int[100];
  private int tableHi[] = new int[100];

  // ---------------------------------------------------------------------------------//
  public ExoBuffer (byte[] inBuffer)
  // ---------------------------------------------------------------------------------//
  {
    this.inBuffer = inBuffer;
    Utility.reverse (inBuffer);

    switch (Utility.getShortBigEndian (inBuffer, 0))
    {
      case 0x6000:
        outBuffer = new byte[0x2000];     // HGR
        break;
      case 0x8000:
        outBuffer = new byte[0x4000];     // DHGR
        break;
      case 0xA000:
        outBuffer = new byte[0x8000];     // SHR
        break;
    }

    decrunch ();
    Utility.reverse (outBuffer);
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getExpandedBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    return outBuffer;
  }

  // ---------------------------------------------------------------------------------//
  public static boolean isExomizer (byte[] buffer, int auxType)
  // ---------------------------------------------------------------------------------//
  {
    if (auxType != 0x1FF8 && auxType != 0x3FF8)
      return false;

    int address = Utility.unsignedShort (buffer, buffer.length - 2);

    if (address != 0x6000 && address != 0x8000 && address != 0xA000)
      return false;

    return true;        // maybe
  }

  // ---------------------------------------------------------------------------------//
  private int bitBufRotate (int carryIn)
  // ---------------------------------------------------------------------------------//
  {
    int carryOut;

    if ((flags & PFLAG_BITS_ORDER_BE) != 0)
    {
      carryOut = (bitBuffer & 0x80) >>> 7;
      bitBuffer = (bitBuffer << 1) & 0xFF;

      if (carryIn != 0)
        bitBuffer |= 0x01;
    }
    else
    {
      carryOut = bitBuffer & 0x01;
      bitBuffer = (bitBuffer >>> 1) & 0xFF;

      if (carryIn != 0)
        bitBuffer |= 0x80;
    }

    return carryOut;
  }

  // ---------------------------------------------------------------------------------//
  private int getByte ()
  // ---------------------------------------------------------------------------------//
  {
    return inBuffer[inPos++] & 0xFF;
  }

  // ---------------------------------------------------------------------------------//
  private int getBits (int count)
  // ---------------------------------------------------------------------------------//
  {
    int byteCopy = 0;
    int value = 0;

    if ((flags & PFLAG_BITS_COPY_GT_7) != 0)
    {
      while (count > 7)
      {
        byteCopy = count >>> 3;
        count &= 7;
      }
    }

    while (count-- > 0)
    {
      int carry = bitBufRotate (0);

      if (bitBuffer == 0)
      {
        bitBuffer = getByte ();
        carry = bitBufRotate (1);
      }
      value = (value << 1) | carry;
    }

    while (byteCopy-- > 0)
    {
      value <<= 8;
      value |= getByte ();
    }

    return value;
  }

  // ---------------------------------------------------------------------------------//
  private void tableInit ()
  // ---------------------------------------------------------------------------------//
  {
    int end;
    int a = 0;
    int b = 0;

    tableBit[0] = 2;
    tableBit[1] = 4;
    tableBit[2] = 4;

    if ((flags & PFLAG_4_OFFSET_TABLES) != 0)
    {
      end = 68;

      tableBit[3] = 4;

      tableOff[0] = 64;
      tableOff[1] = 48;
      tableOff[2] = 32;
      tableOff[3] = 16;
    }
    else
    {
      end = 52;

      tableOff[0] = 48;
      tableOff[1] = 32;
      tableOff[2] = 16;
    }

    for (int i = 0; i < end; i++)
    {
      if ((i & 0x0F) != 0)
        a += (1 << b);
      else
        a = 1;

      tableLo[i] = a & 0xFF;
      tableHi[i] = a >>> 8;

      if ((flags & PFLAG_BITS_COPY_GT_7) != 0)
      {
        b = getBits (3);
        b |= getBits (1) << 3;
      }
      else
        b = getBits (4);

      tableBi[i] = b;
    }
    //    tableDump ();
  }

  // ---------------------------------------------------------------------------------//
  private void tableDump ()
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < 16; i++)
      System.out.printf ("%X", tableBi[i]);

    for (int j = 0; j < 3; j++)
    {
      System.out.printf (",");
      int start = tableOff[j];
      int end = start + (1 << tableBit[j]);
      for (int i = start; i < end; i++)
        System.out.printf ("%X", tableBi[i]);
    }
    System.out.println ();
  }

  // ---------------------------------------------------------------------------------//
  private void decrunch ()
  // ---------------------------------------------------------------------------------//
  {
    inPos = 2;
    outPos = 0;
    flags = 23;

    if ((flags & PFLAG_BITS_ALIGN_START) != 0)
      bitBuffer = 0;
    else
      bitBuffer = getByte ();

    tableInit ();

    int len;
    int srcPtr = 0;
    boolean literal;
    int threshold = (flags & PFLAG_4_OFFSET_TABLES) != 0 ? 4 : 3;

    if ((flags & PFLAG_IMPL_1LITERAL) != 0)
    {
      len = 1;
      literal = true;
      srcPtr = copy (len, literal, srcPtr);
    }

    while (true)
    {
      if (getBits (1) != 0)
      {
        len = 1;
        literal = true;
      }
      else
      {
        int val = getGammaCode ();

        if (val == 16)
          break;

        if (val == 17)
        {
          len = getBits (16);
          literal = true;
        }
        else
        {
          len = getCooked (val);
          literal = false;

          int i = (len > threshold ? threshold : len) - 1;
          srcPtr = outPos - getCooked (tableOff[i] + getBits (tableBit[i]));
        }
      }
      srcPtr = copy (len, literal, srcPtr);
    }
    assert outPos == outBuffer.length;
  }

  // ---------------------------------------------------------------------------------//
  private int getGammaCode ()
  // ---------------------------------------------------------------------------------//
  {
    int gammaCode = 0;

    while (getBits (1) == 0)
      ++gammaCode;

    return gammaCode;
  }

  // ---------------------------------------------------------------------------------//
  private int getCooked (int index)
  // ---------------------------------------------------------------------------------//
  {
    int base = tableLo[index] | (tableHi[index] << 8);
    return base + getBits (tableBi[index]);
  }

  // ---------------------------------------------------------------------------------//
  private int copy (int len, boolean literal, int src)
  // ---------------------------------------------------------------------------------//
  {
    do
    {
      int val = literal ? getByte () : outBuffer[src++];
      outBuffer[outPos++] = (byte) (val & 0xFF);

    } while (--len > 0);

    return src;
  }
}
