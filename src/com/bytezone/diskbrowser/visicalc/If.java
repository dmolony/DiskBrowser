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
  public void calculate ()
  {
    if (condition.getResult ())
    {
      if (expTrue == null)
        expTrue = new Expression (parent, textTrue);
      value = expTrue.getValue ();
    }
    else
    {
      if (expFalse == null)
        expFalse = new Expression (parent, textFalse);
      value = expFalse.getValue ();
    }
  }

  @Override
  public String toString ()
  {
    return String.format ("[IF:%s, True:%s, False:%s]", condition, textTrue, textFalse);
  }
}