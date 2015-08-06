package com.tinman.games.egg_catcher;

/* Imported packages. */
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.content.Intent;

import com.tinman.games.egg_catcher.EggCatcher;

/**
 * The StartDialog activity shows the main screen from where a
 * user can view game instructions, start or exit the game.
 * 
 * @author Shahzeb Ihsan
 * @version 1.0
 * @see {@link SplashScreen}, {@link EggCatcher}
 */
public class StartDialog extends Activity {
   /**
    * Private class members.
    */
   private final String instructions = "1.) Move the farmer boy by tilting the phone left or right.\n\n" +
                                       "2.) You can hold a maximum of 5 eggs at any one time, after" +
                                            " which you have to dump them into the basket.\n\n" +
                                       "3.) The score increases only when you dump eggs into basket," +
                                            " you need atleast 3 eggs before you can dump them.\n\n" +
                                       "4.) Dump the eggs by moving to the basket and touching the screen.\n\n" +
                                       "5.) Shoo the crow within 3 seconds otherwise it will steal 5 eggs. Additional" +
                                            " 25 points for frightening the crow away. \n\n" +
                                       "6.) Shoo the eagle within 3 seconds otherwise it will eat a hen. Additional" + 
                                            " 50 points for frightening the eagle away. \n\n" +
                                       "7.) The game ends if you break more than 5 eggs or the eagle eats all your hens.\n\n" +
                                       "8.) Collect the yellow egg for extra 100 points and the blue egg for extra chances.";
   
   
   private Activity mThisActivity;
   private ImageButton mStartBtn, mInstrBtn, mExitBtn;
   
   /** 
    * Called when the "Start Game" button is clicked. 
    */
   private OnClickListener startBtnListener = new OnClickListener() {
      /* Start the game by launching the EggCatcher activity. */
      public void onClick(View v) {
         Intent gameIntent = new Intent(StartDialog.this, EggCatcher.class);
         mThisActivity.startActivity(gameIntent);
      }
   };
   
   /**
    * Called when the "Instructions" button is clicked.
    */
   private OnClickListener instrBtnListener = new OnClickListener() {
      /* Show instructions in an AlertDialog. */
      public void onClick(View v) {
         new AlertDialog.Builder(mThisActivity).setMessage(instructions).setPositiveButton("Ok", null).show();
      }
   };
   
   /** 
    * Called when the "Exit Game" button is clicked.
    */
   private OnClickListener exitBtnListener = new OnClickListener() {
      /* Exit the game. */
      public void onClick(View v) {
         mThisActivity.finish();
      }
   };
   
   @Override
   /** 
    * Called when the activity is first created.
    */
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.startdialog);
      
      /* Set the main activity variable. */
      mThisActivity = this;
      
      /* Set event listeners for all the buttons. */
      mStartBtn = (ImageButton)findViewById(R.id.button_start);
      mStartBtn.setOnClickListener(startBtnListener);
      
      mInstrBtn = (ImageButton)findViewById(R.id.button_instructions);
      mInstrBtn.setOnClickListener(instrBtnListener);
      
      mExitBtn = (ImageButton)findViewById(R.id.button_exit);
      mExitBtn.setOnClickListener(exitBtnListener);
   }
}
