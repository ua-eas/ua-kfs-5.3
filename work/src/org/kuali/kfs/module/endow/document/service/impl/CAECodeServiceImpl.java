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
package org.kuali.kfs.module.endow.document.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.endow.businessobject.CAECode;
import org.kuali.kfs.module.endow.document.service.CAECodeService;
import org.kuali.rice.krad.service.BusinessObjectService;

public class CAECodeServiceImpl implements CAECodeService {
    
    private BusinessObjectService businessObjectService;
    
    /**
     * @see org.kuali.kfs.module.endow.document.service.CAECodeService#getByPrimaryKey(java.lang.String)
     */
    public CAECode getByPrimaryKey(String code) {
        CAECode caeCode = null;
        if (StringUtils.isNotBlank(code)) {
            Map criteria = new HashMap();
            criteria.put("code", code);
            caeCode = (CAECode) businessObjectService.findByPrimaryKey(CAECode.class, criteria);

        }
        return caeCode;
    }
    
    /**
     * Gets the businessObjectService.
     * 
     * @return businessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService.
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}