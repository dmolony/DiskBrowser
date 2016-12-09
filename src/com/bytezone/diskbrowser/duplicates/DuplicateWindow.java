package com.bytezone.diskbrowser.duplicates;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

public class DuplicateWindow extends JFrame
{
  private final JTable table;
  int folderNameLength;
  Map<String, List<DiskDetails>> duplicateDisks;
  File rootFolder;

  JButton buttonDelete = new JButton ("Delete selected");
  JButton buttonCancel = new JButton ("Cancel");
  JButton buttonAll = new JButton ("Select all duplicates");
  JButton buttonClear = new JButton ("Clear all");
  //  JPanel mainPanel = new JPanel ();

  List<DiskDetails> disksSelected = new ArrayList<DiskDetails> ();

  DuplicateHandler duplicateHandler;

  public DuplicateWindow (File rootFolder)
  {
    super ("Duplicate Disk Detection - " + rootFolder.getAbsolutePath ());

    folderNameLength = rootFolder.getAbsolutePath ().length ();

    table = new JTable ();
    JScrollPane scrollPane =
        new JScrollPane (table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    table.setFillsViewportHeight (true);

    //    table.setShowGrid (true);
    //    table.setGridColor (Color.BLACK);

    add (scrollPane, BorderLayout.CENTER);

    JPanel panel = new JPanel ();
    panel.add (buttonClear);
    panel.add (buttonAll);
    panel.add (buttonDelete);
    panel.add (buttonCancel);
    add (panel, BorderLayout.SOUTH);

    buttonClear.setEnabled (false);
    buttonAll.setEnabled (false);
    buttonDelete.setEnabled (false);
    buttonCancel.setEnabled (false);

    buttonAll.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        //        for (DuplicatePanel dp : duplicatePanels)
        //        {
        //          int count = 0;
        //          for (JCheckBox cb : dp.checkBoxes)
        //          {
        //            if (count > 0 && dp.duplicateDisks.get (count).isDuplicate ())
        //              if (!cb.isSelected ())
        //              {
        //                cb.setSelected (true);              // doesn't fire the actionListener!
        //                disksSelected.add (dp.duplicateDisks.get (count));
        //              }
        //            ++count;
        //          }
        //        }
        buttonDelete.setEnabled (disksSelected.size () > 0);
        buttonClear.setEnabled (disksSelected.size () > 0);
      }
    });

    buttonClear.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        //        for (DuplicatePanel dp : duplicatePanels)
        //          for (JCheckBox cb : dp.checkBoxes)
        //            cb.setSelected (false);                 // doesn't fire the actionListener!

        disksSelected.clear ();
        buttonDelete.setEnabled (false);
        buttonClear.setEnabled (false);
      }
    });

    buttonCancel.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        DuplicateWindow.this.setVisible (false);
      }
    });

    buttonDelete.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        int totalDeleted = 0;
        int totalFailed = 0;

        //        for (DuplicatePanel dp : duplicatePanels)
        //        {
        //          int count = 0;
        //          for (JCheckBox cb : dp.checkBoxes)
        //          {
        //            if (cb.isSelected ())
        //            {
        //              DiskDetails dd = dp.duplicateDisks.get (count);
        //              if (dd.delete ())
        //              {
        //                ++totalDeleted;
        //                System.out.println ("Deleted : " + dd);
        //              }
        //              else
        //              {
        //                ++totalFailed;
        //                System.out.println ("Failed  : " + dd);
        //              }
        //            }
        //            ++count;
        //          }
        //        }
        System.out.printf ("Deleted : %d, Failed : %d%n", totalDeleted, totalFailed);
      }
    });

    setSize (900, 700);
    setLocationRelativeTo (null);
    setDefaultCloseOperation (HIDE_ON_CLOSE);
  }

  public void setDuplicateHandler (DuplicateHandler duplicateHandler)
  {
    this.duplicateHandler = duplicateHandler;
    table.setModel (new DiskTableModel (duplicateHandler));
    table.getColumnModel ().getColumn (0).setPreferredWidth (250);
    table.getColumnModel ().getColumn (1).setPreferredWidth (500);
    table.getColumnModel ().getColumn (2).setPreferredWidth (100);
    setVisible (true);
  }
}