package com.tinman.games.egg_catcher;

/* Imported packages. */
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Message;

/* TODO: [Shahzeb] Enable when running on the hardware. */
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/* TODO: [Shahzeb] Enable for running with OI SensorSimulator. */
//import org.openintents.sensorsimulator.hardware.Sensor;
//import org.openintents.sensorsimulator.hardware.SensorEvent;
//import org.openintents.sensorsimulator.hardware.SensorEventListener;
//import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;

import com.tinman.games.egg_catcher.GameView;
import com.tinman.games.egg_catcher.GameView.GameThread;

/**
 * The game implementation activity.
 * 
 * @author Shahzeb Ihsan
 * @version 1.0
 * @see {@link SplashScreen}, {@link StartDialog}, {@link GameView}
 */
@SuppressWarnings("unused")
public class EggCatcher extends Activity implements SensorEventListener {
   /**
    *  Public class members.
    */
   public static final int NUM_CHANCES = 5;
   public static final int MIN_NUM_EGGS = 3;
   public static final int MAX_NUM_EGGS = 5;
   public static final int MAX_SHOO_SEC = 3;
   public static final int NUM_EGGS_STOLEN = 3;
   public static final int SCORE_PER_EGG = 10;
   public static final int BIRD_INTERVAL = 25;
   public static final int YELLOW_EGG_BONUS = 100;
   public static final int YELLOW_EGG_THRESHOLD = 500;
   public static final int BLUE_EGG_THRESHOLD = 1000;
   public static final int CROW_SHOO_BONUS = 25;
   public static final int EAGLE_SHOO_BONUS = 50;
   
   /**
    *  Private class members.
    */
   private int mLastSample;
   private GameView mGameView;
   private GameThread mGameThread;
   
   /* TODO: [Shahzeb] Enable for running with actual sensors. */
   private SensorManager mSensorManager;
   
   /* TODO: [Shahzeb] Enable for running with OI SensorSimulator. */
   //private SensorManagerSimulator mSensorManager;
   
   @Override
   /**
    *  Called when the activity is first created.
    */
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.eggcatcher);
      
      /* Initialize the game view. */
      mGameView = (GameView)findViewById(R.id.game);
      mGameView.setTextViews((TextView)findViewById(R.id.score_text),
                             (TextView)findViewById(R.id.eggs_text),
                             (TextView)findViewById(R.id.chances_text),
                             (TextView)findViewById(R.id.game_over),
                             (TextView)findViewById(R.id.game_paused),
                             (TextView)findViewById(R.id.time_text));
      
      /* Setup the sensor simulator. */
      /* TODO: [Shahzeb] Enable for running with OI SensorSimulator. */
      //mSensorManager = SensorManagerSimulator.getSystemService(this, SENSOR_SERVICE);
      //mSensorManager.connectSimulator();
      
      /* TODO: [Shahzeb] Enable for running with actual sensors. */
      mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
   }
   
   @Override
   /**
    *  Called when the activity is resumed.
    */
   protected void onResume() {
      super.onResume();
      
      /* Register the sensor listener. */
      /* TODO: [Shahzeb] Enable for running with OI SensorSimulator. */
      //mSensorManager.registerListener(this,
      //                                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
      //                                SensorManagerSimulator.SENSOR_DELAY_GAME);

      /* TODO: [Shahzeb] Enable for running with actual sensors. */
      mSensorManager.registerListener(this,
                                      mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                                      SensorManager.SENSOR_DELAY_GAME);
      
   }
   
   @Override
   /**
    *  Called when the activity is paused.
    */
   protected void onPause() {
      super.onPause();
   }

   @Override
   /**
    *  Called when the activity is stopped.
    */
   protected void onStop() {
      super.onStop();
      mSensorManager.unregisterListener(this);
   }
   
   @Override
   /**
    * onSensorChanged callback method as specified in SensorEventListener.
    */
   public void onSensorChanged(SensorEvent event) {
      int angle;
      /* Message to be sent to GameView, containing information related to
       * the tilt of the device. */
      Message msg = new Message();
      msg.what = GameView.EVENT_STAND_STILL;
      
      /* If the game thread object is not initialized, initialize it. */
      if(mGameThread == null)
      {
         mGameThread = mGameView.getThread();
         return;
      }
      
      /* Handle the orientation sensor event. */
      synchronized(this) {
         switch(event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION:
               /* Convert the pitch (x-axis) and roll (y-axis) to the angle using
                * the following formula:
                * 
                *          angle = inverse tangent(y / x)
                * 
                * Positive angles are considered as left movement and negative
                * angles are considered as right movement. */
               angle = (int)(Math.toDegrees(Math.atan(event.values[2] / event.values[1])));
               
               /* Send the sensor value. */
               if(angle > 0){
                  msg.what = GameView.EVENT_MOVE_LEFT;
                  msg.obj = angle;
                  mGameThread.mGameMsgHandler.sendMessage(msg);
               } else if(angle < 0){
                  msg.what = GameView.EVENT_MOVE_RIGHT;
                  msg.obj = angle;
                  mGameThread.mGameMsgHandler.sendMessage(msg);
               }
         }
      }
   }
   
   @Override
   /**
    * onAccuracyChanged callback method as specified in SensorEventListener.
    */
   public void onAccuracyChanged(Sensor sensor, int accuracy) {
      /* Nothing to see here, move along... */
   }
}
