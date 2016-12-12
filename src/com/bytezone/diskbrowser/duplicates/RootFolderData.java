package com.bytezone.diskbrowser.duplicates;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.bytezone.diskbrowser.gui.DuplicateAction.DiskTableSelectionListener;

public class RootFolderData
{
  public File rootFolder;

  // list of checksum -> DiskDetails
  public final Map<Long, DiskDetails> checksumMap = new HashMap<Long, DiskDetails> ();

  // list of unique disk names -> DiskDetails
  public final Map<String, DiskDetails> fileNameMap = new TreeMap<String, DiskDetails> ();

  public final ProgressState progressState = new ProgressState ();
  public final ProgressPanel progressPanel;

  public DuplicateWindow window;

  public final List<DiskTableSelectionListener> listeners =
      new ArrayList<DiskTableSelectionListener> ();

  public boolean doChecksums;
  public boolean showTotals;

  public JDialog dialog;

  public RootFolderData ()
  {
    dialog = new JDialog (window);
    progressPanel = new ProgressPanel ();
    progressPanel.setPreferredSize (new Dimension (485, 300));
    dialog.add (progressPanel);
    dialog.setTitle ("Disk Totals");
    dialog.pack ();
  }

  class ProgressPanel extends JPanel
  {
    @Override
    protected void paintComponent (Graphics graphics)
    {
      super.paintComponent (graphics);
      progressState.paintComponent (graphics);
    }
  }
}