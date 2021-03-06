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

package org.kuali.kfs.module.cg.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * This class represents an association between an award and a subcontractor. It's like a reference to the subcontractor from the
 * award. This way an award can maintain a collection of these references instead of owning subcontractors directly.
 */
public class AwardSubcontractor extends PersistableBusinessObjectBase implements MutableInactivatable {

    private String awardSubcontractorAmendmentNumber;
    private String awardSubcontractorNumber;
    private String subcontractorNumber;
    private Long proposalNumber;
    private KualiDecimal subcontractorAmount;
    private String subcontractorContactFirstName;
    private String subcontractorContactLastName;
    private String subcontractorAuditHistoryText;
    private String awardSubcontractorDescription;
    private boolean active = true;

    private SubContractor subcontractor;

    /**
     * Default constructor.
     */
    public AwardSubcontractor() {
    }

    /**
     * Gets the awardSubcontractorAmendmentNumber attribute.
     * 
     * @return Returns the awardSubcontractorAmendmentNumber
     */
    public String getAwardSubcontractorAmendmentNumber() {
        return awardSubcontractorAmendmentNumber;
    }

    /**
     * Sets the awardSubcontractorAmendmentNumber attribute.
     * 
     * @param awardSubcontractorAmendmentNumber The awardSubcontractorAmendmentNumber to set.
     */
    public void setAwardSubcontractorAmendmentNumber(String awardSubcontractorAmendmentNumber) {
        this.awardSubcontractorAmendmentNumber = awardSubcontractorAmendmentNumber;
    }


    /**
     * Gets the awardSubcontractorNumber attribute.
     * 
     * @return Returns the awardSubcontractorNumber
     */
    public String getAwardSubcontractorNumber() {
        return awardSubcontractorNumber;
    }

    /**
     * Sets the awardSubcontractorNumber attribute.
     * 
     * @param awardSubcontractorNumber The awardSubcontractorNumber to set.
     */
    public void setAwardSubcontractorNumber(String awardSubcontractorNumber) {
        this.awardSubcontractorNumber = awardSubcontractorNumber;
    }


    /**
     * Gets the subcontractorNumber attribute.
     * 
     * @return Returns the subcontractorNumber
     */
    public String getSubcontractorNumber() {
        return subcontractorNumber;
    }

    /**
     * Sets the subcontractorNumber attribute.
     * 
     * @param subcontractorNumber The subcontractorNumber to set.
     */
    public void setSubcontractorNumber(String subcontractorNumber) {
        this.subcontractorNumber = subcontractorNumber;
    }


    /**
     * Gets the proposalNumber attribute.
     * 
     * @return Returns the proposalNumber
     */
    public Long getProposalNumber() {
        return proposalNumber;
    }

    /**
     * Sets the proposalNumber attribute.
     * 
     * @param proposalNumber The proposalNumber to set.
     */
    public void setProposalNumber(Long proposalNumber) {
        this.proposalNumber = proposalNumber;
    }


    /**
     * Gets the subcontractorAmount attribute.
     * 
     * @return Returns the subcontractorAmount
     */
    public KualiDecimal getSubcontractorAmount() {
        return subcontractorAmount;
    }

    /**
     * Sets the subcontractorAmount attribute.
     * 
     * @param subcontractorAmount The subcontractorAmount to set.
     */
    public void setSubcontractorAmount(KualiDecimal subcontractorAmount) {
        this.subcontractorAmount = subcontractorAmount;
    }


    /**
     * Gets the subcontractorContactFirstName attribute.
     * 
     * @return Returns the subcontractorContactFirstName
     */
    public String getSubcontractorContactFirstName() {
        return subcontractorContactFirstName;
    }

    /**
     * Sets the subcontractorContactFirstName attribute.
     * 
     * @param subcontractorContactFirstName The subcontractorContactFirstName to set.
     */
    public void setSubcontractorContactFirstName(String subcontractorContactFirstName) {
        this.subcontractorContactFirstName = subcontractorContactFirstName;
    }


    /**
     * Gets the subcontractorContactLastName attribute.
     * 
     * @return Returns the subcontractorContactLastName
     */
    public String getSubcontractorContactLastName() {
        return subcontractorContactLastName;
    }

    /**
     * Sets the subcontractorContactLastName attribute.
     * 
     * @param subcontractorContactLastName The subcontractorContactLastName to set.
     */
    public void setSubcontractorContactLastName(String subcontractorContactLastName) {
        this.subcontractorContactLastName = subcontractorContactLastName;
    }


    /**
     * Gets the subcontractorAuditHistoryText attribute.
     * 
     * @return Returns the subcontractorAuditHistoryText
     */
    public String getSubcontractorAuditHistoryText() {
        return subcontractorAuditHistoryText;
    }

    /**
     * Sets the subcontractorAuditHistoryText attribute.
     * 
     * @param subcontractorAuditHistoryText The subcontractorAuditHistoryText to set.
     */
    public void setSubcontractorAuditHistoryText(String subcontractorAuditHistoryText) {
        this.subcontractorAuditHistoryText = subcontractorAuditHistoryText;
    }

    /**
     * Gets the awardSubcontractorDescription attribute.
     * 
     * @return Returns the awardSubcontractorDescription.
     */
    public String getAwardSubcontractorDescription() {
        return awardSubcontractorDescription;
    }

    /**
     * Sets the awardSubcontractorDescription attribute value.
     * 
     * @param awardSubcontractorDescription The awardSubcontractorDescription to set.
     */
    public void setAwardSubcontractorDescription(String awardSubcontractorDescription) {
        this.awardSubcontractorDescription = awardSubcontractorDescription;
    }

    /**
     * Gets the subcontractor attribute.
     * 
     * @return Returns the subcontractor.
     */
    public SubContractor getSubcontractor() {
        return subcontractor;
    }

    /**
     * Sets the subcontractor attribute.
     * 
     * @param subcontractor The subcontractor to set.
     * @deprecated Setter is required by OJB, but should not be used to modify this attribute. This attribute is set on the initial
     *             creation of the object and should not be changed.
     */
    @Deprecated
    public void setSubcontractor(SubContractor subcontractor) {
        this.subcontractor = subcontractor;
    }

    /**
     * @see org.kuali.rice.core.api.mo.common.active.MutableInactivatable#setActive(boolean)
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @see org.kuali.rice.core.api.mo.common.active.MutableInactivatable#isActive()
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    @SuppressWarnings("unchecked")
    
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("awardSubcontractorAmendmentNumber", this.awardSubcontractorAmendmentNumber);
        m.put("awardSubcontractorNumber", this.awardSubcontractorNumber);
        m.put("subcontractorNumber", this.subcontractorNumber);
        if (this.proposalNumber != null) {
            m.put("proposalNumber", this.proposalNumber.toString());
        }
        return m;
    }

}
