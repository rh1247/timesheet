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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 *
 * @author  richard.hierlmeier@sun.com
 */
public class Year {
   
   private Logger logger = Logger.getLogger("timesheet");
   
   private int year;
   private Properties feiertage;
   
   private static Map instanceMap = new HashMap();
   
   protected Year(Calendar cal) {
      this(cal.get(Calendar.YEAR));
   }
   
   
   /** Creates a new instance of Feiertag */
   public Year(int year) {
      this.year = year;
   }
   
   public static Year getInstance(int year) {
      Integer key = new Integer(year);
      Year ret = (Year)instanceMap.get(key);
      if(ret == null) {
         ret = new Year(year);
         instanceMap.put(key, ret);
      }
      return ret;
   }
   
   
   private void initFeiertage() {      
      if( feiertage == null ) {
         feiertage = new Properties();
         File file = new File(getDir(),"feiertage");
         if(file.exists()) {
            try {
               feiertage.load(new FileInputStream(file));
            } catch(IOException ioe) {
               throw new IllegalStateException("Can not read file " + file, ioe);
            }
         } else {
           logger.warning("No holidays defined");
         }
      }
   }
   
   public int getYear() {
      return year;
   }
   
   private File dir;
   
   public File getDir() {
      if(dir == null ) {
         File basedir = Util.getBaseDir();
         dir = new File(basedir, Integer.toString(year));
         if(!dir.exists()) {
            if(!dir.mkdir()) {
               throw new IllegalStateException("Can not create directory " + dir);
            }
         }
      }
      return dir;
   }
   
   public String getFeiertag(Calendar cal) {
      return getFeiertag( cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
   }
   
   public String getFeiertag(int month, int day) {      
      String dayStr = String.format("%1$02d.%2$02d", day, month+1);
      //System.out.println("getFeiertag: '" + dayStr + "'");
      return getFeiertag(dayStr);
   }
   
   public String getFeiertag(String day) {
      initFeiertage();
      return feiertage.getProperty(day);
   }
   
   public Enumeration getFeiertage() {
      initFeiertage();
      return feiertage.propertyNames();
   }
   
   private File statFile;
   public File getStatFile() {
       if(statFile == null) {
           statFile = new File(getDir(), "stat");
       }
       return statFile;
   }
   
   private MonthStatEntry [] entries = new MonthStatEntry[12];
   private boolean dirty = false;
   
   public void read() throws IOException {
       
       if(getStatFile().exists()) {
           BufferedReader br = new BufferedReader(new FileReader(getStatFile()));

           String line = null;

           while( (line = br.readLine()) != null) {
               MonthStatEntry entry = new MonthStatEntry(line);
               entries[entry.getMonth()] = entry;
           }       
       }
       
       for(int i = 0; i < entries.length; i++) {
           Month month = new Month(getYear(), i);
           if(entries[i] == null) {
               entries[i] = new MonthStatEntry();
               entries[i].setMonth(i);
           }
           if(entries[i].getTimestamp() < month.lastModified()) {
               month.read();
               month.calcStatistic();
               updateMonthStat(i, month.getMonthIst(), month.getMonthSoll());
               dirty = true;
           }
       }
       
   }
   
   public void write() throws IOException {
       if(dirty) {
           FileWriter fw = new FileWriter(getStatFile());
           PrintWriter pw = new PrintWriter(fw);
           for(MonthStatEntry entry: entries) {
                entry.write(pw);
           }
           pw.close();           
           dirty = false;
       }
   }
   
   public void updateMonthStat(int month, double ist, double soll) {       
       entries[month].setIst(ist);
       entries[month].setSoll(soll);
       entries[month].setTimestamp(System.currentTimeMillis());
   }
   
   public MonthStatEntry getMonthStat(int month) {
       return entries[month];
   }
   
   public double getDelta(Month month) {
       
       double ret = 0.0;
       for(int i = 0; i < month.getMonth(); i++) {
           ret += entries[i].getDelta();
       }
       return ret;
   }
   
   public double getIst(Month month) {
       double ret = 0.0;
       for(int i = 0; i < month.getMonth(); i++) {
           ret += entries[i].getIst();
       }
       return ret;
   }
   
   public double getSoll(Month month) {
       double ret = 0.0;
       for(int i = 0; i < month.getMonth(); i++) {
           ret += entries[i].getSoll();
       }
       return ret;
   }
}
