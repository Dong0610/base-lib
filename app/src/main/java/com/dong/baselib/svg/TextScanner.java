
package com.dong.baselib.svg;


import java.util.Locale;


public class TextScanner
{
   final String  input;
   int           position = 0;
   int           inputLength;

   private  final NumberParser numberParser = new NumberParser();


   public TextScanner(String input)
   {
      this.input = input.trim();
      this.inputLength = this.input.length();
   }

   
   public boolean  empty()
   {
      return (position == inputLength);
   }

   boolean  isWhitespace(int c)
   {
      return (c==' ' || c=='\n' || c=='\r' || c =='\t');
   }

   public void  skipWhitespace()
   {
      while (position < inputLength) {
         if (!isWhitespace(input.charAt(position)))
            break;
         position++;
      }
   }

   boolean  isEOL(int c)
   {
      return (c=='\n' || c=='\r');
   }

   
   
   public boolean  skipCommaWhitespace()
   {
      skipWhitespace();
      if (position == inputLength)
         return false;
      if (!(input.charAt(position) == ','))
         return false;
      position++;
      skipWhitespace();
      return true;
   }


   public float  nextFloat()
   {
      float  val = numberParser.parseNumber(input, position, inputLength);
      if (!Float.isNaN(val))
         position = numberParser.getEndPos();
      return val;
   }

   
   float  possibleNextFloat()
   {
      skipCommaWhitespace();
      float  val = numberParser.parseNumber(input, position, inputLength);
      if (!Float.isNaN(val))
         position = numberParser.getEndPos();
      return val;
   }

   
   float  checkedNextFloat(float lastRead)
   {
      if (Float.isNaN(lastRead)) {
         return Float.NaN;
      }
      skipCommaWhitespace();
      return nextFloat();
   }

   float  checkedNextFloat(Boolean lastRead)
   {
      if (lastRead == null) {
         return Float.NaN;
      }
      skipCommaWhitespace();
      return nextFloat();
   }

   Integer  nextInteger(boolean withSign)
   {
      IntegerParser  ip = IntegerParser.parseInt(input, position, inputLength, withSign);
      if (ip == null)
         return null;
      position = ip.getEndPos();
      return ip.value();
   }

   
   Integer  nextChar()
   {
      if (position == inputLength)
         return null;
      return (int) input.charAt(position++);
   }

   SVGBase.Length nextLength()
   {
      float  scalar = nextFloat();
      if (Float.isNaN(scalar))
         return null;
      SVGBase.Unit unit = nextUnit();
      if (unit == null)
         return new SVGBase.Length(scalar, SVGBase.Unit.px);
      else
         return new SVGBase.Length(scalar, unit);
   }

   
   Boolean  nextFlag()
   {
      if (position == inputLength)
         return null;
      char  ch = input.charAt(position);
      if (ch == '0' || ch == '1') {
         position++;
         return (ch == '1');
      }
      return null;
   }

   
   Boolean  checkedNextFlag(Object lastRead)
   {
      if (lastRead == null) {
         return null;
      }
      skipCommaWhitespace();
      return nextFlag();
   }

   public boolean  consume(char ch)
   {
      boolean  found = (position < inputLength && input.charAt(position) == ch);
      if (found)
         position++;
      return found;
   }


   public boolean  consume(String str)
   {
      int  len = str.length();
      boolean  found = (position <= (inputLength - len) && input.substring(position,position+len).equals(str));
      if (found)
         position += len;
      return found;
   }


   
   int  advanceChar()
   {
      if (position == inputLength)
         return -1;
      position++;
      if (position < inputLength)
         return input.charAt(position);
      else
         return -1;
   }


   
   public String  nextToken()
   {
      return nextToken(' ', false);
   }

   
   public String  nextToken(char terminator)
   {
      return nextToken(terminator, false);
   }

   
   String  nextTokenWithWhitespace(char terminator)
   {
      return nextToken(terminator, true);
   }

   
   String  nextToken(char terminator, boolean allowWhitespace)
   {
      if (empty())
         return null;

      int  ch = input.charAt(position);
      if ((!allowWhitespace && isWhitespace(ch)) || ch == terminator)
         return null;

      int  start = position;
      ch = advanceChar();
      while (ch != -1) {
         if (ch == terminator)
            break;
         if (!allowWhitespace && isWhitespace(ch))
            break;
         ch = advanceChar();
      }
      return input.substring(start, position);
   }


   
   public String  nextWord()
   {
      if (empty())
         return null;
      int  start = position;

      int  ch = input.charAt(position);
      if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))
      {
         ch = advanceChar();
         while ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'))
            ch = advanceChar();
         return input.substring(start, position);
      }
      position = start;
      return null;
   }


   
   String  nextFunction()
   {
      if (empty())
         return null;
      int  start = position;

      int  ch = input.charAt(position);
      while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
         ch = advanceChar();
      int end = position;
      while (isWhitespace(ch))
         ch = advanceChar();
      if (ch == '(') {
         position++;
         return input.substring(start, end);
      }
      position = start;
      return null;
   }

   
   String  ahead()
   {
      int start = position;
      while (!empty() && !isWhitespace(input.charAt(position)))
         position++;
      String  str = input.substring(start, position);
      position = start;
      return str;
   }

   SVGBase.Unit nextUnit()
   {
      if (empty())
         return null;
      int  ch = input.charAt(position);
      if (ch == '%') {
         position++;
         return SVGBase.Unit.percent;
      }
      if (position > (inputLength - 2))
         return null;
      try {
         SVGBase.Unit result = SVGBase.Unit.valueOf(input.substring(position, position + 2).toLowerCase(Locale.US));
         position +=2;
         return result;
      } catch (IllegalArgumentException e) {
         return null;
      }
   }

   
   boolean  hasLetter()
   {
      if (position == inputLength)
         return false;
      char  ch = input.charAt(position);
      return ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'));
   }

   
   public String  nextQuotedString()
   {
      if (empty())
         return null;
      int  start = position;
      int  ch = input.charAt(position);
      int  endQuote = ch;
      if (ch != '\'' && ch!='"')
         return null;
      ch = advanceChar();
      while (ch != -1 && ch != endQuote)
         ch = advanceChar();
      if (ch == -1) {
         position = start;
         return null;
      }
      position++;
      return input.substring(start+1, position-1);
   }

   
   String  restOfText()
   {
      if (empty())
         return null;

      int  start = position;
      position = inputLength;
      return input.substring(start);
   }

}


