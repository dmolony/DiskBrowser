package com.bytezone.diskbrowser.visicalc;

class If extends Function
{
  private final String conditionText;
  private final String textTrue;
  private final String textFalse;

  private final Condition condition;
  private final Expression expTrue;
  private final Expression expFalse;

  public If (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    int pos1 = functionText.indexOf (',');
    int pos2 = -1;
    if (pos1 < 0)
      throw new IllegalArgumentException ("Not enough parameters for IF: " + text);

    conditionText = functionText.substring (0, pos1);
    condition = new Condition (parent, cell, conditionText);
    //    System.out.printf ("Cond : %s%n", conditionText);
    values.add (condition);

    if (functionText.charAt (pos1 + 1) == '@')
    {
      textTrue = Expression.getBalancedText (functionText.substring (pos1 + 1));
      //      System.out.printf ("True : %s%n", textTrue);
      expTrue = new Expression (parent, cell, textTrue);
      pos2 = functionText.indexOf (',', pos1 + textTrue.length () + 1);
    }
    else
    {
      pos2 = functionText.indexOf (',', pos1 + 1);
      textTrue = functionText.substring (pos1 + 1, pos2);
      //      System.out.printf ("True : %s%n", textTrue);
      expTrue = new Expression (parent, cell, functionText.substring (pos1 + 1, pos2));
    }
    values.add (expTrue);

    if (pos2 < 0)
      throw new IllegalArgumentException ("Not enough parameters for IF: " + text);

    textFalse = functionText.substring (pos2 + 1);
    //    System.out.printf ("False: %s%n", textFalse);
    expFalse = new Expression (parent, cell, textFalse);
    values.add (expFalse);

    //    System.out.println ();
  }

  @Override
  public void calculate ()
  {
    valueType = ValueType.VALUE;
    condition.calculate ();

    if (condition.getValue () == 1)
    {
      expTrue.calculate ();

      if (!expTrue.isValueType (ValueType.VALUE))
        valueType = expTrue.getValueType ();
      else
        value = expTrue.getValue ();
    }
    else
    {
      expFalse.calculate ();

      if (!expFalse.isValueType (ValueType.VALUE))
        valueType = expFalse.getValueType ();
      else
        value = expFalse.getValue ();
    }
  }

  @Override
  public String toString ()
  {
    return String.format ("[IF:%s, True:%s, False:%s]", condition, textTrue, textFalse);
  }
}