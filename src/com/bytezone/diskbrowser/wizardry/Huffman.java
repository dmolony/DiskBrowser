package com.bytezone.diskbrowser.wizardry;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// Based on a pascal routine by Tom Ewers

// link for possible display algorithm:
// http://stackoverflow.com/questions/14184655/set-position-for-drawing-binary-tree

// -----------------------------------------------------------------------------------//
class Huffman extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  private static final byte[] mask = { 2, 1 };          // bits: 10 or 01
  private static final int[] offset = { 512, 256 };     // offset to left/right nodes

  private byte depth;
  private int msgPtr;
  private byte currentByte;
  private byte[] message;

  private String bufferContents;

  // ---------------------------------------------------------------------------------//
  Huffman (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }

  // ---------------------------------------------------------------------------------//
  byte[] decodeMessageOld (byte[] buffer, int offset, int length)
  // ---------------------------------------------------------------------------------//
  {
    this.message = buffer;
    List<Byte> decoded = new ArrayList<> ();
    int retPtr = 0;
    int max = offset + length;

    depth = 0;
    msgPtr = offset;
    currentByte = 0;

    while (msgPtr < max)
      decoded.add (getChar ());

    byte[] returnBuffer = new byte[decoded.size ()];
    for (byte b : decoded)
      returnBuffer[retPtr++] = b;

    return returnBuffer;
  }

  // ---------------------------------------------------------------------------------//
  byte[] decodeMessage (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    this.message = buffer;

    depth = 0;
    msgPtr = offset;
    currentByte = 0;

    int size = (getChar () & 0xFF) + 1;
    byte[] returnBuffer = new byte[size];
    returnBuffer[0] = (byte) size;
    int ptr = 1;

    while (ptr < size)
      returnBuffer[ptr++] = getChar ();

    return returnBuffer;
  }

  // ---------------------------------------------------------------------------------//
  String decodeMessage (byte[] message)
  // ---------------------------------------------------------------------------------//
  {
    this.message = message;

    depth = 0;
    msgPtr = 0;
    currentByte = 0;

    int len = getChar () & 0xFF;
    StringBuilder text = new StringBuilder ();
    for (int i = 0; i < len; i++)
    {
      int c = getChar () & 0xFF;
      text.append (switch (c)
      {
        case 0x09 -> " OF ";
        case 0x0A -> "POTION";
        case 0x0B -> "STAFF";
        default -> c < 32 ? '?' : (char) c;
      });
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private byte getChar ()
  // ---------------------------------------------------------------------------------//
  {
    int treePtr = 0;                            // start at the root

    while (true)
    {
      if ((depth++ & 0x07) == 0)                // every 8th bit...
        currentByte = message[msgPtr++];        // ...get a new byte

      int currentBit = currentByte & 0x01;      // extract the next bit to process
      currentByte >>>= 1;                        // and remove it from the current byte

      // use currentBit to determine whether to use the left or right node
      byte nodeValue = buffer[treePtr + offset[currentBit]];

      // if the node is a leaf, return its contents
      if ((buffer[treePtr] & mask[currentBit]) != 0)
        return nodeValue;

      // else continue traversal
      treePtr = nodeValue & 0xFF;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    if (bufferContents == null)
    {
      StringBuilder text = new StringBuilder ();
      walk (0, "", text);
      bufferContents = text.toString ();
    }

    return bufferContents;
  }

  // ---------------------------------------------------------------------------------//
  private void walk (int treePtr, String path, StringBuilder text)
  // ---------------------------------------------------------------------------------//
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