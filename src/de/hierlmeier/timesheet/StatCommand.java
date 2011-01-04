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
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 */
public class StatCommand extends AbstractCommand {
    
    /** Creates a new instance of StatCommand */
    public StatCommand(String name, char abrev) {
        super(name, abrev);
    }
    
    public void execute(String[] args) {
        Calendar cal = null;
        
        switch(args.length) {
            case 0:
                cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
                cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
                cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
                break;
            case 1:
                try {
                    cal = Timestamp.parse(args[0]);
                } catch(ParseException pe) {
                    throw new IllegalArgumentException("Invalid timestamp '" + args[0] + "'" );
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid number of arguments");
        }
        
        
        Month month = new Month(cal);
        Year year = Year.getInstance(month.getYear());
        try {
            month.read();
            year.read();
        } catch(IOException ioe) {
            throw new IllegalStateException("Can not read month " + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.MONTH));
        }
        
        month.calcStatistic();
        
        int min = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        
        
        int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        SimpleDateFormat wdf = new SimpleDateFormat("MMMM yyyy");
        
        System.out.println("------------------------------------------------------");
        System.out.println(" " + wdf.format(cal.getTime()) );
        System.out.println("------------------------------------------------------");
        
        wdf = new SimpleDateFormat("E");
        
        System.out.println("    Day    Debit      Act    Delta  Description");
        for(int i = min; i <= max; i++ ) {
            cal.set(Calendar.DAY_OF_MONTH,i);
            int weekDay = cal.get(Calendar.DAY_OF_WEEK);
            String weekDayStr = wdf.format(cal.getTime());
            
            switch(weekDay) {
                case Calendar.SATURDAY:
                case Calendar.SUNDAY:
                    System.out.printf("%1$ 3d %2$s ", i, weekDayStr );
                    break;
                default:
                    String feiertag = year.getFeiertag(month.getMonth(),i);
                    if(feiertag == null ) {
                        if( Double.isNaN(month.getIst(i))) {
                            System.out.printf("%1$ 3d %2$s       --       --       --", i,  weekDayStr);
                        } else {
                            double diff = month.getIst(i) - month.getSoll(i);
                            String typeStr = "";
                            switch(month.getType(i)) {
                                case Entry.URLAUB:
                                    typeStr = "Urlaub"; break;
                                case Entry.KRANK:
                                    typeStr = "Krank"; break;
                                default:
                                    typeStr = "";
                            }
                            System.out.printf("%1$ 3d %2$s %3$ 8.2f %4$ 8.2f %5$ 8.2f  %6$s", i,  weekDayStr, month.getSoll(i), month.getIst(i), diff, typeStr);
                        }
                    } else {
                        System.out.printf("%1$ 3d %2$s                             %3$s", i, weekDayStr, feiertag );
                    }
            }
            System.out.println();
        }
        System.out.println("-----------------------------------");
        System.out.printf("   Sum: %1$ 8.2f %2$ 8.2f %3$ 8.2f",
                month.getMonthSoll(), month.getMonthIst(), month.getMonthIst() - month.getMonthSoll() );
        System.out.println();
        System.out.println("-----------------------------------");
        System.out.printf("  Year: %1$ 8.2f %2$ 8.2f %3$ 8.2f",
                year.getSoll(month) + month.getMonthSoll(), 
                year.getIst(month) + month.getMonthIst(), 
                year.getDelta(month) + month.getMonthIst() - month.getMonthSoll() );
        System.out.println();
        System.out.println("-----------------------------------");
        System.out.println("   Day    Debit    Act      Delta   Description");
        
        
        try {
            year.write();
        } catch (IOException ex) {
            throw new IllegalStateException("Can not write year statistic", ex);
        }
    }
    
    public java.lang.String[] usage() {
        return new String [] {
            "[<timestamp>]", "print statistic of a month"
        };
    }
    
}
