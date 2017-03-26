package com.bytezone.diskbrowser.visicalc;

class If extends Function
{
  private final String conditionText;
  private final String textTrue;
  private final String textFalse;

  private final Condition condition;
  private final Value expTrue;
  private final Value expFalse;

  public If (Cell cell, String text)
  {
    super (cell, text);

    assert text.startsWith ("@IF(") : text;

    conditionText = Expression.getParameter (functionText);

    int ptr = conditionText.length () + 1;
    if (ptr >= functionText.length ())
      throw new IllegalArgumentException (text);

    textTrue = Expression.getParameter (functionText.substring (ptr));

    ptr = conditionText.length () + textTrue.length () + 2;
    if (ptr >= functionText.length ())
      throw new IllegalArgumentException (text);

    textFalse = Expression.getParameter (functionText.substring (ptr));

    condition = new Condition (cell, conditionText);
    values.add (condition);

    expTrue = new Expression (cell, textTrue).reduce ();
    values.add (expTrue);

    expFalse = new Expression (cell, textFalse).reduce ();
    values.add (expFalse);

    valueType = expTrue.getValueType ();
  }

  @Override
  public void calculate ()
  {
    valueResult = ValueResult.VALID;

    condition.calculate ();

    if (condition.getBoolean ())        // true
    {
      expTrue.calculate ();

      if (!expTrue.isValid ())
        valueResult = expTrue.getValueResult ();
      else if (valueType == ValueType.NUMBER)
        value = expTrue.getDouble ();
      else
        bool = expTrue.getBoolean ();
    }
    else                                // false
    {
      expFalse.calculate ();

      if (!expFalse.isValid ())
        valueResult = expTrue.getValueResult ();
      else if (valueType == ValueType.NUMBER)
        value = expFalse.getDouble ();
      else
        bool = expFalse.getBoolean ();
    }
  }

  @Override
  public String getType ()
  {
    return "If";
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("%s%n", LINE));
    text.append (String.format (FMT4, getType (), getFullText (), getValueType (),
        getValueText (this)));
    attach (text, "condition", conditionText, condition);
    if (condition.getBoolean ())
      attach (text, "true", textTrue, expTrue);
    else
      attach (text, "false", textFalse, expFalse);

    return text.toString ();
  }
}