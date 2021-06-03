package com.bytezone.diskbrowser.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

// -----------------------------------------------------------------------------------//
public class ImagePanel extends JPanel
// -----------------------------------------------------------------------------------//
{
  private static final int BACKGROUND = 245;

  private BufferedImage image;
  private double scale = 1;
  private double userScale = .5;

  // ---------------------------------------------------------------------------------//
  public ImagePanel ()
  // ---------------------------------------------------------------------------------//
  {
    this.setBackground (new Color (BACKGROUND, BACKGROUND, BACKGROUND));
  }

  // ---------------------------------------------------------------------------------//
  void setScale (double scale)
  // ---------------------------------------------------------------------------------//
  {
    this.userScale = scale;
  }

  // ---------------------------------------------------------------------------------//
  void setImage (BufferedImage image)
  // ---------------------------------------------------------------------------------//
  {
    this.image = image;
    int width, height;

    if (image != null)
    {
      Graphics2D g2 = image.createGraphics ();
      g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      width = image.getWidth ();
      height = image.getHeight ();
    }
    else
      width = height = 0;

    if (true)
    {
      if (width < 400 && width > 0)
        scale = (400 - 1) / width + 1;
      else
        scale = 1;
      if (scale > 4)
        scale = 4;
    }

    scale *= userScale;

    setPreferredSize (new Dimension ((int) (width * scale), (int) (height * scale)));
    repaint ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void paintComponent (Graphics g)
  // ---------------------------------------------------------------------------------//
  {
    super.paintComponent (g);

    if (image != null)
    {
      Graphics2D g2 = ((Graphics2D) g);
      g2.transform (AffineTransform.getScaleInstance (scale, scale));
      g2.drawImage (image, (int) ((getWidth () - image.getWidth () * scale) / 2 / scale),
          4, this);
    }
  }
}