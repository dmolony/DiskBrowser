package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;

public class FileTypeDescriptorTable extends AbstractFile
{
  int versionMajor;
  int versionMinor;
  int flags;
  int numEntries;
  int spareWord;
  int indexRecordSize;
  int offsetToIdx;

  private final List<IndexRecord> indexRecords = new ArrayList<> ();

  public FileTypeDescriptorTable (String name, byte[] buffer)
  {
    super (name, buffer);

    versionMajor = buffer[0] & 0xFF;
    versionMinor = buffer[1] & 0xFF;
    flags = HexFormatter.unsignedShort (buffer, 2);
    numEntries = HexFormatter.unsignedShort (buffer, 4);
    spareWord = HexFormatter.unsignedShort (buffer, 6);
    indexRecordSize = HexFormatter.unsignedShort (buffer, 8);
    offsetToIdx = HexFormatter.unsignedShort (buffer, 10);

    int ptr = offsetToIdx;
    for (int i = 0; i < numEntries; i++)
    {
      indexRecords.add (new IndexRecord (buffer, ptr));
      ptr += indexRecordSize;
    }
  }

  @Override
  public String getText ()
  {
    StringBuilder text = new StringBuilder ("Name : " + name + "\n\n");
    text.append ("File Type Descriptor Table\n\n");
    text.append (
        String.format ("Version ........... %d.%d%n", versionMajor, versionMinor));
    text.append (String.format ("Flags ............. %04X%n", flags));
    text.append (String.format ("NumEntries ........ %d%n", numEntries));
    text.append (String.format ("SpareWord ......... %d%n", spareWord));
    text.append (String.format ("IndexRecordSize ... %d%n", indexRecordSize));
    text.append (String.format ("OffsetToIdx ....... %d%n%n", offsetToIdx));
    text.append ("Type     Aux   Flags   Offset   String\n");

    for (IndexRecord indexRecord : indexRecords)
      text.append (String.format ("%04X    %04X    %04X    %04X    %s%n",
          indexRecord.fileType, indexRecord.auxType, indexRecord.flags,
          indexRecord.offset, indexRecord.string));

    text.deleteCharAt (text.length () - 1);

    return text.toString ();
  }

  class IndexRecord
  {
    int fileType;
    int auxType;
    int flags;
    int offset;
    String string;

    public IndexRecord (byte[] buffer, int offset)
    {
      fileType = HexFormatter.unsignedShort (buffer, offset);
      auxType = HexFormatter.unsignedLong (buffer, offset + 2);
      flags = HexFormatter.unsignedShort (buffer, offset + 6);
      this.offset = HexFormatter.unsignedShort (buffer, offset + 8);
      string = HexFormatter.getPascalString (buffer, this.offset);
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("File type ...... %d%n", fileType));
      text.append (String.format ("Aux type ....... %04X%n", auxType));
      text.append (String.format ("Flags .......... %04X%n", flags));
      text.append (String.format ("Offset ......... %04X%n", offset));
      text.append (String.format ("String ......... %s%n", string));

      return text.toString ();
    }
  }
}