package com.bytezone.diskbrowser.wizardry;

public class Huffman
{
  private final byte[] tree;
  private final byte[] left;
  private final byte[] right;

  private int bit = 0;
  private int msgPtr = 0;
  private int b = 0;
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
    bit = 0;
    msgPtr = 0;
    b = 0;

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
      if (bit == 0)
      {
        bit = 8;
        b = message[msgPtr++] & 0xFF;
      }

      int thisBit = b % 2;
      b /= 2;
      bit--;

      if (thisBit == 0)                          // take right path
      {
        if ((tree[treePtr] & 0x02) != 0)         // if has right leaf
          return right[treePtr];
        treePtr = right[treePtr] & 0xFF;                // go to right node
      }
      else                                       // take left path
      {
        if ((tree[treePtr] & 0x01) != 0)         // if has left leaf
          return left[treePtr];
        treePtr = left[treePtr] & 0xFF;                 // go to left node
      }
    }
  }
}