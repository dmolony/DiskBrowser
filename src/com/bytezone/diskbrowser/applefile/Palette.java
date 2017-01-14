package com.bytezone.diskbrowser.applefile;

public class Palette
{
  /*-
   *  Michael Pohoreski - The Apple II Forever Anthology
  
  @reference: Technote tn-iigs-063 “Master Color Values”
  Color Register Values
            Color      Reg  LR HR  DHR  Master  Authentic   Tweaked    NTSC     
            Name            #  #   #    Value                          Corrected
  -----------------------------------------------------------------------------
            Black       0   0  0,4 0    $0000   (00,00,00)  (00,00,00) 00,00,00 
  (Magenta) Deep Red    1   1      1    $0D03   (D0,00,30)  (D0,00,30) 90,17,40 
            Dark Blue   2   2      8    $0009   (00,00,90)  (00,00,80) 40,2C,A5 
   (Violet) Purple      3   3  2   9    $0D2D   (D0,20,D0)  (FF,00,FF) D0,43,E5 
            Dark Green  4   4      4    $0072   (00,70,20)  (00,80,00) 00,69,40 
   (Gray 1) Dark Gray   5   5      5    $0555   (50,50,50)  (80,80,80) 80,80,80 
     (Blue) Medium Blue 6   6  6   C    $022F   (20,20,F0)  (00,00,FF) 2F,95,E5 
     (Cyan) Light Blue  7   7      D    $06AF   (60,A0,F0)  (60,A0,FF) BF,AB,FF 
            Brown       8   8      2    $0850   (80,50,00)  (80,50,00) 40,54,00 
            Orange      9   9  5   3    $0F60   (F0,60,00)  (FF,80,00) D0,6A,1A 
   (Gray 2) Light Gray  A   A      A    $0AAA   (A0,A0,A0)  (C0,C0,C0) 80,80,80 
            Pink        B   B      B    $0F98   (F0,90,80)  (FF,90,80) FF,96,BF 
    (Green) Light Green C   C  1   6    $01D0   (10,D0,00)  (00,FF,00) 2F,BC,1A 
            Yellow      D   D      7    $0FF0   (F0,F0,00)  (FF,FF,00) BF,D3,5A 
     (Aqua) Aquamarine  E   E      E    $04F9   (40,F0,90)  (40,FF,90) 6F,E8,BF 
            White       F   F  3,7 F    $0FFF   (F0,F0,F0)  (FF,FF,FF) FF,FF,FF 
  Legend:
   LR: Lo-Res   HR: Hi-Res   DHR: Double Hi-Res 
   */
  private final String name;
  private final int[] colours;

  public Palette (String name, int[] colours)
  {
    this.name = name;
    this.colours = colours;
  }

  public String getName ()
  {
    return name;
  }

  public int[] getColours ()
  {
    return colours;
  }

  @Override
  public String toString ()
  {
    return String.format ("Palette: %s", name);
  }
}