package com.bytezone.diskbrowser.disk;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.gui.DataSource;

public class DefaultDataSource implements DataSource
{
  public String text;
  byte[] buffer;

  public DefaultDataSource (String text)
  {
    this.text = text;
  }

  public String getAssembler ()
  {
    return null;
  }

  public String getHexDump ()
  {
    if (buffer != null)
      return HexFormatter.format (buffer, 0, buffer.length);
    return null;
  }

  public BufferedImage getImage ()
  {
    return null;
  }

  public String getText ()
  {
    return text;
  }

  public JComponent getComponent ()
  {
    System.out.println ("In DefaultDataSource.getComponent()");
    JPanel panel = new JPanel ();
    return panel;
  }
}