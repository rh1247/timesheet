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
import java.util.Calendar;
import java.util.Enumeration;

/**
 *
 */
public class FeiertagCommand extends AbstractCommand {
   
   /** Creates a new instance of FeiertagCommand */
   public FeiertagCommand(String name, char abrev) {
      super(name,abrev);
   }
   
   public void execute(String[] args) {
      Calendar cal = null;
      
      switch(args.length) {
         case 0:
            cal = Calendar.getInstance();
            break;
         case 1:
            try {
               cal = Timestamp.parse(args[1]);
            } catch(ParseException pe) {
               throw new IllegalArgumentException("Invalid timestamp '" + args[0] + "'" );
            }
            break;
         default:
            throw new IllegalArgumentException("Invalid number of arguments");
      }
      
      Year year = Year.getInstance(cal.get(Calendar.YEAR));
      Enumeration feiertage = year.getFeiertage();
      while(feiertage.hasMoreElements()) {
         String day = (String)feiertage.nextElement();
         System.out.println( day + " " + year.getFeiertag(day));
      }
      
   }

   public java.lang.String[] usage() {
      return new String [] { "<timestamp>", "print a holidays" };
   }
   
}
