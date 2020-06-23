package com.bytezone.diskbrowser.applefile;

// pack::: ~/exomizer-3.0.2/src/exomizer mem -q -P23 -lnone LODE148@0x4000 -o LODE148c
// unpack: ~/exomizer-3.0.2/src/exomizer raw -d -b -P23 LODE148c,0,-2 -o LODE148x

// -----------------------------------------------------------------------------------//
public class ExoBuffer
{
  private static int PBIT_BITS_ORDER_BE = 0;
  private static int PBIT_BITS_COPY_GT_7 = 1;
  private static int PBIT_IMPL_1LITERAL = 2;
  private static int PBIT_BITS_ALIGN_START = 3;
  private static int PBIT_4_OFFSET_TABLES = 4;

  private static int PFLAG_BITS_ORDER_BE = (1 << PBIT_BITS_ORDER_BE);
  private static int PFLAG_BITS_COPY_GT_7 = (1 << PBIT_BITS_COPY_GT_7);
  private static int PFLAG_IMPL_1LITERAL = (1 << PBIT_IMPL_1LITERAL);
  private static int PFLAG_BITS_ALIGN_START = (1 << PBIT_BITS_ALIGN_START);
  private static int PFLAG_4_OFFSET_TABLES = (1 << PBIT_4_OFFSET_TABLES);

  int inPos;
  int inEnd;
  int outPos;

  byte[] inBuffer;
  byte[] outBuffer = new byte[50000];

  int bitBuffer;

  int bitsRead;
  int flagsProto;

  int tableBit[] = new int[8];
  int tableOff[] = new int[8];
  int tableBi[] = new int[100];
  int tableLo[] = new int[100];
  int tableHi[] = new int[100];

  // ---------------------------------------------------------------------------------//
  public ExoBuffer (byte[] inBuffer)
  // ---------------------------------------------------------------------------------//
  {
    reverse (inBuffer);

    bitsRead = 0;

    this.inBuffer = inBuffer;
    inEnd = inBuffer.length;

    inPos = 2;
    flagsProto = 23;

    outPos = 0;

    if ((flagsProto & PFLAG_BITS_ALIGN_START) != 0)
      bitBuffer = 0;
    else
      bitBuffer = getByte ();

    tableInit ();

    //    tableDump (decCtx.table);
    decrunch ();

    if (outPos != outBuffer.length)
    {
      byte[] outBuffer2 = new byte[outPos];
      System.arraycopy (outBuffer, 0, outBuffer2, 0, outBuffer2.length);
      outBuffer = outBuffer2;
    }
    reverse (outBuffer);
  }

  // ---------------------------------------------------------------------------------//
  private void reverse (byte[] inBuffer)
  // ---------------------------------------------------------------------------------//
  {
    int lo = 0;
    int hi = inBuffer.length - 1;
    while (lo < hi)
    {
      byte temp = inBuffer[lo];
      inBuffer[lo++] = inBuffer[hi];
      inBuffer[hi--] = temp;
    }
  }

  // ---------------------------------------------------------------------------------//
  public byte[] getExpandedBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    return outBuffer;
  }

  // ---------------------------------------------------------------------------------//
  private int bitBufRotate (int carry)
  // ---------------------------------------------------------------------------------//
  {
    int carryOut;

    if ((flagsProto & PFLAG_BITS_ORDER_BE) != 0)
    {
      carryOut = (bitBuffer & 0x80) == 0 ? 0 : 1;
      bitBuffer = (bitBuffer << 1) & 0xFF;

      if (carry != 0)
        bitBuffer |= 0x01;
    }
    else
    {
      carryOut = bitBuffer & 0x01;
      bitBuffer = (bitBuffer >>> 1) & 0xFF;

      if (carry != 0)
        bitBuffer |= 0x80;
    }

    return carryOut;
  }

  // ---------------------------------------------------------------------------------//
  private int getByte ()
  // ---------------------------------------------------------------------------------//
  {
    bitsRead += 8;
    int c = inBuffer[inPos++] & 0xFF;
    return c;
  }

  // ---------------------------------------------------------------------------------//
  private int getBits (int count)
  // ---------------------------------------------------------------------------------//
  {
    int byteCopy = 0;
    int value = 0;

    if ((flagsProto & PFLAG_BITS_COPY_GT_7) != 0)
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
        bitsRead -= 8;
        carry = bitBufRotate (1);
      }
      value <<= 1;
      value |= carry;
      bitsRead++;
    }

    while (byteCopy-- > 0)
    {
      value <<= 8;
      value |= getByte ();
    }

    return value;
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
  private void tableInit ()
  // ---------------------------------------------------------------------------------//
  {
    int end;
    int a = 0;
    int b = 0;

    tableBit[0] = 2;
    tableBit[1] = 4;
    tableBit[2] = 4;

    if ((flagsProto & PFLAG_4_OFFSET_TABLES) != 0)
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

      if ((flagsProto & PFLAG_BITS_COPY_GT_7) != 0)
      {
        b = getBits (3);
        b |= getBits (1) << 3;
      }
      else
        b = getBits (4);

      tableBi[i] = b;
    }
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
    int len;
    int offset;
    int i;
    int val;
    int src = 0;
    int literal;
    int threshold = (flagsProto & PFLAG_4_OFFSET_TABLES) != 0 ? 4 : 3;

    if ((flagsProto & PFLAG_IMPL_1LITERAL) != 0)
    {
      len = 1;
      literal = 1;
      src = literal (len, literal, src);
    }

    while (true)
    {
      literal = 0;

      if (getBits (1) != 0)
      {
        len = 1;
        literal = 1;
        src = literal (len, literal, src);
        continue;
      }

      val = getGammaCode ();

      if (val == 16)
        break;

      if (val == 17)
      {
        len = getBits (16);
        literal = 1;
        src = literal (len, literal, src);
        continue;
      }

      len = getCooked (val);
      i = (len > threshold ? threshold : len) - 1;
      val = tableOff[i] + getBits (tableBit[i]);
      offset = getCooked (val);

      src = outPos - offset;

      src = literal (len, literal, src);
    }
  }

  // ---------------------------------------------------------------------------------//
  private int literal (int len, int literal, int src)
  // ---------------------------------------------------------------------------------//
  {
    assert len > 0;
    do
    {
      int val = literal == 0 ? outBuffer[src++] : getByte ();
      outBuffer[outPos++] = (byte) (val & 0xFF);

    } while (--len > 0);

    return src;
  }
}
