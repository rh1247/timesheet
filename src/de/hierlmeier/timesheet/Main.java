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

/**
 *
 */
public class Main {
   
   private static AbstractCommand [] COMMANDS = {
      new AddCommand("add", 'a'),
      new ListCommand("list", 'l'),
      new StatCommand("stat", 's'),
      new FeiertagCommand("feiertag", 'f'),
      new DeleteCommand("delete", 'd'),
      new MoveCommand("move", 'm'),
      new WeeklyReportCommand("weekly_report", 'w')
   };
   
   private static AbstractCommand getCommand(String name) {
      
      if(name.length() == 1 ) {
         char abrev = name.charAt(0);
         for(AbstractCommand cmd : COMMANDS) {
            if(cmd.getAbrev() == abrev) {
               return cmd;
            } 
         }
      } else {
         for(AbstractCommand cmd : COMMANDS) {
            if(cmd.getName().equals(name)) {
               return cmd;
            } 
         }
      }
      usage("Unkonwn command " + name, 1);
      return null;
   }
   
   /** Creates a new instance of Main */
   public Main() {
   }
   
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      
      if( args.length < 1 ) {
         usage("Error: To few arguments", 1);
      }
      
      AbstractCommand cmd = getCommand(args[0]);
      String [] newArgs = new String[args.length-1];
      System.arraycopy(args, 1, newArgs, 0, args.length-1);
      try {
         cmd.execute(newArgs);
      } catch(Exception e) {
         System.err.println("Error: " + e.getMessage());
         e.printStackTrace();
         
         if(e instanceof IllegalArgumentException) {
            for(String s: cmd.usage()) {
                System.err.print("    ");
                System.err.println(s);
            }
         }
         System.exit(1);
      }      
      
   }
   
   private static void usage(String message, int exitCode ) {
      if (message != null ) {
         System.err.println(message);
      }
      
      System.err.println("timesheet <command> [<options>]");
      
      System.err.println("  Commands: ");
      
      for(AbstractCommand cmd: COMMANDS)  {
         String [] us = cmd.usage();
         boolean first = true;
         for(String s: us ) {
            if(first) {
               System.err.print("  ");
               System.err.print(cmd.getAbrev());
               first = false;
            } else {
               System.err.print("    ");
            }
            System.err.print(" ");
            System.err.println(s);
         }
      }
      System.err.println();
      System.err.println("   timestamp:");
      String [] formats = Timestamp.getFormats();
      for(int i = 0; i < formats.length; i++ ) {
         System.err.println("     " + formats[i]);
      }
      if( exitCode != 0 ) {
         System.exit(exitCode);
      }
   }
 
}


