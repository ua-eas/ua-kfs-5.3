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
package org.kuali.kfs.module.bc.document.validation;

import org.kuali.kfs.module.bc.businessobject.BudgetConstructionMonthly;
import org.kuali.kfs.module.bc.document.BudgetConstructionDocument;

/**
 * Defines a rule for saving a Budget Construction Monthly, invoked when the user saves when using the Monthly Budget screen
 */
public interface SaveMonthlyBudgetRule<D extends BudgetConstructionDocument, M extends BudgetConstructionMonthly> {

    /**
     * Processes all the rules for this event and returns true if all rules passed
     * 
     * @param budgetConstructionDocument
     * @param budgetConstructionMonthly
     * @return
     */
    public boolean processSaveMonthlyBudgetRules(D budgetConstructionDocument, M budgetConstructionMonthly);
}
