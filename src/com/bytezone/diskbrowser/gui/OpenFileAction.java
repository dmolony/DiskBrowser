package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.bytezone.common.DefaultAction;
import com.bytezone.diskbrowser.disk.DiskFactory;

// I don't think this is needed anymore
class OpenFileAction extends DefaultAction
{
  //  DiskBrowser owner;
  CatalogPanel catalogPanel;

  public OpenFileAction (DiskBrowser owner, CatalogPanel catalogPanel)
  {
    super ("Open disk...", "Opens a single disk image", "/com/bytezone/diskbrowser/icons/");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("control O"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_O);
    //    this.owner = owner;
    this.catalogPanel = catalogPanel;

    setIcon (Action.SMALL_ICON, "Open16.gif");
  }

  public void actionPerformed (ActionEvent e)
  {
    JFileChooser chooser = new JFileChooser ("C:/");
    chooser.setDialogTitle ("Select disk image");
    FileNameExtensionFilter filter = new FileNameExtensionFilter ("DSK & PO Images", "dsk", "po");
    chooser.setFileFilter (filter);
    //    if (owner.selectedDisk != null)
    //      chooser.setSelectedFile (owner.selectedDisk.getDisk ().getFile ());
    int result = chooser.showOpenDialog (null);
    if (result == JFileChooser.APPROVE_OPTION)
    {
      File file = chooser.getSelectedFile ();
      if (file != null)
        catalogPanel.addDiskPanel (DiskFactory.createDisk (file.getAbsolutePath ()), null, true);
    }
  }
}