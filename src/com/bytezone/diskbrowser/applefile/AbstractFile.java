package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public abstract class AbstractFile implements DataSource
// -----------------------------------------------------------------------------------//
{
  protected String name;
  public byte[] buffer;
  protected AssemblerProgram assembler;
  protected BufferedImage image;
  protected int loadAddress;

  // ---------------------------------------------------------------------------------//
  public AbstractFile (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.name = name;
    this.buffer = buffer;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()      // Override this to get a tailored text representation
  // ---------------------------------------------------------------------------------//
  {
    return "Name : " + name + "\n\nNo text description";
  }

  // ---------------------------------------------------------------------------------//
  public String getName ()
  // ---------------------------------------------------------------------------------//
  {
    return name;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getAssembler ()
  // ---------------------------------------------------------------------------------//
  {
    if (buffer == null)
      return "No buffer";

    if (assembler == null)
      this.assembler = new AssemblerProgram (name, buffer, loadAddress);

    return assembler.getText ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getHexDump ()
  // ---------------------------------------------------------------------------------//
  {
    if (buffer == null || buffer.length == 0)
      return "No buffer";

    if (buffer.length <= 999999)
      return HexFormatter.format (buffer, 0, buffer.length);

    System.out.println ("**** truncating hex dump");
    return HexFormatter.format (buffer, 0, 999999);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public BufferedImage getImage ()
  // ---------------------------------------------------------------------------------//
  {
    return image;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public JComponent getComponent ()
  // ---------------------------------------------------------------------------------//
  {
    JPanel panel = new JPanel ();
    return panel;
  }
}