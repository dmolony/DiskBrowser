package com.bytezone.diskbrowser.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
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
import com.bytezone.diskbrowser.gui.DebuggingAction.DebugListener;
import com.bytezone.diskbrowser.gui.FontAction.FontChangeEvent;
import com.bytezone.diskbrowser.gui.FontAction.FontChangeListener;

// -----------------------------------------------------------------------------------//
public class DataPanel extends JTabbedPane
    implements DiskSelectionListener, FileSelectionListener, SectorSelectionListener,
    FileNodeSelectionListener, FontChangeListener, BasicPreferencesListener,
    AssemblerPreferencesListener, TextPreferencesListener, DebugListener
// -----------------------------------------------------------------------------------//
{
  private static final int TEXT_WIDTH = 65;
  private static final int BACKGROUND = 245;

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

  private Worker animation;

  final MenuHandler menuHandler;

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

    LineWrapAction lineWrapAction = new LineWrapAction ();
    menuHandler.lineWrapItem.setAction (lineWrapAction);
    lineWrapAction.addListener (formattedText);

    menuHandler.colourQuirksItem.setAction (new ColourQuirksAction (this));
    menuHandler.monochromeItem.setAction (new MonochromeAction (this));

    DebuggingAction debuggingAction = new DebuggingAction ();
    debuggingAction.addDebugListener (this);
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
  @Override
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
          animation = new Worker ((SHRPictureFile2) dataSource);
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
  private class ImagePanel extends JPanel
  // ---------------------------------------------------------------------------------//
  {
    private BufferedImage image;
    private double scale = 1;
    private double userScale = .5;

    public ImagePanel ()
    {
      this.setBackground (new Color (BACKGROUND, BACKGROUND, BACKGROUND));
    }

    private void setScale (double scale)
    {
      this.userScale = scale;
    }

    private void setImage (BufferedImage image)
    {
      this.image = image;
      int width, height;

      if (image != null)
      {
        Graphics2D g2 = image.createGraphics ();
        g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        width = image.getWidth ();
        height = image.getHeight ();
      }
      else
        width = height = 0;

      if (true)
      {
        if (width < 400 && width > 0)
          scale = (400 - 1) / width + 1;
        else
          scale = 1;
        if (scale > 4)
          scale = 4;
      }

      scale *= userScale;

      setPreferredSize (new Dimension ((int) (width * scale), (int) (height * scale)));
      repaint ();
    }

    @Override
    public void paintComponent (Graphics g)
    {
      super.paintComponent (g);

      if (image != null)
      {
        Graphics2D g2 = ((Graphics2D) g);
        g2.transform (AffineTransform.getScaleInstance (scale, scale));
        g2.drawImage (image,
            (int) ((getWidth () - image.getWidth () * scale) / 2 / scale), 4, this);
      }
    }
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

  // ---------------------------------------------------------------------------------//
  class Worker extends SwingWorker<Void, Integer>
  // ---------------------------------------------------------------------------------//
  {
    volatile boolean running;
    SHRPictureFile2 image;

    public Worker (SHRPictureFile2 image)
    {
      assert image.isAnimation ();
      this.image = image;
    }

    public void cancel ()
    {
      running = false;
    }

    @Override
    protected Void doInBackground () throws Exception
    {
      running = true;
      try
      {
        while (running)
        {
          Thread.sleep (image.getDelay ());
          publish (0);
        }
      }
      catch (InterruptedException e)
      {
        e.printStackTrace ();
      }
      return null;
    }

    @Override
    protected void process (List<Integer> chunks)
    {
      image.nextFrame ();
      update ();
    }
  }
}