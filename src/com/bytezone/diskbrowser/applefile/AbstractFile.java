package com.bytezone.diskbrowser.applefile;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.bytezone.diskbrowser.HexFormatter;
import com.bytezone.diskbrowser.gui.DataSource;

public abstract class AbstractFile implements DataSource
{
  public String name;
  public byte[] buffer;
  AssemblerProgram assembler;
  protected BufferedImage image;
  protected List<HexBlock> hexBlocks = new ArrayList<HexBlock> ();

  public AbstractFile (String name, byte[] buffer)
  {
    this.name = name;
    this.buffer = buffer;
  }

  @Override
  public String getText () // Override this to get a tailored text representation
  {
    return "Name : " + name + "\n\nNo text description";
  }

  @Override
  public String getAssembler ()
  {
    if (buffer == null)
      return "No buffer";
    if (assembler == null)
      this.assembler = new AssemblerProgram (name, buffer, 0);
    return assembler.getText ();
  }

  @Override
  public String getHexDump ()
  {
    if (hexBlocks.size () > 0)
    {
      StringBuilder text = new StringBuilder ();

      for (HexBlock hb : hexBlocks)
      {
        if (hb.title != null)
          text.append (hb.title + "\n\n");
        text.append (HexFormatter.format (buffer, hb.ptr, hb.size) + "\n\n");
      }
      text.deleteCharAt (text.length () - 1);
      text.deleteCharAt (text.length () - 1);

      return text.toString ();
    }
    if (buffer == null || buffer.length == 0)
      return "No buffer";
    if (buffer.length <= 99999)
      return HexFormatter.format (buffer, 0, buffer.length);
    return HexFormatter.format (buffer, 0, 99999);
  }

  @Override
  public BufferedImage getImage ()
  {
    return image;
  }

  @Override
  public JComponent getComponent ()
  {
    System.out.println ("In AbstractFile.getComponent()");
    JPanel panel = new JPanel ();
    return panel;
  }

  protected class HexBlock
  {
    public int ptr;
    public int size;
    public String title;

    public HexBlock (int ptr, int size, String title)
    {
      this.ptr = ptr;
      this.size = size;
      this.title = title;
    }
  }
}