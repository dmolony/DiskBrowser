package com.bytezone.diskbrowser.nib;

public interface ByteTranslator
{
  abstract byte encode (byte b);

  abstract byte decode (byte b) throws DiskNibbleException;
}
