package com.bytezone.diskbrowser.gui;

import java.util.EventObject;

import com.bytezone.diskbrowser.gui.TreeBuilder.FileNode;

// -----------------------------------------------------------------------------------//
public class FileNodeSelectedEvent extends EventObject
// -----------------------------------------------------------------------------------//
{
  private final FileNode node;
  boolean redo;

  // ---------------------------------------------------------------------------------//
  public FileNodeSelectedEvent (Object source, FileNode node)
  // ---------------------------------------------------------------------------------//
  {
    super (source);
    this.node = node;
  }

  // ---------------------------------------------------------------------------------//
  public FileNode getFileNode ()
  // ---------------------------------------------------------------------------------//
  {
    return node;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return node.file.getAbsolutePath ();
  }

  // ---------------------------------------------------------------------------------//
  public String toText ()
  // ---------------------------------------------------------------------------------//
  {
    return node.file.getAbsolutePath ();
  }
}