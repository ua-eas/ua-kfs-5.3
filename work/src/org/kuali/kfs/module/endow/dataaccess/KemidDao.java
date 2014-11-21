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
package org.kuali.kfs.module.endow.dataaccess;

import java.util.List;

import org.kuali.kfs.module.endow.businessobject.KEMID;

public interface KemidDao {
    
    /**
     * Gets KEMIDs by kemids
     * 
     * @param kemids
     * @param endowmentOption
     * @param currentDate
     * @return
     */
    public List<KEMID> getKemidRecordsByIds(List<String> kemids, String endowmentOption, String closedIndicator); 
    
    /**
     * Gets kemids by the given criteria
     * 
     * @param attributeName
     * @param values
     * @param endowmentOption
     * @return
     */
    public List<String> getKemidsByAttributeWithEndowmentOption(String attributeName, List<String> values, String endowmentOption, String closedIndicator);
    
    /**
     * Gets kemids by the given attribute value strings
     * 
     * @param attributeName
     * @param values
     * @return
     */
    public List<String> getKemidsByAttribute(String attributeName, List<String> values);  
    
    /**
     * Gets all the values of the attribute by the given value strings 
     * 
     * @param attributeName
     * @param values
     * @return
     */
    public List<String> getAttributeValues(String attributeName, List<String> values);
}