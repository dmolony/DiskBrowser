package com.bytezone.diskbrowser.catalog;

/*******************************************************************************
 * Factory object that determines whether iText is available, and creates a
 * CatalogLister and a DiskLister accordingly. Also links the two xxxListers to
 * menu items.
 ******************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.bytezone.diskbrowser.gui.MenuHandler;

public class DocumentCreatorFactory
{
  public CatalogLister catalogLister;
  public DiskLister diskLister;

  public DocumentCreatorFactory (MenuHandler mh)
  {
    //		try
    //		{
    //			Class.forName ("com.lowagie.text.Document");
    //			catalogLister = new PDFCatalogCreator ();
    //			diskLister = new PDFDiskCreator ();
    //		}
    //		catch (ClassNotFoundException e)
    {
      catalogLister = new TextCatalogCreator ();
      diskLister = new TextDiskCreator ();
    }

    mh.createCatalogFileItem.setText (catalogLister.getMenuText ());
    mh.createDiskFileItem.setText (diskLister.getMenuText ());

    mh.createCatalogFileItem.addActionListener (new ActionListener ()
    {
      public void actionPerformed (ActionEvent e)
      {
        catalogLister.createCatalog ();
      }
    });

    mh.createDiskFileItem.addActionListener (new ActionListener ()
    {
      public void actionPerformed (ActionEvent e)
      {
        diskLister.createDisk ();
      }
    });
  }
}