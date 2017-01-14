package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.*;

import com.bytezone.diskbrowser.applefile.Palette;
import com.bytezone.diskbrowser.applefile.PaletteFactory.CycleDirection;

public class PreviousPaletteAction extends AbstractAction
{
  private final DataPanel owner;
  private final ButtonGroup buttonGroup;

  public PreviousPaletteAction (DataPanel owner, ButtonGroup buttonGroup)
  {
    super ("Previous Palette");
    putValue (Action.SHORT_DESCRIPTION, "Select previous color palette");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt P"));
    //    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_P);
    this.owner = owner;
    this.buttonGroup = buttonGroup;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    Palette palette = owner.cyclePalette (CycleDirection.BACKWARDS);
    owner.selectPalette (palette);

    if (palette != null)
    {
      Enumeration<AbstractButton> enumeration = buttonGroup.getElements ();
      while (enumeration.hasMoreElements ())
      {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem) enumeration.nextElement ();
        if (item.getText ().equals (palette.getName ()))
        {
          item.setSelected (true);
          break;
        }
      }
    }
  }
}