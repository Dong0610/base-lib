

package com.dong.baselib.svg;

import com.dong.baselib.svg.SVGBase.Box;



public class RenderOptionsBase
{
   String               css = null;
   CSSParser.Ruleset    cssRuleset = null;
   
   PreserveAspectRatio  preserveAspectRatio = null;
   String               targetId = null;
   Box              viewBox = null;
   String               viewId = null;
   Box              viewPort = null;


   
   public RenderOptionsBase()
   {
   }


   
   public static RenderOptionsBase create()
   {
      return new RenderOptionsBase();
   }


   
   public RenderOptionsBase(RenderOptionsBase other)
   {
      if (other == null)
         return;
      this.css = other.css;
      this.cssRuleset = other.cssRuleset;
      
      this.preserveAspectRatio = other.preserveAspectRatio;
      this.viewBox = other.viewBox;
      this.viewId = other.viewId;
      this.viewPort = other.viewPort;
      this.targetId = other.targetId;
   }

   
   public RenderOptionsBase css(CSS css)
   {
      this.cssRuleset = css.cssRuleset;
      this.css = null;
      return this;
   }

   public RenderOptionsBase css(String css)
   {
      this.css = css;
      this.cssRuleset = null;
      return this;
   }

   
   public boolean hasCss()
   {
      return this.css != null && this.css.trim().length() > 0 || this.cssRuleset != null;
   }


   
   @SuppressWarnings("UnusedReturnValue")
   public RenderOptionsBase preserveAspectRatio(PreserveAspectRatio preserveAspectRatio)
   {
      this.preserveAspectRatio = preserveAspectRatio;
      return this;
   }


   
   public boolean hasPreserveAspectRatio()
   {
      return this.preserveAspectRatio != null;
   }


   
   public RenderOptionsBase view(String viewId)
   {
      this.viewId = viewId;
      return this;
   }


   
   public boolean hasView()
   {
      return this.viewId != null;
   }


   
   public RenderOptionsBase viewBox(float minX, float minY, float width, float height)
   {
      this.viewBox = new Box(minX, minY, width, height);
      return this;
   }


   
   public boolean hasViewBox()
   {
      return this.viewBox != null;
   }


   
   public RenderOptionsBase viewPort(float minX, float minY, float width, float height)
   {
      this.viewPort = new Box(minX, minY, width, height);
      return this;
   }


   
   public boolean hasViewPort()
   {
      return this.viewPort != null;
   }


   
   public RenderOptionsBase target(String targetId)
   {
      this.targetId = targetId;
      return this;
   }


   
   public boolean hasTarget()
   {
      return this.targetId != null;
   }


}
