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
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author  richard.hierlmeier@sun.com
 */
public class ListCommand extends AbstractCommand {

    /** Creates a new instance of ListCommand */
    public ListCommand(String name, char abrev) {
        super(name, abrev);
    }

    public void execute(String[] args) {
        Calendar cal = null;
        switch (args.length) {
            case 0:
                cal = Calendar.getInstance();
                break;
            case 1:
                try {
                    cal = Timestamp.parse(args[0]);
                    //System.out.println("Use arg " + args[0] + " --> " + cal.getTime());
                } catch (ParseException pe) {
                    throw new IllegalArgumentException("Invalid timestamp '" + args[0] + "'");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid number of arguments");
        }

        Calendar start = (Calendar) cal.clone();

        start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.WEEK_OF_MONTH, 1);

        //System.out.println("Start: " + start.getTime());
        //System.out.println("End: " + end.getTime());
        
        int lastDay = -1;
        int lastMonth = -1;
        SimpleDateFormat wdf = new SimpleDateFormat("w/yyyy");

        System.out.println("----------------------------------------------------");
        System.out.println(" KW " + wdf.format(cal.getTime()));
        System.out.println("----------------------------------------------------");

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        SimpleDateFormat weekDayFormat = new SimpleDateFormat("E");
        
        for (Entry entry : Util.getEntries(start, end)) {
            cal.setTime(entry.getTimestamp());
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            String mstr = monthFormat.format(cal.getTime());
            int min = cal.get(Calendar.MINUTE);
            String type = entry.typeToStr(entry.getType());
            String descr = entry.getDescr() == null ? "" : entry.getDescr();
            String wd = weekDayFormat.format(cal.getTime());
            while (type.length() < 6) {
                type += " ";
            }
            if (month != lastMonth) {
                System.out.printf("%1$s %2$ 3d %3$s %4$02d:%5$02d %6$s %7$s", mstr, day, wd, hour, min, type, descr);
                lastMonth = month;
                lastDay = day;
            } else if (day != lastDay) {
                System.out.printf("    %1$ 3d %2$s %3$02d:%4$02d %5$s %6$s", day, wd, hour, min, type, descr);
                lastDay = day;
            } else {
                System.out.printf("            %1$02d:%2$02d %3$s %4$s", hour, min, type, descr);
            }
            System.out.println();
        }


    }

    public java.lang.String[] usage() {
        return new String[]{
            "[<timestamp>]", "list all entries of a week"
        };
    }
}
