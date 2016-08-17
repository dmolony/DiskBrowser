package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// Based on a pascal routine by Tom Ewers

public class Huffman extends AbstractFile
{
  private final byte[] tree;
  private final byte[] left;
  private final byte[] right;

  private int bitNo;
  private int msgPtr;
  private int currentByte;
  private byte[] message;

  private String treeContents;

  public Huffman (String name, byte[] buffer)
  {
    super (name, buffer);

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
    int treePtr = 0;                            // start at the root

    while (true)
    {
      if (bitNo-- == 0)
      {
        bitNo += 8;
        currentByte = message[msgPtr++] & 0xFF;
      }

      int currentBit = currentByte % 2;         // get the next bit to process
      currentByte /= 2;

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

  @Override
  public String getText ()
  {
    if (treeContents == null)
    {
      StringBuilder text = new StringBuilder ();
      walk (0, "", text);
      treeContents = text.toString ();
    }
    return treeContents;
  }

  private void walk (int treePtr, String path, StringBuilder text)
  {
    if ((tree[treePtr] & 0x01) == 0)
      walk (left[treePtr] & 0xFF, path + "1", text);
    else
      print (path + "1", left[treePtr], text);

    if ((tree[treePtr] & 0x02) == 0)
      walk (right[treePtr] & 0xFF, path + "0", text);
    else
      print (path + "0", right[treePtr], text);
  }

  private void print (String path, byte value, StringBuilder text)
  {
    int val = value & 0xFF;
    char c = val < 32 || val >= 127 ? ' ' : (char) val;
    text.append (String.format ("%3d  %1.1s  %s%n", val, c, path));
  }
}