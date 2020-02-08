package com.bytezone.diskbrowser.gui;

import java.util.EventListener;

// -----------------------------------------------------------------------------------//
public interface FileNodeSelectionListener extends EventListener
// -----------------------------------------------------------------------------------//
{
  public void fileNodeSelected (FileNodeSelectedEvent event);
}