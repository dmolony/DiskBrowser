package com.bytezone.diskbrowser.wizardry;

// -----------------------------------------------------------------------------------//
public class WizLong
// -----------------------------------------------------------------------------------//
{
  private static final int MAX = 10000;

  int high;       // 4 digits per component
  int mid;
  int low;

  // ---------------------------------------------------------------------------------//
  public WizLong (int value)
  // ---------------------------------------------------------------------------------//
  {
    assert value >= 0 && value < MAX;

    low = value;
  }

  // ---------------------------------------------------------------------------------//
  public void addLong (WizLong other)
  // ---------------------------------------------------------------------------------//
  {
    low += other.low;
    if (low >= MAX)
    {
      mid++;
      low -= MAX;
    }

    mid += other.mid;
    if (mid >= MAX)
    {
      high++;
      mid -= MAX;
    }

    high += other.high;
    if (high >= MAX)
    {
      high = MAX - 1;
      mid = MAX - 1;
      low = MAX - 1;
    }
  }

  // ---------------------------------------------------------------------------------//
  public void subLong (WizLong other)
  // ---------------------------------------------------------------------------------//
  {
    low -= other.low;
    if (low < 0)
    {
      mid--;
      low += MAX;
    }

    mid -= other.mid;
    if (mid < 0)
    {
      high--;
      mid += MAX;
    }

    high -= other.high;
    if (high < 0)
    {
      high = 0;
      mid = 0;
      low = 0;
    }
  }

  // ---------------------------------------------------------------------------------//
  public void multLong (int multiplier)
  // ---------------------------------------------------------------------------------//
  {
    BCD bcd = long2bcd ();

    for (int digit = 12; digit >= 1; digit--)
    {
      bcd.value[digit] *= multiplier;
    }

    for (int digit = 12; digit >= 1; digit--)
    {
      if (bcd.value[digit] > 9)
      {
        bcd.value[digit - 1] += bcd.value[digit] / 10;
        bcd.value[digit] %= 10;
      }
    }
    bcd2long (bcd);
  }

  // ---------------------------------------------------------------------------------//
  public void divLong (WizLong other)
  // ---------------------------------------------------------------------------------//
  {

  }

  // ---------------------------------------------------------------------------------//
  public BCD long2bcd ()
  // ---------------------------------------------------------------------------------//
  {
    BCD bcd = new BCD ();

    bcd.value[0] = 0;

    int2bcd (high, 1, bcd);
    int2bcd (mid, 5, bcd);
    int2bcd (low, 9, bcd);

    return bcd;
  }

  private static void int2bcd (int part, int digit, BCD bcd)
  {
    bcd.value[digit++] = part / 1000;
    part %= 1000;
    bcd.value[digit++] = part / 100;
    part %= 100;
    bcd.value[digit++] = part / 10;
    part %= 10;
    bcd.value[digit++] = part;
  }

  // ---------------------------------------------------------------------------------//
  public void bcd2long (BCD other)
  // ---------------------------------------------------------------------------------//
  {
    high = mid = low = 0;

    high = bcd2int (1, other);
    mid = bcd2int (5, other);
    low = bcd2int (9, other);
  }

  private static int bcd2int (int digit, BCD bcd)
  {
    int val = bcd.value[digit++] * 1000;
    val += bcd.value[digit++] * 100;
    val += bcd.value[digit++] * 10;
    val += bcd.value[digit];

    return val;
  }

  // ---------------------------------------------------------------------------------//
  public int testLong (WizLong other)
  // ---------------------------------------------------------------------------------//
  {
    if (high == other.high)
      if (mid == other.mid)
        if (low == other.low)
          return 0;
        else
          return low > other.low ? 1 : -1;
      else
        return mid > other.mid ? 1 : -1;
    else
      return high > other.high ? 1 : -1;
  }

  // ---------------------------------------------------------------------------------//
  public int value ()
  // ---------------------------------------------------------------------------------//
  {
    return high * 10000 * 10000 + mid * 10000 + low;
  }

  // ---------------------------------------------------------------------------------//
  public void printLong ()
  // ---------------------------------------------------------------------------------//
  {
    BCD bcd = long2bcd ();
    int digit = 1;

    while (digit < 12 && bcd.value[digit] == 0)
    {
      System.out.print (' ');
      digit++;
    }

    while (digit <= 12)
      System.out.print (bcd.value[digit++]);

    System.out.println ();
  }

  // ---------------------------------------------------------------------------------//
  class BCD
  // ---------------------------------------------------------------------------------//
  {
    int[] value = new int[14];
  }

  // ---------------------------------------------------------------------------------//
  private static void multAddKX (WizLong killExp, int multiply, int amount)
  // ---------------------------------------------------------------------------------//
  {
    if (multiply == 0)
      return;

    WizLong killExpx = new WizLong (amount);

    while (multiply > 1)
    {
      multiply--;
      killExpx.addLong (killExpx);        // double the value
    }

    killExp.addLong (killExpx);           // add to running total
  }

  // ---------------------------------------------------------------------------------//
  public static void main (String[] args)
  // ---------------------------------------------------------------------------------//
  {
    // Earth Giant
    int hpSides = 1;
    int hpLevel = 1;
    int ac = 9;
    int recsn = 2;
    int drain = 0;
    int mageSpells = 0;
    int priestSpells = 0;
    int heal = 0;
    int breathe = 0;
    int unaffect = 85;
    int wepsty = 64;
    int sppc = 0;

    // Werdna
    //    int hpSides = 10;
    //    int hpLevel = 10;
    //    int ac = -7;
    //    int recsn = 2;
    //    int drain = 4;
    //    int mageSpells = 7;
    //    int priestSpells = 7;
    //    int heal = 5;
    //    int breathe = 0;
    //    int unaffect = 70;
    //    int wepsty = 14;         
    //    int sppc = 15;

    // Will O Wisp
    //    int hpSides = 10;
    //    int hpLevel = 8;
    //    int ac = -8;
    //    int recsn = 1;
    //    int drain = 0;
    //    int mageSpells = 0;
    //    int priestSpells = 0;
    //    int heal = 0;
    //    int breathe = 0;
    //    int unaffect = 95;
    //    int wepsty = 0;
    //    int sppc = 0;

    WizLong killExp = new WizLong (0);                // running total

    WizLong killExpx = new WizLong (hpLevel * hpSides);
    killExpx.multLong (breathe == 0 ? 20 : 40);
    killExp.addLong (killExpx);

    killExp.printLong ();

    multAddKX (killExp, mageSpells, 35);
    multAddKX (killExp, priestSpells, 35);
    multAddKX (killExp, drain, 200);
    multAddKX (killExp, heal, 90);

    killExp.printLong ();

    killExpx = new WizLong (40 * (11 - ac));
    killExp.addLong (killExpx);
    killExp.printLong ();

    if (recsn > 1)
      multAddKX (killExp, recsn, 30);

    killExp.printLong ();

    if (unaffect > 0)
      multAddKX (killExp, (unaffect / 10 + 1), 40);

    killExp.printLong ();

    multAddKX (killExp, Integer.bitCount (wepsty & 0x7E), 35);      // 6 bits
    multAddKX (killExp, Integer.bitCount (sppc & 0x7F), 40);        // 7 bits

    killExp.printLong ();

    System.out.println ();

    WizLong a = new WizLong (1000);
    a.multLong (54);
    a.printLong ();
    a.addLong (a);
    a.printLong ();

    System.out.println ();

    a = new WizLong (1000);
    a.multLong (500);
    a.printLong ();
    a.addLong (a);
    a.printLong ();

    System.out.println ();

    a = new WizLong (1000);
    a.multLong (505);
    a.printLong ();
    a.addLong (a);
    a.printLong ();
  }
}
