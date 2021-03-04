package com.bytezone.diskbrowser.applefile;

import static com.bytezone.diskbrowser.utilities.Utility.ASCII_BACKSPACE;
import static com.bytezone.diskbrowser.utilities.Utility.ASCII_CR;
import static com.bytezone.diskbrowser.utilities.Utility.ASCII_LF;
import static com.bytezone.diskbrowser.utilities.Utility.getIndent;
import static com.bytezone.diskbrowser.utilities.Utility.isHighBitSet;
import static com.bytezone.diskbrowser.utilities.Utility.unsignedShort;

import com.bytezone.diskbrowser.gui.BasicPreferences;

// -----------------------------------------------------------------------------------//
public class AppleBasicFormatter extends BasicFormatter
// -----------------------------------------------------------------------------------//
{
  private static final int LEFT_MARGIN = 5;
  private static final int RIGHT_MARGIN = 33;

  // ---------------------------------------------------------------------------------//
  public AppleBasicFormatter (ApplesoftBasicProgram program,
      BasicPreferences basicPreferences)
  // ---------------------------------------------------------------------------------//
  {
    super (program, basicPreferences);
  }

  // ---------------------------------------------------------------------------------//
  @Override
  public void format (StringBuilder fullText)
  // ---------------------------------------------------------------------------------//
  {
    int loadAddress = getLoadAddress ();
    int ptr = 0;
    int linkField;

    StringBuilder currentLine = new StringBuilder ();

    while ((linkField = unsignedShort (buffer, ptr)) != 0)
    {
      int lineNumber = unsignedShort (buffer, ptr + 2);
      currentLine.append (String.format (" %d ", lineNumber));
      ptr += 4;

      if (basicPreferences.appleLineWrap)
        ptr = appendWithWrap (currentLine, ptr);
      else
        ptr = appendWithOutWrap (currentLine, ptr);

      if (ptr != (linkField - loadAddress))
      {
        System.out.printf ("%s: ptr: %04X, nextLine: %04X%n", program.name,
            ptr + loadAddress, linkField);
        //        ptr = linkField - loadAddress;      // use this when tested
      }

      currentLine.append (NEWLINE);

      fullText.append (currentLine);
      currentLine.setLength (0);
    }
  }

  // ---------------------------------------------------------------------------------//
  private int appendWithOutWrap (StringBuilder currentLine, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    byte b;

    while ((b = buffer[ptr++]) != 0)
      if (isHighBitSet (b))
      {
        String token = String.format (" %s ", ApplesoftConstants.tokens[b & 0x7F]);
        currentLine.append (token);
      }
      else
        switch (b)
        {
          case ASCII_CR:
            currentLine.append (NEWLINE);
            break;

          case ASCII_BACKSPACE:
            if (currentLine.length () > 0)
              currentLine.deleteCharAt (currentLine.length () - 1);
            break;

          case ASCII_LF:
            int indent = getIndent (currentLine);
            currentLine.append ("\n");
            for (int i = 0; i < indent; i++)
              currentLine.append (" ");
            break;

          default:
            currentLine.append ((char) b);
        }

    return ptr;
  }

  // ---------------------------------------------------------------------------------//
  private int appendWithWrap (StringBuilder currentLine, int ptr)
  // ---------------------------------------------------------------------------------//
  {
    byte b;
    int cursor = currentLine.length ();       // controls when to wrap

    while ((b = buffer[ptr++]) != 0)
      if (isHighBitSet (b))
      {
        String token = String.format (" %s ", ApplesoftConstants.tokens[b & 0x7F]);
        currentLine.append (token);
        cursor = incrementCursor (currentLine, cursor, token.length ());
      }
      else
        switch (b)
        {
          case ASCII_CR:
            currentLine.append (NEWLINE);
            cursor = 0;
            break;

          case ASCII_BACKSPACE:
            if (cursor > 0)
            {
              currentLine.deleteCharAt (currentLine.length () - 1);
              --cursor;
            }
            break;

          case ASCII_LF:
            currentLine.append ("\n");
            for (int i = 0; i < cursor; i++)
              currentLine.append (" ");
            break;

          default:
            currentLine.append ((char) b);
            cursor = incrementCursor (currentLine, cursor, 1);
        }

    return ptr;
  }

  // ---------------------------------------------------------------------------------//
  private int incrementCursor (StringBuilder currentLine, int cursor, int size)
  // ---------------------------------------------------------------------------------//
  {
    assert size <= 9;           // longest token possible (7 plus 2 spaces)
    cursor += size;

    if ((cursor) >= RIGHT_MARGIN)
    {
      cursor = cursor >= 40 ? cursor - 40 : LEFT_MARGIN;
      currentLine.append ("\n     ".substring (0, cursor + 1));
    }

    return cursor;
  }
}
