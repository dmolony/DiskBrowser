package com.bytezone.diskbrowser.utilities;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

// -----------------------------------------------------------------------------------//
public class EnvironmentAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  // ---------------------------------------------------------------------------------//
  public EnvironmentAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Environment...");

    putValue (Action.SHORT_DESCRIPTION, "Display java details");
    int mask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMaskEx ();
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_E, mask));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    TextFormatter textFormatter = new TextFormatter ();
    textFormatter.addLine ("Java version", System.getProperty ("java.runtime.version"));
    textFormatter.addLine ();

    String path = System.getProperty ("java.class.path");
    for (String s : path.split (File.pathSeparator))
      textFormatter.addLine ("Classpath", s);

    JOptionPane.showMessageDialog (null, textFormatter.toLabel (), "Java Environment",
        JOptionPane.INFORMATION_MESSAGE);
  }

  // ---------------------------------------------------------------------------------//
  class TextFormatter
  // ---------------------------------------------------------------------------------//
  {
    List<String> titles = new ArrayList<> ();
    List<String> texts = new ArrayList<> ();

    void addLine (String title, String text)
    {
      titles.add (title);
      texts.add (text);
    }

    void addLine ()
    {
      addLine ("", "");
    }

    JLabel toLabel ()
    {
      StringBuilder text = new StringBuilder ("<html>");
      for (int i = 0; i < texts.size (); i++)
      {
        String title = titles.get (i);
        if (title.length () == 0)
          text.append ("<br>");
        else
          text.append (String.format ("%s : %s<br>", title, texts.get (i)));
      }
      text.append ("</html>");

      JLabel label = new JLabel (text.toString ());
      label.setFont (new Font ("Monospaced", Font.PLAIN, 13));
      return label;
    }
  }
}