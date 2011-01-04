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

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

/**
 *
 */
public class DeleteCommand extends AbstractCommand {
   
   /** Creates a new instance of DeleteCommand */
   public DeleteCommand(String name, char abrev) {
      super(name, abrev);
   }
   
   public void execute(String[] args) {
      if( args.length != 1 ) {
         throw new IllegalArgumentException("Invalid number of arguments");
      }
      try {
         Calendar cal = Timestamp.parse(args[0]);
         
         Month month = new Month(cal);
         try {
            month.read();
         } catch(IOException ioe) {
            throw new IllegalStateException("Can not read month " + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.MONTH));
         }
         
         Entry entry = new Entry(cal.getTime(), 0, "");
         if( !month.getEntries().remove(entry)) {
            System.err.println("No entry found");
            System.exit(1);
         } else {
            try {
               month.write();
            } catch( IOException ioe ) {
               System.err.println("Can not save month: " + ioe.getMessage());
               System.exit(1);
            }
         }
      } catch(ParseException pe) {
         throw new IllegalArgumentException("Invalid timestamp");
      }
   }

   public java.lang.String[] usage() {
      return new String [] {
         "<timestamp>", "delete the entry specified by an timestamp"
      };
   }
   
}
