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
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class WeeklyReportCommand extends AbstractCommand {

    public WeeklyReportCommand(String name, char abrev) {
        super(name, abrev);
    }
    @Override
    public String[] usage() {
        return new String[]{
                    "[-t <timestamp>]",
                    "Display the weekly report"
                };
    }

    @Override
    public void execute(String[] args) {
         Calendar cal = null;

        switch (args.length) {
            case 0:
                cal = Calendar.getInstance();
                break;
            case 1:
                try {
                    cal = Timestamp.parse(args[0]);
                } catch (ParseException pe) {
                    throw new IllegalArgumentException("Invalid timestamp '" + args[0] + "'");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid number of arguments");
        }

        Calendar start = (Calendar)cal.clone();
        
        start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        
        Map<String,Double> taskMap = new HashMap<String,Double>();

        Entry lastEntry = null;
        int maxDescrLen = 0;
        for(Entry entry: Util.getEntries(start, cal)) {
            double delta = 0;
            if (lastEntry == null) {
                lastEntry = entry;
                continue;
            } else {
                delta = entry.getTimestamp().getTime() - lastEntry.getTimestamp().getTime();
                
                String descr = (lastEntry.getDescr() == null )? lastEntry.getTypeAsString() : lastEntry.getDescr();
                
                maxDescrLen = Math.max(maxDescrLen, descr.length());
                Double oldValue = taskMap.get(descr);
                
                if (oldValue == null) {
                    taskMap.put(descr, delta);
                } else {
                    taskMap.put(descr, oldValue + delta);
                }
                if (entry.isEnde()) {
                    lastEntry = null;
                } else {
                    lastEntry = entry;
                }
            }
        }
        
        String format = "%1$-" + maxDescrLen + "s %2$ 6.2f PH %3$ 6.2f PD\n";
        System.out.println("----------------------------------------------------");
        System.out.printf("Status report KW %d/%d\n", start.get(Calendar.WEEK_OF_YEAR), cal.get(Calendar.YEAR));
        System.out.println("----------------------------------------------------");
        System.err.printf("%-" + maxDescrLen + "s ATC\n", "Task");
        for(Map.Entry<String,Double> task: taskMap.entrySet()) {
            
            double hours = task.getValue() / (1000.0 * 60 * 60);
            double pd =  hours / 8;
            System.out.printf(format, task.getKey(), hours, pd);
            
        }
    }

}
