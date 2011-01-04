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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  richard.hierlmeier@sun.com
 */
public class Util {
   
   private static File baseDir;
   
   public static File getBaseDir() {
      
      if(baseDir == null) {
         File userHome = new File(System.getProperty("user.home"));
         baseDir= new File(userHome,".timesheet");
         if( !baseDir.exists()) {
            if(!baseDir.mkdir()) {
               throw new IllegalStateException("Can not create directory " + baseDir);
            }
         }
      }
      return baseDir;
   }
   
    public static List<Entry> getEntries(Calendar start, Calendar end) {
        List<Entry> entries = new LinkedList<Entry>();
        int startIndex = start.get(Calendar.MONTH) + 12 * start.get(Calendar.YEAR);
        int endIndex = end.get(Calendar.MONTH) + 12 * end.get(Calendar.YEAR);
        for(int i = startIndex; i <= endIndex; i++) {
            Month month = new Month(i / 12, i % 12);
            try {
                month.read();
            } catch (IOException ex) {
                Logger.getLogger(WeeklyReportCommand.class.getName()).log(Level.SEVERE, "Can not read month" + month, ex);
            }
            for (Entry entry : month.getEntries()) {
                Date ts = entry.getTimestamp();
                if (ts.getTime() >= start.getTimeInMillis() && ts.getTime() < end.getTimeInMillis()) {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }
   
   
}
