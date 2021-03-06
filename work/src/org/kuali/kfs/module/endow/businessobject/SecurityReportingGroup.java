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
package org.kuali.kfs.module.endow.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.krad.bo.KualiCodeBase;

/**
 * Business Object for Security Reporting Group table
 */
public class SecurityReportingGroup extends KualiCodeBase {

    private Integer securityReportingGrpOrder;

    /**
     * This method gets the securityReportingGrpOrder
     * 
     * @return securityReportingGrpOrder
     */
    public Integer getSecurityReportingGrpOrder() {
        return securityReportingGrpOrder;
    }

    /**
     * This method sets the securityReportingGrpOrder
     * 
     * @param securityReportingGrpOrder
     */
    public void setSecurityReportingGrpOrder(Integer securityReportingGrpOrder) {
        this.securityReportingGrpOrder = securityReportingGrpOrder;
    }

    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap m = new LinkedHashMap();
        m.put(KFSConstants.GENERIC_CODE_PROPERTY_NAME, this.code);
        return m;
    }

}
