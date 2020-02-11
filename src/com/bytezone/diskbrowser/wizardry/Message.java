package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// -----------------------------------------------------------------------------------//
abstract class Message extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private static int nextId = 0;
  protected String message;
  private final int id;
  private int totalLines;
  List<String> lines = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  Message (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super ("Message " + nextId, buffer);
    this.id = nextId;

    int recordLength = 42;
    StringBuilder text = new StringBuilder ();

    for (int ptr = 0; ptr < buffer.length; ptr += recordLength)
    {
      nextId++;
      totalLines++;
      String line = getLine (ptr);
      text.append (line + "\n");
      lines.add (line);
    }
    text.deleteCharAt (text.length () - 1);
    message = text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  protected abstract String getLine (int offset);
  // ---------------------------------------------------------------------------------//

  // ---------------------------------------------------------------------------------//
  public boolean match (int messageNum)
  // ---------------------------------------------------------------------------------//
  {
    if (id == messageNum)
      return true;

    // this code is to allow for a bug in scenario #1
    if (messageNum > id && messageNum < (id + totalLines))
      return true;

    return false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    return message;
  }

  // ---------------------------------------------------------------------------------//
  public String toHTMLString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder message = new StringBuilder ();
    for (String line : lines)
      message.append ("&nbsp;" + line + "&nbsp;<br>");
    if (message.length () > 0)
      for (int i = 0; i < 4; i++)
        message.deleteCharAt (message.length () - 1); // remove <br> tag
    return message.toString ();
  }

  // ---------------------------------------------------------------------------------//
  public static void resetMessageId ()
  // ---------------------------------------------------------------------------------//
  {
    nextId = 0;
  }
}