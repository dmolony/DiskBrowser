package com.bytezone.diskbrowser.gui;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EventListener;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

import com.bytezone.diskbrowser.utilities.DefaultAction;

// -----------------------------------------------------------------------------------//
public class FontAction extends DefaultAction implements QuitListener
// -----------------------------------------------------------------------------------//
{
  private static final String prefsFontName = "prefsFontName";
  private static final String prefsFontSize = "prefsFontSize";
  private static Canvas canvas;

  private final EventListenerList listenerList = new EventListenerList ();

  private FontFrame frame;
  private String fontName;
  private String fontSize;
  private String text;

  // ---------------------------------------------------------------------------------//
  public FontAction ()
  // ---------------------------------------------------------------------------------//
  {
    super ("Set Font...", "Set display to a different font or font size",
        "/com/bytezone/loadlister/");

    int mask = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMaskEx ();
    putValue (Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke (KeyEvent.VK_F, mask));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void actionPerformed (ActionEvent e)
  // ---------------------------------------------------------------------------------//
  {
    if (frame == null)
    {
      frame = new FontFrame (this);
      frame.setSelectedValue (fontName);
      frame.setSelectedSize (fontSize);
      frame.setText (text);
    }
    frame.setVisible (true);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void quit (Preferences preferences)
  // ---------------------------------------------------------------------------------//
  {
    if (frame != null)
    {
      String fontName = frame.getSelectedValue ();
      preferences.put (prefsFontName, fontName == null ? "Monospaced" : fontName);
      String fontSize = frame.getSelectedSize ();
      preferences.put (prefsFontSize, fontSize == null ? "12 point" : fontSize);
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void restore (Preferences preferences)
  // ---------------------------------------------------------------------------------//
  {
    String fontName = preferences.get (prefsFontName, "Monospaced");
    if (fontName.isEmpty ())
      fontName = "Monospaced";

    this.fontName = fontName;
    if (frame != null)
      frame.setSelectedValue (fontName);

    String fontSize = preferences.get (prefsFontSize, "12 point");
    if (fontSize.isEmpty ())
      fontSize = "12 point";

    this.fontSize = fontSize;
    if (frame != null)
      frame.setSelectedSize (fontSize);

    int pos = fontSize.indexOf (' ');
    int size = Integer.parseInt (fontSize.substring (0, pos));
    fireFontChangeEvent (new Font (fontName, Font.PLAIN, size));
  }

  // ---------------------------------------------------------------------------------//
  public void setSampleText (String text)
  // ---------------------------------------------------------------------------------//
  {
    this.text = text;
    if (frame != null)
      frame.setText (text);
  }

  // ---------------------------------------------------------------------------------//
  public interface FontChangeListener extends EventListener
  // ---------------------------------------------------------------------------------//
  {
    public void changeFont (FontChangeEvent fontChangeEvent);
  }

  // ---------------------------------------------------------------------------------//
  public class FontChangeEvent
  // ---------------------------------------------------------------------------------//
  {
    public final Font font;
    public final FontMetrics fontMetrics;

    public FontChangeEvent (Font font)
    {
      this.font = font;
      if (canvas == null)
        canvas = new Canvas ();
      fontMetrics = canvas.getFontMetrics (font);
    }
  }

  // ---------------------------------------------------------------------------------//
  public void addFontChangeListener (FontChangeListener listener)
  // ---------------------------------------------------------------------------------//
  {
    listenerList.add (FontChangeListener.class, listener);
  }

  // ---------------------------------------------------------------------------------//
  public void removeFontChangeListener (FontChangeListener listener)
  // ---------------------------------------------------------------------------------//
  {
    listenerList.remove (FontChangeListener.class, listener);
  }

  // ---------------------------------------------------------------------------------//
  public void fireFontChangeEvent (Font font)
  // ---------------------------------------------------------------------------------//
  {
    FontChangeEvent fontChangeEvent = new FontChangeEvent (font);
    FontChangeListener[] listeners = (listenerList.getListeners (FontChangeListener.class));
    for (FontChangeListener listener : listeners)
      listener.changeFont (fontChangeEvent);
  }
}