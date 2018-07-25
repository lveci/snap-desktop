/*
 * Copyright (C) 2016 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.snap.productlibrary.rcp.toolviews.model;

import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.productlibrary.db.ProductEntry;
import org.esa.snap.productlibrary.rcp.toolviews.DatabasePane;

import java.util.*;

/**
 * Calculates statistic on a product entry list
 */
public class DatabaseStatistics implements DatabasePane.DatabaseQueryListener {

    private final DatabasePane dbPane;
    private final Map<Integer, YearData> yearDataMap = new HashMap<>(30);
    private Integer overallMaxYearCnt = 0;
    private Integer overallMaxDayCnt = 0;
    private MonthData monthData;

    private static final Calendar calendar = createCalendar();

    public DatabaseStatistics(final DatabasePane dbPane) {
        this.dbPane = dbPane;
        dbPane.addListener(this);
    }

    public void notifyNewEntryListAvailable() {
        updateStats(dbPane.getProductEntryList());
    }

    private void updateStats(final ProductEntry[] entryList) {
        if (entryList == null)
            return;
        yearDataMap.clear();
        monthData = new MonthData();

        for (ProductEntry entry : entryList) {
            final ProductData.UTC utc = entry.getFirstLineTime();
            if(utc == null) {
                continue;
            }
            final Calendar cal = getAsCalendar(utc);
            final int year = cal.get(Calendar.YEAR);
            YearData yData = yearDataMap.get(year);
            if (yData == null) {
                yData = new YearData(year);
                yearDataMap.put(year, yData);
            }
            final int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
            yData.addDayOfYear(dayOfYear);

            final int month = cal.get(Calendar.MONTH);
            monthData.add(month);
        }

        // find highest year count
        overallMaxYearCnt = 0;
        overallMaxDayCnt = 0;
        for (Integer year : yearDataMap.keySet()) {
            final YearData yData = yearDataMap.get(year);
            int cnt = yData.yearCnt;
            int dayCnt = yData.maxDayCnt;
            if (cnt > overallMaxYearCnt) {
                overallMaxYearCnt = cnt;
            }
            if (dayCnt > overallMaxDayCnt) {
                overallMaxDayCnt = dayCnt;
            }
        }

        //showStats();
    }

    private static Calendar createCalendar() {
        final Calendar calendar = GregorianCalendar.getInstance(ProductData.UTC.UTC_TIME_ZONE, Locale.ENGLISH);
        calendar.clear();
        calendar.set(2000, 0, 1);
        return calendar;
    }

    private static Calendar getAsCalendar(final ProductData.UTC utc) {
        calendar.clear();
        calendar.set(2000, 0, 1);
        calendar.add(Calendar.DATE, utc.getDaysFraction());
        calendar.add(Calendar.SECOND, (int) utc.getSecondsFraction());
        calendar.add(Calendar.MILLISECOND, (int) Math.round(utc.getMicroSecondsFraction() / 1000.0));
        return calendar;
    }

    public Map<Integer, YearData> getYearDataMap() {
        return yearDataMap;
    }

    public MonthData getMonthData() {
        return monthData;
    }

    public int getOverallMaxYearCnt() {
        return overallMaxYearCnt;
    }

    public int getOverallMaxDayCnt() {
        return overallMaxDayCnt;
    }

    private void showStats() {
        final SortedSet<Integer> years = new TreeSet<>(yearDataMap.keySet());
        System.out.print("Year: ");
        for (Integer y : years) {
            System.out.print(y + "= " + yearDataMap.get(y).yearCnt + "  ");
        }
        System.out.println();

        final Set<Integer> months = monthData.getMonthSet();
        System.out.print("Month: ");
        for (Integer m : months) {
            System.out.print(m + "= " + monthData.get(m) + "  ");
        }
        System.out.println();

        for (Integer y : years) {
            final Map<Integer, Integer> dayOfYear = yearDataMap.get(y).dayOfYearMap;
            final Set<Integer> days = dayOfYear.keySet();
            System.out.print(y + ": ");
            for (Integer d : days) {
                Integer dayCnt = dayOfYear.get(d);
                if (dayCnt != 0) {
                    System.out.print(d + "=" + dayCnt + " ");
                }
            }
            System.out.println();
        }
    }

    public static class YearData {
        public final int year;
        public int yearCnt = 0;
        public int maxDayCnt = 0;
        public final Map<Integer, Integer> dayOfYearMap = new HashMap<>(365); // starts from 1
        private boolean isSelected = true;

        YearData(final int year) {
            this.year = year;

            // init dayOfYear
            for (int d = 1; d < 366; ++d) {
                dayOfYearMap.put(d, 0);
            }
        }

        void addDayOfYear(final int dayOfYear) {
            Integer dayOfYearCnt = dayOfYearMap.get(dayOfYear);
            if (dayOfYearCnt == null) {
                dayOfYearCnt = 1;
            } else {
                dayOfYearCnt += 1;
            }
            dayOfYearMap.put(dayOfYear, dayOfYearCnt);

            // save max day cnt per year
            if (dayOfYearCnt > maxDayCnt) {
                maxDayCnt = dayOfYearCnt;
            }
            yearCnt += 1;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(final boolean selected) {
            isSelected = selected;
        }
    }

    public class MonthData {
        private int maxMonthCnt = 0;
        private final Map<Integer, Integer> monthMap = new HashMap<>(12); // starts from 0

        MonthData() {
            //init months to 0
            for (int m = 0; m < 12; ++m) {
                monthMap.put(m, 0);
            }
        }

        public void add(final Integer month) {
            Integer monthCnt = monthMap.get(month);
            if (monthCnt != null) {
                monthCnt += 1;
                if (monthCnt > maxMonthCnt) {
                    maxMonthCnt = monthCnt;
                }
                monthMap.put(month, monthCnt);
            }
        }

        public Set<Integer> getMonthSet() {
            return monthMap.keySet();
        }

        public Integer get(final Integer m) {
            return monthMap.get(m);
        }

        public int getMaxMonthCnt() {
            return maxMonthCnt;
        }

        public void setSelected(final int m, final boolean selected) {
            dbPane.getDBQuery().setMonthSelected(m, selected);
            dbPane.partialQuery();
        }

        public boolean isSelected(final int m) {
            return dbPane.getDBQuery().isMonthSelected(m);
        }
    }
}
