package com.bytezone.diskbrowser.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import com.bytezone.common.Platform;
import com.bytezone.common.Platform.FontSize;
import com.bytezone.common.Platform.FontType;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.disk.SectorType;
import com.bytezone.diskbrowser.gui.DiskLayoutPanel.LayoutDetails;

class DiskLegendPanel extends DiskPanel
{
  private static final int LEFT = 3;
  private static final int TOP = 10;

  private final Font font;

  public DiskLegendPanel ()
  {
    font = Platform.getFont (FontType.SANS_SERIF, FontSize.BASE);
    setBackground (Color.WHITE);
  }

  @Override
  public void setDisk (FormattedDisk disk, LayoutDetails details)
  {
    super.setDisk (disk, details);

    repaint ();
  }

  @Override
  public Dimension getPreferredSize ()
  {
    return new Dimension (0, 160);            // width/height
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    super.paintComponent (g);

    if (formattedDisk == null)
      return;

    g.setFont (font);

    int count = 0;
    int lineHeight = 20;

    for (SectorType type : formattedDisk.getSectorTypeList ())
    {
      int x = LEFT + (count % 2 == 0 ? 0 : 155);
      int y = TOP + count / 2 * lineHeight;
      ++count;

      // draw border
      g.setColor (backgroundColor);
      g.drawRect (x + 1, y + 1, blockWidth - 1, blockHeight - 1);

      // draw block
      g.setColor (type.colour);
      g.fillRect (x + 1, y + 1, blockWidth - 1, blockHeight - 1);

      // draw text
      g.setColor (Color.BLACK);
      g.drawString (type.name, x + blockWidth + 4, y + 12);
    }

    int y = ++count / 2 * lineHeight + TOP * 2 + 5;
    int val = formattedDisk.falseNegativeBlocks ();
    if (val > 0)
    {
      g.drawString (
          val + " unused sector" + (val == 1 ? "" : "s") + " marked as unavailable", 10,
          y);
      y += lineHeight;
    }
    val = formattedDisk.falsePositiveBlocks ();
    if (val > 0)
      g.drawString (val + " used sector" + (val == 1 ? "" : "s") + " marked as available",
          10, y);
  }
}