package com.bytezone.diskbrowser.prodos;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;

// see Prodos 8 Tech note #25
// -----------------------------------------------------------------------------------//
class ProdosExtendedKeySector extends AbstractSector
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  ProdosExtendedKeySector (Disk disk, byte[] buffer, DiskAddress diskAddress)
  // ---------------------------------------------------------------------------------//
  {
    super (disk, buffer, diskAddress);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String createText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = getHeader ("Prodos Extended Key Block");

    for (int i = 0; i < 512; i += 256)
    {
      String type = i == 0 ? "Data" : "Resource";
      addText (text, buffer, i, 1,
          type + " fork storage type (" + getType (buffer[i]) + ")");
      addTextAndDecimal (text, buffer, i + 1, 2, "Key block");
      addTextAndDecimal (text, buffer, i + 3, 2, "Blocks used");
      addTextAndDecimal (text, buffer, i + 5, 3, "EOF");
      text.append ("\n");

      // check for Finder Info records
      if (i == 0 && buffer[8] != 0)
      {
        for (int j = 0; j <= 18; j += 18)
        {
          addTextAndDecimal (text, buffer, j + 8, 1, "Size");
          addTextAndDecimal (text, buffer, j + 9, 1, "Type");
          addTextAndDecimal (text, buffer, j + 10, 16, "Finder info");
        }
      }
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String getType (byte flag)
  // ---------------------------------------------------------------------------------//
  {
    switch ((flag & 0x0F))
    {
      case ProdosConstants.SEEDLING:
        return "Seedling";
      case ProdosConstants.SAPLING:
        return "Sapling";
      case ProdosConstants.TREE:
        return "Tree";
      default:
        return "???";
    }
  }
}