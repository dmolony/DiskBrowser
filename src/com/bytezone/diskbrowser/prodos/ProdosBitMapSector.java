package com.bytezone.diskbrowser.prodos;

import java.awt.Dimension;

import com.bytezone.diskbrowser.disk.AbstractSector;
import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;

// -----------------------------------------------------------------------------------//
class ProdosBitMapSector extends AbstractSector
// -----------------------------------------------------------------------------------//
{
  private final ProdosDisk parent;

  // ---------------------------------------------------------------------------------//
  ProdosBitMapSector (ProdosDisk parent, Disk disk, byte[] buffer, DiskAddress da)
  // ---------------------------------------------------------------------------------//
  {
    super (disk, buffer, da);
    this.parent = parent;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String createText ()
  // ---------------------------------------------------------------------------------//
  {
    Dimension grid = parent.getGridLayout ();

    // check range of bits for current block - so far I don't have a disk that needs
    // more than a single block
    int relativeBlock =
        diskAddress.getBlockNo () - parent.getVolumeDirectoryHeader ().bitMapBlock;
    int startBit = relativeBlock * 4096;
    //    int endBit = startBit + 4096;
    if (startBit >= grid.width * grid.height)
      return "This sector is not used - the physical file size makes it unnecessary";

    int width = (grid.width - 1) / 8 + 1;   // must be 1-4

    StringBuilder text = getHeader ("Volume Bit Map Block");

    if (false)
    {
      int block = 0;
      for (int row = 0; row < grid.height; row++)
      {
        int offset = block / 8;
        StringBuilder details = new StringBuilder ();
        for (int col = 0; col < grid.width; col++)
          details.append (parent.isSectorFree (block++) ? ". " : "X ");
        addText (text, buffer, offset, width, details.toString ());
      }
    }
    else
    {
      int startRow = relativeBlock * 512 / width;
      int endRow = startRow + (512 / width);
      int block = startBit;
      int byteNo = 0;
      //      System.out.printf ("Start %d, end %d%n", startRow, endRow);
      for (int row = startRow; row < endRow; row++)
      {
        StringBuilder details = new StringBuilder ();
        for (int col = 0; col < grid.width; col++)
          details.append (parent.isSectorFree (block++) ? ". " : "X ");
        addText (text, buffer, byteNo, width, details.toString ());
        byteNo += width;
      }
    }

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }
}