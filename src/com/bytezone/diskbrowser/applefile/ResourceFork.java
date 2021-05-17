package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.diskbrowser.utilities.Utility;

// -----------------------------------------------------------------------------------//
public class ResourceFork
// -----------------------------------------------------------------------------------//
{
  byte[] buffer;
  ResourceFileHeader resourceFileHeader;

  // ---------------------------------------------------------------------------------//
  public ResourceFork (byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    this.buffer = buffer;

    resourceFileHeader = new ResourceFileHeader (buffer);
    System.out.println (resourceFileHeader);
  }

  // ---------------------------------------------------------------------------------//
  class ResourceFileHeader
  // ---------------------------------------------------------------------------------//
  {
    int fileVersion;
    int fileToMap;
    int fileMapSize;
    ResourceMap resourceMap;

    // -------------------------------------------------------------------------------//
    ResourceFileHeader (byte[] buffer)
    // -------------------------------------------------------------------------------//
    {
      fileVersion = Utility.getLong (buffer, 0);
      fileToMap = Utility.getLong (buffer, 4);
      fileMapSize = Utility.getLong (buffer, 8);

      resourceMap = new ResourceMap (buffer, fileToMap, fileMapSize);
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Version ....... %04X  %<d%n", fileVersion));
      text.append (String.format ("Map offset .... %04X  %<d%n", fileToMap));
      text.append (String.format ("Map size ...... %04X  %<d%n%n", fileMapSize));
      text.append (String.format ("%s", resourceMap));

      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  class ResourceMap
  // ---------------------------------------------------------------------------------//
  {
    int mapNext;
    int mapFlags;
    int mapOffset;
    int mapSize;
    int mapToIndex;
    int mapFileNum;
    int mapId;
    int mapIndexSize;
    int mapIndexUsed;
    int mapFreeListSize;
    int mapFreeListUsed;
    List<ResourceFreeBlock> resourceFreeBlocks = new ArrayList<> ();
    List<ResourceReferenceRecord> resourceReferenceRecords = new ArrayList<> ();

    // -------------------------------------------------------------------------------//
    ResourceMap (byte[] buffer, int ptr, int size)
    // -------------------------------------------------------------------------------//
    {
      int offset = ptr;

      mapNext = Utility.getLong (buffer, ptr);
      mapFlags = Utility.getWord (buffer, ptr + 4);
      mapOffset = Utility.getLong (buffer, ptr + 6);
      mapSize = Utility.getLong (buffer, ptr + 10);
      mapToIndex = Utility.getWord (buffer, ptr + 14);
      mapFileNum = Utility.getWord (buffer, ptr + 16);
      mapId = Utility.getWord (buffer, ptr + 18);
      mapIndexSize = Utility.getLong (buffer, ptr + 20);
      mapIndexUsed = Utility.getLong (buffer, ptr + 24);
      mapFreeListSize = Utility.getWord (buffer, ptr + 28);
      mapFreeListUsed = Utility.getWord (buffer, ptr + 30);

      ptr = offset + 32;
      for (int i = 0; i < mapFreeListUsed; i++)
      {
        resourceFreeBlocks.add (new ResourceFreeBlock (buffer, ptr));
        ptr += 8;
      }

      ptr = offset + mapToIndex;
      for (int i = 0; i < mapIndexUsed; i++)
      {
        resourceReferenceRecords.add (new ResourceReferenceRecord (buffer, ptr));
        ptr += 20;
      }
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Map next ............ %04X  %<d%n", mapNext));
      text.append (String.format ("Map flags ........... %04X  %<d%n", mapFlags));
      text.append (String.format ("Map offset .......... %04X  %<d%n", mapOffset));
      text.append (String.format ("Map size ............ %04X  %<d%n", mapSize));
      text.append (String.format ("Map to index ........ %04X  %<d%n", mapToIndex));
      text.append (String.format ("Map file num ........ %04X  %<d%n", mapFileNum));
      text.append (String.format ("Map ID .............. %04X  %<d%n", mapId));
      text.append (String.format ("Map index size ...... %04X  %<d%n", mapIndexSize));
      text.append (String.format ("Map index used ...... %04X  %<d%n", mapIndexUsed));
      text.append (String.format ("Map free list size .. %04X  %<d%n", mapFreeListSize));
      text.append (
          String.format ("Map free list used .. %04X  %<d%n%n", mapFreeListUsed));

      for (ResourceFreeBlock resourceFreeBlock : resourceFreeBlocks)
        text.append (resourceFreeBlock);
      text.append ("\n");

      for (ResourceReferenceRecord resourceReferenceRecord : resourceReferenceRecords)
        text.append (resourceReferenceRecord);

      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  class ResourceFreeBlock
  // ---------------------------------------------------------------------------------//
  {
    int blockOffset;
    int blockSize;

    // -------------------------------------------------------------------------------//
    public ResourceFreeBlock (byte[] buffer, int ptr)
    // -------------------------------------------------------------------------------//
    {
      blockOffset = Utility.getLong (buffer, ptr);
      blockSize = Utility.getLong (buffer, ptr + 4);
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Offset ........ %04X  %<d%n", blockOffset));
      text.append (String.format ("Size .......... %04X  %<d%n", blockSize));

      return text.toString ();
    }
  }

  // ---------------------------------------------------------------------------------//
  class ResourceReferenceRecord
  // ---------------------------------------------------------------------------------//
  {
    int resType;
    int resId;
    int resOffset;
    int resAttr;
    int resSize;
    int resHandle;

    byte[] buffer;
    int ptr;

    // -------------------------------------------------------------------------------//
    public ResourceReferenceRecord (byte[] buffer, int ptr)
    // -------------------------------------------------------------------------------//
    {
      resType = Utility.getWord (buffer, ptr);
      resId = Utility.getLong (buffer, ptr + 2);
      resOffset = Utility.getLong (buffer, ptr + 6);
      resAttr = Utility.getWord (buffer, ptr + 10);
      resSize = Utility.getLong (buffer, ptr + 12);
      resHandle = Utility.getLong (buffer, ptr + 16);

      this.buffer = buffer;
      this.ptr = ptr;
    }

    // -------------------------------------------------------------------------------//
    @Override
    public String toString ()
    // -------------------------------------------------------------------------------//
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Type .......... %04X%n", resType));
      text.append (String.format ("ID ............ %04X  %<d%n", resId));
      text.append (String.format ("Offset ........ %04X  %<d%n", resOffset));
      text.append (String.format ("Attr .......... %04X  %<d%n", resAttr));
      text.append (String.format ("Size .......... %04X  %<d%n", resSize));
      text.append (String.format ("Handle ........ %04X  %<d%n%n", resHandle));

      text.append (HexFormatter.format (buffer, resOffset, resSize));
      text.append ("\n\n");

      return text.toString ();
    }
  }
}
