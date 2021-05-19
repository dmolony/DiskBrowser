package com.bytezone.diskbrowser.applefile;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// see IIGS System 6.0.1 - Disk 5 Fonts.po
// -----------------------------------------------------------------------------------//
public class QuickDrawFont extends CharacterList
// -----------------------------------------------------------------------------------//
{
  Map<Integer, QuickDrawCharacter> qdCharacters = new HashMap<> ();

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

  private final int fontDefinitionOffset;
  private final int bitImageOffset;
  private final int locationTableOffset;
  private final int offsetWidthTableOffset;
  private int offsetWidthTableSize;

  private BitSet[] strike;        // bit image of all characters

  // ---------------------------------------------------------------------------------//
  public QuickDrawFont (String name, byte[] buffer, int fileType, int auxType)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    assert fileType == ProdosConstants.FILE_TYPE_FONT;
    this.fileType = fileType;
    this.auxType = auxType;

    if (auxType != 0)
      System.out.printf ("Font aux: %04X%n", auxType);

    fontName = HexFormatter.getPascalString (buffer, 0);
    int nameLength = (buffer[0] & 0xFF);

    int ptr = nameLength + 1;         // start of header record

    headerSize = Utility.getShort (buffer, ptr);
    fontDefinitionOffset = nameLength + 1 + headerSize * 2;

    fontFamily = Utility.getShort (buffer, ptr + 2);
    fontStyle = Utility.getShort (buffer, ptr + 4);
    fontSize = Utility.getShort (buffer, ptr + 6);
    versionMajor = buffer[ptr + 8] & 0xFF;
    versionMinor = buffer[ptr + 9] & 0xFF;
    extent = Utility.getShort (buffer, ptr + 10);

    ptr = fontDefinitionOffset;

    fontType = Utility.getShort (buffer, ptr);
    firstChar = Utility.getShort (buffer, ptr + 2);
    lastChar = Utility.getShort (buffer, ptr + 4);
    widMax = Utility.getShort (buffer, ptr + 6);
    kernMax = Utility.signedShort (buffer, ptr + 8);
    nDescent = Utility.signedShort (buffer, ptr + 10);
    fRectWidth = Utility.getShort (buffer, ptr + 12);
    fRectHeight = Utility.getShort (buffer, ptr + 14);

    owTLoc = Utility.getShort (buffer, ptr + 16);

    offsetWidthTableOffset = (ptr + 16) + owTLoc * 2;
    locationTableOffset = offsetWidthTableOffset - (lastChar - firstChar + 3) * 2;
    bitImageOffset = ptr + 26;

    ascent = Utility.getShort (buffer, ptr + 18);
    descent = Utility.getShort (buffer, ptr + 20);
    leading = Utility.getShort (buffer, ptr + 22);
    rowWords = Utility.getShort (buffer, ptr + 24);

    totalCharacters = lastChar - firstChar + 2;       // includes 'missing' character

    offsetWidthTableSize = (totalCharacters + 1) * 2;

    if (offsetWidthTableOffset + offsetWidthTableSize > buffer.length)
    {
      System.out.println ("*********** Bad ow length");
      strike = null;
      corrupt = true;
      return;
    }

    createStrike ();
    createCharacters ();
    //    buildDisplay ();
    buildImage (10, 10, 5, 5, widMax, fRectHeight,
        (int) (Math.sqrt (totalCharacters) + .5));
  }

  // ---------------------------------------------------------------------------------//
  private void createStrike ()
  // ---------------------------------------------------------------------------------//
  {
    // create bitset for each row
    strike = new BitSet[fRectHeight];
    for (int i = 0; i < fRectHeight; i++)
      strike[i] = new BitSet (rowWords * 16);

    // convert image data to bitset
    int rowLenBits = rowWords * 16;                     // # bits in each row
    int rowLenBytes = rowWords * 2;                     // # bytes in each row

    for (int row = 0; row < fRectHeight; row++)         // for each row in character
      for (int bit = 0; bit < rowLenBits; bit++)        // for each bit in the row
      {
        byte b = buffer[bitImageOffset + row * rowLenBytes + bit / 8];
        strike[row].set (bit, ((b & (0x80 >>> (bit % 8))) != 0));
      }
  }

  // ---------------------------------------------------------------------------------//
  private void createCharacters ()
  // ---------------------------------------------------------------------------------//
  {
    for (int i = 0, max = totalCharacters + 1; i < max; i++)
    {
      // index into the strike
      int location = Utility.getShort (buffer, locationTableOffset + i * 2);

      int j = i + 1;      // next character
      if (j < max)
      {
        int nextLocation = Utility.getShort (buffer, locationTableOffset + j * 2);
        int pixelWidth = nextLocation - location;

        if (pixelWidth > 0)
        {
          QuickDrawCharacter c = new QuickDrawCharacter (location, pixelWidth);
          qdCharacters.put (i, c);
          characters.add (c);
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void buildDisplay ()
  // ---------------------------------------------------------------------------------//
  {
    int inset = 10;
    int spacing = 5;

    int charsWide = (int) (Math.sqrt (totalCharacters) + .5);
    int charsHigh = (totalCharacters - 1) / charsWide + 1;

    image = new BufferedImage (charsWide * (widMax + spacing) + inset * 2,
        charsHigh * (fRectHeight + spacing) + inset * 2, BufferedImage.TYPE_BYTE_GRAY);

    Graphics2D g2d = image.createGraphics ();
    g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) 1.0));

    int x = inset;
    int y = inset;
    int count = 0;

    for (int i = 0; i < totalCharacters + 1; i++)
    {
      int pos = qdCharacters.containsKey (i) ? i : lastChar + 1;
      QuickDrawCharacter character = qdCharacters.get (pos);

      // how the character image to be drawn should be positioned with
      // respect to the current pen location
      //      int offset = buffer[offsetWidthTableOffset + i * 2 + 1];
      // how far the pen should be advanced after the character is drawn
      //      int width = buffer[offsetWidthTableOffset + i * 2] & 0xFF;

      if (character != null)
        g2d.drawImage (character.image, x, y, null);

      x += widMax + spacing;
      if (++count % charsWide == 0)
      {
        x = inset;
        y += fRectHeight + spacing;
      }
    }
    g2d.dispose ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");
    text.append ("File type : Font\n");

    String auxTypeText =
        auxType == 0 ? "QuickDraw Font File" : auxType == 1 ? "XX" : "??";
    text.append (String.format ("Aux type  : %04X  (%s)%n%n", auxType, auxTypeText));
    text.append (String.format ("Font name    : %s%n", fontName));
    text.append (String.format ("Font family  : %d%n", fontFamily));
    text.append (String.format ("File type    : %d%n", fileType));
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
      int offset = buffer[offsetWidthTableOffset + i * 2 + 1] & 0xFF;
      int width = buffer[offsetWidthTableOffset + i * 2] & 0xFF;

      if (offset == 255 && width == 255)
        continue;

      int location = Utility.getShort (buffer, locationTableOffset + i * 2);
      int nextLocation = Utility.getShort (buffer, locationTableOffset + (i + 1) * 2);
      int pixelWidth = nextLocation - location;

      text.append (String.format (
          "Char %3d, location %,5d, pixelWidth %2d. offset %,5d, width %,5d%n", i,
          location, pixelWidth, offset, width));
    }

    //    text.append (super.getText ());

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  class QuickDrawCharacter extends Character
  // ---------------------------------------------------------------------------------//
  {
    // -------------------------------------------------------------------------------//
    public QuickDrawCharacter (int strikeOffset, int strikeWidth)
    // -------------------------------------------------------------------------------//
    {
      super (strikeWidth, fRectHeight);

      DataBuffer dataBuffer = image.getRaster ().getDataBuffer ();

      int element = 0;
      for (int row = 0; row < fRectHeight; row++)
        for (int j = strikeOffset; j < strikeOffset + strikeWidth; j++)
          dataBuffer.setElem (element++, strike[row].get (j) ? 255 : 0);
    }
  }
}