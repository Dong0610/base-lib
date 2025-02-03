
package com.dong.baselib.svg;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.RectF;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SVGBase {


    private static final int DEFAULT_PICTURE_WIDTH = 512;
    private static final int DEFAULT_PICTURE_HEIGHT = 512;

    private static final double SQRT2 = 1.414213562373095;


    private static SVGExternalFileResolver externalFileResolverSingleton = null;
    private static boolean enableInternalEntitiesSingleton = true;


    private final SVGExternalFileResolver externalFileResolver;
    private final boolean enableInternalEntities;


    private Svg
            rootElement = null;


    private String title = "";
    private String desc = "";


    private float renderDPI = 96f;


    private final CSSParser.Ruleset cssRules = new CSSParser.Ruleset();


    private final Map<String, SvgElementBase> idToElementMap = new HashMap<>();


    enum Unit {
        px,
        em,
        ex,
        in,
        cm,
        mm,
        pt,
        pc,
        percent
    }


    @SuppressWarnings("unused")
    enum GradientSpread {
        pad,
        reflect,
        repeat
    }


    SVGBase(boolean enableInternalEntities, SVGExternalFileResolver fileResolver) {
        this.enableInternalEntities = enableInternalEntities;
        this.externalFileResolver = fileResolver;
    }


    @SuppressWarnings("WeakerAccess")
    public static SVGBase getFromInputStream(InputStream is) throws SVGParseException {
        return createParser().parseStream(is);
    }

    public static SVGBase getFromString(String svg) throws SVGParseException {
        return createParser().parseStream(new ByteArrayInputStream(svg.getBytes()));
    }

    public static SVGBase getFromResource(Context context, int resourceId) throws SVGParseException {
        return getFromResource(context.getResources(), resourceId);
    }


    public static SVGBase getFromResource(Resources resources, int resourceId) throws SVGParseException {
        InputStream is = resources.openRawResource(resourceId);

        try {
            return createParser().parseStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {

            }
        }
    }

    public static SVGBase getFromAsset(AssetManager assetManager, String filename) throws SVGParseException, IOException {
        InputStream is = assetManager.open(filename);

        try {
            return createParser().parseStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {

            }
        }
    }

    public static android.graphics.Path parsePath(String pathDefinition) {
        PathDefinition pathDef = SVGParserImpl.parsePath(pathDefinition);
        SVGAndroidRenderer.PathConverter pathConv = new SVGAndroidRenderer.PathConverter(pathDef);
        return pathConv.getPath();
    }

    public static void setInternalEntitiesEnabled(boolean enable) {
        enableInternalEntitiesSingleton = enable;
    }

    public boolean isInternalEntitiesEnabled() {
        return enableInternalEntities;
    }


    @SuppressWarnings("unused")
    public static void registerExternalFileResolver(SVGExternalFileResolver fileResolver) {
        externalFileResolverSingleton = fileResolver;
    }


    @SuppressWarnings("unused")
    public static void deregisterExternalFileResolver() {
        externalFileResolverSingleton = null;
    }


    @SuppressWarnings("unused")
    public SVGExternalFileResolver getExternalFileResolver() {
        return externalFileResolver;
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setRenderDPI(float dpi) {
        this.renderDPI = dpi;
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getRenderDPI() {
        return renderDPI;
    }

    @SuppressWarnings("WeakerAccess")
    public Picture renderToPicture() {
        return renderToPicture(null);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Picture renderToPicture(int widthInPixels, int heightInPixels) {
        return renderToPicture(widthInPixels, heightInPixels, null);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Picture renderToPicture(RenderOptionsBase renderOptions) {
        Box viewBox = (renderOptions != null && renderOptions.hasViewBox()) ? renderOptions.viewBox
                : rootElement.viewBox;


        if (renderOptions != null && renderOptions.hasViewPort()) {
            float w = renderOptions.viewPort.maxX();
            float h = renderOptions.viewPort.maxY();
            return renderToPicture((int) Math.ceil(w), (int) Math.ceil(h), renderOptions);
        } else if (rootElement.width != null && rootElement.width.unit != Unit.percent &&
                rootElement.height != null && rootElement.height.unit != Unit.percent) {
            float w = rootElement.width.floatValue(this.renderDPI);
            float h = rootElement.height.floatValue(this.renderDPI);
            return renderToPicture((int) Math.ceil(w), (int) Math.ceil(h), renderOptions);
        } else if (rootElement.width != null && viewBox != null) {


            float w = rootElement.width.floatValue(this.renderDPI);
            float h = w * viewBox.height / viewBox.width;
            return renderToPicture((int) Math.ceil(w), (int) Math.ceil(h), renderOptions);
        } else if (rootElement.height != null && viewBox != null) {

            float h = rootElement.height.floatValue(this.renderDPI);
            float w = h * viewBox.width / viewBox.height;
            return renderToPicture((int) Math.ceil(w), (int) Math.ceil(h), renderOptions);
        } else {
            return renderToPicture(DEFAULT_PICTURE_WIDTH, DEFAULT_PICTURE_HEIGHT, renderOptions);
        }
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Picture renderToPicture(int widthInPixels, int heightInPixels, RenderOptionsBase renderOptions) {
        Picture picture = new Picture();
        Canvas canvas = picture.beginRecording(widthInPixels, heightInPixels);

        if (renderOptions == null || renderOptions.viewPort == null) {
            renderOptions = (renderOptions == null) ? new RenderOptionsBase() : new RenderOptionsBase(renderOptions);
            renderOptions.viewPort(0f, 0f, (float) widthInPixels, (float) heightInPixels);
        }

        SVGAndroidRenderer renderer = new SVGAndroidRenderer(canvas, this.renderDPI, externalFileResolver);

        renderer.renderDocument(this, renderOptions);

        picture.endRecording();
        return picture;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Picture renderViewToPicture(String viewId, int widthInPixels, int heightInPixels) {
        RenderOptions renderOptions = new RenderOptions();
        renderOptions.view(viewId)
                .viewPort(0f, 0f, (float) widthInPixels, (float) heightInPixels);


        Picture picture = new Picture();
        Canvas canvas = picture.beginRecording(widthInPixels, heightInPixels);

        SVGAndroidRenderer renderer = new SVGAndroidRenderer(canvas, this.renderDPI, externalFileResolver);

        renderer.renderDocument(this, renderOptions);

        picture.endRecording();
        return picture;
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderToCanvas(Canvas canvas) {
        renderToCanvas(canvas, (RenderOptions) null);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderToCanvas(Canvas canvas, RectF viewPort) {
        RenderOptions renderOptions = new RenderOptions();

        if (viewPort != null) {
            renderOptions.viewPort(viewPort.left, viewPort.top, viewPort.width(), viewPort.height());
        } else {
            renderOptions.viewPort(0f, 0f, (float) canvas.getWidth(), (float) canvas.getHeight());
        }

        SVGAndroidRenderer renderer = new SVGAndroidRenderer(canvas, this.renderDPI, externalFileResolver);

        renderer.renderDocument(this, renderOptions);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderToCanvas(Canvas canvas, RenderOptions renderOptions) {
        if (renderOptions == null)
            renderOptions = new RenderOptions();

        if (!renderOptions.hasViewPort()) {
            renderOptions.viewPort(0f, 0f, (float) canvas.getWidth(), (float) canvas.getHeight());
        }

        SVGAndroidRenderer renderer = new SVGAndroidRenderer(canvas, this.renderDPI, externalFileResolver);

        renderer.renderDocument(this, renderOptions);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderViewToCanvas(String viewId, Canvas canvas) {
        renderToCanvas(canvas, RenderOptions.create().view(viewId));
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderViewToCanvas(String viewId, Canvas canvas, RectF viewPort) {
        RenderOptions renderOptions = RenderOptions.create().view(viewId);

        if (viewPort != null) {
            renderOptions.viewPort(viewPort.left, viewPort.top, viewPort.width(), viewPort.height());
        }

        renderToCanvas(canvas, renderOptions);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public String getDocumentTitle() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return title;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public String getDocumentDescription() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return desc;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public String getDocumentSVGVersion() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return rootElement.version;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public Set<String> getViewList() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        List<SvgObject> viewElems = getElementsByTagName(View.NODE_NAME);

        Set<String> viewIds = new HashSet<>(viewElems.size());
        for (SvgObject elem : viewElems) {
            View view = (View) elem;
            if (view.id != null)
                viewIds.add(view.id);
            else
                Log.w("AndroidSVG", "getViewList(): found a <view> without an id attribute");
        }
        return viewIds;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getDocumentWidth() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return getDocumentDimensions(this.renderDPI).width;
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentWidth(float pixels) {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.width = new Length(pixels);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentWidth(String value) throws SVGParseException {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.width = SVGParserImpl.parseLength(value);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getDocumentHeight() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return getDocumentDimensions(this.renderDPI).height;
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentHeight(float pixels) {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.height = new Length(pixels);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentHeight(String value) throws SVGParseException {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.height = SVGParserImpl.parseLength(value);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentViewBox(float minX, float minY, float width, float height) {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.viewBox = new Box(minX, minY, width, height);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public RectF getDocumentViewBox() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        if (this.rootElement.viewBox == null)
            return null;

        return this.rootElement.viewBox.toRectF();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentPreserveAspectRatio(PreserveAspectRatio preserveAspectRatio) {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.preserveAspectRatio = preserveAspectRatio;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public PreserveAspectRatio getDocumentPreserveAspectRatio() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        if (this.rootElement.preserveAspectRatio == null)
            return null;

        return this.rootElement.preserveAspectRatio;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getDocumentAspectRatio() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        Length w = this.rootElement.width;
        Length h = this.rootElement.height;


        if (w != null && h != null && w.unit != Unit.percent && h.unit != Unit.percent) {
            if (w.isZero() || h.isZero())
                return -1f;
            return w.floatValue(this.renderDPI) / h.floatValue(this.renderDPI);
        }


        if (this.rootElement.viewBox != null && this.rootElement.viewBox.width != 0f && this.rootElement.viewBox.height != 0f) {
            return this.rootElement.viewBox.width / this.rootElement.viewBox.height;
        }


        return -1f;
    }


    protected static SVGParser createParser() {
        return new SVGParserImpl().setInternalEntitiesEnabled(enableInternalEntitiesSingleton)
                .setExternalFileResolver(externalFileResolverSingleton);
    }


    public Svg getRootElement() {
        return rootElement;
    }


    void setRootElement(Svg rootElement) {
        this.rootElement = rootElement;
    }


    SvgObject resolveIRI(String iri) {
        if (iri == null)
            return null;

        iri = cssQuotedString(iri);
        if (iri.length() > 1 && iri.startsWith("#")) {
            return getElementById(iri.substring(1));
        }
        return null;
    }


    private String cssQuotedString(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {

            str = str.substring(1, str.length() - 1).replace("\\\"", "\"");
        } else if (str.startsWith("'") && str.endsWith("'")) {

            str = str.substring(1, str.length() - 1).replace("\\'", "'");
        }

        return str.replace("\\\n", "").replace("\\A", "\n");
    }


    private Box getDocumentDimensions(float dpi) {
        Length w = this.rootElement.width;
        Length h = this.rootElement.height;

        if (w == null || w.isZero() || w.unit == Unit.percent || w.unit == Unit.em || w.unit == Unit.ex)
            return new Box(-1, -1, -1, -1);

        float wOut = w.floatValue(dpi);
        float hOut;

        if (h != null) {
            if (h.isZero() || h.unit == Unit.percent || h.unit == Unit.em || h.unit == Unit.ex) {
                return new Box(-1, -1, -1, -1);
            }
            hOut = h.floatValue(dpi);
        } else {


            if (this.rootElement.viewBox != null) {
                hOut = (wOut * this.rootElement.viewBox.height) / this.rootElement.viewBox.width;
            } else {
                hOut = wOut;
            }
        }
        return new Box(0, 0, wOut, hOut);
    }


    void addCSSRules(CSSParser.Ruleset ruleset) {
        this.cssRules.addAll(ruleset);
    }


    List<CSSParser.Rule> getCSSRules() {
        return this.cssRules.getRules();
    }


    boolean hasCSSRules() {
        return !this.cssRules.isEmpty();
    }


    void clearRenderCSSRules() {
        this.cssRules.removeFromSource(CSSParser.Source.RenderOptions);
    }


    static class Box {
        float minX, minY, width, height;

        Box(float minX, float minY, float width, float height) {
            this.minX = minX;
            this.minY = minY;
            this.width = width;
            this.height = height;
        }

        Box(Box copy) {
            this.minX = copy.minX;
            this.minY = copy.minY;
            this.width = copy.width;
            this.height = copy.height;
        }

        static Box fromLimits(float minX, float minY, float maxX, float maxY) {
            return new Box(minX, minY, maxX - minX, maxY - minY);
        }


        RectF toRectF() {
            return new RectF(minX, minY, maxX(), maxY());
        }

        float maxX() {
            return minX + width;
        }

        float maxY() {
            return minY + height;
        }

        void union(Box other) {
            if (other.minX < minX) minX = other.minX;
            if (other.minY < minY) minY = other.minY;
            if (other.maxX() > maxX()) width = other.maxX() - minX;
            if (other.maxY() > maxY()) height = other.maxY() - minY;
        }

        public String toString() {
            return "[" + minX + " " + minY + " " + width + " " + height + "]";
        }
    }


    public abstract static class SvgPaint implements Cloneable {
    }


    public static class Colour extends SvgPaint {
        final int colour;

        static final Colour BLACK = new Colour(0xff000000);
        static final Colour TRANSPARENT = new Colour(0);

        Colour(int val) {
            this.colour = val;
        }

        public String toString() {
            return String.format("#%08x", colour);
        }
    }


    static class CurrentColor extends SvgPaint {
        private final static CurrentColor instance = new CurrentColor();

        private CurrentColor() {
        }

        static CurrentColor getInstance() {
            return instance;
        }
    }


    static class PaintReference extends SvgPaint {
        final String href;
        final SvgPaint fallback;

        PaintReference(String href, SvgPaint fallback) {
            this.href = href;
            this.fallback = fallback;
        }

        public String toString() {
            return href + " " + fallback;
        }
    }


    public static class Length implements Cloneable {
        final float value;
        final Unit unit;

        final static Length ZERO = new Length(0f);
        final static Length PERCENT_100 = new Length(100f, Unit.percent);

        public Length(float value, Unit unit) {
            this.value = value;
            this.unit = unit;
        }

        public Length(float value) {
            this.value = value;
            this.unit = Unit.px;
        }

        float floatValue() {
            return value;
        }


        float floatValueX(SVGAndroidRenderer renderer) {
            switch (unit) {
                case em:
                    return value * renderer.getCurrentFontSize();
                case ex:
                    return value * renderer.getCurrentFontXHeight();
                case in:
                    return value * renderer.getDPI();
                case cm:
                    return value * renderer.getDPI() / 2.54f;
                case mm:
                    return value * renderer.getDPI() / 25.4f;
                case pt:
                    return value * renderer.getDPI() / 72f;
                case pc:
                    return value * renderer.getDPI() / 6f;
                case percent:
                    Box viewPortUser = renderer.getEffectiveViewPortInUserUnits();
                    if (viewPortUser == null)
                        return value;
                    return value * viewPortUser.width / 100f;
                case px:
                default:
                    return value;
            }
        }


        float floatValueY(SVGAndroidRenderer renderer) {
            if (unit == Unit.percent) {
                Box viewPortUser = renderer.getEffectiveViewPortInUserUnits();
                if (viewPortUser == null)
                    return value;
                return value * viewPortUser.height / 100f;
            }
            return floatValueX(renderer);
        }


        float floatValue(SVGAndroidRenderer renderer) {
            if (unit == Unit.percent) {
                Box viewPortUser = renderer.getEffectiveViewPortInUserUnits();
                if (viewPortUser == null)
                    return value;
                float w = viewPortUser.width;
                float h = viewPortUser.height;
                if (w == h)
                    return value * w / 100f;
                float n = (float) (Math.sqrt(w * w + h * h) / SQRT2);
                return value * n / 100f;
            }
            return floatValueX(renderer);
        }


        float floatValue(SVGAndroidRenderer renderer, float max) {
            if (unit == Unit.percent) {
                return value * max / 100f;
            }
            return floatValueX(renderer);
        }


        float floatValue(float dpi) {
            switch (unit) {
                case in:
                    return value * dpi;
                case cm:
                    return value * dpi / 2.54f;
                case mm:
                    return value * dpi / 25.4f;
                case pt:
                    return value * dpi / 72f;
                case pc:
                    return value * dpi / 6f;
                case px:
                case em:
                case ex:
                case percent:
                default:
                    return value;
            }
        }

        boolean isZero() {
            return value == 0f;
        }

        boolean isNegative() {
            return value < 0f;
        }

        @Override
        public String toString() {
            return String.valueOf(value) + unit;
        }
    }


    public static class CSSClipRect {
        final Length top;
        final Length right;
        final Length bottom;
        final Length left;

        CSSClipRect(Length top, Length right, Length bottom, Length left) {
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }
    }


    public static class SvgObject {
        SVGBase document;
        SvgContainer parent;

        String getNodeName() {
            return "";
        }
    }


    static abstract class SvgElementBase extends SvgObject {
        String id = null;
        Boolean spacePreserve = null;
        Style baseStyle = null;
        Style style = null;
        List<String> classNames = null;

        public String toString() {
            return this.getNodeName();
        }
    }


    static abstract class SvgElement extends SvgElementBase {
        Box boundingBox = null;
    }


    interface SvgConditional {
        void setRequiredFeatures(Set<String> features);

        Set<String> getRequiredFeatures();

        void setRequiredExtensions(String extensions);

        String getRequiredExtensions();

        void setSystemLanguage(Set<String> languages);

        Set<String> getSystemLanguage();

        void setRequiredFormats(Set<String> mimeTypes);

        Set<String> getRequiredFormats();

        void setRequiredFonts(Set<String> fontNames);

        Set<String> getRequiredFonts();
    }


    static abstract class SvgConditionalElement extends SvgElement implements SvgConditional {
        Set<String> requiredFeatures = null;
        String requiredExtensions = null;
        Set<String> systemLanguage = null;
        Set<String> requiredFormats = null;
        Set<String> requiredFonts = null;

        @Override
        public void setRequiredFeatures(Set<String> features) {
            this.requiredFeatures = features;
        }

        @Override
        public Set<String> getRequiredFeatures() {
            return this.requiredFeatures;
        }

        @Override
        public void setRequiredExtensions(String extensions) {
            this.requiredExtensions = extensions;
        }

        @Override
        public String getRequiredExtensions() {
            return this.requiredExtensions;
        }

        @Override
        public void setSystemLanguage(Set<String> languages) {
            this.systemLanguage = languages;
        }

        @Override
        public Set<String> getSystemLanguage() {
            return this.systemLanguage;
        }

        @Override
        public void setRequiredFormats(Set<String> mimeTypes) {
            this.requiredFormats = mimeTypes;
        }

        @Override
        public Set<String> getRequiredFormats() {
            return this.requiredFormats;
        }

        @Override
        public void setRequiredFonts(Set<String> fontNames) {
            this.requiredFonts = fontNames;
        }

        @Override
        public Set<String> getRequiredFonts() {
            return this.requiredFonts;
        }
    }


    public interface SvgContainer {
        List<SvgObject> getChildren();

        void addChild(SvgObject elem) throws SVGParseException;
    }


    public static abstract class SvgConditionalContainer extends SvgElement implements SvgContainer, SvgConditional {
        List<SvgObject> children = new ArrayList<>();

        Set<String> requiredFeatures = null;
        String requiredExtensions = null;
        Set<String> systemLanguage = null;
        Set<String> requiredFormats = null;
        Set<String> requiredFonts = null;

        @Override
        public List<SvgObject> getChildren() {
            return children;
        }

        @Override
        public void addChild(SvgObject elem) throws SVGParseException {
            children.add(elem);
        }

        @Override
        public void setRequiredFeatures(Set<String> features) {
            this.requiredFeatures = features;
        }

        @Override
        public Set<String> getRequiredFeatures() {
            return this.requiredFeatures;
        }

        @Override
        public void setRequiredExtensions(String extensions) {
            this.requiredExtensions = extensions;
        }

        @Override
        public String getRequiredExtensions() {
            return this.requiredExtensions;
        }

        @Override
        public void setSystemLanguage(Set<String> languages) {
            this.systemLanguage = languages;
        }

        @Override
        public Set<String> getSystemLanguage() {
            return null;
        }

        @Override
        public void setRequiredFormats(Set<String> mimeTypes) {
            this.requiredFormats = mimeTypes;
        }

        @Override
        public Set<String> getRequiredFormats() {
            return this.requiredFormats;
        }

        @Override
        public void setRequiredFonts(Set<String> fontNames) {
            this.requiredFonts = fontNames;
        }

        @Override
        public Set<String> getRequiredFonts() {
            return this.requiredFonts;
        }
    }


    interface HasTransform {
        void setTransform(Matrix matrix);
    }


    static abstract class SvgPreserveAspectRatioContainer extends SvgConditionalContainer {
        PreserveAspectRatio preserveAspectRatio = null;
    }


    static abstract class SvgViewBoxContainer extends SvgPreserveAspectRatioContainer {
        Box viewBox;
    }


    public static class Svg extends SvgViewBoxContainer {
        Length x;
        Length y;
        Length width;
        Length height;
        public String version;

        @Override
        String getNodeName() {
            return "svg";
        }
    }


    static class Group extends SvgConditionalContainer implements HasTransform {
        Matrix transform;

        @Override
        public void setTransform(Matrix transform) {
            this.transform = transform;
        }

        @Override
        String getNodeName() {
            return "group";
        }
    }


    interface NotDirectlyRendered {
    }


    static class Defs extends Group implements NotDirectlyRendered {
        @Override
        String getNodeName() {
            return "defs";
        }
    }


    static abstract class GraphicsElement extends SvgConditionalElement implements HasTransform {
        Matrix transform;

        @Override
        public void setTransform(Matrix transform) {
            this.transform = transform;
        }
    }


    static class A extends Group {
        String href;

        @Override
        String getNodeName() {
            return "a";
        }
    }


    static class Use extends Group {
        String href;
        Length x;
        Length y;
        Length width;
        Length height;

        @Override
        String getNodeName() {
            return "use";
        }
    }


    static class Path extends GraphicsElement {
        PathDefinition d;
        Float pathLength;

        @Override
        String getNodeName() {
            return "path";
        }
    }


    static class Rect extends GraphicsElement {
        Length x;
        Length y;
        Length width;
        Length height;
        Length rx;
        Length ry;

        @Override
        String getNodeName() {
            return "rect";
        }
    }


    static class Circle extends GraphicsElement {
        Length cx;
        Length cy;
        Length r;

        @Override
        String getNodeName() {
            return "circle";
        }
    }


    static class Ellipse extends GraphicsElement {
        Length cx;
        Length cy;
        Length rx;
        Length ry;

        @Override
        String getNodeName() {
            return "ellipse";
        }
    }


    static class Line extends GraphicsElement {
        Length x1;
        Length y1;
        Length x2;
        Length y2;

        @Override
        String getNodeName() {
            return "line";
        }
    }


    static class PolyLine extends GraphicsElement {
        float[] points;

        @Override
        String getNodeName() {
            return "polyline";
        }
    }


    static class Polygon extends PolyLine {
        @Override
        String getNodeName() {
            return "polygon";
        }
    }


    interface TextRoot {
    }


    interface TextChild {
        void setTextRoot(TextRoot obj);

        TextRoot getTextRoot();
    }


    public static abstract class TextContainer extends SvgConditionalContainer {
        @Override
        public void addChild(SvgObject elem) throws SVGParseException {
            if (elem instanceof TextChild)
                children.add(elem);
            else
                throw new SVGParseException("Text content elements cannot contain " + elem + " elements.");
        }
    }


    static abstract class TextPositionedContainer extends TextContainer {
        List<Length> x;
        List<Length> y;
        List<Length> dx;
        List<Length> dy;
    }


    static class Text extends TextPositionedContainer implements TextRoot, HasTransform {
        Matrix transform;

        @Override
        public void setTransform(Matrix transform) {
            this.transform = transform;
        }

        @Override
        String getNodeName() {
            return "text";
        }
    }


    static class TSpan extends TextPositionedContainer implements TextChild {
        private TextRoot textRoot;

        @Override
        public void setTextRoot(TextRoot obj) {
            this.textRoot = obj;
        }

        @Override
        public TextRoot getTextRoot() {
            return this.textRoot;
        }

        @Override
        String getNodeName() {
            return "tspan";
        }
    }


    public static class TextSequence extends SvgObject implements TextChild {
        String text;

        private TextRoot textRoot;

        public TextSequence(String text) {
            this.text = text;
        }

        public String toString() {
            return "TextChild: '" + text + "'";
        }

        @Override
        public void setTextRoot(TextRoot obj) {
            this.textRoot = obj;
        }

        @Override
        public TextRoot getTextRoot() {
            return this.textRoot;
        }
    }


    static class TRef extends TextContainer implements TextChild {
        String href;

        private TextRoot textRoot;

        @Override
        public void setTextRoot(TextRoot obj) {
            this.textRoot = obj;
        }

        @Override
        public TextRoot getTextRoot() {
            return this.textRoot;
        }

        @Override
        String getNodeName() {
            return "tref";
        }
    }


    static class TextPath extends TextContainer implements TextChild {
        String href;
        Length startOffset;

        private TextRoot textRoot;

        @Override
        public void setTextRoot(TextRoot obj) {
            this.textRoot = obj;
        }

        @Override
        public TextRoot getTextRoot() {
            return this.textRoot;
        }

        @Override
        String getNodeName() {
            return "textPath";
        }
    }


    static class Switch extends Group {
        @Override
        String getNodeName() {
            return "switch";
        }
    }


    static class Symbol extends SvgViewBoxContainer implements NotDirectlyRendered {
        @Override
        String getNodeName() {
            return "symbol";
        }
    }


    static class Marker extends SvgViewBoxContainer implements NotDirectlyRendered {
        boolean markerUnitsAreUser;
        Length refX;
        Length refY;
        Length markerWidth;
        Length markerHeight;
        Float orient;

        @Override
        String getNodeName() {
            return "marker";
        }
    }


    static abstract class GradientElement extends SvgElementBase implements SvgContainer {
        List<SvgObject> children = new ArrayList<>();

        Boolean gradientUnitsAreUser;
        Matrix gradientTransform;
        GradientSpread spreadMethod;
        String href;

        @Override
        public List<SvgObject> getChildren() {
            return children;
        }

        @Override
        public void addChild(SvgObject elem) throws SVGParseException {
            if (elem instanceof Stop)
                children.add(elem);
            else
                throw new SVGParseException("Gradient elements cannot contain " + elem + " elements.");
        }
    }


    static class Stop extends SvgElementBase implements SvgContainer {
        Float offset;


        @Override
        public List<SvgObject> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public void addChild(SvgObject elem) {
        }

        @Override
        String getNodeName() {
            return "stop";
        }
    }


    static class SvgLinearGradient extends GradientElement {
        Length x1;
        Length y1;
        Length x2;
        Length y2;

        @Override
        String getNodeName() {
            return "linearGradient";
        }
    }


    static class SvgRadialGradient extends GradientElement {
        Length cx;
        Length cy;
        Length r;
        Length fx;
        Length fy;
        Length fr;

        @Override
        String getNodeName() {
            return "radialGradient";
        }
    }


    static class ClipPath extends Group implements NotDirectlyRendered {
        static final String NODE_NAME = "clipPath";

        Boolean clipPathUnitsAreUser;

        @Override
        String getNodeName() {
            return NODE_NAME;
        }
    }


    static class Pattern extends SvgViewBoxContainer implements NotDirectlyRendered {
        Boolean patternUnitsAreUser;
        Boolean patternContentUnitsAreUser;
        Matrix patternTransform;
        Length x;
        Length y;
        Length width;
        Length height;
        String href;

        @Override
        String getNodeName() {
            return "pattern";
        }
    }


    static class Image extends SvgPreserveAspectRatioContainer implements HasTransform {
        String href;
        Length x;
        Length y;
        Length width;
        Length height;
        Matrix transform;

        @Override
        public void setTransform(Matrix transform) {
            this.transform = transform;
        }

        @Override
        String getNodeName() {
            return "image";
        }
    }


    static class View extends SvgViewBoxContainer implements NotDirectlyRendered {
        static final String NODE_NAME = "view";

        @Override
        String getNodeName() {
            return NODE_NAME;
        }
    }


    static class Mask extends SvgConditionalContainer implements NotDirectlyRendered {
        Boolean maskUnitsAreUser;
        Boolean maskContentUnitsAreUser;
        Length x;
        Length y;
        Length width;
        Length height;

        @Override
        String getNodeName() {
            return "mask";
        }
    }


    static class SolidColor extends SvgElementBase implements SvgContainer {


        @Override
        public List<SvgObject> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public void addChild(SvgObject elem) {
        }

        @Override
        String getNodeName() {
            return "solidColor";
        }
    }


    void setTitle(String title) {
        this.title = title;
    }


    void setDesc(String desc) {
        this.desc = desc;
    }


    interface PathInterface {
        void moveTo(float x, float y);

        void lineTo(float x, float y);

        void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3);

        void quadTo(float x1, float y1, float x2, float y2);

        void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y);

        void close();
    }


    static class PathDefinition implements PathInterface {
        private byte[] commands;
        private int commandsLength = 0;
        private float[] coords;
        private int coordsLength = 0;

        private static final byte MOVETO = 0;
        private static final byte LINETO = 1;
        private static final byte CUBICTO = 2;
        private static final byte QUADTO = 3;
        private static final byte ARCTO = 4;
        private static final byte CLOSE = 8;


        PathDefinition() {
            this.commands = new byte[8];
            this.coords = new float[16];
        }


        boolean isEmpty() {
            return commandsLength == 0;
        }


        private void addCommand(byte value) {
            if (commandsLength == commands.length) {
                byte[] newCommands = new byte[commands.length * 2];
                System.arraycopy(commands, 0, newCommands, 0, commands.length);
                commands = newCommands;
            }
            commands[commandsLength++] = value;
        }


        private void coordsEnsure(int num) {
            if (coords.length < (coordsLength + num)) {
                float[] newCoords = new float[coords.length * 2];
                System.arraycopy(coords, 0, newCoords, 0, coords.length);
                coords = newCoords;
            }
        }


        @Override
        public void moveTo(float x, float y) {
            addCommand(MOVETO);
            coordsEnsure(2);
            coords[coordsLength++] = x;
            coords[coordsLength++] = y;
        }


        @Override
        public void lineTo(float x, float y) {
            addCommand(LINETO);
            coordsEnsure(2);
            coords[coordsLength++] = x;
            coords[coordsLength++] = y;
        }


        @Override
        public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
            addCommand(CUBICTO);
            coordsEnsure(6);
            coords[coordsLength++] = x1;
            coords[coordsLength++] = y1;
            coords[coordsLength++] = x2;
            coords[coordsLength++] = y2;
            coords[coordsLength++] = x3;
            coords[coordsLength++] = y3;
        }


        @Override
        public void quadTo(float x1, float y1, float x2, float y2) {
            addCommand(QUADTO);
            coordsEnsure(4);
            coords[coordsLength++] = x1;
            coords[coordsLength++] = y1;
            coords[coordsLength++] = x2;
            coords[coordsLength++] = y2;
        }


        @Override
        public void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
            int arc = ARCTO | (largeArcFlag ? 2 : 0) | (sweepFlag ? 1 : 0);
            addCommand((byte) arc);
            coordsEnsure(5);
            coords[coordsLength++] = rx;
            coords[coordsLength++] = ry;
            coords[coordsLength++] = xAxisRotation;
            coords[coordsLength++] = x;
            coords[coordsLength++] = y;
        }


        @Override
        public void close() {
            addCommand(CLOSE);
        }


        void enumeratePath(PathInterface handler) {
            int coordsPos = 0;

            for (int commandPos = 0; commandPos < commandsLength; commandPos++) {
                byte command = commands[commandPos];
                switch (command) {
                    case MOVETO:
                        handler.moveTo(coords[coordsPos++], coords[coordsPos++]);
                        break;
                    case LINETO:
                        handler.lineTo(coords[coordsPos++], coords[coordsPos++]);
                        break;
                    case CUBICTO:
                        handler.cubicTo(coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], coords[coordsPos++]);
                        break;
                    case QUADTO:
                        handler.quadTo(coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], coords[coordsPos++]);
                        break;
                    case CLOSE:
                        handler.close();
                        break;
                    default:
                        boolean largeArcFlag = (command & 2) != 0;
                        boolean sweepFlag = (command & 1) != 0;
                        handler.arcTo(coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], largeArcFlag, sweepFlag, coords[coordsPos++], coords[coordsPos++]);
                }
            }
        }

    }


    SvgElementBase getElementById(String id) {
        if (id == null || id.length() == 0)
            return null;
        if (id.equals(rootElement.id))
            return rootElement;

        if (idToElementMap.containsKey(id))
            return idToElementMap.get(id);


        SvgElementBase result = getElementById(rootElement, id);
        idToElementMap.put(id, result);
        return result;
    }


    private SvgElementBase getElementById(SvgContainer obj, String id) {
        SvgElementBase elem = (SvgElementBase) obj;
        if (id.equals(elem.id))
            return elem;
        for (SvgObject child : obj.getChildren()) {
            if (!(child instanceof SvgElementBase))
                continue;
            SvgElementBase childElem = (SvgElementBase) child;
            if (id.equals(childElem.id))
                return childElem;
            if (child instanceof SvgContainer) {
                SvgElementBase found = getElementById((SvgContainer) child, id);
                if (found != null)
                    return found;
            }
        }
        return null;
    }


    private List<SvgObject> getElementsByTagName(String nodeName) {
        List<SvgObject> result = new ArrayList<>();


        getElementsByTagName(result, rootElement, nodeName);
        return result;
    }


    private void getElementsByTagName(List<SvgObject> result, SvgObject obj, String nodeName) {

        if (obj.getNodeName().equals(nodeName))
            result.add(obj);

        if (obj instanceof SvgContainer) {
            for (SvgObject child : ((SvgContainer) obj).getChildren())
                getElementsByTagName(result, child, nodeName);
        }
    }


}
