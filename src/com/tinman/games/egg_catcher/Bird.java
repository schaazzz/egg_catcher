package com.tinman.games.egg_catcher;

/* Imported packages. */
import com.tinman.games.egg_catcher.Sprite2D;
import com.tinman.games.egg_catcher.Util.AudioClip;

/**
 * The Bird class implements an abstraction for the eagle
 * and the crow.
 * 
 * @author Shahzeb Ihsan
 * @version 1.0
 * @see {@link GameView}
 */
@SuppressWarnings("unused")
public class Bird {
   /**
    * Private class members.
    */
   private int mDefaultX;
   private int mDefaultY;
   
   /**
    * Public class members.
    */
   public int x;
   public int y;
   public boolean isDisplayed;
   public boolean isAttacking;
   public Sprite2D birdSprite;
   public Util.AudioClip birdCall;
   
   /**
    * Bird constructor.
    * 
    * @param defaultX Default X position of the bird.
    * @param defaultY Default Y position of the bird.
    */
   public Bird(int defaultX, int defaultY) {
      mDefaultX = defaultX;
      mDefaultY = defaultY;
      reset();
   }

   /**
    * Reset the bird's flags and position.
    */
   public void reset() {
      x = mDefaultX;
      y = mDefaultY;
      isDisplayed = false;
      isAttacking = false;
   }
}
