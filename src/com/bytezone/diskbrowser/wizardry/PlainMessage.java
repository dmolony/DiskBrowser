package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.utilities.HexFormatter;

class PlainMessage extends Message
{
	public PlainMessage (byte[] buffer)
	{
		super (buffer);
	}

	@Override
	protected String getLine (int offset)
	{
		int length = HexFormatter.intValue (buffer[offset]);
		return HexFormatter.getString (buffer, offset + 1, length);
	}
}