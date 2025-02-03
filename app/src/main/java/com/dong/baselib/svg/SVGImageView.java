
package com.dong.baselib.svg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dong.baselib.R;

public class SVGImageView extends androidx.appcompat.widget.AppCompatImageView
{
   private SVG                  svg = null;
   private final RenderOptions  renderOptions = new RenderOptions();

   private static Method  setLayerTypeMethod = null;


   static {
      try
      {
         setLayerTypeMethod = View.class.getMethod("setLayerType", Integer.TYPE, Paint.class);
      }
      catch (NoSuchMethodException e) {  }
   }


   public SVGImageView(Context context)
   {
      super(context);
   }


   public SVGImageView(Context context, AttributeSet attrs)
   {
      super(context, attrs, 0);
      init(attrs, 0);
   }


   public SVGImageView(Context context, AttributeSet attrs, int defStyle)
   {
      super(context, attrs, defStyle);
      init(attrs, defStyle);
   }

   
   private void  init(AttributeSet attrs, int defStyle)
   {
      if (isInEditMode())
         return;

      TypedArray a = getContext().getTheme()
                     .obtainStyledAttributes(attrs, R.styleable.SVGImageView, defStyle, 0);
      try
      {
         
         String  css = a.getString(R.styleable.SVGImageView_css);
         if (css != null)
            renderOptions.css(css);

         
         int  resourceId = a.getResourceId(R.styleable.SVGImageView_svg, -1);
         if (resourceId != -1) {
            setImageResource(resourceId);
            return;
         }

         
         
         String  url = a.getString(R.styleable.SVGImageView_svg);
         if (url != null)
         {
            Uri  uri = Uri.parse(url);
            if (internalSetImageURI(uri))
               return;

            
            if (internalSetImageAsset(url))
               return;

            
            
            setFromString(url);
         }
         
      } finally {
         a.recycle();
      }
   }


   
   public void  setSVG(SVG svg)
   {
      if (svg == null)
         throw new IllegalArgumentException("Null value passed to setSVG()");
      this.svg = svg;
      doRender();
   }


   
   public void  setSVG(SVG svg, String css)
   {
      if (svg == null)
         throw new IllegalArgumentException("Null value passed to setSVG()");

      this.svg = svg;
      this.renderOptions.css(css);

      doRender();
   }


   
   public void  setCSS(String css)
   {
      this.renderOptions.css(css);
      doRender();
   }



   
   @Override
   public void setImageResource(int resourceId)
   {
      new LoadResourceTask(this.getContext()).execute(resourceId);
   }


   
   @Override
   public void  setImageURI(Uri uri)
   {
      if (!internalSetImageURI(uri))
         Log.e("SVGImageView", "File not found: " + uri);
   }


   
   public void  setImageAsset(String filename)
   {
      if (!internalSetImageAsset(filename))
         Log.e("SVGImageView", "File not found: " + filename);
   }



   


   
   private boolean  internalSetImageURI(Uri uri)
   {
      try
      {
         InputStream  is = getContext().getContentResolver().openInputStream(uri);
         new LoadURITask().execute(is);
         return true;
      }
      catch (FileNotFoundException e)
      {
         return false;
      }

   }


   private boolean  internalSetImageAsset(String filename)
   {
      try
      {
         InputStream  is = getContext().getAssets().open(filename);
         new LoadURITask().execute(is);
         return true;
      }
      catch (IOException e)
      {
         return false;
      }

   }


   private void setFromString(String url)
   {
      try {
         this.svg = SVG.getFromString(url);
         doRender();
      } catch (SVGParseException e) {
         
         Log.e("SVGImageView", "Could not find SVG at: " + url);
      }
   }


   


   @SuppressLint("StaticFieldLeak")
   private class LoadResourceTask extends AsyncTask<Integer, Integer, SVG>
   {
      private final Context  context;

      LoadResourceTask(Context context)
      {
         this.context = context;
      }

      protected SVG  doInBackground(Integer... params)
      {
         int  resourceId = params[0];
         try
         {
            return SVG.getFromResource(context, resourceId);
         }
         catch (SVGParseException e)
         {
            Log.e("SVGImageView", String.format("Error loading resource 0x%x: %s", resourceId, e.getMessage()));
         }
         return null;
      }

      protected void  onPostExecute(SVG svg)
      {
         SVGImageView.this.svg = svg;
         doRender();
      }
   }


   @SuppressLint("StaticFieldLeak")
   private class LoadURITask extends AsyncTask<InputStream, Integer, SVG>
   {
      protected SVG  doInBackground(InputStream... is)
      {
         try
         {
            return SVG.getFromInputStream(is[0]);
         }
         catch (SVGParseException e)
         {
            Log.e("SVGImageView", "Parse error loading URI: " + e.getMessage());
         }
         finally
         {
            try
            {
               is[0].close();
            }
            catch (IOException e) {  }
         }
         return null;
      }

      protected void  onPostExecute(SVG svg)
      {
         SVGImageView.this.svg = svg;
         doRender();
      }
   }


   


   
   private void  setSoftwareLayerType()
   {
      if (setLayerTypeMethod == null)
         return;

      try
      {
         int  LAYER_TYPE_SOFTWARE = View.class.getField("LAYER_TYPE_SOFTWARE").getInt(new View(getContext()));
         setLayerTypeMethod.invoke(this, LAYER_TYPE_SOFTWARE, null);
      }
      catch (Exception e)
      {
         Log.w("SVGImageView", "Unexpected failure calling setLayerType", e);
      }
   }


   private void  doRender()
   {
      if (svg == null)
         return;
      Picture  picture = this.svg.renderToPicture(renderOptions);
      setSoftwareLayerType();
      setImageDrawable(new PictureDrawable(picture));
   }

}
