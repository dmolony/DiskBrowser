package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.bytezone.common.DefaultAction;

public class AboutAction extends DefaultAction
{
  public AboutAction ()
  {
    super ("About...", "Display build information", "/com/bytezone/diskbrowser/icons/");
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke ("alt A"));
    putValue (Action.MNEMONIC_KEY, KeyEvent.VK_A);

    setIcon (Action.SMALL_ICON, "information_16.png");
    setIcon (Action.LARGE_ICON_KEY, "information_32.png");
  }

  @Override
  public void actionPerformed (ActionEvent e)
  {
    about ();
  }

  public void about ()
  {
    //    int build = 0;
    //    String buildDate = "<no date>";
    //    Properties props = new Properties ();
    //    InputStream in = this.getClass ().getResourceAsStream ("build.properties");
    //    if (in != null)
    //    {
    //      try
    //      {
    //        props.load (in);
    //        in.close ();
    //        build = Integer.parseInt (props.getProperty ("build.number"));
    //        buildDate = props.getProperty ("build.date");
    //      }
    //      catch (IOException e1)
    //      {
    //        System.out.println ("Properties file not found");
    //      }
    //    }

    JOptionPane.showMessageDialog (null, "Author - Denis Molony"       //
        //        + "\nBuild #" + String.format ("%d", build) + " - " + buildDate + "\n"      //
        + "\nGitHub - https://github.com/dmolony/DiskBrowser",         //
        //        + "\nContact - dmolony@iinet.net.au",         //
        "About DiskBrowser", JOptionPane.INFORMATION_MESSAGE);
  }
}