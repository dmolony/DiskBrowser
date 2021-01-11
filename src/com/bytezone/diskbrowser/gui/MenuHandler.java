package com.bytezone.diskbrowser.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.AssemblerProgram;
import com.bytezone.diskbrowser.applefile.BasicProgram;
import com.bytezone.diskbrowser.applefile.BasicTextFile;
import com.bytezone.diskbrowser.applefile.HiResImage;
import com.bytezone.diskbrowser.applefile.Palette;
import com.bytezone.diskbrowser.applefile.PaletteFactory;
import com.bytezone.diskbrowser.disk.DataDisk;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.prodos.ProdosDisk;
import com.bytezone.diskbrowser.utilities.EnvironmentAction;

// -----------------------------------------------------------------------------------//
class MenuHandler implements DiskSelectionListener, FileSelectionListener, QuitListener,
    SectorSelectionListener
// -----------------------------------------------------------------------------------//
{
  static final String PREFS_LINE_WRAP = "line wrap";
  private static final String PREFS_SHOW_CATALOG = "show catalog";
  private static final String PREFS_SHOW_LAYOUT = "show layout";
  private static final String PREFS_SHOW_FREE_SECTORS = "show free sectors";
  private static final String PREFS_COLOUR_QUIRKS = "colour quirks";
  private static final String PREFS_MONOCHROME = "monochrome";
  private static final String PREFS_SCALE = "scale";

  private static final String PREFS_SHOW_HEADER = "showHeader";
  private static final String PREFS_SHOW_ALL_FORMAT = "formatApplesoft";
  private static final String PREFS_SHOW_ALL_XREF = "showAllXref";

  private static final String PREFS_SPLIT_REMARKS = "splitRemarks";
  private static final String PREFS_SPLIT_DIM = "splitDim";
  private static final String PREFS_ALIGN_ASSIGN = "alignAssign";
  private static final String PREFS_SHOW_TARGETS = "showTargets";
  private static final String PREFS_ONLY_SHOW_TARGETS = "onlyShowTargets";
  private static final String PREFS_SHOW_CARET = "showCaret";
  private static final String PREFS_SHOW_THEN = "showThen";
  private static final String PREFS_SHOW_XREF = "showXref";
  private static final String PREFS_SHOW_CALLS = "showCalls";
  private static final String PREFS_SHOW_SYMBOLS = "showSymbols";
  private static final String PREFS_SHOW_CONSTANTS = "showConstants";
  private static final String PREFS_SHOW_FUNCTIONS = "showFunctions";
  private static final String PREFS_SHOW_DUPLICATE_SYMBOLS = "showDuplicateSymbols";
  private static final String PREFS_LIST_STRINGS = "listStrings";
  private static final String PREFS_BLANK_AFTER_RETURN = "blankAfterReturn";
  private static final String PREFS_DELETE_EXTRA_REM_SPACE = "deleteExtraRemSpace";
  private static final String PREFS_DELETE_EXTRA_DATA_SPACE = "deleteExtraDataSpace";

  private static final String PREFS_SHOW_ASSEMBLER_TARGETS = "showAssemblerTargets";
  private static final String PREFS_SHOW_ASSEMBLER_STRINGS = "showAssemblerStrings";
  private static final String PREFS_SHOW_ASSEMBLER_HEADER = "showAssemblerHeader";

  private static final String PREFS_PRODOS_SORT_DIRECTORIES = "prodosSortDirectories";

  private static final String PREFS_TEXT_SHOW_OFFSETS = "showTextOffsets";
  private static final String PREFS_TEXT_SHOW_HEADER = "showTextHeader";

  private static final String PREFS_PALETTE = "palette";

  FormattedDisk currentDisk;
  private final SaveTempFileAction saveTempFileAction = new SaveTempFileAction ();
  final SaveSectorsAction saveSectorsAction = new SaveSectorsAction ();

  private final BasicPreferences basicPreferences = new BasicPreferences ();
  private final List<BasicPreferencesListener> basicPreferencesListeners =
      new ArrayList<> ();

  private final AssemblerPreferences assemblerPreferences = new AssemblerPreferences ();
  private final List<AssemblerPreferencesListener> assemblerPreferencesListeners =
      new ArrayList<> ();

  private final ProdosPreferences prodosPreferences = new ProdosPreferences ();
  private final List<ProdosPreferencesListener> prodosPreferencesListeners =
      new ArrayList<> ();

  private final TextPreferences textPreferences = new TextPreferences ();
  private final List<TextPreferencesListener> textPreferencesListeners =
      new ArrayList<> ();

  private List<JCheckBoxMenuItem> applesoftFormatItems;
  private List<JCheckBoxMenuItem> applesoftXrefItems;

  JMenuBar menuBar = new JMenuBar ();
  JMenu fileMenu = new JMenu ("File");
  JMenu formatMenu = new JMenu ("Format");
  JMenu imageMenu = new JMenu ("Images");
  JMenu applesoftMenu = new JMenu ("Applesoft");
  JMenu textMenu = new JMenu ("Text");
  JMenu assemblerMenu = new JMenu ("Assembler");
  JMenu prodosMenu = new JMenu ("Prodos");
  JMenu helpMenu = new JMenu ("Help");

  // File menu items
  final JMenuItem rootItem = new JMenuItem ("Set root folder...");
  final JMenuItem refreshTreeItem = new JMenuItem ("Refresh current tree");
  final JMenuItem executeDiskItem = new JMenuItem ();
  final JMenuItem saveDiskItem = new JMenuItem ("Save converted disk as...");
  final JMenuItem saveSectorsItem = new JMenuItem ("Save sectors as...");
  final JMenuItem printItem = new JMenuItem ("Print output panel...");
  final JMenuItem closeTabItem = new JMenuItem ();
  final JMenuItem duplicateItem = new JMenuItem ();
  final FontAction fontAction = new FontAction ();
  final JMenuItem debuggingItem = new JCheckBoxMenuItem ("Debugging");

  // Format menu items
  final JMenuItem lineWrapItem = new JCheckBoxMenuItem ("Line wrap");
  final JMenuItem showLayoutItem = new JCheckBoxMenuItem ("Show layout panel");
  final JMenuItem showCatalogItem = new JCheckBoxMenuItem ("Show catalog panel");
  final JMenuItem showFreeSectorsItem = new JCheckBoxMenuItem ("Show free sectors");

  final JMenuItem sector256Item = new JRadioButtonMenuItem ("256 byte sectors");
  final JMenuItem sector512Item = new JRadioButtonMenuItem ("512 byte blocks");
  final JMenuItem interleave0Item = new JRadioButtonMenuItem (new InterleaveAction (0));
  final JMenuItem interleave1Item = new JRadioButtonMenuItem (new InterleaveAction (1));
  final JMenuItem interleave2Item = new JRadioButtonMenuItem (new InterleaveAction (2));
  final JMenuItem interleave3Item = new JRadioButtonMenuItem (new InterleaveAction (3));

  final JMenuItem colourQuirksItem = new JCheckBoxMenuItem ("Colour quirks");
  final JMenuItem monochromeItem = new JCheckBoxMenuItem ("Monochrome");
  final JMenuItem nextPaletteItem = new JMenuItem ("Next Palette");
  final JMenuItem prevPaletteItem = new JMenuItem ("Previous Palette");
  final JMenuItem scale1Item = new JRadioButtonMenuItem ("Scale 1");
  final JMenuItem scale2Item = new JRadioButtonMenuItem ("Scale 1.5");
  final JMenuItem scale3Item = new JRadioButtonMenuItem ("Scale 2");

  // Applesoft menu items
  final JMenuItem showHeaderItem = new JCheckBoxMenuItem ("Show header");
  final JMenuItem showAllFormatItem = new JCheckBoxMenuItem ("Enable Format options");
  final JMenuItem showAllXrefItem = new JCheckBoxMenuItem ("Enable XREF options");

  final JMenuItem splitRemarkItem = new JCheckBoxMenuItem ("Split remarks");
  final JMenuItem splitDimItem = new JCheckBoxMenuItem ("Split DIM");
  final JMenuItem alignAssignItem = new JCheckBoxMenuItem ("Align consecutive assign");
  final JMenuItem showBasicTargetsItem = new JCheckBoxMenuItem ("Show targets");
  final JMenuItem onlyShowTargetLinesItem =
      new JCheckBoxMenuItem ("Only show target line numbers");
  final JMenuItem showCaretItem = new JCheckBoxMenuItem ("Show caret");
  final JMenuItem showThenItem = new JCheckBoxMenuItem ("Show THEN after IF");
  final JMenuItem blankAfterReturn = new JCheckBoxMenuItem ("Blank line after RETURN");
  final JMenuItem deleteExtraRemSpace = new JCheckBoxMenuItem ("Delete extra REM space");
  final JMenuItem deleteExtraDataSpace =
      new JCheckBoxMenuItem ("Delete extra DATA space");

  final JMenuItem showXrefItem = new JCheckBoxMenuItem ("List GOSUB/GOTO");
  final JMenuItem showCallsItem = new JCheckBoxMenuItem ("List CALLs");
  final JMenuItem showSymbolsItem = new JCheckBoxMenuItem ("List variables");
  final JMenuItem showFunctionsItem = new JCheckBoxMenuItem ("List functions");
  final JMenuItem showConstantsItem = new JCheckBoxMenuItem ("List literals");
  final JMenuItem showDuplicateSymbolsItem =
      new JCheckBoxMenuItem ("List duplicate variables");
  final JMenuItem listStringsItem = new JCheckBoxMenuItem ("List strings");

  // Assembler menu items
  final JMenuItem showAssemblerTargetsItem = new JCheckBoxMenuItem ("Show targets");
  final JMenuItem showAssemblerStringsItem =
      new JCheckBoxMenuItem ("List possible strings");
  final JMenuItem showAssemblerHeaderItem = new JCheckBoxMenuItem ("Show header");

  // Prodos menu items
  final JMenuItem prodosSortDirectoriesItem = new JCheckBoxMenuItem ("Sort directories");

  // Text menu items
  final JMenuItem showTextOffsetsItem = new JCheckBoxMenuItem ("Show offsets");
  final JMenuItem showTextHeaderItem = new JCheckBoxMenuItem ("Show header");

  ButtonGroup paletteGroup = new ButtonGroup ();

  // ---------------------------------------------------------------------------------//
  MenuHandler ()
  // ---------------------------------------------------------------------------------//
  {
    menuBar.add (fileMenu);
    menuBar.add (formatMenu);
    menuBar.add (imageMenu);
    menuBar.add (prodosMenu);
    menuBar.add (applesoftMenu);
    menuBar.add (assemblerMenu);
    menuBar.add (textMenu);
    menuBar.add (helpMenu);

    fileMenu.add (rootItem);
    fileMenu.addSeparator ();
    fileMenu.add (refreshTreeItem);
    fileMenu.add (saveDiskItem);
    fileMenu.add (saveSectorsItem);

    addLauncherMenu ();

    fileMenu.add (printItem);
    fileMenu.addSeparator ();
    fileMenu.add (closeTabItem);

    JMenuItem fontItem = new JMenuItem (fontAction);
    fileMenu.add (fontItem);
    fontAction.setSampleText ("120  FOR Z = 14 TO 24:\n  VTAB 5:\n  HTAB Z:\n"
        + "  PRINT AB$:\n  FOR TI = 1 TO 50:\n  NEXT :\n  POKE 0,Z + 40:\n"
        + "  POKE 1,9:\n  CALL MU:\n  VTAB 5:\n  HTAB Z:\n"
        + "  PRINT SPC(12):\nNEXT :\nVTAB 5:\nHTAB 24:\nPRINT AB$\n");

    fileMenu.add (duplicateItem);
    fileMenu.add (debuggingItem);

    formatMenu.add (lineWrapItem);
    formatMenu.add (showCatalogItem);
    formatMenu.add (showLayoutItem);
    formatMenu.add (showFreeSectorsItem);

    formatMenu.addSeparator ();

    formatMenu.add (interleave0Item);
    formatMenu.add (interleave1Item);
    formatMenu.add (interleave2Item);
    formatMenu.add (interleave3Item);

    formatMenu.addSeparator ();

    formatMenu.add (sector256Item);
    formatMenu.add (sector512Item);

    // set placeholders for the palettes
    List<Palette> palettes = HiResImage.getPalettes ();
    for (int i = 0; i < palettes.size (); i++)
    {
      JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem ("x");
      paletteGroup.add (menuItem);
      imageMenu.add (menuItem);
    }

    imageMenu.addSeparator ();
    imageMenu.add (colourQuirksItem);
    imageMenu.add (monochromeItem);
    imageMenu.addSeparator ();
    imageMenu.add (nextPaletteItem);
    imageMenu.add (prevPaletteItem);
    imageMenu.addSeparator ();
    imageMenu.add (scale1Item);
    imageMenu.add (scale2Item);
    imageMenu.add (scale3Item);

    applesoftMenu.add (showHeaderItem);
    applesoftMenu.add (showAllFormatItem);
    applesoftMenu.add (showAllXrefItem);
    applesoftMenu.addSeparator ();
    applesoftMenu.add (splitRemarkItem);
    applesoftMenu.add (splitDimItem);
    applesoftMenu.add (alignAssignItem);
    applesoftMenu.add (showBasicTargetsItem);
    applesoftMenu.add (onlyShowTargetLinesItem);
    applesoftMenu.add (showCaretItem);
    applesoftMenu.add (showThenItem);
    applesoftMenu.add (blankAfterReturn);
    applesoftMenu.add (deleteExtraRemSpace);
    applesoftMenu.add (deleteExtraDataSpace);
    applesoftMenu.addSeparator ();
    applesoftMenu.add (showXrefItem);
    applesoftMenu.add (showCallsItem);
    applesoftMenu.add (showSymbolsItem);
    applesoftMenu.add (showFunctionsItem);
    applesoftMenu.add (showConstantsItem);
    applesoftMenu.add (listStringsItem);
    applesoftMenu.add (showDuplicateSymbolsItem);

    assemblerMenu.add (showAssemblerHeaderItem);
    assemblerMenu.add (showAssemblerTargetsItem);
    assemblerMenu.add (showAssemblerStringsItem);

    textMenu.add (showTextHeaderItem);
    textMenu.add (showTextOffsetsItem);

    prodosMenu.add (prodosSortDirectoriesItem);

    applesoftFormatItems = new ArrayList (Arrays.asList (splitRemarkItem, splitDimItem,
        alignAssignItem, showBasicTargetsItem, onlyShowTargetLinesItem, showCaretItem,
        showThenItem, blankAfterReturn, deleteExtraRemSpace, deleteExtraDataSpace));

    applesoftXrefItems = new ArrayList (
        Arrays.asList (showXrefItem, showCallsItem, showSymbolsItem, showFunctionsItem,
            showConstantsItem, listStringsItem, showDuplicateSymbolsItem));

    ActionListener basicPreferencesAction = new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        setBasicPreferences ();
        notifyBasicPreferencesListeners ();
      }
    };

    ActionListener assemblerPreferencesAction = new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        setAssemblerPreferences ();
        notifyAssemblerPreferencesListeners ();
      }
    };

    ActionListener prodosPreferencesAction = new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        setProdosPreferences ();
        notifyProdosPreferencesListeners ();
      }
    };

    ActionListener textPreferencesAction = new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        setTextPreferences ();
        notifyTextPreferencesListeners ();
      }
    };

    showHeaderItem.addActionListener (basicPreferencesAction);
    showAllFormatItem.addActionListener (basicPreferencesAction);
    showAllXrefItem.addActionListener (basicPreferencesAction);
    for (JMenuItem item : applesoftFormatItems)
      item.addActionListener (basicPreferencesAction);
    for (JMenuItem item : applesoftXrefItems)
      item.addActionListener (basicPreferencesAction);

    showAssemblerTargetsItem.addActionListener (assemblerPreferencesAction);
    showAssemblerStringsItem.addActionListener (assemblerPreferencesAction);
    showAssemblerHeaderItem.addActionListener (assemblerPreferencesAction);

    prodosSortDirectoriesItem.addActionListener (prodosPreferencesAction);

    showTextOffsetsItem.addActionListener (textPreferencesAction);
    showTextHeaderItem.addActionListener (textPreferencesAction);

    helpMenu.add (new JMenuItem (new EnvironmentAction ()));

    sector256Item.setActionCommand ("256");
    sector256Item.setAccelerator (KeyStroke.getKeyStroke ("alt 4"));
    sector512Item.setActionCommand ("512");
    sector512Item.setAccelerator (KeyStroke.getKeyStroke ("alt 5"));

    ButtonGroup sectorGroup = new ButtonGroup ();
    ButtonGroup interleaveGroup = new ButtonGroup ();
    ButtonGroup scaleGroup = new ButtonGroup ();

    sectorGroup.add (sector256Item);
    sectorGroup.add (sector512Item);
    interleaveGroup.add (interleave0Item);
    interleaveGroup.add (interleave1Item);
    interleaveGroup.add (interleave2Item);
    interleaveGroup.add (interleave3Item);
    scaleGroup.add (scale1Item);
    scaleGroup.add (scale2Item);
    scaleGroup.add (scale3Item);

    saveDiskItem.setAction (saveTempFileAction);
    saveSectorsItem.setAction (saveSectorsAction);
  }

  // ---------------------------------------------------------------------------------//
  private void enableApplesoftFormatItems (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    for (JMenuItem item : applesoftFormatItems)
      item.setEnabled (value);
  }

  // ---------------------------------------------------------------------------------//
  private void enableAllXrefItems (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    for (JMenuItem item : applesoftXrefItems)
      item.setEnabled (value);
  }

  // ---------------------------------------------------------------------------------//
  private void setBasicPreferences ()
  // ---------------------------------------------------------------------------------//
  {
    basicPreferences.showHeader = showHeaderItem.isSelected ();
    basicPreferences.formatApplesoft = showAllFormatItem.isSelected ();
    basicPreferences.showAllXref = showAllXrefItem.isSelected ();

    basicPreferences.splitRem = splitRemarkItem.isSelected ();
    basicPreferences.splitDim = splitDimItem.isSelected ();
    basicPreferences.alignAssign = alignAssignItem.isSelected ();
    basicPreferences.showTargets = showBasicTargetsItem.isSelected ();
    basicPreferences.onlyShowTargetLineNumbers = onlyShowTargetLinesItem.isSelected ();
    basicPreferences.showCaret = showCaretItem.isSelected ();
    basicPreferences.showThen = showThenItem.isSelected ();
    basicPreferences.blankAfterReturn = blankAfterReturn.isSelected ();
    basicPreferences.deleteExtraRemSpace = deleteExtraRemSpace.isSelected ();
    basicPreferences.deleteExtraDataSpace = deleteExtraDataSpace.isSelected ();

    basicPreferences.showXref = showXrefItem.isSelected ();
    basicPreferences.showCalls = showCallsItem.isSelected ();
    basicPreferences.showSymbols = showSymbolsItem.isSelected ();
    basicPreferences.showFunctions = showFunctionsItem.isSelected ();
    basicPreferences.showConstants = showConstantsItem.isSelected ();
    basicPreferences.listStrings = listStringsItem.isSelected ();
    basicPreferences.showDuplicateSymbols = showDuplicateSymbolsItem.isSelected ();

    BasicProgram.setBasicPreferences (basicPreferences);

    enableApplesoftFormatItems (basicPreferences.formatApplesoft);
    enableAllXrefItems (basicPreferences.showAllXref);
  }

  // ---------------------------------------------------------------------------------//
  void addBasicPreferencesListener (BasicPreferencesListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!basicPreferencesListeners.contains (listener))
    {
      basicPreferencesListeners.add (listener);
      listener.setBasicPreferences (basicPreferences);
    }
  }

  // ---------------------------------------------------------------------------------//
  void notifyBasicPreferencesListeners ()
  // ---------------------------------------------------------------------------------//
  {
    for (BasicPreferencesListener listener : basicPreferencesListeners)
      listener.setBasicPreferences (basicPreferences);
  }

  // ---------------------------------------------------------------------------------//
  private void setAssemblerPreferences ()
  // ---------------------------------------------------------------------------------//
  {
    assemblerPreferences.showTargets = showAssemblerTargetsItem.isSelected ();
    assemblerPreferences.showStrings = showAssemblerStringsItem.isSelected ();
    assemblerPreferences.showHeader = showAssemblerHeaderItem.isSelected ();
    AssemblerProgram.setAssemblerPreferences (assemblerPreferences);
  }

  // ---------------------------------------------------------------------------------//
  void addAssemblerPreferencesListener (AssemblerPreferencesListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!assemblerPreferencesListeners.contains (listener))
    {
      assemblerPreferencesListeners.add (listener);
      listener.setAssemblerPreferences (assemblerPreferences);
    }
  }

  // ---------------------------------------------------------------------------------//
  void notifyAssemblerPreferencesListeners ()
  // ---------------------------------------------------------------------------------//
  {
    for (AssemblerPreferencesListener listener : assemblerPreferencesListeners)
      listener.setAssemblerPreferences (assemblerPreferences);
  }

  // ---------------------------------------------------------------------------------//
  private void setProdosPreferences ()
  // ---------------------------------------------------------------------------------//
  {
    prodosPreferences.sortDirectories = prodosSortDirectoriesItem.isSelected ();
    ProdosDisk.setProdosPreferences (prodosPreferences);
  }

  // ---------------------------------------------------------------------------------//
  void addProdosPreferencesListener (ProdosPreferencesListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!prodosPreferencesListeners.contains (listener))
    {
      prodosPreferencesListeners.add (listener);
      listener.setProdosPreferences (prodosPreferences);
    }
  }

  // ---------------------------------------------------------------------------------//
  void notifyProdosPreferencesListeners ()
  // ---------------------------------------------------------------------------------//
  {
    for (ProdosPreferencesListener listener : prodosPreferencesListeners)
      listener.setProdosPreferences (prodosPreferences);
  }

  // ---------------------------------------------------------------------------------//
  private void setTextPreferences ()
  // ---------------------------------------------------------------------------------//
  {
    textPreferences.showTextOffsets = showTextOffsetsItem.isSelected ();
    textPreferences.showHeader = showTextHeaderItem.isSelected ();
    BasicTextFile.setTextPreferences (textPreferences);
  }

  // ---------------------------------------------------------------------------------//
  void addTextPreferencesListener (TextPreferencesListener listener)
  // ---------------------------------------------------------------------------------//
  {
    if (!textPreferencesListeners.contains (listener))
    {
      textPreferencesListeners.add (listener);
      listener.setTextPreferences (textPreferences);
    }
  }

  // ---------------------------------------------------------------------------------//
  void notifyTextPreferencesListeners ()
  // ---------------------------------------------------------------------------------//
  {
    for (TextPreferencesListener listener : textPreferencesListeners)
      listener.setTextPreferences (textPreferences);
  }

  // ---------------------------------------------------------------------------------//
  private void addLauncherMenu ()
  // ---------------------------------------------------------------------------------//
  {
    if (!Desktop.isDesktopSupported ())
      return;

    boolean openSupported = false;
    for (Desktop.Action action : Desktop.Action.values ())
      if (action.toString ().equals ("OPEN"))
      {
        openSupported = true;
        break;
      }
    if (!openSupported)
      return;

    executeDiskItem.setAction (new ExecuteDiskAction (this));
    fileMenu.add (executeDiskItem);
    fileMenu.addSeparator ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void quit (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    prefs.putBoolean (PREFS_LINE_WRAP, lineWrapItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_LAYOUT, showLayoutItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_CATALOG, showCatalogItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_FREE_SECTORS, showFreeSectorsItem.isSelected ());
    prefs.putBoolean (PREFS_COLOUR_QUIRKS, colourQuirksItem.isSelected ());
    prefs.putBoolean (PREFS_MONOCHROME, monochromeItem.isSelected ());
    prefs.putInt (PREFS_PALETTE,
        HiResImage.getPaletteFactory ().getCurrentPaletteIndex ());
    fontAction.quit (prefs);

    int scale = scale1Item.isSelected () ? 1 : scale2Item.isSelected () ? 2 : 3;
    prefs.putInt (PREFS_SCALE, scale);

    prefs.putBoolean (PREFS_SHOW_HEADER, showHeaderItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_ALL_FORMAT, showAllFormatItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_ALL_XREF, showAllXrefItem.isSelected ());

    prefs.putBoolean (PREFS_SPLIT_REMARKS, splitRemarkItem.isSelected ());
    prefs.putBoolean (PREFS_SPLIT_DIM, splitDimItem.isSelected ());
    prefs.putBoolean (PREFS_ALIGN_ASSIGN, alignAssignItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_CARET, showCaretItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_THEN, showThenItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_XREF, showXrefItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_CALLS, showCallsItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_SYMBOLS, showSymbolsItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_FUNCTIONS, showFunctionsItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_CONSTANTS, showConstantsItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_DUPLICATE_SYMBOLS,
        showDuplicateSymbolsItem.isSelected ());
    prefs.putBoolean (PREFS_LIST_STRINGS, listStringsItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_TARGETS, showBasicTargetsItem.isSelected ());
    prefs.putBoolean (PREFS_ONLY_SHOW_TARGETS, onlyShowTargetLinesItem.isSelected ());
    prefs.putBoolean (PREFS_BLANK_AFTER_RETURN, blankAfterReturn.isSelected ());
    prefs.putBoolean (PREFS_DELETE_EXTRA_REM_SPACE, deleteExtraRemSpace.isSelected ());
    prefs.putBoolean (PREFS_DELETE_EXTRA_DATA_SPACE, deleteExtraDataSpace.isSelected ());

    prefs.putBoolean (PREFS_SHOW_ASSEMBLER_TARGETS,
        showAssemblerTargetsItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_ASSEMBLER_STRINGS,
        showAssemblerStringsItem.isSelected ());
    prefs.putBoolean (PREFS_SHOW_ASSEMBLER_HEADER, showAssemblerHeaderItem.isSelected ());

    prefs.putBoolean (PREFS_PRODOS_SORT_DIRECTORIES,
        prodosSortDirectoriesItem.isSelected ());

    prefs.putBoolean (PREFS_TEXT_SHOW_OFFSETS, showTextOffsetsItem.isSelected ());
    prefs.putBoolean (PREFS_TEXT_SHOW_HEADER, showTextHeaderItem.isSelected ());
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences prefs)
  // ---------------------------------------------------------------------------------//
  {
    lineWrapItem.setSelected (prefs.getBoolean (PREFS_LINE_WRAP, true));
    showLayoutItem.setSelected (prefs.getBoolean (PREFS_SHOW_LAYOUT, true));
    showCatalogItem.setSelected (prefs.getBoolean (PREFS_SHOW_CATALOG, true));
    showFreeSectorsItem.setSelected (prefs.getBoolean (PREFS_SHOW_FREE_SECTORS, false));
    colourQuirksItem.setSelected (prefs.getBoolean (PREFS_COLOUR_QUIRKS, false));
    monochromeItem.setSelected (prefs.getBoolean (PREFS_MONOCHROME, false));

    switch (prefs.getInt (PREFS_SCALE, 2))
    {
      case 1:
        scale1Item.doClick ();
        break;
      case 2:
        scale2Item.doClick ();
        break;
      case 3:
        scale3Item.doClick ();
        break;
    }

    showHeaderItem.setSelected (prefs.getBoolean (PREFS_SHOW_HEADER, true));
    showAllFormatItem.setSelected (prefs.getBoolean (PREFS_SHOW_ALL_FORMAT, true));
    showAllXrefItem.setSelected (prefs.getBoolean (PREFS_SHOW_ALL_XREF, true));

    splitRemarkItem.setSelected (prefs.getBoolean (PREFS_SPLIT_REMARKS, false));
    splitDimItem.setSelected (prefs.getBoolean (PREFS_SPLIT_DIM, false));
    alignAssignItem.setSelected (prefs.getBoolean (PREFS_ALIGN_ASSIGN, true));
    showCaretItem.setSelected (prefs.getBoolean (PREFS_SHOW_CARET, false));
    showThenItem.setSelected (prefs.getBoolean (PREFS_SHOW_THEN, true));
    showXrefItem.setSelected (prefs.getBoolean (PREFS_SHOW_XREF, false));
    showCallsItem.setSelected (prefs.getBoolean (PREFS_SHOW_CALLS, false));
    showSymbolsItem.setSelected (prefs.getBoolean (PREFS_SHOW_SYMBOLS, false));
    showFunctionsItem.setSelected (prefs.getBoolean (PREFS_SHOW_FUNCTIONS, false));
    showConstantsItem.setSelected (prefs.getBoolean (PREFS_SHOW_CONSTANTS, false));
    showDuplicateSymbolsItem
        .setSelected (prefs.getBoolean (PREFS_SHOW_DUPLICATE_SYMBOLS, false));
    listStringsItem.setSelected (prefs.getBoolean (PREFS_LIST_STRINGS, false));
    showBasicTargetsItem.setSelected (prefs.getBoolean (PREFS_SHOW_TARGETS, false));
    onlyShowTargetLinesItem
        .setSelected (prefs.getBoolean (PREFS_ONLY_SHOW_TARGETS, false));
    blankAfterReturn.setSelected (prefs.getBoolean (PREFS_BLANK_AFTER_RETURN, false));
    deleteExtraRemSpace
        .setSelected (prefs.getBoolean (PREFS_DELETE_EXTRA_REM_SPACE, false));
    deleteExtraDataSpace
        .setSelected (prefs.getBoolean (PREFS_DELETE_EXTRA_DATA_SPACE, false));

    showAssemblerTargetsItem
        .setSelected (prefs.getBoolean (PREFS_SHOW_ASSEMBLER_TARGETS, true));
    showAssemblerStringsItem
        .setSelected (prefs.getBoolean (PREFS_SHOW_ASSEMBLER_STRINGS, true));
    showAssemblerHeaderItem
        .setSelected (prefs.getBoolean (PREFS_SHOW_ASSEMBLER_HEADER, true));

    prodosSortDirectoriesItem
        .setSelected (prefs.getBoolean (PREFS_PRODOS_SORT_DIRECTORIES, true));

    showTextOffsetsItem.setSelected (prefs.getBoolean (PREFS_TEXT_SHOW_OFFSETS, true));
    showTextHeaderItem.setSelected (prefs.getBoolean (PREFS_TEXT_SHOW_HEADER, true));

    setBasicPreferences ();
    setAssemblerPreferences ();
    setProdosPreferences ();
    setTextPreferences ();

    int paletteIndex = prefs.getInt (PREFS_PALETTE, 0);
    PaletteFactory paletteFactory = HiResImage.getPaletteFactory ();
    paletteFactory.setCurrentPalette (paletteIndex);
    Palette palette = paletteFactory.getCurrentPalette ();
    Enumeration<AbstractButton> enumeration = paletteGroup.getElements ();
    while (enumeration.hasMoreElements ())
    {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem) enumeration.nextElement ();
      if (item.getText ().equals (palette.getName ()))
      {
        item.setSelected (true);
        break;
      }
    }

    HiResImage.setDefaultColourQuirks (colourQuirksItem.isSelected ());
    HiResImage.setDefaultMonochrome (monochromeItem.isSelected ());
    AbstractFile.setDefaultDebug (debuggingItem.isSelected ());

    fontAction.restore (prefs);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void diskSelected (DiskSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    currentDisk = event.getFormattedDisk ();
    adjustMenus (currentDisk);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void fileSelected (FileSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    // This can happen if a file is selected from a hybrid disk
    if (event.appleFileSource.getFormattedDisk () != currentDisk)
    {
      currentDisk = event.appleFileSource.getFormattedDisk ();
      adjustMenus (currentDisk);
    }
  }

  // ---------------------------------------------------------------------------------//
  private void adjustMenus (final FormattedDisk disk)
  // ---------------------------------------------------------------------------------//
  {
    if (disk != null)
    {
      sector256Item.setSelected (disk.getDisk ().getBlockSize () == 256);
      sector512Item.setSelected (disk.getDisk ().getBlockSize () == 512);
      interleave0Item.setSelected (disk.getDisk ().getInterleave () == 0);
      interleave1Item.setSelected (disk.getDisk ().getInterleave () == 1);
      interleave2Item.setSelected (disk.getDisk ().getInterleave () == 2);
      interleave3Item.setSelected (disk.getDisk ().getInterleave () == 3);
    }

    boolean isDataDisk = (disk instanceof DataDisk);

    sector256Item.setEnabled (isDataDisk);
    sector512Item.setEnabled (isDataDisk);
    interleave0Item.setEnabled (isDataDisk);
    interleave1Item.setEnabled (isDataDisk);
    interleave2Item.setEnabled (isDataDisk);
    interleave3Item.setEnabled (isDataDisk);

    if (isDataDisk)
    {
      // make this an action too
      ActionListener sectorListener = new ActionListener ()
      {
        @Override
        public void actionPerformed (ActionEvent e)
        {
          int size = Integer.parseInt (e.getActionCommand ());
          disk.getDisk ().setBlockSize (size);
        }
      };

      sector256Item.addActionListener (sectorListener);
      sector512Item.addActionListener (sectorListener);

      ((InterleaveAction) interleave0Item.getAction ()).setDisk (currentDisk);
      ((InterleaveAction) interleave1Item.getAction ()).setDisk (currentDisk);
      ((InterleaveAction) interleave2Item.getAction ()).setDisk (currentDisk);
      ((InterleaveAction) interleave3Item.getAction ()).setDisk (currentDisk);
    }

    saveDiskItem.setEnabled (disk.isTempDisk ());
    saveTempFileAction.setDisk (disk);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void sectorSelected (SectorSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    //    List<DiskAddress> sectors = event.getSectors ();
  }
}