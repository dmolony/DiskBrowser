package com.bytezone.diskbrowser.visicalc;

class If extends Function
{
  private final Condition condition;
  private final String textTrue;
  private final String textFalse;

  private Expression expTrue;
  private Expression expFalse;

  public If (Sheet parent, String text)
  {
    super (parent, text);

    int pos1 = functionText.indexOf (',');
    int pos2 = functionText.indexOf (',', pos1 + 1);

    condition = new Condition (parent, functionText.substring (0, pos1));

    textTrue = functionText.substring (pos1 + 1, pos2);
    textFalse = functionText.substring (pos2 + 1);
  }

  @Override
  public Value calculate ()
  {
    valueType = ValueType.VALUE;

    System.out.println (functionText);
    if (condition.getResult ())
    {
      //      System.out.println ("true");
      if (expTrue == null)
        expTrue = new Expression (parent, textTrue);

      expTrue.calculate ();

      if (expTrue.isError () || expTrue.isNotAvailable ())
        valueType = expTrue.getValueType ();
      else
        value = expTrue.getValue ();
    }
    else
    {
      //      System.out.println ("false");
      if (expFalse == null)
        expFalse = new Expression (parent, textFalse);

      expFalse.calculate ();

      if (expFalse.isError () || expFalse.isNotAvailable ())
        valueType = expFalse.getValueType ();
      else
        value = expFalse.getValue ();
    }

    return this;
  }

  @Override
  public String toString ()
  {
    return String.format ("[IF:%s, True:%s, False:%s]", condition, textTrue, textFalse);
  }
}