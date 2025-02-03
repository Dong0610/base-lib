

package com.dong.baselib.svg;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.Typeface.Builder;
import android.os.Build;
import android.util.Log;

public class SimpleAssetResolver extends SVGExternalFileResolver
{
   private static final String  TAG = "SimpleAssetResolver";

   private AssetManager  assetManager;
   

   @SuppressWarnings({"WeakerAccess", "unused"})
   public SimpleAssetResolver(AssetManager assetManager)
   {
      super();
      this.assetManager = assetManager;
   }


   private static final Set<String>  supportedFormats = new HashSet<>(8);

   
   static {
      
      supportedFormats.add("image/svg+xml");
      supportedFormats.add("image/jpeg");
      supportedFormats.add("image/png");
      
      supportedFormats.add("image/pjpeg");
      supportedFormats.add("image/gif");
      supportedFormats.add("image/bmp");
      supportedFormats.add("image/x-windows-bmp");
      
      if (Build.VERSION.SDK_INT >= 14) {
         supportedFormats.add("image/webp");
      }
      
      if (Build.VERSION.SDK_INT >= 31) {
         supportedFormats.add("image/avif");
      }
   }


   
   @Override
   public Typeface  resolveFont(String fontFamily, float fontWeight, String fontStyle, float fontStretch)
   {
      Log.i(TAG, "resolveFont('"+fontFamily+"',"+fontWeight+",'"+fontStyle+"',"+fontStretch+")");

      
      try
      {
         return Typeface.createFromAsset(assetManager, fontFamily + ".ttf");
      }
      catch (RuntimeException ignored) {}

      
      try
      {
         return Typeface.createFromAsset(assetManager, fontFamily + ".otf");
      }
      catch (RuntimeException e) {}

      
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
      {
         Builder builder = new Builder(assetManager, fontFamily + ".ttc");
         
         builder.setTtcIndex(0);
         return builder.build();
      }

      return null;
   }


   
   @Override
   public Bitmap  resolveImage(String filename)
   {
      Log.i(TAG, "resolveImage("+filename+")");

      try
      {
         InputStream  istream = assetManager.open(filename);
         return BitmapFactory.decodeStream(istream);
      }
      catch (IOException e1)
      {
         return null;
      }
   }


   
   @Override
   public boolean  isFormatSupported(String mimeType)
   {
      return supportedFormats.contains(mimeType);
   }


   
   @Override
   public String  resolveCSSStyleSheet(String url)
   {
      Log.i(TAG, "resolveCSSStyleSheet("+url+")");
      return getAssetAsString(url);
   }


   
   private String getAssetAsString(String url)
   {
      InputStream is = null;
      
      try
      {
         is = assetManager.open(url);

         
         Reader r = new InputStreamReader(is, Charset.forName("UTF-8"));
         char[]         buffer = new char[4096];
         StringBuilder  sb = new StringBuilder();
         int            len = r.read(buffer);
         while (len > 0) {
            sb.append(buffer, 0, len);
            len = r.read(buffer);
         }
         return sb.toString();
      }
      catch (IOException e)
      {
         return null;
      }
      finally {
         try {
            if (is != null)
               is.close();
         } catch (IOException e) {
           
         }
      }
   }

}
