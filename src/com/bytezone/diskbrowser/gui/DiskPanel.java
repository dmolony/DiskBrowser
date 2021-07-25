package com.bytezone.diskbrowser.gui;

import java.awt.Color;

import javax.swing.JPanel;

import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.DiskLayoutPanel.LayoutDetails;

// -----------------------------------------------------------------------------------//
public class DiskPanel extends JPanel
// -----------------------------------------------------------------------------------//
{
  FormattedDisk formattedDisk;
  LayoutDetails layoutDetails;

  int blockWidth = 30;              // default
  int blockHeight = 15;             // default
  int centerOffset;

  Color backgroundColor = new Color (0xE0, 0xE0, 0xE0);

  // ---------------------------------------------------------------------------------//
  public void setDisk (FormattedDisk formattedDisk, LayoutDetails layoutDetails)
  // ---------------------------------------------------------------------------------//
  {
    this.formattedDisk = formattedDisk;
    this.layoutDetails = layoutDetails;

    blockWidth = layoutDetails.block.width;
    blockHeight = layoutDetails.block.height;
    centerOffset = (blockWidth - 4) / 2 + 1;
  }
}
