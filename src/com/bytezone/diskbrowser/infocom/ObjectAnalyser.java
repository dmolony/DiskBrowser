package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bytezone.diskbrowser.infocom.ZObject.Property;

class ObjectAnalyser
{
  Header header;
  ObjectManager parent;
  List<Statistics> list = new ArrayList<Statistics> ();
  List<Integer> routines = new ArrayList<Integer> ();

  public ObjectAnalyser (Header header, ObjectManager parent)
  {
    this.header = header;
    this.parent = parent;

    // assign the DICT property for each object
    setDictionary ();

    // find the point where code ends and strings begin
    setStringPointer ();

    // add routines called from object properties (requires stringPointer)
    createPropertyLinks ();

    // assumes that all properties with exactly three bytes are routine addresses
    //		checkThreeByteProperties ();
  }

  public void setStringPointer ()
  {
    PropertyTester pt = new PropertyTester (parent.getObjects ());
    pt.addTest (new LengthTwoCondition ());
    HighMemoryCondition hmc = new HighMemoryCondition ();
    pt.addTest (hmc);
    pt.doTests ();

    for (Integer propertyNo : pt)       // list of all properties that passed all tests
      list.add (hmc.statistics[propertyNo]);
    Collections.sort (list);

    // Calculate lowest string pointer
    int lo = list.get (0).lo;
    for (Statistics s : list)
    {
      System.out.println (s);
      if (s.hi > lo && s.lo < lo)
        lo = s.lo;
      if (s.hi < lo)
      {
        header.stringPointer = lo;
        break;
      }
    }
  }

  public void createPropertyLinks ()
  {
    int sCount = 0;
    int rCount = 0;
    int totStrings = 0;
    int totRoutines = 0;

    for (Statistics s : list)
    {
      if (header.getPropertyName (s.propertyNumber).charAt (0) >= 'a')
        continue;
      if (s.lo >= header.stringPointer)
      {
        header.propertyNames[s.propertyNumber] = "STR" + ++sCount;
        totStrings += s.offsets.size ();
      }
      else
      {
        header.propertyNames[s.propertyNumber] = "CODE" + ++rCount;
        routines.addAll (s.offsets);
        totRoutines += s.offsets.size ();
      }
    }
    System.out.println ("Strings found : " + totStrings);
    System.out.println ("Routines found : " + totRoutines);
  }

  //  private void checkThreeByteProperties ()
  //  {
  //    for (ZObject object : parent.getObjects ())
  //    {
  //      for (Property property : object.properties)
  //      {
  //        if (header.getPropertyName (property.propertyNumber).charAt (0) < 'a'
  //            && property.length == 3)
  //        {
  //          int address = header.getWord (property.ptr + 1) * 2;
  //          System.out.println ("checking " + address);
  //          header.codeManager.addRoutine (address, 0);
  //        }
  //      }
  //    }
  //  }

  // find the property with only dictionary entries
  public void setDictionary ()
  {
    PropertyTester pt = new PropertyTester (parent.getObjects ());
    pt.addTest (new LengthEvenCondition ());
    pt.addTest (new ValidDictionaryCondition ());
    pt.doTests ();

    for (Integer i : pt)
      // should only be one
      header.propertyNames[i] = "DICT";       // SYNONYM
  }

  class Statistics implements Comparable<Statistics>
  {
    int propertyNumber;
    int lo;
    int hi;
    List<Integer> offsets = new ArrayList<Integer> ();

    public Statistics (int propertyNumber)
    {
      this.propertyNumber = propertyNumber;
    }

    public void increment (Property property)
    {
      offsets.add (property.offset);
      if (property.offset > hi)
        hi = property.offset;
      if (property.offset < lo || lo == 0)
        lo = property.offset;
    }

    @Override
    public String toString ()
    {
      return String.format ("%2d   %3d   %,7d   %,7d", propertyNumber, offsets.size (),
          lo, hi);
    }

    @Override
    public int compareTo (Statistics o)
    {
      return o.hi - hi;
    }
  }

  class LengthTwoCondition extends Condition
  {
    @Override
    boolean test (Property property)
    {
      return property.length == 2;
    }
  }

  class LengthThreeCondition extends Condition
  {
    @Override
    boolean test (Property property)
    {
      return property.length == 3;
    }
  }

  class LengthEvenCondition extends Condition
  {
    @Override
    boolean test (Property property)
    {
      return (property.length % 2) == 0;
    }
  }

  class HighMemoryCondition extends Condition
  {
    int lo, hi;
    Statistics[] statistics = new Statistics[32]; // note there is no property #0

    public HighMemoryCondition ()
    {
      lo = header.highMemory;
      hi = header.fileLength;
      for (int i = 1; i < statistics.length; i++)
        statistics[i] = new Statistics (i);
    }

    @Override
    boolean test (Property property)
    {
      statistics[property.propertyNumber].increment (property);
      int address = header.getWord (property.ptr + 1) * 2;
      return (address >= lo && address < hi) || address == 0;
    }
  }

  class ValidDictionaryCondition extends Condition
  {
    @Override
    boolean test (Property property)
    {
      for (int i = 1; i <= property.length; i += 2)
      {
        int address = header.getWord (property.ptr + i);
        if (!header.containsWordAt (address))
          return false;
      }
      return true;
    }
  }
}