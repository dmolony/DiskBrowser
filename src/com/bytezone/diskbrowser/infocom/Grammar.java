package com.bytezone.diskbrowser.infocom;

import java.util.*;

import com.bytezone.diskbrowser.utilities.HexFormatter;

class Grammar extends InfocomAbstractFile
{
  private static final int SENTENCE_LENGTH = 8;
  private final Header header;
  private final int indexPtr, indexSize;
  private final int tablePtr, tableSize;
  private final int actionPtr, actionSize;
  private final int preActionPtr, preActionSize;
  private final int prepositionPtr, prepositionSize;
  private final int indexEntries;
  private final int totalPrepositions;
  private final int padding;

  private final List<SentenceGroup> sentenceGroups = new ArrayList<SentenceGroup> ();
  private final Map<Integer, List<Sentence>> actionList =
      new TreeMap<Integer, List<Sentence>> ();

  private final List<Integer> actionRoutines = new ArrayList<Integer> ();
  private final List<Integer> preActionRoutines = new ArrayList<Integer> ();

  Grammar (String name, byte[] buffer, Header header)
  {
    super (name, buffer);
    this.header = header;

    indexPtr = header.staticMemory;               // start of the index
    tablePtr = header.getWord (indexPtr);         // start of the data (end of the index)
    indexSize = tablePtr - indexPtr;
    indexEntries = indexSize / 2;

    padding = getPadding ();

    int lastEntry = header.getWord (tablePtr - 2);    // address of the last data entry
    tableSize = lastEntry + getRecordLength (lastEntry) - tablePtr; // uses padding
    actionPtr = tablePtr + tableSize;                 // start of the action routines
    actionSize = getTotalActions () * 2;              // uses padding

    preActionSize = actionSize;
    preActionPtr = actionPtr + actionSize;
    prepositionPtr = preActionPtr + preActionSize;

    totalPrepositions = header.getWord (prepositionPtr);
    prepositionSize = totalPrepositions * 4 + 2;

    if (false)
    {
      System.out.printf ("indexPtr      %,8d  %4X%n", indexPtr, indexPtr);
      System.out.printf ("indexSize     %,8d%n", indexSize);
      System.out.printf ("indexEntries  %,8d%n", indexEntries);
      System.out.printf ("tablePtr      %,8d  %4X%n", tablePtr, tablePtr);
      System.out.printf ("tableSize     %,8d%n", tableSize);
      System.out.printf ("actionPtr     %,8d  %4X%n", actionPtr, actionPtr);
      System.out.printf ("actionSize    %,8d%n", actionSize);
      System.out.printf ("actionEntries %,8d%n", actionSize / 2);
      System.out.printf ("preActionPtr  %,8d  %4X%n", preActionPtr, preActionPtr);
      System.out.printf ("preActionSize %,8d%n", preActionSize);
      System.out.printf ("prepPtr       %,8d  %4X%n", prepositionPtr, prepositionPtr);
      System.out.printf ("prepSize      %,8d%n", prepositionSize);
      System.out.printf ("totPreps      %,8d%n", totalPrepositions);
    }

    // add entries for AbstractFile.getHexDump ()
    hexBlocks.add (new HexBlock (indexPtr, indexSize, "Index:"));
    hexBlocks.add (new HexBlock (tablePtr, tableSize, "Grammar data:"));
    hexBlocks.add (new HexBlock (actionPtr, actionSize, "Action routines:"));
    hexBlocks.add (new HexBlock (preActionPtr, preActionSize, "Pre-action routines:"));
    hexBlocks.add (new HexBlock (prepositionPtr, prepositionSize, "Preposition table:"));

    // create SentenceGroup and Sentence objects and action lists
    int count = 255;
    for (int i = 0; i < indexEntries; i++)
    {
      int offset = header.getWord (indexPtr + i * 2);
      SentenceGroup sg = new SentenceGroup (count--, offset);
      sentenceGroups.add (sg);
      for (Sentence sentence : sg)
      {
        if (!actionList.containsKey (sentence.actionId))    // add to hashmap
          actionList.put (sentence.actionId, new ArrayList<Sentence> ());
        actionList.get (sentence.actionId).add (sentence);

        if (sentence.preActionRoutine > 0         // add to pre-action routine list
            && !preActionRoutines.contains (sentence.preActionRoutine))
          preActionRoutines.add (sentence.preActionRoutine);

        // add to action routine list
        if (sentence.actionRoutine > 0
            && !actionRoutines.contains (sentence.actionRoutine))
          actionRoutines.add (sentence.actionRoutine);
      }
    }
    Collections.sort (actionRoutines);
    Collections.sort (preActionRoutines);
  }

  private int getPadding ()
  {
    // calculate record padding size (Zork has 1 byte padding, Planetfall has 0)
    int r1 = header.getWord (indexPtr);
    int r2 = header.getWord (indexPtr + 2);
    int sentences = header.getByte (r1);
    return r2 - r1 - (sentences * SENTENCE_LENGTH) - 1;
  }

  private int getRecordLength (int recordPtr)
  {
    return (buffer[recordPtr] & 0xFF) * SENTENCE_LENGTH + padding + 1;
  }

  private int getTotalActions ()
  {
    // loop through each record in each index entry, and find the highest action number
    int ptr = tablePtr;
    int highest = 0;
    for (int i = 0; i < indexEntries; i++)
    {
      int totSentences = buffer[ptr++];
      for (int j = 0; j < totSentences; j++)
      {
        int val = buffer[ptr + 7] & 0xFF;
        if (val > highest)
          highest = val;
        ptr += SENTENCE_LENGTH;
      }
      ptr += padding;             // could be zero or one
    }
    return highest + 1;           // zero-based, so increment it
  }

  public List<Integer> getActionRoutines ()
  {
    List<Integer> routines = new ArrayList<Integer> ();
    routines.addAll (actionRoutines);
    routines.addAll (preActionRoutines);
    return routines;
  }

  @Override
  public String getText ()
  {
    String line = "-----------------------------------------------------"
        + "-----------------------------------------------------------\n";
    StringBuilder text = new StringBuilder (
        sentenceGroups.size () + " Grammar tables\n==================\n\n");

    // add the sentences in their original SentenceGroup sequence
    for (SentenceGroup sg : sentenceGroups)
      text.append (sg + "\n" + line);

    text.append ("\n" + actionList.size () + " Action groups\n=================\n\n");

    // add the sentences in their actionId sequence
    for (List<Sentence> list : actionList.values ())
    {
      for (Sentence sentence : list)
        text.append (sentence + "\n");
      text.append (line);
    }

    text.append ("\n" + preActionRoutines.size ()
        + " Pre-action routines\n======================\n\n");

    // add sentences in pre-action routine sequence
    for (Integer routine : preActionRoutines)
    {
      for (Sentence sentence : getSentences (routine))
        text.append (sentence + "\n");
      text.append (line);
    }

    text.append (
        "\n" + actionRoutines.size () + " Action routines\n===================\n\n");

    // add sentences in action routine sequence
    for (Integer routine : actionRoutines)
    {
      for (Sentence sentence : getSentences (routine))
        text.append (sentence + "\n");
      text.append (line);
    }

    text.append ("\n" + totalPrepositions + " Prepositions\n===============\n\n");
    text.append (HexFormatter.getHexString (buffer, prepositionPtr, 2) + "\n");
    for (int i = 0, ptr = prepositionPtr + 2; i < totalPrepositions; i++, ptr += 4)
    {
      text.append (HexFormatter.getHexString (buffer, ptr, 4) + "   ");
      int id = header.getByte (ptr + 3);
      List<String> preps = header.dictionary.getPrepositions (id);
      String prepString = makeWordBlock (preps);
      text.append (prepString + "\n");
    }

    text.deleteCharAt (text.length () - 1);
    return text.toString ();
  }

  private List<Sentence> getSentences (int routine)
  {
    List<Sentence> sentences = new ArrayList<Sentence> ();

    for (SentenceGroup sg : sentenceGroups)
      for (Sentence s : sg.sentences)
        if (s.actionRoutine == routine || s.preActionRoutine == routine)
          sentences.add (s);

    return sentences;
  }

  private String makeWordBlock (List<String> words)
  {
    StringBuilder text = new StringBuilder ("[");
    if (words.size () > 0)
    {
      for (String word : words)
        text.append (word + ", ");
      text.deleteCharAt (text.length () - 1);
      text.deleteCharAt (text.length () - 1);
    }
    else
      text.append ("** not found **");
    text.append ("]");
    return text.toString ();
  }

  private class SentenceGroup implements Iterable<Sentence>
  {
    int startPtr;
    int id;
    List<Sentence> sentences = new ArrayList<Sentence> ();
    String verbString; // list of synonyms inside []

    public SentenceGroup (int id, int ptr)
    {
      this.startPtr = ptr;
      this.id = id;

      int records = buffer[ptr] & 0xFF;
      verbString = makeWordBlock (header.dictionary.getVerbs (id));

      for (int j = 0, offset = startPtr + 1; j < records; j++, offset += SENTENCE_LENGTH)
        sentences.add (new Sentence (offset, this));
    }

    @Override
    public String toString ()
    {
      StringBuilder text = new StringBuilder ();
      for (Sentence sentence : sentences)
        text.append (sentence + "\n");
      text.deleteCharAt (text.length () - 1);
      return text.toString ();
    }

    @Override
    public Iterator<Sentence> iterator ()
    {
      return sentences.iterator ();
    }
  }

  private class Sentence
  {
    int startPtr;
    SentenceGroup parent;
    int actionId;
    int actionRoutine;        // mandatory
    int preActionRoutine;     // optional
    String sentenceText;

    public Sentence (int ptr, SentenceGroup parent)
    {
      this.startPtr = ptr;
      this.parent = parent;

      // byte 0 contains the number of objects in the sentence
      int totObjects = buffer[ptr++] & 0xFF; // 0-2

      // build the sentence text from bytes 1-2
      StringBuilder sentence = new StringBuilder ();
      for (int k = 0; k < totObjects; k++)
      {
        int b = buffer[ptr++] & 0xFF;
        if (b > 0)
          sentence.append (" " + getPrep (b));
        sentence.append (" OBJ");
      }
      sentenceText = sentence.toString ();

      // do something with bytes 3-6
      // ... what that is I have no idea

      // get action pointer from byte 7
      actionId = buffer[startPtr + 7] & 0xFF;
      int targetOffset = actionId * 2; // index into the action and pre-action blocks
      actionRoutine = header.getWord (actionPtr + targetOffset) * 2;
      preActionRoutine = header.getWord (preActionPtr + targetOffset) * 2;
    }

    private String getPrep (int value)
    {
      int offset = prepositionPtr + 2 + (totalPrepositions - (255 - value) - 1) * 4;
      int address = header.getWord (offset);
      return header.dictionary.wordAt (address);
    }

    private String getText ()
    {
      return parent.verbString + " " + sentenceText;
    }

    @Override
    public String toString ()
    {
      StringBuilder text =
          new StringBuilder (String.format ("%3d  %04X  ", parent.id, startPtr));
      text.append (HexFormatter.getHexString (buffer, startPtr, SENTENCE_LENGTH));
      String r1 = preActionRoutine == 0 ? "" : String.format ("R:%05X", preActionRoutine);
      text.append (String.format ("  %-7s  R:%05X  %s", r1, actionRoutine, getText ()));
      return text.toString ();
    }
  }
}