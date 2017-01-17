package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class QuickDrawFont extends AbstractFile
{
  private boolean corrupt;
  private final int fileType;
  private final int auxType;
  private final String fontName;
  private final int headerSize;
  private final int fontSize;
  private final int fontFamily;
  private final int fontStyle;
  private final int versionMajor;
  private final int versionMinor;
  private final int extent;
  private final int fontType;
  private final int firstChar;
  private final int lastChar;
  private final int maxWidth;
  private final int maxKern;
  private final int negativeDescent;
  private final int rectangleWidth;
  private final int rectangleHeight;
  private final int offsetToOffsetWidthTable;
  private final int ascent;
  private final int descent;
  private final int leading;
  private final int rowWords;

  private final int totalCharacters;
  private final String[] imageLines;

  private final byte[] bitImage;
  private final byte[] locationTable;
  private final byte[] offsetWidthTable;

  private final int fontDefinitionOffset;
  private final int bitImageOffset;
  private final int locationTableOffset;
  private final int offsetWidthTableOffset;

  public QuickDrawFont (String name, byte[] buffer, int fileType, int auxType)
  {
    super (name, buffer);

    assert fileType == ProdosConstants.FILE_TYPE_FONT;
    this.fileType = fileType;
    this.auxType = auxType;

    fontName = HexFormatter.getPascalString (buffer, 0);
    int nameLength = (buffer[0] & 0xFF);

    int ptr = nameLength + 1;

    headerSize = HexFormatter.getShort (buffer, ptr);
    fontDefinitionOffset = nameLength + 1 + headerSize * 2;

    fontFamily = HexFormatter.getShort (buffer, ptr + 2);
    fontStyle = HexFormatter.getShort (buffer, ptr + 4);
    fontSize = HexFormatter.getShort (buffer, ptr + 6);
    versionMajor = buffer[ptr + 8] & 0xFF;
    versionMinor = buffer[ptr + 9] & 0xFF;
    extent = HexFormatter.getShort (buffer, ptr + 10);

    ptr = fontDefinitionOffset;

    fontType = HexFormatter.getShort (buffer, ptr);
    firstChar = HexFormatter.getShort (buffer, ptr + 2);
    lastChar = HexFormatter.getShort (buffer, ptr + 4);
    maxWidth = HexFormatter.getShort (buffer, ptr + 6);
    maxKern = HexFormatter.getShort (buffer, ptr + 8);
    negativeDescent = HexFormatter.getShort (buffer, ptr + 10);
    rectangleWidth = HexFormatter.getShort (buffer, ptr + 12);
    rectangleHeight = HexFormatter.getShort (buffer, ptr + 14);
    imageLines = new String[rectangleHeight];

    offsetToOffsetWidthTable = HexFormatter.getShort (buffer, ptr + 16);

    offsetWidthTableOffset = (ptr + 16) + offsetToOffsetWidthTable * 2;
    locationTableOffset = offsetWidthTableOffset - (lastChar - firstChar + 3) * 2;
    bitImageOffset = ptr + 26;

    ascent = HexFormatter.getShort (buffer, ptr + 18);
    descent = HexFormatter.getShort (buffer, ptr + 20);
    leading = HexFormatter.getShort (buffer, ptr + 22);
    rowWords = HexFormatter.getShort (buffer, ptr + 24);

    totalCharacters = lastChar - firstChar + 2;       // includes missing character

    bitImage = new byte[rowWords * 2 * rectangleHeight];      // should use java bits
    locationTable = new byte[(totalCharacters + 1) * 2];
    offsetWidthTable = new byte[(totalCharacters + 1) * 2];

    if (false)
    {
      System.out.printf ("Buffer length  : %d%n", buffer.length);
      System.out.printf ("Total chars    : %d%n", totalCharacters);
      System.out.printf ("owtable offset : %d%n", offsetWidthTableOffset);
      System.out.printf ("owtable size   : %d%n", offsetWidthTable.length);
    }

    if (offsetWidthTableOffset + offsetWidthTable.length > buffer.length)
    {
      System.out.println ("*********** Bad ow length");
      corrupt = true;
      return;
    }

    System.arraycopy (buffer, bitImageOffset, bitImage, 0, bitImage.length);
    System.arraycopy (buffer, locationTableOffset, locationTable, 0,
        locationTable.length);
    System.arraycopy (buffer, offsetWidthTableOffset, offsetWidthTable, 0,
        offsetWidthTable.length);

    for (int i = 0; i < rectangleHeight; i++)
    {
      int rowOffset = i * rowWords * 2;

      StringBuilder bits = new StringBuilder ();
      for (int j = rowOffset; j < rowOffset + rowWords * 2; j++)
        bits.append (HexFormatter.getBitString (bitImage[j]));
      imageLines[i] = bits.toString ().replaceAll ("1", "#");
    }

    //    System.out.println ("\n  Location table       o/w table\n");
    for (int i = 0, max = totalCharacters + 1; i < max; i++)
    {
      int location = HexFormatter.getShort (locationTable, i * 2);
      int offset = offsetWidthTable[i * 2] & 0xFF;
      int width = offsetWidthTable[i * 2 + 1] & 0xFF;

      int j = i + 1;
      if (j < max)
      {
        int nextLocation = HexFormatter.getShort (locationTable, j * 2);
        int pixelWidth = nextLocation - location;
        //        System.out.printf ("%3d  %04X  %04X  %2d     %02X  %02X%n", i, location,
        //            nextLocation, pixelWidth, offset, width);
        if (pixelWidth < 0)
        {
          System.out.println ("*********** Bad pixelWidth");
          corrupt = true;
          return;
        }
      }
      //      else
      //        System.out.printf ("%3d  %04X %n", i, location);
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");
    text.append ("File type : Font\n");

    String auxTypeText =
        auxType == 0 ? "QuickDraw Font File" : auxType == 1 ? "XX" : "??";
    text.append (String.format ("Aux type  : %04X  (%s)%n%n", auxType, auxTypeText));
    text.append (String.format ("Font name    : %s%n", fontName));
    text.append (String.format ("Font family  : %d%n", fontFamily));
    text.append (String.format ("Font style   : %d%n", fontStyle));
    text.append (String.format ("Font size    : %d%n", fontSize));
    text.append (String.format ("Font version : %d.%d%n", versionMajor, versionMinor));
    text.append (String.format ("Font extent  : %d%n%n", extent));
    text.append (String.format ("Font type    : %d%n", fontType));
    text.append (String.format ("First char   : %d%n", firstChar));
    text.append (String.format ("Last char    : %d%n", lastChar));
    text.append (String.format ("Max width    : %d%n", maxWidth));
    text.append (String.format ("Max kern     : %d%n", maxKern));
    text.append (String.format ("Neg descent  : %d%n", negativeDescent));
    text.append (String.format ("Width        : %d%n", rectangleWidth));
    text.append (String.format ("Height       : %d%n", rectangleHeight));
    text.append (String.format ("O/W Offset   : %d%n", offsetToOffsetWidthTable));
    text.append (String.format ("Ascent       : %d%n", ascent));
    text.append (String.format ("Descent      : %d%n", descent));
    text.append (String.format ("Leading      : %d%n", leading));
    text.append (String.format ("Row words    : %d%n%n", rowWords));

    if (corrupt)
    {
      text.append ("\nCannot interpret Font file");
      return text.toString ();
    }

    for (int i = 0; i < totalCharacters; i++)
    {
      int offset = offsetWidthTable[i * 2] & 0xFF;
      int width = offsetWidthTable[i * 2 + 1] & 0xFF;

      if (offset == 255 && width == 255)
        continue;

      int location = HexFormatter.getShort (locationTable, i * 2);
      int nextLocation = HexFormatter.getShort (locationTable, (i + 1) * 2);
      int pixelWidth = nextLocation - location;

      text.append (String.format ("Char %3d  %,5d  %2d  %,5d  %,5d%n", i, location,
          pixelWidth, offset, width));

      if (pixelWidth > 0 && location + pixelWidth < imageLines[0].length ())
        for (int j = 0; j < rectangleHeight; j++)
        {
          for (int w = 0; w < width; w++)
            text.append (' ');
          text.append (imageLines[j].substring (location, location + pixelWidth));
          text.append ("\n");
        }
    }

    if (false)
    {
      text.append ("\n\n");
      for (int i = 0; i < rectangleHeight; i++)
      {
        text.append (String.format ("Row: %d%n", i));
        int rowOffset = i * rowWords * 2;
        String line = HexFormatter.format (bitImage, rowOffset, rowWords * 2,
            bitImageOffset + rowOffset);
        text.append (line);
        text.append ("\n\n");
      }

      text.append ("\n\n");
      text.append (HexFormatter.format (locationTable, 0, locationTable.length,
          locationTableOffset));

      text.append ("\n\n");
      text.append (HexFormatter.format (offsetWidthTable, 0, offsetWidthTable.length,
          offsetWidthTableOffset));

      text.append ("\n\n");
      for (int i = 0; i < totalCharacters; i++)
      {
        int location = HexFormatter.getShort (locationTable, i * 2);
        text.append (String.format ("%3d  %04X  %,7d%n", i, location, location));
      }

      text.append ("\n\n");
      for (int i = 0; i < totalCharacters; i++)
      {
        int offset = offsetWidthTable[i * 2] & 0xFF;
        int width = offsetWidthTable[i * 2 + 1] & 0xFF;
        text.append (String.format ("%3d  %02X  %02X%n", i, offset, width));
      }
    }

    return text.toString ();
  }
}