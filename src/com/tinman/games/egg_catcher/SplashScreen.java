package com.tinman.games.egg_catcher;

/* Imported packages. */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tinman.games.egg_catcher.StartDialog;

/**
 * The SplashScreen activity launches the rest of the game. It is
 * displayed for only a fixed number of seconds and then launches
 * the StartDialog activity.
 * 
 * @author Shahzeb Ihsan
 * @version 1.0
 * @see {@link StartDialog}
 */
public class SplashScreen extends Activity implements Runnable {
   /**
    * Private class members.
    */
   private final int mDelaySec = 5;
   private Intent mDialogIntent;
   private Thread mThisThread;
   
   @Override
   /**
    *  Called when the activity is first created.
    */
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.splashscreen);

      /* Intent used to launch the StartDialog activity. */
      mDialogIntent = new Intent(SplashScreen.this, StartDialog.class);
      
      /* Create and start new thread from this activity. */
      mThisThread = new Thread(this);
      mThisThread.start();
   }
   
   @Override
   /**
    * Called when this thread is started. 
    */
   public void run() {
      /* Wait for the specified number of seconds, then launch the
       * StartDialog activity, then exit this activity as we won't
       * be needing it anymore. */
      try {
         Thread.sleep(mDelaySec * 1000);
      }
      catch(InterruptedException e)
      {
         e.printStackTrace();
      }
      this.startActivity(mDialogIntent);
      this.finish();
   }
}
