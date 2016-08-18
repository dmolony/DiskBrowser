package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// Based on a pascal routine by Tom Ewers

// link for possible display algorithm:
// http://stackoverflow.com/questions/14184655/set-position-for-drawing-binary-tree

public class Huffman extends AbstractFile
{
  private static final byte[] mask = { 2, 1 };          // bits: 10 or 01
  private static final int[] offset = { 512, 256 };     // offset to left/right nodes

  private byte depth;
  private int msgPtr;
  private byte currentByte;
  private byte[] message;

  private String bufferContents;

  public Huffman (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  public String decodeMessage (byte[] message)
  {
    this.message = message;
    depth = 0;
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
      if ((depth++ & 0x07) == 0)                // every 8th bit...
        currentByte = message[msgPtr++];        // ...get a new byte

      int currentBit = currentByte & 0x01;      // extract the next bit to process
      currentByte >>= 1;                        // and remove it from the current byte

      // use currentBit to determine whether to use the left or right node
      byte nodeValue = buffer[treePtr + offset[currentBit]];

      // if the node is a leaf, return its contents
      if ((buffer[treePtr] & mask[currentBit]) != 0)
        return nodeValue;

      // else continue traversal
      treePtr = nodeValue & 0xFF;
    }
  }

  @Override
  public String getText ()
  {
    if (bufferContents == null)
    {
      StringBuilder text = new StringBuilder ();
      walk (0, "", text);
      bufferContents = text.toString ();
    }
    return bufferContents;
  }

  private void walk (int treePtr, String path, StringBuilder text)
  {
    for (int currentBit = 1; currentBit >= 0; --currentBit)
      if ((buffer[treePtr] & mask[currentBit]) == 0)
        walk (buffer[treePtr + offset[currentBit]] & 0xFF, path + currentBit, text);
      else
      {
        int val = buffer[treePtr + offset[currentBit]] & 0xFF;
        char c = val < 32 || val >= 127 ? ' ' : (char) val;
        text.append (String.format ("%3d  %1.1s  %s%n", val, c, path + currentBit));
      }
  }
}