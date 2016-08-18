package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.common.Utility;

public class MessageBlock implements Iterable<MessageDataBlock>
{
  private final int indexOffset;
  private final int indexLength;

  private final List<MessageDataBlock> messageDataBlocks =
      new ArrayList<MessageDataBlock> ();

  public MessageBlock (byte[] buffer, Huffman huffman)
  {
    indexOffset = Utility.getWord (buffer, 0);
    indexLength = Utility.getWord (buffer, 2);

    int ptr = indexOffset * 512;

    for (int i = 0, max = indexLength / 2; i < max; i++)
    {
      int firstMessageNo = Utility.getWord (buffer, ptr + i * 2);
      byte[] data = new byte[512];
      System.arraycopy (buffer, i * 512, data, 0, data.length);
      MessageDataBlock messageDataBlock = new MessageDataBlock (
          "Block " + i + " Msg " + firstMessageNo, data, firstMessageNo, huffman);
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

  @Override
  public Iterator<MessageDataBlock> iterator ()
  {
    return messageDataBlocks.iterator ();
  }
}