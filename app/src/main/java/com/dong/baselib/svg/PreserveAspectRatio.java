

package com.dong.baselib.svg;

import java.util.HashMap;
import java.util.Map;


public class PreserveAspectRatio
{
   private final Alignment  alignment;
   private final Scale      scale;

   private static final Map<String, Alignment> aspectRatioKeywords = new HashMap<>(10);


   
   @SuppressWarnings("unused")
   public static final PreserveAspectRatio  UNSCALED = new PreserveAspectRatio(null, null);

   
   @SuppressWarnings("WeakerAccess")
   public static final PreserveAspectRatio  STRETCH = new PreserveAspectRatio(Alignment.none, null);

   
   @SuppressWarnings("WeakerAccess")
   public static final PreserveAspectRatio  LETTERBOX = new PreserveAspectRatio(Alignment.xMidYMid, Scale.meet);

   
   @SuppressWarnings("unused")
   public static final PreserveAspectRatio  START = new PreserveAspectRatio(Alignment.xMinYMin, Scale.meet);

   
   @SuppressWarnings("unused")
   public static final PreserveAspectRatio  END = new PreserveAspectRatio(Alignment.xMaxYMax, Scale.meet);

   
   @SuppressWarnings("unused")
   public static final PreserveAspectRatio  TOP = new PreserveAspectRatio(Alignment.xMidYMin, Scale.meet);

   
   @SuppressWarnings("unused")
   public static final PreserveAspectRatio  BOTTOM = new PreserveAspectRatio(Alignment.xMidYMax, Scale.meet);

   
   @SuppressWarnings("unused")
   public static final PreserveAspectRatio  FULLSCREEN = new PreserveAspectRatio(Alignment.xMidYMid, Scale.slice);

   
   @SuppressWarnings("unused")
   public static final PreserveAspectRatio  FULLSCREEN_START = new PreserveAspectRatio(Alignment.xMinYMin, Scale.slice);



   
   public enum Alignment
   {
      
      none,
      
      xMinYMin,
      
      xMidYMin,
      
      xMaxYMin,
      
      xMinYMid,
      
      xMidYMid,
      
      xMaxYMid,
      
      xMinYMax,
      
      xMidYMax,
      
      xMaxYMax
   }


   
   public enum Scale
   {
      
      meet,
      
      slice
   }


   static {
      aspectRatioKeywords.put("none", Alignment.none);
      aspectRatioKeywords.put("xMinYMin", Alignment.xMinYMin);
      aspectRatioKeywords.put("xMidYMin", Alignment.xMidYMin);
      aspectRatioKeywords.put("xMaxYMin", Alignment.xMaxYMin);
      aspectRatioKeywords.put("xMinYMid", Alignment.xMinYMid);
      aspectRatioKeywords.put("xMidYMid", Alignment.xMidYMid);
      aspectRatioKeywords.put("xMaxYMid", Alignment.xMaxYMid);
      aspectRatioKeywords.put("xMinYMax", Alignment.xMinYMax);
      aspectRatioKeywords.put("xMidYMax", Alignment.xMidYMax);
      aspectRatioKeywords.put("xMaxYMax", Alignment.xMaxYMax);
   }


   
   PreserveAspectRatio(Alignment alignment, Scale scale)
   {
      this.alignment = alignment;
      this.scale = scale;
   }


   
   public static PreserveAspectRatio  of(String value)
   {
      try {
         return parsePreserveAspectRatio(value);
      } catch (SVGParseException e) {
         throw new IllegalArgumentException(e.getMessage());
      }
   }


   
   @SuppressWarnings("WeakerAccess")
   public Alignment  getAlignment()
   {
      return alignment;
   }


   
   @SuppressWarnings("WeakerAccess")
   public Scale  getScale()
   {
      return scale;
   }


   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PreserveAspectRatio other = (PreserveAspectRatio) obj;
      return (alignment == other.alignment && scale == other.scale);
   }


   @Override
   public String toString()
   {
      return alignment + " " + scale;
   }




   private static PreserveAspectRatio  parsePreserveAspectRatio(String val) throws SVGParseException
   {
      TextScanner scan = new TextScanner(val);
      scan.skipWhitespace();

      String  word = scan.nextToken();
      if ("defer".equals(word)) {    
         scan.skipWhitespace();
         word = scan.nextToken();
      }

      Alignment  align = aspectRatioKeywords.get(word);
      Scale      scale = null;

      scan.skipWhitespace();

      if (!scan.empty()) {
         String meetOrSlice = scan.nextToken();
         switch (meetOrSlice) {
            case "meet":
               scale = Scale.meet; break;
            case "slice":
               scale = Scale.slice; break;
            default:
               throw new SVGParseException("Invalid preserveAspectRatio definition: " + val);
         }
      }
      return new PreserveAspectRatio(align, scale);
   }

}
