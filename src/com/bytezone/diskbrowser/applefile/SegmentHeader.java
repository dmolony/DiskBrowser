package com.bytezone.diskbrowser.applefile;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class SegmentHeader
// -----------------------------------------------------------------------------------//
{
  int bytecnt;
  int resspc;
  int length;
  int kind1;

  String kindWhereText;
  boolean kindReload;
  boolean kindAbsoluteBank;
  boolean kindSpecialMemory;
  boolean kindPosIndependent;
  boolean kindPrivate;
  boolean kindStatic;

  int lablen;
  int numlen;
  int version;
  int banksize;
  int kind2;
  int unused;
  int org;
  int align;
  int numsex;
  int lcbank;
  int segnum;
  int entry;
  int dispname;
  int dispdata;
  String loadname;
  String segname;

  boolean debug = false;

  // ---------------------------------------------------------------------------------//
  public SegmentHeader (byte[] buffer, int offset)
  // ---------------------------------------------------------------------------------//
  {
    bytecnt = Utility.getLong (buffer, offset);
    resspc = Utility.getLong (buffer, offset + 4);
    length = Utility.getLong (buffer, offset + 8);

    kind1 = buffer[offset + 12] & 0xFF;

    lablen = buffer[offset + 13] & 0xFF;
    numlen = buffer[offset + 14] & 0xFF;
    version = buffer[offset + 15] & 0xFF;

    banksize = Utility.getLong (buffer, offset + 16);
    kind2 = Utility.getWord (buffer, offset + 20);
    unused = Utility.getWord (buffer, offset + 22);
    org = Utility.getLong (buffer, offset + 24);
    align = Utility.getLong (buffer, offset + 28);

    numsex = buffer[offset + 32] & 0xFF;
    lcbank = buffer[offset + 33] & 0xFF;

    segnum = Utility.getWord (buffer, offset + 34);

    entry = Utility.getLong (buffer, offset + 36);

    dispname = Utility.getWord (buffer, offset + 40);
    dispdata = Utility.getWord (buffer, offset + 42);

    decodeKind ();

    if (buffer[offset + dispname] == 0)
      loadname = "";
    else
      loadname = new String (buffer, offset + dispname, 10);

    if (lablen == 0)
      segname = HexFormatter.getPascalString (buffer, offset + dispname + 10);
    else
      segname = "not finished";

    if (debug)
      System.out.println (this);

    int ptr = offset + dispdata;
    while (true)
    {
      int recType = buffer[ptr] & 0xFF;
      //      System.out.printf ("%04X = %02X%n", ptr, recType);

      if (recType > 0 && recType <= 0xDF)
      {
        if (debug)
        {
          System.out.printf ("Const: %02X%n", recType);
          System.out.println (HexFormatter.format (buffer, ptr, recType + 1, ptr));
        }
        ptr += recType + 1;
        continue;
      }

      if (debug)
        System.out.printf ("%02X ", recType);

      switch (recType)
      {
        case 0x00:        // END
          if (debug)
            System.out.println ("END");
          break;

        case 0xE0:        // ALIGN
          if (debug)
            System.out.printf ("ALIGN:%n");
          break;

        case 0xE1:        // ORG
          if (debug)
            System.out.printf ("ORG:%n");
          break;

        case 0xE2:        // RELOC
          int bytesRelocated = buffer[ptr + 1] & 0xFF;
          int bitShift = buffer[ptr + 2] & 0xFF;
          int segmentOffset = Utility.getLong (buffer, ptr + 3);
          int value = Utility.getLong (buffer, ptr + 7);
          if (debug)
            System.out.printf ("RELOC: %02X %02X %08X %08X%n", bytesRelocated, bitShift,
                segmentOffset, value);
          ptr += 11;
          continue;

        case 0xE3:        // INTERSEG
          int count1 = buffer[ptr + 1] & 0xFF;
          int count2 = buffer[ptr + 2] & 0xFF;
          int operandOffset = Utility.getLong (buffer, ptr + 3);
          int fileNo = Utility.getWord (buffer, ptr + 7);
          int segNo = Utility.getWord (buffer, ptr + 9);
          int subroutineOffset = Utility.getLong (buffer, ptr + 11);
          if (debug)
            System.out.printf ("INTERSEG: %02X %02X %08X %04X %04X %08X%n", count1,
                count2, operandOffset, fileNo, segNo, subroutineOffset);
          ptr += 15;
          break;

        case 0xE4:        // USING
          if (debug)
            System.out.printf ("USING:%n");
          break;

        case 0xE5:        // STRONG
          if (debug)
            System.out.printf ("STRONG:%n");
          break;

        case 0xE6:        // GLOBAL
          if (debug)
            System.out.printf ("GLOBAL:%n");
          break;

        case 0xE7:        // GEQU
          if (debug)
            System.out.printf ("GEQU:%n");
          break;

        case 0xE8:        // MEM
          if (debug)
            System.out.printf ("MEM:%n");
          break;

        case 0xEB:        // EXPR
          if (debug)
            System.out.printf ("EXPR:%n");
          break;

        case 0xEC:        // ZEXPR
          if (debug)
            System.out.printf ("ZEXPR:%n");
          break;

        case 0xED:        // BEXPR
          if (debug)
            System.out.printf ("BEXPR:%n");
          break;

        case 0xEE:        // RELEXPR
          if (debug)
            System.out.printf ("RELEXPR:%n");
          break;

        case 0xEF:        // LOCAL
          if (debug)
            System.out.printf ("LOCAL:%n");
          break;

        case 0xF0:        // EQU
          String label = HexFormatter.getPascalString (buffer, ptr + 1);
          if (debug)
            System.out.printf ("EQU: %s%n", label);
          break;

        case 0xF1:        // DS
          if (debug)
            System.out.printf ("DS:%n");
          break;

        case 0xF2:        // LCONST
          int constLength = Utility.getLong (buffer, ptr + 1);
          if (debug)
            System.out.printf ("Const: %04X%n", constLength);
          ptr += constLength + 5;
          continue;

        case 0xF3:        // LEXPR
          if (debug)
            System.out.printf ("LEXPR:%n");
          break;

        case 0xF4:        // ENTRY
          if (debug)
            System.out.printf ("ENTRY:%n");
          break;

        case 0xF5:        // cRELOC
          int cBytesRelocated = buffer[ptr + 1] & 0xFF;
          int cBitShift = buffer[ptr + 2] & 0xFF;
          int cSegmentOffset = Utility.getWord (buffer, ptr + 3);
          int cValue = Utility.getWord (buffer, ptr + 5);
          if (debug)
            System.out.printf ("cRELOC: %02X %02X %08X %08X%n", cBytesRelocated,
                cBitShift, cSegmentOffset, cValue);
          ptr += 7;
          continue;

        case 0xF6:        // cINTERSEG
          int cCount1 = buffer[ptr + 1] & 0xFF;
          int cCount2 = buffer[ptr + 2] & 0xFF;
          int cOperandOffset = Utility.getWord (buffer, ptr + 3);
          int cSegNo = buffer[ptr + 5] & 0xFF;
          int cSubroutineOffset = Utility.getWord (buffer, ptr + 6);
          if (debug)
            System.out.printf ("cINTERSEG: %02X %02X %04X %02X %04X%n", cCount1, cCount2,
                cOperandOffset, cSegNo, cSubroutineOffset);
          ptr += 8;
          continue;

        case 0xF7:        // SUPER
          int superLength = Utility.getLong (buffer, ptr + 1);
          int recordType = buffer[ptr + 5] & 0xFF;
          if (debug)
            System.out.printf ("Super type %02X%n", recordType);
          ptr += superLength + 5;
          continue;

        default:
          System.out.printf ("Unknown record type: %02X%n", recType);
          break;
      }

      if (debug)
        System.out.println ();
      break;
    }
  }

  // ---------------------------------------------------------------------------------//
  private void decodeKind ()
  // ---------------------------------------------------------------------------------//
  {
    int segType;
    int segAttr;

    if (version < 2)              // 8 bits
    {
      segType = kind1 & 0x1F;
      segAttr = kind1 & 0xE0;
    }
    else                          // 16 bits
    {
      segType = kind2 & 0x001F;
      segAttr = kind2 >>> 8;
    }

    kindReload = (segAttr & 0x04) != 0;
    kindAbsoluteBank = (segAttr & 0x08) != 0;
    kindSpecialMemory = (segAttr & 0x10) == 0;
    kindPosIndependent = (segAttr & 0x20) != 0;
    kindPrivate = (segAttr & 0x40) != 0;
    kindStatic = (segAttr & 0x80) == 0;

    kindWhereText = switch (segType)
    {
      case 0x00 -> "Code Segment";
      case 0x01 -> "Data Segment";
      case 0x02 -> "Jump Table Segment";
      case 0x04 -> "Pathname Segment";
      case 0x08 -> "Library Dictionary Segment";
      case 0x10 -> "Initialization Segment";
      case 0x11 -> "Absolute Bank Segment";
      case 0x12 -> "Direct Page / Stack Segment";
      default -> "Unknown";
    };
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Byte count ......... %08X%n", bytecnt));
    text.append (String.format ("Reserved space ..... %08X%n", resspc));
    text.append (String.format ("Length ............. %08X%n", length));

    text.append (String.format ("Kind v1 ............ %02X%n", kind1));
    if (version < 2)
    {
      text.append (String.format ("  Type ............. %s%n", kindWhereText));
      text.append (
          String.format ("  Position ind ..... %s%n", kindPosIndependent ? "Yes" : "No"));
      text.append (
          String.format ("  Private .......... %s%n", kindPrivate ? "Yes" : "No"));
      text.append (
          String.format ("  Static/Dynamic ... %s%n", kindStatic ? "Static" : "Dynamic"));
    }

    text.append (String.format ("Label length ....... %02X%n", lablen));
    text.append (String.format ("Number length ...... %02X%n", numlen));
    text.append (String.format ("Version ............ %02X%n", version));

    text.append (String.format ("Bank size .......... %08X%n", banksize));
    text.append (String.format ("Kind v2 ............ %04X%n", kind2));

    if (version >= 2)
    {
      text.append (String.format ("  Type ............. %s%n", kindWhereText));
      text.append (
          String.format ("  Reload ........... %s%n", kindReload ? "Yes" : "No"));
      text.append (
          String.format ("  Absolute Bank .... %s%n", kindAbsoluteBank ? "Yes" : "No"));
      text.append (
          String.format ("  Special Memory ... %s%n", kindSpecialMemory ? "Yes" : "No"));
      text.append (
          String.format ("  Position ind ..... %s%n", kindPosIndependent ? "Yes" : "No"));
      text.append (
          String.format ("  Private .......... %s%n", kindPrivate ? "Yes" : "No"));
      text.append (
          String.format ("  Static/Dynamic ... %s%n", kindStatic ? "Static" : "Dynamic"));
    }

    text.append (String.format ("Unused ............. %04X%n", unused));
    text.append (String.format ("Org ................ %08X%n", org));
    text.append (String.format ("Align .............. %08X%n", align));

    text.append (String.format ("Number sex ......... %02X%n", numsex));
    text.append (String.format ("LC Bank ............ %02X%n", lcbank));

    text.append (String.format ("Segment number ..... %04X%n", segnum));

    text.append (String.format ("Entry .............. %08X%n", entry));

    text.append (String.format ("Disp name .......... %04X%n", dispname));
    text.append (String.format ("Disp data .......... %04X%n", dispdata));

    text.append (String.format ("Load name .......... %s%n", loadname));
    text.append (String.format ("Segment name ....... %s%n", segname));

    return text.toString ();
  }
}
