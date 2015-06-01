package com.bytezone.diskbrowser.disk;

import java.util.List;

import com.bytezone.diskbrowser.applefile.AppleFileSource;
import com.bytezone.diskbrowser.gui.DataSource;

/*
 * Most AppleFileSource objects are CatalogEntry types. In order to allow Disk
 * and Volume nodes in the tree to show some text in the centre panel, use a
 * DefaultAppleFileSource which returns a DefaultDataSource (just some text).
 */
public class DefaultAppleFileSource implements AppleFileSource
{
  final String title;
  final DataSource file;
  final FormattedDisk owner;
  List<DiskAddress> blocks;

  public DefaultAppleFileSource (String text, FormattedDisk owner)
  {
    this ("", text, owner);
  }

  public DefaultAppleFileSource (String title, String text, FormattedDisk owner)
  {
    this (title, new DefaultDataSource (text), owner);
  }

  public DefaultAppleFileSource (String title, DataSource file, FormattedDisk owner)
  {
    this.title = title;
    this.file = file;
    this.owner = owner;
  }

  public DefaultAppleFileSource (String title, DataSource file, FormattedDisk owner,
        List<DiskAddress> blocks)
  {
    this (title, file, owner);
    this.blocks = blocks;
    if (file instanceof DefaultDataSource)
      ((DefaultDataSource) file).buffer = owner.getDisk ().readSectors (blocks);
  }

  public void setSectors (List<DiskAddress> blocks)
  {
    this.blocks = blocks;
    if (file instanceof DefaultDataSource)
      ((DefaultDataSource) file).buffer = owner.getDisk ().readSectors (blocks);
  }

  public DataSource getDataSource ()
  {
    return file;
  }

  public FormattedDisk getFormattedDisk ()
  {
    return owner;
  }

  public List<DiskAddress> getSectors ()
  {
    return blocks;
  }

  /*
   * See similar routine in CatalogPanel.DiskNode
   */
  @Override
  public String toString ()
  {
    final int MAX_NAME_LENGTH = 40;
    final int SUFFIX_LENGTH = 12;
    final int PREFIX_LENGTH = MAX_NAME_LENGTH - SUFFIX_LENGTH - 3;

    if (title.length () > MAX_NAME_LENGTH)
      return title.substring (0, PREFIX_LENGTH) + "..."
            + title.substring (title.length () - SUFFIX_LENGTH);
    return title;
  }

  public String getUniqueName ()
  {
    return title;
  }
}