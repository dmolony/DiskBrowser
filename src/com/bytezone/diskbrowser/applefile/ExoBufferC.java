package com.bytezone.diskbrowser.applefile;

// pack::: ~/exomizer-3.0.2/src/exomizer mem -q -P23 -lnone LODE148@0x4000 -o LODE148c
// unpack: ~/exomizer-3.0.2/src/exomizer raw -d -b -P23 LODE148c,0,-2 -o LODE148x

// -----------------------------------------------------------------------------------//
public class ExoBufferC
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

  byte[] outBuffer = new byte[50000];

  // ---------------------------------------------------------------------------------//
  public ExoBufferC (byte[] inBuffer)
  // ---------------------------------------------------------------------------------//
  {
    reverse (inBuffer);
    DecCtx decCtx = new DecCtx ();
    decCtxInit (decCtx, inBuffer, outBuffer, 23);
    //    tableDump (decCtx.table);
    decCtxDecrunch (decCtx);

    if (decCtx.outPos != outBuffer.length)
    {
      byte[] outBuffer2 = new byte[decCtx.outPos];
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
  private int bitBufRotate (DecCtx decCtx, int carry)
  // ---------------------------------------------------------------------------------//
  {
    int carryOut;

    if ((decCtx.flagsProto & PFLAG_BITS_ORDER_BE) != 0)
    {
      carryOut = (decCtx.bitBuffer & 0x80) == 0 ? 0 : 1;
      decCtx.bitBuffer = (decCtx.bitBuffer << 1) & 0xFF;

      if (carry != 0)
        decCtx.bitBuffer |= 0x01;
    }
    else
    {
      carryOut = decCtx.bitBuffer & 0x01;
      decCtx.bitBuffer = (decCtx.bitBuffer >>> 1) & 0xFF;

      if (carry != 0)
        decCtx.bitBuffer |= 0x80;
    }

    return carryOut;
  }

  // ---------------------------------------------------------------------------------//
  private int getByte (DecCtx decCtx)
  // ---------------------------------------------------------------------------------//
  {
    decCtx.bitsRead += 8;
    int c = decCtx.inBuffer[decCtx.inPos++] & 0xFF;
    return c;
  }

  // ---------------------------------------------------------------------------------//
  private int getBits (DecCtx decCtx, int count)
  // ---------------------------------------------------------------------------------//
  {
    int byteCopy = 0;
    int value = 0;

    if ((decCtx.flagsProto & PFLAG_BITS_COPY_GT_7) != 0)
    {
      while (count > 7)
      {
        byteCopy = count >>> 3;
        count &= 7;
      }
    }

    while (count-- > 0)
    {
      int carry = bitBufRotate (decCtx, 0);

      if (decCtx.bitBuffer == 0)
      {
        decCtx.bitBuffer = getByte (decCtx);
        decCtx.bitsRead -= 8;
        carry = bitBufRotate (decCtx, 1);
      }
      value <<= 1;
      value |= carry;
      decCtx.bitsRead++;
    }

    while (byteCopy-- > 0)
    {
      value <<= 8;
      value |= getByte (decCtx);
    }

    return value;
  }

  // ---------------------------------------------------------------------------------//
  private int getGammaCode (DecCtx decCtx)
  // ---------------------------------------------------------------------------------//
  {
    int gammaCode = 0;

    while (getBits (decCtx, 1) == 0)
      ++gammaCode;

    return gammaCode;
  }

  // ---------------------------------------------------------------------------------//
  private int getCooked (DecCtx decCtx, int index)
  // ---------------------------------------------------------------------------------//
  {
    int base = decCtx.table.tableLo[index] | (decCtx.table.tableHi[index] << 8);
    return base + getBits (decCtx, decCtx.table.tableBi[index]);
  }

  // ---------------------------------------------------------------------------------//
  private void tableInit (DecCtx decCtx, DecTable decTable)
  // ---------------------------------------------------------------------------------//
  {
    int end;
    int a = 0;
    int b = 0;

    decTable.tableBit[0] = 2;
    decTable.tableBit[1] = 4;
    decTable.tableBit[2] = 4;

    if ((decCtx.flagsProto & PFLAG_4_OFFSET_TABLES) != 0)
    {
      end = 68;

      decTable.tableBit[3] = 4;

      decTable.tableOff[0] = 64;
      decTable.tableOff[1] = 48;
      decTable.tableOff[2] = 32;
      decTable.tableOff[3] = 16;
    }
    else
    {
      end = 52;

      decTable.tableOff[0] = 48;
      decTable.tableOff[1] = 32;
      decTable.tableOff[2] = 16;
    }

    for (int i = 0; i < end; i++)
    {
      if ((i & 0x0F) != 0)
        a += (1 << b);
      else
        a = 1;

      decTable.tableLo[i] = a & 0xFF;
      decTable.tableHi[i] = a >>> 8;

      if ((decCtx.flagsProto & PFLAG_BITS_COPY_GT_7) != 0)
      {
        b = getBits (decCtx, 3);
        b |= getBits (decCtx, 1) << 3;
      }
      else
        b = getBits (decCtx, 4);

      decTable.tableBi[i] = b;
    }
  }

  // ---------------------------------------------------------------------------------//
  private void tableDump (DecTable table)
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0; i < 16; i++)
      System.out.printf ("%X", table.tableBi[i]);

    for (int j = 0; j < 3; j++)
    {
      System.out.printf (",");
      int start = table.tableOff[j];
      int end = start + (1 << table.tableBit[j]);
      for (int i = start; i < end; i++)
        System.out.printf ("%X", table.tableBi[i]);
    }
    System.out.println ();
  }

  // ---------------------------------------------------------------------------------//
  private void decCtxInit (DecCtx decCtx, byte[] inBuffer, byte[] outBuffer, int flags)
  // ---------------------------------------------------------------------------------//
  {
    decCtx.bitsRead = 0;

    decCtx.inBuffer = inBuffer;
    decCtx.inEnd = inBuffer.length;

    decCtx.inPos = 2;
    decCtx.flagsProto = flags;

    decCtx.outBuffer = outBuffer;
    decCtx.outPos = 0;

    if ((decCtx.flagsProto & PFLAG_BITS_ALIGN_START) != 0)
      decCtx.bitBuffer = 0;
    else
      decCtx.bitBuffer = getByte (decCtx);

    tableInit (decCtx, decCtx.table);
  }

  // ---------------------------------------------------------------------------------//
  private void decCtxDecrunch (DecCtx decCtx)
  // ---------------------------------------------------------------------------------//
  {
    int len;
    int offset;
    int i;
    int val;
    int src = 0;
    int literal;
    int threshold = (decCtx.flagsProto & PFLAG_4_OFFSET_TABLES) != 0 ? 4 : 3;

    if ((decCtx.flagsProto & PFLAG_IMPL_1LITERAL) != 0)
    {
      len = 1;
      literal = 1;
      src = literal (decCtx, len, literal, src);
    }

    while (true)
    {
      literal = 0;

      if (getBits (decCtx, 1) != 0)
      {
        len = 1;
        literal = 1;
        src = literal (decCtx, len, literal, src);
        continue;
      }

      val = getGammaCode (decCtx);

      if (val == 16)
        break;

      if (val == 17)
      {
        len = getBits (decCtx, 16);
        literal = 1;
        src = literal (decCtx, len, literal, src);
        continue;
      }

      len = getCooked (decCtx, val);
      i = (len > threshold ? threshold : len) - 1;
      val = decCtx.table.tableOff[i] + getBits (decCtx, decCtx.table.tableBit[i]);
      offset = getCooked (decCtx, val);

      src = decCtx.outPos - offset;

      src = literal (decCtx, len, literal, src);
    }
  }

  // ---------------------------------------------------------------------------------//
  private int literal (DecCtx decCtx, int len, int literal, int src)
  // ---------------------------------------------------------------------------------//
  {
    assert len > 0;
    do
    {
      int val = literal == 0 ? decCtx.outBuffer[src++] : getByte (decCtx);
      decCtx.outBuffer[decCtx.outPos++] = (byte) (val & 0xFF);

    } while (--len > 0);

    return src;
  }

  // ---------------------------------------------------------------------------------//
  class DecCtx
  // ---------------------------------------------------------------------------------//
  {
    int inPos;
    int inEnd;
    int outPos;

    byte[] inBuffer;
    byte[] outBuffer;

    int bitBuffer;

    DecTable table = new DecTable ();
    int bitsRead;
    int flagsProto;
  }

  // ---------------------------------------------------------------------------------//
  class DecTable
  // ---------------------------------------------------------------------------------//
  {
    int tableBit[] = new int[8];
    int tableOff[] = new int[8];
    int tableBi[] = new int[100];
    int tableLo[] = new int[100];
    int tableHi[] = new int[100];
  }
}
