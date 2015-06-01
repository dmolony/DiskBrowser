package com.bytezone.diskbrowser.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Enumeration;
import java.util.Vector;

class PrintDocument extends Component implements Printable
{
  String lines[];
  int lineHeight;
  int pages;
  Font font = new Font ("Lucida Sans Typewriter", Font.PLAIN, 7);
  int linesPerPage;
  int x = 50;
  int y = 20;

  public PrintDocument (String text)
  {
    lines = wrapText (text, 112);
  }

  public int print (Graphics g, PageFormat pageFormat, int page)
  {
    Graphics2D g2 = (Graphics2D) g;
    if (lineHeight == 0)
    {
      LineMetrics lm = font.getLineMetrics ("0", g2.getFontRenderContext ());
      lineHeight = (int) lm.getHeight ();
      linesPerPage = (int) pageFormat.getImageableHeight () / lineHeight - 5;
      pages = (lines.length - 1) / linesPerPage;
    }

    if (pages < page)
      return Printable.NO_SUCH_PAGE;

    g2.translate (pageFormat.getImageableX (), pageFormat.getImageableY ());
    g2.setPaint (Color.black);
    g2.setStroke (new BasicStroke (2));

    g2.setFont (font);

    int first = page * linesPerPage;
    int last = first + linesPerPage;
    if (last > lines.length)
      last = lines.length;

    for (int line = first; line < last; line++)
      g2.drawString (lines[line], x, y + (line % linesPerPage + 2) * lineHeight);

    return (PAGE_EXISTS);
  }

  // Routine copied from http://progcookbook.blogspot.com/2006/02/text-wrapping-function-for-java.html
  static String[] wrapText (String text, int len)
  {
    // return empty array for null text
    if (text == null)
      return new String[] {};

    // return text if len is zero or less
    if (len <= 0)
      return new String[] { text };

    // return text if less than length
    if (text.length () <= len)
      return new String[] { text };

    char[] chars = text.toCharArray ();
    Vector<String> lines = new Vector<String> ();
    StringBuilder line = new StringBuilder ();
    StringBuilder word = new StringBuilder ();

    for (int i = 0; i < chars.length; i++)
    {
      if (chars[i] == 10)
      {
        line.append (word);
        word.delete (0, word.length ());
        lines.add (line.toString ());
        line.delete (0, line.length ());
        continue;
      }

      word.append (chars[i]);

      if (chars[i] == ' ')
      {
        if ((line.length () + word.length ()) > len)
        {
          lines.add (line.toString ());
          line.delete (0, line.length ());
        }

        line.append (word);
        word.delete (0, word.length ());
      }
    }

    // handle any extra chars in current word
    if (word.length () > 0)
    {
      if ((line.length () + word.length ()) > len)
      {
        lines.add (line.toString ());
        line.delete (0, line.length ());
      }
      line.append (word);
    }

    // handle extra line
    if (line.length () > 0)
      lines.add (line.toString ());

    String[] ret = new String[lines.size ()];
    int c = 0; // counter
    for (Enumeration<String> e = lines.elements (); e.hasMoreElements (); c++)
      ret[c] = e.nextElement ();

    return ret;
  }
}