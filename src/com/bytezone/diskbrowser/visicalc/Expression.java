package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;

class Expression implements Value
{
  // Expressions:
  //   number
  //   cell address
  //   function
  //   expression [+-*/^] expression
  //   [+-] expression
  //   ( expression )

  // From the reference card:
  // Expressions are evaluated strictly from left to right except as modified by
  // parentheses. You must start an expression with a +, a digit (0-9), or one of
  // the symbols @-(. or #

  // [@IF(@ISERROR(BK24),0,BK24)]
  // [@IF(D4=0,0,1)]
  // [@IF(D4=0,0,B32+1)]
  // [@IF(D4=0,0,1+(D3/100/D4)^D4-1*100)]
  // [@SUM(C4...F4)]
  // [+C4-@SUM(C5...C12)]
  // [+D5/100/12]
  // [.3*(B4+B7+B8+B9)]
  // [+N12+(P12*(.2*K12+K9-O12))]

  private final List<Value> values = new ArrayList<Value> ();
  private final List<String> operators = new ArrayList<String> ();
  private final List<String> signs = new ArrayList<String> ();

  private ValueType valueType;
  private double value;
  private String text;

  public Expression (Sheet parent, String text)
  {
    this.text = text;
    String line = checkBrackets (text);

    int ptr = 0;
    while (ptr < line.length ())
    {
      // check for optional leading + or -
      char ch = line.charAt (ptr);
      if (ch == '-')
      {
        signs.add ("(-)");
        ch = line.charAt (++ptr);
      }
      else
      {
        signs.add ("(+)");
        if (ch == '+')
          ch = line.charAt (++ptr);
      }

      // check for mandatory function/sub-expression/number/cell reference
      switch (ch)
      {
        case '@':                                           // function
          String functionText = getFunctionText (line.substring (ptr));
          ptr += functionText.length ();
          values.add (Function.getInstance (parent, functionText));
          break;

        case '(':                                           // parentheses block
          String bracketText = getFunctionText (line.substring (ptr));
          ptr += bracketText.length ();
          values.add (new Expression (parent,
              bracketText.substring (1, bracketText.length () - 1)));
          break;

        case '#':
          System.out.printf ("Hash character [%s] in [%s]%n", ch, line);
          ptr++;                                            // no idea
          break;

        default:
          if (ch == '.' || (ch >= '0' && ch <= '9'))        // number
          {
            String numberText = getNumberText (line.substring (ptr));
            ptr += numberText.length ();
            values.add (new Number (numberText));
          }
          else if (ch >= 'A' && ch <= 'Z')                  // cell address
          {
            String addressText = getAddressText (line.substring (ptr));
            ptr += addressText.length ();
            Cell cell = parent.getCell (addressText);
            if (cell == null)
              values.add (Function.getInstance (parent, "@NA"));
            else
              values.add (parent.getCell (addressText));
          }
          else
          {
            System.out.printf ("Unexpected character [%s] in [%s]%n", ch, line);
            return;
          }
      }

      // check for optional continuation operator
      if (ptr < line.length ())
      {
        ch = line.charAt (ptr);
        if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '^')
          operators.add (line.substring (ptr, ++ptr));
        else
        {
          System.out.printf ("Unknown operator [%s] in [%s]%n", ch, line);
          return;
        }
      }
    }

    assert values.size () > 0;
  }

  @Override
  public Value calculate ()
  {
    try
    {
      Value thisValue = values.get (0);
      thisValue.calculate ();
      if (thisValue.isError ())
      {
        valueType = thisValue.getValueType ();
        return this;
      }
      value = thisValue.isNotAvailable () ? 0 : thisValue.getValue ();

      String sign = signs.get (0);
      if (sign.equals ("(-)"))
        value *= -1;

      for (int i = 1; i < values.size (); i++)
      {
        thisValue = values.get (i);
        thisValue.calculate ();
        if (thisValue.isError ())
        {
          valueType = thisValue.getValueType ();
          return this;
        }

        double nextValue = thisValue.isNotAvailable () ? 0 : thisValue.getValue ();

        sign = signs.get (i);
        if (sign.equals ("(-)"))
          nextValue *= -1;

        String operator = operators.get (i - 1);
        if (operator.equals ("+"))
          value += nextValue;
        else if (operator.equals ("-"))
          value -= nextValue;
        else if (operator.equals ("*"))
          value *= nextValue;
        else if (operator.equals ("/"))
          value /= nextValue;
        else if (operator.equals ("^"))
          value = Math.pow (value, nextValue);
      }

      if (Double.isNaN (value))
        valueType = ValueType.NAN;
      else
        valueType = ValueType.VALUE;
    }
    catch (Exception e)
    {
      valueType = ValueType.ERROR;
    }

    return this;
  }

  @Override
  public ValueType getValueType ()
  {
    return valueType;
  }

  @Override
  public boolean isValue ()
  {
    return valueType == ValueType.VALUE;
  }

  @Override
  public boolean isNotAvailable ()
  {
    return valueType == ValueType.NA;
  }

  @Override
  public boolean isNotANumber ()
  {
    return valueType == ValueType.NAN;
  }

  @Override
  public boolean isError ()
  {
    return valueType == ValueType.ERROR;
  }

  @Override
  public double getValue ()
  {
    //    assert valueType == ValueType.VALUE : "Expression ValueType = " + valueType;
    return value;
  }

  @Override
  public String getText ()
  {
    return isNotAvailable () ? "NA" : isError () ? "Error" : isNotANumber () ? "NaN" : "";
    //    return isNotAvailable () ? "NA" : isError () ? "Error" : "";
  }

  private String checkBrackets (String input)
  {
    String line = input.trim ();

    int leftBracket = 0;
    int rightBracket = 0;

    for (char c : line.toCharArray ())
      if (c == '(')
        leftBracket++;
      else if (c == ')')
        rightBracket++;

    if (leftBracket != rightBracket)
    {
      if (rightBracket > leftBracket)
      {
        System.out.printf ("**** Unbalanced brackets: left:%d, right:%d  ****%n",
                           leftBracket, rightBracket);
        System.out.println (input);
        return "@ERROR";
      }

      while (rightBracket < leftBracket)
      {
        line = line + ")";
        rightBracket++;
      }
    }
    return line;
  }

  private String getFunctionText (String text)
  {
    int ptr = text.indexOf ('(');         // find first left parenthesis
    if (ptr < 0)
      return text;
    int depth = 1;

    while (++ptr < text.length ())        // find matching right parenthesis
    {
      if (text.charAt (ptr) == ')')
      {
        --depth;
        if (depth == 0)
          break;
      }
      else if (text.charAt (ptr) == '(')
        ++depth;
    }

    return text.substring (0, ptr + 1);   // include closing parenthesis
  }

  private String getNumberText (String text)
  {
    int ptr = 0;
    while (++ptr < text.length ())
    {
      char c = text.charAt (ptr);
      if (c != '.' && (c < '0' || c > '9'))
        break;
    }
    return text.substring (0, ptr);
  }

  private String getAddressText (String text)
  {
    int ptr = 0;
    while (++ptr < text.length ())
    {
      char c = text.charAt (ptr);
      if ((c < '0' || c > '9') && (c < 'A' || c > 'Z'))
        break;
    }
    return text.substring (0, ptr);
  }

  @Override
  public String toString ()
  {
    //    StringBuilder text = new StringBuilder ();
    //
    //    int ptr = 0;
    //    for (Value value : values)
    //    {
    //      assert value != null;
    //      text.append (signs.get (ptr));
    //      //      value.calculate ();
    //      //      if (value.isValue ())
    //      //        text.append (value.getValue ());
    //      if (ptr < operators.size ())
    //      {
    //        //        System.out.println (operators.get (ptr));
    //        text.append (operators.get (ptr++));
    //      }
    //    }
    //    //    System.out.println ("finished building");
    //
    //    return text.toString ();
    return "Expression: " + text;
  }

  public static void main (String[] args)
  {
    Expression ex = new Expression (null, "-5+((-4-(20-(2^3))+6/3))*-2");
    System.out.println (ex.getValue ());
    System.out.println (ex);
  }
}