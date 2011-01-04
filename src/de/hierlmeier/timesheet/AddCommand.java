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
 * @author  richard.hierlmeier@sun.com
 */
public class AddCommand extends AbstractCommand {
   
   /** Creates a new instance of AddCommand */
   public AddCommand(String name, char abrev) {
      super(name, abrev);
   }
   
   public void execute(String[] args) {
      String timestamp = null;
      String typeStr = null;
      StringBuffer description = new StringBuffer();
      
      for(int i = 0; i < args.length; i++ ) {
         
         if(args[i].equals("-t")) {
            i++;
            if(i >= args.length ) {
               throw new IllegalArgumentException("missing timestamp");
            }
            timestamp = args[i];
         } else if ( typeStr == null ) {
            typeStr = args[i];
         } else {
            if( description.length() > 0 ) {
               description.append(' ');
            }
            description.append(args[i]);
         }
      }
      
      if( typeStr == null ) {
         throw new IllegalArgumentException("No type specified");
      }
      
      int type = Entry.strToType(typeStr);
      
      Calendar cal = null;
      if( timestamp != null ) {
         try {
            cal = Timestamp.parse(timestamp);
         } catch(ParseException pe) {
            throw new IllegalArgumentException("Invalid timestamp '" + timestamp + "'");
         }
      } else {
         cal = Calendar.getInstance();
      }
      
      Month month = new Month(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
      try {
         month.read();
      } catch(IOException ioe) {
         throw new IllegalArgumentException("Can not read month " + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.MONTH));
      }
      
      Entry entry = new Entry(cal.getTime(), type, description.toString());
      
      month.getEntries().remove(entry);
      
      month.getEntries().add(entry);
      
      try {
         month.write();
      } catch( IOException ioe ) {
         throw new IllegalStateException("Can not save month", ioe);
      }
   }

   
   public java.lang.String[] usage() {
      return new String [] {
         "[-t <timestamp>] <type> [<description>]",
         "Add an entry"
      };
   }
   
}
