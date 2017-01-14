package com.bytezone.diskbrowser.applefile;

import java.util.ArrayList;
import java.util.List;

public class PaletteFactory
{
  private final List<Palette> palettes = new ArrayList<Palette> ();
  private int currentPalette;

  public PaletteFactory ()
  {
    palettes.add (new Palette ("Palette 1",
        new int[] { 0x000000, // 0 black        A
                    0xFF0000, // 1 red          C
                    0xA52A2A, // 2 brown        E  (8)
                    0xFFA500, // 3 orange       G  (9)
                    0x008000, // 4 dark green   I
                    0x808080, // 5 grey1        K
                    0x90EE90, // 6 light green  M  (C)
                    0xFFFF00, // 7 yellow       O  (D)
                    0x00008B, // 8 dark blue    B  (2)
                    0x800080, // 9 purple       D  (3)
                    0xC0C0C0, // A grey2        F
                    0xFFC0CB, // B pink         H
                    0x00BFFF, // C med blue     J  (6)
                    0x87CEFA, // D light blue   L  (7)
                    0x00FFFF, // E aqua         N
                    0xFFFFFF  // F white        P
        }));

    palettes.add (new Palette ("Virtual II",
        new int[] { 0x000000, // 0 black
                    0xDD0033, // 1 magenta
                    0x885500, // 2 brown         (8)
                    0xFF6600, // 3 orange        (9)
                    0x007722, // 4 dark green
                    0x555555, // 5 grey1
                    0x11DD00, // 6 light green   (C)
                    0xFFFF00, // 7 yellow        (D)
                    0x000099, // 8 dark blue     (2)
                    0xDD22DD, // 9 purple        (3)
                    0xAAAAAA, // A grey2
                    0xFF9988, // B pink
                    0x2222FF, // C med blue      (6)
                    0x66AAFF, // D light blue    (7)
                    0x44FF99, // E aqua
                    0xFFFFFF  // F white
        }));

    // Applewin new (BB)
    palettes.add (new Palette ("Applewin (new)",
        new int[] { 0x000000, // 0 black    
                    0x9D0966, // 1 red    
                    0x555500, // 2 brown    
                    0xF25E00, // 3 orange     
                    0x00761A, // 4 dk green 
                    0x808080, // 5 gray     
                    0x38CB00, // 6 lt green 
                    0xD5D51A, // 7 yellow   
                    0x2A2AE5, // 8 dk blue  
                    0xC734FF, // 9 purple   
                    0xC0C0C0, // A grey     
                    0xFF89E5, // B pink     
                    0x0DA1FF, // C med blue 
                    0xAAAAFF, // D lt blue  
                    0x62F699, // E aqua     
                    0xFFFFFF  // F white    
        }));
    // Applewin old (BB)
    palettes.add (new Palette ("Applewin (old)",
        new int[] { rgb (0, 0, 0),        // black    */
                    rgb (208, 0, 48),     // red      */
                    rgb (128, 80, 0),     // brown    */
                    rgb (255, 128, 0),    // orange   */
                    rgb (0, 128, 0),      // dk green */
                    rgb (128, 128, 128),  // gray     */
                    rgb (0, 255, 0),      // lt green */
                    rgb (255, 255, 0),    // yellow   */
                    rgb (0, 0, 128),      // dk blue  */
                    rgb (255, 0, 255),    // purple   */
                    rgb (192, 192, 192),  // grey     */
                    rgb (255, 144, 128),  // pink     */
                    rgb (0, 0, 255),      // med blue */
                    rgb (96, 160, 255),   // lt blue  */
                    rgb (64, 255, 144),   // aqua     */
                    rgb (255, 255, 255)   // white
        }));
    // Kegs (BB)
    palettes.add (new Palette ("Kegs",
        new int[] { rgb (0, 0, 0),        // black    */
                    rgb (221, 0, 51),     // red      */
                    rgb (136, 85, 34),    // brown    */
                    rgb (255, 102, 0),    // orange   */
                    rgb (0, 119, 0),      // dk green */
                    rgb (85, 85, 85),     // gray     */
                    rgb (0, 221, 0),      // lt green */
                    rgb (255, 255, 0),    // yellow   */
                    rgb (0, 0, 153),      // dk blue  */
                    rgb (221, 0, 221),    // purple   */
                    rgb (170, 170, 170),  // grey     */
                    rgb (255, 153, 136),  // pink     */
                    rgb (34, 34, 255),    // med blue */
                    rgb (102, 170, 255),  // lt blue  */
                    rgb (0, 255, 153),    // aqua     */
                    rgb (255, 255, 255)   // white
        }));
    // Authentic (MP)
    palettes.add (new Palette ("Authentic",
        new int[] { 0x000000, // black
                    0xD00030, // magenta
                    0x805000, // brown
                    0xF06000, // orange
                    0x007020, // dark green
                    0x505050, // grey1
                    0x10D000, // light green
                    0xF0F000, // yellow
                    0x000090, // dark blue
                    0xD020D0, // purple
                    0xA0A0A0, // grey2
                    0xF09080, // pink
                    0x2020F0, // med blue
                    0x60A0F0, // light blue
                    0x40F090, // aqua
                    0xFFFFFF  // white
        }));
    // Tweaked (MP)
    palettes.add (new Palette ("Tweaked",
        new int[] { 0x000000, // black
                    0xD00030, // magenta
                    0x805000, // brown
                    0xFF8000, // orange
                    0x008000, // dark green
                    0x808080, // grey1
                    0x00FF00, // light green
                    0xFFFF00, // yellow
                    0x000080, // dark blue
                    0xFF00FF, // purple
                    0xC0C0C0, // grey2
                    0xFF9080, // pink
                    0x0000FF, // med blue
                    0x60A0FF, // light blue
                    0x40FF90, // aqua
                    0xFFFFFF  // white
        }));
    // NTSC Corrected (MP)
    palettes.add (new Palette ("NTSC corrected",
        new int[] { 0x000000, // black
                    0x901740, // magenta
                    0x405400, // brown
                    0xD06A1A, // orange
                    0x006940, // dark green
                    0x808080, // grey1
                    0x2FBC1A, // light green
                    0xBFD35A, // yellow
                    0x402CA5, // dark blue
                    0xD043E5, // purple
                    0x808080, // grey2
                    0xFF96BF, // pink
                    0x2F95E5, // med blue
                    0xBFABFF, // light blue
                    0x6FE8BF, // aqua
                    0xFFFFFF  // white
        }));
  }

  public enum CycleDirection
  {
    FORWARDS, BACKWARDS
  }

  public Palette cyclePalette (CycleDirection direction)
  {
    switch (direction)
    {
      case FORWARDS:
        ++currentPalette;
        if (currentPalette >= palettes.size ())
          currentPalette = 0;
        break;

      case BACKWARDS:
        --currentPalette;
        if (currentPalette < 0)
          currentPalette = palettes.size () - 1;
        break;
    }
    return getCurrentPalette ();
  }

  public List<Palette> getPalettes ()
  {
    return palettes;
  }

  public Palette getCurrentPalette ()
  {
    return palettes.get (currentPalette);
  }

  public int getCurrentPaletteIndex ()
  {
    return currentPalette;
  }

  public void setCurrentPalette (int index)
  {
    assert index >= 0 && index < palettes.size ();
    currentPalette = index;
  }

  public void setCurrentPalette (Palette palette)
  {
    int count = 0;
    for (Palette p : palettes)
    {
      if (p == palette)
      {
        currentPalette = count;
        break;
      }
      ++count;
    }
  }

  public Palette get (int index)
  {
    return palettes.get (index);
  }

  private int rgb (int red, int green, int blue)
  {
    //    System.out.printf ("%3d %3d %3d = 0x%06X%n", red, green, blue,
    //        (red << 16 | green << 8 | blue));
    return red << 16 | green << 8 | blue;
  }
}