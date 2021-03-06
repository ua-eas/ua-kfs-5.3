/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.fixture;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.NotImplementedException;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.service.UniversityDateService;

/**
 * A testing fixture to mimic UniversityDateService calls that
 * need to be centered around time other than "now".
 */
public enum UniversityDateServiceFixture {

    DATE_2009_03_14(2009, "09", 3, 14),
    DATE_EMPTY(0, null, 0, 0);


    private int fiscalYear;
    private String fiscalPeriod;
    private int month;
    private int day;


    private UniversityDateServiceFixture(int fiscalYear, String fiscalPeriod, int month, int day){
        this.fiscalYear = fiscalYear;
        this.fiscalPeriod = fiscalPeriod;
        this.month = month;
        this.day = day;
    }


    /**
    *
    *
    * @return a very basic UniversityDateService that returns values from the selected enum.
    */
   public UniversityDateService createUniversityDateService(){
       UniversityDateService universityDateService = new TestingUniversityDateService(this.fiscalYear, this.fiscalPeriod, this.month, this.day);
       return universityDateService;
   }


    public int getFiscalYear() {
        return fiscalYear;
    }



    public String getFiscalPeriod() {
        return fiscalPeriod;
    }



    public int getMonth() {
        return month;
    }



    public int getDay() {
        return day;
    }


    /*
     * Inner class to contain basic information for simple UniversityDateService calls.
     * Keeping this here, as I don't necessarily want people to acess this without knowing
     * how rudimentary it is.
     */
    private class TestingUniversityDateService implements UniversityDateService {

        private static final String DATE_FORMAT = "YY-MM-dd";

        private int fiscalYear;
        private String fiscalPeriod;
        private int month;
        private int day;
        private UniversityDate universityDate;


        /**
         * Contsructs a TestingUniversityDateService object with the specified values.
         *
         * @param fiscalYear the current fiscal year you wish this service to return.
         * @param fiscalPeriod the current period year you wish this service to return.
         * @param month used in creating this.universityDate
         * @param day used in creating this.universityDate
         */
        public TestingUniversityDateService(int fiscalYear, String fiscalPeriod, int month, int day){

            // Initiate primitives
            this.fiscalYear = fiscalYear;
            this.fiscalPeriod = fiscalPeriod;
            this.month = month;
            this.day = day;

            // Initialize universityDate
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date date = null;
            try {
                date = sdf.parse(String.format("%s-%s-%s", this.fiscalYear, this.month, this.day));
            }
            catch (ParseException e) {
                throw new RuntimeException(e);
            }
            this.universityDate = new UniversityDate();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            this.universityDate.setUniversityDate(sqlDate);
            this.universityDate.setUniversityFiscalAccountingPeriod(this.fiscalPeriod);
        }

        @Override
        public UniversityDate getCurrentUniversityDate() {
            return universityDate;
        }

        @Override
        public Integer getFiscalYear(Date date) {
            return fiscalYear;
        }

        @Override
        public Date getFirstDateOfFiscalYear(Integer fiscalYear) {
            throw new NotImplementedException();
        }

        @Override
        public Date getLastDateOfFiscalYear(Integer fiscalYear) {
            throw new NotImplementedException();
        }

        @Override
        public Integer getCurrentFiscalYear() {
            return fiscalYear;
        }

        public int getFiscalYear() {
            return fiscalYear;
        }

        public void setFiscalYear(int fiscalYear) {
            this.fiscalYear = fiscalYear;
        }

        public String getFiscalPeriod() {
            return fiscalPeriod;
        }

        public void setFiscalPeriod(String fiscalPeriod) {
            this.fiscalPeriod = fiscalPeriod;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

    }

}


