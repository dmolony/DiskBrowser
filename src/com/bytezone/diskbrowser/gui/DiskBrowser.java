package com.bytezone.diskbrowser.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
public class DiskBrowser extends JFrame
    implements DiskSelectionListener, QuitListener, PropertyChangeListener
// -----------------------------------------------------------------------------------//
{
  private static String[] args;
  private static final String windowTitle = "Apple ][ Disk Browser";
  private final Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private WindowSaver windowSaver;

  private static final String OS = System.getProperty ("os.name").toLowerCase ();
  private static final boolean MAC = OS.startsWith ("mac os");

  private final RootFolderData rootFolderData = new RootFolderData ();

  private final List<QuitListener> quitListeners = new ArrayList<> ();

  private final JPanel catalogBorderPanel;
  private final JPanel layoutBorderPanel;

  private final HideCatalogAction hideCatalogAction = new HideCatalogAction ();
  private final HideLayoutAction hideLayoutAction = new HideLayoutAction ();

  // ---------------------------------------------------------------------------------//
  public DiskBrowser ()
  // ---------------------------------------------------------------------------------//
  {
    super (windowTitle);

    UIManager.put ("TabbedPane.foreground", Color.BLACK);   // java bug fix

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
    catalogBorderPanel = addPanel (catalogPanel, "Catalog", BorderLayout.WEST);

    // create and add the centre output panel
    DataPanel dataPanel = new DataPanel (menuHandler);
    addPanel (dataPanel, "Output", BorderLayout.CENTER);

    // create and add the right-hand disk layout panel
    DiskLayoutPanel diskLayoutPanel = new DiskLayoutPanel ();
    layoutBorderPanel = addPanel (diskLayoutPanel, "Disk layout", BorderLayout.EAST);

    // create actions
    DuplicateAction duplicateAction = new DuplicateAction (rootFolderData);
    RootDirectoryAction rootDirectoryAction = new RootDirectoryAction ();

    RefreshTreeAction refreshTreeAction = new RefreshTreeAction (catalogPanel);
    //    PreferencesAction preferencesAction = new PreferencesAction (this, prefs);
    AbstractAction print = new PrintAction (dataPanel);
    //    AboutAction aboutAction = new AboutAction ();
    //    HideLayoutAction hideLayoutAction = new HideLayoutAction (this, layoutBorderPanel);
    ShowFreeSectorsAction showFreeAction = new ShowFreeSectorsAction ();
    CloseTabAction closeTabAction = new CloseTabAction (catalogPanel);

    hideCatalogAction.addPropertyChangeListener (this);
    hideLayoutAction.addPropertyChangeListener (this);

    // add action buttons to toolbar
    toolBar.add (rootDirectoryAction);
    toolBar.add (refreshTreeAction);
    //    toolBar.add (preferencesAction);
    toolBar.add (duplicateAction);
    toolBar.add (print);
    //    toolBar.add (aboutAction);

    // set the listeners
    rootDirectoryAction.addPropertyChangeListener (rootFolderData);
    rootDirectoryAction.addPropertyChangeListener (catalogPanel);
    rootDirectoryAction.addPropertyChangeListener (duplicateAction);

    catalogPanel.addDiskSelectionListener (this, dataPanel, diskLayoutPanel, redoHandler,
        menuHandler, menuHandler.saveDiskAction);

    catalogPanel.addFileSelectionListener (dataPanel, diskLayoutPanel, redoHandler,
        menuHandler, menuHandler.saveFileAction);

    catalogPanel.addFileNodeSelectionListener (dataPanel, redoHandler);

    diskLayoutPanel.addSectorSelectionListener (dataPanel, redoHandler, catalogPanel,
        menuHandler.saveSectorsAction);

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
    menuHandler.refreshTreeItem.setAction (refreshTreeAction);
    menuHandler.rootItem.setAction (rootDirectoryAction);
    menuHandler.showCatalogItem.setAction (hideCatalogAction);
    menuHandler.showLayoutItem.setAction (hideLayoutAction);
    menuHandler.showFreeSectorsItem.setAction (showFreeAction);
    menuHandler.duplicateItem.setAction (duplicateAction);
    menuHandler.closeTabItem.setAction (closeTabAction);

    showFreeAction.addPropertyChangeListener (diskLayoutPanel);

    quitListeners.add (rootDirectoryAction);
    quitListeners.add (menuHandler);
    quitListeners.add (catalogPanel);
    quitListeners.add (this);

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
        desktop.setQuitHandler ( (e, r) -> fireQuitEvent ());   // needed for cmd-Q
      //      else
      setQuitHandler ();        // needed for the close button
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
      setLayoutPanel (false);
    if (!menuHandler.showCatalogItem.isSelected ())
      setCatalogPanel (false);

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
  public void propertyChange (PropertyChangeEvent evt)
  // ---------------------------------------------------------------------------------//
  {
    if (evt.getSource () == hideCatalogAction)
      setCatalogPanel ((boolean) evt.getNewValue ());
    else if (evt.getSource () == hideLayoutAction)
      setLayoutPanel ((boolean) evt.getNewValue ());
  }

  // ---------------------------------------------------------------------------------//
  private void setCatalogPanel (boolean show)
  // ---------------------------------------------------------------------------------//
  {
    if (show)
      add (catalogBorderPanel, BorderLayout.WEST);
    else
      remove (catalogBorderPanel);

    validate ();
  }

  // ---------------------------------------------------------------------------------//
  private void setLayoutPanel (boolean show)
  // ---------------------------------------------------------------------------------//
  {
    if (show)
      add (layoutBorderPanel, BorderLayout.EAST);
    else
      remove (layoutBorderPanel);

    validate ();
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
  private static void setLookAndFeel ()
  // ---------------------------------------------------------------------------------//
  {
    //    FlatLightLaf.install ();
    try
    {
      UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName ());
      //      UIManager.setLookAndFeel (new FlatLightLaf ());
      if (MAC)
        System.setProperty ("apple.laf.useScreenMenuBar", "true");
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
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
}