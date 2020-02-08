package com.bytezone.diskbrowser.gui;

import java.util.EventListener;

// -----------------------------------------------------------------------------------//
public interface DiskSelectionListener extends EventListener
// -----------------------------------------------------------------------------------//
{
  public void diskSelected (DiskSelectedEvent event);
}