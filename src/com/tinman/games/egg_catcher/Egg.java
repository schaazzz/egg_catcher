package com.tinman.games.egg_catcher;

/* Imported packages. */
import android.graphics.Bitmap;

import com.tinman.games.egg_catcher.Util.AudioClip;

/**
 * The Egg class implements an abstraction for the eggs used
 * in the game.
 * 
 * @author Shahzeb Ihsan
 * @version 1.0
 * @see {@link GameView}
 */
@SuppressWarnings("unused")
public class Egg {
   /**
    * Public class members.
    */
   public Bitmap wholeEgg;
   public Bitmap crackedEgg;
   public boolean isDisplayed;
}
