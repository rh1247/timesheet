/**
 * Copyright (c) 2010 Richard Hierlmeier
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.hierlmeier.timesheet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 *
 * @author  richard.hierlmeier@sun.com
 */
public class Timestamp {

   private final static TSFormat [] FORMATS = {
      new TSFormat( "mm"         , "[0-5][0-9]", Calendar.MINUTE, Calendar.MINUTE )
    , new TSFormat( "HH:mm"      , "[0-2][0-9]:[0-5][0-9]", Calendar.HOUR_OF_DAY, Calendar.MINUTE )
    , new TSFormat( "HH:mm:ss"   , "[0-2][0-9]:[0-5][0-9]:[0-5][0-9]", Calendar.HOUR_OF_DAY, Calendar.SECOND )
    , new TSFormat( "dd HH:mm"   , "[0-3][0-9] [0-2][0-9]:[0-5][0-9]", Calendar.DAY_OF_MONTH, Calendar.MINUTE )
    , new TSFormat( "dd HH:mm:ss"  , "[0-3][0-9] [0-2][0-9]:[0-5][0-9]:[0-5][0-9]", Calendar.DAY_OF_MONTH, Calendar.SECOND )
    , new TSFormat( "MM/dd HH:mm", "[0-1][0-9]/[0-3][0-9] [0-2][0-9]:[0-5][0-9]", Calendar.MONDAY, Calendar.MINUTE)
    , new TSFormat( "MM/dd HH:mm:ss", "[0-1][0-9]/[0-3][0-9] [0-2][0-9]:[0-5][0-9]:[0-5][0-9]", Calendar.MONDAY, Calendar.SECOND)
    , new TSFormat( "yyyy/MM/dd HH:mm", "[0-9]{4}/[0-1][0-9]/[0-3][0-9] [0-2][0-9]:[0-5][0-9]", Calendar.YEAR, Calendar.MINUTE)
    , new TSFormat( "yyyy/MM/dd HH:mm:ss", "[0-9]{4}/[0-1][0-9]/[0-3][0-9] [0-2][0-9]:[0-5][0-9]:[0-5][0-9]", Calendar.YEAR, Calendar.SECOND)
    , new WeekFormat( "w[0-9]{1,2}")
    , new MonthFormat( "(m[0-1]{0,1}[0-9]|jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)")
   };
   
   /**
    * Gets the available formats
    * @return formats
    */
   public static String [] getFormats() {
      String [] ret = new String[FORMATS.length];
      for(int i = 0; i < FORMATS.length; i++ ) {
         ret[i] = FORMATS[i].formatStr;
      }
      return ret;
   }
   
   /** Creates a new instance of Timestamp */
   public static Calendar parse(String str) throws ParseException {
   
      for(int i = 0; i <FORMATS.length; i++)  {
         if(FORMATS[i].matches(str)) {            
            return FORMATS[i].parse(str);        
         }
      }
      throw new ParseException("No time format for '" + str + "' found", 0);
   }
   
   /**
    * Creates a new Format
    */
   static class TSFormat {
      
       
       private final static int[] FIELDS = {
           Calendar.YEAR, // 0
           Calendar.MONTH, // 1
           Calendar.DAY_OF_MONTH, // 2
           Calendar.HOUR_OF_DAY, // 3
           Calendar.MINUTE, // 4
           Calendar.SECOND, // 5
           Calendar.MILLISECOND        // 6
       };
   
      private String formatStr;
      private SimpleDateFormat df;
      
      private int firstSetField;
      private int lastSetField;
      private Pattern matchExpr;
      
      public TSFormat(String formatStr, String matchExpr,  int firstSetField, int lastSetField) {
         
         this.formatStr = formatStr;
         this.firstSetField = firstSetField;
         this.lastSetField = lastSetField;
         this.matchExpr = Pattern.compile(matchExpr);
      }
      
      protected int[] getFields() {
          return FIELDS;
      }
      
      public boolean matches(String str) {
          return matchExpr.matcher(str).matches();
      }
      
      public Calendar parse(String str) throws ParseException {
         
         if(df == null ) {
            df = new SimpleDateFormat(formatStr);
         }
         Date d = df.parse(str);
         Calendar cal = Calendar.getInstance();
         cal.setTime(d);
         
         Calendar ret = Calendar.getInstance();
         
         // 0 = leave, 1 = set, 2 = reset
         int mode = 0;
         int [] fields = getFields();
         
         for(int i = 0; i < fields.length; i++ ) {
            
            if( fields[i] == firstSetField ) {
               mode = 1;
            }
            
            switch(mode) {
               case 0: // do nothing
                  break;
               case 1: // set field
                  ret.set(fields[i], cal.get(FIELDS[i]));   
                  break;
               case 2: // reset field
                  ret.set(fields[i], cal.getMinimum(FIELDS[i]));
                  break;
            }
            
            if( FIELDS[i] == lastSetField ) {
               mode  = 2;
            }
         }         
         return ret;
      }      
   }
   
   static class WeekFormat extends TSFormat {
       
       public WeekFormat(String matchExpr) {
           super("w[0-9][0-9]", matchExpr, 0, 0);
       }

        @Override
        public Calendar parse(String str) throws ParseException {
            String weekStr = str.substring(1);
            
            Integer week = Integer.parseInt(weekStr);
            
            Calendar ret = Calendar.getInstance();
            
            ret.set(Calendar.WEEK_OF_YEAR, week);
            return ret;
        }
   }
   static class MonthFormat extends TSFormat {
       
       public MonthFormat(String matchExpr) {
           super("m[0-1][0-9]|jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec", matchExpr, 0, 0);
       }

       private static String MONTH_NAMES [] = {
           "jan",
           "feb",
           "mar",
           "apr",
           "may",
           "jun",
           "jul",
           "aug",
           "sep",
           "oct",
           "nov",
           "dec"
       };
       
        @Override
        public Calendar parse(String str) throws ParseException {
            
            Integer month = null;
            for(int i=0; i < MONTH_NAMES.length; i++) {
                if(MONTH_NAMES[i].equalsIgnoreCase(str)) {
                    month = new Integer(i);
                    break;
                }
                
            }
            if (month == null) {
                String monthStr = str.substring(1);
                month = Integer.parseInt(monthStr) - 1;
                if (month < 0 || month > 12) {
                    throw new IllegalArgumentException("Unknown month " + monthStr);
                }
            }
            
            Calendar ret = Calendar.getInstance();
            
            ret.set(Calendar.MONTH, month);
            return ret;
        }
   }
}
