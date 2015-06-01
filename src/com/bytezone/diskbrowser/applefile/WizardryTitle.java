package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.HexFormatter;

public class WizardryTitle extends AbstractFile
{
	public WizardryTitle (String name, byte[] buffer)
	{
		super (name, buffer);
	}

	@Override
	public String getText ()
	{
		int size = 20;
		StringBuilder text = new StringBuilder ();
		for (int i = 0; i < buffer.length; i += size)
		{
			for (int line = 0; line < size; line++)
			{
				int p = i + line;
				if (p >= buffer.length)
					break;
				int value = HexFormatter.intValue (buffer[p]);
				text = decode2 (value, text);
			}
			text.append ("\n");
		}
		return text.toString ();
	}

	private StringBuilder decode (int value, StringBuilder text)
	{
		for (int bit = 0; bit < 8; bit++)
		{
			text.append ((value & 0x01) == 1 ? "X" : " ");
			value >>= 1;
		}
		return text;
	}

	private StringBuilder decode2 (int value, StringBuilder text)
	{
		for (int bit = 7; bit >= 0; bit--)
		{
			text.append ((value & 0x01) == 1 ? "X" : " ");
			value >>= 1;
		}
		return text;
	}
}