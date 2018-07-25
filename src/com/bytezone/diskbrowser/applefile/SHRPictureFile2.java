package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class SHRPictureFile2 extends HiResImage
{
  ColorTable[] colorTables;
  byte[] scb;                     // 0xC1 aux=0

  // see Graphics & Animation.2mg

  public SHRPictureFile2 (String name, byte[] buffer, int fileType, int auxType, int eof)
  {
    super (name, buffer, fileType, auxType, eof);

    switch (fileType)
    {
      case ProdosConstants.FILE_TYPE_PNT:
        switch (auxType)
        {
          case 0:
            System.out.printf (
                "%s: PNT aux 0 (Paintworks Packed SHR Image) not written yet%n", name);
            break;

          case 1:          // Eagle/PackBytes - unpacks to PIC/$00
            this.buffer = unpackBytes (buffer);
            scb = new byte[200];
            System.arraycopy (this.buffer, 32000, scb, 0, scb.length);

            colorTables = new ColorTable[16];
            for (int i = 0; i < colorTables.length; i++)
              colorTables[i] = new ColorTable (i, this.buffer, 32256 + i * 32);
            break;

          case 2:         // handled in SHRPictureFile1
            break;

          case 3:
            System.out.printf ("%s: PNT aux 3 (Packed IIGS SHR Image) not written yet%n",
                name);
            break;

          case 4:
            System.out.printf (
                "%s: PNT aux 4 (Packed SHR Brooks Image) not written yet%n", name);
            break;

          default:
            System.out.printf ("%s: PNT unknown aux: %04X%n", name, auxType);
        }
        break;

      case ProdosConstants.FILE_TYPE_PIC:
        if (auxType > 2)
        {
          System.out.printf ("%s: PIC changing aux from %04X to 0%n", name, auxType);
          auxType = 0;
        }

        switch (auxType)
        {
          case 0:               // 32,768
            scb = new byte[200];
            System.arraycopy (buffer, 32000, scb, 0, scb.length);

            colorTables = new ColorTable[16];
            for (int i = 0; i < colorTables.length; i++)
              colorTables[i] = new ColorTable (i, buffer, 32256 + i * 32);
            break;

          case 1:
            System.out.printf ("%s: PIC aux 1 not written yet%n", name);
            break;

          case 2:          // Brooks 38,400
            colorTables = new ColorTable[200];
            for (int i = 0; i < colorTables.length; i++)
            {
              colorTables[i] = new ColorTable (i, buffer, 32000 + i * 32);
              colorTables[i].reverse ();
            }
            break;

          default:
            System.out.println ("PIC unknown aux " + auxType);
        }
        break;
      default:
        System.out.println ("unknown filetype " + fileType);
    }

    if (colorTables != null)
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
    {
      text.append ("SCB\n---\n");
      for (int i = 0; i < scb.length; i += 8)
      {
        for (int j = 0; j < 8; j++)
          text.append (String.format ("  %3d:  %02X  ", i + j, scb[i + j]));
        text.append ("\n");
      }
      text.append ("\n");
    }

    if (colorTables != null)
    {
      text.append ("Color Table\n\n #");
      for (int i = 0; i < 16; i++)
        text.append (String.format ("   %02X ", i));
      text.append ("\n--");
      for (int i = 0; i < 16; i++)
        text.append ("  ----");
      text.append ("\n");
      for (ColorTable colorTable : colorTables)
      {
        text.append (colorTable.toLine ());
        text.append ("\n");
      }
    }

    text.append ("\nScreen lines\n\n");
    for (int i = 0; i < 200; i++)
    {
      text.append (HexFormatter.format (buffer, i * 160, 160));
      text.append ("\n\n");
    }

    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}