package com.bytezone.diskbrowser.gui;

/*****************************************************************************************
 * JPanel used to display a scrolling JTree containing details of a single disk. The JTree
 * consists entirely of AppleFileSource objects. Any number of these objects are contained
 * in Catalog Panel, along with a single FileSystemTab.
 ****************************************************************************************/

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.disk.DiskFactory;
import com.bytezone.diskbrowser.disk.DualDosDisk;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.RedoHandler.RedoEvent;

class AppleDiskTab extends AbstractTab
{
  FormattedDisk disk;

  // restoring from a file selection
  public AppleDiskTab (FormattedDisk disk, DiskAndFileSelector selector,
      RedoHandler redoHandler, Font font, FileSelectedEvent event)
  {
    super (redoHandler, selector, font);
    create (disk);
    redoHandler.fileSelected (event);
  }

  // restoring from a sector selection
  public AppleDiskTab (FormattedDisk disk, DiskAndFileSelector selector,
      RedoHandler redoHandler, Font font, SectorSelectedEvent event)
  {
    super (redoHandler, selector, font);
    create (disk);
    redoHandler.sectorSelected (event);
  }

  // This constructor is only called when lastFileUsed is not null, but the disk
  // couldn't find the file entry. Either the file has been deleted, or it is a disk
  // with redefined files (Wizardry, Infocom etc).
  public AppleDiskTab (FormattedDisk disk, DiskAndFileSelector selector,
      RedoHandler redoHandler, Font font, String lastFileUsed)
  {
    super (redoHandler, selector, font);
    System.out.println ("****************** File not found");
    create (disk);
    //    System.out.println ("ooh - couldn't find the previous file");
    DefaultMutableTreeNode node = findNode (lastFileUsed);
    if (node != null)
    {
      AppleFileSource afs = (AppleFileSource) node.getUserObject ();
      FileSelectedEvent event = new FileSelectedEvent (this, afs);
      redoHandler.fileSelected (event);
    }
  }

  // User is selecting a new disk from the catalog
  public AppleDiskTab (FormattedDisk disk, DiskAndFileSelector selector,
      RedoHandler redoHandler, Font font)
  {
    super (redoHandler, selector, font);
    create (disk);

    // select Catalog
    AppleFileSource afs = (AppleFileSource) findNode (2).getUserObject ();
    if (afs == null)
      afs = (AppleFileSource) findNode (1).getUserObject (); // select Disk
    redoHandler.fileSelected (new FileSelectedEvent (this, afs));
  }

  private void create (FormattedDisk disk)
  {
    this.disk = disk;
    setTree (disk.getCatalogTree ());
    setSelectionListener (tree);
  }

  @Override
  public void activate ()
  {
    //    System.out.println ("=========== Activating AppleDiskTab =============");
    eventHandler.redo = true;
    eventHandler.fireDiskSelectionEvent (disk);
    eventHandler.redo = false;
    tree.setSelectionPath (null);   // turn off any current selection to force an event
    redoHandler.setCurrentData (redoData);
  }

  @Override
  public void refresh ()                  // called when the user gives ALT-R command
  {
    Object o = getSelectedObject ();
    String currentFile = (o == null) ? null : ((AppleFileSource) o).getUniqueName ();
    disk = DiskFactory.createDisk (disk.getAbsolutePath ());
    setTree (disk.getCatalogTree ());
    setSelectionListener (tree);
    selectNode (currentFile);
  }

  private void selectNode (String nodeName)
  {
    DefaultMutableTreeNode selectNode = null;
    if (nodeName != null)
      selectNode = findNode (nodeName);
    if (selectNode == null)
      selectNode = findNode (2);
    if (selectNode != null)
      showNode (selectNode);
    else
      System.out.println ("First node not found");
  }

  void redoEvent (RedoEvent event)
  {
    AppleFileSource afs = ((FileSelectedEvent) event.value).appleFileSource;
    FileSelectedEvent fileSelectedEvent = (FileSelectedEvent) event.value;
    if (fileSelectedEvent.volumeNo >= 0)
    {
      DualDosDisk ddd = (DualDosDisk) afs.getFormattedDisk ().getParent ();
      ddd.setCurrentDiskNo (fileSelectedEvent.volumeNo);
    }
    selectNode (fileSelectedEvent.appleFileSource.getUniqueName ());
  }

  private DefaultMutableTreeNode findNode (String nodeName)
  {
    DefaultMutableTreeNode rootNode = getRootNode ();

    // check for multi-volume disk (only search the current branch)
    FormattedDisk fd = ((AppleFileSource) rootNode.getUserObject ()).getFormattedDisk ();
    if (fd instanceof DualDosDisk)
    {
      int volume = ((DualDosDisk) fd).getCurrentDiskNo ();
      rootNode = (DefaultMutableTreeNode) rootNode.getChildAt (volume);
    }

    Enumeration<TreeNode> children = rootNode.breadthFirstEnumeration ();
    while (children.hasMoreElements ())
    {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement ();
      Object userObject = node.getUserObject ();
      if (userObject instanceof AppleFileSource
          && nodeName.equals (((AppleFileSource) userObject).getUniqueName ()))
        return node;
    }
    return null;
  }

  public boolean contains (FormattedDisk disk)
  {
    return this.disk.getAbsolutePath ().equals (disk.getAbsolutePath ());
  }

  // This action is triggered by AppleDiskTab.selectNode (String), which calls
  // AbstractTab.showNode (DefaultMutableTreeNode). That will trigger this listener
  // ONLY if the value is different, so it is set to null first to force the event.
  private void setSelectionListener (JTree tree)
  {
    tree.addTreeSelectionListener (new TreeSelectionListener ()
    {
      @Override
      public void valueChanged (TreeSelectionEvent e)
      {
        // A null happens when there is a click in the DiskLayoutPanel, in order
        // to turn off the currently selected file
        AppleFileSource afs = (AppleFileSource) getSelectedObject ();
        if (afs != null)
          eventHandler.fireFileSelectionEvent (afs);
      }
    });
  }
}