package com.bytezone.diskbrowser.prodos;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;

class ProdosExtendedKeySector extends AbstractSector
{

  public ProdosExtendedKeySector (Disk disk, byte[] buffer)
  {
    super (disk, buffer);
  }

  @Override
  public String createText ()
  {
    StringBuilder text = getHeader ("Prodos Extended Key Block");

    for (int i = 0; i < 512; i += 256)
    {
      addText (text, buffer, i, 1, "Storage type (" + getType (buffer[i]) + ")");
      addTextAndDecimal (text, buffer, i + 1, 2, "Key block");
      addTextAndDecimal (text, buffer, i + 3, 2, "Blocks used");
      addTextAndDecimal (text, buffer, i + 5, 3, "EOF");
      text.append ("\n");
    }

    return text.toString ();
  }

  private String getType (byte flag)
  {
    switch ((flag & 0x0F))
    {
      case ProdosConstants.TYPE_SEEDLING:
        return "Seedling";
      case ProdosConstants.TYPE_SAPLING:
        return "Sapling";
      case ProdosConstants.TYPE_TREE:
        return "Tree";
      default:
        return "???";
    }
  }
}