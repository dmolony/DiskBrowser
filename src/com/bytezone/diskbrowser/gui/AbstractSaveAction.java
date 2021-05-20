package com.bytezone.diskbrowser.gui;

import javax.swing.JFileChooser;

import com.bytezone.diskbrowser.utilities.DefaultAction;

// -----------------------------------------------------------------------------------//
public abstract class AbstractSaveAction extends DefaultAction
// -----------------------------------------------------------------------------------//
{
  JFileChooser fileChooser;

  // ---------------------------------------------------------------------------------//
  public AbstractSaveAction (String text, String tip)
  // ---------------------------------------------------------------------------------//
  {
    super (text, tip);
  }

}
