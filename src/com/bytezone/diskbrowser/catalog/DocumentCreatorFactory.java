package com.bytezone.diskbrowser.catalog;

import com.bytezone.diskbrowser.gui.MenuHandler;

public class DocumentCreatorFactory
{
  public CatalogLister catalogLister;
  public DiskLister diskLister;

  private DocumentCreatorFactory (MenuHandler mh)
  {
    //		try
    //		{
    //			Class.forName ("com.lowagie.text.Document");
    //			catalogLister = new PDFCatalogCreator ();
    //			diskLister = new PDFDiskCreator ();
    //		}
    //		catch (ClassNotFoundException e)
    //    {
    //      catalogLister = new TextCatalogCreator ();
    //      diskLister = new TextDiskCreator ();
    //    }

    //    mh.createCatalogFileItem.setText (catalogLister.getMenuText ());
    //    mh.createDiskFileItem.setText (diskLister.getMenuText ());
    //
    //    mh.createCatalogFileItem.addActionListener (new ActionListener ()
    //    {
    //      public void actionPerformed (ActionEvent e)
    //      {
    //        catalogLister.createCatalog ();
    //      }
    //    });

    //    mh.createDiskFileItem.addActionListener (new ActionListener ()
    //    {
    //      public void actionPerformed (ActionEvent e)
    //      {
    //        diskLister.createDisk ();
    //      }
    //    });
  }
}