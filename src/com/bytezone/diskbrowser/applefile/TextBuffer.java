package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;

// only used by Prodos text files - note the fixed block size of 512 - bad!
// -----------------------------------------------------------------------------------//
public class TextBuffer
// -----------------------------------------------------------------------------------//
{
  public final byte[] buffer;
  public final int reclen;
  public final int firstRecNo;

  // ---------------------------------------------------------------------------------//
  public TextBuffer (byte[] tempBuffer, int reclen, int firstBlock)
  // ---------------------------------------------------------------------------------//
  {
    this.reclen = reclen;

    // calculate recNo of first full record
    int firstByte = firstBlock * 512;                     // logical byte #
    int rem = firstByte % reclen;
    firstRecNo = firstByte / reclen + (rem > 0 ? 1 : 0);
    int offset = (rem > 0) ? reclen - rem : 0;

    int availableBytes = tempBuffer.length - offset;
    int totalRecords = (availableBytes - 1) / reclen + 1;

    // should check whether the two buffers are identical, and maybe skip this
    // step
    buffer = new byte[totalRecords * reclen];
    int copyBytes = Math.min (availableBytes, buffer.length);

    if (copyBytes < 0)
      System.out.printf ("offset %d  len %d  copy %d%n", offset, buffer.length,
          copyBytes);
    else
      System.arraycopy (tempBuffer, offset, buffer, 0, copyBytes);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Record length : " + reclen + "\n");
    text.append ("Buffer length : " + buffer.length + "\n");
    text.append ("First record  : " + firstRecNo + "\n\n");
    text.append (HexFormatter.format (buffer, 0, buffer.length) + "\n");

    return text.toString ();
  }
}