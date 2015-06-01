package com.bytezone.diskbrowser.wizardry;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

class ImageV2 extends AbstractImage
{
	public ImageV2 (String name, byte[] buffer)
	{
		super (name, buffer);

		image = new BufferedImage (70, 48, BufferedImage.TYPE_BYTE_GRAY); // width/height
		DataBuffer db = image.getRaster ().getDataBuffer ();
		int offset = 0;
		int size = 7;

		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 10; j++)
				for (int k = 7; k >= 0; k--)
				{
					int element = i * 560 + j * 7 + k * 70;
					int bits = buffer[offset++] & 0xFF;
					for (int m = size - 1; m >= 0; m--)
					{
						if ((bits & 1) == 1)
							db.setElem (element, 255);
						bits >>= 1;
						element++;
					}
				}
	}
}