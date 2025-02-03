package com.dong.baselib.svg;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.RectF;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class SVG {

    private static final String VERSION = "1.5";

    private SVGBase base;

    private SVG(SVGBase base) {
        this.base = base;
    }

    @SuppressWarnings("WeakerAccess")
    public static SVG getFromInputStream(InputStream is) throws SVGParseException {
        return new SVG(SVGBase.getFromInputStream(is));
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static SVG getFromString(String svg) throws SVGParseException {
        return new SVG(SVGBase.getFromString(svg));
    }

    @SuppressWarnings("WeakerAccess")
    public static SVG getFromResource(Context context, int resourceId) throws SVGParseException {
        return getFromResource(context.getResources(), resourceId);
    }

    @SuppressWarnings("WeakerAccess")
    public static SVG getFromResource(Resources resources, int resourceId) throws SVGParseException {
        return new SVG(SVGBase.getFromResource(resources, resourceId));
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static SVG getFromAsset(AssetManager assetManager, String filename) throws SVGParseException, IOException {
        return new SVG(SVGBase.getFromAsset(assetManager, filename));
    }

    public static android.graphics.Path parsePath(String pathDefinition) {
        return SVGBase.parsePath(pathDefinition);
    }

    @SuppressWarnings("unused")
    public static void setInternalEntitiesEnabled(boolean enable) {
        SVGBase.setInternalEntitiesEnabled(enable);
    }

    @SuppressWarnings("unused")
    public boolean isInternalEntitiesEnabled() {
        return base.isInternalEntitiesEnabled();
    }


    @SuppressWarnings("unused")
    public static void registerExternalFileResolver(SVGExternalFileResolver fileResolver) {
        SVGBase.registerExternalFileResolver(fileResolver);
    }

    @SuppressWarnings("unused")
    public static void deregisterExternalFileResolver() {
        SVGBase.deregisterExternalFileResolver();
    }

    @SuppressWarnings("unused")
    public SVGExternalFileResolver getExternalFileResolver() {
        return base.getExternalFileResolver();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setRenderDPI(float dpi) {
        base.setRenderDPI(dpi);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getRenderDPI() {
        return base.getRenderDPI();
    }


    @SuppressWarnings("WeakerAccess")
    public Picture renderToPicture() {
        return base.renderToPicture(null);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public Picture renderToPicture(int widthInPixels, int heightInPixels) {
        return renderToPicture(widthInPixels, heightInPixels, null);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public Picture renderToPicture(RenderOptions renderOptions) {
        return base.renderToPicture(renderOptions);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public Picture renderToPicture(int widthInPixels, int heightInPixels, RenderOptions renderOptions) {
        return base.renderToPicture(widthInPixels, heightInPixels, renderOptions);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public Picture renderViewToPicture(String viewId, int widthInPixels, int heightInPixels) {
        return base.renderViewToPicture(viewId, widthInPixels, heightInPixels);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderToCanvas(Canvas canvas) {
        renderToCanvas(canvas, (RenderOptions) null);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderToCanvas(Canvas canvas, RectF viewPort) {
        base.renderToCanvas(canvas, viewPort);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderToCanvas(Canvas canvas, RenderOptions renderOptions) {
        base.renderToCanvas(canvas, renderOptions);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderViewToCanvas(String viewId, Canvas canvas) {
        renderToCanvas(canvas, RenderOptions.create().view(viewId));
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderViewToCanvas(String viewId, Canvas canvas, RectF viewPort) {
        base.renderViewToCanvas(viewId, canvas, viewPort);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public static String getVersion() {
        return VERSION;
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public String getDocumentTitle() {
        return base.getDocumentTitle();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public String getDocumentDescription() {
        return base.getDocumentDescription();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public String getDocumentSVGVersion() {
        return base.getDocumentSVGVersion();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public Set<String> getViewList() {
        return base.getViewList();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getDocumentWidth() {
        return base.getDocumentWidth();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentWidth(float pixels) {
        base.setDocumentWidth(pixels);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentWidth(String value) throws SVGParseException {
        base.setDocumentWidth(value);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getDocumentHeight() {
        return base.getDocumentHeight();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentHeight(float pixels) {
        base.setDocumentHeight(pixels);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentHeight(String value) throws SVGParseException {
        base.setDocumentHeight(value);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentViewBox(float minX, float minY, float width, float height) {
        base.setDocumentViewBox(minX, minY, width, height);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public RectF getDocumentViewBox() {
        return base.getDocumentViewBox();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentPreserveAspectRatio(PreserveAspectRatio preserveAspectRatio) {
        base.setDocumentPreserveAspectRatio(preserveAspectRatio);
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public PreserveAspectRatio getDocumentPreserveAspectRatio() {
        return base.getDocumentPreserveAspectRatio();
    }


    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getDocumentAspectRatio() {
        return base.getDocumentAspectRatio();
    }


    SVGBase.Svg getRootElement() {
        return base.getRootElement();
    }
}
