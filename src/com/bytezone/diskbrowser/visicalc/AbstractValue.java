package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractValue implements Value, Iterable<Value>
{
  protected final Cell cell;
  protected final String fullText;
  protected double value;
  protected boolean bool;
  protected ValueType valueType = ValueType.NUMBER;         // could be BOOLEAN
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

  //  @Override
  //  public boolean isValueType (ValueType type)
  //  {
  //    return valueType == type;
  //  }

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

  @Override
  public Iterator<Value> iterator ()
  {
    return values.iterator ();
  }

  // for debugging
  //  String getValueText (int depth)
  //  {
  //    StringBuilder text = new StringBuilder ();
  //
  //    String typeText = "  " + getTypeText ();
  //    if (getValueType () == ValueType.NUMBER)
  //    {
  //      String valueText = String.format ("%f", getDouble ());
  //      text.append (String.format ("| %-10s : %-69s |%n", typeText, valueText));
  //    }
  //    else if (getValueType () == ValueType.BOOLEAN)
  //    {
  //      String valueText = String.format ("%s", getBoolean ());
  //      text.append (String.format ("| %-10s : %-69s |%n", typeText, valueText));
  //    }
  //    else
  //      text.append (String.format ("| %-10s : %-69s |%n", typeText, getValueType ()));
  //
  //    if (this instanceof Expression)
  //    {
  //      text.append (
  //          String.format ("| Expression : %-69s |%n", ((Expression) this).fullText ()));
  //      for (Value v : (Expression) this)
  //      {
  //        if (v instanceof Cell)
  //        {
  //          Cell c = (Cell) v;
  //          ValueType vt = c.getValueType ();
  //          String tx = vt == ValueType.NUMBER ? c.getDouble () + "" : c.getBoolean () + "";
  //          text.append (
  //              String.format ("|   Cell %-3s : %-69s |%n", c.getAddressText (), tx));
  //        }
  //        else
  //          text.append (((AbstractValue) v).getValueText (depth + 1));
  //      }
  //    }
  //    else if (this instanceof Function)
  //    {
  //      text.append (
  //          String.format ("| Function   : %-69s |%n", ((Function) this).fullText));
  //      for (Value v : (Function) this)
  //        text.append (((AbstractValue) v).getValueText (depth + 1));
  //    }
  //    else if (this instanceof Condition)
  //    {
  //      text.append (
  //          String.format ("| Condition  : %-69s |%n", ((Condition) this).getFullText ()));
  //      for (Value v : (Condition) this)
  //        text.append (((AbstractValue) v).getValueText (depth + 1));
  //    }
  //
  //    return text.toString ();
  //  }
}