package com.dong.baselib.svg;

public class CSSParseException extends Exception
{
   public CSSParseException(String msg)
   {
      super(msg);
   }

   public CSSParseException(String msg, Exception cause)
   {
      super(msg, cause);
   }
}
