package com.bytezone.diskbrowser.gui;

import java.util.EventListener;

public interface FileSelectionListener extends EventListener
{
  public void fileSelected (FileSelectedEvent event);
}