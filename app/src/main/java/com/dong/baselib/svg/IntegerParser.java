

package com.dong.baselib.svg;



class IntegerParser
{
   private final int      pos;
   private final long     value;


   IntegerParser(long value, int pos)
   {
      this.value = value;
      this.pos = pos;
   }


   
   int  getEndPos()
   {
      return this.pos;
   }


   
   static IntegerParser  parseInt(String input, int startpos, int len, boolean includeSign)
   {
      int      pos = startpos;
      boolean  isNegative = false;
      long     value = 0;
      char     ch;

      if (pos >= len)
        return null;  

      if (includeSign)
      {
         ch = input.charAt(pos);
         switch (ch) {
            case '-': isNegative = true;
               
            case '+': pos++;
         }
      }
      int  sigStart = pos;

      while (pos < len)
      {
         ch = input.charAt(pos);
         if (ch >= '0' && ch <= '9')
         {
            if (isNegative) {
               value = value * 10 - ((int)ch - (int)'0');
               if (value < Integer.MIN_VALUE)
                  return null;
            } else {
               value = value * 10 + ((int)ch - (int)'0');
               if (value > Integer.MAX_VALUE)
                  return null;
            }
         }
         else
            break;
         pos++;
      }

      
      if (pos == sigStart) {
         return null;
      }

      return new IntegerParser(value, pos);
   }


   
   public int  value()
   {
      return (int)value;
   }


   
   static IntegerParser  parseHex(String input, int startpos, int len)
   {
      int   pos = startpos;
      long  value = 0;
      char  ch;


      if (pos >= len)
        return null;  

      while (pos < len)
      {
         ch = input.charAt(pos);
         if (ch >= '0' && ch <= '9')
         {
            value = value * 16 + ((int)ch - (int)'0');
         }
         else if (ch >= 'A' && ch <= 'F')
         {
            value = value * 16 + ((int)ch - (int)'A') + 10;
         }
         else if (ch >= 'a' && ch <= 'f')
         {
            value = value * 16 + ((int)ch - (int)'a') + 10;
         }
         else
            break;

         if (value > 0xffffffffL)
            return null;

         pos++;
      }

      
      if (pos == startpos) {
         return null;
      }

      return new IntegerParser(value, pos);
   }

}
