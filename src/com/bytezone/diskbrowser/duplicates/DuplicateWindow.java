package com.bytezone.diskbrowser.duplicates;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

public class DuplicateWindow extends JFrame
{
  int unfinishedWorkers;
  int folderNameLength;
  Map<String, List<DiskDetails>> duplicateDisks;
  File rootFolder;

  JButton buttonDelete = new JButton ("Delete selected");
  JButton buttonCancel = new JButton ("Cancel");
  JButton buttonAll = new JButton ("Select all duplicates");
  JButton buttonClear = new JButton ("Clear all");
  JPanel mainPanel = new JPanel ();

  List<DiskDetails> disksSelected = new ArrayList<DiskDetails> ();
  List<DuplicatePanel> duplicatePanels = new ArrayList<DuplicatePanel> ();

  DuplicateHandler duplicateHandler;

  public DuplicateWindow (File rootFolder)
  {
    super ("Duplicate Disk Detection - " + rootFolder.getAbsolutePath ());

    duplicateHandler = new DuplicateHandler (rootFolder);
    Map<String, List<DiskDetails>> duplicateDisks = duplicateHandler.getDuplicateDisks ();
    for (List<DiskDetails> diskList : duplicateDisks.values ())
      new DuplicateWorker (diskList, this).execute ();

    unfinishedWorkers = duplicateDisks.size ();
    folderNameLength = rootFolder.getAbsolutePath ().length ();

    mainPanel.setLayout (new BoxLayout (mainPanel, BoxLayout.PAGE_AXIS));

    JScrollPane sp =
        new JScrollPane (mainPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    sp.getVerticalScrollBar ().setUnitIncrement (100);
    add (sp, BorderLayout.CENTER);

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
        for (DuplicatePanel dp : duplicatePanels)
        {
          int count = 0;
          for (JCheckBox cb : dp.checkBoxes)
          {
            if (count > 0 && dp.duplicateDisks.get (count).isDuplicate ())
              if (!cb.isSelected ())
              {
                cb.setSelected (true);              // doesn't fire the actionListener!
                disksSelected.add (dp.duplicateDisks.get (count));
              }
            ++count;
          }
        }
        buttonDelete.setEnabled (disksSelected.size () > 0);
        buttonClear.setEnabled (disksSelected.size () > 0);
      }
    });

    buttonClear.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        for (DuplicatePanel dp : duplicatePanels)
          for (JCheckBox cb : dp.checkBoxes)
            cb.setSelected (false);                 // doesn't fire the actionListener!

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
        for (DuplicatePanel dp : duplicatePanels)
        {
          int count = 0;
          for (JCheckBox cb : dp.checkBoxes)
          {
            if (cb.isSelected () && false)
            {
              DiskDetails dd = dp.duplicateDisks.get (count);
              if (dd.delete ())
              {
                ++totalDeleted;
                System.out.println ("Deleted : " + dd);
              }
              else
              {
                ++totalFailed;
                System.out.println ("Failed  : " + dd);
              }
            }
            ++count;
          }
        }
        System.out.printf ("Deleted : %d, Failed : %d%n", totalDeleted, totalFailed);
      }
    });

    setSize (600, 700);
    setLocationRelativeTo (null);
    setDefaultCloseOperation (HIDE_ON_CLOSE);
    setVisible (true);
  }

  // create a DuplicatePanel based on the updated DiskDetails
  public synchronized void addResult (List<DiskDetails> duplicateDisks)
  {
    // create panel and add it to the window
    DuplicatePanel dp = new DuplicatePanel (duplicateDisks, folderNameLength,
        disksSelected, buttonDelete, buttonClear);
    mainPanel.add (dp);
    duplicatePanels.add (dp);

    validate ();

    if (--unfinishedWorkers == 0)
    {
      buttonAll.setEnabled (true);
      buttonCancel.setEnabled (true);
    }
    else
      mainPanel.add (Box.createRigidArea (new Dimension (0, 20)));
  }
}
