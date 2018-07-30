package com.bytezone.diskbrowser.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.bytezone.common.FontTester;
import com.bytezone.input.SpringUtilities;

class PreferencesDialog extends JDialog
{
  static final String prefsCatalogFont = "CatalogFont";
  static final String prefsDataFont = "DataFont";
  static final String prefsCatalogFontSize = "CatalogFontSize";
  static final String prefsDataFontSize = "DataFontSize";

  static final String defaultFontName = "Lucida Sans Typewriter";
  static final int defaultFontSize = 12;
  static final String[] monoFonts = new FontTester ().getMonospacedFontList ();

  private final JComboBox<String> catalogFontList = new JComboBox<String> (monoFonts);
  private final JComboBox<String> dataFontList = new JComboBox<String> (monoFonts);
  private final String[] sizes =
      { "8", "9", "10", "11", "12", "13", "14", "15", "16", "18" };
  private final JComboBox<String> catalogFontSizes = new JComboBox<String> (sizes);
  private final JComboBox<String> dataFontSizes = new JComboBox<String> (sizes);
  private final Preferences prefs;

  private final JButton apply = new JButton ("Apply");

  private String catalogFontName;
  private String dataFontName;
  private int catalogFontSize;
  private int dataFontSize;

  public PreferencesDialog (JFrame owner, Preferences prefs)
  {
    super (owner, "Set Preferences", false);

    this.prefs = prefs;
    System.out.println ("********* not used ***********");

    catalogFontName = prefs.get (prefsCatalogFont, defaultFontName);
    dataFontName = prefs.get (prefsDataFont, defaultFontName);
    catalogFontSize = prefs.getInt (prefsCatalogFontSize, defaultFontSize);
    dataFontSize = prefs.getInt (prefsDataFontSize, defaultFontSize);

    catalogFontList.setSelectedItem (catalogFontName);
    dataFontList.setSelectedItem (dataFontName);
    catalogFontSizes.setSelectedItem (catalogFontSize + "");
    dataFontSizes.setSelectedItem (dataFontSize + "");

    catalogFontList.setMaximumRowCount (30);
    dataFontList.setMaximumRowCount (30);
    catalogFontSizes.setMaximumRowCount (sizes.length);
    dataFontSizes.setMaximumRowCount (sizes.length);

    Listener listener = new Listener ();
    catalogFontList.addActionListener (listener);
    dataFontList.addActionListener (listener);
    catalogFontSizes.addActionListener (listener);
    dataFontSizes.addActionListener (listener);

    setDefaultCloseOperation (DISPOSE_ON_CLOSE);
    setResizable (false);
    addCancelByEscapeKey (); // doesn't seem to work

    JPanel layoutPanel = new JPanel ();
    layoutPanel.setBorder (new EmptyBorder (10, 20, 0, 20)); // T/L/B/R
    layoutPanel.setLayout (new SpringLayout ());

    layoutPanel.add (new JLabel ("Catalog panel font", JLabel.TRAILING));
    layoutPanel.add (catalogFontList);
    layoutPanel.add (catalogFontSizes);

    layoutPanel.add (new JLabel ("Output panel font", JLabel.TRAILING));
    layoutPanel.add (dataFontList);
    layoutPanel.add (dataFontSizes);

    SpringUtilities.makeCompactGrid (layoutPanel, 2, 3, //rows, cols
        10, 5, //initX, initY
        10, 5); //xPad, yPad

    JPanel panel = new JPanel (new BorderLayout ());
    panel.add (layoutPanel, BorderLayout.CENTER);
    panel.add (getCommandPanel (), BorderLayout.SOUTH);
    getContentPane ().add (panel);

    pack ();
    setLocationRelativeTo (owner);
    setVisible (true);
  }

  private JComponent getCommandPanel ()
  {
    JButton cancel = new JButton ("Cancel");
    cancel.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent event)
      {
        closeDialog ();
      }
    });

    apply.setEnabled (false);
    apply.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent event)
      {
        updatePreferences ();
        apply.setEnabled (false);
      }
    });

    JButton ok = new JButton ("OK");
    getRootPane ().setDefaultButton (ok);
    ok.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent event)
      {
        updatePreferences ();
        closeDialog ();
      }
    });

    JPanel commandPanel = new JPanel ();
    commandPanel.add (cancel);
    commandPanel.add (apply);
    commandPanel.add (ok);

    return commandPanel;
  }

  private void updatePreferences ()
  {
    String newFontName = (String) catalogFontList.getSelectedItem ();
    if (!newFontName.equals (catalogFontName))
    {
      prefs.put (prefsCatalogFont, newFontName);
      catalogFontName = newFontName;
    }

    newFontName = (String) dataFontList.getSelectedItem ();
    if (!newFontName.equals (dataFontName))
    {
      prefs.put (prefsDataFont, newFontName);
      dataFontName = newFontName;
    }

    int newFontSize = Integer.parseInt ((String) catalogFontSizes.getSelectedItem ());
    if (newFontSize != catalogFontSize)
    {
      prefs.putInt (prefsCatalogFontSize, newFontSize);
      catalogFontSize = newFontSize;
    }

    newFontSize = Integer.parseInt ((String) dataFontSizes.getSelectedItem ());
    if (newFontSize != dataFontSize)
    {
      prefs.putInt (prefsDataFontSize, newFontSize);
      dataFontSize = newFontSize;
    }
  }

  private void addCancelByEscapeKey ()
  {
    String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
    int noModifiers = 0;
    KeyStroke escapeKey = KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE, noModifiers, false);
    InputMap inputMap =
        getRootPane ().getInputMap (JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    inputMap.put (escapeKey, CANCEL_ACTION_KEY);
    AbstractAction cancelAction = new AbstractAction ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        closeDialog ();
      }
    };
    getRootPane ().getActionMap ().put (CANCEL_ACTION_KEY, cancelAction);
  }

  private void closeDialog ()
  {
    dispose ();
  }

  class Listener implements ActionListener
  {
    @Override
    public void actionPerformed (ActionEvent e)
    {
      apply.setEnabled (true);
    }
  }
}