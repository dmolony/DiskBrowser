package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Expression implements Value
{
  // Expressions:
  //  - number
  //  - cell address
  //  - function
  //  - expression [+-*/^] expression
  //  - [+-=] expression
  //  - ( expression )

  // From the reference card:
  // Expressions are evaluated strictly from left to right except as modified by
  // parentheses. You must start an expression with a +, a digit (0-9), or one of
  // the symbols @-(. or #.

  private static final Pattern pattern = Pattern.compile ("");

  private boolean isUnavailable;
  private boolean isError;

  private final List<Value> values = new ArrayList<Value> ();
  private final List<String> operators = new ArrayList<String> ();

  public Expression (Sheet parent, String input)
  {
    String line = input.trim ();

    //    System.out.printf ("New expression [%s]%n", input);

    if (line.startsWith ("-"))
      line = "0" + line;
    else if (line.startsWith ("+"))
      line = line.substring (1);

    int ptr = 0;
    while (ptr < line.length ())
    {
      char ch = line.charAt (ptr);

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

          while (bracketText.startsWith ("(") && bracketText.endsWith (")"))
            bracketText = bracketText.substring (1, bracketText.length () - 1);

          values.add (new Expression (parent, bracketText));
          break;

        default:
          if (ch == '.' || (ch >= '0' && ch <= '9'))        // number
          {
            String numberText = getNumberText (line.substring (ptr));
            ptr += numberText.length ();
            values.add (new Number (Double.parseDouble (numberText)));
          }
          else if (ch >= 'A' && ch <= 'Z')                  // cell address
          {
            String addressText = getAddressText (line.substring (ptr));
            ptr += addressText.length ();
            values.add (parent.getCell (new Address (addressText)));
          }
          else
          {
            System.out.printf ("Unknown character [%s] in [%s]%n", ch, line);
            return;
          }
      }

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
    //    ptr = 0;
    //    for (Value val : values)
    //    {
    //      System.out.println (val.getValue ());
    //      if (ptr < operators.size ())
    //        System.out.println (operators.get (ptr++));
    //    }
  }

  @Override
  public double getValue ()
  {
    double value = values.get (0).getValue ();
    for (int i = 1; i < values.size (); i++)
    {
      double nextValue = values.get (i).getValue ();
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
    return value;
  }

  private String getFunctionText (String text)
  {
    int ptr = text.indexOf ('(');         // find first left parenthesis
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
    StringBuilder text = new StringBuilder ();

    //    text.append (String.format ("Has value ......... %s%n", hasValue));
    //    text.append (String.format ("Value ............. %f%n", value));
    //    text.append (String.format ("Function .......... %s%n", function));
    //    text.append (String.format ("Address ........... %s%n", address));
    //    text.append (String.format ("Operator .......... %s%n", operator));
    //    text.append (String.format ("Expression1 ....... %s%n", expression1));
    //    text.append (String.format ("Expression2 ....... %s%n", expression2));
    int ptr = 0;
    for (Value value : values)
    {
      text.append (value.getValue ());
      if (ptr < operators.size ())
        text.append (operators.get (ptr++));
    }

    return text.toString ();
  }

  public static void main (String[] args)
  {
    Expression ex = new Expression (null, "5+((4-(10-2)+6/3))*2");
    System.out.println (ex.getValue ());
  }
}