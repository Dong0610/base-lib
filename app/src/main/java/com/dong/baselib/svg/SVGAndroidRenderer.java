
package com.dong.baselib.svg;


import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.dong.baselib.BuildConfig;
import com.dong.baselib.svg.SVGBase.Box;
import com.dong.baselib.svg.SVGBase.Circle;
import com.dong.baselib.svg.SVGBase.ClipPath;
import com.dong.baselib.svg.SVGBase.Colour;
import com.dong.baselib.svg.SVGBase.CurrentColor;
import com.dong.baselib.svg.SVGBase.Ellipse;
import com.dong.baselib.svg.SVGBase.GradientElement;
import com.dong.baselib.svg.SVGBase.GradientSpread;
import com.dong.baselib.svg.SVGBase.GraphicsElement;
import com.dong.baselib.svg.SVGBase.Group;
import com.dong.baselib.svg.SVGBase.Image;
import com.dong.baselib.svg.SVGBase.Length;
import com.dong.baselib.svg.SVGBase.Line;
import com.dong.baselib.svg.SVGBase.Marker;
import com.dong.baselib.svg.SVGBase.Mask;
import com.dong.baselib.svg.SVGBase.NotDirectlyRendered;
import com.dong.baselib.svg.SVGBase.PaintReference;
import com.dong.baselib.svg.SVGBase.PathDefinition;
import com.dong.baselib.svg.SVGBase.PathInterface;
import com.dong.baselib.svg.SVGBase.Pattern;
import com.dong.baselib.svg.SVGBase.PolyLine;
import com.dong.baselib.svg.SVGBase.Polygon;
import com.dong.baselib.svg.SVGBase.Rect;
import com.dong.baselib.svg.SVGBase.SolidColor;
import com.dong.baselib.svg.SVGBase.Stop;
import com.dong.baselib.svg.SVGBase.Svg;
import com.dong.baselib.svg.SVGBase.SvgConditional;
import com.dong.baselib.svg.SVGBase.SvgContainer;
import com.dong.baselib.svg.SVGBase.SvgElement;
import com.dong.baselib.svg.SVGBase.SvgElementBase;
import com.dong.baselib.svg.SVGBase.SvgLinearGradient;
import com.dong.baselib.svg.SVGBase.SvgObject;
import com.dong.baselib.svg.SVGBase.SvgPaint;
import com.dong.baselib.svg.SVGBase.SvgRadialGradient;
import com.dong.baselib.svg.SVGBase.Switch;
import com.dong.baselib.svg.SVGBase.Symbol;
import com.dong.baselib.svg.SVGBase.TRef;
import com.dong.baselib.svg.SVGBase.TSpan;
import com.dong.baselib.svg.SVGBase.Text;
import com.dong.baselib.svg.SVGBase.TextContainer;
import com.dong.baselib.svg.SVGBase.TextPath;
import com.dong.baselib.svg.SVGBase.TextSequence;
import com.dong.baselib.svg.SVGBase.Unit;
import com.dong.baselib.svg.SVGBase.Use;
import com.dong.baselib.svg.SVGBase.View;
import com.dong.baselib.svg.Style.CSSBlendMode;
import com.dong.baselib.svg.Style.FontStyle;
import com.dong.baselib.svg.Style.Isolation;
import com.dong.baselib.svg.Style.RenderQuality;
import com.dong.baselib.svg.Style.TextAnchor;
import com.dong.baselib.svg.Style.TextDecoration;
import com.dong.baselib.svg.Style.VectorEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

public class SVGAndroidRenderer
{
   private static final String  TAG = "SVGAndroidRenderer";

   private static final boolean  SUPPORTS_FONT_HINTING = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
   private static final boolean  SUPPORTS_STROKED_UNDERLINES = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
   private static final boolean  SUPPORTS_PATH_OP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
   private static final boolean  SUPPORTS_PAINT_FONT_FEATURE_SETTINGS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
   private static final boolean  SUPPORTS_PAINT_LETTER_SPACING = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
   private static final boolean  SUPPORTS_PAINT_FONT_VARIATION_SETTINGS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
   private static final boolean  SUPPORTS_BLEND_MODE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;          
   private static final boolean  SUPPORTS_PAINT_WORD_SPACING = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
   private static final boolean  SUPPORTS_SAVE_LAYER_FLAGLESS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
   private static final boolean  SUPPORTS_RADIAL_GRADIENT_WITH_FOCUS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;

   private static final java.util.regex.Pattern PATTERN_TABS_OR_LINE_BREAKS = java.util.regex.Pattern.compile("[\\n\\t]");
   private static final java.util.regex.Pattern PATTERN_TABS = java.util.regex.Pattern.compile("\\t");
   private static final java.util.regex.Pattern PATTERN_LINE_BREAKS = java.util.regex.Pattern.compile("\\n");
   private static final java.util.regex.Pattern PATTERN_START_SPACES = java.util.regex.Pattern.compile("^\\s+");
   private static final java.util.regex.Pattern PATTERN_END_SPACES = java.util.regex.Pattern.compile("\\s+$");
   private static final java.util.regex.Pattern PATTERN_DOUBLE_SPACES = java.util.regex.Pattern.compile("\\s{2,}");

   private final Canvas   canvas;
   private final float    dpi;    

   
   private SVGBase document;
   private RendererState        state;
   private Stack<RendererState> stateStack;  
   
   
   private Stack<SvgContainer>  parentStack; 
   private Stack<Matrix>        matrixStack; 

   private static final float  BEZIER_ARC_FACTOR = 0.5522847498f;

   
   
   public static final float  LUMINANCE_TO_ALPHA_RED   = 0.2127f;
   public static final float  LUMINANCE_TO_ALPHA_GREEN = 0.7151f;
   public static final float  LUMINANCE_TO_ALPHA_BLUE  = 0.0722f;

   private static final String DEFAULT_FONT_FAMILY = "serif";

   private static HashSet<String>  supportedFeatures = null;

   private CSSParser.RuleMatchContext  ruleMatchContext = null;

   private SVGExternalFileResolver externalFileResolver;


   public static class RendererState
   {
      Style    style;
      boolean  hasFill;
      boolean  hasStroke;
      Box      viewPort;
      Box      viewBox;
      boolean  spacePreserve;

      final Paint    fillPaint;
      final Paint    strokePaint;

      final CSSFontFeatureSettings    fontFeatureSet;
      final CSSFontVariationSettings  fontVariationSet;


      @TargetApi(Build.VERSION_CODES.LOLLIPOP)
      RendererState()
      {
         fillPaint = new Paint();
         fillPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
         if (SUPPORTS_FONT_HINTING) {
            fillPaint.setHinting(Paint.HINTING_OFF);
         }
         fillPaint.setStyle(Paint.Style.FILL);
         fillPaint.setTypeface(Typeface.DEFAULT);

         strokePaint = new Paint();
         strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
         if (SUPPORTS_FONT_HINTING) {
            strokePaint.setHinting(Paint.HINTING_OFF);
         }
         strokePaint.setStyle(Paint.Style.STROKE);
         strokePaint.setTypeface(Typeface.DEFAULT);

         fontFeatureSet = new CSSFontFeatureSettings();
         fontVariationSet = new CSSFontVariationSettings();

         style = Style.getDefaultStyle();
      }

      RendererState(RendererState copy)
      {
         hasFill = copy.hasFill;
         hasStroke = copy.hasStroke;
         fillPaint = new Paint(copy.fillPaint);
         strokePaint = new Paint(copy.strokePaint);
         if (copy.viewPort != null)
            viewPort = new Box(copy.viewPort);
         if (copy.viewBox != null)
            viewBox = new Box(copy.viewBox);
         spacePreserve = copy.spacePreserve;
         fontFeatureSet = new CSSFontFeatureSettings(copy.fontFeatureSet);
         fontVariationSet = new CSSFontVariationSettings(copy.fontVariationSet);
         try
         {
            style = (Style) copy.style.clone();
         }
         catch (CloneNotSupportedException e)
         {
            
            Log.e(TAG, "Unexpected clone error", e);
            style = Style.getDefaultStyle();
         }
      }
   }


   private void  resetState()
   {
      state = new RendererState();
      stateStack = new Stack<>();

      
      updateStyle(state, Style.getDefaultStyle());

      state.viewPort = null;  

      state.spacePreserve = false;

      
      stateStack.push(new RendererState(state));   

      
      
      matrixStack = new Stack<>();
      parentStack = new Stack<>();
   }


   

   SVGAndroidRenderer(Canvas canvas, float defaultDPI, SVGExternalFileResolver externalFileResolver)
   {
      this.canvas = canvas;
      this.dpi = defaultDPI;
      this.externalFileResolver = externalFileResolver;
   }


   float  getDPI()
   {
      return dpi;
   }


   float  getCurrentFontSize()
   {
      return state.fillPaint.getTextSize();
   }


   float  getCurrentFontXHeight()
   {
      
      return state.fillPaint.getTextSize() / 2f;
   }


   
   Box getEffectiveViewPortInUserUnits()
   {
      if (state.viewBox != null)
         return state.viewBox;
      else
         return state.viewPort;
   }


   
   void  renderDocument(SVGBase document, RenderOptionsBase renderOptions)
   {
      if (renderOptions == null)
         throw new NullPointerException("renderOptions shouldn't be null");  

      this.document = document;

      Svg  rootObj = document.getRootElement();

      if (rootObj == null) {
         warn("Nothing to render. Document is empty.");
         return;
      }

      Box          viewBox;
      PreserveAspectRatio  preserveAspectRatio;

      if (renderOptions.hasView())
      {
         SvgObject  obj = this.document.getElementById(renderOptions.viewId);
         if (!(obj instanceof View)) {
            Log.w(TAG, String.format("View element with id \"%s\" not found.", renderOptions.viewId));
            return;
         }
         View  view = (View) obj;

         if (view.viewBox == null) {
            Log.w(TAG, String.format("View element with id \"%s\" is missing a viewBox attribute.", renderOptions.viewId));
            return;
         }
         viewBox = view.viewBox;
         preserveAspectRatio = view.preserveAspectRatio;
      }
      else
      {
         viewBox = renderOptions.hasViewBox() ? renderOptions.viewBox
                                              : rootObj.viewBox;
         preserveAspectRatio = renderOptions.hasPreserveAspectRatio() ? renderOptions.preserveAspectRatio
                                                                      : rootObj.preserveAspectRatio;
      }

      if (renderOptions.hasCss()) {
         if (renderOptions.css != null) {
            CSSParser parser = new CSSParser(CSSParser.Source.RenderOptions, externalFileResolver);
            document.addCSSRules(parser.parse(renderOptions.css));
         } else if (renderOptions.cssRuleset != null) {
            document.addCSSRules(renderOptions.cssRuleset);
         }
      }
      if (renderOptions.hasTarget()) {
         this.ruleMatchContext = new CSSParser.RuleMatchContext();
         this.ruleMatchContext.targetElement = document.getElementById(renderOptions.targetId);
      }

      
      resetState();

      checkXMLSpaceAttribute(rootObj);

      
      statePush(true);

      Box  viewPort = new Box(renderOptions.viewPort);
      
      if (rootObj.width != null)
         viewPort.width = rootObj.width.floatValue(this, viewPort.width);
      if (rootObj.height != null)
         viewPort.height = rootObj.height.floatValue(this, viewPort.height);

      
      render(rootObj, viewPort, viewBox, preserveAspectRatio);

      
      statePop();

      if (renderOptions.hasCss())
         document.clearRenderCSSRules();
   }


   
   


   private void  render(SvgObject obj)
   {
      if (obj instanceof NotDirectlyRendered)
         return;

      
      statePush();

      checkXMLSpaceAttribute(obj);

      if (obj instanceof Svg) {
         render((Svg) obj);
      } else if (obj instanceof Use) {
         render((Use) obj);
      } else if (obj instanceof Switch) {
         render((Switch) obj);
      } else if (obj instanceof Group) {   
         render((Group) obj);
      } else if (obj instanceof Image) {
         render((Image) obj);
      } else if (obj instanceof SVGBase.Path) {
         render((SVGBase.Path) obj);
      } else if (obj instanceof Rect) {
         render((Rect) obj);
      } else if (obj instanceof Circle) {
         render((Circle) obj);
      } else if (obj instanceof Ellipse) {
         render((Ellipse) obj);
      } else if (obj instanceof Line) {
         render((Line) obj);
      } else if (obj instanceof Polygon) {
         render((Polygon) obj);
      } else if (obj instanceof PolyLine) {
         render((PolyLine) obj);
      } else if (obj instanceof Text) {
         render((Text) obj);
      }

      
      statePop();
   }


   


   private void  renderChildren(SvgContainer obj, boolean isContainer)
   {
      if (isContainer) {
         parentPush(obj);
      }

      for (SvgObject child: obj.getChildren()) {
         render(child);
      }

      if (isContainer) {
         parentPop();
      }
   }


   


   private void  statePush()
   {
      statePush(false);
   }

   private void  statePush(boolean isRootContext)
   {
      if (isRootContext) {
         
         
         canvasSaveLayer(canvas, null, null);
      } else {
         canvas.save();
      }
      
      stateStack.push(state);
      state = new RendererState(state);
   }


   private void  statePop()
   {
      
      canvas.restore();
      
      state = stateStack.pop();
   }


   
   private void canvasSaveLayer(Canvas canvas, RectF bounds, Paint paint)
   {
      if (SUPPORTS_SAVE_LAYER_FLAGLESS) {
         
         canvas.saveLayer(bounds, paint);
      } else {
         
         CanvasLegacy.saveLayer(canvas, bounds, paint, CanvasLegacy.ALL_SAVE_FLAG);
      }
   }


   


   private void  parentPush(SvgContainer obj)
   {
      parentStack.push(obj);
      matrixStack.push(canvas.getMatrix());
   }


   private void  parentPop()
   {
      parentStack.pop();
      matrixStack.pop();
   }


   


   private void updateStyleForElement(RendererState state, SvgElementBase obj)
   {
      boolean  isRootSVG = (obj.parent == null);
      state.style.resetNonInheritingProperties(isRootSVG);

      
      if (obj.baseStyle != null)
         updateStyle(state, obj.baseStyle);

      
      if (document.hasCSSRules())
      {
         for (CSSParser.Rule rule: document.getCSSRules())
         {
            if (CSSParser.ruleMatch(this.ruleMatchContext, rule.selector, obj)) {
               updateStyle(state, rule.style);
            }
         }
      }

      
      if (obj.style != null)
         updateStyle(state, obj.style);
   }


   
   private void checkXMLSpaceAttribute(SvgObject obj)
   {
      if (!(obj instanceof SvgElementBase))
        return;

      SvgElementBase bobj = (SvgElementBase) obj;
      if (bobj.spacePreserve != null)
         state.spacePreserve = bobj.spacePreserve;
   }


   
   private void doFilledPath(SvgElement obj, Path path)
   {
      
      if (state.style.fill instanceof PaintReference)
      {
         SvgObject  ref = document.resolveIRI(((PaintReference) state.style.fill).href);
         if (ref instanceof Pattern) {
            Pattern  pattern = (Pattern)ref;
            fillWithPattern(obj, path, pattern);
            return;
         }
      }

      
      canvas.drawPath(path, state.fillPaint);
   }


   private void  doStroke(Path path)
   {
      

      if (state.style.vectorEffect == VectorEffect.NonScalingStroke)
      {
         
         

         
         Matrix  currentMatrix = canvas.getMatrix();
         
         Path  transformedPath = new Path();
         path.transform(currentMatrix, transformedPath);
         
         canvas.setMatrix(new Matrix());

         
         Shader  shader = state.strokePaint.getShader();
         Matrix  currentShaderMatrix = new Matrix();
         if (shader != null) {
            shader.getLocalMatrix(currentShaderMatrix);
            Matrix  newShaderMatrix = new Matrix(currentShaderMatrix);
            newShaderMatrix.postConcat(currentMatrix);
            shader.setLocalMatrix(newShaderMatrix);
         }

         
         canvas.drawPath(transformedPath, state.strokePaint);

         
         canvas.setMatrix(currentMatrix);
         
         if (shader != null)
            shader.setLocalMatrix(currentShaderMatrix);
      }
      else
      {
         canvas.drawPath(path, state.strokePaint);
      }
   }


   


   private static void  warn(String format, Object... args)
   {
      Log.w(TAG, String.format(format, args));
   }


   private static void  error(String format, Object... args)
   {
      Log.e(TAG, String.format(format, args));
   }


   private static void  debug(String format, Object... args)
   {
      if (BuildConfig.DEBUG)
         Log.d(TAG, String.format(format, args));
   }


   


   
   


   private void render(Svg obj)
   {
      
      Box viewPort = makeViewPort(obj.x, obj.y, obj.width, obj.height);

      render(obj, viewPort, obj.viewBox, obj.preserveAspectRatio);
   }


   
   private void render(Svg obj, Box viewPort)
   {
      render(obj, viewPort, obj.viewBox, obj.preserveAspectRatio);
   }


   
   
   
   private void render(Svg obj, Box viewPort, Box viewBox, PreserveAspectRatio positioning)
   {
      debug("Svg render");

      if (viewPort.width == 0f || viewPort.height == 0f)
         return;

      
      if (positioning == null)
         positioning = (obj.preserveAspectRatio != null) ? obj.preserveAspectRatio : PreserveAspectRatio.LETTERBOX;

      updateStyleForElement(state, obj);

      if (!display())
         return;

      state.viewPort = viewPort;

      if (!state.style.overflow) {
         setClipRect(state.viewPort.minX, state.viewPort.minY, state.viewPort.width, state.viewPort.height);
      }

      checkForClipPath(obj, state.viewPort);

      if (viewBox != null) {
         canvas.concat(calculateViewBoxTransform(state.viewPort, viewBox, positioning));
         state.viewBox = obj.viewBox;  
      } else {
         canvas.translate(state.viewPort.minX, state.viewPort.minY);
         state.viewBox = null;
      }

      boolean  compositing = pushLayer();

      
      viewportFill();

      renderChildren(obj, true);

      if (compositing)
         popLayer(obj);

      updateParentBoundingBox(obj);
   }


   
   private Box makeViewPort(Length x, Length y, Length width, Length height)
   {
      float  _x = (x != null) ? x.floatValueX(this) : 0f;
      float  _y = (y != null) ? y.floatValueY(this) : 0f;

      Box viewPortUser = getEffectiveViewPortInUserUnits();
      float  _w = (width != null) ? width.floatValueX(this) : viewPortUser.width;  
      float  _h = (height != null) ? height.floatValueY(this) : viewPortUser.height;

      return new Box(_x, _y, _w, _h);
   }


   


   
   private void render(Group obj)
   {
      debug(obj.getNodeName() + " render");

      updateStyleForElement(state, obj);

      if (!display())
         return;

      if (obj.transform != null) {
         canvas.concat(obj.transform);
      }

      checkForClipPath(obj);

      boolean  compositing = pushLayer();

      renderChildren(obj, true);

      if (compositing)
         popLayer(obj);

      updateParentBoundingBox(obj);
   }


   


   
   private void updateParentBoundingBox(SvgElement obj)
   {
      if (obj.parent == null)       
         return;
      if (obj.boundingBox == null)  
         return;

      
      Matrix  m = new Matrix();
      
      if (matrixStack.peek().invert(m)) {
         float[] pts = {obj.boundingBox.minX, obj.boundingBox.minY,
                        obj.boundingBox.maxX(), obj.boundingBox.minY,
                        obj.boundingBox.maxX(), obj.boundingBox.maxY(),
                        obj.boundingBox.minX, obj.boundingBox.maxY()};
         
         m.preConcat(canvas.getMatrix());
         m.mapPoints(pts);
         
         RectF  rect = new RectF(pts[0], pts[1], pts[0], pts[1]);
         for (int i=2; i<=6; i+=2) {
            if (pts[i] < rect.left) rect.left = pts[i]; 
            if (pts[i] > rect.right) rect.right = pts[i]; 
            if (pts[i+1] < rect.top) rect.top = pts[i+1]; 
            if (pts[i+1] > rect.bottom) rect.bottom = pts[i+1]; 
         }
         
         SvgElement  parent = (SvgElement) parentStack.peek();
         if (parent.boundingBox == null)
            parent.boundingBox = Box.fromLimits(rect.left, rect.top, rect.right, rect.bottom);
         else
            parent.boundingBox.union(Box.fromLimits(rect.left, rect.top, rect.right, rect.bottom));
      }
   }


   


   private boolean  pushLayer()
   {
      return pushLayer(1f);
   }


   private boolean  pushLayer(float opacityAdjustment)
   {
      
      

      if (!requiresCompositing() && opacityAdjustment == 1f)
         return false;

      
      Paint  savePaint = new Paint();
      savePaint.setAlpha(clamp255(state.style.opacity * opacityAdjustment));
      if (SUPPORTS_BLEND_MODE && state.style.mixBlendMode != CSSBlendMode.normal) {
         setBlendMode(savePaint);
      }
      canvasSaveLayer(canvas, null, savePaint);

      
      stateStack.push(state);
      state = new RendererState(state);

      if (state.style.mask != null) {
         SvgObject  ref = document.resolveIRI(state.style.mask);
         
         if (!(ref instanceof Mask)) {
            
            error("Mask reference '%s' not found", state.style.mask);
            state.style.mask = null;
            return true;
         }

         
         
      }

      return true;
   }


   private void  popLayer(SvgElement obj)
   {
      popLayer(obj, obj.boundingBox);
   }


   
   private void  popLayer(SvgElement obj, Box originalObjBBox)
   {
      
      if (state.style.mask != null) {
         
         
         
         

         
         Paint  maskPaintCombined = new Paint();
         maskPaintCombined.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
         canvasSaveLayer(canvas, null, maskPaintCombined);

           
           Paint  maskPaint1 = new Paint();
           
           ColorMatrix  luminanceToAlpha = new ColorMatrix(new float[] {0,       0,       0,       0, 0,
                                                                        0,       0,       0,       0, 0,
                                                                        0,       0,       0,       0, 0,
                                                                        SVGAndroidRenderer.LUMINANCE_TO_ALPHA_RED, SVGAndroidRenderer.LUMINANCE_TO_ALPHA_GREEN, SVGAndroidRenderer.LUMINANCE_TO_ALPHA_BLUE, 0, 0});
           maskPaint1.setColorFilter(new ColorMatrixColorFilter(luminanceToAlpha));
           canvasSaveLayer(canvas, null, maskPaint1);   

             
             SvgObject  ref = document.resolveIRI(state.style.mask);
             renderMask((Mask) ref, obj, originalObjBBox);

           
           canvas.restore();

           
           Paint  maskPaint2 = new Paint();
           maskPaint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
           canvasSaveLayer(canvas, null, maskPaint2);

             
             renderMask((Mask) ref, obj, originalObjBBox);

           
           canvas.restore();

         
         canvas.restore();
      }

      statePop();
   }


   private boolean requiresCompositing()
   {
      return (state.style.opacity < 1.0f) ||
             (state.style.mask != null) ||
             (state.style.isolation == Isolation.isolate) ||
             (SUPPORTS_BLEND_MODE && state.style.mixBlendMode != CSSBlendMode.normal);
   }


   @TargetApi(Build.VERSION_CODES.Q)
   private void  setBlendMode(Paint paint)
   {
      debug("Setting blend mode to "+state.style.mixBlendMode);
      switch (state.style.mixBlendMode)
      {
         case multiply:    paint.setBlendMode(BlendMode.MULTIPLY); break;
         case screen:      paint.setBlendMode(BlendMode.SCREEN); break;
         case overlay:     paint.setBlendMode(BlendMode.OVERLAY); break;
         case darken:      paint.setBlendMode(BlendMode.DARKEN); break;
         case lighten:     paint.setBlendMode(BlendMode.LIGHTEN); break;
         case color_dodge: paint.setBlendMode(BlendMode.COLOR_DODGE); break;
         case color_burn:  paint.setBlendMode(BlendMode.COLOR_BURN); break;
         case hard_light:  paint.setBlendMode(BlendMode.HARD_LIGHT); break;
         case soft_light:  paint.setBlendMode(BlendMode.SOFT_LIGHT); break;
         case difference:  paint.setBlendMode(BlendMode.DIFFERENCE); break;
         case exclusion:   paint.setBlendMode(BlendMode.EXCLUSION); break;
         case hue:         paint.setBlendMode(BlendMode.HUE); break;
         case saturation:  paint.setBlendMode(BlendMode.SATURATION); break;
         case color:       paint.setBlendMode(BlendMode.COLOR); break;
         case luminosity:  paint.setBlendMode(BlendMode.LUMINOSITY); break;
         case normal:
         default: paint.setBlendMode(null); break;
      }
   }


   


   
   private void render(Switch obj)
   {
      debug("Switch render");

      updateStyleForElement(state, obj);

      if (!display())
         return;

      if (obj.transform != null) {
         canvas.concat(obj.transform);
      }

      checkForClipPath(obj);

      boolean  compositing = pushLayer();

      renderSwitchChild(obj);

      if (compositing)
         popLayer(obj);

      updateParentBoundingBox(obj);
   }


   private void  renderSwitchChild(Switch obj)
   {
      String  deviceLanguage = Locale.getDefault().getLanguage();

      ChildLoop:
      for (SvgObject child: obj.getChildren())
      {
         
         if (!(child instanceof SvgConditional)) {
            continue;
         }
         SvgConditional  condObj = (SvgConditional) child;

         
         if (condObj.getRequiredExtensions() != null) {
            continue;
         }
         
         Set<String>  syslang = condObj.getSystemLanguage();
         if (syslang != null && (syslang.isEmpty() || !syslang.contains(deviceLanguage))) {
            continue;
         }
         
         Set<String>  reqfeat = condObj.getRequiredFeatures();
         if (reqfeat != null) {
            if (supportedFeatures == null)
               initialiseSupportedFeaturesMap();
            if (reqfeat.isEmpty() || !supportedFeatures.containsAll(reqfeat)) {
               continue;
            }
         }
         
         Set<String>  reqfmts = condObj.getRequiredFormats();
         if (reqfmts != null) {
            if (reqfmts.isEmpty() || externalFileResolver == null)
               continue;
            for (String mimeType: reqfmts) {
               if (!externalFileResolver.isFormatSupported(mimeType))
                  continue ChildLoop;
            }
         }
         
         Set<String>  reqfonts = condObj.getRequiredFonts();
         if (reqfonts != null) {
            if (reqfonts.isEmpty() || externalFileResolver == null)
               continue;
            for (String fontName: reqfonts) {
               if (externalFileResolver.resolveFont(fontName, state.style.fontWeight, String.valueOf(state.style.fontStyle), state.style.fontStretch) == null)
                  continue ChildLoop;
            }
         }
         
         
         render(child);
         break;
      }
   }


   private static synchronized void  initialiseSupportedFeaturesMap()
   {
      supportedFeatures = new HashSet<>();

      
      
      
      
      
      
      
      
      
      
      
      
      
      

      
      
      supportedFeatures.add("Structure");                   
      supportedFeatures.add("BasicStructure");              
      
      supportedFeatures.add("ConditionalProcessing");       
      supportedFeatures.add("Image");                       
      supportedFeatures.add("Style");                       
      supportedFeatures.add("ViewportAttribute");           
      supportedFeatures.add("Shape");                       
      
      supportedFeatures.add("BasicText");                   
      supportedFeatures.add("PaintAttribute");              
      supportedFeatures.add("BasicPaintAttribute");         
      supportedFeatures.add("OpacityAttribute");            
      
      supportedFeatures.add("BasicGraphicsAttribute");      
      supportedFeatures.add("Marker");                      
      
      supportedFeatures.add("Gradient");                    
      supportedFeatures.add("Pattern");                     
      supportedFeatures.add("Clip");                        
      supportedFeatures.add("BasicClip");                   
      supportedFeatures.add("Mask");                        
      
      
      
      
      
      
      
      
      
      supportedFeatures.add("View");                        
      
      
      
      
      

      
      
      
      
      
      
      
      
      
      
      
      
   }


   


   private void render(Use obj)
   {
      debug("Use render");

      if ((obj.width != null && obj.width.isZero()) ||
          (obj.height != null && obj.height.isZero()))
         return;

      updateStyleForElement(state, obj);

      if (!display())
         return;

      
      SvgObject  ref = obj.document.resolveIRI(obj.href);
      if (ref == null) {
         error("Use reference '%s' not found", obj.href);
         return;
      }

      if (obj.transform != null) {
         canvas.concat(obj.transform);
      }

      
      float _x = (obj.x != null) ? obj.x.floatValueX(this) : 0f;
      float _y = (obj.y != null) ? obj.y.floatValueY(this) : 0f;
      canvas.translate(_x, _y);

      checkForClipPath(obj);

      boolean  compositing = pushLayer();

      parentPush(obj);

      if (ref instanceof Svg)
      {
         Svg  svgElem = (Svg) ref;
         Box viewPort = makeViewPort(null, null, obj.width, obj.height);

         statePush();
         render(svgElem, viewPort);
         statePop();
      }
      else if (ref instanceof Symbol)
      {
         Length _w = (obj.width != null) ? obj.width : new Length(100, Unit.percent);
         Length _h = (obj.height != null) ? obj.height : new Length(100, Unit.percent);
         Box viewPort = makeViewPort(null, null, _w, _h);

         statePush();
         render((Symbol) ref, viewPort);
         statePop();
      }
      else
      {
         render(ref);
      }

      parentPop();

      if (compositing)
         popLayer(obj);

      updateParentBoundingBox(obj);
   }


   


   private void render(SVGBase.Path obj)
   {
      debug("Path render");

      if (obj.d == null)
         return;

      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;
      if (!state.hasStroke && !state.hasFill)
         return;

      if (obj.transform != null)
         canvas.concat(obj.transform);

      Path  path = (new PathConverter(obj.d)).getPath();

      if (obj.boundingBox == null) {
         obj.boundingBox = calculatePathBounds(path);
      }
      updateParentBoundingBox(obj);

      checkForGradientsAndPatterns(obj);
      checkForClipPath(obj);
      
      boolean  compositing = pushLayer();

      if (state.hasFill) {
         path.setFillType(getFillTypeFromState());
         doFilledPath(obj, path);
      }
      if (state.hasStroke)
         doStroke(path);

      renderMarkers(obj);

      if (compositing)
         popLayer(obj);
   }


   private Box  calculatePathBounds(Path path)
   {
      RectF  pathBounds = new RectF();
      path.computeBounds(pathBounds, true);
      return new Box(pathBounds.left, pathBounds.top, pathBounds.width(), pathBounds.height());
   }


   


   private void render(Rect obj)
   {
      debug("Rect render");

      if (obj.width == null || obj.height == null || obj.width.isZero() || obj.height.isZero())
         return;

      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;

      if (obj.transform != null)
         canvas.concat(obj.transform);

      Path  path = makePathAndBoundingBox(obj);
      updateParentBoundingBox(obj);

      checkForGradientsAndPatterns(obj);
      checkForClipPath(obj);

      boolean  compositing = pushLayer();

      if (state.hasFill)
         doFilledPath(obj, path);
      if (state.hasStroke)
         doStroke(path);


      if (compositing)
         popLayer(obj);
   }


   


   private void render(Circle obj)
   {
      debug("Circle render");

      if (obj.r == null || obj.r.isZero())
         return;

      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;

      if (obj.transform != null)
         canvas.concat(obj.transform);

      Path  path = makePathAndBoundingBox(obj);
      updateParentBoundingBox(obj);

      checkForGradientsAndPatterns(obj);
      checkForClipPath(obj);

      boolean  compositing = pushLayer();

      if (state.hasFill)
         doFilledPath(obj, path);
      if (state.hasStroke)
         doStroke(path);

      if (compositing)
         popLayer(obj);
   }


   


   private void render(Ellipse obj)
   {
      debug("Ellipse render");

      if (obj.rx == null || obj.ry == null || obj.rx.isZero() || obj.ry.isZero())
         return;

      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;

      if (obj.transform != null)
         canvas.concat(obj.transform);

      Path  path = makePathAndBoundingBox(obj);
      updateParentBoundingBox(obj);

      checkForGradientsAndPatterns(obj);
      checkForClipPath(obj);

      boolean  compositing = pushLayer();

      if (state.hasFill)
         doFilledPath(obj, path);
      if (state.hasStroke)
         doStroke(path);

      if (compositing)
         popLayer(obj);
   }


   


   private void render(Line obj)
   {
      debug("Line render");

      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;
      if (!state.hasStroke)
         return;

      if (obj.transform != null)
         canvas.concat(obj.transform);

      Path  path = makePathAndBoundingBox(obj);
      updateParentBoundingBox(obj);

      checkForGradientsAndPatterns(obj);
      checkForClipPath(obj);

      boolean  compositing = pushLayer();

      doStroke(path);

      renderMarkers(obj);

      if (compositing)
         popLayer(obj);
   }


   private List<MarkerVector>  calculateMarkerPositions(Line obj)
   {
      float _x1, _y1, _x2, _y2;
      _x1 = (obj.x1 != null) ? obj.x1.floatValueX(this) : 0f;
      _y1 = (obj.y1 != null) ? obj.y1.floatValueY(this) : 0f;
      _x2 = (obj.x2 != null) ? obj.x2.floatValueX(this) : 0f;
      _y2 = (obj.y2 != null) ? obj.y2.floatValueY(this) : 0f;

      List<MarkerVector>  markers = new ArrayList<>(2);
      markers.add(new MarkerVector(_x1, _y1, (_x2-_x1), (_y2-_y1)));
      markers.add(new MarkerVector(_x2, _y2, (_x2-_x1), (_y2-_y1)));
      return markers;
   }


   


   private void render(PolyLine obj)
   {
      debug("PolyLine render");

      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;
      if (!state.hasStroke && !state.hasFill)
         return;

      if (obj.transform != null)
         canvas.concat(obj.transform);

      int  numPoints = (obj.points != null) ? obj.points.length : 0;
      if (numPoints < 2 ||     
          numPoints % 2 == 1)  
         return;

      Path  path = makePathAndBoundingBox(obj);
      updateParentBoundingBox(obj);

      path.setFillType(getFillTypeFromState());

      checkForGradientsAndPatterns(obj);
      checkForClipPath(obj);
      
      boolean  compositing = pushLayer();

      if (state.hasFill)
         doFilledPath(obj, path);
      if (state.hasStroke)
         doStroke(path);

      renderMarkers(obj);

      if (compositing)
         popLayer(obj);
   }


   private List<MarkerVector>  calculateMarkerPositions(PolyLine obj)
   {
      int  numPoints = (obj.points != null) ? obj.points.length : 0;
      if (numPoints < 2)
         return null;

      List<MarkerVector>  markers = new ArrayList<>();
      MarkerVector        lastPos = new MarkerVector(obj.points[0], obj.points[1], 0, 0);
      float               x = 0, y = 0;

      for (int i=2; i<numPoints; i+=2) {
         x = obj.points[i];
         y = obj.points[i+1];
         lastPos.add(x, y);
         markers.add(lastPos);
         lastPos = new MarkerVector(x, y, x-lastPos.x, y-lastPos.y);
      }

      
      if (obj instanceof Polygon) {
         if (x != obj.points[0] && y != obj.points[1]) {
            x = obj.points[0];
            y = obj.points[1];
            lastPos.add(x, y);
            markers.add(lastPos);
            
            
            MarkerVector  newPos = new MarkerVector(x, y, x-lastPos.x, y-lastPos.y);
            newPos.add(markers.get(0));
            markers.add(newPos);
            markers.set(0, newPos);  
         }
      } else {
         markers.add(lastPos);
      }
      return markers;
   }


   


   private void render(Polygon obj)
   {
      debug("Polygon render");

      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;
      if (!state.hasStroke && !state.hasFill)
         return;

      if (obj.transform != null)
         canvas.concat(obj.transform);

      int  numPoints = (obj.points != null) ? obj.points.length : 0;
      if (numPoints < 2)
         return;

      Path  path = makePathAndBoundingBox(obj);
      updateParentBoundingBox(obj);

      checkForGradientsAndPatterns(obj);
      checkForClipPath(obj);
      
      boolean  compositing = pushLayer();

      if (state.hasFill)
         doFilledPath(obj, path);
      if (state.hasStroke)
         doStroke(path);

      renderMarkers(obj);

      if (compositing)
         popLayer(obj);
   }


   


   private void render(Text obj)
   {
      debug("Text render");

      updateStyleForElement(state, obj);

      if (!display())
         return;

      selectTypefaceAndFontStyling();

      if (obj.transform != null)
         canvas.concat(obj.transform);

      
      float  x = (obj.x == null || obj.x.size() == 0) ? 0f : obj.x.get(0).floatValueX(this);
      float  y = (obj.y == null || obj.y.size() == 0) ? 0f : obj.y.get(0).floatValueY(this);
      float  dx = (obj.dx == null || obj.dx.size() == 0) ? 0f : obj.dx.get(0).floatValueX(this);
      float  dy = (obj.dy == null || obj.dy.size() == 0) ? 0f : obj.dy.get(0).floatValueY(this);

      
      TextAnchor  anchor = getAnchorPosition();
      if (anchor != TextAnchor.Start) {
         float  textWidth = calculateTextWidth(obj);
         if (anchor == TextAnchor.Middle) {
            x -= (textWidth / 2);
         } else {
            x -= textWidth;  
         }
      }

      if (obj.boundingBox == null) {
         TextBoundsCalculator  proc = new TextBoundsCalculator(x, y);
         enumerateTextSpans(obj, proc);
         obj.boundingBox = new Box(proc.bbox.left, proc.bbox.top, proc.bbox.width(), proc.bbox.height());
      }
      updateParentBoundingBox(obj);

      checkForGradientsAndPatterns(obj);
      checkForClipPath(obj);
      
      boolean  compositing = pushLayer();

      enumerateTextSpans(obj, new PlainTextDrawer(x + dx, y + dy));

      if (compositing)
         popLayer(obj);
   }


   private void selectTypefaceAndFontStyling()
   {
      Typeface  font = null;

      if (state.style.fontFamily != null && document != null) {
         for (String fontName: state.style.fontFamily) {
            font = checkGenericFont(fontName, state.style.fontWeight, state.style.fontStyle);
            if (font == null && externalFileResolver != null) {
               font = externalFileResolver.resolveFont(fontName, state.style.fontWeight, String.valueOf(state.style.fontStyle), state.style.fontStretch);
            }
            if (font != null)
               break;
         }
      }
      if (font == null) {
         
         font = checkGenericFont(DEFAULT_FONT_FAMILY, state.style.fontWeight, state.style.fontStyle);
      }
      state.fillPaint.setTypeface(font);
      state.strokePaint.setTypeface(font);

      
      
      if (SUPPORTS_PAINT_FONT_VARIATION_SETTINGS) {
         state.fontVariationSet.addSetting(CSSFontVariationSettings.VARIATION_WEIGHT, state.style.fontWeight);
         if (state.style.fontStyle == FontStyle.italic) {
            state.fontVariationSet.addSetting(CSSFontVariationSettings.VARIATION_ITALIC, CSSFontVariationSettings.VARIATION_ITALIC_VALUE_ON);
            
            state.fontVariationSet.addSetting(CSSFontVariationSettings.VARIATION_OBLIQUE, CSSFontVariationSettings.VARIATION_OBLIQUE_VALUE_ON);
         }
         else if (state.style.fontStyle == FontStyle.oblique)
            state.fontVariationSet.addSetting(CSSFontVariationSettings.VARIATION_OBLIQUE, CSSFontVariationSettings.VARIATION_OBLIQUE_VALUE_ON);
         state.fontVariationSet.addSetting(CSSFontVariationSettings.VARIATION_WIDTH, state.style.fontStretch);

         String  fontVariationSettings = state.fontVariationSet.toString();
         debug("fontVariationSettings = "+fontVariationSettings);
         state.fillPaint.setFontVariationSettings(fontVariationSettings);
         state.strokePaint.setFontVariationSettings(fontVariationSettings);
      }

      if (SUPPORTS_PAINT_FONT_FEATURE_SETTINGS) {
         String  fontFeatureSettings = state.fontFeatureSet.toString();
         debug("fontFeatureSettings = "+fontFeatureSettings);
         state.fillPaint.setFontFeatureSettings(fontFeatureSettings);
         state.strokePaint.setFontFeatureSettings(fontFeatureSettings);
      }
   }


   private TextAnchor  getAnchorPosition()
   {
      if (state.style.direction == Style.TextDirection.LTR || state.style.textAnchor == TextAnchor.Middle)
         return state.style.textAnchor;

      
      return (state.style.textAnchor == TextAnchor.Start) ? TextAnchor.End : TextAnchor.Start;
   }


   private class  PlainTextDrawer extends TextProcessor
   {
      float x;
      float y;

      PlainTextDrawer(float x, float y)
      {
         this.x = x;
         this.y = y;
      }

      @Override
      public void processText(String text)
      {
         debug("TextSequence render");

         if (visible())
         {
            
            
            float letterspacingAdj = SUPPORTS_PAINT_LETTER_SPACING ? state.style.letterSpacing.floatValue(SVGAndroidRenderer.this) / 2 : 0;
            if (state.hasFill)
               canvas.drawText(text, x - letterspacingAdj, y, state.fillPaint);
            if (state.hasStroke)
               canvas.drawText(text, x - letterspacingAdj, y, state.strokePaint);
         }

         
         x += measureText(text, state.fillPaint);
      }
   }


   
   


   private static abstract class  TextProcessor
   {
      public boolean  doTextContainer(TextContainer obj)
      {
         return true;
      }

      public abstract void  processText(String text);
   }


   
   private void enumerateTextSpans(TextContainer obj, TextProcessor textprocessor)
   {
      if (!display())
         return;

      Iterator<SvgObject>  iter = obj.children.iterator();
      boolean              isFirstChild = true;

      while (iter.hasNext())
      {
         SvgObject  child = iter.next();

         if (child instanceof TextSequence) {
            textprocessor.processText(textXMLSpaceTransform(((TextSequence) child).text, isFirstChild, !iter.hasNext() ));
         } else {
            processTextChild(child, textprocessor);
         }
         isFirstChild = false;
      }
   }


   private void  processTextChild(SvgObject obj, TextProcessor textprocessor)
   {
      
      if (!textprocessor.doTextContainer((TextContainer) obj))
         return;

      if (obj instanceof TextPath)
      {
         
         statePush();

         renderTextPath((TextPath) obj);

         
         statePop();
      }
      else if (obj instanceof TSpan)
      {
         debug("TSpan render");

         
         statePush();

         TSpan tspan = (TSpan) obj; 

         updateStyleForElement(state, tspan);

         if (display())
         {
            selectTypefaceAndFontStyling();

            
            float    x=0, y=0, dx=0, dy=0;
            boolean  specifiedX = (tspan.x != null && tspan.x.size() > 0);
            if (textprocessor instanceof PlainTextDrawer) {
               x = !specifiedX ? ((PlainTextDrawer) textprocessor).x : tspan.x.get(0).floatValueX(this);
               y = (tspan.y == null || tspan.y.size() == 0) ? ((PlainTextDrawer) textprocessor).y : tspan.y.get(0).floatValueY(this);
               dx = (tspan.dx == null || tspan.dx.size() == 0) ? 0f : tspan.dx.get(0).floatValueX(this);
               dy = (tspan.dy == null || tspan.dy.size() == 0) ? 0f : tspan.dy.get(0).floatValueY(this);
            }

            
            if (specifiedX) {
              TextAnchor  anchor = getAnchorPosition();
              if (anchor != TextAnchor.Start) {
                 float  textWidth = calculateTextWidth(tspan);
                 if (anchor == TextAnchor.Middle) {
                    x -= (textWidth / 2);
                 } else {
                    x -= textWidth;  
                 }
               }
            }

            checkForGradientsAndPatterns((SvgElement) tspan.getTextRoot());

            if (textprocessor instanceof PlainTextDrawer) {
               ((PlainTextDrawer) textprocessor).x = x + dx;
               ((PlainTextDrawer) textprocessor).y = y + dy;
            }

            boolean  compositing = pushLayer();

            enumerateTextSpans(tspan, textprocessor);

            if (compositing)
               popLayer(tspan);
         }

         
         statePop();
      }
      else if  (obj instanceof TRef)
      {
         
         statePush();

         TRef tref = (TRef) obj; 

         updateStyleForElement(state, tref);

         if (display())
         {
            checkForGradientsAndPatterns((SvgElement) tref.getTextRoot());

            
            SvgObject  ref = obj.document.resolveIRI(tref.href);
            if (ref instanceof TextContainer)
            {
               StringBuilder  str = new StringBuilder();
               extractRawText((TextContainer) ref, str);
               if (str.length() > 0) {
                  textprocessor.processText(str.toString());
               }
            }
            else
            {
               error("Tref reference '%s' not found", tref.href);
            }
         }

         
         statePop();
      }
   }


   


   private void renderTextPath(TextPath obj)
   {
      debug("TextPath render");

      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;

      selectTypefaceAndFontStyling();

      SvgObject  ref = obj.document.resolveIRI(obj.href);
      if (ref == null)
      {
         error("TextPath reference '%s' not found", obj.href);
         return;
      }

      SVGBase.Path  pathObj = (SVGBase.Path) ref;
      Path          path = (new PathConverter(pathObj.d)).getPath();

      if (pathObj.transform != null)
         path.transform(pathObj.transform);

      PathMeasure  measure = new PathMeasure(path, false);

      float  startOffset = (obj.startOffset != null) ? obj.startOffset.floatValue(this, measure.getLength()) : 0f;

      
      TextAnchor  anchor = getAnchorPosition();
      if (anchor != TextAnchor.Start) {
         float  textWidth = calculateTextWidth(obj);
         if (anchor == TextAnchor.Middle) {
            startOffset -= (textWidth / 2);
         } else {
            startOffset -= textWidth;  
         }
      }

      checkForGradientsAndPatterns((SvgElement) obj.getTextRoot());
      
      boolean  compositing = pushLayer();

      enumerateTextSpans(obj, new PathTextDrawer(path, startOffset, 0f));

      if (compositing)
         popLayer(obj);
   }


   private class  PathTextDrawer extends PlainTextDrawer
   {
      private final Path  path;

      PathTextDrawer(Path path, float x, float y)
      {
         super(x, y);
         this.path = path;
      }

      @Override
      public void processText(String text)
      {
         if (visible())
         {
            
            
            float letterspacingAdj = SUPPORTS_PAINT_LETTER_SPACING ? state.style.letterSpacing.floatValue(SVGAndroidRenderer.this) / 2 : 0;
            if (state.hasFill)
               canvas.drawTextOnPath(text, path, x - letterspacingAdj, y, state.fillPaint);
            if (state.hasStroke)
               canvas.drawTextOnPath(text, path, x - letterspacingAdj, y, state.strokePaint);
         }

         
         x += measureText(text, state.fillPaint);
      }
   }


   


   
   private float  calculateTextWidth(TextContainer parentTextObj)
   {
      TextWidthCalculator  proc = new TextWidthCalculator();
      enumerateTextSpans(parentTextObj, proc);
      return proc.x;
   }

   private class  TextWidthCalculator extends TextProcessor
   {
      float x = 0;

      @Override
      public void processText(String text)
      {
         x += measureText(text, state.fillPaint);
      }
   }


   
   private float  measureText(String text, Paint paint)
   {
      float[] widths = new float[text.length()];
      paint.getTextWidths(text, widths);
      float total = 0;
      for (int i=0; i < widths.length; i++) {
         total += widths[i];
      }
      return total;
   }


   


   
   private class  TextBoundsCalculator extends TextProcessor
   {
      float  x;
      float  y;
      final RectF  bbox = new RectF();

      TextBoundsCalculator(float x, float y)
      {
         this.x = x;
         this.y = y;
      }

      @Override
      public boolean doTextContainer(TextContainer obj)
      {
         if (obj instanceof TextPath)
         {
            
            
            TextPath  tpath = (TextPath) obj;
            SvgObject  ref = obj.document.resolveIRI(tpath.href);
            if (ref == null) {
               error("TextPath path reference '%s' not found", tpath.href);
               return false;
            }
            SVGBase.Path  pathObj = (SVGBase.Path) ref;
            Path          path = (new PathConverter(pathObj.d)).getPath();
            if (pathObj.transform != null)
               path.transform(pathObj.transform);
            RectF     pathBounds = new RectF();
            path.computeBounds(pathBounds, true);
            bbox.union(pathBounds);
            return false;
         }
         return true;
      }

      @Override
      public void processText(String text)
      {
         if (visible())
         {
            android.graphics.Rect  rect = new android.graphics.Rect();
            
            state.fillPaint.getTextBounds(text, 0, text.length(), rect);
            RectF  textbounds = new RectF(rect);
            
            textbounds.offset(x, y);
            
            bbox.union(textbounds);
         }

         
         x += measureText(text, state.fillPaint);
      }
   }


   
   private void  extractRawText(TextContainer parent, StringBuilder str)
   {
      Iterator<SvgObject>  iter = parent.children.iterator();
      boolean              isFirstChild = true;

      while (iter.hasNext())
      {
         SvgObject  child = iter.next();

         if (child instanceof TextContainer) {
            extractRawText((TextContainer) child, str);
         } else if (child instanceof TextSequence) {
            str.append(textXMLSpaceTransform(((TextSequence) child).text, isFirstChild, !iter.hasNext() ));
         }
         isFirstChild = false;
      }
   }
 

   

   
   private String  textXMLSpaceTransform(String text, boolean isFirstChild, boolean isLastChild)
   {
      if (state.spacePreserve)  
         return PATTERN_TABS_OR_LINE_BREAKS.matcher(text).replaceAll(" ");

      
      text = PATTERN_TABS.matcher(text).replaceAll("");
      text = PATTERN_LINE_BREAKS.matcher(text).replaceAll(" ");
      
      if (isFirstChild)
         text = PATTERN_START_SPACES.matcher(text).replaceAll("");
      if (isLastChild)
         text = PATTERN_END_SPACES.matcher(text).replaceAll("");
      return PATTERN_DOUBLE_SPACES.matcher(text).replaceAll(" ");
   }


   


   private void render(Symbol obj, Box viewPort)
   {
      debug("Symbol render");

      if (viewPort.width == 0f || viewPort.height == 0f)
         return;

      
      PreserveAspectRatio  positioning = (obj.preserveAspectRatio != null) ? obj.preserveAspectRatio : PreserveAspectRatio.LETTERBOX;

      updateStyleForElement(state, obj);

      state.viewPort = viewPort;

      if (!state.style.overflow) {
         setClipRect(state.viewPort.minX, state.viewPort.minY, state.viewPort.width, state.viewPort.height);
      }

      if (obj.viewBox != null) {
         canvas.concat(calculateViewBoxTransform(state.viewPort, obj.viewBox, positioning));
         state.viewBox = obj.viewBox;
      } else {
         canvas.translate(state.viewPort.minX, state.viewPort.minY);
         state.viewBox = null;
      }
      
      boolean  compositing = pushLayer();

      renderChildren(obj, true);

      if (compositing)
         popLayer(obj);

      updateParentBoundingBox(obj);
   }


   


   private void render(Image obj)
   {
      debug("Image render");

      if (obj.width == null || obj.width.isZero() ||
          obj.height == null || obj.height.isZero())
         return;

      if (obj.href == null)
         return;

      
      PreserveAspectRatio  positioning = (obj.preserveAspectRatio != null) ? obj.preserveAspectRatio : PreserveAspectRatio.LETTERBOX;

      
      Bitmap  image = checkForImageDataURL(obj.href);
      if (image == null)
      {
         if (externalFileResolver == null)
            return;

         image = externalFileResolver.resolveImage(obj.href);
      }
      if (image == null) {
         error("Could not locate image '%s'", obj.href);
         return;
      }
      Box  imageNaturalSize = new Box(0,  0,  image.getWidth(), image.getHeight());

      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;

      if (obj.transform != null) {
         canvas.concat(obj.transform);
      }

      float  _x = (obj.x != null) ? obj.x.floatValueX(this) : 0f;
      float  _y = (obj.y != null) ? obj.y.floatValueY(this) : 0f;
      float  _w = obj.width.floatValueX(this);
      float  _h = obj.height.floatValueX(this);
      state.viewPort = new Box(_x, _y, _w, _h);

      if (!state.style.overflow) {
         setClipRect(state.viewPort.minX, state.viewPort.minY, state.viewPort.width, state.viewPort.height);
      }

      obj.boundingBox = state.viewPort;
      updateParentBoundingBox(obj);

      checkForClipPath(obj);

      boolean  compositing = pushLayer();

      viewportFill();

      canvas.save();

      
      canvas.concat(calculateViewBoxTransform(state.viewPort, imageNaturalSize, positioning));

      Paint  bmPaint = new Paint((state.style.imageRendering == RenderQuality.optimizeSpeed) ? 0 : Paint.FILTER_BITMAP_FLAG);
      canvas.drawBitmap(image, 0, 0, bmPaint);

      canvas.restore();

      if (compositing)
         popLayer(obj);
   }


   


   
   private Bitmap  checkForImageDataURL(String url)
   {
      if (!url.startsWith("data:"))
         return null;
      if (url.length() < 14)
         return null;

      int  comma = url.indexOf(',');
      if (comma < 12) 
         return null;
      if (!";base64".equals(url.substring(comma-7, comma)))
         return null;
      try {
         byte[]  imageData = Base64.decode(url.substring(comma+1), Base64.DEFAULT);  
         return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
      } catch (Exception e) {
         Log.e(TAG, "Could not decode bad Data URL", e);
         return null;
      }
   }


   private boolean  display()
   {
      if (state.style.display != null)
        return state.style.display;
      return true;
   }


   private boolean  visible()
   {
      if (state.style.visibility != null)
        return state.style.visibility;
      return true;
   }


   
   private Matrix calculateViewBoxTransform(Box viewPort, Box viewBox, PreserveAspectRatio positioning)
   {
      Matrix m = new Matrix();

      if (positioning == null || positioning.getAlignment() == null)
         return m;

      float  xScale = viewPort.width / viewBox.width;
      float  yScale = viewPort.height / viewBox.height;
      float  xOffset = -viewBox.minX;
      float  yOffset = -viewBox.minY;

      
      if (positioning.equals(PreserveAspectRatio.STRETCH))
      {
         m.preTranslate(viewPort.minX, viewPort.minY);
         m.preScale(xScale, yScale);
         m.preTranslate(xOffset, yOffset);
         return m;
      }

      
      
      float  scale = (positioning.getScale() == PreserveAspectRatio.Scale.slice) ? Math.max(xScale,  yScale) : Math.min(xScale,  yScale);
      
      float  imageW = viewPort.width / scale;
      float  imageH = viewPort.height / scale;
      
      switch (positioning.getAlignment())
      {
         case xMidYMin:
         case xMidYMid:
         case xMidYMax:
            xOffset -= (viewBox.width - imageW) / 2;
            break;
         case xMaxYMin:
         case xMaxYMid:
         case xMaxYMax:
            xOffset -= (viewBox.width - imageW);
            break;
         default:
            
            break;
      }
      
      switch (positioning.getAlignment())
      {
         case xMinYMid:
         case xMidYMid:
         case xMaxYMid:
            yOffset -= (viewBox.height - imageH) / 2;
            break;
         case xMinYMax:
         case xMidYMax:
         case xMaxYMax:
            yOffset -= (viewBox.height - imageH);
            break;
         default:
            
            break;
      }

      m.preTranslate(viewPort.minX, viewPort.minY);
      m.preScale(scale, scale);
      m.preTranslate(xOffset, yOffset);
      return m;
   }


   private boolean  isSpecified(Style style, long flag)
   {
      return (style.specifiedFlags & flag) != 0;
   }


   
   private void  updateStyle(RendererState state, Style style)
   {
      
      if (isSpecified(style, Style.SPECIFIED_COLOR))
      {
         state.style.color = style.color;
      }

      if (isSpecified(style, Style.SPECIFIED_OPACITY))
      {
         state.style.opacity = style.opacity;
      }

      if (isSpecified(style, Style.SPECIFIED_FILL))
      {
         state.style.fill = style.fill;
         state.hasFill = (style.fill != null && style.fill != Colour.TRANSPARENT);
      }

      if (isSpecified(style, Style.SPECIFIED_FILL_OPACITY))
      {
         state.style.fillOpacity = style.fillOpacity;
      }

      
      if (isSpecified(style, Style.SPECIFIED_FILL | Style.SPECIFIED_FILL_OPACITY | Style.SPECIFIED_COLOR | Style.SPECIFIED_OPACITY))
      {
         setPaintColour(state, true, state.style.fill);
      }

      if (isSpecified(style, Style.SPECIFIED_FILL_RULE))
      {
         state.style.fillRule = style.fillRule;
      }


      if (isSpecified(style, Style.SPECIFIED_STROKE))
      {
         state.style.stroke = style.stroke;
         state.hasStroke = (style.stroke != null && style.stroke != Colour.TRANSPARENT);
      }

      if (isSpecified(style, Style.SPECIFIED_STROKE_OPACITY))
      {
         state.style.strokeOpacity = style.strokeOpacity;
      }

      if (isSpecified(style, Style.SPECIFIED_STROKE | Style.SPECIFIED_STROKE_OPACITY | Style.SPECIFIED_COLOR | Style.SPECIFIED_OPACITY))
      {
         setPaintColour(state, false, state.style.stroke);
      }

      if (isSpecified(style, Style.SPECIFIED_VECTOR_EFFECT))
      {
         state.style.vectorEffect = style.vectorEffect;
      }

      if (isSpecified(style, Style.SPECIFIED_STROKE_WIDTH))
      {
         state.style.strokeWidth = style.strokeWidth;
         state.strokePaint.setStrokeWidth(state.style.strokeWidth.floatValue(this));
      }

      if (isSpecified(style, Style.SPECIFIED_STROKE_LINECAP))
      {
         state.style.strokeLineCap = style.strokeLineCap;
         switch (style.strokeLineCap)
         {
            case Butt:
               state.strokePaint.setStrokeCap(Paint.Cap.BUTT);
               break;
            case Round:
               state.strokePaint.setStrokeCap(Paint.Cap.ROUND);
               break;
            case Square:
               state.strokePaint.setStrokeCap(Paint.Cap.SQUARE);
               break;
            default:
               break;
         }
      }

      if (isSpecified(style, Style.SPECIFIED_STROKE_LINEJOIN))
      {
         state.style.strokeLineJoin = style.strokeLineJoin;
         switch (style.strokeLineJoin)
         {
            case Miter:
               state.strokePaint.setStrokeJoin(Paint.Join.MITER);
               break;
            case Round:
               state.strokePaint.setStrokeJoin(Paint.Join.ROUND);
               break;
            case Bevel:
               state.strokePaint.setStrokeJoin(Paint.Join.BEVEL);
               break;
            default:
               break;
         }
      }

      if (isSpecified(style, Style.SPECIFIED_STROKE_MITERLIMIT))
      {
         
         state.style.strokeMiterLimit = style.strokeMiterLimit;
         state.strokePaint.setStrokeMiter(style.strokeMiterLimit);
      }

      if (isSpecified(style, Style.SPECIFIED_STROKE_DASHARRAY))
      {
         state.style.strokeDashArray = style.strokeDashArray;
      }

      if (isSpecified(style, Style.SPECIFIED_STROKE_DASHOFFSET))
      {
         state.style.strokeDashOffset = style.strokeDashOffset;
      }

      if (isSpecified(style, Style.SPECIFIED_STROKE_DASHARRAY | Style.SPECIFIED_STROKE_DASHOFFSET))
      {
         
         if (state.style.strokeDashArray == null)
         {
            state.strokePaint.setPathEffect(null);
         }
         else
         {
            float  intervalSum = 0f;
            int    n = state.style.strokeDashArray.length;
            
            
            int    arrayLen = (n % 2==0) ? n : n*2;
            float[] intervals = new float[arrayLen];
            for (int i=0; i<arrayLen; i++) {
               intervals[i] = state.style.strokeDashArray[i % n].floatValue(this);
               intervalSum += intervals[i];
            }
            if (intervalSum == 0f) {
               state.strokePaint.setPathEffect(null);
            } else {
               float offset = state.style.strokeDashOffset.floatValue(this);
               if (offset < 0) {
                  
                  
                  offset = intervalSum + (offset % intervalSum);
               }
               state.strokePaint.setPathEffect( new DashPathEffect(intervals, offset) );
            }
         }
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_SIZE))
      {
         float  currentFontSize = getCurrentFontSize();
         state.style.fontSize = style.fontSize;
         state.fillPaint.setTextSize(style.fontSize.floatValue(this, currentFontSize));
         state.strokePaint.setTextSize(style.fontSize.floatValue(this, currentFontSize));
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_FAMILY))
      {
         state.style.fontFamily = style.fontFamily;
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_WEIGHT))
      {
         
         
         if (style.fontWeight == Style.FONT_WEIGHT_LIGHTER)
         {
            float fw = state.style.fontWeight;
            if (fw >= 100f && fw < 550f)
               state.style.fontWeight = 100f;
            else if (fw >= 550f && fw < 750f)
               state.style.fontWeight = 400f;
            else if (fw >= 750f)
               state.style.fontWeight = 700f;
         }
         else if (style.fontWeight == Style.FONT_WEIGHT_BOLDER)
         {
            float fw = state.style.fontWeight;
            if (fw < 350f)
               state.style.fontWeight = 400f;
            else if (fw >= 350f && fw < 550f)
               state.style.fontWeight = 700f;
            else if (fw >= 550f && fw < 900f)
               state.style.fontWeight = 900f;
         }
         else
            state.style.fontWeight = style.fontWeight;
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_STYLE))
      {
         state.style.fontStyle = style.fontStyle;
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_STRETCH))
      {
         
         state.style.fontStretch = style.fontStretch;
      }

      if (isSpecified(style, Style.SPECIFIED_TEXT_DECORATION))
      {
         state.style.textDecoration = style.textDecoration;
         state.fillPaint.setStrikeThruText(style.textDecoration == TextDecoration.LineThrough);
         state.fillPaint.setUnderlineText(style.textDecoration == TextDecoration.Underline);
         
         
         if (SUPPORTS_STROKED_UNDERLINES) {
            state.strokePaint.setStrikeThruText(style.textDecoration == TextDecoration.LineThrough);
            state.strokePaint.setUnderlineText(style.textDecoration == TextDecoration.Underline);
         }
      }

      if (isSpecified(style, Style.SPECIFIED_DIRECTION))
      {
         state.style.direction = style.direction;
      }

      if (isSpecified(style, Style.SPECIFIED_TEXT_ANCHOR))
      {
         state.style.textAnchor = style.textAnchor;
      }

      if (isSpecified(style, Style.SPECIFIED_OVERFLOW))
      {
         state.style.overflow = style.overflow;
      }

      if (isSpecified(style, Style.SPECIFIED_MARKER_START))
      {
         state.style.markerStart = style.markerStart;
      }

      if (isSpecified(style, Style.SPECIFIED_MARKER_MID))
      {
         state.style.markerMid = style.markerMid;
      }

      if (isSpecified(style, Style.SPECIFIED_MARKER_END))
      {
         state.style.markerEnd = style.markerEnd;
      }

      if (isSpecified(style, Style.SPECIFIED_DISPLAY))
      {
         state.style.display = style.display;
      }

      if (isSpecified(style, Style.SPECIFIED_VISIBILITY))
      {
         state.style.visibility = style.visibility;
      }

      if (isSpecified(style, Style.SPECIFIED_CLIP))
      {
         state.style.clip = style.clip;
      }

      if (isSpecified(style, Style.SPECIFIED_CLIP_PATH))
      {
         state.style.clipPath = style.clipPath;
      }

      if (isSpecified(style, Style.SPECIFIED_CLIP_RULE))
      {
         state.style.clipRule = style.clipRule;
      }

      if (isSpecified(style, Style.SPECIFIED_MASK))
      {
         state.style.mask = style.mask;
      }

      if (isSpecified(style, Style.SPECIFIED_STOP_COLOR))
      {
         state.style.stopColor = style.stopColor;
      }

      if (isSpecified(style, Style.SPECIFIED_STOP_OPACITY))
      {
         state.style.stopOpacity = style.stopOpacity;
      }

      if (isSpecified(style, Style.SPECIFIED_VIEWPORT_FILL))
      {
         state.style.viewportFill = style.viewportFill;
      }

      if (isSpecified(style, Style.SPECIFIED_VIEWPORT_FILL_OPACITY))
      {
         state.style.viewportFillOpacity = style.viewportFillOpacity;
      }

      if (isSpecified(style, Style.SPECIFIED_IMAGE_RENDERING))
      {
         state.style.imageRendering = style.imageRendering;
      }

      if (isSpecified(style, Style.SPECIFIED_ISOLATION))
      {
         state.style.isolation = style.isolation;
      }

      if (isSpecified(style, Style.SPECIFIED_MIX_BLEND_MODE))
      {
         state.style.mixBlendMode = style.mixBlendMode;
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_KERNING))
      {
         state.style.fontKerning = style.fontKerning;
         state.fontFeatureSet.applyKerning(style.fontKerning);
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_FEATURE_SETTINGS))
      {
         state.style.fontFeatureSettings = style.fontFeatureSettings;
         state.fontFeatureSet.applySettings(style.fontFeatureSettings);
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_VARIANT_LIGATURES))
      {
         state.style.fontVariantLigatures = style.fontVariantLigatures;
         state.fontFeatureSet.applySettings(style.fontVariantLigatures);
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_VARIANT_POSITION))
      {
         state.style.fontVariantPosition = style.fontVariantPosition;
         state.fontFeatureSet.applySettings(style.fontVariantPosition);
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_VARIANT_CAPS))
      {
         state.style.fontVariantCaps = style.fontVariantCaps;
         state.fontFeatureSet.applySettings(style.fontVariantCaps);
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_VARIANT_NUMERIC))
      {
         state.style.fontVariantNumeric = style.fontVariantNumeric;
         state.fontFeatureSet.applySettings(style.fontVariantNumeric);
      }

      if (isSpecified(style, Style.SPECIFIED_FONT_VARIANT_EAST_ASIAN))
      {
         state.style.fontVariantEastAsian = style.fontVariantEastAsian;
         state.fontFeatureSet.applySettings(style.fontVariantEastAsian);
      }

      if (SUPPORTS_PAINT_FONT_VARIATION_SETTINGS && isSpecified(style, Style.SPECIFIED_FONT_VARIATION_SETTINGS))
      {
         state.style.fontVariationSettings = style.fontVariationSettings;
         state.fontVariationSet.applySettings(style.fontVariationSettings);
      }

      if (isSpecified(style, Style.SPECIFIED_WRITING_MODE))
      {
         state.style.writingMode = style.writingMode;
      }

      if (isSpecified(style, Style.SPECIFIED_GLYPH_ORIENTATION_VERTICAL))
      {
         state.style.glyphOrientationVertical = style.glyphOrientationVertical;
      }

      if (isSpecified(style, Style.SPECIFIED_TEXT_ORIENTATION))
      {
         state.style.textOrientation = style.textOrientation;
      }

      if (isSpecified(style, Style.SPECIFIED_LETTER_SPACING))
      {
         state.style.letterSpacing = style.letterSpacing;
         if (SUPPORTS_PAINT_LETTER_SPACING) {
            
            state.fillPaint.setLetterSpacing(style.letterSpacing.floatValue(this) / getCurrentFontSize());
            state.strokePaint.setLetterSpacing(style.letterSpacing.floatValue(this) / getCurrentFontSize());
         }
      }

      if (isSpecified(style, Style.SPECIFIED_WORD_SPACING))
      {
         state.style.wordSpacing = style.wordSpacing;
         if (SUPPORTS_PAINT_WORD_SPACING) {
            state.fillPaint.setWordSpacing(style.wordSpacing.floatValue(this));
            state.strokePaint.setWordSpacing(style.wordSpacing.floatValue(this));
         }
      }

   }


   private void  setPaintColour(RendererState state, boolean isFill, SvgPaint paint)
   {
      float  paintOpacity = (isFill) ? state.style.fillOpacity : state.style.strokeOpacity;
      int    col;
      if (paint instanceof Colour) {
         col = ((Colour) paint).colour;
      } else if (paint instanceof CurrentColor) {
         col = state.style.color.colour;
      } else {
         return;
      }
      col = colourWithOpacity(col, paintOpacity);
      if (isFill)
         state.fillPaint.setColor(col);
      else
         state.strokePaint.setColor(col);
   }


   private Typeface  checkGenericFont(String fontName, Float fontWeight, FontStyle fontStyle)
   {
      Typeface font = null;
      int      typefaceStyle;

      boolean  italic = (fontStyle == FontStyle.italic);
      typefaceStyle = (fontWeight >= Style.FONT_WEIGHT_BOLD) ? (italic ? Typeface.BOLD_ITALIC : Typeface.BOLD)
                                                             : (italic ? Typeface.ITALIC : Typeface.NORMAL);

      switch (fontName) {
         case "serif":
            font = Typeface.create(Typeface.SERIF, typefaceStyle); break;
         case "sans-serif":
         case "cursive":
         case "fantasy":
            font = Typeface.create(Typeface.SANS_SERIF, typefaceStyle); break;
         case "monospace":
            font = Typeface.create(Typeface.MONOSPACE, typefaceStyle); break;
      }
      return font;
   }


   
   private static int  clamp255(float val)
   {
      int  i = (int)(val * 256f);
      return (i<0) ? 0 : Math.min(i, 255);
   }


   private static int  colourWithOpacity(int colour, float opacity)
   {
      int  alpha = (colour >> 24) & 0xff;
      alpha = Math.round(alpha * opacity);
      alpha = (alpha<0) ? 0 : Math.min(alpha, 255);
      return (alpha << 24) | (colour & 0xffffff);
   }


   private Path.FillType  getFillTypeFromState()
   {
      if (state.style.fillRule != null && state.style.fillRule == Style.FillRule.EvenOdd)
         return Path.FillType.EVEN_ODD;
      else
         return Path.FillType.WINDING;
   }


   private void  setClipRect(float minX, float minY, float width, float height)
   {
      float  left = minX;
      float  top = minY;
      float  right = minX + width;
      float  bottom = minY + height;

      if (state.style.clip != null) {
         left += state.style.clip.left.floatValueX(this);
         top += state.style.clip.top.floatValueY(this);
         right -= state.style.clip.right.floatValueX(this);
         bottom -= state.style.clip.bottom.floatValueY(this);
      }

      canvas.clipRect(left, top, right, bottom);
   }


   
   private void  viewportFill()
   {
      int    col;
      if (state.style.viewportFill instanceof Colour) {
         col = ((Colour) state.style.viewportFill).colour;
      } else if (state.style.viewportFill instanceof CurrentColor) {
         col = state.style.color.colour;
      } else {
         return;
      }
      if (state.style.viewportFillOpacity != null)
         col = colourWithOpacity(col, state.style.viewportFillOpacity);

      canvas.drawColor(col);
   }


   

   
   protected static class  PathConverter implements PathInterface
   {
      final Path   path = new Path();
      float  lastX, lastY;
      
      PathConverter(PathDefinition pathDef)
      {
         if (pathDef == null)
            return;
         pathDef.enumeratePath(this);
      }

      Path  getPath()
      {
         return path;
      }

      @Override
      public void moveTo(float x, float y)
      {
         path.moveTo(x, y);
         lastX = x;
         lastY = y;
      }

      @Override
      public void lineTo(float x, float y)
      {
         path.lineTo(x, y);
         lastX = x;
         lastY = y;
      }

      @Override
      public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3)
      {
         path.cubicTo(x1, y1, x2, y2, x3, y3);
         lastX = x3;
         lastY = y3;
      }

      @Override
      public void quadTo(float x1, float y1, float x2, float y2)
      {
         path.quadTo(x1, y1, x2, y2);
         lastX = x2;
         lastY = y2;
      }

      @Override
      public void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y)
      {
         SVGAndroidRenderer.arcTo(lastX, lastY, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y, this);
         lastX = x;
         lastY = y;
      }

      @Override
      public void close()
      {
         path.close();
      }
         
   }


   
   

   

   private static void arcTo(float lastX, float lastY, float rx, float ry, float angle, boolean largeArcFlag, boolean sweepFlag, float x, float y, PathInterface pather)
   {
      if (lastX == x && lastY == y) {
         
         
         
         return;
      }

      
      if (rx == 0 || ry == 0) {
         pather.lineTo(x, y);
         return;
      }

      
      rx = Math.abs(rx);
      ry = Math.abs(ry);

      
      double angleRad = Math.toRadians(angle % 360.0);
      double cosAngle = Math.cos(angleRad);
      double sinAngle = Math.sin(angleRad);

      
      
      

      
      double dx2 = (lastX - x) / 2.0;
      double dy2 = (lastY - y) / 2.0;

      
      
      double x1 = (cosAngle * dx2 + sinAngle * dy2);
      double y1 = (-sinAngle * dx2 + cosAngle * dy2);

      double rx_sq = rx * rx;
      double ry_sq = ry * ry;
      double x1_sq = x1 * x1;
      double y1_sq = y1 * y1;

      
      
      
      double radiiCheck = x1_sq / rx_sq + y1_sq / ry_sq;
      if (radiiCheck > 0.99999) {
         double radiiScale = Math.sqrt(radiiCheck) * 1.00001;
         rx = (float) (radiiScale * rx);
         ry = (float) (radiiScale * ry);
         rx_sq = rx * rx;
         ry_sq = ry * ry;
      }

      
      double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
      double sq = ((rx_sq * ry_sq) - (rx_sq * y1_sq) - (ry_sq * x1_sq)) / ((rx_sq * y1_sq) + (ry_sq * x1_sq));
      sq = (sq < 0) ? 0 : sq;
      double coef = (sign * Math.sqrt(sq));
      double cx1 = coef * ((rx * y1) / ry);
      double cy1 = coef * -((ry * x1) / rx);

      
      double sx2 = (lastX + x) / 2.0;
      double sy2 = (lastY + y) / 2.0;
      double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
      double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

      
      double ux = (x1 - cx1) / rx;
      double uy = (y1 - cy1) / ry;
      double vx = (-x1 - cx1) / rx;
      double vy = (-y1 - cy1) / ry;
      double p, n;

      
      

      final double  TWO_PI = Math.PI * 2.0;

      
      
      n = Math.sqrt((ux * ux) + (uy * uy));  
      p = ux;                                
      sign = (uy < 0) ? -1.0 : 1.0;          
      double angleStart = sign * Math.acos(p / n);  

      
      n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
      p = ux * vx + uy * vy;
      sign = (ux * vy - uy * vx < 0) ? -1.0f : 1.0f;
      double angleExtent = sign * checkedArcCos(p / n);

      
      if (angleExtent == 0f) {
         pather.lineTo(x, y);
         return;
      }

      if (!sweepFlag && angleExtent > 0) {
         angleExtent -= TWO_PI;
      } else if (sweepFlag && angleExtent < 0) {
         angleExtent += TWO_PI;
      }
      angleExtent %= TWO_PI;
      angleStart %= TWO_PI;

      
      
      
      
      float[]  bezierPoints = arcToBeziers(angleStart, angleExtent);

      
      Matrix m = new Matrix();
      m.postScale(rx, ry);
      m.postRotate(angle);
      m.postTranslate((float) cx, (float) cy);
      m.mapPoints(bezierPoints);

      
      
      
      
      bezierPoints[bezierPoints.length-2] = x;
      bezierPoints[bezierPoints.length-1] = y;

      
      for (int i=0; i<bezierPoints.length; i+=6)
      {
         pather.cubicTo(bezierPoints[i], bezierPoints[i+1], bezierPoints[i+2], bezierPoints[i+3], bezierPoints[i+4], bezierPoints[i+5]);
      }
   }


   
   
   private static double  checkedArcCos(double val)
   {
      return (val < -1.0) ? Math.PI : (val > 1.0) ? 0 : Math.acos(val);
   }


   
   private static float[]  arcToBeziers(double angleStart, double angleExtent)
   {
      int    numSegments = (int) Math.ceil(Math.abs(angleExtent) * 2.0 / Math.PI);  

      double  angleIncrement = angleExtent / numSegments;
      
      
      double  controlLength = 4.0 / 3.0 * Math.sin(angleIncrement / 2.0) / (1.0 + Math.cos(angleIncrement / 2.0));
      
      float[] coords = new float[numSegments * 6];
      int     pos = 0;

      for (int i=0; i<numSegments; i++)
      {
         double  angle = angleStart + i * angleIncrement;
         
         double  dx = Math.cos(angle);
         double  dy = Math.sin(angle);
         
         coords[pos++]   = (float) (dx - controlLength * dy);
         coords[pos++] = (float) (dy + controlLength * dx);
         
         angle += angleIncrement;
         dx = Math.cos(angle);
         dy = Math.sin(angle);
         coords[pos++] = (float) (dx + controlLength * dy);
         coords[pos++] = (float) (dy - controlLength * dx);
         
         coords[pos++] = (float) dx;
         coords[pos++] = (float) dy;
      }
      return coords;
   }


   
   
   


   private static class MarkerVector
   {
      final float  x, y;
      float        dx = 0f, dy = 0f;
      boolean      isAmbiguous = false;

      MarkerVector(float x, float y, float dx, float dy)
      {
         this.x = x;
         this.y = y;
         
         double  len = Math.sqrt( dx*dx + dy*dy );
         if (len != 0) {
            this.dx = (float) (dx / len);
            this.dy = (float) (dy / len);
         }
      }

      void add(float x, float y)
      {
         
         
         
         float dx = (x - this.x);
         float dy = (y - this.y);
         double  len = Math.sqrt( dx*dx + dy*dy );
         if (len != 0) {
            dx = (float) (dx / len);
            dy = (float) (dy / len);
         }
         
         if (dx == -this.dx && dy == -this.dy) {
            this.isAmbiguous = true;
            
            this.dx = -dy;
            this.dy = dx;
         } else {
            this.dx += dx;
            this.dy += dy;
         }
      }

      void add(MarkerVector v2)
      {
         
         if (v2.dx == -this.dx && v2.dy == -this.dy) {
            this.isAmbiguous = true;
            
            this.dx = -v2.dy;
            this.dy = v2.dx;
         } else {
            this.dx += v2.dx;
            this.dy += v2.dy;
         }
      }


      @Override
      public String toString()
      {
         return "("+x+","+y+" "+dx+","+dy+")";
      }
   }
   

   
   private class  MarkerPositionCalculator implements PathInterface
   {
      private final List<MarkerVector>  markers = new ArrayList<>();

      private float               startX, startY;
      private MarkerVector        lastPos = null;
      private boolean             startArc = false, normalCubic = true;
      private int                 subpathStartIndex = -1;
      private boolean             closepathReAdjustPending;

      
      MarkerPositionCalculator(PathDefinition pathDef)
      {
         if (pathDef == null)
            return;

         
         pathDef.enumeratePath(this);

         if (closepathReAdjustPending) {
            
            
            lastPos.add(markers.get(subpathStartIndex));
            
            markers.set(subpathStartIndex,  lastPos);
            closepathReAdjustPending = false;
         }
         
         if (lastPos != null) {
            markers.add(lastPos);
         }
      }

      List<MarkerVector>  getMarkers()
      {
         return markers;
      }

      @Override
      public void moveTo(float x, float y)
      {
         if (closepathReAdjustPending) {
            
            
            lastPos.add(markers.get(subpathStartIndex));
            
            markers.set(subpathStartIndex,  lastPos);
            closepathReAdjustPending = false;
         }
         if (lastPos != null) {
            markers.add(lastPos);
         }
         startX = x;
         startY = y;
         lastPos = new MarkerVector(x, y, 0, 0);
         subpathStartIndex = markers.size();
      }

      @Override
      public void lineTo(float x, float y)
      {
         lastPos.add(x, y);
         markers.add(lastPos);
         lastPos = new MarkerVector(x, y, x-lastPos.x, y-lastPos.y);
         closepathReAdjustPending = false;
      }

      @Override
      public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3)
      {
         if (normalCubic || startArc) {
            lastPos.add(x1, y1);
            markers.add(lastPos);
            startArc = false;
         }
         lastPos = new MarkerVector(x3, y3, x3-x2, y3-y2);
         closepathReAdjustPending = false;
      }

      @Override
      public void quadTo(float x1, float y1, float x2, float y2)
      {
         lastPos.add(x1, y1);
         markers.add(lastPos);
         lastPos = new MarkerVector(x2, y2, x2-x1, y2-y1);
         closepathReAdjustPending = false;
      }

      @Override
      public void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y)
      {
         
         startArc = true;
         normalCubic = false;
         SVGAndroidRenderer.arcTo(lastPos.x, lastPos.y, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y, this);
         normalCubic = true;
         closepathReAdjustPending = false;
      }

      @Override
      public void close()
      {
         markers.add(lastPos);
         lineTo(startX, startY);
         
         
         
         
         closepathReAdjustPending = true;
      }
         
   }


   private void  renderMarkers(GraphicsElement obj)
   {
      if (state.style.markerStart == null && state.style.markerMid == null && state.style.markerEnd == null)
         return;

      Marker  _markerStart = null;
      Marker  _markerMid = null;
      Marker  _markerEnd = null;

      if (state.style.markerStart != null) {
         SvgObject  ref = obj.document.resolveIRI(state.style.markerStart);
         if (ref != null)
            _markerStart = (Marker) ref;
         else
            error("Marker reference '%s' not found", state.style.markerStart);
      }

      if (state.style.markerMid != null) {
         SvgObject  ref = obj.document.resolveIRI(state.style.markerMid);
         if (ref != null)
            _markerMid = (Marker) ref;
         else
            error("Marker reference '%s' not found", state.style.markerMid);
      }

      if (state.style.markerEnd != null) {
         SvgObject  ref = obj.document.resolveIRI(state.style.markerEnd);
         if (ref != null)
            _markerEnd = (Marker) ref;
         else
            error("Marker reference '%s' not found", state.style.markerEnd);
      }

      List<MarkerVector>  markers;
      if (obj instanceof SVGBase.Path)
         markers = (new MarkerPositionCalculator(((SVGBase.Path) obj).d)).getMarkers();
      else if (obj instanceof Line)
         markers = calculateMarkerPositions((Line) obj);
      else 
         markers = calculateMarkerPositions((PolyLine) obj);

      if (markers == null)
         return;

      int  markerCount = markers.size();
      if (markerCount == 0)
         return;

      
      state.style.markerStart = state.style.markerMid = state.style.markerEnd = null;

      if (_markerStart != null)
         renderMarker(_markerStart, markers.get(0));

      if (_markerMid != null && markers.size() > 2)
      {
         MarkerVector  lastPos = markers.get(0);
         MarkerVector  thisPos = markers.get(1);

         for (int i=1; i<(markerCount-1); i++)
         {
            MarkerVector  nextPos = markers.get(i + 1);
            if (thisPos.isAmbiguous)
               thisPos = realignMarkerMid(lastPos, thisPos, nextPos);
            renderMarker(_markerMid, thisPos);
            lastPos = thisPos;
            thisPos = nextPos;
         }
      }

      if (_markerEnd != null)
         renderMarker(_markerEnd, markers.get(markerCount - 1));
   }


   
   private MarkerVector  realignMarkerMid(MarkerVector lastPos, MarkerVector thisPos, MarkerVector nextPos)
   {
      
      float  dot = dotProduct(thisPos.dx, thisPos.dy, (thisPos.x - lastPos.x), (thisPos.y - lastPos.y));
      if (dot == 0f) {
         
         dot = dotProduct(thisPos.dx, thisPos.dy, (nextPos.x - thisPos.x), (nextPos.y - thisPos.y));
      }
      if (dot > 0)
         return thisPos;
      if (dot == 0f) {
         
         
         if (thisPos.dx > 0f || thisPos.dy >= 0)
            return thisPos;
      }
      
      thisPos.dx = -thisPos.dx;
      thisPos.dy = -thisPos.dy;
      return thisPos;
   }


   
   private float  dotProduct(float x1, float y1, float x2, float y2)
   {
      return x1 * x2 + y1 * y2;
   }


   
   private void renderMarker(Marker marker, MarkerVector pos)
   {
      float  angle = 0f;
      float  unitsScale;

      statePush();

      
      if (marker.orient != null)
      {
         if (Float.isNaN(marker.orient))  
         {
            if (pos.dx != 0 || pos.dy != 0) {
               angle = (float) Math.toDegrees( Math.atan2(pos.dy, pos.dx) );
            }
         } else {
            angle = marker.orient;
         }
      }
      
      unitsScale = marker.markerUnitsAreUser ? 1f : state.style.strokeWidth.floatValue(dpi);

      
      
      state = findInheritFromAncestorState(marker);

      Matrix m = new Matrix();
      m.preTranslate(pos.x, pos.y);
      m.preRotate(angle);
      m.preScale(unitsScale, unitsScale);
      
      float _refX = (marker.refX != null) ? marker.refX.floatValueX(this) : 0f;
      float _refY = (marker.refY != null) ? marker.refY.floatValueY(this) : 0f;
      float _markerWidth = (marker.markerWidth != null) ? marker.markerWidth.floatValueX(this) : 3f;
      float _markerHeight = (marker.markerHeight != null) ? marker.markerHeight.floatValueY(this) : 3f;

      if (marker.viewBox != null)
      {
         
         
         
         float xScale, yScale;

         xScale = _markerWidth / marker.viewBox.width;
         yScale = _markerHeight / marker.viewBox.height;

         
         PreserveAspectRatio  positioning = (marker.preserveAspectRatio != null) ? marker.preserveAspectRatio :  PreserveAspectRatio.LETTERBOX;
         if (!positioning.equals(PreserveAspectRatio.STRETCH))
         {
            float  aspectScale = (positioning.getScale() == PreserveAspectRatio.Scale.slice) ? Math.max(xScale,  yScale) : Math.min(xScale,  yScale);
            xScale = yScale = aspectScale;
         }

         
         m.preTranslate(-_refX * xScale, -_refY * yScale);
         canvas.concat(m);

         
         
         float  imageW = marker.viewBox.width * xScale;
         float  imageH = marker.viewBox.height * yScale;
         float  xOffset = 0f;
         float  yOffset = 0f;
         switch (positioning.getAlignment())
         {
            case xMidYMin:
            case xMidYMid:
            case xMidYMax:
               xOffset -= (_markerWidth - imageW) / 2;
               break;
            case xMaxYMin:
            case xMaxYMid:
            case xMaxYMax:
               xOffset -= (_markerWidth - imageW);
               break;
            default:
               
                  break;
         }
         
         switch (positioning.getAlignment())
         {
            case xMinYMid:
            case xMidYMid:
            case xMaxYMid:
               yOffset -= (_markerHeight - imageH) / 2;
               break;
            case xMinYMax:
            case xMidYMax:
            case xMaxYMax:
               yOffset -= (_markerHeight - imageH);
               break;
            default:
               
               break;
         }

         if (!state.style.overflow) {
            setClipRect(xOffset, yOffset, _markerWidth, _markerHeight);
         }

         m.reset();
         m.preScale(xScale, yScale);
         canvas.concat(m);
      }
      else
      {
         

         m.preTranslate(-_refX, -_refY);
         canvas.concat(m);

         if (!state.style.overflow) {
            setClipRect(0, 0, _markerWidth, _markerHeight);
         }
      }

      boolean  compositing = pushLayer();

      renderChildren(marker, false);

      if (compositing)
         popLayer(marker);

      statePop();
   }


   
   private RendererState  findInheritFromAncestorState(SvgObject obj)
   {
      RendererState newState = new RendererState();
      updateStyle(newState, Style.getDefaultStyle());
      return findInheritFromAncestorState(obj, newState);
   }


   private RendererState  findInheritFromAncestorState(SvgObject obj, RendererState newState)
   {
      List<SvgElementBase>    ancestors = new ArrayList<>();

      
      while (true) {
         if (obj instanceof SvgElementBase) {
            ancestors.add(0, (SvgElementBase) obj);
         }
         if (obj.parent == null)
            break;
         obj = (SvgObject) obj.parent;
      }
      
      
      for (SvgElementBase ancestor: ancestors)
         updateStyleForElement(newState, ancestor);

      
      newState.viewBox = state.viewBox;
      newState.viewPort = state.viewPort;

      return newState;
   }


   
   
   


   
   private void  checkForGradientsAndPatterns(SvgElement obj)
   {
      if (state.style.fill instanceof PaintReference) {
         decodePaintReference(true, obj.boundingBox, (PaintReference) state.style.fill);
      }
      if (state.style.stroke instanceof PaintReference) {
         decodePaintReference(false, obj.boundingBox, (PaintReference) state.style.stroke);
      }
   }


   
   private void  decodePaintReference(boolean isFill, Box boundingBox, PaintReference paintref)
   {
      SvgObject  ref = document.resolveIRI(paintref.href);
      if (ref == null)
      {
         error("%s reference '%s' not found", (isFill ? "Fill":"Stroke"), paintref.href);
         if (paintref.fallback != null) {
            setPaintColour(state, isFill, paintref.fallback);
         } else {
            if (isFill)
               state.hasFill = false;
            else
               state.hasStroke = false;
         }
         return;
      }
      if (ref instanceof SvgLinearGradient)
         makeLinearGradient(isFill, boundingBox, (SvgLinearGradient) ref);
      else if (ref instanceof SvgRadialGradient)
         makeRadialGradient(isFill, boundingBox, (SvgRadialGradient) ref);
      else if (ref instanceof SolidColor)
         setSolidColor(isFill, (SolidColor) ref);
      
   }


   private void  makeLinearGradient(boolean isFill, Box boundingBox, SvgLinearGradient gradient)
   {
      if (gradient.href != null)
         fillInChainedGradientFields(gradient, gradient.href);

      boolean  userUnits = (gradient.gradientUnitsAreUser != null && gradient.gradientUnitsAreUser);
      Paint    paint = isFill ? state.fillPaint : state.strokePaint;

      float  _x1,_y1,_x2,_y2;
      if (userUnits)
      {
         _x1 = (gradient.x1 != null) ? gradient.x1.floatValueX(this): 0f;
         _y1 = (gradient.y1 != null) ? gradient.y1.floatValueY(this): 0f;
         _x2 = (gradient.x2 != null) ? gradient.x2.floatValueX(this): Length.PERCENT_100.floatValueX(this);  
         _y2 = (gradient.y2 != null) ? gradient.y2.floatValueY(this): 0f;
      }
      else
      {
         _x1 = (gradient.x1 != null) ? gradient.x1.floatValue(this, 1f): 0f;
         _y1 = (gradient.y1 != null) ? gradient.y1.floatValue(this, 1f): 0f;
         _x2 = (gradient.x2 != null) ? gradient.x2.floatValue(this, 1f): 1f;  
         _y2 = (gradient.y2 != null) ? gradient.y2.floatValue(this, 1f): 0f;
      }

      
      statePush();

      
      state = findInheritFromAncestorState(gradient);

      
      Matrix m = new Matrix();
      if (!userUnits)
      {
         m.preTranslate(boundingBox.minX, boundingBox.minY);
         m.preScale(boundingBox.width, boundingBox.height);
      }
      if (gradient.gradientTransform != null)
      {
         m.preConcat(gradient.gradientTransform);
      }

      
      int    numStops = gradient.children.size();
      if (numStops == 0) {
         
         statePop();
         if (isFill)
            state.hasFill = false;
         else
            state.hasStroke = false;
         return;
      }

      int[]  colours = new int[numStops];
      float[]  positions = new float[numStops];
      int  i = 0;
      float  lastOffset = -1;
      for (SvgObject child: gradient.children)
      {
         Stop  stop = (Stop) child;
         float offset = (stop.offset != null) ? stop.offset : 0f;
         if (i == 0 || offset >= lastOffset) {
            positions[i] = offset;
            lastOffset = offset;
         } else {
            
            
            positions[i] = lastOffset;
         }

         statePush();

         updateStyleForElement(state, stop);
         Colour col = (Colour) state.style.stopColor;
         if (col == null)
            col = Colour.BLACK;
         colours[i] = colourWithOpacity(col.colour, state.style.stopOpacity);
         i++;

         statePop();
      }

      
      if ((_x1 == _x2 && _y1 == _y2) || numStops == 1) {
         statePop();
         paint.setColor(colours[numStops - 1]);
         return;
      }

      
      TileMode  tileMode = TileMode.CLAMP;
      if (gradient.spreadMethod != null)
      {
         if (gradient.spreadMethod == GradientSpread.reflect)
            tileMode = TileMode.MIRROR;
         else if (gradient.spreadMethod == GradientSpread.repeat)
            tileMode = TileMode.REPEAT;
      }
      
      statePop();

      
      LinearGradient  gr = new LinearGradient(_x1, _y1, _x2, _y2, colours, positions, tileMode);
      gr.setLocalMatrix(m);
      paint.setShader(gr);
      paint.setAlpha(clamp255(state.style.fillOpacity));
   }


   private void  makeRadialGradient(boolean isFill, Box boundingBox, SvgRadialGradient gradient)
   {
      if (gradient.href != null)
         fillInChainedGradientFields(gradient, gradient.href);

      boolean  userUnits = (gradient.gradientUnitsAreUser != null && gradient.gradientUnitsAreUser);
      Paint    paint = isFill ? state.fillPaint : state.strokePaint;

      float  _cx, _cy, _r,
             _fx = 0, _fy = 0, _fr = 0;
      if (userUnits)
      {
         Length  fiftyPercent = new Length(50f, Unit.percent);
         _cx = (gradient.cx != null) ? gradient.cx.floatValueX(this): fiftyPercent.floatValueX(this);
         _cy = (gradient.cy != null) ? gradient.cy.floatValueY(this): fiftyPercent.floatValueY(this);
         _r = (gradient.r != null) ? gradient.r.floatValue(this): fiftyPercent.floatValue(this);

         if (SUPPORTS_RADIAL_GRADIENT_WITH_FOCUS) {
            _fx = (gradient.fx != null) ? gradient.fx.floatValueX(this): _cx;
            _fy = (gradient.fy != null) ? gradient.fy.floatValueY(this): _cy;
            _fr = (gradient.fr != null) ? gradient.fr.floatValue(this): 0;
         }
      }
      else
      {
         _cx = (gradient.cx != null) ? gradient.cx.floatValue(this, 1f): 0.5f;
         _cy = (gradient.cy != null) ? gradient.cy.floatValue(this, 1f): 0.5f;
         _r = (gradient.r != null) ? gradient.r.floatValue(this, 1f): 0.5f;

         if (SUPPORTS_RADIAL_GRADIENT_WITH_FOCUS) {
            _fx = (gradient.fx != null) ? gradient.fx.floatValue(this, 1f): 0.5f;
            _fy = (gradient.fy != null) ? gradient.fy.floatValue(this, 1f): 0.5f;
            _fr = (gradient.fr != null) ? gradient.fr.floatValue(this, 1f): 0;
         }
      }
      
      

      
      statePush();

      
      state = findInheritFromAncestorState(gradient);

      
      Matrix m = new Matrix();
      if (!userUnits)
      {
         m.preTranslate(boundingBox.minX, boundingBox.minY);
         m.preScale(boundingBox.width, boundingBox.height);
      }
      if (gradient.gradientTransform != null)
      {
         m.preConcat(gradient.gradientTransform);
      }

      
      int    numStops = gradient.children.size();
      if (numStops == 0) {
         
         statePop();
         if (isFill)
            state.hasFill = false;
         else
            state.hasStroke = false;
         return;
      }

      int[]  colours = null;
      
      long[] colourLongs = null;

      if (SUPPORTS_RADIAL_GRADIENT_WITH_FOCUS) {
         colourLongs = new long[numStops];
      } else {
         colours = new int[numStops];
      }

      float[]  positions = new float[numStops];
      int  i = 0;
      float  lastOffset = -1;
      for (SvgObject child: gradient.children)
      {
         Stop  stop = (Stop) child;
         float offset = (stop.offset != null) ? stop.offset : 0f;
         if (i == 0 || offset >= lastOffset) {
            positions[i] = offset;
            lastOffset = offset;
         } else {
            
            
            positions[i] = lastOffset;
         }

         statePush();

         updateStyleForElement(state, stop);
         Colour col = (Colour) state.style.stopColor;
         if (col == null)
            col = Colour.BLACK;
         if (SUPPORTS_RADIAL_GRADIENT_WITH_FOCUS) {
            colourLongs[i] = Color.pack( colourWithOpacity(col.colour, state.style.stopOpacity) );
         } else {
            colours[i] = colourWithOpacity(col.colour, state.style.stopOpacity);
         }
         i++;

         statePop();
      }

      
      if (_r == 0 || numStops == 1) {
         statePop();
         paint.setColor(colours[numStops - 1]);
         return;
      }

      
      TileMode  tileMode = TileMode.CLAMP;
      if (gradient.spreadMethod != null)
      {
         if (gradient.spreadMethod == GradientSpread.reflect)
            tileMode = TileMode.MIRROR;
         else if (gradient.spreadMethod == GradientSpread.repeat)
            tileMode = TileMode.REPEAT;
      }

      statePop();

      
      RadialGradient  gr = SUPPORTS_RADIAL_GRADIENT_WITH_FOCUS ? new RadialGradient(_fx, _fy, _fr, _cx, _cy, _r, colourLongs, positions, tileMode)
                                                               : new RadialGradient(_cx, _cy, _r, colours, positions, tileMode);
      gr.setLocalMatrix(m);
      paint.setShader(gr);
      paint.setAlpha(clamp255(state.style.fillOpacity));
   }


   
   private void fillInChainedGradientFields(GradientElement gradient, String href)
   {
      
      SvgObject  ref = gradient.document.resolveIRI(href);
      if (ref == null) {
         
         warn("Gradient reference '%s' not found", href);
         return;
      }
      if (!(ref instanceof GradientElement)) {
         error("Gradient href attributes must point to other gradient elements");
         return;
      }
      if (ref == gradient) {
         error("Circular reference in gradient href attribute '%s'", href);
         return;
      }

      GradientElement  grRef = (GradientElement) ref;

      if (gradient.gradientUnitsAreUser == null)
         gradient.gradientUnitsAreUser = grRef.gradientUnitsAreUser;
      if (gradient.gradientTransform == null)
         gradient.gradientTransform = grRef.gradientTransform;
      if (gradient.spreadMethod == null)
         gradient.spreadMethod = grRef.spreadMethod;
      if (gradient.children.isEmpty())
         gradient.children = grRef.children;

      try
      {
         if (gradient instanceof SvgLinearGradient) {
            fillInChainedGradientFields((SvgLinearGradient) gradient, (SvgLinearGradient) ref);
         } else {
            fillInChainedGradientFields((SvgRadialGradient) gradient, (SvgRadialGradient) ref);
         }
      }
      catch (ClassCastException e) {  }

      if (grRef.href != null)
         fillInChainedGradientFields(gradient, grRef.href);
   }


   private void fillInChainedGradientFields(SvgLinearGradient gradient, SvgLinearGradient grRef)
   {
      if (gradient.x1 == null)
         gradient.x1 = grRef.x1;
      if (gradient.y1 == null)
         gradient.y1 = grRef.y1;
      if (gradient.x2 == null)
         gradient.x2 = grRef.x2;
      if (gradient.y2 == null)
         gradient.y2 = grRef.y2;
   }


   private void fillInChainedGradientFields(SvgRadialGradient gradient, SvgRadialGradient grRef)
   {
      if (gradient.cx == null)
         gradient.cx = grRef.cx;
      if (gradient.cy == null)
         gradient.cy = grRef.cy;
      if (gradient.r == null)
         gradient.r = grRef.r;
      if (gradient.fx == null)
         gradient.fx = grRef.fx;
      if (gradient.fy == null)
         gradient.fy = grRef.fy;
      if (gradient.fr == null)
         gradient.fr = grRef.fr;
   }


   private void setSolidColor(boolean isFill, SolidColor ref)
   {
      
      if (isFill)
      {
        if (isSpecified(ref.baseStyle, Style.SPECIFIED_SOLID_COLOR))
        {
           state.style.fill = ref.baseStyle.solidColor;
           state.hasFill = (ref.baseStyle.solidColor != null);
        }

        if (isSpecified(ref.baseStyle, Style.SPECIFIED_SOLID_OPACITY))
        {
           state.style.fillOpacity = ref.baseStyle.solidOpacity;
        }

        
        if (isSpecified(ref.baseStyle, Style.SPECIFIED_SOLID_COLOR | Style.SPECIFIED_SOLID_OPACITY))
        {
           
           setPaintColour(state, isFill, state.style.fill);
        }
      }
      else
      {
        if (isSpecified(ref.baseStyle, Style.SPECIFIED_SOLID_COLOR))
        {
           state.style.stroke = ref.baseStyle.solidColor;
           state.hasStroke = (ref.baseStyle.solidColor != null);
        }

        if (isSpecified(ref.baseStyle, Style.SPECIFIED_SOLID_OPACITY))
        {
           state.style.strokeOpacity = ref.baseStyle.solidOpacity;
        }

        
        if (isSpecified(ref.baseStyle, Style.SPECIFIED_SOLID_COLOR | Style.SPECIFIED_SOLID_OPACITY))
        {
           
           setPaintColour(state, isFill, state.style.stroke);
        }
      }
      
   }


   
   
   


   private void  checkForClipPath(SvgElement obj)
   {
      checkForClipPath(obj, obj.boundingBox);
   }


   private void  checkForClipPath(SvgElement obj, Box boundingBox)
   {
      if (state.style.clipPath == null)
         return;

      if (SUPPORTS_PATH_OP)
      {
         
         Path  combinedPath = calculateClipPath(obj, boundingBox);
         if (combinedPath != null)
           canvas.clipPath(combinedPath);
      }
      else
      {
         checkForClipPath_OldStyle(obj, boundingBox);
      }
   }


   
   
   
   

   @TargetApi(Build.VERSION_CODES.KITKAT)
   private Path  calculateClipPath(SvgElement obj, Box boundingBox)
   {
      
      SvgObject  ref = obj.document.resolveIRI(state.style.clipPath);
      if (ref == null) {
         error("ClipPath reference '%s' not found", state.style.clipPath);
         return null;
      }
      
      
      
      if (ref.getNodeName() != ClipPath.NODE_NAME)
         return null;

      ClipPath  clipPath = (ClipPath) ref;

      
      stateStack.push(state);

      
      
      state = findInheritFromAncestorState(clipPath);

      boolean  userUnits = (clipPath.clipPathUnitsAreUser == null || clipPath.clipPathUnitsAreUser);
      Matrix   m = new Matrix();
      if (!userUnits)
      {
         m.preTranslate(boundingBox.minX, boundingBox.minY);
         m.preScale(boundingBox.width, boundingBox.height);
      }
      if (clipPath.transform != null)
      {
         m.preConcat(clipPath.transform);
      }

      Path  combinedPath = new Path();
      for (SvgObject child: clipPath.children)
      {
         if (!(child instanceof SvgElement))
            continue;
         Path part = objectToPath((SvgElement) child, true);
         if (part != null)
            combinedPath.op(part, Path.Op.UNION);
      }

      
      if (state.style.clipPath != null)
      {
         if (clipPath.boundingBox == null)
            clipPath.boundingBox = calculatePathBounds(combinedPath);
         Path clipClipPath = calculateClipPath(clipPath, clipPath.boundingBox);
         if (clipClipPath != null)
            combinedPath.op(clipClipPath, Path.Op.INTERSECT);
      }

      combinedPath.transform(m);

      
      state = stateStack.pop();

      return combinedPath;
   }


   
   @TargetApi(Build.VERSION_CODES.KITKAT)
   private Path objectToPath(SvgElement obj, boolean allowUse)
   {
      
      stateStack.push(state);
      state = new RendererState(state);

      updateStyleForElement(state, obj);

      if (!display() || !visible()) {
         state = stateStack.pop();
         return null;
      }

      Path  path = null;

      if (obj instanceof Use)
      {
         if (!allowUse) {
            error("<use> elements inside a <clipPath> cannot reference another <use>");
         }

         
         Use  useElement = (Use) obj;
         SvgObject  ref = obj.document.resolveIRI(useElement.href);
         if (ref == null) {
            error("Use reference '%s' not found", useElement.href);
            state = stateStack.pop();
            return null;
         }
         if (!(ref instanceof SvgElement)) {
            state = stateStack.pop();
            return null;
         }

         path = objectToPath((SvgElement) ref, false);
         if (path == null)
            return null;

         if (useElement.boundingBox == null) {
            useElement.boundingBox = calculatePathBounds(path);
         }

         if (useElement.transform != null)
            path.transform(useElement.transform);
      }
      else if (obj instanceof GraphicsElement)
      {
         GraphicsElement  elem = (GraphicsElement) obj;

         if (obj instanceof SVGBase.Path)
         {
            SVGBase.Path  pathElem = (SVGBase.Path) obj;
            path = (new PathConverter(pathElem.d)).getPath();
            if (obj.boundingBox == null)
               obj.boundingBox = calculatePathBounds(path);
         }
         else if (obj instanceof Rect)
            path = makePathAndBoundingBox((Rect) obj);
         else if (obj instanceof Circle)
            path = makePathAndBoundingBox((Circle) obj);
         else if (obj instanceof Ellipse)
            path = makePathAndBoundingBox((Ellipse) obj);
         else if (obj instanceof PolyLine)
            path = makePathAndBoundingBox((PolyLine) obj);

         if (path == null)
            return null;

         if (elem.boundingBox == null) {
            elem.boundingBox = calculatePathBounds(path);
         }

         if (elem.transform != null)
            path.transform(elem.transform);

         path.setFillType(getClipRuleFromState());
      }
      else if (obj instanceof Text)
      {
         Text  textElem = (Text) obj;
         path = makePathAndBoundingBox(textElem);

         if (textElem.transform != null)
            path.transform(textElem.transform);

         path.setFillType(getClipRuleFromState());
      }
      else {
         error("Invalid %s element found in clipPath definition", obj.getNodeName());
         return null;
      }

      
      if (state.style.clipPath != null)
      {
         Path  childsClipPath = calculateClipPath(obj, obj.boundingBox);
         if (childsClipPath != null)
            path.op(childsClipPath, Path.Op.INTERSECT);
      }

      
      state = stateStack.pop();

      return path;
   }



   
   
   


   private void checkForClipPath_OldStyle(SvgElement obj, Box boundingBox)
   {
      

      
      SvgObject  ref = obj.document.resolveIRI(state.style.clipPath);
      if (ref == null) {
         error("ClipPath reference '%s' not found", state.style.clipPath);
         return;
      }
      
      
      
      if (ref.getNodeName() != ClipPath.NODE_NAME)
         return;

      ClipPath clipPath = (ClipPath) ref;

      
      if (clipPath.children.isEmpty()) {
         canvas.clipRect(0, 0, 0, 0);
         return;
      }

      boolean  userUnits = (clipPath.clipPathUnitsAreUser == null || clipPath.clipPathUnitsAreUser);

      if ((obj instanceof Group) && !userUnits) {
         warn("<clipPath clipPathUnits=\"objectBoundingBox\"> is not supported when referenced from container elements (like %s)", obj.getNodeName());
         return;
      }

      clipStatePush();

      if (!userUnits)
      {
         Matrix m = new Matrix();
         m.preTranslate(boundingBox.minX, boundingBox.minY);
         m.preScale(boundingBox.width, boundingBox.height);
         canvas.concat(m);
      }
      if (clipPath.transform != null)
      {
         canvas.concat(clipPath.transform);
      }

      
      
      state = findInheritFromAncestorState(clipPath);

      checkForClipPath(clipPath);

      Path combinedPath = new Path();
      for (SvgObject child: clipPath.children)
      {
         addObjectToClip(child, true, combinedPath, new Matrix());
      }
      canvas.clipPath(combinedPath);

      clipStatePop();
   }


   private void addObjectToClip(SvgObject obj, boolean allowUse, Path combinedPath, Matrix combinedPathMatrix)
   {
      if (!display())
         return;

      
      clipStatePush();

      if (obj instanceof Use) {
         if (allowUse) {
            addObjectToClip((Use) obj, combinedPath, combinedPathMatrix);
         } else {
            error("<use> elements inside a <clipPath> cannot reference another <use>");
         }
      } else if (obj instanceof SVGBase.Path) {
         addObjectToClip((SVGBase.Path) obj, combinedPath, combinedPathMatrix);
      } else if (obj instanceof Text) {
         addObjectToClip((Text) obj, combinedPath, combinedPathMatrix);
      } else if (obj instanceof GraphicsElement) {
         addObjectToClip((GraphicsElement) obj, combinedPath, combinedPathMatrix);
      } else {
         error("Invalid %s element found in clipPath definition", obj.toString());
      }

      
      clipStatePop();
   }


   
   
   
   private void  clipStatePush()
   {
      
      CanvasLegacy.save(canvas, CanvasLegacy.MATRIX_SAVE_FLAG);
      
      stateStack.push(state);
      state = new RendererState(state);
   }


   private void  clipStatePop()
   {
      
      canvas.restore();
      
      state = stateStack.pop();
   }


   private Path.FillType  getClipRuleFromState()
   {
      if (state.style.clipRule != null && state.style.clipRule == Style.FillRule.EvenOdd)
         return Path.FillType.EVEN_ODD;
      else
         return Path.FillType.WINDING;
   }


   private void addObjectToClip(SVGBase.Path obj, Path combinedPath, Matrix combinedPathMatrix)
   {
      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;

      if (obj.transform != null)
         combinedPathMatrix.preConcat(obj.transform);

      Path  path = (new PathConverter(obj.d)).getPath();

      if (obj.boundingBox == null) {
         obj.boundingBox = calculatePathBounds(path);
      }
      checkForClipPath(obj);

      
      combinedPath.setFillType(getClipRuleFromState());
      combinedPath.addPath(path, combinedPathMatrix);
   }


   private void addObjectToClip(GraphicsElement obj, Path combinedPath, Matrix combinedPathMatrix)
   {
      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;

      if (obj.transform != null)
         combinedPathMatrix.preConcat(obj.transform);

      Path  path;
      if (obj instanceof Rect)
         path = makePathAndBoundingBox((Rect) obj);
      else if (obj instanceof Circle)
         path = makePathAndBoundingBox((Circle) obj);
      else if (obj instanceof Ellipse)
         path = makePathAndBoundingBox((Ellipse) obj);
      else if (obj instanceof PolyLine)
         path = makePathAndBoundingBox((PolyLine) obj);
      else
         return;

      if (path == null)  
         return;

      checkForClipPath(obj);

      combinedPath.setFillType(getClipRuleFromState());
      combinedPath.addPath(path, combinedPathMatrix);
   }


   private void addObjectToClip(Use obj, Path combinedPath, Matrix combinedPathMatrix)
   {
      updateStyleForElement(state, obj);

      if (!display())
         return;
      if (!visible())
         return;

      if (obj.transform != null)
         combinedPathMatrix.preConcat(obj.transform);

      
      SvgObject  ref = obj.document.resolveIRI(obj.href);
      if (ref == null) {
         error("Use reference '%s' not found", obj.href);
         return;
      }

      checkForClipPath(obj);
      
      addObjectToClip(ref, false, combinedPath, combinedPathMatrix);
   }


   private void addObjectToClip(Text obj, Path combinedPath, Matrix combinedPathMatrix)
   {
      updateStyleForElement(state, obj);

      if (!display())
         return;

      if (obj.transform != null)
         combinedPathMatrix.preConcat(obj.transform);

      
      float  x = (obj.x == null || obj.x.size() == 0) ? 0f : obj.x.get(0).floatValueX(this);
      float  y = (obj.y == null || obj.y.size() == 0) ? 0f : obj.y.get(0).floatValueY(this);
      float  dx = (obj.dx == null || obj.dx.size() == 0) ? 0f : obj.dx.get(0).floatValueX(this);
      float  dy = (obj.dy == null || obj.dy.size() == 0) ? 0f : obj.dy.get(0).floatValueY(this);

      
      if (state.style.textAnchor != TextAnchor.Start) {
         float  textWidth = calculateTextWidth(obj);
         if (state.style.textAnchor == TextAnchor.Middle) {
            x -= (textWidth / 2);
         } else {
            x -= textWidth;  
         }
      }

      if (obj.boundingBox == null) {
         TextBoundsCalculator  proc = new TextBoundsCalculator(x, y);
         enumerateTextSpans(obj, proc);
         obj.boundingBox = new Box(proc.bbox.left, proc.bbox.top, proc.bbox.width(), proc.bbox.height());
      }
      checkForClipPath(obj);

      Path  textAsPath = new Path();
      enumerateTextSpans(obj, new PlainTextToPath(x + dx, y + dy, textAsPath));

      combinedPath.setFillType(getClipRuleFromState());
      combinedPath.addPath(textAsPath, combinedPathMatrix);
   }


   


   private class  PlainTextToPath extends TextProcessor
   {
      float   x;
      float   y;
      final Path    textAsPath;

      PlainTextToPath(float x, float y, Path textAsPath)
      {
         this.x = x;
         this.y = y;
         this.textAsPath = textAsPath;
      }

      @Override
      public boolean doTextContainer(TextContainer obj)
      {
         if (obj instanceof TextPath)
         {
            warn("Using <textPath> elements in a clip path is not supported.");
            return false;
         }
         return true;
      }

      @Override
      public void processText(String text)
      {
         if (visible())
         {
            
            Path spanPath = new Path();
            state.fillPaint.getTextPath(text, 0, text.length(), x, y, spanPath);
            textAsPath.addPath(spanPath);
         }

         
         x += measureText(text, state.fillPaint);
      }
   }


   
   
   


   private Path  makePathAndBoundingBox(Line obj)
   {
      float x1 = (obj.x1 == null) ? 0 : obj.x1.floatValueX(this);
      float y1 = (obj.y1 == null) ? 0 : obj.y1.floatValueY(this);
      float x2 = (obj.x2 == null) ? 0 : obj.x2.floatValueX(this);
      float y2 = (obj.y2 == null) ? 0 : obj.y2.floatValueY(this);

      if (obj.boundingBox == null) {
         obj.boundingBox = new Box(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2-x1), Math.abs(y2-y1));
      }

      Path  p = new Path();
      p.moveTo(x1, y1);
      p.lineTo(x2, y2);
      return p;
   }


   private Path  makePathAndBoundingBox(Rect obj)
   {
      float x, y, w, h, rx, ry;

      if (obj.rx == null && obj.ry == null) {
         rx = 0;
         ry = 0;
      } else if (obj.rx == null) {
         rx = ry = obj.ry.floatValueY(this);
      } else if (obj.ry == null) {
         rx = ry = obj.rx.floatValueX(this);
      } else {
         rx = obj.rx.floatValueX(this);
         ry = obj.ry.floatValueY(this);
      }
      rx = Math.min(rx, obj.width.floatValueX(this) / 2f);
      ry = Math.min(ry, obj.height.floatValueY(this) / 2f);
      x = (obj.x != null) ? obj.x.floatValueX(this) : 0f;
      y = (obj.y != null) ? obj.y.floatValueY(this) : 0f;
      w = obj.width.floatValueX(this);
      h = obj.height.floatValueY(this);

      if (obj.boundingBox == null) {
         obj.boundingBox = new Box(x, y, w, h);
      }

      float  right = x + w;
      float  bottom = y + h;

      Path  p = new Path();
      if (rx == 0 || ry == 0)
      {
         
         p.moveTo(x, y);
         p.lineTo(right, y);
         p.lineTo(right, bottom);
         p.lineTo(x, bottom);
         p.lineTo(x, y);
      }
      else
      {
         
         
         
         float  cpx = rx * BEZIER_ARC_FACTOR;
         float  cpy = ry * BEZIER_ARC_FACTOR;

         p.moveTo(x, y+ry);
         p.cubicTo(x, y+ry-cpy, x+rx-cpx, y, x+rx, y);
         p.lineTo(right-rx, y);
         p.cubicTo(right-rx+cpx, y, right, y+ry-cpy, right, y+ry);
         p.lineTo(right, bottom-ry);
         p.cubicTo(right, bottom-ry+cpy, right-rx+cpx, bottom, right-rx, bottom);
         p.lineTo(x+rx, bottom);
         p.cubicTo(x+rx-cpx, bottom, x, bottom-ry+cpy, x, bottom-ry);
         p.lineTo(x, y+ry);
      }
      p.close();
      return p;
   }


   private Path makePathAndBoundingBox(Circle obj)
   {
      float  cx = (obj.cx != null) ? obj.cx.floatValueX(this) : 0f;
      float  cy = (obj.cy != null) ? obj.cy.floatValueY(this) : 0f;
      float  r = obj.r.floatValue(this);

      float  left = cx - r;
      float  top = cy - r;
      float  right = cx + r;
      float  bottom = cy + r;

      if (obj.boundingBox == null) {
         obj.boundingBox = new Box(left, top, r*2, r*2);
      }

      float  cp = r * BEZIER_ARC_FACTOR;

      Path  p = new Path();
      p.moveTo(cx, top);
      p.cubicTo(cx+cp, top, right, cy-cp, right, cy);
      p.cubicTo(right, cy+cp, cx+cp, bottom, cx, bottom);
      p.cubicTo(cx-cp, bottom, left, cy+cp, left, cy);
      p.cubicTo(left, cy-cp, cx-cp, top, cx, top);
      p.close();
      return p;
   }


   private Path makePathAndBoundingBox(Ellipse obj)
   {
      float  cx = (obj.cx != null) ? obj.cx.floatValueX(this) : 0f;
      float  cy = (obj.cy != null) ? obj.cy.floatValueY(this) : 0f;
      float  rx = obj.rx.floatValueX(this);
      float  ry = obj.ry.floatValueY(this);

      float  left = cx - rx;
      float  top = cy - ry;
      float  right = cx + rx;
      float  bottom = cy + ry;

      if (obj.boundingBox == null) {
         obj.boundingBox = new Box(left, top, rx*2, ry*2);
      }

      float  cpx = rx * BEZIER_ARC_FACTOR;
      float  cpy = ry * BEZIER_ARC_FACTOR;

      Path  p = new Path();
      p.moveTo(cx, top);
      p.cubicTo(cx+cpx, top, right, cy-cpy, right, cy);
      p.cubicTo(right, cy+cpy, cx+cpx, bottom, cx, bottom);
      p.cubicTo(cx-cpx, bottom, left, cy+cpy, left, cy);
      p.cubicTo(left, cy-cpy, cx-cpx, top, cx, top);
      p.close();
      return p;
   }


   private Path makePathAndBoundingBox(PolyLine obj)
   {
      Path  path = new Path();

      int  numPoints = (obj.points != null) ? obj.points.length : 0;
      
      if (numPoints % 2 != 0)
         return null;

      if (numPoints > 0)
      {
         int  i = 0;
         while (numPoints >= 2)
         {
            if (i == 0)
               path.moveTo(obj.points[i], obj.points[i+1]);
            else
               path.lineTo(obj.points[i], obj.points[i+1]);
            i += 2;
            numPoints -= 2;
         }
         if (obj instanceof Polygon)
            path.close();
      }

      if (obj.boundingBox == null) {
         obj.boundingBox = calculatePathBounds(path);
      }
      return path;
   }


   private Path makePathAndBoundingBox(Text obj)
   {
      
      float  x = (obj.x == null || obj.x.size() == 0) ? 0f : obj.x.get(0).floatValueX(this);
      float  y = (obj.y == null || obj.y.size() == 0) ? 0f : obj.y.get(0).floatValueY(this);
      float  dx = (obj.dx == null || obj.dx.size() == 0) ? 0f : obj.dx.get(0).floatValueX(this);
      float  dy = (obj.dy == null || obj.dy.size() == 0) ? 0f : obj.dy.get(0).floatValueY(this);

      
      if (state.style.textAnchor != TextAnchor.Start) {
         float  textWidth = calculateTextWidth(obj);
         if (state.style.textAnchor == TextAnchor.Middle) {
            x -= (textWidth / 2);
         } else {
            x -= textWidth;  
         }
      }

      if (obj.boundingBox == null) {
         TextBoundsCalculator  proc = new TextBoundsCalculator(x, y);
         enumerateTextSpans(obj, proc);
         obj.boundingBox = new Box(proc.bbox.left, proc.bbox.top, proc.bbox.width(), proc.bbox.height());
      }

      Path  textAsPath = new Path();
      enumerateTextSpans(obj, new PlainTextToPath(x + dx, y + dy, textAsPath));

      return textAsPath;
   }



   
   
   


   
   private void  fillWithPattern(SvgElement obj, Path path, Pattern pattern)
   {
      boolean      patternUnitsAreUser = (pattern.patternUnitsAreUser != null && pattern.patternUnitsAreUser);
      float        x, y, w, h;
      float        originX, originY;
      float        objFillOpacity = state.style.fillOpacity;

      if (pattern.href != null)
         fillInChainedPatternFields(pattern, pattern.href);

      if (patternUnitsAreUser)
      {
         x = (pattern.x != null) ? pattern.x.floatValueX(this): 0f;
         y = (pattern.y != null) ? pattern.y.floatValueY(this): 0f;
         w = (pattern.width != null) ? pattern.width.floatValueX(this): 0f;
         h = (pattern.height != null) ? pattern.height.floatValueY(this): 0f;
      }
      else
      {
         
         x = (pattern.x != null) ? pattern.x.floatValue(this, 1f): 0f;
         y = (pattern.y != null) ? pattern.y.floatValue(this, 1f): 0f;
         w = (pattern.width != null) ? pattern.width.floatValue(this, 1f): 0f;
         h = (pattern.height != null) ? pattern.height.floatValue(this, 1f): 0f;
         x = obj.boundingBox.minX + x * obj.boundingBox.width;
         y = obj.boundingBox.minY + y * obj.boundingBox.height;
         w *= obj.boundingBox.width;
         h *= obj.boundingBox.height;
      }
      if (w == 0 || h == 0)
         return;

      
      PreserveAspectRatio  positioning = (pattern.preserveAspectRatio != null) ? pattern.preserveAspectRatio : PreserveAspectRatio.LETTERBOX;

      
      statePush();
      
      canvas.clipPath(path);

      
      RendererState  baseState = new RendererState();
      updateStyle(baseState, Style.getDefaultStyle());
      baseState.style.overflow = false;    

      
      state = findInheritFromAncestorState(pattern, baseState);

      
      Box  patternArea = obj.boundingBox;
      
      if (pattern.patternTransform != null)
      {
         canvas.concat(pattern.patternTransform);
         
         
         
         Matrix inverse = new Matrix();
         if (pattern.patternTransform.invert(inverse)) {
            float[] pts = {obj.boundingBox.minX, obj.boundingBox.minY,
                           obj.boundingBox.maxX(), obj.boundingBox.minY,
                           obj.boundingBox.maxX(), obj.boundingBox.maxY(),
                           obj.boundingBox.minX, obj.boundingBox.maxY()};
            inverse.mapPoints(pts);
            
            RectF  rect = new RectF(pts[0], pts[1], pts[0], pts[1]);
            for (int i=2; i<=6; i+=2) {
               if (pts[i] < rect.left) rect.left = pts[i]; 
               if (pts[i] > rect.right) rect.right = pts[i]; 
               if (pts[i+1] < rect.top) rect.top = pts[i+1]; 
               if (pts[i+1] > rect.bottom) rect.bottom = pts[i+1]; 
            }
            patternArea = new Box(rect.left, rect.top, rect.right-rect.left, rect.bottom-rect.top);
         }
      }

      
      originX = x + (float) Math.floor((patternArea.minX - x) / w) * w;
      originY = y + (float) Math.floor((patternArea.minY - y) / h) * h;

      
      float  right = patternArea.maxX();
      float  bottom = patternArea.maxY();
      Box    stepViewBox = new Box(0,0,w,h);

      boolean  compositing = pushLayer(objFillOpacity);

      for (float stepY = originY; stepY < bottom; stepY += h)
      {
         for (float stepX = originX; stepX < right; stepX += w)
         {
            stepViewBox.minX = stepX;
            stepViewBox.minY = stepY;

            
            statePush();

            
            if (!state.style.overflow) {
               setClipRect(stepViewBox.minX, stepViewBox.minY, stepViewBox.width, stepViewBox.height);
            }
            
            if (pattern.viewBox != null)
            {
               canvas.concat(calculateViewBoxTransform(stepViewBox, pattern.viewBox, positioning));
            }
            else
            {
               boolean  patternContentUnitsAreUser = (pattern.patternContentUnitsAreUser == null || pattern.patternContentUnitsAreUser);
               
               canvas.translate(stepX, stepY);
               if (!patternContentUnitsAreUser) {
                  canvas.scale(obj.boundingBox.width, obj.boundingBox.height);
               }
            }


            
            for (SvgObject child: pattern.children) {
               render(child);
            }

            
            statePop();
         }
      }

      if (compositing)
         popLayer(pattern);

      
      statePop();
   }


   
   private void fillInChainedPatternFields(Pattern pattern, String href)
   {
      
      SvgObject  ref = pattern.document.resolveIRI(href);
      if (ref == null) {
         
         warn("Pattern reference '%s' not found", href);
         return;
      }
      if (!(ref instanceof Pattern)) {
         error("Pattern href attributes must point to other pattern elements");
         return;
      }
      if (ref == pattern) {
         error("Circular reference in pattern href attribute '%s'", href);
         return;
      }

      Pattern  pRef = (Pattern) ref;

      if (pattern.patternUnitsAreUser == null)
         pattern.patternUnitsAreUser = pRef.patternUnitsAreUser;
      if (pattern.patternContentUnitsAreUser == null)
         pattern.patternContentUnitsAreUser = pRef.patternContentUnitsAreUser;
      if (pattern.patternTransform == null)
         pattern.patternTransform = pRef.patternTransform;
      if (pattern.x == null)
         pattern.x = pRef.x;
      if (pattern.y == null)
         pattern.y = pRef.y;
      if (pattern.width == null)
         pattern.width = pRef.width;
      if (pattern.height == null)
         pattern.height = pRef.height;
      
      if (pattern.children.isEmpty())
         pattern.children = pRef.children;
      if (pattern.viewBox == null)
         pattern.viewBox = pRef.viewBox;
      if (pattern.preserveAspectRatio == null) {
         pattern.preserveAspectRatio = pRef.preserveAspectRatio;
      }

      if (pRef.href != null)
         fillInChainedPatternFields(pattern, pRef.href);
   }


   
   
   


   
   private void  renderMask(Mask mask, SvgElement obj, Box originalObjBBox)
   {
      debug("Mask render");

      boolean      maskUnitsAreUser = (mask.maskUnitsAreUser != null && mask.maskUnitsAreUser);
      float        w, h;

      if (maskUnitsAreUser)
      {
         w = (mask.width != null) ? mask.width.floatValueX(this): originalObjBBox.width;
         h = (mask.height != null) ? mask.height.floatValueY(this): originalObjBBox.height;
         
         
      }
      else
      {
         
         
         
         w = (mask.width != null) ? mask.width.floatValue(this, 1f): 1.2f;
         h = (mask.height != null) ? mask.height.floatValue(this, 1f): 1.2f;
         
         
         w *= originalObjBBox.width;
         h *= originalObjBBox.height;
      }
      if (w == 0 || h == 0)
         return;

      
      statePush();

      state = findInheritFromAncestorState(mask);
      
      
      state.style.opacity = 1f;
      

      boolean  compositing = pushLayer();

      
      canvas.save();

      boolean  maskContentUnitsAreUser = (mask.maskContentUnitsAreUser == null || mask.maskContentUnitsAreUser);
      if (!maskContentUnitsAreUser) {
         canvas.translate(originalObjBBox.minX, originalObjBBox.minY);
         canvas.scale(originalObjBBox.width, originalObjBBox.height);
      }

      
      renderChildren(mask, false);

      
      canvas.restore();

      if (compositing)
         popLayer(obj, originalObjBBox);

      
      statePop();
   }


}
