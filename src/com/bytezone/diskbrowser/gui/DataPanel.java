package com.bytezone.diskbrowser.gui;

import java.awt.Font;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.ApplesoftBasicProgram;
import com.bytezone.diskbrowser.applefile.AssemblerProgram;
import com.bytezone.diskbrowser.applefile.BasicTextFile;
import com.bytezone.diskbrowser.applefile.BootSector;
import com.bytezone.diskbrowser.applefile.HiResImage;
import com.bytezone.diskbrowser.applefile.Palette;
import com.bytezone.diskbrowser.applefile.PaletteFactory.CycleDirection;
import com.bytezone.diskbrowser.applefile.QuickDrawFont;
import com.bytezone.diskbrowser.applefile.SHRPictureFile2;
import com.bytezone.diskbrowser.disk.DiskAddress;
import com.bytezone.diskbrowser.disk.SectorList;
import com.bytezone.diskbrowser.gui.FontAction.FontChangeEvent;
import com.bytezone.diskbrowser.gui.FontAction.FontChangeListener;

// -----------------------------------------------------------------------------------//
public class DataPanel extends JTabbedPane
    implements DiskSelectionListener, FileSelectionListener, SectorSelectionListener,
    FileNodeSelectionListener, FontChangeListener, BasicPreferencesListener,
    AssemblerPreferencesListener, TextPreferencesListener, PropertyChangeListener
// -----------------------------------------------------------------------------------//
{
  private static final int TEXT_WIDTH = 65;

  private final JTextArea formattedText;
  private final JTextArea hexText;
  private final JTextArea disassemblyText;

  // these two panes are interchangeable
  private final JScrollPane formattedPane;
  private final JScrollPane imagePane;
  private boolean imageVisible = false;

  private final ImagePanel imagePanel;                        // internal class
  private boolean debugMode;

  // used to determine whether the text has been set
  boolean formattedTextValid;
  boolean hexTextValid;
  boolean assemblerTextValid;
  DataSource currentDataSource;

  private AnimationWorker animation;

  final MenuHandler menuHandler;

  DebuggingAction debuggingAction = new DebuggingAction ();
  MonochromeAction monochromeAction = new MonochromeAction ();
  ColourQuirksAction colourQuirksAction = new ColourQuirksAction ();
  LineWrapAction lineWrapAction = new LineWrapAction ();

  enum TabType
  {
    FORMATTED, HEX, DISASSEMBLED
  }

  // ---------------------------------------------------------------------------------//
  public DataPanel (MenuHandler mh)
  // ---------------------------------------------------------------------------------//
  {
    this.menuHandler = mh;
    setTabPlacement (SwingConstants.BOTTOM);

    formattedText = new JTextArea (10, TEXT_WIDTH);
    formattedPane = setPanel (formattedText, "Formatted");
    //    formattedText.setLineWrap (prefs.getBoolean (MenuHandler.PREFS_LINE_WRAP, true));
    formattedText.setText ("Please use the 'File->Set HOME folder...' command to "
        + "\ntell DiskBrowser where your Apple disks are located."
        + "\n\nTo see the contents of a disk in more detail, double-click"
        + "\nthe disk. You will then be able to select individual files to "
        + "view completely.");

    hexText = new JTextArea (10, TEXT_WIDTH);
    setPanel (hexText, "Hex dump");

    disassemblyText = new JTextArea (10, TEXT_WIDTH);
    setPanel (disassemblyText, "Disassembly");

    imagePanel = new ImagePanel ();
    imagePane =
        new JScrollPane (imagePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    imagePane.setBorder (null);

    imagePane.getVerticalScrollBar ().setUnitIncrement (50);
    imagePane.getHorizontalScrollBar ().setUnitIncrement (25);

    addChangeListener (new ChangeListener ()
    {
      @Override
      public void stateChanged (ChangeEvent e)
      {
        switch (getSelectedIndex ())
        {
          case 0:                           // Formatted
            if (!formattedTextValid)
            {
              if (currentDataSource == null)
                formattedText.setText ("");
              else
                setText (formattedText, currentDataSource.getText ());
              formattedTextValid = true;
            }
            break;
          case 1:                           // Hex
            if (!hexTextValid)
            {
              if (currentDataSource == null)
                hexText.setText ("");
              else
                setText (hexText, currentDataSource.getHexDump ());
              hexTextValid = true;
            }
            break;
          case 2:                           // Assembler
            if (!assemblerTextValid)
            {
              if (currentDataSource == null)
                disassemblyText.setText ("");
              else
                setText (disassemblyText, currentDataSource.getAssembler ());
              assemblerTextValid = true;
            }
            break;
          default:
            System.out.println ("Invalid index selected in DataPanel");
        }
      }
    });

    menuHandler.lineWrapItem.setAction (lineWrapAction);
    lineWrapAction.addPropertyChangeListener (this);

    colourQuirksAction.addPropertyChangeListener (this);
    menuHandler.colourQuirksItem.setAction (colourQuirksAction);

    monochromeAction.addPropertyChangeListener (this);
    menuHandler.monochromeItem.setAction (monochromeAction);

    debuggingAction.addPropertyChangeListener (this);
    menuHandler.debuggingItem.setAction (debuggingAction);

    // fill in the placeholders created by the MenuHandler
    List<Palette> palettes = HiResImage.getPalettes ();
    ButtonGroup buttonGroup = menuHandler.paletteGroup;
    Enumeration<AbstractButton> enumeration = buttonGroup.getElements ();
    int ndx = 0;
    while (enumeration.hasMoreElements ())
    {
      JCheckBoxMenuItem item = (JCheckBoxMenuItem) enumeration.nextElement ();
      item.setAction (new PaletteAction (this, palettes.get (ndx++)));
    }
    menuHandler.nextPaletteItem.setAction (new NextPaletteAction (this, buttonGroup));
    menuHandler.prevPaletteItem.setAction (new PreviousPaletteAction (this, buttonGroup));
  }

  // ---------------------------------------------------------------------------------//
  public void selectPalette (Palette palette)
  // ---------------------------------------------------------------------------------//
  {
    HiResImage.getPaletteFactory ().setCurrentPalette (palette);
    if (currentDataSource instanceof HiResImage)
    {
      HiResImage image = (HiResImage) currentDataSource;
      image.setPalette ();
      imagePanel.setImage (image.getImage ());
    }
  }

  // ---------------------------------------------------------------------------------//
  public Palette cyclePalette (CycleDirection direction)
  // ---------------------------------------------------------------------------------//
  {
    Palette palette = HiResImage.getPaletteFactory ().cyclePalette (direction);
    if (currentDataSource instanceof HiResImage)
    {
      HiResImage image = (HiResImage) currentDataSource;
      image.setPalette ();
      imagePanel.setImage (image.getImage ());
    }
    return palette;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void propertyChange (PropertyChangeEvent evt)
  // ---------------------------------------------------------------------------------//
  {
    if (evt.getSource () == debuggingAction)
      setDebug ((Boolean) evt.getNewValue ());
    else if (evt.getSource () == monochromeAction)
      setMonochrome ((Boolean) evt.getNewValue ());
    else if (evt.getSource () == colourQuirksAction)
      setColourQuirks ((Boolean) evt.getNewValue ());
    else if (evt.getSource () == lineWrapAction)
      setLineWrap ((Boolean) evt.getNewValue ());
  }

  // ---------------------------------------------------------------------------------//
  void setLineWrap (boolean lineWrap)
  // ---------------------------------------------------------------------------------//
  {
    formattedText.setLineWrap (lineWrap);
  }

  // ---------------------------------------------------------------------------------//
  public void setColourQuirks (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    if (currentDataSource instanceof HiResImage)
    {
      HiResImage image = (HiResImage) currentDataSource;
      image.setColourQuirks (value);
      imagePanel.setImage (image.getImage ());
    }
  }

  // ---------------------------------------------------------------------------------//
  public void setMonochrome (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    if (currentDataSource instanceof HiResImage)
    {
      HiResImage image = (HiResImage) currentDataSource;
      image.setMonochrome (value);
      imagePanel.setImage (image.getImage ());
    }
  }

  // ---------------------------------------------------------------------------------//
  public void setScale (double scale)
  // ---------------------------------------------------------------------------------//
  {
    imagePanel.setScale (scale);
    if (currentDataSource instanceof HiResImage)
    {
      HiResImage image = (HiResImage) currentDataSource;
      imagePanel.setImage (image.getImage ());
    }
  }

  // ---------------------------------------------------------------------------------//
  public void update ()
  // ---------------------------------------------------------------------------------//
  {
    if (currentDataSource instanceof HiResImage)
    {
      HiResImage image = (HiResImage) currentDataSource;
      imagePanel.setImage (image.getImage ());
    }
  }

  // ---------------------------------------------------------------------------------//
  public void setDebug (boolean value)
  // ---------------------------------------------------------------------------------//
  {
    debugMode = value;

    AbstractFile.setDebug (value);
    setText (formattedText, currentDataSource.getText ());

    if (currentDataSource instanceof HiResImage
        || currentDataSource instanceof QuickDrawFont)
      setDataSource (currentDataSource);      // toggles text/image
  }

  // ---------------------------------------------------------------------------------//
  private void setTabsFont (Font font)
  // ---------------------------------------------------------------------------------//
  {
    formattedText.setFont (font);
    hexText.setFont (font);
    disassemblyText.setFont (font);
    imagePane.getVerticalScrollBar ().setUnitIncrement (font.getSize ());
  }

  // ---------------------------------------------------------------------------------//
  public String getCurrentText ()
  // ---------------------------------------------------------------------------------//
  {
    int index = getSelectedIndex ();
    return index == 0 ? formattedText.getText ()
        : index == 1 ? hexText.getText () : disassemblyText.getText ();
  }

  // ---------------------------------------------------------------------------------//
  private JScrollPane setPanel (JTextArea outputPanel, String tabName)
  // ---------------------------------------------------------------------------------//
  {
    outputPanel.setEditable (false);
    outputPanel.setMargin (new Insets (5, 5, 5, 5));

    JScrollPane outputScrollPane =
        new JScrollPane (outputPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    outputScrollPane.setBorder (null);              // remove the ugly default border
    add (outputScrollPane, tabName);
    return outputScrollPane;
  }

  // ---------------------------------------------------------------------------------//
  private void setDataSource (DataSource dataSource)
  // ---------------------------------------------------------------------------------//
  {
    currentDataSource = dataSource;

    if (dataSource == null)
    {
      formattedText.setText ("");
      hexText.setText ("");
      disassemblyText.setText ("");
      removeImage ();
      return;
    }

    switch (TabType.values ()[getSelectedIndex ()])
    {
      case FORMATTED:
        try
        {
          setText (formattedText, dataSource.getText ());
        }
        catch (Exception e)
        {
          setText (formattedText, e.toString ());
          e.printStackTrace ();
        }
        hexTextValid = false;
        assemblerTextValid = false;
        break;

      case HEX:
        setText (hexText, dataSource.getHexDump ());
        formattedTextValid = false;
        assemblerTextValid = false;
        break;

      case DISASSEMBLED:
        setText (disassemblyText, dataSource.getAssembler ());
        hexTextValid = false;
        formattedTextValid = false;
        break;

      default:
        System.out.println ("Unexpected Tab #" + getSelectedIndex ());
    }

    BufferedImage image = dataSource.getImage ();
    if (image == null || debugMode)
      removeImage ();
    else
    {
      if (dataSource instanceof HiResImage)
      {
        ((HiResImage) dataSource).checkPalette ();
        image = dataSource.getImage ();
        if (((HiResImage) dataSource).isAnimation ())
        {
          if (animation != null)
            animation.cancel ();
          animation = new AnimationWorker (this, (SHRPictureFile2) dataSource);
          animation.execute ();
        }
      }

      imagePanel.setImage (image);
      imagePane.setViewportView (imagePanel);

      if (!imageVisible)
      {
        int selected = getSelectedIndex ();
        remove (formattedPane);
        add (imagePane, "Formatted", 0);
        setSelectedIndex (selected);
        imageVisible = true;

      }
    }
  }

  // ---------------------------------------------------------------------------------//
  private void removeImage ()
  // ---------------------------------------------------------------------------------//
  {
    if (imageVisible)
    {
      int selected = getSelectedIndex ();
      remove (imagePane);
      add (formattedPane, "Formatted", 0);
      setSelectedIndex (selected);
      imageVisible = false;
    }

    if (animation != null)
    {
      animation.cancel ();
      animation = null;
    }
  }

  // ---------------------------------------------------------------------------------//
  private void setText (JTextArea textArea, String text)
  // ---------------------------------------------------------------------------------//
  {
    textArea.setText (text);
    textArea.setCaretPosition (0);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void diskSelected (DiskSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    setSelectedIndex (0);
    setDataSource (null);
    if (event.getFormattedDisk () != null)
      setDataSource (event.getFormattedDisk ().getCatalog ().getDataSource ());
    else
      System.out.println ("bollocks in diskSelected()");
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void fileSelected (FileSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    DataSource dataSource = event.appleFileSource.getDataSource ();
    setDataSource (dataSource);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void sectorSelected (SectorSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    List<DiskAddress> sectors = event.getSectors ();
    if (sectors == null || sectors.size () == 0)
      return;

    if (sectors.size () == 1)
    {
      DiskAddress da = sectors.get (0);
      if (da != null)
        setDataSource (event.getFormattedDisk ().getFormattedSector (da));
    }
    else
      setDataSource (new SectorList (event.getFormattedDisk (), sectors));
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void fileNodeSelected (FileNodeSelectedEvent event)
  // ---------------------------------------------------------------------------------//
  {
    setSelectedIndex (0);
    setDataSource (event.getFileNode ());
    //    FileNode node = event.getFileNode ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void changeFont (FontChangeEvent fontChangeEvent)
  // ---------------------------------------------------------------------------------//
  {
    setTabsFont (fontChangeEvent.font);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setBasicPreferences (BasicPreferences basicPreferences)
  // ---------------------------------------------------------------------------------//
  {
    if (currentDataSource instanceof ApplesoftBasicProgram)
      setDataSource (currentDataSource);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setAssemblerPreferences (AssemblerPreferences assemblerPreferences)
  // ---------------------------------------------------------------------------------//
  {
    if (currentDataSource instanceof AssemblerProgram
        || currentDataSource instanceof BootSector)
      setDataSource (currentDataSource);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void setTextPreferences (TextPreferences textPreferences)
  // ---------------------------------------------------------------------------------//
  {
    if (currentDataSource instanceof BasicTextFile)
      setDataSource (currentDataSource);
  }
}