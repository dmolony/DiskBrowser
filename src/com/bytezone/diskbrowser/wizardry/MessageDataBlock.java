package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class MessageDataBlock
{
  private final byte[] buffer;
  private final int offset;
  final int firstMessageNo;

  private final int groupCount;
  private final List<Message> messages = new ArrayList<Message> ();

  public MessageDataBlock (byte[] buffer, int offset, int firstMessageNo)
  {
    this.buffer = buffer;
    this.offset = offset;
    this.firstMessageNo = firstMessageNo;

    boolean debug = firstMessageNo == 0;

    if (debug)
    {
      System.out.println (HexFormatter.format (buffer, offset, 512));
      System.out.println ();
    }

    int ptr = offset + 0x1FF;          // last byte in block
    groupCount = buffer[ptr--] & 0xFF;

    int currentMessageNo = firstMessageNo;
    int totalMessageBytes = 0;

    for (int i = 0, max = groupCount - 1; i < groupCount; i++, max--)
    {
      int huffBytes = buffer[ptr];

      for (int j = 0; j < huffBytes; j++)
      {
        int messageLength = buffer[ptr - j - 1] & 0xFF;
        totalMessageBytes += messageLength;
        Message message = new Message (currentMessageNo + j,
            offset + totalMessageBytes - messageLength, messageLength);
        messages.add (message);
      }

      ptr -= huffBytes;
      currentMessageNo += huffBytes;

      ptr--;

      if (max > 0)
      {
        byte gap = buffer[ptr--];
        int skip = gap & 0xFF;

        if ((gap & 0x80) != 0)          // is high bit set?
        {
          gap &= 0x7F;
          int gap2 = buffer[ptr--] & 0xFF;
          skip = gap * 256 + gap2;
        }

        skip--;
        currentMessageNo += skip;
      }
    }

    if (debug)
      System.out.println (this);
  }

  byte[] getMessage (int messageNo)
  {
    for (Message message : messages)
      if (message.msgNo == messageNo)
      {
        byte[] returnMessage = new byte[message.length];
        System.arraycopy (buffer, message.offset, returnMessage, 0, message.length);
        return returnMessage;
      }
    return null;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    for (Message message : messages)
    {
      text.append (message);
      text.append ("\n");
    }

    if (text.length () > 0)
      text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  class Message
  {
    final int msgNo;
    final int offset;
    final int length;

    public Message (int msgNo, int offset, int length)
    {
      this.msgNo = msgNo;
      this.offset = offset;
      this.length = length;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      String data = Utility.getHex (buffer, offset, length);
      text.append (String.format ("%5d: %02X  %02X : %s", msgNo, offset, length, data));

      return text.toString ();
    }
  }
}