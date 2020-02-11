package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// -----------------------------------------------------------------------------------//
abstract class AbstractImage extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  AbstractImage (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);
  }
}