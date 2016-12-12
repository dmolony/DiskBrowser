package com.bytezone.diskbrowser.duplicates;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class RootFolderData
{
  // list of checksum -> DiskDetails
  public final Map<Long, DiskDetails> checksumMap = new HashMap<Long, DiskDetails> ();

  // list of unique disk names -> DiskDetails
  public final Map<String, DiskDetails> fileNameMap = new TreeMap<String, DiskDetails> ();

  public final ProgressState progressState = new ProgressState ();

  //  public RootFolderData ()
  //  {
  //  }
}