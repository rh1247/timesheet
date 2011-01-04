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
import java.util.logging.Logger;

/**
 *
 */
public class MoveCommand extends AbstractCommand {
   
   private Logger logger = Logger.getLogger("timesheet");
   
   /** Creates a new instance of MoveCommand */
   public MoveCommand(String name, char abrev) {
      super(name, abrev);
   }

   public void execute(String[] args) {
      if( args.length != 2 ) {
         throw new IllegalArgumentException("Invalid number of arguments");
      }
      Calendar from = null;
      Calendar to   = null;
      try {
         from = Timestamp.parse(args[0]);
      } catch(ParseException pe) {
         throw new IllegalArgumentException("Invalid from timestamp '" + args[0] + "'");
      }

      try {
         to = Timestamp.parse(args[1]);
      } catch(ParseException pe) {
         throw new IllegalArgumentException("Invalid to timestamp '" + args[1] + "'");
      }
      
      Month fromMonth = new Month(from);
      try {
         fromMonth.read();
      } catch(IOException ioe) {
         throw new IllegalStateException("Can not read month " + from.get(Calendar.YEAR) + "_" + from.get(Calendar.MONTH));
      }
         
      
      Entry entry = fromMonth.getEntry(from);
      if(entry == null) {
         throw new IllegalArgumentException("Not entry with timestamp " + args[0] + " found");
      } 
      logger.fine("found entry " + entry);
      
      Month toMonth = new Month(to);
      
      if(toMonth.getMonth() == fromMonth.getMonth() &&
         toMonth.getYear() == toMonth.getYear() ) {
         logger.fine( args[0] + " and " + args[1] + " are in the same month");
         toMonth = fromMonth;
      } else {
         try {
            logger.fine( args[0] + " and " + args[1] + " are in the different month");
            toMonth.read();
         } catch(IOException ioe) {
            throw new IllegalStateException("Can not read month " + to.get(Calendar.YEAR) + "_" + to.get(Calendar.MONTH));
         }
      }
      
      fromMonth.getEntries().remove(entry);
      entry.setTimestamp(to.getTime());
      logger.fine("moved entry: " + entry);
      
      toMonth.getEntries().add(entry);
      
      
      
      try {
         toMonth.write();
      } catch( IOException ioe ) {
         System.err.println("Can not save month: " + ioe.getMessage());
         System.exit(1);
      }
      
      if(fromMonth != toMonth ) {
         try {
            fromMonth.write();
         } catch( IOException ioe ) {
            System.err.println("Can not save month: " + ioe.getMessage());
            System.exit(1);
         }
      }
   }

   public String[] usage() {
      return new String [] {
         "<from timestamp> <to timestamp>",
         "move an entry"
      };
   }
   
}
