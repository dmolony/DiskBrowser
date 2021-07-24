package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.bytezone.diskbrowser.applefile.AbstractFile;
import com.bytezone.diskbrowser.utilities.HexFormatter;

// -----------------------------------------------------------------------------------//
class ZObject extends AbstractFile implements Comparable<ZObject>
// -----------------------------------------------------------------------------------//
{
  static final int HEADER_SIZE = 9;

  private final Header header;
  private final int id;
  private final int startPtr;

  private final int propertyTablePtr;
  private final int propertyTableLength;

  final int parent, sibling, child;
  final List<Property> properties = new ArrayList<> ();
  final BitSet attributes = new BitSet (32);

  // ---------------------------------------------------------------------------------//
  ZObject (String name, byte[] buffer, int offset, int id, Header header)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    this.header = header;
    this.startPtr = offset;
    this.id = id;

    // 32 attributes
    int bitIndex = 0;
    for (int i = 0; i < 4; i++)
    {
      byte b = buffer[offset + i];
      for (int j = 0; j < 8; j++)
      {
        if ((b & 0x80) != 0)
          attributes.set (bitIndex);
        b <<= 1;
        ++bitIndex;
      }
    }

    // object's relatives
    parent = header.getByte (offset + 4);
    sibling = header.getByte (offset + 5);
    child = header.getByte (offset + 6);

    // the property header contains the object's short name
    propertyTablePtr = header.getWord (offset + 7);
    int ptr = propertyTablePtr;
    int nameLength = header.getByte (ptr) * 2;
    setName (nameLength == 0 ? "<<" + id + ">>" : new ZString (header, ++ptr).value);
    ptr += nameLength;

    // read each property
    while (buffer[ptr] != 0)
    {
      Property p = new Property (ptr);
      properties.add (p);
      ptr += p.length + 1;
    }
    propertyTableLength = ptr - propertyTablePtr;
  }

  // ---------------------------------------------------------------------------------//
  int getId ()
  // ---------------------------------------------------------------------------------//
  {
    return id;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("ID       : %02X  (%<3d)  %s%n%n", id, getName ()));

    String obj1 = parent == 0 ? "" : header.getObject (parent - 1).getName ();
    String obj2 = sibling == 0 ? "" : header.getObject (sibling - 1).getName ();
    String obj3 = child == 0 ? "" : header.getObject (child - 1).getName ();

    text.append (String.format ("Parent   : %02X  (%<3d)  %s%n", parent, obj1));
    text.append (String.format ("Sibling  : %02X  (%<3d)  %s%n", sibling, obj2));
    text.append (String.format ("Child    : %02X  (%<3d)  %s%n%n", child, obj3));

    text.append ("Attributes : ");
    text.append (HexFormatter.getHexString (buffer, startPtr, 4));
    text.append ("   " + attributes.toString () + "\n\n");

    for (Property prop : properties)
      text.append (prop + "\n");

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getHexDump ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ("Header :\n\n");
    text.append (HexFormatter.formatNoHeader (buffer, startPtr, HEADER_SIZE));
    text.append ("\n\nProperty table:\n\n");
    text.append (
        HexFormatter.formatNoHeader (buffer, propertyTablePtr, propertyTableLength));
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  Property getProperty (int id)
  // ---------------------------------------------------------------------------------//
  {
    for (Property p : properties)
      if (p.propertyNumber == id)
        return p;
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    return HexFormatter.getHexString (buffer, startPtr, HEADER_SIZE) + "   " + getName ();
  }

  // ---------------------------------------------------------------------------------//
  public String getDescription (List<ZObject> list)
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder (String.format (" %-40s", getName ()));

    for (int i = 4; i < 7; i++)
    {
      int index = buffer[startPtr + i] & 0xFF;
      String name = index > 0 ? list.get (index - 1).getName () : "";
      text.append (String.format (" %-40s", name));
    }

    text.append (" ");
    text.append (HexFormatter.getHexString (buffer, startPtr, 4));
    text.append ("  ");
    text.append (HexFormatter.getHexString (buffer, startPtr + 7, 2));

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  class Property
  // ---------------------------------------------------------------------------------//
  {
    int propertyNumber;
    int ptr;
    int length;
    int offset;                 //  only used if length == 2

    public Property (int ptr)
    {
      this.ptr = ptr;
      length = header.getByte (ptr) / 32 + 1;
      propertyNumber = header.getByte (ptr) % 32;

      if (length == 2)
        offset = header.getWord (ptr + 1) * 2;
    }

    private ZObject getObject ()
    {
      return header.getObject ((buffer[ptr + 1] & 0xFF) - 1);
    }

    private ZObject getObject (int id)
    {
      return header.getObject (id - 1);
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder (
          String.format ("%8s : ", header.getPropertyName (propertyNumber)));

      String propertyType = header.getPropertyName (propertyNumber);

      if (!(propertyType.equals ("DICT") || propertyType.startsWith ("STR")))
        text.append (
            String.format ("%-20s", HexFormatter.getHexString (buffer, ptr + 1, length)));

      if (propertyNumber >= 19)                   // directions
      {
        ZObject object = getObject ();
        String objectName = object == null ? "no object" : object.getName ();

        switch (length)
        {
          case 1:                                 // UEXIT - unconditional exit
            text.append (objectName);
            break;
          case 2:
            text.append ("\"" + header.stringManager.stringAt (offset) + "\"");
            break;
          case 3:                                 // FEXIT - function exit
            int address = header.getWord (ptr + 1) * 2;
            text.append (String.format ("R:%05X", address));
            appendRoutine (text, address);
            break;
          case 4:
            text.append (String.format ("%s : IF G%02X ELSE ", objectName,
                header.getByte (ptr + 2)));
            address = header.getWord (ptr + 3) * 2;
            if (address > 0)
              text.append ("\"" + header.stringManager.stringAt (address) + "\"");
            break;
          case 5:
            text.append (
                String.format ("%s : IF G%02X ", objectName, header.getByte (ptr + 2)));
            break;
          default:
            break;
        }
      }
      else if (propertyType.equals ("DICT"))
      {
        for (int i = 1; i <= length; i += 2)
        {
          int address = header.getWord (ptr + i);
          text.append (String.format ("%02X: %s, ", address, header.wordAt (address)));
        }
        text.deleteCharAt (text.length () - 1);
        text.deleteCharAt (text.length () - 1);
      }
      else if (propertyType.startsWith ("CODE"))
      {
        if (offset > 0)          // cretin contains 00 00
          appendRoutine (text, offset);
      }
      else if (propertyType.startsWith ("STR"))
      {
        text.append (String.format ("(%4X) \"%s\"", offset,
            header.stringManager.stringAt (offset)));
      }
      else if (propertyType.equals ("ADJ"))
      {

      }
      else if (propertyType.equals ("SIZE"))
      {

      }
      else if (propertyType.equals ("VALUE"))
      {

      }
      else if (propertyType.equals ("TVALU"))
      {

      }
      else if (propertyType.equals ("GLBL"))
      {
        for (int i = 0; i < length; i++)
        {
          int objectId = header.getByte (ptr + i + 1);
          text.append (String.format ("%s%s", (i == 0 ? "" : ", "),
              getObject (objectId).getName ()));
        }
      }
      //      else
      //        text.append ("Unknown property type: " + propertyType);

      return text.toString ();
    }

    private void appendRoutine (StringBuilder text, int offset)
    {
      Routine r = header.codeManager.getRoutine (offset);
      if (r != null)
        text.append ("\n\n" + r.getText ());
      else                  // this can happen if the property is mislabelled as code
        text.append ("\n\n****** null routine\n");
    }
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public int compareTo (ZObject o)
  // ---------------------------------------------------------------------------------//
  {
    return this.getName ().compareTo (o.getName ());
  }
}