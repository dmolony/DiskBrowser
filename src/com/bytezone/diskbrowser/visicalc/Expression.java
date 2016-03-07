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

    char firstChar = text.charAt (0);
    if (firstChar == '-')
    {
      operator = '-';
      expression1 = new Expression (parent, text.substring (1));
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
      else if (op != '!')
        setExpressions (functionText, op, text);
    }
    else if (firstChar == '(')
    {
      String bracketText = getFunctionText (text);
      char op = getOperator (bracketText, text);
      if (op == ' ')
        expression1 =
            new Expression (parent, bracketText.substring (1, bracketText.length () - 2));
      else if (op != '!')
        setExpressions (bracketText, op, text);
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
      else if (op != '!')
        setExpressions (numberText, op, text);
    }
    else if (firstChar >= 'A' && firstChar <= 'Z')
    {
      String addressText = getAddressText (text);
      char op = getOperator (addressText, text);
      if (op == ' ')
        address = new Address (addressText);
      else if (op != '!')
        setExpressions (addressText, op, text);
    }
    else
      System.out.printf ("Error processing [%s]%n", text);
  }

  private void setExpressions (String text1, char op, String text2)
  {
    expression1 = new Expression (parent, text1);
    operator = op;
    expression2 = new Expression (parent, text2.substring (text1.length () + 2));
  }

  double getValue ()
  {
    if (hasValue)
      return value;

    if (function != null)
      return function.getValue ();

    if (address != null)
      return parent.getCell (address).getValue ();

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

  private char getOperator (String text1, String text2)
  {
    if (text1.length () == text2.length ())
      return ' ';

    char c = text2.charAt (text1.length ());
    if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^')
    {
      setExpressions (text1, c, text2);
      return c;
    }

    // error
    return '!';
  }
}