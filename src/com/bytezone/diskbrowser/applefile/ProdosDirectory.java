package com.bytezone.diskbrowser.applefile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.bytezone.diskbrowser.disk.FormattedDisk;
import com.bytezone.diskbrowser.prodos.DirectoryHeader;
import com.bytezone.diskbrowser.prodos.ProdosConstants;
import com.bytezone.diskbrowser.prodos.ProdosDisk;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class ProdosDirectory extends AbstractFile implements ProdosConstants
// -----------------------------------------------------------------------------------//
{
  static final DateTimeFormatter df = DateTimeFormatter.ofPattern ("d-LLL-yy");
  static final DateTimeFormatter tf = DateTimeFormatter.ofPattern ("H:mm");
  static final String UNDERLINE = "--------------------------------------------------\n";

  private static final String NO_DATE = "<NO DATE>";

  private final ProdosDisk parentFD;
  private final int totalBlocks;
  private final int freeBlocks;
  private final int usedBlocks;

  // ---------------------------------------------------------------------------------//
  public ProdosDirectory (FormattedDisk parent, String name, byte[] buffer,
      int totalBlocks, int freeBlocks, int usedBlocks)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.parentFD = (ProdosDisk) parent;
    this.totalBlocks = totalBlocks;
    this.freeBlocks = freeBlocks;
    this.usedBlocks = usedBlocks;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    if (showDebugText)
      return getDebugText ();
    else
      return getDirectoryText ();
  }

  // ---------------------------------------------------------------------------------//
  private String getDebugText ()
  // ---------------------------------------------------------------------------------//
  {
    List<DirectoryHeader> directoryHeaders = parentFD.getDirectoryHeaders ();
    StringBuilder text = new StringBuilder ();

    for (DirectoryHeader directoryHeader : directoryHeaders)
    {
      text.append (UNDERLINE);
      text.append (directoryHeader.getText ());
      text.append ("\n");
      text.append (UNDERLINE);
      directoryHeader.listFileEntries (text);
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String getDirectoryText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append ("File : " + parentFD.getDisplayPath () + "\n\n");
    for (int i = 0; i < buffer.length; i += ENTRY_SIZE)
    {
      int storageType = (buffer[i] & 0xF0) >> 4;
      if (storageType == 0)
        continue;                               // break??

      int nameLength = buffer[i] & 0x0F;
      String filename = HexFormatter.getString (buffer, i + 1, nameLength);
      String subType = "";
      String locked;

      switch (storageType)
      {
        case VOLUME_HEADER:
        case SUBDIRECTORY_HEADER:
          String root = storageType == VOLUME_HEADER ? "/" : "";
          text.append (root + filename + "\n\n");
          text.append (" NAME           TYPE  BLOCKS  "
              + "MODIFIED         CREATED          ENDFILE SUBTYPE" + "\n\n");
          break;

        case FREE:
        case SEEDLING:
        case SAPLING:
        case TREE:
        case PASCAL_ON_PROFILE:
        case GSOS_EXTENDED_FILE:
        case SUBDIRECTORY:
          int type = buffer[i + 16] & 0xFF;
          int blocks = Utility.intValue (buffer[i + 19], buffer[i + 20]);

          LocalDateTime createdDate = Utility.getAppleDate (buffer, i + 24);
          LocalDateTime modifiedDate = Utility.getAppleDate (buffer, i + 33);

          String dateC =
              createdDate == null ? NO_DATE : createdDate.format (df).toUpperCase ();
          String dateM =
              modifiedDate == null ? NO_DATE : modifiedDate.format (df).toUpperCase ();

          String timeC = createdDate == null ? "" : createdDate.format (tf);
          String timeM = modifiedDate == null ? "" : modifiedDate.format (tf);

          int eof = Utility.intValue (buffer[i + 21], buffer[i + 22], buffer[i + 23]);
          int fileType = buffer[i + 16] & 0xFF;
          locked = (buffer[i + 30] & 0xE0) == 0xE0 ? " " : "*";

          switch (fileType)
          {
            case FILE_TYPE_TEXT:
              int aux = Utility.intValue (buffer[i + 31], buffer[i + 32]);
              subType = String.format ("R=%5d", aux);
              break;

            case FILE_TYPE_BINARY:
            case FILE_TYPE_PNT:
            case FILE_TYPE_PIC:
            case FILE_TYPE_FOT:
              aux = Utility.intValue (buffer[i + 31], buffer[i + 32]);
              subType = String.format ("A=$%4X", aux);
              break;

            case FILE_TYPE_AWP:
              aux = Utility.intValue (buffer[i + 32], buffer[i + 31]);      // backwards!
              if (aux != 0)
                filename = convert (filename, aux);
              break;

            default:
              subType = "";
          }

          text.append (String.format ("%s%-15s %3s   %5d  %9s %5s  %9s %5s %8d %7s%n",
              locked, filename, ProdosConstants.fileTypes[type], blocks, dateM, timeM,
              dateC, timeC, eof, subType));
          break;

        default:
          text.append (" <Unknown strage type : " + storageType + "\n");
      }
    }

    text.append (
        String.format ("%nBLOCKS FREE:%5d     BLOCKS USED:%5d     TOTAL BLOCKS:%5d%n",
            freeBlocks, usedBlocks, totalBlocks));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private String convert (String name, int flags)
  // ---------------------------------------------------------------------------------//
  {
    char[] newName = name.toCharArray ();
    for (int i = 0, weight = 0x8000; i < newName.length; i++, weight >>>= 1)
    {
      if ((flags & weight) != 0)
      {
        if (newName[i] == '.')
          newName[i] = ' ';
        else if (newName[i] >= 'A' && newName[i] <= 'Z')
          newName[i] += 32;
      }
    }
    return new String (newName);
  }
}