package com.bytezone.diskbrowser.duplicates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import com.bytezone.diskbrowser.gui.DuplicateAction;
import com.bytezone.diskbrowser.gui.DuplicateAction.DiskTableSelectionListener;
import com.bytezone.diskbrowser.utilities.NumberRenderer;

public class DuplicateWindow extends JFrame
{
  private final JTable table;

  private final JButton btnExport = new JButton ("Export");
  private final JButton btnHide = new JButton ("Close");

  private final List<DiskTableSelectionListener> listeners;

  public DuplicateWindow (File rootFolder,
      List<DuplicateAction.DiskTableSelectionListener> listeners)
  {
    super ("Duplicate Disk Detection - " + rootFolder.getAbsolutePath ());

    this.listeners = listeners;

    table = new JTable ();
    JScrollPane scrollPane =
        new JScrollPane (table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    table.setFillsViewportHeight (true);
    table.setAutoCreateRowSorter (true);

    table.setShowGrid (true);
    table.setGridColor (Color.LIGHT_GRAY);

    add (scrollPane, BorderLayout.CENTER);

    JPanel panel = new JPanel ();
    panel.add (btnHide);
    panel.add (btnExport);
    add (panel, BorderLayout.SOUTH);

    btnHide.setEnabled (true);
    btnExport.setEnabled (false);

    btnHide.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        DuplicateWindow.this.setVisible (false);
      }
    });

    setSize (1200, 700);
    setLocationRelativeTo (null);
    setDefaultCloseOperation (HIDE_ON_CLOSE);
  }

  // called from DuplicateSwingWorker
  public void setTableModel (DiskTableModel diskTableModel)
  {
    table.setModel (diskTableModel);

    int[] columnWidths = { 300, 300, 30, 40, 40, 40, 100 };
    TableColumnModel tcm = table.getColumnModel ();
    for (int i = 0; i < columnWidths.length; i++)
      tcm.getColumn (i).setPreferredWidth (columnWidths[i]);

    tcm.getColumn (3).setCellRenderer (NumberRenderer.getIntegerRenderer ());

    final TableRowSorter<DiskTableModel> sorter =
        new TableRowSorter<DiskTableModel> ((DiskTableModel) table.getModel ());
    table.setRowSorter (sorter);

    ListSelectionModel listSelectionModel = table.getSelectionModel ();
    listSelectionModel.addListSelectionListener (new ListSelectionListener ()
    {
      @Override
      public void valueChanged (ListSelectionEvent e)
      {
        if (e.getValueIsAdjusting ())
          return;

        ListSelectionModel lsm = (ListSelectionModel) e.getSource ();
        if (lsm.isSelectionEmpty ())
          return;

        table.scrollRectToVisible (
            new Rectangle (table.getCellRect (lsm.getMinSelectionIndex (), 0, true)));
        int selectedRow = table.getSelectedRow ();
        int actualRow = sorter.convertRowIndexToModel (selectedRow);

        DiskTableModel diskTableModel = (DiskTableModel) table.getModel ();
        DiskDetails diskDetails = diskTableModel.lines.get (actualRow).diskDetails;

        for (DiskTableSelectionListener listener : listeners)
          listener.diskSelected (diskDetails);
      }
    });

    JTableHeader header = table.getTableHeader ();
    header.setFont (header.getFont ().deriveFont ((float) 13.0));

    setVisible (true);
  }
}