package com.bytezone.diskbrowser.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.DiskLayoutPanel.LayoutDetails;

public class DiskPanel extends JPanel
{
  FormattedDisk formattedDisk;
  LayoutDetails layoutDetails;
  boolean isRetina;
  int blockWidth = 30;            // default
  int blockHeight = 15;            // default
  int width;
  int height;
  int offset;
  int centerOffset;

  Color backgroundColor = new Color (0xE0, 0xE0, 0xE0);

  public void setDisk (FormattedDisk disk, LayoutDetails details)
  {
    formattedDisk = disk;
    layoutDetails = details;

    blockWidth = layoutDetails.block.width;
    blockHeight = layoutDetails.block.height;

    Graphics2D g = (Graphics2D) this.getGraphics ();
    if (g != null)                              // panel might not be showing
      isRetina = g.getFontRenderContext ().getTransform ()
          .equals (AffineTransform.getScaleInstance (2.0, 2.0));

    width = blockWidth - (isRetina ? 2 : 3) + 1;
    height = blockHeight - (isRetina ? 2 : 3) + 1;
    offset = isRetina ? 1 : 2;
    centerOffset = (blockWidth - 4) / 2 + 1;
  }
}
