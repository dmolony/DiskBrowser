package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class ObjectModule extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  List<SegmentHeader> segmentHeaders = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  public ObjectModule (String name, byte[] buffer, int auxType)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    int ptr = 0;

    while (ptr < buffer.length - 4)
    {
      int byteCount = Utility.getLong (buffer, ptr);
      if (byteCount == 0)
        break;

      segmentHeaders.add (new SegmentHeader (buffer, ptr));
      ptr += byteCount;
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");

    text.append ("Object Module\n\n");

    for (SegmentHeader segmentHeader : segmentHeaders)
    {
      text.append (segmentHeader);
      text.append ("\n");
    }

    return text.toString ();
  }
}
