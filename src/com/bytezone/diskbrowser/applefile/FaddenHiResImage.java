package com.bytezone.diskbrowser.applefile;

// -----------------------------------------------------------------------------------//
public class FaddenHiResImage extends OriginalHiResImage
// -----------------------------------------------------------------------------------//
{
  // https://github.com/fadden/fhpack/blob/master/fhpack.cpp

  // ---------------------------------------------------------------------------------//
  public FaddenHiResImage (String name, byte[] buffer, int fileType, int auxType,
      int endOfFile)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer, fileType, auxType, endOfFile);

    assert buffer[0] == 0x66;

    this.buffer = new byte[0x2000];
    int outPtr = 0;

    int ptr = 1;
    while (ptr < buffer.length)
    {
      int literalLen = (buffer[ptr] & 0xF0) >>> 4;
      int matchLen = (buffer[ptr++] & 0x0F) + 4;

      if (literalLen == 15)
        literalLen = (buffer[ptr++] & 0xFF) + 15;

      if (literalLen > 0)
      {
        System.arraycopy (buffer, ptr, this.buffer, outPtr, literalLen);
        ptr += literalLen;
        outPtr += literalLen;
      }

      if (matchLen == 19)           // 15 + 4
      {
        matchLen = (buffer[ptr++] & 0xFF);
        if (matchLen == 254)        // eof
          break;
        if (matchLen == 253)        // no match
          continue;
        matchLen += 19;
      }

      int offset = (buffer[ptr++] & 0xFF) | ((buffer[ptr++] & 0xFF) << 8);
      while (matchLen-- > 0)
        this.buffer[outPtr++] = this.buffer[offset++];
    }
    createImage ();
  }
}
