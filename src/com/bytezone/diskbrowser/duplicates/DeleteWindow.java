package com.bytezone.diskbrowser.duplicates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import com.bytezone.diskbrowser.gui.DuplicateAction.DiskTableSelectionListener;

public class DeleteWindow extends JFrame implements DiskTableSelectionListener
{
  private List<DiskDetails> lines = new ArrayList<DiskDetails> ();
  private final JButton btnHide = new JButton ("Close");
  private final RootFolderData rootFolderData;

  private final DeleteTableModel deleteTableModel = new DeleteTableModel ();
  private final JTable table = new JTable (deleteTableModel);

  public DeleteWindow (RootFolderData rootFolderData)
  {
    super ("Duplicate Disks");

    JScrollPane scrollPane =
        new JScrollPane (table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    table.setFillsViewportHeight (true);

    table.setShowGrid (true);
    table.setGridColor (Color.LIGHT_GRAY);

    add (scrollPane, BorderLayout.CENTER);

    JPanel panel = new JPanel ();
    panel.add (btnHide);
    add (panel, BorderLayout.SOUTH);

    btnHide.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        setVisible (false);
      }
    });

    scrollPane.setPreferredSize (new Dimension (700, 400));
    setDefaultCloseOperation (HIDE_ON_CLOSE);

    this.rootFolderData = rootFolderData;
    rootFolderData.listeners.add (this);

    int[] columnWidths = { 400, 30, 70, 100 };
    TableColumnModel tcm = table.getColumnModel ();
    for (int i = 0; i < columnWidths.length; i++)
      tcm.getColumn (i).setPreferredWidth (columnWidths[i]);

    pack ();
    setLocationRelativeTo (null);
  }

  @Override
  public void diskSelected (DiskDetails diskDetails)
  {
    lines = rootFolderData.listDuplicates (diskDetails.getChecksum ());
    deleteTableModel.fireTableDataChanged ();
  }

  class DeleteTableModel extends AbstractTableModel
  {
    final String[] headers = { "Name", "Type", "Size", "Checksum", };

    @Override
    public int getRowCount ()
    {
      return lines.size ();
    }

    @Override
    public int getColumnCount ()
    {
      return headers.length;
    }

    @Override
    public String getColumnName (int column)
    {
      return headers[column];
    }

    @Override
    public Object getValueAt (int rowIndex, int columnIndex)
    {
      DiskDetails diskDetails = lines.get (rowIndex);
      switch (columnIndex)
      {
        case 0:
          return diskDetails.getRootName () + File.separator + diskDetails.getFileName ();
        case 1:
          return diskDetails.getType ();
        case 2:
          return diskDetails.getSize ();
        case 3:
          return diskDetails.getChecksum ();
        default:
          return "?";
      }
    }

    @Override
    public Class<?> getColumnClass (int columnIndex)
    {
      return lines.isEmpty () ? Object.class : getValueAt (0, columnIndex).getClass ();
    }
  }
}