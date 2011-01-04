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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Month {
   
   private Logger logger = Logger.getLogger("timesheet");
   
   private Set<Entry> entries = new TreeSet<Entry>();
   private int year;
   private int month;
   private double [] soll;
   private  double [] ist;
   private double monthSoll;
   private double monthIst;
   /** Urlaub, Work, Feiertag **/
   private int  [] type;
   
   /** Creates a new instance of Month
    * @param d 
    */
   public Month(Date d) {
       this(getCal(d));
   }
   
   private static Calendar getCal(Date d) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
       return cal;
   }
   public Month(Calendar cal) {
      this(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
   }
   
   public Month(int year, int month) {
      this.year = year;
      this.month = month;
   }
   
   @Override
   public String toString() {
       return getFilename();
   }
   
   public String getFilename() {
       return MessageFormat.format("{0,number,00}_{1,number,00}", getYear(), getMonth()+1 );
   }
   
   private File file;
   
   private File getFile() {
      if(file == null) {
         Year yearObj = Year.getInstance(year);
         file = new File(yearObj.getDir(), getFilename());
      }
      return file;
   }
   
   public long lastModified() {
       return getFile().lastModified();
   }
   
   public void read() throws IOException {
      entries.clear();
      File file  = getFile();
      if( file.exists() ) {
         FileReader fr = new FileReader(getFile());
         BufferedReader br = new BufferedReader(fr);

         String line = null;

         while( (line=br.readLine()) != null) {
            Entry entry = new Entry(line);
            entries.add(entry);
         }
      }
   }
   
   public void write() throws IOException {
      
      File file  = getFile();
      FileWriter fw = new FileWriter(file);
      PrintWriter pw = new PrintWriter(fw);
      
      for(Entry entry: entries) {         
         pw.println(entry);
      }
      
      pw.flush();
      pw.close();
   }
   
   public void calcStatistic() {
      
      Calendar cal = Calendar.getInstance();
      cal.set(getYear(), getMonth(), cal.getMinimum(Calendar.DAY_OF_MONTH));
      
      Calendar cal1 = Calendar.getInstance();
      
      
      int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
      int min = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
      
      Iterator iter = entries.iterator();
      int weekDay = 0;
      
      ArrayList dayEntries = new ArrayList();
      
      Entry entry = null;
      if(iter.hasNext()) {
         entry = (Entry)iter.next();
         cal1.setTime(entry.getTimestamp());
      }
      
      boolean firstEntry = true;
      
      soll = new double[max+2]; 
      ist  = new double[max+2];
      type = new int[max+2];
      
      Year yearObj = Year.getInstance(year);

      if (logger.isLoggable(Level.FINE)) {
         logger.fine("Calculate from " + min + " to " + max );
      }
      for(int day = min;  day <= max;  day++) {
         
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("Calculate day " + day);
         }
         
         
         // read all entries for the current day from the 
         // entry iterator and store it in dayEntries
         dayEntries.clear();
         
         while(true) {
            if(entry == null ) {
               break;
            }
            cal1.setTime(entry.getTimestamp());
            if( cal1.get(Calendar.DAY_OF_MONTH) != day ) {
               break;
            }
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("entry " + entry + " belongs to day " + day);
            }            
            dayEntries.add(entry);
            if(iter.hasNext()) {
               entry = (Entry)iter.next();
               firstEntry = false;
            } else {
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine("no more entries");
               }            
               entry = null;
               break;
            }
         }
         
         // Calculate soll and monthSoll
         // It depends on the weekday (on weekend soll = 0)
         // and on public holidays
         
         cal.set(Calendar.DAY_OF_MONTH, day);
         weekDay = cal.get(Calendar.DAY_OF_WEEK);
         
         if( weekDay == Calendar.SUNDAY ||
             weekDay == Calendar.SATURDAY ||
             yearObj.getFeiertag(month,day) != null ) {
             soll[day] = 0;
         } else {
            if(firstEntry) {
               soll[day] = 0; 
            } else {
                soll[day] = 8.0;
            }
         }
         
         
         // Go through the day entries and calculate 
         // ist for the day
         
         if( dayEntries.isEmpty() ) {
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("day " + day + " has no entries");
            }            
            ist[day] = Double.NaN;
         } else {
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("day " + day + " has " + dayEntries.size() + " entries");
            }            
            double sum = 0;
            Iterator dayIter = dayEntries.iterator();
            Entry dayEntry = null;
            Calendar dayCal = Calendar.getInstance();
            double startTime = -1;
            boolean hasStart = false;
            boolean hasEnde  = false;
            double time= 0;

            type[day] = Entry.NOT_SET;
            
            while(dayIter.hasNext()) {
               dayEntry = (Entry)dayIter.next();
               dayCal.setTime(dayEntry.getTimestamp());            
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine("entry " + dayEntry + " isArbeit ? " + dayEntry.isArbeit() );
               }
               time = dayCal.get(Calendar.HOUR_OF_DAY) + (double)dayCal.get(Calendar.MINUTE) / 60.0;
               
               if( hasStart ) {
                  sum += time - startTime;
                   
                  if( dayEntry.isArbeit() ) {                     
                     hasStart = true;
                     startTime = time;
                  } else {
                     hasStart = false;
                  }
                  
                  if(dayEntry.isEnde() ) {
                     hasEnde = true;
                  }
                  
               } else if ( dayEntry.isArbeit() ) {
                  hasStart = true;
                  startTime = time;
               } else {
                  hasStart = false;
               }
               
               if (type[day] == Entry.NOT_SET) {
                  switch(dayEntry.getType()) {
                     case Entry.URLAUB:
                        type[day] = Entry.URLAUB;
                        break;
                     case Entry.KRANK:
                        type[day] = Entry.KRANK;
                        break;
                     default:
                        type[day] = Entry.NOT_SET;       
                  }
               }
            }

            if( hasStart || !hasEnde ) {
               ist[day] = Double.NaN;
            } else {
               ist[day] = sum;
               monthIst += ist[day];
               monthSoll += soll[day];
            }
         }
      }
      
   }
   
   public double getSoll(int day) {
      return soll[day];
   }
   
   public double getIst(int day) {
      return ist[day];
   }
   
   public int getType(int day) {
      return type[day];
   }
   
   public double getMonthSoll() {
      return monthSoll;
   }
   
   public double getMonthIst() {
      return monthIst;
   }

   
   
   public Set<Entry> getEntries() {
      return entries;
   }
   
   public void setEntries(Set<Entry> entries) {
       this.entries = entries;
   }
   
   public Entry getEntry(Calendar cal) {
      Date date = cal.getTime();
      for(Entry entry: entries) {
         if(entry.getTimestamp().equals(date)) {
            return entry;
         }
      }
      return null;
   }

   public int getYear() {
      return year;
   }

   public int getMonth() {
      return month;
   }
   
}
