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
package org.kuali.kfs.module.endow.batch.service;

/**
 * Defines a batch job that locates all active securities that have been accruing income and are scheduled to distribute income on
 * the processing date and generate a transactional document that will credit the accrued income to the KEMID spendable income.
 */
public interface CreateAccrualTransactionsService {

    /**
     * Locates all active securities that have been accruing income and are scheduled to distribute income on the processing date
     * and generate a transactional document that will credit the accrued income to the KEMID spendable income.
     * 
     * @return true if successful, false otherwise
     */
    public boolean createAccrualTransactions();

}