package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;

// -----------------------------------------------------------------------------------//
class Expression extends AbstractValue
// -----------------------------------------------------------------------------------//
{
  private final List<String> operators = new ArrayList<> ();
  private final List<String> signs = new ArrayList<> ();

  // ---------------------------------------------------------------------------------//
  Expression (Cell cell, String text)
  // ---------------------------------------------------------------------------------//
  {
    super (cell, text);

    String line = balanceBrackets (text);   // add trailing right brackets if necessary

    if (Condition.isCondition (text))
    {
      values.add (new Condition (cell, text));
      signs.add ("(+)");                        // reduce() needs this
      return;
    }

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
            values.add (new Number (cell, numberText));
          }
          else if (ch >= 'A' && ch <= 'Z')                  // cell address
          {
            String addressText = getAddressText (line.substring (ptr));
            ptr += addressText.length ();
            values.add (cell.getCell (addressText));
          }
          else
          {
            System.out.printf ("Unexpected character [%s] in [%s]%n", ch, line);
            return;
          }
      }

      // check for possible continuation operator
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
    valueType = values.get (0).getValueType ();
  }

  // ---------------------------------------------------------------------------------//
  Value reduce ()
  // ---------------------------------------------------------------------------------//
  {
    return values.size () == 1 && signs.get (0).equals ("(+)") ? values.get (0) : this;
  }

  // ---------------------------------------------------------------------------------//
  Value get (int index)
  // ---------------------------------------------------------------------------------//
  {
    if (index < 0 || index >= values.size ())
      throw new IllegalArgumentException ();
    return values.get (index);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getType ()
  // ---------------------------------------------------------------------------------//
  {
    return "Expression";
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void calculate ()
  // ---------------------------------------------------------------------------------//
  {
    assert values.size () > 0;

    try
    {
      Value thisValue = values.get (0);
      thisValue.calculate ();

      if (valueType == ValueType.NUMBER)
        value = thisValue.getDouble ();
      else
        bool = thisValue.getBoolean ();

      if (!thisValue.isValid ())            // ERROR / NA
      {
        valueType = thisValue.getValueType ();
        return;
      }

      String sign = signs.get (0);
      if (sign.equals ("(-)"))
        value *= -1;

      for (int i = 1; i < values.size (); i++)      // only NUMBER will enter here
      {
        thisValue = values.get (i);
        thisValue.calculate ();

        if (!thisValue.isValid ())
        {
          valueType = thisValue.getValueType ();
          return;
        }

        double nextValue = thisValue.getDouble ();

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
            valueResult = ValueResult.ERROR;
            return;
          }
          value /= nextValue;
        }
        else if (operator.equals ("^"))
          value = Math.pow (value, nextValue);

        if (Double.isNaN (value))
        {
          valueResult = ValueResult.ERROR;
          return;
        }
      }
    }
    catch (Exception e)
    {
      valueResult = ValueResult.ERROR;
      e.printStackTrace ();
      return;
    }
  }

  // ---------------------------------------------------------------------------------//
  private String balanceBrackets (String input)
  // ---------------------------------------------------------------------------------//
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
  // ---------------------------------------------------------------------------------//
  private String getBalancedText (String text)
  // ---------------------------------------------------------------------------------//
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
  // ---------------------------------------------------------------------------------//
  static String getParameter (String text)
  // ---------------------------------------------------------------------------------//
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
  // ---------------------------------------------------------------------------------//
  private String getFunctionCall (String text)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  private String getNumberText (String text)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  private String getAddressText (String text)
  // ---------------------------------------------------------------------------------//
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

  // ---------------------------------------------------------------------------------//
  public String fullText ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();

    int ptr = 0;
    for (Value value : values)
    {
      assert value != null;
      text.append (signs.get (ptr));
      text.append (value.getDouble ());
      if (ptr < operators.size ())
        text.append (operators.get (ptr++));
    }

    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String toString ()
  // ---------------------------------------------------------------------------------//
  {
    StringBuilder text = new StringBuilder ();
    text.append (String.format ("%s%n", LINE));
    text.append (String.format (FMT4, "Exprssion", getFullText (), valueType,
        getValueText (this)));
    int index = 0;
    for (Value value : values)
    {
      String sign = signs.get (index);
      if (!"(+)".equals (sign))
        text.append (String.format (FMT2, "sign", sign));
      text.append (String.format (FMT4, value.getType (), value.getFullText (),
          value.getValueType (), getValueText (value)));
      if (index < operators.size ())
        text.append (String.format (FMT2, "operator", operators.get (index)));
      ++index;
    }
    return text.toString ();
  }
}