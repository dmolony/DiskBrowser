package com.bytezone.diskbrowser.gui;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File>
{

  @Override
  public int compare (File thisFile, File thatFile)
  {
    boolean thisFileIsDirectory = thisFile.isDirectory ();
    boolean thatFileIsDirectory = thatFile.isDirectory ();

    if (thisFileIsDirectory && !thatFileIsDirectory)
      return 1;
    if (!thisFileIsDirectory && thatFileIsDirectory)
      return -1;

    return thisFile.getName ().compareToIgnoreCase (thatFile.getName ());
  }
}