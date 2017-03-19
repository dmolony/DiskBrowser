package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Expression extends AbstractValue implements Iterable<Value>
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

  private final Cell cell;
  private final List<String> operators = new ArrayList<String> ();
  private final List<String> signs = new ArrayList<String> ();

  private final String text;

  public Expression (Cell cell, String text)
  {
    super ("Exp");
    this.cell = cell;
    this.text = text;
    Sheet parent = cell.getParent ();

    String line = balanceBrackets (text);   // add trailing right brackets if necessary

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
          String functionText = getFunctionCall (line.substring (ptr));
          ptr += functionText.length ();
          values.add (cell.getFunction (functionText));
          break;

        case '(':                                           // parentheses block
          String bracketText = getBalancedText (line.substring (ptr));
          ptr += bracketText.length ();
          values.add (new Expression (cell,
              bracketText.substring (1, bracketText.length () - 1)));
          break;

        case '#':                                           // no idea
          System.out.printf ("Hash character [%s] in [%s]%n", ch, line);
          ptr++;
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

  Value reduce ()
  {
    if (values.size () == 1 && signs.get (0).equals ("(+)"))
      return values.get (0);
    return this;
  }

  int size ()
  {
    return values.size ();
  }

  Value get (int index)
  {
    return values.get (index);
  }

  @Override
  public void calculate ()
  {
    if (values.size () == 0)
    {
      System.out.println ("nothing to calculate: " + text);
      return;
    }

    if (!isVolatile)
      return;

    boolean currentVolatile = false;

    //    boolean debug = cell.getAddress ().matches ("C8");
    boolean debug = false;
    if (debug)
    {
      System.out.println (this);
      System.out.printf ("(1) exp %s is currently %svolatile%n", text,
          isVolatile ? " " : "not ");
    }

    try
    {
      Value thisValue = values.get (0);
      thisValue.calculate ();
      if (debug)
      {
        System.out.println (this);
        System.out.printf ("(2) exp %s is currently %svolatile%n", thisValue.getText (),
            thisValue.isVolatile () ? " " : "not ");
      }

      value = 0;
      if (!thisValue.isValueType (ValueType.VALUE))
      {
        valueType = thisValue.getValueType ();
        return;
      }

      value = thisValue.getValue ();
      if (!currentVolatile)
        currentVolatile = thisValue.isVolatile ();

      String sign = signs.get (0);
      if (sign.equals ("(-)"))
        value *= -1;

      for (int i = 1; i < values.size (); i++)
      {
        thisValue = values.get (i);
        thisValue.calculate ();

        if (!thisValue.isValueType (ValueType.VALUE))
        {
          valueType = thisValue.getValueType ();
          return;
        }

        double nextValue = thisValue.getValue ();
        if (debug)
        {
          System.out.println (this);
          System.out.printf ("(3.%d) exp %s is currently %svolatile%n", i,
              thisValue.getText (), thisValue.isVolatile () ? " " : "not ");
        }
        if (!currentVolatile)
          currentVolatile = thisValue.isVolatile ();

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
        {
          if (nextValue == 0)
          {
            valueType = ValueType.ERROR;
            return;
          }
          value /= nextValue;
        }
        else if (operator.equals ("^"))
          value = Math.pow (value, nextValue);
      }

      if (Double.isNaN (value))
        valueType = ValueType.ERROR;
      else
        valueType = ValueType.VALUE;
    }
    catch (Exception e)
    {
      valueType = ValueType.ERROR;
      e.printStackTrace ();
      return;
    }

    isVolatile = currentVolatile;
    if (debug)
    {
      System.out.println (this);
      System.out.printf ("(4) exp %s is currently %svolatile%n", text,
          isVolatile ? " " : "not ");
      System.out.println ();
    }
  }

  private String balanceBrackets (String input)
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

  // called for functions and expressions
  private String getBalancedText (String text)
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

  // reads text up to the next comma that is not part of a function
  // text does not include the outer brackets or calling function name
  static String getParameter (String text)
  {
    int depth = 0;
    int ptr = 0;

    while (ptr < text.length ())
    {
      char c = text.charAt (ptr);
      if (c == '(')
        ++depth;
      else if (c == ')')
        --depth;
      else if (c == ',' && depth == 0)
        break;
      ++ptr;
    }

    return text.substring (0, ptr);
  }

  // receives a string starting with the function name
  private String getFunctionCall (String text)
  {
    if (text.charAt (0) != '@')
      throw new IllegalArgumentException ("Bad function name: " + text);

    for (String functionName : Function.functionList)
      if (text.startsWith (functionName))
      {
        if (functionName.endsWith ("("))          // if function has parameters
          return getBalancedText (text);          //   return full function call
        return functionName;                      // return function name only
      }

    throw new IllegalArgumentException ("Bad function name: " + text);
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

  public String fullText ()
  {
    StringBuilder text = new StringBuilder ();

    int ptr = 0;
    for (Value value : values)
    {
      assert value != null;
      text.append (signs.get (ptr));
      text.append (value.getValue ());
      if (ptr < operators.size ())
        text.append (operators.get (ptr++));
    }

    return text.toString ();
  }

  @Override
  public Iterator<Value> iterator ()
  {
    return values.iterator ();
  }

  @Override
  public String toString ()
  {
    return "Expression : " + text;
  }
}