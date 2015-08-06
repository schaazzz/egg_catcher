package com.tinman.games.egg_catcher;

/* Imported packages. */
import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.R.bool;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * 2D sprite animation class.
 * 
 * @author Shahzeb Ihsan
 * @version 1.0
 */
@SuppressWarnings("unused")
public class Sprite2D {
   /**
    *  Private class members.
    */
   private int mXPos;
   private int mYPos;
   private int mFPS;
   private long mFrameTimer;

   private int mNumRows;
   private int mNumSprites;
   private int mNumColumns;
   private int mSpriteWidth;
   private int mSpriteHeight;
   private int mCollisionWidth;
   private int mCollisionHeight;
   
   private Rect mDestRect;
   private Rect mSpriteRect;
   private Rect mCollisionRect;
   private Bitmap mAnimation;
   private int mCurrentRow;
   private int mCurrentColumn;
   private int mCurrentSprite;

   /**
    * Sprite2D constructor.
    * 
    * @param context Context for the "view" which instantiated Sprite2D which will
    *                be used to get the resource.
    * @param spriteXML XmlResourceParser object for the sprite's XML file.
    */
   public Sprite2D(Context context, XmlResourceParser spriteXML) throws Exception {
      /* Initialization to default values. */
      mSpriteRect = new Rect(0,0,0,0);
      mDestRect = new Rect(0, 0, 0, 0);
      mCollisionRect = new Rect(0, 0, 0, 0);
      mFrameTimer = 0;
      mCurrentRow = 0;
      mCurrentColumn = 0;
      mCurrentSprite = 0;

      /* Decode the sprite XML to get the sprite attributes. */
      try {
         while(spriteXML.getEventType() != XmlResourceParser.END_DOCUMENT) {
            if(spriteXML.getEventType() == XmlResourceParser.START_TAG) {
         
               String str = spriteXML.getName();
               if(str.equals("Sprite")) {
                  /* Get the resource ID for the sprite and decode it to get the sprite bitmap. */
                  mAnimation = BitmapFactory.decodeResource(context.getResources(),
                                                            spriteXML.getAttributeResourceValue(null, "id", 0));
                  
                  /* Read the number of sprites. */
                  mNumSprites = spriteXML.getAttributeIntValue(null, "num_sprites", 0);
                  
                  /* Read the number of columns. */
                  mNumColumns = spriteXML.getAttributeIntValue(null, "num_columns", 0);
                  
                  /* Read the number of rows. */
                  mNumRows = spriteXML.getAttributeIntValue(null, "num_rows", 0);
                  
                  /* Read the width of the sprite. */
                  mSpriteWidth = spriteXML.getAttributeIntValue(null, "sprite_width", 0);
                  
                  /* Read the height of the sprite. */
                  mSpriteHeight = spriteXML.getAttributeIntValue(null, "sprite_height", 0);
                  
                  /* Read the collision rectangle parameters. */
                  mCollisionHeight = spriteXML.getAttributeIntValue(null, "collision_height", 0);
                  mCollisionWidth = spriteXML.getAttributeIntValue(null, "collision_width", 0);
                  
                  if((mAnimation == null) || (mNumSprites == 0) || (mNumColumns == 0) ||
                     (mNumRows == 0) || (mNumColumns == 0) || (mSpriteWidth == 0) ||
                     (mSpriteHeight == 0) || (mCollisionHeight == 0) || (mCollisionWidth == 0) ||
                     (mSpriteHeight < mCollisionHeight) || (mSpriteWidth < mCollisionWidth)) {
                     Log.w(this.getClass().getName(), "Invalid sprite parameters.");
                     throw new Exception("Invalid sprite parameters.");
                  }
               }
            }
               
            spriteXML.next();
         }
      }
      catch (XmlPullParserException e) {
         e.printStackTrace();
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      
      /* Initialize the sprite rectangle. */
      mSpriteRect.top = 0;
      mSpriteRect.bottom = mSpriteRect.top + mSpriteHeight;
      mSpriteRect.left = 0;
      mSpriteRect.right = mSpriteWidth;
   }

   /**
    * Initializes the sprite.
    * 
    * @param FPS Frames per second.
    * @param xPos Default X position of the sprite.
    * @param yPos Default Y position of the sprite.
    */
   public void initSprite(int FPS, int xPos, int yPos) {
      mFPS = 1000 / FPS;
      mXPos = xPos;
      mYPos = yPos;
   }
   
   /**
    * Returns the sprite's destination rectangle.
    * 
    * @return The sprite's destination rectangle.
    */
   public Rect getCollisionRect() {
      return mCollisionRect;
   }

   /**
    * Return the sprite width.
    * 
    * @return Sprite width.
    */
   public int getSpriteWidth() {
      return mSpriteWidth;
   }
   
   /**
    * Return the sprite height.
    * 
    * @return Sprite height.
    */
   public int getSpriteHeight() {
      return mSpriteHeight;
   }
   
   /**
    * Update sprite animation time.
    * 
    * @param gameTime The current game time.
    * @return
    */
   public boolean updateTime(long gameTime) {
      boolean willUpdate = false;
      
      /* Go to the next frame if minimum frame time has passed */ 
      if(gameTime > (mFrameTimer + mFPS)) {
         mFrameTimer = gameTime;
         mCurrentSprite += 1;
         mCurrentColumn += 1;
         
         /* Check if we need to move to the next row. */
         if(mCurrentColumn >= mNumColumns) {
            mCurrentRow += 1;
            mCurrentColumn = 0;
            
            if(mCurrentRow >= mNumRows) {
               mCurrentRow = 0;
            }
         }
         
         /* Loop back to the first sprite if we're at the last one. */
         if(mCurrentSprite >= mNumSprites) {
            mCurrentSprite = 0;
            mCurrentColumn = 0;
            mCurrentRow = 0;
         }
         
         /* Indicate that the sprite will be updated. */
         willUpdate = true;
      }

      /* Update the sprite rectangle (no change if the minimum frame time
       * hasn't passed. */
      mSpriteRect.left = (mCurrentSprite % mNumColumns) * mSpriteWidth;
      mSpriteRect.right = (mSpriteRect.left + mSpriteWidth);
      mSpriteRect.top = (mCurrentRow * mSpriteHeight);
      mSpriteRect.bottom = mSpriteRect.top + mSpriteHeight;
      
      return willUpdate;
   }

   /**
    * Draws the current sprite.
    * 
    * @param canvas The canvas on which to draw the sprite.
    * @param flip Setting this to true, flips the sprite 
    */
   public void drawSprite(Canvas canvas, boolean flip) {
      /* Update the collision rectangle. */
      mCollisionRect.left = mXPos + ((mSpriteWidth - mCollisionWidth) / 2);
      mCollisionRect.top = mYPos + (mSpriteHeight - mCollisionHeight);
      mCollisionRect.right = mXPos + mCollisionWidth;
      mCollisionRect.bottom = mYPos + mCollisionHeight;

      /* Draw the sprite. */
      if(flip) {
         /* Create a matrix to flip the sprite. */
         Matrix transform = new Matrix();
         transform.preScale(-1.0f, 1.0f);
         
         /* Create a flipped bitmap. */
         Bitmap flippedSprite = Bitmap.createBitmap(mAnimation, mSpriteRect.left, mSpriteRect.top, mSpriteWidth, mSpriteHeight, transform, false);
         
         /* Draw the flipped sprite. */
         canvas.drawBitmap(flippedSprite, mXPos, mYPos, null);
      }
      else {
         /* Create a destination rectangle for drawing the bitmap. */
         mDestRect.set(mXPos, mYPos, (mXPos + mSpriteWidth), (mYPos + mSpriteHeight));
         
         /* Draw the sprite. */
         canvas.drawBitmap(mAnimation, mSpriteRect, mDestRect, null);
      }
   }

   /**
    * Return the sprite's X position.
    * 
    * @return Sprite's X position.
    */   
   public int getXPos()
   {
      return mXPos;
   }
   
   /**
    * Return the sprite's Y position.
    * 
    * @return Sprite's Y position.
    */
   public int getYPos()
   {
      return mYPos;
   }

   /**
    * Set the sprite's X position.
    * 
    * @param yPos Sprite's new X position.
    */
   public void setXPos(int xPos)
   {
      mXPos = xPos;
   }

   /**
    * Set the sprite's Y position.
    * 
    * @param yPos Sprite's new Y position.
    */
   public void setYPos(int yPos)
   {
      mYPos = yPos;
   }
}