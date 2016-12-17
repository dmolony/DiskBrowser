package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class IconFile extends AbstractFile
{
  private final int iBlkNext;
  private final int iBlkID;
  private final int iBlkPath;
  private final String iBlkName;
  private final List<Icon> icons = new ArrayList<IconFile.Icon> ();

  public IconFile (String name, byte[] buffer)
  {
    super (name, buffer);

    iBlkNext = HexFormatter.getLong (buffer, 0);
    iBlkID = HexFormatter.getShort (buffer, 4);
    iBlkPath = HexFormatter.getLong (buffer, 6);
    iBlkName = HexFormatter.getHexString (buffer, 10, 16);

    int ptr = 26;
    while (true)
    {
      int dataLen = HexFormatter.getShort (buffer, ptr);
      if (dataLen == 0 || (dataLen + ptr) > buffer.length)
        break;
      icons.add (new Icon (buffer, ptr));
      ptr += dataLen;
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");

    text.append (String.format ("Next Icon file .. %d%n", iBlkNext));
    text.append (String.format ("Block ID ........ %d%n", iBlkID));
    text.append (String.format ("Block path ...... %d%n", iBlkPath));
    text.append (String.format ("Block name ...... %s%n", iBlkName));

    text.append ("\n");
    for (Icon icon : icons)
    {
      text.append (icon);
      text.append ("\n\n");
    }

    return text.toString ();
  }

  class Icon
  {
    byte[] buffer;
    int iDataLen;
    String pathName;
    String dataName;
    int iDataType;
    int iDataAux;
    Image largeImage;
    Image smallImage;

    public Icon (byte[] fullBuffer, int ptr)
    {
      iDataLen = HexFormatter.getShort (fullBuffer, ptr);

      buffer = new byte[iDataLen];
      System.arraycopy (fullBuffer, ptr, buffer, 0, buffer.length);

      int len = buffer[2] & 0xFF;
      pathName = new String (buffer, 3, len);

      len = buffer[66] & 0xFF;
      dataName = new String (buffer, 67, len);

      iDataType = HexFormatter.getShort (buffer, 82);
      iDataAux = HexFormatter.getShort (buffer, 84);

      largeImage = new Image (buffer, 86);
      smallImage = new Image (buffer, 86 + largeImage.size ());
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();
      text.append (String.format ("Data length .. %04X%n", iDataLen));
      text.append (String.format ("Path name .... %s%n", pathName));
      text.append (String.format ("Data name .... %s%n", dataName));
      text.append ("\n");
      text.append (largeImage);
      text.append ("\n");
      text.append ("\n");
      text.append (smallImage);
      return text.toString ();
    }
  }

  class Image
  {
    int iconType;
    int iconSize;
    int iconHeight;
    int iconWidth;
    byte[] main;
    byte[] mask;

    public Image (byte[] buffer, int ptr)
    {
      iconType = HexFormatter.getShort (buffer, ptr);
      iconSize = HexFormatter.getShort (buffer, ptr + 2);
      iconHeight = HexFormatter.getShort (buffer, ptr + 4);
      iconWidth = HexFormatter.getShort (buffer, ptr + 6);

      main = new byte[iconSize];
      mask = new byte[iconSize];

      System.arraycopy (buffer, ptr + 8, main, 0, iconSize);
      System.arraycopy (buffer, ptr + 8 + iconSize, mask, 0, iconSize);
    }

    public int size ()
    {
      return 8 + iconSize * 2;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Icon type .... %04X%n", iconType));
      text.append (String.format ("Icon size .... %d%n", iconSize));
      text.append (String.format ("Icon height .. %d%n", iconHeight));
      text.append (String.format ("Icon width ... %d%n%n", iconWidth));
      appendIcon (text, main);
      text.append ("\n\n");
      appendIcon (text, mask);

      return text.toString ();
    }

    /*
        Offset  Color   RGB  Mini-Palette
    
        0       Black   000    0
        1       Blue    00F    1
        2       Yellow  FF0    2
        3       White   FFF    3
        4       Black   000    0
        5       Red     D00    1
        6       Green   0E0    2
        7       White   FFF    3
        
        8       Black   000    0
        9       Blue    00F    1
        10      Yellow  FF0    2
        11      White   FFF    3
        12      Black   000    0
        13      Red     D00    1
        14      Green   0E0    2
        15      White   FFF    3
        
    The displayMode word bits are defined as:
    
    Bit 0       selectedIconBit    1 = invert image before copying
    Bit 1       openIconBit        1 = copy light-gray pattern instead of image
    Bit 2       offLineBit         1 = AND light-gray pattern to image being copied
    Bits 3-7    reserved.
    Bits 8-11   foreground color to apply to black part of black & white icons
    Bits 12-15  background color to apply to white part of black & white icons
    
    Bits 0-2 can occur at once and are tested in the order 1-2-0.
    
    "Color is only applied to the black and white icons if bits 15-8 are not all 0.
    Colored pixels in an icon are inverted by black pixels becoming white and any
    other color of pixel becoming black."
    */

    private void appendIcon (StringBuilder text, byte[] buffer)
    {
      int rowBytes = 1 + (iconWidth - 1) / 2;
      for (int i = 0; i < main.length; i += rowBytes)
      {
        for (int ptr = i, max = i + rowBytes; ptr < max; ptr++)
        {
          int left = (byte) ((buffer[ptr] & 0xF0) >> 4);
          int right = (byte) (buffer[ptr] & 0x0F);
          text.append (String.format ("%X %X ", left, right));
        }
        text.append ("\n");
      }
      if (text.length () > 0)
        text.deleteCharAt (text.length () - 1);
    }
  }
}