package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class SHRPictureFile2 extends HiResImage
{
  ColorTable[] colorTables;
  byte[] controlBytes;

  // see Graphics & Animation.2mg

  public SHRPictureFile2 (String name, byte[] buffer, int fileType, int auxType, int eof)
  {
    super (name, buffer, fileType, auxType, eof);

    switch (fileType)
    {
      case ProdosConstants.FILE_TYPE_PNT:           // packed images
        doPnt (buffer);
        break;

      case ProdosConstants.FILE_TYPE_PIC:           // unpacked images
        doPic ();
        break;

      default:
        System.out.println ("unknown filetype " + fileType);
    }

    if (colorTables != null)
      createImage ();
  }

  private void doPnt (byte[] buffer)
  {
    switch (auxType)
    {
      case 0:                               // packed Paintworks SHR
        controlBytes = new byte[200];       // all pointing to 0th color table
        colorTables = new ColorTable[1];
        colorTables[0] = new ColorTable (0, this.buffer, 0);

        byte[] data = new byte[buffer.length - 0x222];
        System.arraycopy (buffer, 0x0222, data, 0, data.length);
        this.buffer = unpackBytes (data);

        break;

      case 1:                             // packed version of PIC/$00
        this.buffer = unpackBytes (buffer);
        controlBytes = new byte[200];
        System.arraycopy (this.buffer, 32000, controlBytes, 0, controlBytes.length);

        colorTables = new ColorTable[16];
        for (int i = 0; i < colorTables.length; i++)
          colorTables[i] = new ColorTable (i, this.buffer, 32256 + i * 32);
        break;

      case 2:                             // handled in SHRPictureFile1
        break;

      case 3:                             // packed version of PIC/$01
        System.out.printf ("%s: PNT aux 3 (QuickDraw PICT) not written yet%n", name);
        failureReason = "not written yet";

        // Apple IIGS Tech Note #46
        // https://www.prepressure.com/library/file-formats/pict
        this.buffer = unpackBytes (buffer);
        int mode = HexFormatter.unsignedShort (this.buffer, 0);
        int rect1 = HexFormatter.unsignedLong (this.buffer, 2);
        int rect2 = HexFormatter.unsignedLong (this.buffer, 6);
        int version = HexFormatter.unsignedShort (this.buffer, 10);    // $8211

        break;

      case 4:                             // packed version of PIC/$02
        System.out.printf ("%s: PNT aux 4 (Packed SHR Brooks Image) not tested yet%n",
            name);

      case 99:            // testing .3201 binary files
        // 00000 - 00003  'APP' 0x00
        // 00004 - 06403  200 color tables of 32 bytes each (one color table per scan line)
        // 06404 - eof    packed pixel data --> 32,000 bytes

        colorTables = new ColorTable[200];
        for (int i = 0; i < colorTables.length; i++)
        {
          colorTables[i] = new ColorTable (i, this.buffer, 4 + i * 32);
          colorTables[i].reverse ();
        }

        data = new byte[buffer.length - 6404];      // skip APP. and color tables
        System.arraycopy (buffer, 6404, data, 0, data.length);
        this.buffer = unpackBytes (data);
        break;

      default:
        System.out.printf ("%s: PNT unknown aux: %04X%n", name, auxType);
        failureReason = "unknown PNT aux";
    }
  }

  private void doPic ()
  {
    switch (auxType)
    {
      case 0:                             // unpacked version of PNT/$01
      case 0x4100:                        // no idea what this is
        // 00000 - 31999  pixel data 32,000 bytes
        // 32000 - 32199  200 control bytes (one per scan line)
        // 32200 - 32255  empty
        // 32256 - 32767  16 color tables of 32 bytes each

        controlBytes = new byte[200];
        System.arraycopy (buffer, 32000, controlBytes, 0, controlBytes.length);

        colorTables = new ColorTable[16];
        for (int i = 0; i < colorTables.length; i++)
          colorTables[i] = new ColorTable (i, buffer, 32256 + i * 32);
        break;

      case 1:                             // unpacked version of PNT/$03
        System.out.printf ("%s: PIC aux 1 (QuickDraw PICT) not written yet%n", name);
        failureReason = "not written yet";
        break;

      case 2:                             // unpacked version of PNT/$04, .3200
        // 00000 - 31999  pixel data 32,000 bytes
        // 32000 - 38399  200 color tables of 32 bytes each (one color table per scan line)

        if (buffer.length < 38400)
        {
          failureReason = "Buffer should be 38,400 bytes";
          return;
        }
        colorTables = new ColorTable[200];
        for (int i = 0; i < colorTables.length; i++)
        {
          int ptr = 32000 + i * 32;
          colorTables[i] = new ColorTable (i, buffer, ptr);
          colorTables[i].reverse ();
        }
        break;

      default:
        System.out.println ("PIC unknown aux " + auxType);
        failureReason = "unknown PIC aux";
    }
  }

  @Override
  void createMonochromeImage ()
  {
  }

  @Override
  void createColourImage ()
  {
    image = new BufferedImage (320, 200, BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();

    int element = 0;
    int ptr = 0;
    ColorTable colorTable = null;

    for (int row = 0; row < 200; row++)
    {
      if (controlBytes != null)
      {
        int controlByte = controlBytes[row];
        int mode = controlByte & 0x80;
        int index = controlByte & 0x0F;
        int fillMode = controlByte & 0x20;
        colorTable = colorTables[index];
        if (mode != 0)
          System.out.println ("640!!!");
      }
      else
        colorTable = colorTables[row];

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
    text.append ("\n");

    if (controlBytes != null)
    {
      text.append ("SCB\n---\n");
      for (int i = 0; i < controlBytes.length; i += 8)
      {
        for (int j = 0; j < 8; j++)
          text.append (String.format ("  %3d:  %02X  ", i + j, controlBytes[i + j]));
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
      text.append (String.format ("Line: %02X  %<3d%n", i));
      text.append (HexFormatter.format (buffer, i * 160, 160));
      text.append ("\n\n");
    }

    text.deleteCharAt (text.length () - 1);
    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }
}