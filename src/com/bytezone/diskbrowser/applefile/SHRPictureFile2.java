package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import com.bytezone.diskbrowser.prodos.ProdosConstants;

public class SHRPictureFile2 extends HiResImage
{
  ColorTable[] colorTables;
  byte[] scb;

  public SHRPictureFile2 (String name, byte[] buffer, int fileType, int auxType, int eof)
  {
    super (name, buffer, fileType, auxType, eof);

    if (fileType == ProdosConstants.FILE_TYPE_PIC)          // 0xC1
    {
      if (auxType == 0)
      {
        scb = new byte[200];
        System.arraycopy (buffer, 32000, scb, 0, scb.length);

        colorTables = new ColorTable[16];
        for (int i = 0; i < colorTables.length; i++)
          colorTables[i] = new ColorTable (i, buffer, 32256 + i * 32);
      }
      else if (auxType == 1)
      {
        System.out.println ("0xC1 aux 1 not written");
      }
      else if (auxType == 2)          // Brooks
      {
        colorTables = new ColorTable[200];
        for (int i = 0; i < colorTables.length; i++)
        {
          colorTables[i] = new ColorTable (i, buffer, 32000 + i * 32);
          colorTables[i].reverse ();
        }
      }
      else
        System.out.println ("unknown aux " + auxType);
    }
    else if (fileType == ProdosConstants.FILE_TYPE_PNT)     // 0xC0
    {
      if (auxType == 0)
      {
        System.out.println ("0xC0 aux 0 not written");
      }
      else if (auxType == 1)
      {
        System.out.println ("0xC0 aux 1 not written");
      }
      else
        System.out.println ("unknown aux " + auxType);
    }
    else
      System.out.println ("unknown filetype " + fileType);

    createImage ();
  }

  @Override
  protected void createMonochromeImage ()
  {
  }

  @Override
  protected void createColourImage ()
  {
    image = new BufferedImage (320, 200, BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();

    int element = 0;
    int ptr = 0;
    for (int row = 0; row < 200; row++)
    {
      ColorTable colorTable =
          scb != null ? colorTables[scb[row] & 0x0F] : colorTables[row];

      for (int col = 0; col < 160; col++)
      {
        int left = (buffer[ptr] & 0xF0) >> 4;
        int right = buffer[ptr] & 0x0F;

        dataBuffer.setElem (element++, colorTable.entries[left].color.getRGB ());
        dataBuffer.setElem (element++, colorTable.entries[right].color.getRGB ());

        ptr++;
      }
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder (super.getText ());
    text.append ("\n\n");

    if (scb != null)
      for (int i = 0; i < scb.length; i++)
        text.append (String.format ("%3d  %02X%n", i, scb[i]));

    for (ColorTable colorTable : colorTables)
    {
      text.append (colorTable);
      text.append ("\n\n");
    }

    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}