package com.bytezone.diskbrowser.prodos;

public interface ProdosConstants
{
  int FILE_TYPE_TEXT = 0x04;
  int FILE_TYPE_BINARY = 0x06;
  int FILE_TYPE_FOT = 0x08;                // was Apple /// FotoFile
  int FILE_TYPE_DIRECTORY = 0x0F;
  int FILE_TYPE_ADB = 0x19;
  int FILE_TYPE_AWP = 0x1A;
  int FILE_TYPE_ASP = 0x1B;
  int FILE_TYPE_DESCRIPTOR_TABLE = 0x42;
  int FILE_TYPE_GWP = 0x50;
  int FILE_TYPE_GEO = 0x82;
  int FILE_TYPE_IIGS_SOURCE = 0xB0;
  int FILE_TYPE_IIGS_OBJECT = 0xB1;
  //  int FILE_TYPE_FORKED_FILE = 0xB3;         // S16
  int FILE_TYPE_IIGS_APPLICATION = 0xB3;
  int FILE_TYPE_IIGS_DEVICE_DRIVER = 0xBB;
  int FILE_TYPE_LDF = 0xBC;
  int FILE_TYPE_GSOS_FILE_SYSTEM_TRANSLATOR = 0xBD;
  int FILE_TYPE_PNT = 0xC0;
  int FILE_TYPE_PIC = 0xC1;
  int FILE_TYPE_ANI = 0xC2;
  int FILE_TYPE_PAL = 0xC3;
  int FILE_TYPE_FONT = 0xC8;
  int FILE_TYPE_FINDER = 0xC9;
  int FILE_TYPE_ICN = 0xCA;
  int FILE_TYPE_APPLETALK = 0xE2;
  int FILE_TYPE_PASCAL_VOLUME = 0xEF;
  int FILE_TYPE_USER_DEFINED_1 = 0xF1;
  int FILE_TYPE_BAT = 0xF5;
  int FILE_TYPE_INTEGER_BASIC = 0xFA;
  int FILE_TYPE_INTEGER_BASIC_VARS = 0xFB;
  int FILE_TYPE_APPLESOFT_BASIC = 0xFC;
  int FILE_TYPE_APPLESOFT_BASIC_VARS = 0xFD;
  int FILE_TYPE_RELOCATABLE = 0xFE;
  int FILE_TYPE_SYS = 0xFF;

  int VOLUME_HEADER = 15;
  int SUBDIRECTORY_HEADER = 14;
  int SUBDIRECTORY = 13;
  int GSOS_EXTENDED_FILE = 5;      // tech note #25
  int PASCAL_ON_PROFILE = 4;       // tech note #25
  int TREE = 3;
  int SAPLING = 2;
  int SEEDLING = 1;
  int FREE = 0;

  String[] fileTypes = { //
                        "NON", "BAD", "PCD", "PTX", "TXT", "PDA", "BIN", "FNT", //
                        "FOT", "BA3", "DA3", "WPF", "SOS", "$0D", "$0E", "DIR", //
                        "RPD", "RPI", "AFD", "AFM", "AFR", "SCL", "PFS", "$17", //
                        "$18", "ADB", "AWP", "ASP", "$1C", "$1D", "$1E", "$1F", //
                        "TDM", "$21", "$22", "$23", "$24", "$25", "$26", "$27", //
                        "$28", "$29", "8SC", "8OB", "8IC", "8LD", "P8C", "$2F", //
                        "$30", "$31", "$32", "$33", "$34", "$35", "$36", "$37", //
                        "$38", "$39", "$3A", "$3B", "$3C", "$3D", "$3E", "$3F", //
                        "DIC", "OCR", "FTD", "$43", "$44", "$45", "$46", "$47", //
                        "$48", "$49", "$4A", "$4B", "$4C", "$4D", "$4E", "$4F", //
                        "GWP", "GSS", "GDB", "DRW", "GDP", "HMD", "EDU", "STN", //
                        "HLP", "COM", "CFG", "ANM", "MUM", "ENT", "DVU", "FIN", //
                        "$60", "$61", "$62", "$63", "$64", "$65", "$66", "$67", //
                        "$68", "$69", "$6A", "BIO", "$6C", "TDR", "PRE", "HDV", //
                        "$70", "$71", "$72", "$73", "$74", "$75", "$76", "$77", //
                        "$78", "$79", "$7A", "$7B", "$7C", "$7D", "$7E", "$7F", //
                        "GES", "GEA", "GEO", "GED", "GEF", "GEP", "GEI", "GEX", //
                        "$88", "GEV", "$8A", "GEC", "GEK", "GEW", "$8E", "$8F", //
                        "$90", "$91", "$92", "$93", "$94", "$95", "$96", "$97", //
                        "$98", "$99", "$9A", "$9B", "$9C", "$9D", "$9E", "$9F", //
                        "WP ", "$A1", "$A2", "$A3", "$A4", "$A5", "$A6", "$A7", //
                        "$A8", "$A9", "$AA", "GSB", "TDF", "BDF", "$AE", "$AF", //
                        "SRC", "OBJ", "LIB", "S16", "RTL", "EXE", "PIF", "TIF", //
                        "NDA", "CDA", "TOL", "DVR", "LDF", "FST", "$BE", "DOC", //
                        "PNT", "PIC", "ANI", "PAL", "$C4", "OOG", "SCR", "CDV", //
                        "FON", "FND", "ICN", "$CB", "$CC", "$CD", "$CE", "$CF", //
                        "$D0", "$D1", "$D2", "$D3", "$D4", "MUS", "INS", "MDI", //
                        "SND", "$D9", "$DA", "DBM", "$DC", "DDD", "$DE", "$DF", //
                        "LBR", "$E1", "ATK", "$E3", "$E4", "$E5", "$E6", "$E7", //
                        "$E8", "$E9", "$EA", "$EB", "$EC", "$ED", "R16", "PAR", //
                        "CMD", "OVL", "UD2", "UD3", "UD4", "BAT", "UD6", "UD7", //
                        "PRG", "P16", "INT", "IVR", "BAS", "VAR", "REL", "SYS" };

  int ENTRY_SIZE = 39;
  int ENTRIES_PER_BLOCK = 13;
  int BLOCK_ENTRY_SIZE = ENTRY_SIZE * ENTRIES_PER_BLOCK;
}

/* http://www.kreativekorp.com/miscpages/a2info/filetypes.shtml
 * 
 * $00 UNK Unknown
 * $01 BAD Bad Block File
 * $02 PCD Pascal Code
 * $03 PTX Pascal Text
 * $04 TXT ASCII Text
 * $05 PDA Pascal Data
 * $06 BIN Binary File
 * $07 FNT Apple III Font
 * $08 FOT HiRes/Double HiRes File
 * $09 BA3 Apple III BASIC Program
 * $0A DA3 Apple III BASIC Data
 * $0B WPF Generic Word Processor File
 * $0C SOS SOS System File
 * $0F DIR ProDOS Directory
 * $10 RPD RPS Data
 * $11 RPI RPS Index
 * $12 AFD AppleFile Discard
 * $13 AFM AppleFile Model
 * $14 AFR AppleFile Report
 * $15 SCL Screen Library
 * $16 PFS PFS Document
 * $19 ADB AppleWorks Database
 * $1A AWP AppleWorks Word Processor
 * $1B ASP AppleWorks Spreadsheet
 * $20 TDM Desktop Manager File
 * $21 IPS Instant Pascal Source
 * $22 UPV UCSD Pascal Volume
 * $29 3SD SOS Directory
 * $2A 8SC Source Code
 * $2B 8OB Object Code
 * $2C 8IC Interpretted Code
 * $2D 8LD Language Data
 * $2E P8C ProDOS 8 Code Module
 * $41 OCR Optical Character Recognition File
 * $50 GWP Apple IIgs Word Processor File
 * $51 GSS Apple IIgs Spreadsheet File
 * $52 GDB Apple IIgs Database File
 * $53 DRW Object Oriented Graphics File
 * $54 GDP Apple IIgs Desktop Publishing File
 * $55 HMD HyperMedia
 * $56 EDU Educational Program Data
 * $57 STN Stationery
 * $58 HLP Help File
 * $59 COM Communications File
 * $5A CFG Configuration File
 * $5B ANM Animation File
 * $5C MUM Multimedia File
 * $5D ENT Entertainment Program File
 * $5E DVU Development Utility File
 * $60 PRE PC Pre-Boot
 * $6B BIO PC BIOS
 * $6D DVR PC Driver
 * $6E PRE PC Pre-Boot
 * $6F HDV PC Hard Disk Image
 * $77 KES KES Software
 * $7B TLB KES Software
 * $7F JCP KES Software
 * $80 GeOS System File
 * $81 GeOS Desk Accessory
 * $82 GeOS Application
 * $83 GeOS Document
 * $84 GeOS Font
 * $85 GeOS Printer Driver
 * $86 GeOS Input Driver
 * $87 GeOS Auxilary Driver
 * $8B GeOS Clock Driver
 * $8C GeOS Interface Card Driver
 * $8D GeOS Formatting Data
 * $A0 WP  WordPerfect File
 * $A6
 * $AB GSB Apple IIgs BASIC Program
 * $AC TDF Apple IIgs BASIC TDF
 * $AD BDF Apple IIgs BASIC Data
 * $B0 SRC Apple IIgs Source Code
 * $B1 OBJ Apple IIgs Object Code
 * $B2 LIB Apple IIgs Library
 * $B3 S16 Apple IIgs Application Program
 * $B4 RTL Apple IIgs Runtime Library
 * $B5 EXE Apple IIgs Shell
 * $B6 PIF Apple IIgs Permanent INIT
 * $B7 TIF Apple IIgs Temporary INIT
 * $B8 NDA Apple IIgs New Desk Accessory
 * $B9 CDA Apple IIgs Classic Desk Accessory
 * $BA TOL Apple IIgs Tool
 * $BB DRV Apple IIgs Device Driver
 * $BC LDF Apple IIgs Generic Load File
 * $BD FST Apple IIgs File System Translator
 * $BF DOC Apple IIgs Document
 * $C0 PNT Apple IIgs Packed Super HiRes
 * $C1 PIC Apple IIgs Super HiRes
 * $C2 ANI PaintWorks Animation
 * $C3 PAL PaintWorks Palette
 * $C5 OOG Object-Oriented Graphics
 * $C6 SCR Script
 * $C7 CDV Apple IIgs Control Panel
 * $C8 FON Apple IIgs Font
 * $C9 FND Apple IIgs Finder Data
 * $CA ICN Apple IIgs Icon File
 * $D5 MUS Music File
 * $D6 INS Instrument File
 * $D7 MDI MIDI File
 * $D8 SND Apple IIgs Sound File
 * $DB DBM DB Master Document
 * $E0 LBR Archive File
 * $E2 ATK AppleTalk Data
 * $EE R16 EDASM 816 Relocatable Code
 * $EF PAR Pascal Area
 * $F0 CMD ProDOS Command File
 * $F1 OVL User Defined 1
 * $F2 User Defined 2
 * $F3 User Defined 3
 * $F4 User Defined 4
 * $F5 BAT User Defined 5
 * $F6 User Defined 6
 * $F7 User Defined 7
 * $F8 PRG User Defined 8
 * $F9 P16 Apple IIgs System File
 * $FA INT Integer BASIC Program
 * $FB IVR Integer BASIC Variables
 * $FC BAS Applesoft BASIC Program
 * $FD VAR Applesoft BASIC Variables
 * $FE REL EDASM Relocatable Code
 * $FF SYS ProDOS System File
 */

// See also http://www.kreativekorp.com/miscpages/a2info/filetypes.shtml

/*
 * https://groups.google.com/forum/#!topic/comp.sys.apple2/waoYCIbkJKs
 * 
 * There are a number of disk utilities available that store images of disks that 
 * utilize file systems that are not ProDOS, at the end of a ProDOS volume.  
 * There's DOS Master, by Glen Bredon, that stores images of DOS 3.3 disks at the 
 * end of a ProDOS volume.  Similarly, Pro/Part, by Steven Hirsch, stores images 
 * of CP/M volumes.  Also, there's Pascal Partition Manager (PPM) that stores 
 * images of UCSD Pascal volumes.  I've decided to refer to the area used to store 
 * volume images, by all three of these systems, as a Foreign Volume Area or FVA.  
 * All three of these systems modify the Block Allocation Map of a ProDOS volume 
 * to keep ProDOS from assigning blocks used by FVAs for use by files being 
 * written by ProDOS.  Pascal Partition Manager is different from the other two 
 * in that it has a file type ($EF) and file kind (4) assigned to it by Apple.  
 * A directory listing of a ProODS volume containing an FVA managed by PPM will 
 * show a file name of "PASCAL.AREA".  A directory listing of a ProDOS volume 
 * containing an FVA managed by DOS Master or Pro/Part will show absolutely nothing.  
 * Running a popular utility named "MR.FIXIT", also by Glen Bredon, against a 
 * ProDOS volume containing an FVA will report an error.  Specifically, "MR.FIXIT" 
 * will complain that all the blocks used by an FVA as allocated but not in use.  
 * To solve this problem for Pro/Part I wrote a Foreign Volume Area utility 
 * program that generates a directory entry for the Pro/Part area.  That entry has 
 * file kind 4, file type $EF, file name "PROPART.AREA" and an auxiliary file 
 * type $4853 (Steven Hirsch's initials).  Today I realized that it's likely that 
 * the same thing could be done for DOS Master.  Study of the source code for 
 * DOS Master will reveal it that's true.  If it is, I propose that "DOS33.AREA" 
 * be used as the file name and $4247 as the auxiliary type (Glen Bredon's initials).  
 * As I compose the text of this message I realize that another solution is to 
 * modify "MR.FIXIT" to be aware of FVAs.  But doing that would not allow someone 
 * doing a directory listing of a ProDOS volume containing an FVA to be aware 
 * that the FVA exists.
 */