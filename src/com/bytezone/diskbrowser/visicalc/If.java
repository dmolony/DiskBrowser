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
      else
        value = expTrue.getDouble ();
    }
    else                                // false
    {
      expFalse.calculate ();

      if (!expFalse.isValid ())
        valueResult = expTrue.getValueResult ();
      else
        value = expFalse.getDouble ();
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
    text.append (
        String.format (FMT4, getType (), fullText, valueType, getValueText (this)));
    text.append (String.format (FMT4, "condition", conditionText,
        condition.getValueType (), getValueText (condition)));
    if (condition.getBoolean ())
      attach (text, "true", expTrue, textTrue);
    else
      attach (text, "false", expFalse, textFalse);
    return text.toString ();
  }

  private void attach (StringBuilder text, String title, Value exp, String s)
  {
    text.append (String.format (FMT4, title, s, exp.getValueType (), getValueText (exp)));
    if (exp.size () > 1)
      for (Value value : exp)
        text.append (value);
  }
}