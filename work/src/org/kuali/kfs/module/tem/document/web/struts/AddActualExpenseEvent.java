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
package org.kuali.kfs.module.tem.document.web.struts;

import static org.kuali.kfs.module.tem.TemPropertyConstants.NEW_ACTUAL_EXPENSE_LINE;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.kuali.kfs.module.tem.TemPropertyConstants;
import org.kuali.kfs.module.tem.businessobject.ActualExpense;
import org.kuali.kfs.module.tem.document.TravelDocument;
import org.kuali.kfs.module.tem.document.service.TravelDocumentService;
import org.kuali.kfs.module.tem.document.validation.event.AddActualExpenseLineEvent;
import org.kuali.kfs.module.tem.document.web.bean.TravelMvcWrapperBean;
import org.kuali.kfs.module.tem.service.AccountingDistributionService;
import org.kuali.kfs.module.tem.service.TravelExpenseService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.service.KualiRuleService;


public class AddActualExpenseEvent implements Observer {

    public static Logger LOG = Logger.getLogger(AddActualExpenseDetailEvent.class);

    protected volatile TravelExpenseService travelExpenseService;

    @Override
    public void update(Observable arg0, Object arg1) {
        if (!(arg1 instanceof TravelMvcWrapperBean)) {
            return;
        }
        final TravelMvcWrapperBean wrapper = (TravelMvcWrapperBean) arg1;

        final TravelDocument document = wrapper.getTravelDocument();
        final ActualExpense newActualExpenseLine = wrapper.getNewActualExpenseLine();

        if(newActualExpenseLine != null){
            newActualExpenseLine.refreshReferenceObject(TemPropertyConstants.EXPENSE_TYPE_OBJECT_CODE);
        }

        boolean rulePassed = true;

        // check any business rules
        rulePassed &= getRuleService().applyRules(new AddActualExpenseLineEvent<ActualExpense>(NEW_ACTUAL_EXPENSE_LINE, document, newActualExpenseLine));

        if (rulePassed){
            if(newActualExpenseLine != null){
                document.addExpense(newActualExpenseLine);
            }

            ActualExpense newExpense = getTravelExpenseService().createNewDetailForActualExpense(newActualExpenseLine);

            wrapper.setNewActualExpenseLine(new ActualExpense());
            wrapper.getNewActualExpenseLines().add(newExpense);
            wrapper.setDistribution(getAccountingDistributionService().buildDistributionFrom(document));
        }
    }

    /**
     * Gets the travelReimbursementService attribute.
     *
     * @return Returns the travelReimbursementService.
     */
    protected TravelDocumentService getTravelDocumentService() {
        return SpringContext.getBean(TravelDocumentService.class);
    }

    protected KualiRuleService getRuleService() {
        return SpringContext.getBean(KualiRuleService.class);
    }

    protected AccountingDistributionService getAccountingDistributionService() {
        return SpringContext.getBean(AccountingDistributionService.class);
    }

    protected TravelExpenseService getTravelExpenseService() {
        if (travelExpenseService == null) {
            travelExpenseService = SpringContext.getBean(TravelExpenseService.class);
        }
        return travelExpenseService;
    }
}
