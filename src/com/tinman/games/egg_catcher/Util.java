package com.tinman.games.egg_catcher;

/* Imported packages. */
import android.content.Context;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

/**
 * Utility class.
 * 
 * @author Zihua Qiu
 * @version 1.0
 */
@SuppressWarnings("unused")
public class Util {
   
   /**
    * Utility sub-class for playing audio clips.
    *
    * @author Zihua Qiu
    * version 1.0
    */
   public class AudioClip {
      /**
       * Private class members.
       */
      private String name;
      private MediaPlayer mPlayer;
      private boolean mLoop = false;
      private boolean mPlaying = false;
     
      /**
       * Audio clip class constructor.
       * 
       * @param ctx Context of the calling activity.
       * @param resID Resource ID.
       */
      public AudioClip(Context ctx, int resID) {
         /* Initialize the media player object. */
         name = ctx.getResources().getResourceName(resID);
         mPlayer = MediaPlayer.create(ctx, resID);
         mPlayer.setVolume(1000, 1000);
         
         /* Callback for for playback is complete . */
         mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
               mPlaying = false;
               
               /* If mLoop is true, start the clip again. */
               if ( mLoop) {
                  System.out.println("AudioClip loop " + name);
                  mPlaying = true;
                  mp.start();
               }
            }
         });
      }

      /**
       * Play the audio clip.
       */
      public synchronized void play() {
         /* If a sound clip is already playing, return. */
         if(mPlaying) { 
            return;
         }
           
         /* Play the audio clip. */
         if (mPlayer != null ) {
            mPlaying = true;
            mPlayer.start();
         }
      }
      
      /**
       * Stop the audio clip.
       */
      public synchronized void stop() {
         try {
            mLoop = false;
            if (mPlaying) { 
               mPlaying = false;
               mPlayer.pause();
               mPlayer.release();
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      
      /**
       * Play the audio file and loop the audio playback.
       */
      public synchronized void loop() {
         mLoop = true;
         mPlaying = true;
         mPlayer.start();               
      }
   }
   
   /**
    * Checks if two rectangles are colliding. It is basically
    * just a wrapper for the intersect function in Rect.
    * 
    * @param r1 The first rectangle.
    * @param r2 The second rectangle.
    * @return true if colliding, false otherwise.
    */
   public static boolean isColliding(Rect r1, Rect r2) {
      return r1.intersect(r2);
   }
   
   /**
    * Vibrate for the specified number of milliseconds.
    * 
    * @param context Activity context.
    * @param ms Vibration time in milliseconds.
    */
   public static void vibrate(Context context, long ms) {
      ((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(ms);
   }
}
