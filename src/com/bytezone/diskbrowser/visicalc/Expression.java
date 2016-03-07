package com.bytezone.diskbrowser.visicalc;

import java.util.regex.Pattern;

public class Expression
{
  // Expressions:
  //  - number
  //  - cell address
  //  - function
  //  - expression [+-*/^] expression
  //  - [+-=] expression
  //  - ( expression )

  private static final Pattern pattern = Pattern.compile ("");

  private boolean isUnavailable;
  private boolean isError;

  private boolean hasValue;
  private double value;
  private Function function;
  private Address address;

  private Expression expression1;
  private char operator;
  private Expression expression2;

  private final Sheet parent;

  public Expression (Sheet parent, String input)
  {
    this.parent = parent;
    String text = input.trim ();

    System.out.printf ("New expression:[%s]%n", input);
    char firstChar = text.charAt (0);
    if (firstChar == '-')
    {
      char secondChar = text.charAt (1);
      if ((secondChar >= '0' && secondChar <= '9') || secondChar == '.')
      {
        String text2 = text.substring (1);
        String numberText = getNumberText (text2);
        char op = getOperator (numberText, text2);
        if (op == ' ')
        {
          value = Double.parseDouble (numberText) * -1;
          hasValue = true;
        }
        else
          expression1 = new Expression (parent, "-" + numberText);
      }
      else
      {
        operator = '-';
        expression1 = new Expression (parent, text.substring (1));
      }
    }
    else if (firstChar == '=' || firstChar == '+')
    {
      expression1 = new Expression (parent, text.substring (1));
    }
    else if (firstChar == '@')
    {
      String functionText = getFunctionText (text);
      char op = getOperator (functionText, text);
      if (op == ' ')
        function = Function.getInstance (parent, functionText);
    }
    else if (firstChar == '(')
    {
      String bracketText = getFunctionText (text);
      char op = getOperator (bracketText, text);
      if (op == ' ')
        expression1 =
            new Expression (parent, bracketText.substring (1, bracketText.length () - 2));
    }
    else if ((firstChar >= '0' && firstChar <= '9') || firstChar == '.')
    {
      String numberText = getNumberText (text);
      char op = getOperator (numberText, text);
      if (op == ' ')
      {
        value = Double.parseDouble (numberText);
        hasValue = true;
      }
    }
    else if (firstChar >= 'A' && firstChar <= 'Z')
    {
      String addressText = getAddressText (text);
      char op = getOperator (addressText, text);
      if (op == ' ')
        address = new Address (addressText);
    }
    else
      System.out.printf ("Error processing [%s]%n", text);
  }

  private char getOperator (String text1, String text2)
  {
    if (text1.length () == text2.length ())
      return ' ';

    char op = text2.charAt (text1.length ());
    if (op == '+' || op == '-' || op == '*' || op == '/' || op == '^')
    {
      expression1 = new Expression (parent, text1);
      operator = op;
      expression2 = new Expression (parent, text2.substring (text1.length () + 1));
      return op;
    }

    System.out.println ("error");
    // error
    return '!';
  }

  double getValue ()
  {
    if (hasValue)
      return value;

    if (function != null)
      return function.getValue ();

    if (address != null)
    {
      Cell cell = parent.getCell (address);
      if (cell != null)
        return parent.getCell (address).getValue ();
      System.out.println ("Error with address");
      return 0;
    }

    if (expression2 == null)
    {
      if (operator == '-')
        return expression1.getValue () * -1;
      return expression1.getValue ();
    }

    switch (operator)
    {
      case ' ':
        return expression1.getValue ();
      case '+':
        return expression1.getValue () + expression2.getValue ();
      case '-':
        return expression1.getValue () - expression2.getValue ();
      case '*':
        return expression1.getValue () * expression2.getValue ();
      case '/':
        return expression1.getValue () / expression2.getValue ();
      case '^':
        return Math.pow (expression1.getValue (), expression2.getValue ());
    }

    System.out.println ("Unresolved value");
    return 0;
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
    return text.substring (0, ptr);
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

    text.append (String.format ("Has value ......... %s%n", hasValue));
    text.append (String.format ("Value ............. %f%n", value));
    text.append (String.format ("Function .......... %s%n", function));
    text.append (String.format ("Address ........... %s%n", address));
    text.append (String.format ("Operator .......... %s%n", operator));
    text.append (String.format ("Expression1 ....... %s%n", expression1));
    text.append (String.format ("Expression2 ....... %s%n", expression2));

    return text.toString ();
  }

  public static void main (String[] args)
  {
    Expression ex = new Expression (null, "-5+12-6");
    System.out.println (ex.getValue ());
  }
}