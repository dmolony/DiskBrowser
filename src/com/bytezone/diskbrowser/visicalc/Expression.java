package com.bytezone.diskbrowser.visicalc;

import java.util.ArrayList;
import java.util.List;

public class Expression implements Value
{
  // Expressions:
  //   number
  //   cell address
  //   function
  //   expression [+-*/^] expression
  //   [+-=] expression
  //   ( expression )
  //   -expression

  // From the reference card:
  // Expressions are evaluated strictly from left to right except as modified by
  // parentheses. You must start an expression with a +, a digit (0-9), or one of
  // the symbols @-(. or #.

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

  public Expression (Sheet parent, String input)
  {
    String line = input.trim ();

    //    System.out.printf ("New expression [%s]%n", input);

    int leftBracket = 0;
    int rightBracket = 0;

    for (char c : input.toCharArray ())
      if (c == '(')
        leftBracket++;
      else if (c == ')')
        rightBracket++;

    if (leftBracket != rightBracket)
    {
      System.out.printf ("**** Unbalanced brackets: left:%d, right:%d  ****%n",
                         leftBracket, rightBracket);
      line = "@ERROR()";
    }

    //    System.out.printf ("Exp [%s]%n", line);
    int ptr = 0;
    while (ptr < line.length ())
    {
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
          bracketText = bracketText.substring (1, bracketText.length () - 1);
          values.add (new Expression (parent, bracketText));
          break;

        case '#':
          System.out.printf ("Hash character [%s] in [%s]%n", ch, line);
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

    if (false)
    {
      ptr = 0;
      for (Value val : values)
      {
        System.out.println (signs.get (ptr));
        if (val == null)
          System.out.println ("null");
        else
          System.out.println (val.getValue ());
        if (ptr < operators.size ())
          System.out.println (operators.get (ptr++));
      }
    }
  }

  @Override
  public double getValue ()
  {
    Value thisValue = values.get (0);
    double value = thisValue == null ? 0 : values.get (0).getValue ();

    String sign = signs.get (0);
    if (sign.equals ("(-)"))
      value *= -1;

    for (int i = 1; i < values.size (); i++)
    {
      thisValue = values.get (i);
      double nextValue = thisValue == null ? 0 : thisValue.getValue ();

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
    return value;
  }

  private String getFunctionText (String text)
  {
    int ptr = text.indexOf ('(');         // find first left parenthesis
    if (ptr < 0)
      return "";
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

    int ptr = 0;
    for (Value value : values)
    {
      text.append (signs.get (ptr));
      text.append (value.getValue ());
      if (ptr < operators.size ())
        text.append (operators.get (ptr++));
    }

    return text.toString ();
  }

  public static void main (String[] args)
  {
    Expression ex = new Expression (null, "-5+((-4-(20-(2^3))+6/3))*-2");
    System.out.println (ex.getValue ());
    System.out.println (ex);
  }
}