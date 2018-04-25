package com.bytezone.diskbrowser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import javax.swing.*;
import javax.swing.event.EventListenerList;

class RedoHandler implements FileSelectionListener, DiskSelectionListener,
    SectorSelectionListener, FileNodeSelectionListener
{
  private static final String base = "/com/bytezone/diskbrowser/icons/";
  EventListenerList listenerList = new EventListenerList ();
  Action leftAction = new LeftAction ();
  Action rightAction = new RightAction ();
  RedoData redoData = new RedoData (leftAction, rightAction);
  static int id = 0;

  public RedoHandler (JRootPane jRootPane, JToolBar toolBar)
  {
    // This code works as long as the toolBar arrows have focus first
    InputMap inputMap = jRootPane.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = jRootPane.getActionMap ();

    inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK),
        "LeftAction");
    actionMap.put ("LeftAction", leftAction);
    inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK),
        "RightAction");
    actionMap.put ("RightAction", rightAction);

    toolBar.add (leftAction);
    toolBar.add (rightAction);
  }

  public RedoData createData ()
  {
    RedoData data = new RedoData (leftAction, rightAction);
    this.redoData = data; // doesn't fire an event this way
    return data;
  }

  public void setCurrentData (RedoData data)
  {
    this.redoData = data;
    RedoEvent event = redoData.getCurrentEvent ();
    if (event != null)
      fireRedoEvent (event);
  }

  private void fireRedoEvent (RedoEvent event)
  {
    RedoListener[] listeners = (listenerList.getListeners (RedoListener.class));
    for (RedoListener listener : listeners)
      listener.redo (event);
  }

  public void addRedoListener (RedoListener listener)
  {
    listenerList.add (RedoListener.class, listener);
  }

  public void removeRedoListener (RedoListener listener)
  {
    listenerList.remove (RedoListener.class, listener);
  }

  @Override
  public void diskSelected (DiskSelectedEvent event)
  {
    if (!event.redo) // it's an event we just caused
      addEvent (new RedoEvent ("DiskEvent", event));
  }

  @Override
  public void fileNodeSelected (FileNodeSelectedEvent event)
  {
    if (!event.redo)                            // it's an event we just caused
      addEvent (new RedoEvent ("FileNodeEvent", event));
  }

  @Override
  public void fileSelected (FileSelectedEvent event)
  {
    if (!event.redo) // it's an event we just caused
      addEvent (new RedoEvent ("FileEvent", event));
  }

  @Override
  public void sectorSelected (SectorSelectedEvent event)
  {
    if (!event.redo)                            // it's an event we just caused
      addEvent (new RedoEvent ("SectorEvent", event));
  }

  private void addEvent (RedoEvent event)
  {
    redoData.addEvent (event);
  }

  public class RedoEvent extends EventObject
  {
    String type;
    EventObject value;

    public RedoEvent (String type, EventObject value)
    {
      super (RedoHandler.this);
      this.type = type;
      this.value = value;
    }

    @Override
    public String toString ()
    {
      return ("[type=" + type + ", value=" + value + "]");
    }
  }

  public interface RedoListener extends EventListener
  {
    void redo (RedoEvent event);
  }

  class LeftAction extends AbstractAction
  {
    public LeftAction ()
    {
      super ("Back");
      putValue (Action.SHORT_DESCRIPTION, "Undo selection");
      URL url = getClass ().getResource (base + "Symbol-Left-32.png");
      if (url != null)
        putValue (Action.LARGE_ICON_KEY, new ImageIcon (url));
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {
      fireRedoEvent (redoData.getPreviousEvent ());
    }
  }

  class RightAction extends AbstractAction
  {
    public RightAction ()
    {
      super ("Forward");
      putValue (Action.SHORT_DESCRIPTION, "Redo selection");
      URL url = getClass ().getResource (base + "Symbol-Right-32.png");
      if (url != null)
        putValue (Action.LARGE_ICON_KEY, new ImageIcon (url));
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {
      fireRedoEvent (redoData.getNextEvent ());
    }
  }

  class RedoData
  {
    List<RedoEvent> events = new ArrayList<RedoEvent> ();
    int currentEvent = -1;
    Action leftAction;
    Action rightAction;
    final int seq = id++;

    public RedoData (Action left, Action right)
    {
      leftAction = left;
      rightAction = right;
      setArrows ();
    }

    RedoEvent getCurrentEvent ()
    {
      if (currentEvent < 0)
        return null;
      setArrows ();
      return events.get (currentEvent);
    }

    RedoEvent getNextEvent ()
    {
      RedoEvent event = events.get (++currentEvent);
      setArrows ();
      return event;
    }

    RedoEvent getPreviousEvent ()
    {
      RedoEvent event = events.get (--currentEvent);
      setArrows ();
      return event;
    }

    void addEvent (RedoEvent event)
    {
      while (currentEvent < events.size () - 1)
        events.remove (events.size () - 1);
      ++currentEvent;
      events.add (event);
      setArrows ();
    }

    private void setArrows ()
    {
      rightAction.setEnabled (currentEvent < events.size () - 1);
      leftAction.setEnabled (currentEvent > 0);
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();
      text.append ("Current event (" + seq + ") : " + currentEvent + "\n");
      for (RedoEvent event : events)
        text.append ("  - " + event + "\n");
      return text.toString ();
    }
  }
}