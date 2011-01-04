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

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 *
 * @author  richard.hierlmeier@sun.com
 */
public class Entry implements Comparable {
   
   public static final int NOT_SET = 0x0000;
   public final static int WORK    = 0x0001;
   public final static int KRANK   = 0x0002;
   public final static int URLAUB  = 0x0003;
   public final static int PAUSE   = 0x0100;
   public final static int ENDE    = 0x0200;
   
   
   
   
   public final static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
   public final static MessageFormat mf = new MessageFormat("{0,date,yyyy-MM-dd HH:mm}|{1}|{2}");
   
   private Date timestamp;
   private int  type;
   private int  month;
   private int  year;
   private String descr;
   
   
   /** Creates a new instance of Entry */
   public Entry() {
   }
   
   public Entry(String line)  {
      StringTokenizer st = new StringTokenizer(line, "|" );
      if(st.countTokens() < 2 ) {
         throw new IllegalArgumentException("Invalid line");
      }
      try {
         timestamp = df.parse(st.nextToken());
      } catch( ParseException pe) {
         throw new IllegalArgumentException("Invalid timestamp in line");
      }
      type = strToType(st.nextToken());
      if(st.hasMoreTokens()) {
         descr = st.nextToken();
      }
   }
   
   public Entry(Date timestamp, int type, String descr) {
      this.setTimestamp(timestamp);
      this.setType(type);
      this.setDescr(descr);
   }
   
   public boolean isArbeit() {
      switch(type) {
         case KRANK: return true;
         case URLAUB: return true;
         case WORK: return true;
         default: return false;
      }
   }
   
   
   public boolean isUrlaub() {
      return type == URLAUB;
   }
   
   public boolean isEnde() {
      return type == ENDE;
   }

   public int compareTo(Object o) {
      Entry entry = (Entry)o;
      return this.getTimestamp().compareTo(entry.getTimestamp());
   }
   
   public boolean equals(Object obj) {
      return obj instanceof Entry &&
             timestamp.equals(((Entry)obj).timestamp);
   }
   
   public int hashcode() {
      return timestamp.hashCode();
   }

   public Date getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(Date timestamp) {
      this.timestamp = timestamp;
      Calendar cal = Calendar.getInstance();
      cal.setTime(timestamp);
      month = cal.get(Calendar.MONTH);
      year = cal.get(Calendar.YEAR);
   }
   
   public String toString() {      
      return mf.format(new Object[] { timestamp, typeToStr(type), descr == null ? "" : descr });
   }

   public int getType() {
      return type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public String getDescr() {
      return descr;
   }

   public void setDescr(String descr) {
      this.descr = descr;
   }
   
   public String getTypeAsString() {
       return typeToStr(type);
   }
   public static String typeToStr(int type) {
      switch(type) {
         case WORK: return "work";
         case PAUSE: return "pause";
         case ENDE: return "ende";
         case KRANK: return "krank";
         case URLAUB: return "urlaub";
         default:
           throw new IllegalArgumentException("Unknown type " + type );
      }
   }
   
   
   public static int strToType(String str) {
      if(str.equalsIgnoreCase("work")) {
         return WORK;
      } else if ( str.equalsIgnoreCase("pause") ) {
         return PAUSE;
      } else if ( str.equalsIgnoreCase("ende") ) {
         return ENDE;
      } else if ( str.equalsIgnoreCase("krank")) {
         return KRANK;
      } else if ( str.equalsIgnoreCase("urlaub")) {
         return URLAUB;
      } else {
         throw new IllegalArgumentException("Unknown type " + str);
      }
   }

   public int getMonth() {
       return month;
   }
   public int getYear() {
       return month;
   }
}
