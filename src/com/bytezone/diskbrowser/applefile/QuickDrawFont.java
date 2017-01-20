package com.bytezone.diskbrowser.applefile;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.HashMap;
import java.util.Map;

import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class QuickDrawFont extends AbstractFile
{
  Map<Integer, Character> characters = new HashMap<Integer, Character> ();

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
  private final int widMax;
  private final int kernMax;
  private final int nDescent;
  private final int fRectWidth;
  private final int fRectHeight;
  private final int owTLoc;
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

    int ptr = nameLength + 1;         // start of header record

    headerSize = HexFormatter.unsignedShort (buffer, ptr);
    fontDefinitionOffset = nameLength + 1 + headerSize * 2;

    fontFamily = HexFormatter.unsignedShort (buffer, ptr + 2);
    fontStyle = HexFormatter.unsignedShort (buffer, ptr + 4);
    fontSize = HexFormatter.unsignedShort (buffer, ptr + 6);
    versionMajor = buffer[ptr + 8] & 0xFF;
    versionMinor = buffer[ptr + 9] & 0xFF;
    extent = HexFormatter.unsignedShort (buffer, ptr + 10);

    ptr = fontDefinitionOffset;

    fontType = HexFormatter.unsignedShort (buffer, ptr);
    firstChar = HexFormatter.unsignedShort (buffer, ptr + 2);
    lastChar = HexFormatter.unsignedShort (buffer, ptr + 4);
    widMax = HexFormatter.unsignedShort (buffer, ptr + 6);
    kernMax = HexFormatter.signedShort (buffer, ptr + 8);
    nDescent = HexFormatter.signedShort (buffer, ptr + 10);
    fRectWidth = HexFormatter.unsignedShort (buffer, ptr + 12);
    fRectHeight = HexFormatter.unsignedShort (buffer, ptr + 14);
    imageLines = new String[fRectHeight];

    owTLoc = HexFormatter.unsignedShort (buffer, ptr + 16);

    offsetWidthTableOffset = (ptr + 16) + owTLoc * 2;
    locationTableOffset = offsetWidthTableOffset - (lastChar - firstChar + 3) * 2;
    bitImageOffset = ptr + 26;

    ascent = HexFormatter.unsignedShort (buffer, ptr + 18);
    descent = HexFormatter.unsignedShort (buffer, ptr + 20);
    leading = HexFormatter.unsignedShort (buffer, ptr + 22);
    rowWords = HexFormatter.unsignedShort (buffer, ptr + 24);

    totalCharacters = lastChar - firstChar + 2;       // includes missing character

    bitImage = new byte[rowWords * 2 * fRectHeight];      // should use java bits
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

    for (int i = 0; i < fRectHeight; i++)
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
      int location = HexFormatter.unsignedShort (locationTable, i * 2);
      int offset = offsetWidthTable[i * 2] & 0xFF;
      int width = offsetWidthTable[i * 2 + 1] & 0xFF;

      int j = i + 1;
      if (j < max)
      {
        int nextLocation = HexFormatter.unsignedShort (locationTable, j * 2);
        int pixelWidth = nextLocation - location;
        //        System.out.printf ("%3d  %04X  %04X  %2d     %02X  %02X%n", i, location,
        //            nextLocation, pixelWidth, offset, width);
        if (pixelWidth < 0)
        {
          System.out.println ("*********** Bad pixelWidth");
          corrupt = true;
          return;
        }

        if (pixelWidth > 0)
          characters.put (i, new Character (location, pixelWidth));
      }
    }

    if (true)
    {
      int base = 10;
      int spacing = 5;

      int charsWide = (int) Math.sqrt (totalCharacters);
      int charsHigh = (totalCharacters - 1) / charsWide + 1;

      image = new BufferedImage (charsWide * (widMax + spacing) + base * 2,
          charsHigh * (fRectHeight + leading) + base * 2, BufferedImage.TYPE_BYTE_GRAY);

      Graphics2D g2d = image.createGraphics ();
      g2d.setComposite (
          AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) 1.0));

      int x = base;
      int y = base;
      int count = 0;

      for (int i = 0; i < totalCharacters + 1; i++)
      {
        Character character;
        if (characters.containsKey (i))
          character = characters.get (i);
        else
          character = characters.get (lastChar + 1);

        if (character != null)
          g2d.drawImage (character.image, x, y, null);

        x += widMax + spacing;
        if (++count % charsWide == 0)
        {
          x = base;
          y += fRectHeight + leading;
        }
      }
      g2d.dispose ();
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
    text.append (String.format ("Max width    : %d%n", widMax));
    text.append (String.format ("Max kern     : %d%n", kernMax));
    text.append (String.format ("Neg descent  : %d%n", nDescent));
    text.append (String.format ("Width        : %d%n", fRectWidth));
    text.append (String.format ("Height       : %d%n", fRectHeight));
    text.append (String.format ("O/W Offset   : %04X%n", owTLoc));
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

      int location = HexFormatter.unsignedShort (locationTable, i * 2);
      int nextLocation = HexFormatter.unsignedShort (locationTable, (i + 1) * 2);
      int pixelWidth = nextLocation - location;

      text.append (String.format (
          "Char %3d, location %,5d, pixelWidth %2d. offset %,5d, width %,5d%n", i,
          location, pixelWidth, offset, width));

      if (pixelWidth > 0 && location + pixelWidth < imageLines[0].length ())
        for (int j = 0; j < fRectHeight; j++)
        {
          for (int w = 0; w < offset; w++)
            text.append (' ');
          text.append (imageLines[j].substring (location, location + pixelWidth));
          text.append ("\n");
        }
    }

    if (false)
    {
      text.append ("\n\n");
      for (int i = 0; i < fRectHeight; i++)
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
        int location = HexFormatter.unsignedShort (locationTable, i * 2);
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

  class Character
  {
    private final BufferedImage image;
    private final int location;
    private final int pixelWidth;

    // offset - where to start drawing relative to current pen location
    // width - how far to move the current pen location after drawing
    // location - index into imageLines[]
    // pixelWidth - number of pixels to copy from imageLines[]

    public Character (int location, int pixelWidth)
    {
      this.location = location;
      this.pixelWidth = pixelWidth;

      image = new BufferedImage (pixelWidth, fRectHeight, BufferedImage.TYPE_BYTE_GRAY);
      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();

      if (pixelWidth > 0 && location + pixelWidth < imageLines[0].length ())
        for (int i = 0; i < fRectHeight; i++)
        {
          int element = i * pixelWidth;             // start of row
          String row = imageLines[i].substring (location, location + pixelWidth);
          for (char c : row.toCharArray ())
            dataBuffer.setElem (element++, c == '.' ? 0 : 255);
        }
    }
  }
}