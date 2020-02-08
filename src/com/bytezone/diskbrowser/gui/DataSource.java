package com.bytezone.diskbrowser.gui;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

// -----------------------------------------------------------------------------------//
public interface DataSource
// -----------------------------------------------------------------------------------//
{
  public String getText ();

  public String getAssembler ();

  public String getHexDump ();

  public BufferedImage getImage ();

  public JComponent getComponent ();
}