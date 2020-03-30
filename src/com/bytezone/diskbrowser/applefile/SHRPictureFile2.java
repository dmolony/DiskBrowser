package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public class SHRPictureFile2 extends HiResImage
// -----------------------------------------------------------------------------------//
{
  ColorTable[] colorTables;
  byte[] controlBytes;
  int rows = 200;           // may change

  // see Graphics & Animation.2mg

  // ---------------------------------------------------------------------------------//
  public SHRPictureFile2 (String name, byte[] buffer, int fileType, int auxType, int eof)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer, fileType, auxType, eof);

    switch (fileType)
    {
      case ProdosConstants.FILE_TYPE_PNT:           // packed images
        doPnt ();
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

  // ---------------------------------------------------------------------------------//
  private void doPnt ()
  // ---------------------------------------------------------------------------------//
  {
    switch (auxType)
    {
      case 0:                               // packed Paintworks SHR
      case 0x8000:                          // Paintworks Gold
        colorTables = new ColorTable[1];
        colorTables[0] = new ColorTable (0, this.buffer, 0);

        byte[] data = new byte[buffer.length - 0x222];
        System.arraycopy (buffer, 0x0222, data, 0, data.length);
        buffer = unpack (data);
        rows = buffer.length / 160;
        controlBytes = new byte[rows];    // all pointing to 0th color table

        break;

      case 1:                             // packed version of PIC/$00
        buffer = unpack (buffer);
        controlBytes = new byte[rows];
        System.arraycopy (this.buffer, 32000, controlBytes, 0, controlBytes.length);

        colorTables = new ColorTable[16];
        for (int i = 0; i < colorTables.length; i++)
          colorTables[i] = new ColorTable (i, this.buffer, 32256 + i * COLOR_TABLE_SIZE);

        break;

      case 2:                             // handled in SHRPictureFile1
        break;

      case 3:                             // packed version of PIC/$01
        System.out.printf ("%s: PNT aux 3 (QuickDraw PICT) not written yet%n", name);
        failureReason = "not written yet";

        // Apple IIGS Tech Note #46
        // https://www.prepressure.com/library/file-formats/pict
        this.buffer = unpack (buffer);
        int mode = HexFormatter.unsignedShort (this.buffer, 0);
        int rect1 = HexFormatter.unsignedLong (this.buffer, 2);
        int rect2 = HexFormatter.unsignedLong (this.buffer, 6);
        int version = HexFormatter.unsignedShort (this.buffer, 10);    // $8211

        break;

      case 4:                             // packed version of PIC/$02
        System.out.printf ("%s: PNT aux 4 (Packed SHR Brooks Image) not tested yet%n",
            name);
        // haven't seen one to test yet, for now drop through to .3201

      case 99:            // testing .3201 binary files
        // 00000 - 00003  'APP' 0x00
        // 00004 - 06403  200 color tables of 32 bytes each (one color table per scan line)
        // 06404 - eof    packed pixel data --> 32,000 bytes

        colorTables = new ColorTable[200];
        for (int i = 0; i < colorTables.length; i++)
        {
          colorTables[i] = new ColorTable (i, this.buffer, 4 + i * COLOR_TABLE_SIZE);
          colorTables[i].reverse ();
        }

        data = new byte[buffer.length - 6404];      // skip APP. and color tables
        System.arraycopy (buffer, 6404, data, 0, data.length);
        this.buffer = unpack (data);

        break;

      case 0x1000:                        // seems to be a PIC/$00
        if (buffer.length < 32768)
        {
          failureReason = "file size not 32,768";
          break;
        }
        controlBytes = new byte[rows];
        System.arraycopy (buffer, 32000, controlBytes, 0, controlBytes.length);

        colorTables = new ColorTable[16];
        for (int i = 0; i < colorTables.length; i++)
          colorTables[i] = new ColorTable (i, buffer, 32256 + i * COLOR_TABLE_SIZE);

        break;

      default:
        System.out.printf ("%s: PNT unknown aux: %04X%n", name, auxType);
        failureReason = "unknown PNT aux";
    }
  }

  // ---------------------------------------------------------------------------------//
  private void doPic ()
  // ---------------------------------------------------------------------------------//
  {
    switch (auxType)
    {
      case 0:                             // unpacked version of PNT/$01
      case 0x2000:                        // see TotalReplay.2mg
      case 0x0042:
      case 0x0043:
      case 0x4100:                        // no idea what this is
      case 0x4950:
        // 00000 - 31999  pixel data 32,000 bytes
        // 32000 - 32199  200 control bytes (one per scan line)
        // 32200 - 32255  empty
        // 32256 - 32767  16 color tables of 32 bytes each

        controlBytes = new byte[rows];
        System.arraycopy (buffer, 32000, controlBytes, 0, controlBytes.length);

        colorTables = new ColorTable[16];
        for (int i = 0; i < colorTables.length; i++)
          colorTables[i] =
              new ColorTable (i, buffer, COLOR_TABLE_OFFSET_AUX_0 + i * COLOR_TABLE_SIZE);

        break;

      case 1:                             // unpacked version of PNT/$03
        System.out.printf ("%s: PIC aux 1 (QuickDraw PICT) not written yet%n", name);
        failureReason = "not written yet";
        break;

      case 2:                             // unpacked version of PNT/$04, .3200
        // 00000 - 31999  pixel data 32,000 bytes
        // 32000 - 38399  200 color tables of 32 bytes each (one color table per scan line)

        if (buffer.length < 38400 && false)
        {
          failureReason = "Buffer should be 38,400 bytes";
          return;
        }
        int maxTables = (buffer.length - COLOR_TABLE_OFFSET_AUX_2) / COLOR_TABLE_SIZE;
        colorTables = new ColorTable[maxTables];
        for (int i = 0; i < colorTables.length; i++)
        {
          colorTables[i] =
              new ColorTable (i, buffer, COLOR_TABLE_OFFSET_AUX_2 + i * COLOR_TABLE_SIZE);
          colorTables[i].reverse ();
        }
        break;

      default:
        System.out.printf ("PIC unknown aux: %04X%n ", auxType);
        failureReason = "unknown PIC aux";
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void createMonochromeImage ()
  // ---------------------------------------------------------------------------------//
  {
    System.out.println ("monochrome not written");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  void createColourImage ()
  // ---------------------------------------------------------------------------------//
  {
    int imageWidth = 640;
    image = new BufferedImage (imageWidth, rows * 2, BufferedImage.TYPE_INT_RGB);
    DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();

    int element = 0;
    int ptr = 0;

    boolean mode320 = true;
    boolean fillMode = false;
    ColorTable colorTable = null;
    int max = 160;

    for (int line = 0; line < rows; line++)
    {
      if (controlBytes != null)
      {
        int controlByte = controlBytes[line] & 0xFF;
        colorTable = colorTables[controlByte & 0x0F];

        mode320 = (controlByte & 0x80) == 0;
        fillMode = (controlByte & 0x20) != 0;
      }
      else
        colorTable = colorTables[line];

      if (mode320)       // two pixels per col
        ptr = mode320Line (ptr, element, max, colorTable, dataBuffer, imageWidth);
      else              // four pixels per col
        ptr = mode640Line (ptr, element, max, colorTable, dataBuffer, imageWidth);

      element += imageWidth * 2;        // skip line already drawn
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (super.getText ());
    text.append ("\n\n");

    if (controlBytes != null)
    {
      text.append ("SCB\n---\n");
      for (int i = 0; i < controlBytes.length; i += 8)
      {
        for (int j = 0; j < 8; j++)
        {
          if (i + j >= controlBytes.length)
            break;
          text.append (String.format ("  %3d:  %02X  ", i + j, controlBytes[i + j]));
        }
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