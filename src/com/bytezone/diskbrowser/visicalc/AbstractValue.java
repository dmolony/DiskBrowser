package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractValue implements Value//, Iterable<Value>
{
  protected static final String FMT2 = "| %-9.9s : %-70.70s|%n";
  protected static final String FMT4 = "| %-9.9s : %-50.50s %-8.8s %-10.10s|%n";
  protected static final String FMT5 = "| %-9.9s : %-3.3s : %-45.45s%-8.8s %-10.10s|%n";
  protected static final String LINE = "+--------------------------------------------"
      + "---------------------------------------+";

  protected final Cell cell;
  protected final String fullText;

  protected ValueType valueType = ValueType.NUMBER;         // could be BOOLEAN
  protected double value;
  protected boolean bool;

  protected ValueResult valueResult = ValueResult.VALID;
  protected List<Value> values = new ArrayList<Value> ();

  public AbstractValue (Cell cell, String text)
  {
    this.cell = cell;
    this.fullText = text;
  }

  @Override
  public String getFullText ()
  {
    return fullText;
  }

  @Override
  public ValueType getValueType ()
  {
    return valueType;
  }

  @Override
  public ValueResult getValueResult ()
  {
    return valueResult;
  }

  @Override
  public boolean isValid ()
  {
    return valueResult == ValueResult.VALID;
  }

  @Override
  public double getDouble ()
  {
    return value;
  }

  @Override
  public boolean getBoolean ()
  {
    return bool;
  }

  @Override
  public int size ()
  {
    return values.size ();
  }

  @Override
  public String getText ()
  {
    switch (valueResult)
    {
      case NA:
        return "NA";
      case ERROR:
        return "ERROR";
      case VALID:
        switch (valueType)
        {
          case BOOLEAN:
            return bool ? "TRUE" : "FALSE";
          case NUMBER:
            return value + "";
          default:
            return "impossible";
        }
      default:
        return "impossible";
    }
  }

  @Override
  public void calculate ()
  {
    //    System.out.println ("calculate not overridden: " + cell);
  }

  protected String getValueText (Value value)
  {
    return "" + (value.getValueType () == ValueType.NUMBER ? value.getDouble ()
        : value.getBoolean ());
  }

  @Override
  public Iterator<Value> iterator ()
  {
    return values.iterator ();
  }
}