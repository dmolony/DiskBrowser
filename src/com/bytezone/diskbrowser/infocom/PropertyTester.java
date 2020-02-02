package com.bytezone.diskbrowser.infocom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bytezone.diskbrowser.infocom.ZObject.Property;

class PropertyTester implements Iterable<Integer>
{
  List<ZObject> objects;
  List<Condition> conditions = new ArrayList<> ();
  List<Integer> matchedProperties;

  public PropertyTester (List<ZObject> objects)
  {
    this.objects = objects;
  }

  public void addTest (Condition test)
  {
    conditions.add (test);
  }

  public void doTests ()
  {
    boolean[] propFail = new boolean[32];
    int[] propTestCount = new int[32];
    matchedProperties = new ArrayList<> ();

    for (ZObject object : objects)
      propertyLoop: for (Property property : object.properties)
      {
        if (propFail[property.propertyNumber] || property.length == 0)
          continue;
        for (Condition condition : conditions)
          if (!condition.test (property))
          {
            propFail[property.propertyNumber] = true;
            continue propertyLoop;
          }
        ++propTestCount[property.propertyNumber];
      }

    for (int i = 1; i < propFail.length; i++)
      if (!propFail[i] && propTestCount[i] > 0)
        matchedProperties.add (i);
  }

  @Override
  public Iterator<Integer> iterator ()
  {
    return matchedProperties.iterator ();
  }

  public int totalSuccessfulProperties ()
  {
    return matchedProperties.size ();
  }
}

abstract class Condition
{
  abstract boolean test (Property property);
}