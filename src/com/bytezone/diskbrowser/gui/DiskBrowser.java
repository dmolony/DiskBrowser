package com.bytezone.diskbrowser.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.util.prefs.Preferences;

import javax.swing.*;

import com.bytezone.common.Platform;
import com.bytezone.common.QuitAction;
import com.bytezone.common.QuitAction.QuitListener;
import com.bytezone.common.State;

public class DiskBrowser extends JFrame implements DiskSelectionListener, QuitListener
{
  private static final String windowTitle = "Apple ][ Disk Browser";
  private static final String PREFS_FULL_SCREEN = "full screen";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private WindowSaver windowSaver;

  public DiskBrowser ()
  {
    super (windowTitle);

    State state = new State (prefs);

    if (false)
      state.clear ();

    JToolBar toolBar = new JToolBar ("Toolbar", JToolBar.HORIZONTAL);
    MenuHandler menuHandler = new MenuHandler (prefs);

    setJMenuBar (menuHandler.menuBar);
    setLayout (new BorderLayout ());
    add (toolBar, BorderLayout.NORTH);

    RedoHandler redoHandler = new RedoHandler (getRootPane (), toolBar); // add nav buttons
    toolBar.addSeparator ();

    // create and add the left-hand catalog panel
    CatalogPanel catalogPanel = new CatalogPanel (menuHandler, redoHandler, prefs);
    JPanel catalogBorderPanel = addPanel (catalogPanel, "Catalog", BorderLayout.WEST);

    // create and add the centre output panel
    DataPanel dataPanel = new DataPanel (menuHandler, prefs);
    addPanel (dataPanel, "Output", BorderLayout.CENTER);

    // create and add the right-hand disk layout panel
    DiskLayoutPanel diskLayoutPanel = new DiskLayoutPanel ();
    JPanel layoutBorderPanel =
        addPanel (diskLayoutPanel, "Disk layout", BorderLayout.EAST);

    // create actions
    RootDirectoryAction rootDirectoryAction =
        new RootDirectoryAction (null, catalogPanel);
    RefreshTreeAction refreshTreeAction = new RefreshTreeAction (catalogPanel);
    //    PreferencesAction preferencesAction = new PreferencesAction (this, prefs);
    AbstractAction print = new PrintAction (dataPanel);
    AboutAction aboutAction = new AboutAction ();
    HideCatalogAction hideCatalogAction =
        new HideCatalogAction (this, catalogBorderPanel);
    HideLayoutAction hideLayoutAction = new HideLayoutAction (this, layoutBorderPanel);
    ShowFreeSectorsAction showFreeAction =
        new ShowFreeSectorsAction (menuHandler, diskLayoutPanel);
    DuplicateAction duplicateAction = new DuplicateAction ();
    CloseTabAction closeTabAction = new CloseTabAction (catalogPanel);

    // add action buttons to toolbar
    toolBar.add (rootDirectoryAction);
    toolBar.add (refreshTreeAction);
    //    toolBar.add (preferencesAction);
    toolBar.add (duplicateAction);
    toolBar.add (print);
    toolBar.add (aboutAction);

    // set the listeners
    catalogPanel.addDiskSelectionListener (this);
    catalogPanel.addDiskSelectionListener (dataPanel);
    catalogPanel.addDiskSelectionListener (diskLayoutPanel);
    catalogPanel.addDiskSelectionListener (redoHandler);
    catalogPanel.addDiskSelectionListener (menuHandler);

    catalogPanel.addFileSelectionListener (dataPanel);
    catalogPanel.addFileSelectionListener (diskLayoutPanel);
    catalogPanel.addFileSelectionListener (redoHandler);
    catalogPanel.addFileSelectionListener (menuHandler);

    catalogPanel.addFileNodeSelectionListener (dataPanel);
    catalogPanel.addFileNodeSelectionListener (redoHandler);

    diskLayoutPanel.addSectorSelectionListener (dataPanel);
    diskLayoutPanel.addSectorSelectionListener (redoHandler);
    diskLayoutPanel.addSectorSelectionListener (catalogPanel);

    redoHandler.addRedoListener (catalogPanel);
    redoHandler.addRedoListener (diskLayoutPanel);

    menuHandler.fontAction.addFontChangeListener (dataPanel);
    menuHandler.fontAction.addFontChangeListener (catalogPanel);
    menuHandler.fontAction.addFontChangeListener (diskLayoutPanel);

    // set the MenuItem Actions
    menuHandler.printItem.setAction (print);
    //    menuHandler.addHelpMenuAction (preferencesAction, "prefs");
    menuHandler.addHelpMenuAction (aboutAction, "about");
    menuHandler.refreshTreeItem.setAction (refreshTreeAction);
    menuHandler.rootItem.setAction (rootDirectoryAction);
    menuHandler.showCatalogItem.setAction (hideCatalogAction);
    menuHandler.showLayoutItem.setAction (hideLayoutAction);
    menuHandler.showFreeSectorsItem.setAction (showFreeAction);
    menuHandler.duplicateItem.setAction (duplicateAction);
    menuHandler.closeTabItem.setAction (closeTabAction);

    final QuitAction quitAction = Platform.setQuit (this, prefs, menuHandler.fileMenu);

    quitAction.addQuitListener (menuHandler);
    quitAction.addQuitListener (menuHandler.fontAction);
    quitAction.addQuitListener (catalogPanel);
    quitAction.addQuitListener (this);

    catalogPanel.setDuplicateAction (duplicateAction);
    catalogPanel.setCloseTabAction (closeTabAction);

    pack ();

    //    prefs.addPreferenceChangeListener (catalogPanel);
    //    prefs.addPreferenceChangeListener (dataPanel);

    // restore the menuHandler items before they are referenced
    quitAction.restore ();

    // Remove the two optional panels if they were previously hidden
    if (!menuHandler.showLayoutItem.isSelected ())
      hideLayoutAction.set (false);
    if (!menuHandler.showCatalogItem.isSelected ())
      hideCatalogAction.set (false);

    // activate the highest panel now that the listeners are ready
    catalogPanel.activate ();
  }

  private JPanel addPanel (JComponent pane, String title, String location)
  {
    JPanel panel = new JPanel (new BorderLayout ());
    panel.setBackground (Color.WHITE);
    // panel.setOpaque (true);
    panel.setBorder (BorderFactory.createTitledBorder (title));
    panel.add (pane);
    add (panel, location);
    return panel;
  }

  @Override
  public void diskSelected (DiskSelectedEvent e)
  {
    setTitle (windowTitle + e.getFormattedDisk () == null ? ""
        : e.getFormattedDisk ().getName ());
  }

  @Override
  public void quit (Preferences preferences)
  {
    windowSaver.saveWindow ();
  }

  @Override
  public void restore (Preferences preferences)
  {
    windowSaver = new WindowSaver (prefs, this, "DiskBrowser");
    windowSaver.restoreWindow ();
  }

  public static void main (String[] args)
  {
    EventQueue.invokeLater (new Runnable ()
    {
      @Override
      public void run ()
      {
        Platform.setLookAndFeel ();
        new DiskBrowser ().setVisible (true);
      }
    });
  }
}