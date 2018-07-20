package com.bytezone.diskbrowser.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

import com.bytezone.common.Platform;
import com.bytezone.common.Platform.FontSize;
import com.bytezone.common.Platform.FontType;
import com.bytezone.diskbrowser.gui.DiskLayoutPanel.LayoutDetails;

class ScrollRuler extends JComponent
{
  // dimensions of the ruler
  public static final int HEIGHT = 20;
  public static final int WIDTH = 40;

  public static final int HORIZONTAL = 0;
  public static final int VERTICAL = 1;

  private final Font font = Platform.getFont (FontType.SANS_SERIF, FontSize.BASE);
  private final int orientation;
  private boolean isHex = true;
  private boolean isTrackMode = true;
  private LayoutDetails layoutDetails;
  private final JComponent image;

  public ScrollRuler (JComponent image, int orientation)
  {
    this.orientation = orientation;
    this.image = image;

    // set defaults until setLayout is called
    if (orientation == HORIZONTAL)
      setPreferredSize (new Dimension (0, HEIGHT)); // width/height
    else
      setPreferredSize (new Dimension (WIDTH, 0));
  }

  public void setLayout (LayoutDetails layout)
  {
    this.layoutDetails = layout;

    // Must match the preferred size of DiskLayoutImage
    if (orientation == HORIZONTAL)
      setPreferredSize (
          new Dimension (layout.block.width * layout.grid.width + 1, HEIGHT));
    else
      setPreferredSize (
          new Dimension (WIDTH, layout.block.height * layout.grid.height + 1));

    setTrackMode (layout.grid.width == 16 || layout.grid.width == 13);
  }

  public void setTrackMode (boolean trackMode)
  {
    isTrackMode = trackMode;
    repaint ();
  }

  public void setHex (boolean hex)
  {
    isHex = hex;
    repaint ();
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    Rectangle clipRect = g.getClipBounds ();
    g.setColor (Color.WHITE);
    g.fillRect (clipRect.x, clipRect.y, clipRect.width, clipRect.height);

    if (layoutDetails == null)
      return;

    g.setFont (font);                 // how do I do this in the constructor?
    g.setColor (Color.black);

    if (orientation == HORIZONTAL)
      drawHorizontal (g, clipRect, layoutDetails.block.width);
    else
      drawVertical (g, clipRect, layoutDetails.block.height);
  }

  private void drawHorizontal (Graphics g, Rectangle clipRect, int width)
  {
    int start = (clipRect.x / width);
    int end = start + clipRect.width / width;
    end = Math.min (end, image.getWidth () / width - 1);

    String format;
    int offset;

    if (layoutDetails.block.width <= 15)
    {
      format = isHex ? "%1X" : "%1d";
      offset = isHex ? 4 : 0;
    }
    else
    {
      format = isHex ? "%02X" : "%02d";
      offset = 7;
    }

    for (int i = start; i <= end; i++)
      g.drawString (String.format (format, i), i * width + offset, 15);
  }

  private void drawVertical (Graphics g, Rectangle clipRect, int height)
  {
    int start = (clipRect.y / height);
    int end = start + clipRect.height / height;
    end = Math.min (end, image.getHeight () / height - 1);

    String format = isHex ? "%04X" : "%04d";

    for (int i = start; i <= end; i++)
    {
      int value = isTrackMode ? i : i * layoutDetails.grid.width;
      g.drawString (String.format (format, value), 4, i * height + 13);
    }
  }
}