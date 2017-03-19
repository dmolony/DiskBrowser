package com.bytezone.diskbrowser.visicalc;

class If extends Function
{
  private final String conditionText;
  private final String textTrue;
  private final String textFalse;

  private final Condition condition;
  private final Expression expTrue;
  private final Expression expFalse;

  public If (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@IF(") : text;

    conditionText = Expression.getParameter (functionText);
    textTrue =
        Expression.getParameter (functionText.substring (conditionText.length () + 1));
    textFalse = Expression.getParameter (
        functionText.substring (conditionText.length () + textTrue.length () + 2));

    condition = new Condition (parent, cell, conditionText);
    values.add (condition);

    expTrue = new Expression (cell, textTrue);
    values.add (expTrue);

    expFalse = new Expression (cell, textFalse);
    values.add (expFalse);
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