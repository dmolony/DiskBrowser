package com.bytezone.diskbrowser.gui;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import com.bytezone.diskbrowser.disk.Disk;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.disk.SectorType;
import com.bytezone.diskbrowser.gui.DiskLayoutPanel.LayoutDetails;
import com.bytezone.diskbrowser.gui.RedoHandler.RedoEvent;
import com.bytezone.diskbrowser.gui.RedoHandler.RedoListener;

class DiskLayoutImage extends DiskPanel implements Scrollable, RedoListener
{
  private static final Cursor crosshairCursor = new Cursor (Cursor.CROSSHAIR_CURSOR);
  private static final Color[] lightColors =
      { Color.WHITE, Color.YELLOW, Color.PINK, Color.CYAN, Color.ORANGE, Color.GREEN };
  private static Stroke missingStroke =
      new BasicStroke ((float) 3.0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

  private boolean showFreeSectors;
  private final DiskLayoutSelection selectionHandler = new DiskLayoutSelection ();
  private boolean redo;

  // set defaults (used until a real disk is set)
  private int gridWidth = 8;
  private int gridHeight = 35;

  public DiskLayoutImage ()
  {
    setPreferredSize (new Dimension (240 + 1, 525 + 1));
    addMouseListener (new MyMouseListener ());
    setBackground (backgroundColor);

    //    setOpaque (true);
    //    https://stackoverflow.com/questions/2451990/setopaquetrue-false-java

    addKeyListener (new MyKeyListener ());
  }

  @Override
  public void setDisk (FormattedDisk disk, LayoutDetails details)
  {
    super.setDisk (disk, details);

    gridWidth = layoutDetails.grid.width;           // width in blocks
    gridHeight = layoutDetails.grid.height;         // height in blocks

    setPreferredSize (
        new Dimension (gridWidth * blockWidth + 1, gridHeight * blockHeight + 1));
    selectionHandler.setSelection (null);

    repaint ();
  }

  public FormattedDisk getDisk ()
  {
    return formattedDisk;
  }

  public void setShowFreeSectors (boolean showFree)
  {
    if (showFree != showFreeSectors)
    {
      showFreeSectors = showFree;
      repaint ();
    }
  }

  void setSelection (List<DiskAddress> sectors)
  {
    selectionHandler.setSelection (sectors);
    if (sectors != null && sectors.size () > 0)
    {
      DiskAddress da = sectors.size () == 1 ? sectors.get (0) : sectors.get (1);
      if (da != null)
        scrollRectToVisible (layoutDetails.getLocation (da));
    }
    repaint ();
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    super.paintComponent (g);

    if (formattedDisk == null)
      return;

    Rectangle clipRect = g.getClipBounds ();

    Point topLeft = new Point (clipRect.x / blockWidth * blockWidth,
        clipRect.y / blockHeight * blockHeight);
    Point bottomRight =
        new Point ((clipRect.x + clipRect.width - 1) / blockWidth * blockWidth,
            (clipRect.y + clipRect.height - 1) / blockHeight * blockHeight);

    int maxBlock = gridWidth * gridHeight;
    Disk d = formattedDisk.getDisk ();

    // this stops an index error when using alt-5 to switch to 512-byte blocks
    //    if (maxBlock > d.getTotalBlocks ())
    //      maxBlock = d.getTotalBlocks ();
    // the index error is caused by not recalculating the grid layout

    for (int y = topLeft.y; y <= bottomRight.y; y += blockHeight)
      for (int x = topLeft.x; x <= bottomRight.x; x += blockWidth)
      {
        int blockNo = y / blockHeight * gridWidth + x / blockWidth;
        if (blockNo < maxBlock)
        {
          SectorType type = formattedDisk.getSectorType (blockNo);
          if (type == null)
            System.out.println ("Sector type is null " + blockNo);
          else
          {
            DiskAddress da = d.getDiskAddress (blockNo);
            boolean free = showFreeSectors && formattedDisk.isSectorFree (da);
            boolean selected = selectionHandler.isSelected (da);
            //            boolean missing = d.isSectorMissing (da);
            drawBlock ((Graphics2D) g, type, x, y, free, selected);
          }
        }
      }
  }

  private void drawBlock (Graphics2D g, SectorType type, int x, int y, boolean flagFree,
      boolean selected)
  {
    g.setColor (type.colour);
    g.fillRect (x + 1, y + 1, blockWidth - 1, blockHeight - 1);

    if (flagFree || selected)
    {
      g.setColor (getContrastColor (type));

      if (flagFree)
        g.drawOval (x + centerOffset - 2, y + 4, 7, 7);

      if (selected)
        g.fillOval (x + centerOffset, y + 6, 3, 3);
    }

    //    if (missing)
    //    {
    //      g.setColor (Color.darkGray);
    //      g.setStroke (missingStroke);
    //      g.drawLine (x + 5, y + 5, x + 11, y + 11);
    //      g.drawLine (x + 5, y + 11, x + 11, y + 5);
    //    }
  }

  private Color getContrastColor (SectorType type)
  {
    for (Color color : lightColors)
      if (type.colour == color)
        return Color.BLACK;
    return Color.WHITE;
  }

  @Override
  public Dimension getPreferredScrollableViewportSize ()
  {
    return new Dimension (240 + 1, 525 + 1);          // floppy disk size
  }

  @Override
  public int getScrollableUnitIncrement (Rectangle visibleRect, int orientation,
      int direction)
  {
    return orientation == SwingConstants.HORIZONTAL ? blockWidth : blockHeight;
  }

  @Override
  public int getScrollableBlockIncrement (Rectangle visibleRect, int orientation,
      int direction)
  {
    return orientation == SwingConstants.HORIZONTAL ? blockWidth * 4 : blockHeight * 10;
  }

  @Override
  public boolean getScrollableTracksViewportHeight ()
  {
    return false;
  }

  @Override
  public boolean getScrollableTracksViewportWidth ()
  {
    return false;
  }

  @Override
  public void redo (RedoEvent redoEvent)
  {
    redo = true;
    SectorSelectedEvent event = (SectorSelectedEvent) redoEvent.value;
    setSelection (event.getSectors ());
    fireSectorSelectionEvent (event);
    redo = false;

    requestFocusInWindow ();
  }

  private void fireSectorSelectionEvent ()
  {
    SectorSelectedEvent event =
        new SectorSelectedEvent (this, selectionHandler.getHighlights (), formattedDisk);
    fireSectorSelectionEvent (event);
  }

  private void fireSectorSelectionEvent (SectorSelectedEvent event)
  {
    event.redo = redo;
    SectorSelectionListener[] listeners =
        (listenerList.getListeners (SectorSelectionListener.class));
    for (SectorSelectionListener listener : listeners)
      listener.sectorSelected (event);
  }

  public void addSectorSelectionListener (SectorSelectionListener listener)
  {
    listenerList.add (SectorSelectionListener.class, listener);
  }

  public void removeSectorSelectionListener (SectorSelectionListener listener)
  {
    listenerList.remove (SectorSelectionListener.class, listener);
  }

  class MyKeyListener extends KeyAdapter
  {
    @Override
    public void keyPressed (KeyEvent e)
    {
      switch (e.getKeyCode ())
      {
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_UP:
        case KeyEvent.VK_DOWN:
          selectionHandler.cursorMove (formattedDisk, e);
          fireSectorSelectionEvent ();
          repaint ();
      }
    }
  }

  class MyMouseListener extends MouseAdapter
  {
    private Cursor currentCursor;

    @Override
    public void mouseClicked (MouseEvent e)
    {
      int x = e.getX () / blockWidth;
      int y = e.getY () / blockHeight;
      int blockNo = y * gridWidth + x;
      DiskAddress da = formattedDisk.getDisk ().getDiskAddress (blockNo);

      boolean extend = ((e.getModifiersEx () & InputEvent.SHIFT_DOWN_MASK) > 0);
      boolean append = ((e.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) > 0);

      selectionHandler.doClick (formattedDisk.getDisk (), da, extend, append);
      fireSectorSelectionEvent ();
      repaint ();
      requestFocusInWindow ();
    }

    @Override
    public void mouseEntered (MouseEvent e)
    {
      currentCursor = getCursor ();
      setCursor (crosshairCursor);
    }

    @Override
    public void mouseExited (MouseEvent e)
    {
      setCursor (currentCursor);
    }
  }
}