package com.bytezone.diskbrowser.gui;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import com.bytezone.common.DefaultAction;
import com.bytezone.diskbrowser.duplicates.DiskDetails;
import com.bytezone.diskbrowser.gui.RootDirectoryAction.RootDirectoryListener;
import com.bytezone.input.SpringUtilities;

public class DuplicateAction extends DefaultAction implements RootDirectoryListener
{
  Map<String, List<DiskDetails>> duplicateDisks;
  int rootFolderLength;
  File rootFolder;
  DuplicateWindow window;

  public DuplicateAction ()
  {
    super ("Check for duplicates...", "Check for duplicate disks",
        "/com/bytezone/diskbrowser/icons/");

    setIcon (Action.SMALL_ICON, "save_delete_16.png");
    setIcon (Action.LARGE_ICON_KEY, "save_delete_32.png");
  }

  //  public void setDuplicates (File rootFolder,
  //      Map<String, List<DiskDetails>> duplicateDisks)
  //  {
  //    this.duplicateDisks = duplicateDisks;
  //    this.rootFolderLength = rootFolder.getAbsolutePath ().length ();
  //    setEnabled (duplicateDisks.size () > 0);
  //  }

  @Override
  public void rootDirectoryChanged (File newRootDirectory)
  {
    this.rootFolder = newRootDirectory;
    System.out.println ("gotcha");
  }

  @Override
  public void actionPerformed (ActionEvent arg0)
  {
    if (duplicateDisks == null)
    {
      System.out.println ("No duplicate disks found");
      return;
    }

    if (window != null)
    {
      window.setVisible (true);
      return;
    }
    window = new DuplicateWindow ();
    for (List<DiskDetails> diskList : duplicateDisks.values ())
      new DuplicateWorker (diskList, window).execute ();
  }

  class DuplicateWindow extends JFrame
  {
    int unfinishedWorkers;
    int folderNameLength;

    JButton buttonDelete = new JButton ("Delete selected");
    JButton buttonCancel = new JButton ("Cancel");
    JButton buttonAll = new JButton ("Select all duplicates");
    JButton buttonClear = new JButton ("Clear all");
    JPanel mainPanel = new JPanel ();

    List<DiskDetails> disksSelected = new ArrayList<DiskDetails> ();
    List<DuplicatePanel> duplicatePanels =
        new ArrayList<DuplicateAction.DuplicatePanel> ();

    public DuplicateWindow ()
    {
      super ("Duplicate Disk Detection - " + rootFolder.getAbsolutePath ());
      unfinishedWorkers = duplicateDisks.size ();
      folderNameLength = rootFolder.getAbsolutePath ().length ();

      mainPanel.setLayout (new BoxLayout (mainPanel, BoxLayout.PAGE_AXIS));

      JScrollPane sp = new JScrollPane (mainPanel, VERTICAL_SCROLLBAR_ALWAYS,
          HORIZONTAL_SCROLLBAR_NEVER);
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
                  cb.setSelected (true); // doesn't fire the actionListener!
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
              cb.setSelected (false); // doesn't fire the actionListener!
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

  class DuplicatePanel extends JPanel
  {
    List<JCheckBox> checkBoxes = new ArrayList<JCheckBox> ();
    List<DiskDetails> duplicateDisks;

    public DuplicatePanel (List<DiskDetails> duplicateDisks, int folderNameLength,
        List<DiskDetails> disksSelected, JButton deleteButton, JButton clearButton)
    {
      this.duplicateDisks = duplicateDisks;
      setLayout (new SpringLayout ());
      setAlignmentX (LEFT_ALIGNMENT);

      int count = 0;
      for (DiskDetails dd : duplicateDisks)
      {
        JCheckBox cb = new JCheckBox ();
        checkBoxes.add (cb);

        cb.addActionListener (
            new CheckBoxActionListener (dd, disksSelected, deleteButton, clearButton));
        add (cb);
        if (++count == 1)
          add (new JLabel ("Source disk"));
        else
        {
          String text = dd.isDuplicate () ? "Duplicate" : "OK";
          add (new JLabel (text));
        }
        String checksum = dd.isDuplicate () || count == 1 ? ""
            : " (checksum = " + dd.getChecksum () + ")";
        add (new JLabel (dd.getAbsolutePath ().substring (folderNameLength) + checksum));
      }
      SpringUtilities.makeCompactGrid (this, duplicateDisks.size (), 3, //rows, cols
          10, 0, //initX, initY
          10, 0); //xPad, yPad
    }
  }

  class CheckBoxActionListener implements ActionListener
  {
    DiskDetails diskDetails;
    List<DiskDetails> disksSelected;
    JButton deleteButton;
    JButton clearButton;

    public CheckBoxActionListener (DiskDetails diskDetails,
        List<DiskDetails> disksSelected, JButton deleteButton, JButton clearButton)
    {
      this.diskDetails = diskDetails;
      this.disksSelected = disksSelected;
      this.deleteButton = deleteButton;
      this.clearButton = clearButton;
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {
      if (((JCheckBox) e.getSource ()).isSelected ())
        disksSelected.add (diskDetails);
      else
        disksSelected.remove (diskDetails);
      deleteButton.setEnabled (disksSelected.size () > 0);
      clearButton.setEnabled (disksSelected.size () > 0);
    }
  }

  class DuplicateWorker extends SwingWorker<List<DiskDetails>, Void>
  {
    List<DiskDetails> duplicateDisks;
    DuplicateWindow owner;

    public DuplicateWorker (List<DiskDetails> duplicateDisks, DuplicateWindow owner)
    {
      this.duplicateDisks = duplicateDisks;
      this.owner = owner;
    }

    @Override
    protected void done ()
    {
      try
      {
        owner.addResult (get ());
      }
      catch (Exception e)
      {
        e.printStackTrace ();
      }
    }

    @Override
    protected List<DiskDetails> doInBackground () throws Exception
    {
      long firstChecksum = -1;
      for (DiskDetails dd : duplicateDisks)
      {
        if (firstChecksum < 0)
          firstChecksum = dd.getChecksum ();
        else
          dd.setDuplicate (dd.getChecksum () == firstChecksum);
      }
      return duplicateDisks;
    }
  }
}