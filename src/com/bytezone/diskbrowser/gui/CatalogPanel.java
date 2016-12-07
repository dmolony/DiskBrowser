package com.bytezone.diskbrowser.gui;

/***********************************************************************************************
 * Contains a single instance of FileSystemTab, and any number of AppleDiskTab instances.
 * 
 * 
 ***********************************************************************************************/

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.bytezone.common.FontAction.FontChangeEvent;
import com.bytezone.common.FontAction.FontChangeListener;
import com.bytezone.common.QuitAction.QuitListener;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.catalog.DocumentCreatorFactory;
import com.bytezone.diskbrowser.disk.DualDosDisk;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.gui.RedoHandler.RedoEvent;
import com.bytezone.diskbrowser.gui.RedoHandler.RedoListener;
import com.bytezone.diskbrowser.gui.RootDirectoryAction.RootDirectoryListener;
import com.bytezone.diskbrowser.gui.TreeBuilder.FileNode;

class CatalogPanel extends JTabbedPane implements RedoListener, SectorSelectionListener,
    QuitListener, FontChangeListener, RootDirectoryListener
{
  private static final String prefsLastDiskUsed = "Last disk used";
  private static final String prefsLastDosUsed = "Last dos used";
  private static final String prefsLastFileUsed = "Last file used";
  private static final String prefsLastSectorsUsed = "Last sectors used";
  private static final String prefsRootDirectory = "Root directory";

  private Font font;
  private FileSystemTab fileTab;
  private final List<AppleDiskTab> diskTabs = new ArrayList<AppleDiskTab> ();
  private final DocumentCreatorFactory lister;
  private final DiskAndFileSelector selector = new DiskAndFileSelector ();
  private final RedoHandler redoHandler;
  private CloseTabAction closeTabAction;
  private File rootDirectoryFile;

  public CatalogPanel (MenuHandler mh, RedoHandler redoHandler, Preferences prefs)
  {
    //    String catalogFontName =
    // prefs.get (PreferencesDialog.prefsCatalogFont, PreferencesDialog.defaultFontName);
    //    int catalogFontSize =
    //          prefs.getInt (PreferencesDialog.prefsCatalogFontSize,
    //                        PreferencesDialog.defaultFontSize);
    //    this.font = new Font (catalogFontName, Font.PLAIN, catalogFontSize);
    this.lister = new DocumentCreatorFactory (mh);
    this.redoHandler = redoHandler;

    selector.addDiskSelectionListener (lister.diskLister);

    setTabPlacement (SwingConstants.BOTTOM);
    setPreferredSize (new Dimension (360, 802));          // width, height
    //    setPreferredSize (new Dimension (360, 523)); // width, height

    createTabs (prefs);
    addChangeListener (new TabChangeListener ());
  }

  File getRootDirectory ()
  {
    return rootDirectoryFile;
  }

  private void createTabs (Preferences prefs)
  {
    String rootDirectory = prefs.get (prefsRootDirectory, "");

    rootDirectoryFile = new File (rootDirectory);
    if (!rootDirectoryFile.exists () || !rootDirectoryFile.isDirectory ())
    {
      System.out.println ("No root directory");
      return;
    }

    String lastDiskUsed = prefs.get (prefsLastDiskUsed, "");
    int lastDosUsed = prefs.getInt (prefsLastDosUsed, -1);
    String lastFileUsed = prefs.get (prefsLastFileUsed, "");
    String lastSectorsUsed = prefs.get (prefsLastSectorsUsed, "");

    if (false)
    {
      System.out.println ("Last disk    : " + lastDiskUsed);
      System.out.println ("Last dos     : " + lastDosUsed);
      System.out.println ("Last file    : " + lastFileUsed);
      System.out.println ("Last sectors : " + lastSectorsUsed);
    }

    DiskSelectedEvent diskEvent = null;
    if (!lastDiskUsed.isEmpty ())
    {
      diskEvent = DiskSelectedEvent.create (this, lastDiskUsed);
      if (diskEvent != null)
      {
        FormattedDisk fd = diskEvent.getFormattedDisk ();
        if (lastDosUsed >= 0 && fd instanceof DualDosDisk)
          ((DualDosDisk) fd).setCurrentDiskNo (lastDosUsed);
      }
    }
    else
      System.out.println ("no disk selected");

    insertFileSystemTab (rootDirectoryFile, diskEvent);

    if (diskEvent != null)
    {
      AppleDiskTab tab = null;
      FormattedDisk fd = diskEvent.getFormattedDisk ();

      if (!lastFileUsed.isEmpty ())
      {
        AppleFileSource afs = fd.getFile (lastFileUsed);
        if (afs != null)
        {
          FileSelectedEvent fileEvent = FileSelectedEvent.create (this, afs);
          tab = new AppleDiskTab (fd, selector, redoHandler, font, fileEvent);
        }
        else
          tab = new AppleDiskTab (fd, selector, redoHandler, font, lastFileUsed);
      }
      else if (!lastSectorsUsed.isEmpty ())
      {
        SectorSelectedEvent sectorEvent =
            SectorSelectedEvent.create (this, fd, lastSectorsUsed);
        tab = new AppleDiskTab (fd, selector, redoHandler, font, sectorEvent);
      }
      else
        tab = new AppleDiskTab (fd, selector, redoHandler, font);

      if (tab != null)
      {
        diskTabs.add (tab);
        add (tab, "D" + diskTabs.size ());
      }
      else
        System.out.println ("No disk tab created");
    }
  }

  @Override
  public void rootDirectoryChanged (File root)
  {
    // is the user replacing an existing root folder?
    if (fileTab != null)
      removeTabAt (0);

    insertFileSystemTab (root, null);
    setSelectedIndex (0);
  }

  private void insertFileSystemTab (File root, DiskSelectedEvent diskEvent)
  {
    rootDirectoryFile = root;
    fileTab =
        new FileSystemTab (rootDirectoryFile, selector, redoHandler, font, diskEvent);
    fileTab.addTreeMouseListener (new MouseListener ());    // listen for disk selection
    lister.catalogLister.setNode (fileTab.getRootNode ());
    insertTab ("Disk Tree", null, fileTab, "Display Apple disks", 0);
  }

  public void activate ()
  {
    if (fileTab == null)
    {
      System.out.println ("No file tab");
      return;
    }

    if (diskTabs.size () > 0)
      setSelectedIndex (1);
    else if (fileTab != null)
      setSelectedIndex (0);
  }

  //  void setDuplicateAction (DuplicateAction action)
  //  {
  //    this.duplicateAction = action;
  //    if (fileTab != null && fileTab.rootFolder != null)
  //      action.setDuplicates (fileTab.rootFolder, fileTab.duplicateDisks);
  //  }

  void setCloseTabAction (CloseTabAction action)
  {
    this.closeTabAction = action;
  }

  // called after a double-click in the fileTab
  public void addDiskPanel (FormattedDisk disk, String lastFileUsed, boolean activate)
  {
    int tabNo = 1;
    for (AppleDiskTab tab : diskTabs)
    {
      if (tab.contains (disk))
      {
        setSelectedIndex (tabNo);
        return;
      }
      tabNo++;
    }

    AppleDiskTab tab = new AppleDiskTab (disk, selector, redoHandler, font);
    diskTabs.add (tab);
    add (tab, "D" + diskTabs.size ());
    if (activate)
      setSelectedIndex (diskTabs.size ());
  }

  // Called from RefreshTreeAction
  public void refreshTree ()
  {
    Tab tab = (Tab) getSelectedComponent ();
    tab.refresh ();

    // Any newly created disk needs to appear in the FileSystemTab's tree
    if (tab instanceof AppleDiskTab)
      fileTab.replaceDisk (((AppleDiskTab) tab).disk);
  }

  // Called from CloseTabAction
  public void closeCurrentTab ()
  {
    Tab tab = (Tab) getSelectedComponent ();
    if (!(tab instanceof AppleDiskTab) || diskTabs.size () < 2)
      return;

    int index = getSelectedIndex ();
    remove (index);
    diskTabs.remove (tab);

    for (int i = 1; i <= diskTabs.size (); i++)
      setTitleAt (i, "D" + i);

    checkCloseTabAction ();
  }

  private void checkCloseTabAction ()
  {
    Tab tab = (Tab) getSelectedComponent ();
    if (diskTabs.size () > 1 && tab instanceof AppleDiskTab)
      closeTabAction.setEnabled (true);
    else
      closeTabAction.setEnabled (false);
  }

  @Override
  public void quit (Preferences prefs)
  {
    if (fileTab == null)
    {
      prefs.put (prefsRootDirectory, "");
      prefs.put (prefsLastDiskUsed, "");
      prefs.putInt (prefsLastDosUsed, -1);
      prefs.put (prefsLastFileUsed, "");
      prefs.put (prefsLastSectorsUsed, "");
    }
    else
    {
      prefs.put (prefsRootDirectory, fileTab.rootFolder.getAbsolutePath ());

      if (diskTabs.size () == 0)
      {
        RedoEvent redoEvent = fileTab.redoData.getCurrentEvent ();
        if (redoEvent != null)
        {
          DiskSelectedEvent event = (DiskSelectedEvent) redoEvent.value;
          prefs.put (prefsLastDiskUsed, event.getFormattedDisk ().getAbsolutePath ());
        }
        prefs.put (prefsLastFileUsed, "");
        prefs.put (prefsLastSectorsUsed, "");
      }
      else
      {
        AbstractTab selectedTab = (AbstractTab) getSelectedComponent ();
        if (selectedTab instanceof FileSystemTab)
          selectedTab = diskTabs.get (diskTabs.size () - 1);

        FormattedDisk fd = ((AppleDiskTab) selectedTab).disk;
        prefs.put (prefsLastDiskUsed, fd.getAbsolutePath ());
        if (fd instanceof DualDosDisk)
          prefs.putInt (prefsLastDosUsed, ((DualDosDisk) fd).getCurrentDiskNo ());
        else
          prefs.putInt (prefsLastDosUsed, -1);

        RedoEvent redoEvent = selectedTab.redoData.getCurrentEvent ();
        if (redoEvent != null)
        {
          EventObject event = redoEvent.value;

          if (event instanceof FileSelectedEvent)
          {
            AppleFileSource afs = ((FileSelectedEvent) event).file;
            prefs.put (prefsLastFileUsed, afs == null ? "" : afs.getUniqueName ());
            prefs.put (prefsLastSectorsUsed, "");
          }
          else if (event instanceof SectorSelectedEvent)
          {
            prefs.put (prefsLastFileUsed, "");
            prefs.put (prefsLastSectorsUsed, ((SectorSelectedEvent) event).toText ());
          }
        }
      }
    }
  }

  @Override
  public void restore (Preferences preferences)
  {
  }

  // Pass through to DiskSelector
  public void addDiskSelectionListener (DiskSelectionListener listener)
  {
    selector.addDiskSelectionListener (listener);
  }

  // Pass through to DiskSelector
  public void addFileSelectionListener (FileSelectionListener listener)
  {
    selector.addFileSelectionListener (listener);
  }

  // Pass through to DiskSelector
  public void addFileNodeSelectionListener (FileNodeSelectionListener listener)
  {
    selector.addFileNodeSelectionListener (listener);
  }

  private class TabChangeListener implements ChangeListener
  {
    @Override
    public void stateChanged (ChangeEvent e)
    {
      Tab tab = (Tab) getSelectedComponent ();
      if (tab != null)
      {
        tab.activate ();
        checkCloseTabAction ();
      }
    }
  }

  @Override
  public void redo (RedoEvent event)
  {
    Tab tab = (Tab) getSelectedComponent ();
    selector.redo = true;

    if (event.type.equals ("DiskEvent"))
    {
      if (tab instanceof FileSystemTab)
        ((FileSystemTab) tab).redoEvent (event);
    }
    else if (event.type.equals ("FileEvent"))
    {
      if (tab instanceof AppleDiskTab)
        ((AppleDiskTab) tab).redoEvent (event);
    }
    else if (event.type.equals ("FileNodeEvent"))
    {
      if (tab instanceof FileSystemTab)
        ((FileSystemTab) tab).redoEvent (event);
    }
    else if (event.type.equals ("SectorEvent"))
    {
      // don't care
    }
    else
      System.out.println ("Unknown event type : " + event.type);

    selector.redo = false;
  }

  @Override
  public void sectorSelected (SectorSelectedEvent event)
  {
    // user has clicked in the DiskLayoutPanel, so turn off any current file selection
    Tab tab = (Tab) getSelectedComponent ();
    if (tab instanceof AppleDiskTab)
      ((AppleDiskTab) tab).tree.setSelectionPath (null);
  }

  //  @Override
  //  public void preferenceChange (PreferenceChangeEvent evt)
  //  {
  //    if (evt.getKey ().equals (PreferencesDialog.prefsCatalogFont))
  //      font = new Font (evt.getNewValue (), Font.PLAIN, font.getSize ());
  //    if (evt.getKey ().equals (PreferencesDialog.prefsCatalogFontSize))
  //      font = new Font (font.getFontName (), 
  //    Font.PLAIN, Integer.parseInt (evt.getNewValue ()));
  //    if (fileTab != null)
  //      fileTab.setTreeFont (font);
  //    for (AppleDiskTab tab : diskTabs)
  //      tab.setTreeFont (font);
  //  }

  @Override
  public void changeFont (FontChangeEvent fontChangeEvent)
  {
    font = fontChangeEvent.font;
    if (fileTab != null)
      fileTab.setTreeFont (font);
    for (AppleDiskTab tab : diskTabs)
      tab.setTreeFont (font);
  }

  private class MouseListener extends MouseAdapter
  {
    @Override
    public void mousePressed (MouseEvent e)
    {
      JTree tree = (JTree) e.getSource ();
      int selRow = tree.getRowForLocation (e.getX (), e.getY ());
      if (selRow < 0)
        return;

      TreePath tp = tree.getPathForLocation (e.getX (), e.getY ());
      DefaultMutableTreeNode selectedNode =
          (DefaultMutableTreeNode) tp.getLastPathComponent ();
      FileNode node = (FileNode) selectedNode.getUserObject ();
      if (node.file.isDirectory ())
        lister.catalogLister.setNode (selectedNode);
      else if (e.getClickCount () == 2)
        addDiskPanel (node.getFormattedDisk (), null, true);
    }
  }
}