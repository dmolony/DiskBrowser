package com.bytezone.diskbrowser.disk;

import java.awt.Dimension;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JTree;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.gui.DataSource;

public interface FormattedDisk
{
  // Methods to be implemented by the implementer
  public DataSource getFormattedSector (DiskAddress da);

  public List<DiskAddress> getFileSectors (int fileNo);

  // Methods implemented by AbstractFormattedDisk
  public JTree getCatalogTree ();           // each node is an AppleFileSource

  public List<AppleFileSource> getCatalogList ();

  public void writeFile (AbstractFile file);

  public SectorType getSectorType (int track, int sector);

  public SectorType getSectorType (int block);

  public SectorType getSectorType (DiskAddress da);

  public void setSectorType (int block, SectorType type);

  public String getSectorFilename (DiskAddress da);

  public List<SectorType> getSectorTypeList ();

  public Disk getDisk ();

  public FormattedDisk getParent ();

  public void setParent (FormattedDisk disk);

  public AppleFileSource getCatalog ();

  public AppleFileSource getFile (String uniqueName);

  public int clearOrphans ();

  public void setSectorFree (int block, boolean free);

  public boolean isSectorFree (DiskAddress da);

  public boolean isSectorFree (int block);

  public void verify ();

  public boolean stillAvailable (DiskAddress da);

  public boolean stillAvailable (int block);

  public Dimension getGridLayout ();

  public String getAbsolutePath ();

  public String getDisplayPath ();

  public void setOriginalPath (Path path);

  // VTOC flags sector as free, but it is in use by a file
  public int falsePositiveBlocks ();

  // VTOC flags sector as in use, but no file is using it
  public int falseNegativeBlocks ();

  public String getName ();
}

// getFileTypeList ()
// getFiles (FileType type)