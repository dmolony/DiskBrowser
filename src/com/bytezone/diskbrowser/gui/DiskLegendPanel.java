package com.bytezone.diskbrowser.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import com.bytezone.common.Platform;
import com.bytezone.common.Platform.FontSize;
import com.bytezone.common.Platform.FontType;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.disk.SectorType;
import com.bytezone.diskbrowser.gui.DiskLayoutPanel.LayoutDetails;

class DiskLegendPanel extends JPanel
{
  private static final int LEFT = 10;
  private static final int TOP = 10;

  private FormattedDisk disk;
  private LayoutDetails layoutDetails;
  private final Font font;
  private boolean retinaTest;

  public DiskLegendPanel ()
  {
    font = Platform.getFont (FontType.SANS_SERIF, FontSize.BASE);
    setBackground (Color.WHITE);
  }

  public void setDisk (FormattedDisk disk, LayoutDetails details)
  {
    this.disk = disk;
    layoutDetails = details;

    Graphics2D g = (Graphics2D) this.getGraphics ();
    retinaTest = g.getFontRenderContext ().getTransform ()
        .equals (AffineTransform.getScaleInstance (2.0, 2.0));

    repaint ();
  }

  @Override
  public Dimension getPreferredSize ()
  {
    return new Dimension (0, 160); // width/height
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    super.paintComponent (g);

    if (disk == null)
      return;

    g.setFont (font);

    int count = 0;
    int lineHeight = 20;
    int width = layoutDetails.block.width - (retinaTest ? 2 : 3);
    int height = layoutDetails.block.height - (retinaTest ? 2 : 3);
    int offset = retinaTest ? 1 : 2;

    for (SectorType type : disk.getSectorTypeList ())
    {
      int x = LEFT + (count % 2 == 0 ? 0 : 145);
      int y = TOP + count++ / 2 * lineHeight;

      // draw border
      g.setColor (Color.GRAY);
      g.drawRect (x, y, layoutDetails.block.width, layoutDetails.block.height);

      // draw the colour
      g.setColor (type.colour);
      g.fillRect (x + offset, y + offset, width, height);

      // draw the text
      g.setColor (Color.BLACK);
      g.drawString (type.name, x + layoutDetails.block.width + 4, y + 12);
    }

    int y = ++count / 2 * lineHeight + TOP * 2 + 5;
    int val = disk.falseNegativeBlocks ();
    if (val > 0)
    {
      g.drawString (val + " empty sector" + (val == 1 ? "" : "s")
          + " marked as unavailable", 10, y);
      y += lineHeight;
    }
    val = disk.falsePositiveBlocks ();
    if (val > 0)
      g.drawString (val + " used sector" + (val == 1 ? "" : "s") + " marked as available",
                    10, y);
  }
}