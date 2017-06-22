package com.bytezone.diskbrowser.applefile;

import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class TextFile extends AbstractFile
{
  private int recordLength;               // prodos aux
  private List<TextBuffer> buffers;       // only used if it is a Prodos text file
  private int eof;
  private boolean prodosFile;

  public TextFile (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  public TextFile (String name, byte[] buffer, int auxType, int eof)
  {
    this (name, buffer);
    this.eof = eof;
    recordLength = auxType;
    prodosFile = true;
  }

  public TextFile (String name, List<TextBuffer> buffers, int auxType, int eof)
  {
    super (name, null);
    this.buffers = buffers;
    this.eof = eof;
    recordLength = auxType;
    prodosFile = true;
  }

  @Override
  public String getHexDump ()
  {
    if (buffers == null)
      return (super.getHexDump ());

    StringBuilder text = new StringBuilder ();
    for (TextBuffer tb : buffers)
    {
      for (int i = 0, rec = 0; i < tb.buffer.length; i += tb.reclen, rec++)
      {
        text.append ("\nRecord #" + (tb.firstRecNo + rec) + "\n");
        text.append (HexFormatter.format (tb.buffer, i, tb.reclen) + "\n");
      }
    }
    return text.toString ();
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Name          : " + name + "\n");
    if (prodosFile)
    {
      text.append (String.format ("Record length : %,8d%n", recordLength));
      text.append (String.format ("End of file   : %,8d%n", eof));
    }
    else
      text.append (String.format ("End of file   : %,8d%n", buffer.length));
    text.append ("\n");

    // check whether file is spread over multiple buffers
    if (buffers != null)
      return treeFileText (text);       // calls knownLength()

    // check whether the record length is known
    if (recordLength == 0)
      return unknownLength (text);

    text.append ("Offset  Record#  Text values\n");
    text.append (
        "------  -------  -------------------------------------------------------\n");
    return knownLength (text, 0).toString ();
  }

  private String treeFileText (StringBuilder text)
  {
    text.append ("  Offset    Record#  Text values\n");
    text.append (
        "----------  -------  -------------------------------------------------------\n");
    for (TextBuffer tb : buffers)
    {
      buffer = tb.buffer;
      knownLength (text, tb.firstRecNo);
    }
    return text.toString ();
  }

  private StringBuilder knownLength (StringBuilder text, int recNo)
  {
    for (int ptr = 0; ptr < buffer.length; ptr += recordLength)
    {
      if (buffer[ptr] == 0)
      {
        recNo++;
        continue;
      }
      int len = buffer.length - ptr;
      int bytes = len < recordLength ? len : recordLength;

      while (buffer[ptr + bytes - 1] == 0)
        bytes--;

      if ((buffer[ptr + bytes - 1] & 0x7F) == 0x0D)     // ignore CR
        bytes--;

      text.append (String.format ("%,10d %,8d  %s%n", recNo * recordLength, recNo++,
          HexFormatter.getString (buffer, ptr, bytes)));
    }
    return text;
  }

  private String unknownLength (StringBuilder text)
  {
    int nulls = 0;
    int ptr = 0;
    int size = buffer.length;
    int lastVal = 0;
    boolean newFormat = true;
    boolean showAllOffsets = true;

    if (newFormat)
    {
      text.append ("  Offset    Text values\n");
      text.append ("----------  -------------------------------------------------------"
          + "-------------------\n");
      if (buffer.length == 0)
        return text.toString ();

      if (buffer[0] != 0)
        text.append (String.format ("%,10d  ", ptr));
    }

    int gcd = 0;

    while (ptr < size)
    {
      int val = buffer[ptr++] & 0x7F;                   // strip hi-order bit
      if (val == 0)
        ++nulls;
      else if (val == 0x0D)                             // carriage return
        text.append ("\n");
      else
      {
        if (nulls > 0)
        {
          if (newFormat)
            text.append (String.format ("%,10d  ", ptr - 1));
          else
            text.append ("\nNew record at : " + (ptr - 1) + "\n");
          nulls = 0;

          gcd = gcd == 0 ? ptr - 1 : gcd (gcd, ptr - 1);
        }
        else if (lastVal == 0x0D && newFormat)
          if (showAllOffsets)
            text.append (String.format ("%,10d  ", ptr - 1));
          else
            text.append ("              ");

        text.append ((char) val);
      }
      lastVal = val;
    }

    if (gcd > 0)
      text.append (String.format ("%nGCD: %,d", gcd));
    else if (text.length () > 0 && text.charAt (text.length () - 1) == '\n')
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  private int gcd (int a, int b)
  {
    return a == 0 ? b : gcd (b % a, a);
  }
}