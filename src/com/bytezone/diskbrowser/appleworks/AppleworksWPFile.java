package com.bytezone.diskbrowser.appleworks;

import com.bytezone.diskbrowser.applefile.AbstractFile;

// -----------------------------------------------------------------------------------//
public class AppleworksWPFile extends AbstractFile
// -----------------------------------------------------------------------------------//
{
  Header header;

  // ---------------------------------------------------------------------------------//
  public AppleworksWPFile (String name, byte[] buffer)
  // ---------------------------------------------------------------------------------//
  {
    super (name, buffer);

    header = new Header ();
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public String getText ()
  // ---------------------------------------------------------------------------------//
  {
    int leftMargin = header.leftMargin;
    int rightMargin;
    int topMargin;
    int bottomMargin;
    int paperLength;
    int indent;

    int ptr = 300; // skip the header
    StringBuilder text = new StringBuilder (header.toString ());
    text.append ("\n");

    while (true)
    {
      int b1 = buffer[ptr] & 0xFF;
      int b2 = buffer[ptr + 1] & 0xFF;

      if (b1 == 0xFF && b2 == 0xFF)
        break;

      switch (b2)
      {
        case 0:
          int len = b1;
          int b3 = buffer[ptr + 2] & 0xFF;
          int b4 = buffer[ptr + 3] & 0xFF;

          int lineMargin = b3 & 0x7F;
          boolean containsTabs = (b3 & 0x80) != 0;
          int textLen = b4 & 0x7F;
          boolean cr = (b4 & 0x80) != 0;

          if (false)
            System.out.printf ("%02X %02X %d  %d  %s %s%n", b3, b4, lineMargin, textLen,
                containsTabs, cr);
          if (b3 == 0xFF)
            text.append ("--------- Ruler ----------\n");
          else
          {
            // left margin
            for (int i = 0; i < leftMargin; i++)
              text.append (" ");
            for (int i = 0; i < lineMargin; i++)
              text.append (" ");

            // check for tabs (I'm guessing about how this works)
            if (false)
            {
              while (buffer[ptr + 4] == 0x16)         // tab character
              {
                ptr++;
                len--;
                while (buffer[ptr + 4] == 0x17)       // tab fill character
                {
                  text.append (" ");
                  ptr++;
                  len--;
                }
              }
              text.append (new String (buffer, ptr + 4, len - 2));
              ptr += len;
            }
            else
            {
              StringBuilder line = new StringBuilder ();
              int p = ptr + 4;
              ptr += len;
              len -= 2;

              while (--len >= 0)
              {
                char c = (char) buffer[p++];
                if (c >= 0x20)
                  line.append (c);
                else if (c == 0x17)
                  line.append (' ');
              }

              text.append (line.toString ());
            }
          }

          text.append ("\n");

          if (cr)
            text.append ("\n");

          break;

        case 0xD0:
          text.append ("\n");
          break;

        case 0xD9:
          leftMargin = b1;
          break;

        case 0xDA:
          rightMargin = b1;
          break;

        case 0xDE:
          indent = b1;
          break;

        case 0xE2:
          paperLength = b1;
          break;

        case 0xE3:
          topMargin = b1;
          break;

        case 0xE4:
          bottomMargin = b1;
          break;

        default:
          System.out.printf ("Unknown value in %s: %02X %02X%n", getName (), b1, b2);
      }
      ptr += 2;
    }
    if (false)
      System.out.printf ("", leftMargin, rightMargin, topMargin, bottomMargin,
          paperLength, indent);
    return text.toString ();
  }

  // ---------------------------------------------------------------------------------//
  private class Header
  // ---------------------------------------------------------------------------------//
  {
    private final char[] tabStops = new char[80];
    private final String tabs;
    private final boolean zoom;
    private final boolean paginated;
    private final int leftMargin;
    private final boolean mailMerge;
    private final int sfMinVers;

    private final boolean multipleRulers;

    public Header ()
    {
      // see Asimov disks/images 2/pd_collections/apple_linc/
      //      1988-02 side A (no boot).dsk
      assert buffer[4] == 0x4F;

      int ptr = 5;
      for (int i = 0; i < 80; i++)
        tabStops[i] = (char) buffer[ptr++];

      tabs = new String (tabStops);
      zoom = buffer[85] != 0;
      paginated = buffer[90] != 0;
      leftMargin = buffer[91] & 0xFF;
      mailMerge = buffer[92] != 0;

      multipleRulers = buffer[176] != 0;
      sfMinVers = buffer[183] & 0xFF;
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();

      text.append (String.format ("Tabs ......... %s %n", tabs));
      text.append (String.format ("Zoom ......... %s %n", zoom));
      text.append (String.format ("Mail merge ... %s %n", mailMerge));
      text.append (String.format ("Left margin .. %d %n", leftMargin));
      text.append (String.format ("Min version .. %d %n", sfMinVers));
      text.append (String.format ("Mult rulers .. %s %n", multipleRulers));
      text.append (String.format ("Paginated .... %s %n", paginated));

      return text.toString ();
    }
  }
}