package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.common.Utility;

public class MessageBlock
{
  private final byte[] buffer;
  private final int indexOffset;
  private final int indexLength;

  private final List<MessageDataBlock> messageDataBlocks =
      new ArrayList<MessageDataBlock> ();

  public MessageBlock (byte[] buffer)
  {
    this.buffer = buffer;

    indexOffset = Utility.getWord (buffer, 0);
    indexLength = Utility.getWord (buffer, 2);

    int ptr = indexOffset * 512;

    for (int i = 0, max = indexLength / 2; i < max; i++)
    {
      int firstMessageNo = Utility.getWord (buffer, ptr + i * 2);
      MessageDataBlock messageDataBlock =
          new MessageDataBlock (buffer, i * 512, firstMessageNo);
      messageDataBlocks.add (messageDataBlock);
    }
  }

  public byte[] getMessage (int messageNo)
  {
    for (int i = 0; i < messageDataBlocks.size (); i++)
    {
      MessageDataBlock messageDataBlock = messageDataBlocks.get (i);
      if (messageDataBlock.firstMessageNo > messageNo)
        return messageDataBlocks.get (i - 1).getMessage (messageNo);
    }
    return null;
  }

  //  public int getBlock (int msgNo)
  //  {
  //    int ptr = indexOffset * 512;
  //
  //    for (int i = 0; i < indexLength; i += 2)
  //    {
  //      int msg = Utility.getWord (buffer, ptr + i);
  //      if (msg > msgNo)
  //        return i - 1;
  //    }
  //    return indexLength - 1;
  //  }
}