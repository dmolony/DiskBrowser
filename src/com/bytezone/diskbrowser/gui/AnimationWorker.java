package com.bytezone.diskbrowser.gui;

import java.util.List;

import javax.swing.SwingWorker;

import com.bytezone.diskbrowser.applefile.SHRPictureFile2;

// -----------------------------------------------------------------------------------//
public class AnimationWorker extends SwingWorker<Void, Integer>
// -----------------------------------------------------------------------------------//
{
  volatile boolean running;
  SHRPictureFile2 image;
  OutputPanel owner;

  // ---------------------------------------------------------------------------------//
  public AnimationWorker (OutputPanel owner, SHRPictureFile2 image)
  // ---------------------------------------------------------------------------------//
  {
    assert image.isAnimation ();
    this.image = image;
    this.owner = owner;
  }

  // ---------------------------------------------------------------------------------//
  public void cancel ()
  // ---------------------------------------------------------------------------------//
  {
    running = false;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected Void doInBackground () throws Exception
  // ---------------------------------------------------------------------------------//
  {
    running = true;
    try
    {
      while (running)
      {
        Thread.sleep (image.getDelay ());
        publish (0);
      }
    }
    catch (InterruptedException e)
    {
      e.printStackTrace ();
    }
    return null;
  }

  // ---------------------------------------------------------------------------------//
  @Override
  protected void process (List<Integer> chunks)
  // ---------------------------------------------------------------------------------//
  {
    image.nextFrame ();
    owner.update ();
  }
}