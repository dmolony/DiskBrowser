package com.bytezone.diskbrowser.utilities;

import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

// -----------------------------------------------------------------------------------//
public abstract class DefaultAction extends AbstractAction
// -----------------------------------------------------------------------------------//
{
  final String baseURL;

  // ---------------------------------------------------------------------------------//
  public DefaultAction (String text, String tip)
  // ---------------------------------------------------------------------------------//
  {
    super (text);

    this.baseURL = null;
    putValue (Action.SHORT_DESCRIPTION, tip);
  }

  // ---------------------------------------------------------------------------------//
  public DefaultAction (String text, String tip, String baseURL)
  // ---------------------------------------------------------------------------------//
  {
    super (text);

    this.baseURL = baseURL;
    putValue (Action.SHORT_DESCRIPTION, tip);
  }

  // ---------------------------------------------------------------------------------//
  protected void setIcon (String iconType, String iconName)
  // ---------------------------------------------------------------------------------//
  {
    if (baseURL == null)
    {
      System.out.println ("Base URL not set");
      return;
    }

    URL url = this.getClass ().getResource (baseURL + iconName);
    if (url != null)
      putValue (iconType, new ImageIcon (url));
  }
}