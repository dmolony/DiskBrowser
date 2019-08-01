package com.bytezone.diskbrowser.nib;

public class NibbleTrack
{
  byte[] buffer;
  int bitsUsed;

  public NibbleTrack (byte[] buffer, int length, int bitsUsed)
  {
    assert false : "Not used";
    this.bitsUsed = bitsUsed;
    this.buffer = new byte[length];
    System.arraycopy (buffer, 0, this.buffer, 0, length);
  }
}
