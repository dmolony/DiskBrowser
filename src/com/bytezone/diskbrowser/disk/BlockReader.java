package com.bytezone.diskbrowser.disk;

import java.util.List;

public interface BlockReader
{
  public byte[] readBlock (int blockNo);

  public byte[] readBlock (Disk2Address diskAddress);

  public byte[] readBlocks (List<Disk2Address> diskAddresses);
}
