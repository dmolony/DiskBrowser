package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

public class InfocomAbstractFile extends AbstractFile
{
  protected List<HexBlock> hexBlocks = new ArrayList<> ();

  public InfocomAbstractFile (String name, byte[] buffer)
  {
    super (name, buffer);
  }

  @Override
  public String getHexDump ()
  {
    if (hexBlocks.size () > 0)
    {
      StringBuilder text = new StringBuilder ();

      for (HexBlock hb : hexBlocks)
      {
        if (hb.title != null)
          text.append (hb.title + "\n\n");
        text.append (HexFormatter.format (buffer, hb.ptr, hb.size) + "\n\n");
      }

      text.deleteCharAt (text.length () - 1);
      text.deleteCharAt (text.length () - 1);

      return text.toString ();
    }

    if (buffer == null || buffer.length == 0)
      return "No buffer";

    if (buffer.length <= 99999)
      return HexFormatter.format (buffer, 0, buffer.length);

    return HexFormatter.format (buffer, 0, 99999);
  }

  protected class HexBlock
  {
    public int ptr;
    public int size;
    public String title;

    public HexBlock (int ptr, int size, String title)
    {
      this.ptr = ptr;
      this.size = size;
      this.title = title;
    }
  }
}