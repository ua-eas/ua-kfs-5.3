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
package org.kuali.kfs.module.purap.document.web.struts;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants.CreditMemoEditMode;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kns.web.ui.ExtraButton;
import org.kuali.rice.kns.web.ui.HeaderField;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 * Struts Action Form for Credit Memo document.
 */
public class VendorCreditMemoForm extends AccountsPayableFormBase {

    /**
     * Constructs a PurchaseOrderForm instance and sets up the appropriately casted document.
     */
    public VendorCreditMemoForm() {
        super();
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return "CM";
    }

    /**
     * KRAD Conversion: Performs customization of an header fields.
     * 
     * Use of data dictionary for bo RequisitionDocument.
     */
    @Override
    public void populateHeaderFields(WorkflowDocument workflowDocument) {
        super.populateHeaderFields(workflowDocument);
        if (ObjectUtils.isNotNull(((VendorCreditMemoDocument) getDocument()).getPurapDocumentIdentifier())) {
            getDocInfo().add(new HeaderField("DataDictionary.VendorCreditMemoDocument.attributes.purapDocumentIdentifier", ((VendorCreditMemoDocument) getDocument()).getPurapDocumentIdentifier().toString()));
        }
        else {
            getDocInfo().add(new HeaderField("DataDictionary.VendorCreditMemoDocument.attributes.purapDocumentIdentifier", PurapConstants.PURAP_APPLICATION_DOCUMENT_ID_NOT_AVAILABLE));
        }
        
        String applicationDocumentStatus = PurapConstants.PURAP_APPLICATION_DOCUMENT_STATUS_NOT_AVAILABLE;
        
        if (ObjectUtils.isNotNull(((VendorCreditMemoDocument) getDocument()).getApplicationDocumentStatus())) {
            applicationDocumentStatus = workflowDocument.getApplicationDocumentStatus();
        }
        
        getDocInfo().add(new HeaderField("DataDictionary.VendorCreditMemoDocument.attributes.applicationDocumentStatus", applicationDocumentStatus));
        
    }

    /**
     * Build additional credit memo specific buttons and set extraButtons list.
     * 
     * @return - list of extra buttons to be displayed to the user
     * KRAD Conversion: Performs customization of extra buttons.
     * 
     * No data dictionary is involved.
     */
    @Override
    public List<ExtraButton> getExtraButtons() {
        extraButtons.clear();
        VendorCreditMemoDocument cmDocument = (VendorCreditMemoDocument) getDocument();
        String externalImageURL = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.RICE_EXTERNALIZABLE_IMAGES_URL_KEY);
        String appExternalImageURL = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY);

        if (getEditingMode().containsKey(CreditMemoEditMode.DISPLAY_INIT_TAB)) {
            addExtraButton("methodToCall.continueCreditMemo", externalImageURL + "buttonsmall_continue.gif", "Continue");
            addExtraButton("methodToCall.clearInitFields", externalImageURL + "buttonsmall_clear.gif", "Clear");
        }
        else {
            if (getEditingMode().containsKey(CreditMemoEditMode.HOLD)) {
                addExtraButton("methodToCall.addHoldOnCreditMemo", appExternalImageURL + "buttonsmall_hold.gif", "Hold");
            }

            if (getEditingMode().containsKey(CreditMemoEditMode.REMOVE_HOLD)) {
                addExtraButton("methodToCall.removeHoldFromCreditMemo", appExternalImageURL + "buttonsmall_removehold.gif", "Remove");
            }

            if (SpringContext.getBean(PurapService.class).isFullDocumentEntryCompleted(cmDocument) == false && documentActions.containsKey(KRADConstants.KUALI_ACTION_CAN_EDIT)) {
                addExtraButton("methodToCall.calculate", appExternalImageURL + "buttonsmall_calculate.gif", "Calculate");
            }

            if (getEditingMode().containsKey(CreditMemoEditMode.ACCOUNTS_PAYABLE_PROCESSOR_CANCEL)) {
                if (cmDocument.isSourceDocumentPaymentRequest() || cmDocument.isSourceDocumentPurchaseOrder()) {
                    //if the source is PREQ or PO, check the PO status
                    if (PurapConstants.PurchaseOrderStatuses.APPDOC_CLOSED.equals(cmDocument.getPurchaseOrderDocument().getApplicationDocumentStatus())) {
                        //if the PO is CLOSED, show the 'open order' button; but only if there are no pending actions on the PO
                        if (!cmDocument.getPurchaseOrderDocument().isPendingActionIndicator()) {
                            addExtraButton("methodToCall.reopenPo", appExternalImageURL + "buttonsmall_openorder.gif", "Reopen PO");
                        }
                    }
                    else {
                        addExtraButton("methodToCall.cancel", externalImageURL + "buttonsmall_cancel.gif", "Cancel");
                    }
                }
                else {
                    //if the source is Vendor, no need to check the PO status
                    addExtraButton("methodToCall.cancel", externalImageURL + "buttonsmall_cancel.gif", "Cancel");
                }
            }
        }

        return extraButtons;
    }
}

