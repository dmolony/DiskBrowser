package com.bytezone.diskbrowser.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.bytezone.input.ButtonPanel;
import com.bytezone.input.ColumnPanel;
import com.bytezone.input.InputPanel;
import com.bytezone.input.RadioButtonPanel;
import com.bytezone.input.RowPanel;
import com.bytezone.input.ScrollPanel;
import com.bytezone.input.TextAreaPanel;

// -----------------------------------------------------------------------------------//
public class FontFrame extends JFrame
// -----------------------------------------------------------------------------------//
{
  private final JList<String> fontList =
      new JList<String> (new DefaultListModel<String> ());
  private final FontAction fontAction;

  private String initialFont;
  private String initialSize;

  private RadioButtonPanel fontSizePanel;
  private TextAreaPanel textPanel;
  private JButton btnCancel;
  private JButton btnOK;
  private JButton btnApply;

  // ---------------------------------------------------------------------------------//
  public FontFrame (FontAction fontAction)
  // ---------------------------------------------------------------------------------//
  {
    super ("Font Selection");

    this.fontAction = fontAction;
    buildLayout ();
    getFonts ();
    setListeners ();
  }

  // ---------------------------------------------------------------------------------//
  private void buildLayout ()
  // ---------------------------------------------------------------------------------//
  {
    fontList.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
    JScrollPane sp = new JScrollPane (fontList);
    sp.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    ScrollPanel listPanel = new ScrollPanel (sp, 200, 300);     // width, height

    fontSizePanel = new RadioButtonPanel (
        new String[][] { { "6 point", "7 point", "8 point", "9 point", "10 point",
                           "11 point", "12 point", "13 point", "14 point", "15 point",
                           "16 point", "18 point", } });

    InputPanel.setTextLength (80);
    textPanel = new TextAreaPanel (10);

    ButtonPanel buttonPanel = new ButtonPanel (new String[] { "Cancel", "OK", "Apply" });
    btnCancel = buttonPanel.getItem (0);
    btnOK = buttonPanel.getItem (1);
    btnApply = buttonPanel.getItem (2);

    RowPanel rp = new RowPanel (new RowPanel ("Font", listPanel),
        new RowPanel ("Size", fontSizePanel), new RowPanel ("Sample code", textPanel));

    add (new ColumnPanel (rp, buttonPanel));

    pack ();
    setResizable (false);
    setLocationRelativeTo (null);
  }

  // ---------------------------------------------------------------------------------//
  public void setText (String text)
  // ---------------------------------------------------------------------------------//
  {
    textPanel.getItem (0).setText (text);
  }

  // ---------------------------------------------------------------------------------//
  public String getSelectedValue ()
  // ---------------------------------------------------------------------------------//
  {
    return fontList.getSelectedValue ();
  }

  // ---------------------------------------------------------------------------------//
  public void setSelectedValue (String fontName)
  // ---------------------------------------------------------------------------------//
  {
    fontList.setSelectedValue (fontName, true);
    initialFont = fontName;
  }

  // ---------------------------------------------------------------------------------//
  public String getSelectedSize ()
  // ---------------------------------------------------------------------------------//
  {
    return fontSizePanel.getSelectedText ();
  }

  // ---------------------------------------------------------------------------------//
  public void setSelectedSize (String fontSize)
  // ---------------------------------------------------------------------------------//
  {
    fontSizePanel.setSelected (fontSize);
    initialSize = fontSize;
  }

  // ---------------------------------------------------------------------------------//
  private void getFonts ()
  // ---------------------------------------------------------------------------------//
  {
    String fonts[] =
        GraphicsEnvironment.getLocalGraphicsEnvironment ().getAvailableFontFamilyNames ();
    //    for (String font : fonts)
    //      System.out.println (font);
    String pf[] =
        { "Andale Mono", "Anonymous Pro", "Anonymous Pro Minus", "Apple II Display Pro",
          "Apple II Pro", "Apple2Forever", "Apple2Forever80", "Bitstream Vera Sans Mono",
          "Consolas", "Courier", "Courier New", "DejaVu Sans Mono", "Envy Code R",
          "Inconsolata", "Input Mono", "Input Mono Narrow", "Iosevka",
          "Lucida Sans Typewriter", "Luculent", "Menlo", "Monaco", "monofur",
          "Monospaced", "Nimbus Mono L", "PCMyungjo", "PR Number 3", "Pragmata",
          "Print Char 21", "ProFont", "ProFontX", "Proggy", "PT Mono", "Source Code Pro",
          "Ubuntu Mono" };

    DefaultListModel<String> lm = (DefaultListModel<String>) fontList.getModel ();

    int ptr = 0;
    for (String fontName : fonts)
      while (ptr < pf.length)
      {
        int result = fontName.compareToIgnoreCase (pf[ptr]);
        if (result >= 0)
        {
          ++ptr;
          if (result > 0)
            continue;
          lm.addElement (fontName);
        }
        break;
      }

    fontList.setSelectedValue (initialFont, true);
  }

  // ---------------------------------------------------------------------------------//
  private void setListeners ()
  // ---------------------------------------------------------------------------------//
  {
    addComponentListener (new ComponentAdapter ()
    {
      @Override
      public void componentShown (ComponentEvent e)
      {
        if (fontList.getModel ().getSize () == 0)
          getFonts ();

        initialFont = getSelectedValue ();
        initialSize = getSelectedSize ();
      }

      @Override
      public void componentHidden (ComponentEvent e)
      {
      }
    });

    fontList.addListSelectionListener (new ListSelectionListener ()
    {
      @Override
      public void valueChanged (ListSelectionEvent e)
      {
        if (e.getValueIsAdjusting ())
          return;
      }
    });

    btnCancel.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        setVisible (false);
        setSelectedValue (initialFont);
        setSelectedSize (initialSize);
      }
    });

    btnOK.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        setVisible (false);
        setSelection ();
      }
    });

    btnApply.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        setSelection ();
      }
    });

    fontList.addListSelectionListener (new ListSelectionListener ()
    {
      @Override
      public void valueChanged (ListSelectionEvent e)
      {
        if (e.getValueIsAdjusting ())
          return;

        Font font = getCurrentFont ();
        if (font != null)
          textPanel.getItem (0).setFont (font);
      }
    });

    fontSizePanel.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        textPanel.getItem (0).setFont (getCurrentFont ());
      }
    });
  }

  // ---------------------------------------------------------------------------------//
  private Font getCurrentFont ()
  // ---------------------------------------------------------------------------------//
  {
    String fontSize = getSelectedSize ();
    if (fontSize.isEmpty ())
      return null;

    int pos = fontSize.indexOf (' ');
    int size = Integer.parseInt (fontSize.substring (0, pos));

    return new Font (getSelectedValue (), Font.PLAIN, size);
  }

  // ---------------------------------------------------------------------------------//
  private void setSelection ()
  // ---------------------------------------------------------------------------------//
  {
    initialFont = getSelectedValue ();
    initialSize = getSelectedSize ();

    int pos = initialSize.indexOf (' ');
    int size = Integer.parseInt (initialSize.substring (0, pos));

    Font font = new Font (initialFont, Font.PLAIN, size);
    fontAction.fireFontChangeEvent (font);
  }
}