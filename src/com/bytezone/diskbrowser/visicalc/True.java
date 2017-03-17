package com.bytezone.diskbrowser.visicalc;

public class True extends Function
{
  True (Sheet parent, Cell cell, String text)
  {
    super (parent, cell, text);

    value = 1;
  }
}