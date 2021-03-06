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
package org.kuali.kfs.module.ld.businessobject.lookup;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.integration.ld.businessobject.inquiry.AbstractPositionDataDetailsInquirableImpl;
import org.kuali.kfs.module.ld.businessobject.July1PositionFunding;
import org.kuali.kfs.module.ld.businessobject.LedgerBalance;
import org.kuali.kfs.module.ld.businessobject.inquiry.July1PositionFundingInquirableImpl;
import org.kuali.kfs.module.ld.businessobject.inquiry.PositionDataDetailsInquirableImpl;
import org.kuali.kfs.module.ld.dataaccess.LaborDao;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl;
import org.kuali.rice.kns.lookup.HtmlData;
import org.kuali.rice.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.lookup.CollectionIncomplete;
import org.kuali.rice.krad.util.BeanPropertyComparator;

/**
 * The July1PositionFundingLookupableHelperServiceImpl class is the front-end for all July 1 funds balance inquiry processing.
 */
public class July1PositionFundingLookupableHelperServiceImpl extends AbstractLookupableHelperServiceImpl {
    private static org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(July1PositionFundingLookupableHelperServiceImpl.class);

    private LaborDao laborDao;

    /**
     * @see org.kuali.rice.kns.lookup.Lookupable#getInquiryUrl(org.kuali.rice.krad.bo.BusinessObject, java.lang.String)
     */
    @Override
    public HtmlData getInquiryUrl(BusinessObject bo, String propertyName) {
        if (KFSPropertyConstants.POSITION_NUMBER.equals(propertyName)) {
            LedgerBalance balance = (LedgerBalance) bo;
            AbstractPositionDataDetailsInquirableImpl positionDataDetailsInquirable = new PositionDataDetailsInquirableImpl();

            Map<String, String> fieldValues = new HashMap<String, String>();
            fieldValues.put(propertyName, balance.getPositionNumber());

            BusinessObject positionData = positionDataDetailsInquirable.getBusinessObject(fieldValues);

            return positionData == null ? new AnchorHtmlData(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING) : positionDataDetailsInquirable.getInquiryUrl(positionData, propertyName);
        }
        return (new July1PositionFundingInquirableImpl()).getInquiryUrl(bo, propertyName);
    }

    /**
     * @see org.kuali.rice.kns.lookup.Lookupable#getSearchResults(java.util.Map)
     */
    @Override
    public List getSearchResults(Map<String, String> fieldValues) {
        setBackLocation(fieldValues.get(KFSConstants.BACK_LOCATION));
        setDocFormKey(fieldValues.get(KFSConstants.DOC_FORM_KEY));

        Collection<July1PositionFunding> searchResultsCollection = getLaborDao().getJuly1PositionFunding(fieldValues);

        // sort list if default sort column given
        List searchResults = (List) searchResultsCollection;
        List defaultSortColumns = getDefaultSortColumns();
        if (defaultSortColumns.size() > 0) {
            Collections.sort(searchResults, new BeanPropertyComparator(defaultSortColumns, true));
        }

        return new CollectionIncomplete(searchResults, new Long(0));
    }

    /**
     * Gets the laborDao attribute.
     * @return Returns the laborDao.
     */
    public LaborDao getLaborDao() {
        return laborDao;
    }

    /**
     * Sets the laborDao attribute value.
     * @param laborDao The laborDao to set.
     */
    public void setLaborDao(LaborDao laborDao) {
        this.laborDao = laborDao;
    }
}
