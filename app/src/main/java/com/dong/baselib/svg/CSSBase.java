package com.dong.baselib.svg;


public class CSSBase {
    protected CSSParser.Ruleset cssRuleset;

    protected CSSBase(String css) {
        this.cssRuleset = new CSSParser(CSSParser.Source.RenderOptions, null).parse(css);
    }
}
