

package com.dong.baselib.svg;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CSSTextScanner extends TextScanner
{
   static final Pattern PATTERN_BLOCK_COMMENTS = Pattern.compile("(?s)/\\*.*?\\*/");

   public CSSTextScanner(String input)
   {
      super(PATTERN_BLOCK_COMMENTS.matcher(input).replaceAll(""));  
   }

   
   public String  nextIdentifier()
   {
      int  end = scanForIdentifier();
      if (end == position)
         return null;
      String result = input.substring(position, end);
      position = end;
      return result;
   }


   
   
   
   
   
   
   
   
   
   
   
   
   
   
   

   private int  scanForIdentifier()
   {
      if (empty())
         return position;
      int  start = position;
      int  lastValidPos = position;

      int  ch = input.charAt(position);
      if (ch == '-')
         ch = advanceChar();
      
      if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch == '-') || (ch == '_') || (ch >= 0x80))
      {
         ch = advanceChar();
         
         while ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || (ch == '-') || (ch == '_') || (ch >= 0x80)) {
            ch = advanceChar();
         }
         lastValidPos = position;
      }
      position = start;
      return lastValidPos;
   }


   
   public List<CSSParser.Selector> nextSelectorGroup() throws CSSParseException
   {
      if (empty())
         return null;

      ArrayList<CSSParser.Selector> selectorGroup = new ArrayList<>(1);
      CSSParser.Selector selector = new CSSParser.Selector();

      while (!empty())
      {
         if (nextSimpleSelector(selector))
         {
            
            if (!skipCommaWhitespace())
               continue;  
            selectorGroup.add(selector);
            selector = new CSSParser.Selector();
         }
         else
            break;
      }
      if (!selector.isEmpty())
         selectorGroup.add(selector);
      return selectorGroup;
   }


   
   boolean  nextSimpleSelector(CSSParser.Selector selector) throws CSSParseException
   {
      if (empty())
         return false;

      int             start = position;
      CSSParser.Combinator combinator = null;
      CSSParser.SimpleSelector selectorPart = null;

      if (!selector.isEmpty())
      {
         if (consume('>')) {
            combinator = CSSParser.Combinator.CHILD;
            skipWhitespace();
         } else if (consume('+')) {
            combinator = CSSParser.Combinator.FOLLOWS;
            skipWhitespace();
         }
      }

      if (consume('*')) {
         selectorPart = new CSSParser.SimpleSelector(combinator, null);
      } else {
         String tag = nextIdentifier();
         if (tag != null) {
            selectorPart = new CSSParser.SimpleSelector(combinator, tag);
            selector.addedElement();
         }
      }

      while (!empty())
      {
         if (consume('.'))
         {
            
            if (selectorPart == null)
               selectorPart = new CSSParser.SimpleSelector(combinator, null);
            String  value = nextIdentifier();
            if (value == null)
               throw new CSSParseException("Invalid \".class\" simpleSelectors");
            selectorPart.addAttrib(CSSParser.CLASS, CSSParser.AttribOp.EQUALS, value);
            selector.addedAttributeOrPseudo();
            continue;
         }

         if (consume('#'))
         {
            
            if (selectorPart == null)
               selectorPart = new CSSParser.SimpleSelector(combinator, null);
            String  value = nextIdentifier();
            if (value == null)
               throw new CSSParseException("Invalid \"#id\" simpleSelectors");
            selectorPart.addAttrib(CSSParser.ID, CSSParser.AttribOp.EQUALS, value);
            selector.addedIdAttribute();
            continue;
         }

         
         if (consume('['))
         {
            if (selectorPart == null)
               selectorPart = new CSSParser.SimpleSelector(combinator, null);
            skipWhitespace();
            String  attrName = nextIdentifier();
            String  attrValue = null;
            if (attrName == null)
               throw new CSSParseException("Invalid attribute simpleSelectors");
            skipWhitespace();
            CSSParser.AttribOp op = null;
            if (consume('='))
               op = CSSParser.AttribOp.EQUALS;
            else if (consume("~="))
               op = CSSParser.AttribOp.INCLUDES;
            else if (consume("|="))
               op = CSSParser.AttribOp.DASHMATCH;
            if (op != null) {
               skipWhitespace();
               attrValue = nextAttribValue();
               if (attrValue == null)
                  throw new CSSParseException("Invalid attribute simpleSelectors");
               skipWhitespace();
            }
            if (!consume(']'))
               throw new CSSParseException("Invalid attribute simpleSelectors");
            selectorPart.addAttrib(attrName, (op == null) ? CSSParser.AttribOp.EXISTS : op, attrValue);
            selector.addedAttributeOrPseudo();
            continue;
         }

         if (consume(':'))
         {
            if (selectorPart == null)
               selectorPart = new CSSParser.SimpleSelector(combinator, null);
            parsePseudoClass(selector, selectorPart);
            continue;
         }

         break;
      }

      if (selectorPart != null)
      {
         selector.add(selectorPart);
         return true;
      }

      
      position = start;
      return false;
   }


   private static class  AnPlusB
   {
      final public int a;
      final public int b;

      AnPlusB(int a, int b) {
         this.a = a;
         this.b = b;
      }
   }


   private AnPlusB  nextAnPlusB()
   {
      if (empty())
         return null;

      int  start = position;

      if (!consume('('))
         return null;
      skipWhitespace();

      AnPlusB  result;
      if (consume("odd"))
         result = new AnPlusB(2, 1);
      else if (consume("even"))
         result = new AnPlusB(2, 0);
      else
      {
         
         
         int  aSign = 1,
               bSign = 1;
         if (consume('+')) {
            
         } else if (consume('-')) {
            bSign = -1;
         }
         
         IntegerParser a = null,
               b = IntegerParser.parseInt(input, position, inputLength, false);
         if (b != null)
            position = b.getEndPos();
         
         if (consume('n') || consume('N')) {
            a = (b != null) ? b : new IntegerParser(1, position);
            aSign = bSign;
            b = null;
            bSign = 1;
            skipWhitespace();
            
            boolean  hasB = consume('+');
            if (!hasB) {
               hasB = consume('-');
               if (hasB)
                  bSign = -1;
            }
            
            if (hasB) {
               skipWhitespace();
               b = IntegerParser.parseInt(input, position, inputLength, false);
               if (b != null) {
                  position = b.getEndPos();
               } else {
                  position = start;
                  return null;
               }
            }
         }
         
         result = new AnPlusB((a == null) ? 0 : aSign * a.value(),
               (b == null) ? 0 : bSign * b.value());
      }

      skipWhitespace();
      if (consume(')'))
         return result;

      position = start;
      return null;
   }


   
   private List<String>  nextIdentListParam()
   {
      if (empty())
         return null;

      int                start = position;
      ArrayList<String>  result = null;

      if (!consume('('))
         return null;
      skipWhitespace();

      do {
         String ident = nextIdentifier();
         if (ident == null) {
            position = start;
            return null;
         }
         if (result == null)
            result = new ArrayList<>();
         result.add(ident);
         skipWhitespace();
      } while (skipCommaWhitespace());

      if (consume(')'))
         return result;

      position = start;
      return null;
   }


   
   private List<CSSParser.Selector>  nextPseudoNotParam() throws CSSParseException
   {
      if (empty())
         return null;

      int  start = position;

      if (!consume('('))
         return null;
      skipWhitespace();

      
      List<CSSParser.Selector>  result = nextSelectorGroup();

      if (result == null) {
         position = start;
         return null;
      }

      if (!consume(')')) {
         position = start;
         return null;
      }

      
      for (CSSParser.Selector selector: result) {
         if (selector.simpleSelectors == null)
            break;
         for (CSSParser.SimpleSelector simpleSelector: selector.simpleSelectors) {
            if (simpleSelector.pseudos == null)
               break;
            for (CSSParser.PseudoClass pseudo: simpleSelector.pseudos) {
               if (pseudo instanceof CSSParser.PseudoClassNot)
                  return null;
            }
         }
      }

      return result;
   }


   
   private void  parsePseudoClass(CSSParser.Selector selector, CSSParser.SimpleSelector selectorPart) throws CSSParseException
   {
      

      String  ident = nextIdentifier();
      if (ident == null)
         throw new CSSParseException("Invalid pseudo class");

      CSSParser.PseudoClass pseudo;
      CSSParser.PseudoClassIdents identEnum = CSSParser.PseudoClassIdents.fromString(ident);
      switch (identEnum)
      {
         case first_child:
            pseudo = new CSSParser.PseudoClassAnPlusB(0, 1, true, false, null);
            selector.addedAttributeOrPseudo();
            break;

         case last_child:
            pseudo = new CSSParser.PseudoClassAnPlusB(0, 1, false, false, null);
            selector.addedAttributeOrPseudo();
            break;

         case only_child:
            pseudo = new CSSParser.PseudoClassOnlyChild(false, null);
            selector.addedAttributeOrPseudo();
            break;

         case first_of_type:
            pseudo = new CSSParser.PseudoClassAnPlusB(0, 1, true, true, selectorPart.tag);
            selector.addedAttributeOrPseudo();
            break;

         case last_of_type:
            pseudo = new CSSParser.PseudoClassAnPlusB(0, 1, false, true, selectorPart.tag);
            selector.addedAttributeOrPseudo();
            break;

         case only_of_type:
            pseudo = new CSSParser.PseudoClassOnlyChild(true, selectorPart.tag);
            selector.addedAttributeOrPseudo();
            break;

         case root:
            pseudo = new CSSParser.PseudoClassRoot();
            selector.addedAttributeOrPseudo();
            break;

         case empty:
            pseudo = new CSSParser.PseudoClassEmpty();
            selector.addedAttributeOrPseudo();
            break;

         case nth_child:
         case nth_last_child:
         case nth_of_type:
         case nth_last_of_type:
            boolean fromStart = identEnum == CSSParser.PseudoClassIdents.nth_child || identEnum == CSSParser.PseudoClassIdents.nth_of_type;
            boolean ofType    = identEnum == CSSParser.PseudoClassIdents.nth_of_type || identEnum == CSSParser.PseudoClassIdents.nth_last_of_type;
            AnPlusB  ab = nextAnPlusB();
            if (ab == null)
               throw new CSSParseException("Invalid or missing parameter section for pseudo class: " + ident);
            pseudo = new CSSParser.PseudoClassAnPlusB(ab.a, ab.b, fromStart, ofType, selectorPart.tag);
            selector.addedAttributeOrPseudo();
            break;

         case not:
            List<CSSParser.Selector>  notSelectorGroup = nextPseudoNotParam();
            if (notSelectorGroup == null)
               throw new CSSParseException("Invalid or missing parameter section for pseudo class: " + ident);
            pseudo = new CSSParser.PseudoClassNot(notSelectorGroup);
            selector.specificity = ((CSSParser.PseudoClassNot) pseudo).getSpecificity();
            break;

         case target:
            
            pseudo = new CSSParser.PseudoClassTarget();
            selector.addedAttributeOrPseudo();
            break;

         case lang:
            List<String>  langs = nextIdentListParam();
            pseudo = new CSSParser.PseudoClassNotSupported(ident);
            selector.addedAttributeOrPseudo();
            break;

         case link:
         case visited:
         case hover:
         case active:
         case focus:
         case enabled:
         case disabled:
         case checked:
         case indeterminate:
            pseudo = new CSSParser.PseudoClassNotSupported(ident);
            selector.addedAttributeOrPseudo();
            break;

         default:
            throw new CSSParseException("Unsupported pseudo class: " + ident);
      }


      selectorPart.addPseudo(pseudo);

   }


   
   private String  nextAttribValue()
   {
      if (empty())
         return null;

      String  result = nextQuotedString();
      if (result != null)
         return result;
      return nextIdentifier();
   }

   
   public String  nextPropertyValue()
   {
      if (empty())
         return null;
      int  start = position;
      int  lastValidPos = position;

      int  ch = input.charAt(position);
      while (ch != -1 && ch != ';' && ch != '}' && ch != '!' && !isEOL(ch)) {
         if (!isWhitespace(ch))  
            lastValidPos = position + 1;
         ch = advanceChar();
      }
      if (position > start)
         return input.substring(start, lastValidPos);
      position = start;
      return null;
   }

   
   public String  nextCSSString()
   {
      if (empty())
         return null;
      int  ch = input.charAt(position);
      int  endQuote = ch;
      if (ch != '\'' && ch != '"')
         return null;

      StringBuilder  sb = new StringBuilder();
      position++;
      ch = nextChar();
      while (ch != -1 && ch != endQuote)
      {
         if (ch == '\\') {
            
            ch = nextChar();
            if (ch == -1)    
               continue;
            if (ch == '\n' || ch == '\r' || ch == '\f') {  
               ch = nextChar();
               continue;     
            }
            int  hc = hexChar(ch);
            if (hc != -1) {
               int  codepoint = hc;
               for (int i=1; i<=5; i++) {
                  ch = nextChar();
                  hc = hexChar(ch);
                  if (hc == -1)
                     break;
                  codepoint = codepoint * 16 + hc;
               }
               sb.append((char) codepoint);
               continue;
            }
            
            
         }
         sb.append((char) ch);
         ch = nextChar();
      }
      return sb.toString();
   }


   private int  hexChar(int ch)
   {
      if (ch >= '0' && ch <= '9')
         return (ch - (int)'0');
      if (ch >= 'A' && ch <= 'F')
         return (ch - (int)'A') + 10;
      if (ch >= 'a' && ch <= 'f')
         return (ch - (int)'a') + 10;
      return -1;
   }


   
   public String  nextURL()
   {
      if (empty())
         return null;
      int  start = position;
      if (!consume("url("))
         return null;

      skipWhitespace();

      String url = nextCSSString();
      if (url == null)
         url = nextLegacyURL();  

      if (url == null) {
         position = start;
         return null;
      }

      skipWhitespace();

      if (empty() || consume(")"))
         return url;

      position = start;
      return null;
   }


   
   String  nextLegacyURL()
   {
      StringBuilder  sb = new StringBuilder();

      while (!empty())
      {
         int  ch = input.charAt(position);

         if (ch == '\'' || ch == '"' || ch == '(' || ch == ')' || isWhitespace(ch) || Character.isISOControl(ch))
            break;

         position++;
         if (ch == '\\')
         {
            if (empty())    
               continue;
            
            ch = input.charAt(position++);
            if (ch == '\n' || ch == '\r' || ch == '\f') {  
               continue;     
            }
            int  hc = hexChar(ch);
            if (hc != -1) {
               int  codepoint = hc;
               for (int i=1; i<=5; i++) {
                  if (empty())
                     break;
                  hc = hexChar( input.charAt(position) );
                  if (hc == -1)  
                     break;
                  position++;
                  codepoint = codepoint * 16 + hc;
               }
               sb.append((char) codepoint);
               continue;
            }
            
            
         }
         sb.append((char) ch);
      }
      if (sb.length() == 0)
         return null;
      return sb.toString();
   }
}
