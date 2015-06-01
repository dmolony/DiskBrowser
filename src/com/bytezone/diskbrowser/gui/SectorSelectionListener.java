package com.bytezone.diskbrowser.gui;

import java.util.EventListener;

public interface SectorSelectionListener extends EventListener
{
	public void sectorSelected (SectorSelectedEvent event);
}