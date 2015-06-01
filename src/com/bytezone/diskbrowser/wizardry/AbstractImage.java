package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.applefile.AbstractFile;

public abstract class AbstractImage extends AbstractFile
{
	public AbstractImage (String name, byte[] buffer)
	{
		super (name, buffer);
	}
}