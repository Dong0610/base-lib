

package com.dong.baselib.svg;



class NumberParser
{
   private int  pos;


   
   int  getEndPos()
   {
      return this.pos;
   }


   
   float  parseNumber(String input, int startpos, int len)
   {
      boolean  isNegative = false;
      long     significand = 0;
      int      numDigits = 0;
      int      numLeadingZeroes = 0;
      int      numTrailingZeroes = 0;
      boolean  decimalSeen = false;
      int      sigStart;
      int      decimalPos = 0;
      int      exponent;

      final long     TOO_BIG_L = Long.MAX_VALUE / 10;
      final int      TOO_BIG_I = Integer.MAX_VALUE / 10;

      pos = startpos;

      if (pos >= len)
        return Float.NaN;  

      char  ch = input.charAt(pos);
      switch (ch) {
         case '-': isNegative = true;
                   
         case '+': pos++;
      }

      sigStart = pos;

      while (pos < len)
      {
         ch = input.charAt(pos);
         if (ch == '0')
         {
            if (numDigits == 0) {
               numLeadingZeroes++;
            } else {
               
               numTrailingZeroes++;
            }
         }
         else if (ch >= '1' && ch <= '9')
         {
            
            numDigits += numTrailingZeroes;
            while (numTrailingZeroes > 0) {
               if (significand > TOO_BIG_L) {
                  
                  return Float.NaN;
               }
               significand *= 10;
               numTrailingZeroes--;
            }

            if (significand > TOO_BIG_L) {
               
               
               return Float.NaN;
            }
            significand = significand * 10 + ((int)ch - (int)'0');
            numDigits++;
            
            if (significand < 0)
               return Float.NaN;  
         }
         else if (ch == '.')
         {
            if (decimalSeen) {
               
               break;
            }
            decimalPos = pos - sigStart;
            decimalSeen = true;
         }
         else
            break;
         pos++;
      }

      if (decimalSeen && pos == (decimalPos + 1)) {
         
         
         return Float.NaN;
      }

      
      if (numDigits == 0) {
         if (numLeadingZeroes == 0) {
            
            return Float.NaN;
         }
         
         
         numDigits = 1;
      }

      if (decimalSeen) {
         exponent = decimalPos - numLeadingZeroes - numDigits;
      } else {
         exponent = numTrailingZeroes;
      }

      
      if (pos < len)
      {
         ch = input.charAt(pos);
         if (ch == 'E' || ch == 'e')
         {
            boolean  expIsNegative = false;
            int      expVal = 0;
            boolean  abortExponent = false;

            pos++;
            if (pos == len) {
               
               
               return Float.NaN;
            }

            switch (input.charAt(pos)) {
               case '-': expIsNegative = true;
                  
               case '+': pos++;
                  break;
               case '0': case '1': case '2': case '3': case '4':
               case '5': case '6': case '7': case '8': case '9':
                   break; 
               default:
                  
                  
                  abortExponent = true;
                  pos--;  
            }

            if (!abortExponent)
            {
               int  expStart = pos;

               while (pos < len)
               {
                  ch = input.charAt(pos);
                  if (ch >= '0' && ch <= '9')
                  {
                     if (expVal > TOO_BIG_I) {
                        
                        
                        return Float.NaN;
                     }
                     expVal = expVal * 10 + ((int)ch - (int)'0');
                     pos++;
                  }
                  else
                     break;
               }

               
               if (pos == expStart) {
                  
                  return Float.NaN;
               }

               if (expIsNegative)
                  exponent -= expVal;
               else
                  exponent += expVal;
            }
         }
      }

      
      
      
      
      
      
      
      if ((exponent + numDigits) > 39 || (exponent + numDigits) < -44)
         return Float.NaN;

      float  f = (float) significand;

      if (significand != 0)
      {
         
         if (exponent > 0)
         {
            f *= positivePowersOf10[exponent];
         }
         else if (exponent < 0)
         {
            
            
            
            if (exponent < -38) {
               
               f *= 1e-20;
               exponent += 20;
            }
            
            f *= negativePowersOf10[-exponent];
         }
      }

      return (isNegative) ? -f : f;
   }


   private static final float[]  positivePowersOf10 = {
      1e0f,  1e1f,  1e2f,  1e3f,  1e4f,  1e5f,  1e6f,  1e7f,  1e8f,  1e9f,
      1e10f, 1e11f, 1e12f, 1e13f, 1e14f, 1e15f, 1e16f, 1e17f, 1e18f, 1e19f,
      1e20f, 1e21f, 1e22f, 1e23f, 1e24f, 1e25f, 1e26f, 1e27f, 1e28f, 1e29f,
      1e30f, 1e31f, 1e32f, 1e33f, 1e34f, 1e35f, 1e36f, 1e37f, 1e38f
   };
   private static final float[]  negativePowersOf10 = {
      1e0f,   1e-1f,  1e-2f,  1e-3f,  1e-4f,  1e-5f,  1e-6f,  1e-7f,  1e-8f,  1e-9f,
      1e-10f, 1e-11f, 1e-12f, 1e-13f, 1e-14f, 1e-15f, 1e-16f, 1e-17f, 1e-18f, 1e-19f,
      1e-20f, 1e-21f, 1e-22f, 1e-23f, 1e-24f, 1e-25f, 1e-26f, 1e-27f, 1e-28f, 1e-29f,
      1e-30f, 1e-31f, 1e-32f, 1e-33f, 1e-34f, 1e-35f, 1e-36f, 1e-37f, 1e-38f
   };

}
