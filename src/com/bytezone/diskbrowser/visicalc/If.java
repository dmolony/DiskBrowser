package com.bytezone.diskbrowser.visicalc;

public class If extends Function
{
  private static String[] comparators = { "<>", "<=", ">=", "=", "<", ">" };

  private final String condition;
  private final String textTrue;
  private final String textFalse;
  private String comparator;
  private String cond;
  private String value;

  private Expression expTrue;
  private Expression expFalse;
  private Expression expCond;
  private Expression expValue;

  public If (Sheet parent, String text)
  {
    super (parent, text);

    text = text.substring (4, text.length () - 1);
    System.out.println (text);
    int pos1 = text.indexOf (',');
    int pos2 = text.indexOf (',', pos1 + 1);
    condition = text.substring (0, pos1);
    textTrue = text.substring (pos1 + 1, pos2);
    textFalse = text.substring (pos2 + 1);

    System.out.printf ("Cond:%s, true=%s, false=%s%n", condition, textTrue, textFalse);

    for (String comp : comparators)
    {
      int pos = condition.indexOf (comp);
      if (pos > 0)
      {
        cond = condition.substring (0, pos);
        value = condition.substring (pos + comp.length ());
        comparator = comp;
        break;
      }
    }

    if (comparator == null)
    {
      if (condition.startsWith ("@"))
      {
        cond = condition;
        value = "1";
        comparator = "=";
      }
      else
        System.out.println ("No comparator and not a function");
    }

    System.out.printf ("cond=%s, op=%s, value=%s%n", cond, comparator, value);
  }

  @Override
  public double getValue ()
  {
    if (expCond == null)
    {
      expCond = new Expression (parent, cond);
      expValue = new Expression (parent, value);
    }

    double condValue = expCond.getValue ();
    double valueValue = expValue.getValue ();

    boolean result;

    if (comparator.equals ("="))
      result = condValue == valueValue;
    else if (comparator.equals ("<>"))
      result = condValue != valueValue;
    else if (comparator.equals ("<"))
      result = condValue < valueValue;
    else if (comparator.equals (">"))
      result = condValue > valueValue;
    else if (comparator.equals ("<="))
      result = condValue <= valueValue;
    else if (comparator.equals (">="))
      result = condValue >= valueValue;
    else
    {
      System.out.printf ("Unexpected comparator result [%s]%n", comparator);
      return 0;
    }

    if (result)
    {
      if (expTrue == null)
        expTrue = new Expression (parent, textTrue);
      return expTrue.getValue ();
    }
    else
    {
      if (expFalse == null)
        expFalse = new Expression (parent, textFalse);
      return expFalse.getValue ();
    }
  }
}