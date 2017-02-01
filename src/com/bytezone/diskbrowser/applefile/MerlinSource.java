package com.bytezone.diskbrowser.applefile;

public class MerlinSource extends AbstractFile
{
  int ptr;
  private static int[] tabs = { 12, 19, 35 };
  private static int TAB_POS = tabs[2];
  private final int recordLength;
  private final int eof;
  private boolean prodosFile;

  // Source : Prodos text file
  public MerlinSource (String name, byte[] buffer, int recordLength, int eof)
  {
    super (name, buffer);
    this.eof = eof;
    this.recordLength = recordLength;
    prodosFile = true;
  }

  // Source : Dos binary file
  public MerlinSource (String name, byte[] buffer)
  {
    super (name, buffer);
    this.eof = 0;
    this.recordLength = 0;
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ();

    text.append ("Merlin source : " + name + "\n");
    if (prodosFile)
    {
      text.append (String.format ("Record length : %,8d%n", recordLength));
      text.append (String.format ("EOF (aux)     : %,8d%n", eof));
    }
    else
      text.append (String.format ("Buffer size   : %,8d%n", buffer.length));
    text.append ("\n");

    ptr = 0;
    while (ptr < buffer.length && buffer[ptr] != 0)
      text.append (getLine () + "\n");
    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  private String getLine ()
  {
    StringBuilder line = new StringBuilder ();
    boolean comment = false;
    boolean string = false;
    while (ptr < buffer.length)
    {
      int val = buffer[ptr++] & 0x7F;
      if (val == 0x0D)
        break;
      if (val == '*' && line.length () == 0)
        comment = true;
      if (val == '"')
        string = !string;
      if (val == ';' && !comment)
      {
        comment = true;
        while (line.length () < TAB_POS)
          line.append (' ');
      }
      if (val == ' ' && !comment && !string)
      {
        line = tab (line);
        if (line.length () >= tabs[2])
          comment = true;
      }
      else
        line.append ((char) val);
    }
    return line.toString ();
  }

  private StringBuilder tab (StringBuilder text)
  {
    int nextTab = 0;
    for (int tab : tabs)
      if (text.length () < tab)
      {
        nextTab = tab;
        break;
      }
    while (text.length () < nextTab)
      text.append (' ');
    return text;
  }
}