package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.*;

import com.bytezone.diskbrowser.applefile.Palette;
import com.bytezone.diskbrowser.applefile.PaletteFactory.CycleDirection;

public class NextPaletteAction extends AbstractAction
{
  private final DataPanel owner;
  private final ButtonGroup buttonGroup;

  public NextPaletteAction (DataPanel owner, ButtonGroup buttonGroup)
  {
    super ("Next Palette");
    putValue (Action.SHORT_DESCRIPTION, "Select next color palette");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt N"));
    //    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_N);
    this.owner = owner;
    this.buttonGroup = buttonGroup;
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    Palette palette = owner.cyclePalette (CycleDirection.FORWARDS);
    //    owner.selectPalette (palette);

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