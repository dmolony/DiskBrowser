package com.bytezone.diskbrowser.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.bytezone.diskbrowser.duplicates.RootFolderData;

// -----------------------------------------------------------------------------------//
public class DiskBrowser extends JFrame implements DiskSelectionListener, QuitListener
// -----------------------------------------------------------------------------------//
{
  private static String[] args;
  private static final String windowTitle = "Apple ][ Disk Browser";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private WindowSaver windowSaver;

  private static final String OS = System.getProperty ("os.name").toLowerCase ();
  private static final boolean MAC = OS.startsWith ("mac os");

  private final RootFolderData rootFolderData = new RootFolderData ();

  // ---------------------------------------------------------------------------------//
  public DiskBrowser ()
  // ---------------------------------------------------------------------------------//
  {
    super (windowTitle);

    if (args.length > 0 && "-reset".equals (args[0]))
      new WindowState (prefs).clear ();

    JToolBar toolBar = new JToolBar ("Toolbar", JToolBar.HORIZONTAL);
    MenuHandler menuHandler = new MenuHandler ();

    setJMenuBar (menuHandler.menuBar);
    setLayout (new BorderLayout ());
    add (toolBar, BorderLayout.NORTH);

    // add navigation buttons
    RedoHandler redoHandler = new RedoHandler (getRootPane (), toolBar);
    toolBar.addSeparator ();

    // create and add the left-hand catalog panel
    CatalogPanel catalogPanel = new CatalogPanel (redoHandler);
    JPanel catalogBorderPanel = addPanel (catalogPanel, "Catalog", BorderLayout.WEST);

    // create and add the centre output panel
    DataPanel dataPanel = new DataPanel (menuHandler);
    addPanel (dataPanel, "Output", BorderLayout.CENTER);

    // create and add the right-hand disk layout panel
    DiskLayoutPanel diskLayoutPanel = new DiskLayoutPanel ();
    JPanel layoutBorderPanel =
        addPanel (diskLayoutPanel, "Disk layout", BorderLayout.EAST);

    // create actions
    DuplicateAction duplicateAction = new DuplicateAction (rootFolderData);
    RootDirectoryAction rootDirectoryAction = new RootDirectoryAction ();

    RefreshTreeAction refreshTreeAction = new RefreshTreeAction (catalogPanel);
    //    PreferencesAction preferencesAction = new PreferencesAction (this, prefs);
    AbstractAction print = new PrintAction (dataPanel);
    //    AboutAction aboutAction = new AboutAction ();
    HideCatalogAction hideCatalogAction =
        new HideCatalogAction (this, catalogBorderPanel);
    HideLayoutAction hideLayoutAction = new HideLayoutAction (this, layoutBorderPanel);
    ShowFreeSectorsAction showFreeAction =
        new ShowFreeSectorsAction (menuHandler, diskLayoutPanel);
    CloseTabAction closeTabAction = new CloseTabAction (catalogPanel);

    // add action buttons to toolbar
    toolBar.add (rootDirectoryAction);
    toolBar.add (refreshTreeAction);
    //    toolBar.add (preferencesAction);
    toolBar.add (duplicateAction);
    toolBar.add (print);
    //    toolBar.add (aboutAction);

    // set the listeners
    rootDirectoryAction.addListener (rootFolderData);
    rootDirectoryAction.addListener (catalogPanel);
    rootDirectoryAction.addListener (duplicateAction);

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
    diskLayoutPanel.addSectorSelectionListener (menuHandler);
    diskLayoutPanel.addSectorSelectionListener (menuHandler.saveSectorsAction);

    duplicateAction.addTableSelectionListener (catalogPanel);

    menuHandler.scale1Item.setAction (new ScaleAction (dataPanel, 1.0, 1));
    menuHandler.scale2Item.setAction (new ScaleAction (dataPanel, 1.5, 2));
    menuHandler.scale3Item.setAction (new ScaleAction (dataPanel, 2.0, 3));

    redoHandler.addRedoListener (catalogPanel);
    redoHandler.addRedoListener (diskLayoutPanel);

    menuHandler.fontAction.addFontChangeListener (dataPanel);
    menuHandler.fontAction.addFontChangeListener (catalogPanel);
    //    menuHandler.fontAction.addFontChangeListener (diskLayoutPanel);

    // set the MenuItem Actions
    menuHandler.printItem.setAction (print);
    //    menuHandler.addHelpMenuAction (preferencesAction, "prefs");
    //    menuHandler.addHelpMenuAction (aboutAction, "about");
    menuHandler.refreshTreeItem.setAction (refreshTreeAction);
    menuHandler.rootItem.setAction (rootDirectoryAction);
    menuHandler.showCatalogItem.setAction (hideCatalogAction);
    menuHandler.showLayoutItem.setAction (hideLayoutAction);
    menuHandler.showFreeSectorsItem.setAction (showFreeAction);
    menuHandler.duplicateItem.setAction (duplicateAction);
    menuHandler.closeTabItem.setAction (closeTabAction);

    addQuitListener (rootDirectoryAction);
    addQuitListener (menuHandler);
    addQuitListener (catalogPanel);
    addQuitListener (this);

    if (Desktop.isDesktopSupported ())
    {
      Desktop desktop = Desktop.getDesktop ();

      if (desktop.isSupported (Desktop.Action.APP_ABOUT))
        desktop.setAboutHandler (e -> JOptionPane.showMessageDialog (null,
            "Author - Denis Molony\nGitHub - https://github.com/dmolony/DiskBrowser",
            "About DiskBrowser", JOptionPane.INFORMATION_MESSAGE));

      if (desktop.isSupported (Desktop.Action.APP_PREFERENCES) && false)
        desktop.setPreferencesHandler (
            e -> JOptionPane.showMessageDialog (null, "Preferences dialog"));

      if (desktop.isSupported (Desktop.Action.APP_QUIT_HANDLER))
        desktop.setQuitHandler ( (e, r) -> fireQuitEvent ());
      else
        setQuitHandler ();
    }
    else
    {
      System.out.println ("Desktop not supported");
      setQuitHandler ();
    }

    catalogPanel.setCloseTabAction (closeTabAction);

    pack ();

    // restore the menuHandler items before they are referenced
    fireRestoreEvent ();

    diskLayoutPanel.setFree (menuHandler.showFreeSectorsItem.isSelected ());
    dataPanel.setLineWrap (menuHandler.lineWrapItem.isSelected ());

    // Remove the two optional panels if they were previously hidden
    if (!menuHandler.showLayoutItem.isSelected ())
      hideLayoutAction.set (false);
    if (!menuHandler.showCatalogItem.isSelected ())
      hideCatalogAction.set (false);

    menuHandler.addBasicPreferencesListener (dataPanel);
    menuHandler.addAssemblerPreferencesListener (dataPanel);
    menuHandler.addTextPreferencesListener (dataPanel);

    // activate the highest panel now that the listeners are ready
    catalogPanel.activate ();
  }

  // ---------------------------------------------------------------------------------//
  private void setQuitHandler ()
  // ---------------------------------------------------------------------------------//
  {
    addWindowListener (new WindowAdapter ()
    {
      @Override
      public void windowClosing (WindowEvent e)
      {
        fireQuitEvent ();
      }
    });

  }

  // ---------------------------------------------------------------------------------//
  private JPanel addPanel (JComponent pane, String title, String location)
  // ---------------------------------------------------------------------------------//
  {
    JPanel panel = new JPanel (new BorderLayout ());
    panel.setBackground (Color.WHITE);
    panel.setBorder (BorderFactory.createTitledBorder (title));
    panel.add (pane);
    add (panel, location);
    return panel;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void diskSelected (DiskSelectedEvent e)
  // ---------------------------------------------------------------------------------//
  {
    setTitle (windowTitle + e.getFormattedDisk () == null ? ""
        : e.getFormattedDisk ().getName ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void quit (Preferences preferences)
  // ---------------------------------------------------------------------------------//
  {
    windowSaver.saveWindow ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences preferences)
  // ---------------------------------------------------------------------------------//
  {
    windowSaver = new WindowSaver (prefs, this, "DiskBrowser");
    windowSaver.restoreWindow ();
  }

  // ---------------------------------------------------------------------------------//
  public static void main (String[] args)
  // ---------------------------------------------------------------------------------//
  {
    DiskBrowser.args = args;
    EventQueue.invokeLater (new Runnable ()
    {
      @Override
      public void run ()
      {
        setLookAndFeel ();
        new DiskBrowser ().setVisible (true);
      }
    });
  }

  // ---------------------------------------------------------------------------------//
  private static void setLookAndFeel ()
  // ---------------------------------------------------------------------------------//
  {
    try
    {
      UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName ());
      if (MAC)
        System.setProperty ("apple.laf.useScreenMenuBar", "true");
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
  }

  List<QuitListener> quitListeners = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public void addQuitListener (QuitListener listener)
  // ---------------------------------------------------------------------------------//
  {
    quitListeners.add (listener);
  }

  // ---------------------------------------------------------------------------------//
  public void removeQuitListener (QuitListener listener)
  // ---------------------------------------------------------------------------------//
  {
    quitListeners.remove (listener);
  }

  // ---------------------------------------------------------------------------------//
  private void fireQuitEvent ()
  // ---------------------------------------------------------------------------------//
  {
    for (QuitListener listener : quitListeners)
      listener.quit (prefs);

    System.exit (0);
  }

  // ---------------------------------------------------------------------------------//
  private void fireRestoreEvent ()
  // ---------------------------------------------------------------------------------//
  {
    for (QuitListener listener : quitListeners)
      listener.restore (prefs);
  }
}