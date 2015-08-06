package com.tinman.games.egg_catcher;

/* Imported packages. */
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewDebug.IntToString;
import android.graphics.Canvas;
import android.widget.TextView;

import java.util.Random;
import java.io.IOException;
import java.util.StringTokenizer;

import org.xmlpull.v1.XmlPullParserException;

import com.tinman.games.egg_catcher.Egg;
import com.tinman.games.egg_catcher.Util;
import com.tinman.games.egg_catcher.Bird;
import com.tinman.games.egg_catcher.Sprite2D;
import com.tinman.games.egg_catcher.Util.AudioClip;

/**
 * The GameView class implements a custom view for the game for drawing the
 * background and the game "pieces" and animating them. It also creates a
 * subclass, GameThread, which uses a thread to implement the
 * game loop.
 * 
 * @author Shahzeb Ihsan
 * @version 1.0
 * @see {@link EggCatcher}
 */
@SuppressWarnings("unused")
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
   /**
    * Private class members.
    */
   /* Constants. */
   private static final int GAMEOVER_DELAY = 3;
   private static final int NOTIFY_SCORE = -1;
   private static final int NOTIFY_NUM_EGGS = -2;
   private static final int NOTIFY_NUM_CHANCES = -3;
   private static final int NOTIFY_TIMELEFT = -4;
   private static final int NOTIFY_TIMECLEAR = -5;
   private static final int NOTIFY_GAMEOVER = -6;

   /* Game play variables. */
   private Bird mCrow;
   private Bird mEagle;
   private Rect mEggRect;
   private Egg mBlueEgg;
   private Egg mWhiteEgg;
   private Egg mYellowEgg;
   private Egg mCurrentEgg = null;
   private int mEggX;
   private int mEggY;
   private int mRefScore = 0;
   private int mAvatarPos = 0;
   private long mRefTime;
   private long mGameTime;
   private boolean mRunState = false;
   private boolean mExitThread = false;
   private boolean mTouch = false;
   private boolean mNewEgg = false;
   private boolean mBonusEgg = false;
   private boolean mFlipAvatar = false;
   private boolean mNextEggBlue = false;
   private int mMovement = EVENT_STAND_STILL;
   
   /* Bitmaps and sprites. */
   private Bitmap mBgImg3;
   private Bitmap mBgImg2;
   private Bitmap mBgImg1;
   private Bitmap mBgImg0;
   private Sprite2D mAvatar;

   /* Display objects. */
   private Display mDisplay;
   private Context mContext;
   private int mCanvasWidth = 1;
   private int mCanvasHeight = 1;
   private SurfaceHolder mSurfaceHolder;

   /* Scoring variables. */
   private int mScore = 0;
   private int mNumEggs = 0;
   private int mNumHens = 3;
   private int mNumChances = EggCatcher.NUM_CHANCES;
   private int mTimeLeft = EggCatcher.MAX_SHOO_SEC;

   /* Audio clips. */
   private Util.AudioClip mHenCluck;
   private Util.AudioClip mEggDump;
   private Util.AudioClip mBgMusic;
   private Util.AudioClip mEggSmash;

   /* Text labels for score etc. */
   private TextView mScoreText;
   private TextView mEggsText;
   private TextView mChancesText;
   private TextView mTimeLeftText;
   private TextView mGameOverText;
   private TextView mGamePausedText;

   /* General purpose variables. */
   private Random mRNG;
   private GameView mThisView;
   private GameThread mGameThread;
 
   /**
    * Public class members.
    */
   /* External events. */
   public static final int EVENT_STAND_STILL = -1;
   public static final int EVENT_MOVE_RIGHT = -2;
   public static final int EVENT_MOVE_LEFT = -3;
   public static final int EVENT_TOUCH_INPUT = -4;
   
   /**
    * The GameThread class implements the game loop.
    * 
    * @author Shahzeb Ihsan
    * @version 1.0
    * @see {@link EggCatcher}, {@link GameView}
    */
   class GameThread extends Thread {
      /**
       * Private class members.
       */
      private Handler mUIMsgHandler;
      private final int EGG_BRK_OFFSET = 40;
      private final int EGG_SKIP_VALUE = 12;
      private final int SPRITE_SKIP_VALUE = 10;
      private final int VIBRATE_CAUGHT = 50;
      private final int VIBRATE_DROPPED = 350;
      private final int CROW = -1;
      private final int EAGLE = -2;
      private final int mHensXPos[] = {100, 400, 715};
      private final int mHensYPos[] = {135, 160, 140};
      private final int mBirdType[] = {CROW, CROW, EAGLE, CROW, CROW, EAGLE, CROW, CROW, EAGLE};
      //private final boolean mBirdProbability[] = {false, true, false, true, false, true, false, true, false, true};
      private final boolean mBirdProbability[] = {true, true, true, true, true, true, true, true, true, true};

      /**
       * Message handler for this thread.
       */
      public Handler mGameMsgHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
            if(EVENT_TOUCH_INPUT == msg.what) {
               setTouchFlag(true);
            } else {
               setMovement(msg.what);
            }
         }
      };
      
      /**
       * Return the movement flag.
       * 
       * @return Returns mMovement.
       */
      private synchronized int getMovement() {
         return mMovement;
      }
      
      /**
       * Update the movement flag.
       * 
       * @param value Movement (STAND_STILL, MOVE_LEFT or MOVE_RIGHT).
       */
      private synchronized void setMovement(int value) {
         mMovement = value;
      }
      
      /**
       * Return the screen touch flag.
       * 
       * @return Returns mTouch.
       */
      private synchronized boolean getTouchFlag() {
         return mTouch;
      }

      /**
       * Update the screen touch flag.
       * 
       * @param b True to set the flag, False to clear it.
       */
      private synchronized void setTouchFlag(boolean b) {
         mTouch = b;
      }

      
      /**
       * Check if the specified number of seconds has elapsed. 
       * 
       * @return True or False.
       */
      private boolean hasElapsed(int seconds) {
         boolean retVal = false;
         if(((mGameTime - mRefTime) / 1000) == seconds){
            mRefTime = mGameTime;
            retVal = true;
         }
            
         return retVal;
      }
      
      /**
       * Notify GameView that the game is over.
       */
      private void notifyGameOver() {
         Message msg = new Message();
         msg.what = NOTIFY_GAMEOVER;
         mUIMsgHandler.sendMessage(msg);
      }

      /**
       * Notify GameView that to display the time left to
       * take care of the crow or the eagle.
       */
      private void notifyTimeLeft() {
         Message msg = new Message();
         msg.what = NOTIFY_TIMELEFT;
         msg.obj = "Time: " + mTimeLeft;
         mUIMsgHandler.sendMessage(msg);
      }
      
      /**
       * Notify GameView that to display the time left to
       * take care of the crow or the eagle should be
       * cleared.
       */
      private void notifyTimeClear() {
         Message msg = new Message();
         msg.what = NOTIFY_TIMECLEAR;
         mUIMsgHandler.sendMessage(msg);
      }
      
      /**
       * Update the displayed game score.
       */
      private void updateScore() {
         Message msg = new Message();
         msg.what = NOTIFY_SCORE;
         msg.obj = "Score: " + mScore;
         mUIMsgHandler.sendMessage(msg);
      }
      
      /**
       * Update the displayed number of eggs.
       */
      private void updateNumEggs() {
         Message msg = new Message();
         msg.what = NOTIFY_NUM_EGGS;
         msg.obj = "Eggs: " + mNumEggs;
         mUIMsgHandler.sendMessage(msg);
      }
      
      /**
       * Update the displayed number of chances left.
       */
      private void updateNumChancesLeft() {
         Message msg = new Message();
         msg.what = NOTIFY_NUM_CHANCES;
         msg.obj = "Chances: " + mNumChances;
         mUIMsgHandler.sendMessage(msg);
      }
      
      /**
       * Egg animation.
       * 
       * @param canvas GameView canvas
       */
      private void animateEgg(Canvas canvas, boolean update) {
         int random;
         boolean reset = false;
         
         /* Select from the default or bonus eggs. */
         if(mNewEgg)
         {
            /* Set the default egg type. */
            if(!mBonusEgg) {
               mCurrentEgg = mWhiteEgg;
            }
   
            /* Set the bonus eggs. */
            if(!mBonusEgg && mNextEggBlue && ((mScore - mRefScore) >= EggCatcher.BLUE_EGG_THRESHOLD)) {
               mBonusEgg = true;
               mNextEggBlue = false;
               mCurrentEgg = mBlueEgg;
               mBlueEgg.isDisplayed = true;
               mRefScore = mScore;
            } else if(!mBonusEgg && !mNextEggBlue && ((mScore - mRefScore) >= EggCatcher.YELLOW_EGG_THRESHOLD)) {
               mBonusEgg = true;
               mNextEggBlue = true;
               mCurrentEgg = mYellowEgg;
               mYellowEgg.isDisplayed = true;
            }
         }
         
         if(mEggY >= (mDisplay.getHeight() - EGG_BRK_OFFSET)) {
            /* If the egg has hit the ground, draw the cracked egg. */ 
            canvas.drawBitmap(mCurrentEgg.crackedEgg, mEggX, mEggY, null);
            
            /* Long vibrate if an egg is dropped. */
            Util.vibrate(mContext, VIBRATE_DROPPED);
            
            /* Play the egg smashing audio clip. */
            mEggSmash.play();
            
            /* Bonus eggs don't have any penalty. */
            if(!mBonusEgg)
            {
               /* Update the number of game chances. */
               mNumChances--;
               updateNumChancesLeft();
               
               /* If the number of chances has expired, send game over notification. */
               if(mNumChances == 0) {
                  notifyGameOver();
               }
            } else {
               mBonusEgg = false;
            }
            
            /* A short delay. */
            try {
               Thread.sleep(150);
            } catch(InterruptedException e) {
               e.printStackTrace();
            }
            
            /* Reset the egg. */
            reset = true;
         } else {
            /* If the egg hasn't hit the ground yet, draw the whole egg. */
            canvas.drawBitmap(mCurrentEgg.wholeEgg, mEggX, mEggY, null);
            
            /* Update the egg position and the rectangle. */
            if(update) {
               /* Play the egg falling audio clip. */
               if(mNewEgg) {
                  mNewEgg = false;
                  mHenCluck.play();
               }
               
               mEggY += EGG_SKIP_VALUE;
               mEggRect.set(mEggX, mEggY, (mEggX + mCurrentEgg.wholeEgg.getWidth()), (mEggY + mCurrentEgg.wholeEgg.getHeight())); 
            }
            
            /* If the avatar has collided with the egg, update the number of eggs. */
            if(Util.isColliding(mEggRect, mAvatar.getCollisionRect())) {
               /* If the maximum number of eggs haven't been caught, update number of eggs. */
               if(mNumEggs < EggCatcher.MAX_NUM_EGGS) {
                  mNumEggs++; 
                  updateNumEggs();
                  
                  /* If this was a bonus egg, increment the score and clear the flag. */
                  if(mBonusEgg) {
                     if(mBlueEgg.isDisplayed) {
                        /* If its a blue egg, increment the number of chances left. */
                        mBlueEgg.isDisplayed = false;
                        mNumChances++;
                        updateNumChancesLeft();
                     } else if(mYellowEgg.isDisplayed) {
                        /* If its a yellow egg, add bonus score. */
                        mYellowEgg.isDisplayed = false;
                        mScore += EggCatcher.YELLOW_EGG_BONUS;
                        updateScore();
                     }
                     
                     mBonusEgg = false;
                  }

                  /* Short vibrate if an egg is caught. */
                  Util.vibrate(mContext, VIBRATE_CAUGHT);
                  
                  /* Reset the egg. */
                  reset = true;
               }
            }
         }
         
         /* Randomly select the next hen and reset the egg position. */
         if(reset) {
            reset = false;
            mNewEgg = true;
            random = mRNG.nextInt(9999) % mNumHens;
            if(mNumHens == 1) {
               random = 1;
            }
            mEggX = mHensXPos[random];
            mEggY = mHensYPos[random];
            mEggRect.set(mEggX, mEggY, (mEggX + mCurrentEgg.wholeEgg.getWidth()), (mEggY + mCurrentEgg.wholeEgg.getHeight()));
         }
      }

      /**
       * The game loop responsible for the game play and all the foreground/background
       * drawing for the game.
       * 
       * @param canvas GameView canvas
       */
      private void gameLoop(Canvas canvas) {
         int movement, random;
         boolean showEggs = true;
         Bitmap background = null;
         
         /* Select the background based on the number of hens. */
         if(mNumHens == 3) {
            background = mBgImg3;
         } else if(mNumHens == 2) {
            background = mBgImg2;
         } else if(mNumHens == 1) {
            background = mBgImg1;
         } else if(mNumHens == 0) {
            notifyGameOver();
            background = mBgImg0;
            showEggs = false;
         }
         
         /* If neither bird is being displayed, start the count down to the next bird. */
         if(!mCrow.isDisplayed && !mEagle.isDisplayed) {
            /* Check if BIRD_INTERVAL seconds have elapsed. */
            if(hasElapsed(EggCatcher.BIRD_INTERVAL)) {
               /* Randomly select if the bird should be displayed or not. */
               random = mRNG.nextInt(9999) % mBirdProbability.length;
               
               if(mBirdProbability[random]) {
                  /* Randomly select the bird type. */
                  random = mRNG.nextInt(9999) % mBirdType.length;
                  
                  if(EAGLE == mBirdType[random]) {
                     mEagle.isDisplayed = true;
                     mEagle.birdCall.play();
                  } else if(CROW == mBirdType[random]) {
                     mCrow.isDisplayed = true;
                     mCrow.birdCall.play();
                  }
                  
                  /* Start displaying the time left. */
                  notifyTimeLeft();
               }
            }
         }
         
         /* Get the current movement direction. */
         movement = getMovement();

         /* Draw the background. */
         canvas.drawBitmap(background, 0, 0, null);
         
         /* Animate the egg. */
         if(mAvatar.updateTime(mGameTime) && showEggs) {
            animateEgg(canvas, true);
         } else if(showEggs) {
            animateEgg(canvas, false);
         }
         
         /* Update bird display time. */
         if(mCrow.isDisplayed) {
            mCrow.birdSprite.updateTime(mGameTime);
         } else if(mEagle.isDisplayed) {
            mEagle.birdSprite.updateTime(mGameTime);
         }
         
         /* Update the avatar position. */
         if ((EVENT_MOVE_RIGHT == movement)
              && (mAvatarPos < (mDisplay.getWidth() - mAvatar.getSpriteWidth()))) {
            /* Update the avatar's position. */
            mAvatarPos += SPRITE_SKIP_VALUE;
            mAvatar.setXPos(mAvatarPos);
            mFlipAvatar = false;
         } else if ((EVENT_MOVE_LEFT == movement) && (mAvatarPos >= 0)) {
            /* Update the avatar's position. */
            mAvatarPos -= SPRITE_SKIP_VALUE;
            mAvatar.setXPos(mAvatarPos);
            mFlipAvatar = true;
         }
         
         /* Common bird behavior. */
         if(mCrow.isDisplayed || mEagle.isDisplayed) {
            Sprite2D bird = null;
            
            /* Select the bird. */
            if(mCrow.isDisplayed) {
               bird = mCrow.birdSprite;
            } else if(mEagle.isDisplayed) {
               bird = mEagle.birdSprite;
            }
            
            /* If 1 second has elapsed, decrement the shoo time left. */
            if((mTimeLeft != 0) && hasElapsed(1)) {
               mTimeLeft--;
               notifyTimeLeft();
            }
            
            /* If the minimum shoo time hasn't elapsed and the avatar has
             * collided with the bird, add bonus score and don't draw the
             * bird anymore. */
            if((mTimeLeft != 0) &&
               Util.isColliding(mAvatar.getCollisionRect(),
                                bird.getCollisionRect())) {
               notifyTimeClear();
               
               /* Add bonus score. */
               if(mCrow.isDisplayed) {
                  mScore += EggCatcher.CROW_SHOO_BONUS;
               } else if(mEagle.isDisplayed) {
                  mScore += EggCatcher.EAGLE_SHOO_BONUS;
               }
               updateScore();
               
               /* Reset flags. */
               mCrow.isDisplayed = false;
               mEagle.isDisplayed = false;
            }
         }
         
         /* Handle the crow animation and behavior. */
         if(mCrow.isDisplayed) {
            /* If the minimum time for the crow to steal eggs has 
             * passed, update the state . */
            if(mTimeLeft == 0) {
               mCrow.isAttacking = true;
               mCrow.birdCall.play();
               notifyTimeClear();
            }
            
            /* If the minimum shoo time has elapsed, move the crow to the right and
             * when it reaches the right end of the screen, decrement the score. */
            if(mCrow.isAttacking && (mCrow.x > 0)) {
               mCrow.x -= SPRITE_SKIP_VALUE;
               mCrow.birdSprite.setXPos(mCrow.x);
            } else if(mCrow.x <= 0) {
               /* Reset the crow's position and flags. */
               mCrow.reset();
               mCrow.birdSprite.setXPos(mCrow.x);
               mTimeLeft = EggCatcher.MAX_SHOO_SEC;
               
               /* Decrement the score. */
               mScore -= EggCatcher.NUM_EGGS_STOLEN * EggCatcher.SCORE_PER_EGG;
               updateScore();
            }
         }
         
         /* Handle the eagle animation and behavior. */
         if(mEagle.isDisplayed) {
            /* If the minimum time for the eagle to attack hens has passed, update the state . */
            if(mTimeLeft == 0) {
               mEagle.isAttacking = true;
               mEagle.birdCall.play();
               notifyTimeClear();
            }
            
            /* If the minimum shoo time has elapsed, move the eagle to the top and when
             * it reaches the clothes line, update the background image. */
            if(mEagle.isAttacking && (mEagle.x > 0) && (mEagle.y > 0)) {
               mEagle.x -= 5;
               mEagle.y -= 3;
               mEagle.birdSprite.setXPos(mEagle.x);
               mEagle.birdSprite.setYPos(mEagle.y);
            } else if(mEagle.isAttacking) {
               /* Reset the eagle's position and flags. */
               mEagle.reset();
               mEagle.birdSprite.setXPos(mEagle.x);
               mEagle.birdSprite.setYPos(mEagle.y);
               mTimeLeft = EggCatcher.MAX_SHOO_SEC;
               
               /* Decrement the number of hens. */
               mNumHens--;
            }
         }
         
         /* If there was a touch input from the user and the minimum number of eggs have been 
          * caught, dump the eggs if the avatar is overlapping the basket. */
         if(getTouchFlag()) {
            /* Clear the touch flag. */
            setTouchFlag(false);
            
            if((mAvatarPos <= 0) && (mNumEggs >= EggCatcher.MIN_NUM_EGGS)) {
               /* Update the score. */
               mScore += mNumEggs * 10;
               updateScore();
               
               /* Reset the number of eggs. */
               mNumEggs = 0;
               updateNumEggs();
               
               /* Play the egg dumping audio clip. */
               mEggDump.play();
            }
         }

         /* Animate the sprites. */
         mAvatar.drawSprite(canvas, mFlipAvatar);
         
         if(mEagle.isDisplayed) {
            mEagle.birdSprite.drawSprite(canvas, false);
         }
         
         if(mCrow.isDisplayed) {
            mCrow.birdSprite.drawSprite(canvas, false);
         }
      }

      /**
       * GameThread class constructor.
       * 
       * @param handler Message handler for the thread.
       */
      public GameThread(Handler handler) {
         /* Initialize the UI message handler. */
         mUIMsgHandler = handler;
         
         /* Initialize the random number generator. */
         mRNG = new Random();
         
         /* Initialize the egg rectangle. */
         mEggRect = new Rect(0,0,0,0);
         
         /* Start from the first hen. */
         mEggX = mHensXPos[0];
         mEggY = mHensYPos[0];
         mNewEgg = true;
         
         /* Initialize the reference time. */
         mRefTime = System.currentTimeMillis();
      }

      @Override
      /**
       * Called when the thread is started.
       */
      public void run() {
         /* The thread exits when mExit is set to 1. */
         mExitThread = false;
         while(!mExitThread){
            /* The game is paused when mRun is 0 and running
             * when mRun is set 1. */
            while(mRunState){
               /* Initialize and lock the game canvas. */
               Canvas canvas = null;
               canvas = mSurfaceHolder.lockCanvas(null);
               
               /* Update game time. */
               mGameTime = System.currentTimeMillis();
               
               synchronized (mSurfaceHolder) {
                  /* Update game logic and animation. */
                  gameLoop(canvas);
               }
               
               /* Unlock the game canvas. */
               mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
            
            /* If the game is paused, sleep for a short interval
             * so as to not have load on the CPU. */
            try{Thread.sleep(100);}catch(Exception e){}
         }
      }
      
      /**
       * Set the game's running state.
       * 
       * @param b Running state.
       */
      public void setThreadState(boolean run) {
         synchronized (mSurfaceHolder) {
            mRunState = run;
         }
      }
      
      /**
       * Return the game's running state.
       * 
       * @return Running state.
       */
      public boolean getThreadState() {
         synchronized (mSurfaceHolder) {
            return mRunState;
         }
      }
      
      /**
       * Exit the game thread. 
       */
      public void exitGameThread() {
         synchronized (mSurfaceHolder) {
            mRunState = false;
            mExitThread = true;
         }
      }
   }
   
   /**
    * Message handler for this view.
    */
   private Handler mViewMsgHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         if(msg.what == NOTIFY_SCORE) {
            /* Set the score display. */
            mScoreText.setText((CharSequence)msg.obj);
         } else if(msg.what == NOTIFY_NUM_EGGS) {
            /* Set the number of eggs display. */
            mEggsText.setText((CharSequence)msg.obj);
         } else if(msg.what == NOTIFY_NUM_CHANCES) {
            /* Set the number of chances display. */
            mChancesText.setText((CharSequence)msg.obj);
         } else if(msg.what == NOTIFY_TIMELEFT) {
            /* Set the time left. */
            mTimeLeftText.setVisibility(TextView.VISIBLE);
            mTimeLeftText.setText((CharSequence)msg.obj);
         } else if(msg.what == NOTIFY_TIMECLEAR) {
            /* Clear time left field. */
            mTimeLeftText.setVisibility(TextView.INVISIBLE);
         } else if(msg.what == NOTIFY_GAMEOVER) {
            /* Show the game over text. */
            mGameOverText.setVisibility(TextView.VISIBLE);

            /* End the game. */
            (new Thread(mThisView)).start();
            endGame();
         }
      }
   };

   /**
    * GameView class constructor.
    * 
    * @param context Activity context
    * @param attrs View attributes
    */
   public GameView(Context context, AttributeSet attrs) throws Exception {
      super(context, attrs);

      Sprite2D crowSprite;
      Sprite2D eagleSprite;
      Util.AudioClip crowCall;
      Util.AudioClip eagleCall;
      Bitmap eggWholeBlue;
      Bitmap eggCrackedBlue;
      Bitmap eggWholeWhite;
      Bitmap eggCrackedWhite;
      Bitmap eggWholeYellow;
      Bitmap eggCrackedYellow;

      mContext = context;
      mThisView = this;
      
      /* Set the SurfaceHolder callback. */
      mSurfaceHolder = getHolder();
      mSurfaceHolder.addCallback(this);
      
      /* Instantiate the game thread. */
      mGameThread = new GameThread(mViewMsgHandler);

      /* Get the default display so we can find out the screen dimension. */
      mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
      
      /* Initialize the bitmaps. */
      mBgImg0 = BitmapFactory.decodeResource(context.getResources(),R.drawable.farm_bg_0);
      mBgImg1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.farm_bg_1);
      mBgImg2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.farm_bg_2);
      mBgImg3 = BitmapFactory.decodeResource(context.getResources(),R.drawable.farm_bg_3);
      
      /* Initialize the audio clips. */
      mHenCluck = (new Util()).new AudioClip(mContext, R.raw.hen_cluck); 
      mEggSmash = (new Util()).new AudioClip(mContext, R.raw.egg_smash); 
      mEggDump = (new Util()).new AudioClip(mContext, R.raw.egg_dump); 

      /* Create all the sprites. */
      XmlResourceParser crowXML = this.getResources().getXml(R.xml.sprite_crow);
      XmlResourceParser eagleXML = this.getResources().getXml(R.xml.sprite_eagle);
      XmlResourceParser avatarXML = this.getResources().getXml(R.xml.sprite_avatar);
      
      try {
         eagleSprite = new Sprite2D(context, eagleXML);
         mAvatar = new Sprite2D(context, avatarXML);
         crowSprite = new Sprite2D(context, crowXML);
      } catch (Exception e) {
         throw new Exception("Unable to create sprite.");
      }
      
      /* Initialize the avatar sprite. */
      mAvatar.initSprite(10, (mDisplay.getWidth() / 2), (mDisplay.getHeight() - mAvatar.getSpriteHeight()));

      /* Create and initialize the crow object and sprite. */
      mCrow = new Bird((mDisplay.getWidth() - crowSprite.getSpriteWidth()), mDisplay.getHeight() - (2 * crowSprite.getSpriteHeight()));
      mCrow.birdSprite = crowSprite;
      mCrow.birdSprite.initSprite(10, mCrow.x, mCrow.y);
      mCrow.birdCall = (new Util()).new AudioClip(mContext, R.raw.crow_call);;
      
      /* Create and initialize the eagle object and sprite. */
      mEagle = new Bird((mDisplay.getWidth() - eagleSprite.getSpriteWidth()), (int)(mDisplay.getHeight() - (1.5 * eagleSprite.getSpriteHeight())));
      mEagle.birdSprite = eagleSprite;
      mEagle.birdSprite.initSprite(10, mEagle.x, mEagle.y);
      mEagle.birdCall = (new Util()).new AudioClip(mContext, R.raw.eagle_call);
      
      /* Create and initialize the eggs. */
      mWhiteEgg = new Egg();
      mWhiteEgg.isDisplayed = false;
      mWhiteEgg.wholeEgg = BitmapFactory.decodeResource(context.getResources(),R.drawable.egg_whole_white);
      mWhiteEgg.crackedEgg = BitmapFactory.decodeResource(context.getResources(),R.drawable.egg_cracked_white);
      
      mBlueEgg = new Egg();
      mBlueEgg.isDisplayed = false;
      mBlueEgg.wholeEgg = BitmapFactory.decodeResource(context.getResources(),R.drawable.egg_whole_blue);
      mBlueEgg.crackedEgg = BitmapFactory.decodeResource(context.getResources(),R.drawable.egg_cracked_blue);

      mYellowEgg = new Egg();
      mYellowEgg.isDisplayed = false;
      mYellowEgg.wholeEgg = BitmapFactory.decodeResource(context.getResources(),R.drawable.egg_whole_yellow);
      mYellowEgg.crackedEgg = BitmapFactory.decodeResource(context.getResources(),R.drawable.egg_cracked_yellow);
            
      setFocusable(true);
      setFocusableInTouchMode(true);
   }
   
   /**
    * Pauses the game.
    */
   private void pauseGame() {
      mGameThread.setThreadState(false);
      mGamePausedText.setVisibility(TextView.VISIBLE);
   }
   
   /**
    * Start/resume the game.
    */
   private void startGame() {
      mGameThread.setThreadState(true);
      mGamePausedText.setVisibility(TextView.INVISIBLE);
   }
   
   /**
    * End the game.
    */
   private void endGame() {
      mGameThread.exitGameThread();
      try {
         mGameThread.join();
      }
      catch (InterruptedException e) {
         e.printStackTrace();
      }
   }

   /**
    * Can be used to get the GameThread object.
    * 
    * @return GameThread object
    */
   public GameThread getThread() {
      return mGameThread;
   }
   
   /**
    * Set {@link TextView} objects for score, eggs and chances. 
    * 
    * @param scoreView TextView for the game score.
    * @param eggsView TextView for the number of eggs being held. 
    * @param chancesView Number of chances left.
    * @param gameOverView TextView for Game Over.
    * @param gamePausedView TextView for Game Paused.
    */
   public void setTextViews(TextView scoreView, TextView eggsView,
                            TextView chancesView, TextView gameOverView,
                            TextView gamePausedView, TextView timeLeftView) {
      mScoreText = scoreView;
      mScoreText.setVisibility(TextView.VISIBLE);
      
      mEggsText = eggsView;
      mChancesText = chancesView;
      mGameOverText = gameOverView;
      mTimeLeftText = timeLeftView;
      mGamePausedText = gamePausedView;

      mEggsText.setVisibility(TextView.VISIBLE);
      mChancesText.setVisibility(TextView.VISIBLE);
      mGameOverText.setVisibility(TextView.INVISIBLE);
      mGamePausedText.setVisibility(TextView.INVISIBLE);
      mTimeLeftText.setVisibility(TextView.INVISIBLE);
   }

   @Override
   /**
    * Called when this thread is started. 
    */
   public void run() {
      /* Wait for the specified number of seconds, then exit the
       * EggCatcher activity. */
      try {
         Thread.sleep(GAMEOVER_DELAY * 1000);
      } catch(InterruptedException e) {
         e.printStackTrace();
      }
      
      /* Exit the activity. */
      ((EggCatcher)mContext).finish();
   }
   
   @Override
   /**
    * Key press handler.
    */
   public boolean onKeyDown(int keyCode, KeyEvent event) {
      boolean retVal = false;
      
      if(mGameThread.getThreadState() &&
         ((KeyEvent.KEYCODE_MENU == keyCode) || 
          (KeyEvent.KEYCODE_BACK == keyCode))) {
         /* The game is running, pause the game on either
          * MENU or BACK key. */
         pauseGame();
         retVal = true;
      } else if(!mGameThread.getThreadState() &&
                (KeyEvent.KEYCODE_BACK == keyCode)) {
         /* If the game is paused and the MENU key is pressed,
          * resume the game. */
         startGame();
         retVal = true;
      } else if(!mGameThread.getThreadState() &&
                (KeyEvent.KEYCODE_MENU == keyCode)) {
         /* If the game is paused and the BACK key is pressed,
          * resume the game. */
         retVal = true;
         
         /* End the game. */
         endGame();
         
         /* Exit the activity. */
         ((EggCatcher)mContext).finish();
      }
      
      return retVal;
   } 
   
   @Override
   /**
    * onTouchEvent callback method as specified in the
    * View class.
    */
   public boolean onTouchEvent(MotionEvent event) {
      /* Send the touch coordinates in a Point object. */
      Point point = new Point();
      Message msg = new Message();
      point.x = (int)event.getRawX();
      point.y = (int)event.getRawY();
      msg.what = EVENT_TOUCH_INPUT;
      msg.obj = point;
      mGameThread.mGameMsgHandler.sendMessage(msg);
      
      return super.onTouchEvent(event);
   }
   
   @Override
   /**
    * surfaceCreated callback method as specified in the
    * SurfaceHolder.Callback interface.
    */
   public void surfaceCreated(SurfaceHolder holder) {
      /* Create a new thread if the previous game ended. */
      if(mGameThread.getState() == Thread.State.TERMINATED) {
         /* Instantiate the game thread. */
         mGameThread = new GameThread(mViewMsgHandler);
         mGameThread.start();
      }else {
         mGameThread.start();
      }
      
      /* Start the game. */
      startGame();
   }

   @Override
   /**
    * surfaceChanged callback method as specified in the
    * SurfaceHolder.Callback interface.
    */
   public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      /* Nothing to see here, move along... */
   }
   
   @Override
   /**
    * Standard window-focus override. Notice focus lost so we can pause on
    * focus lost. e.g. user switches to take a call.
    */
   public void onWindowFocusChanged(boolean hasWindowFocus) {
       if (!hasWindowFocus) {
          pauseGame();
       }
   }
   
   @Override
   /**
    * surfaceDestroyed callback method as specified in the 
    * SurfaceHolder.Callback interface.
    */
   public void surfaceDestroyed(SurfaceHolder holder) {
      endGame();
   }
}
