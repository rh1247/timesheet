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

import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 *
 * @author  richard.hierlmeier@sun.com
 */
public class MonthStatEntry implements Comparable {
    
    private double ist;
    private double soll;
    private long timestamp;
    private int month;

    /** Creates a new instance of MonthStatEntry */
    public MonthStatEntry(String line) {
        StringTokenizer st = new StringTokenizer(line, "|");
        
        if(st.countTokens() < 4 ) {
            throw new IllegalArgumentException("Invalid line");
        }
        
        setMonth(Integer.parseInt(st.nextToken()));
        setTimestamp(Long.parseLong(st.nextToken()));
        setIst(Double.parseDouble(st.nextToken()));
        setSoll(Double.parseDouble(st.nextToken()));
    }
    
    public void write(PrintWriter pw) {
        pw.print(month);
        pw.print('|');
        pw.print(timestamp);
        pw.print('|');
        pw.print(ist);
        pw.print('|');
        pw.print(soll);
        pw.println();
    }
    public boolean equals(Object obj) {
        return obj instanceof MonthStatEntry &&
               month == ((MonthStatEntry)obj).month;
    }
    
    public int hashCode() {
        return month;
    }


    public MonthStatEntry() {
    }

    public double getIst() {
        return ist;
    }

    public void setIst(double ist) {
        this.ist = ist;
    }

    public double getSoll() {
        return soll;
    }

    public void setSoll(double soll) {
        this.soll = soll;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public double getDelta() {
        return getIst() - getSoll();
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int compareTo(Object o) {
        MonthStatEntry e = (MonthStatEntry)o;
        return month - e.month;
    }

    
    
    
}
