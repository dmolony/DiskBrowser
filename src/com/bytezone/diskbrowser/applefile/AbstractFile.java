package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.bytezone.diskbrowser.gui.DataSource;
import com.bytezone.diskbrowser.prodos.ResourceFork;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
public abstract class AbstractFile implements DataSource
// -----------------------------------------------------------------------------------//
{
  static boolean showDebugText;

  protected String name;
  public byte[] buffer;
  protected AssemblerProgram assembler;
  protected BufferedImage image;
  protected int loadAddress;
  ResourceFork resourceFork;

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
  @Override
  public byte[] getBuffer ()
  // ---------------------------------------------------------------------------------//
  {
    return buffer;
  }

  // ---------------------------------------------------------------------------------//
  public static void setDefaultDebug (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    showDebugText = value;
  }

  // ---------------------------------------------------------------------------------//
  public void setResourceFork (ResourceFork resourceFork)
  // ---------------------------------------------------------------------------------//
  {
    this.resourceFork = resourceFork;
  }

  // ---------------------------------------------------------------------------------//
  public static void setDebug (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    showDebugText = value;
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
  String getHeader ()
  // ---------------------------------------------------------------------------------//
  {
    return "Name : " + name + "\n\n";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public JComponent getComponent ()
  // ---------------------------------------------------------------------------------//
  {
    return new JPanel ();
  }
}