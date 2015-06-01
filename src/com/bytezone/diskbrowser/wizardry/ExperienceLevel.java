package com.bytezone.diskbrowser.wizardry;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.applefile.AbstractFile;

class ExperienceLevel extends AbstractFile
{
	private final long[] expLevels = new long[13];

	public ExperienceLevel (String name, byte[] buffer)
	{
		super (name, buffer);

		int seq = 0;

		for (int ptr = 0; ptr < buffer.length; ptr += 6)
		{
			if (buffer[ptr] == 0)
				break;

			long points =
						HexFormatter.intValue (buffer[ptr], buffer[ptr + 1])
									+ HexFormatter.intValue (buffer[ptr + 2], buffer[ptr + 3]) * 10000
									+ HexFormatter.intValue (buffer[ptr + 4], buffer[ptr + 5]) * 100000000L;
			expLevels[seq++] = points;
		}
	}

	public long getExperiencePoints (int level)
	{
		if (level < 13)
			return expLevels[level];
		return (level - 12) * expLevels[0] + expLevels[12];
	}

	@Override
	public String getText ()
	{
		StringBuilder line = new StringBuilder ();
		for (long exp : expLevels)
			line.append (exp + "\n");
		return line.toString ();
	}
}