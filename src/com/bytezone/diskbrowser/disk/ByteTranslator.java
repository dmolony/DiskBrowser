package com.bytezone.diskbrowser.disk;

public abstract class ByteTranslator
{
  abstract byte encode (byte b);

  abstract byte decode (byte b) throws DiskNibbleException;
}
