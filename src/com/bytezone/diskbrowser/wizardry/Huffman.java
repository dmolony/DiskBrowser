package com.bytezone.diskbrowser.wizardry;

public class Huffman
{
  private final byte[] tree;
  private final byte[] left;
  private final byte[] right;

  private int bitNo = 0;
  private int msgPtr = 0;
  private int currentByte = 0;
  private byte[] message;

  public Huffman (byte[] buffer)
  {
    tree = new byte[256];
    left = new byte[256];
    right = new byte[256];

    System.arraycopy (buffer, 0, tree, 0, 256);
    System.arraycopy (buffer, 256, left, 0, 256);
    System.arraycopy (buffer, 512, right, 0, 256);
  }

  public String getMessage (byte[] message)
  {
    this.message = message;
    bitNo = 0;
    msgPtr = 0;
    currentByte = 0;

    int len = getChar ();
    StringBuilder text = new StringBuilder ();
    for (int i = 0; i < len; i++)
      text.append ((char) getChar ());
    return text.toString ();
  }

  private byte getChar ()
  {
    int treePtr = 0;

    while (true)
    {
      if (bitNo == 0)
      {
        bitNo = 8;
        currentByte = message[msgPtr++] & 0xFF;
      }

      int currentBit = currentByte % 2;
      currentByte /= 2;
      bitNo--;

      if (currentBit == 0)                       // take right path
      {
        if ((tree[treePtr] & 0x02) != 0)         // if has right leaf...
          return right[treePtr];                 // return that character
        treePtr = right[treePtr] & 0xFF;         // else go to right node
      }
      else                                       // take left path
      {
        if ((tree[treePtr] & 0x01) != 0)         // if has left leaf...
          return left[treePtr];                  // return that character
        treePtr = left[treePtr] & 0xFF;          // else go to left node
      }
    }
  }
}