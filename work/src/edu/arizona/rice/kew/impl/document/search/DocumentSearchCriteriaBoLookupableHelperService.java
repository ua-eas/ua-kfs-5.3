/*
 * Copyright 2014 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.arizona.rice.kew.impl.document.search;

import edu.arizona.rice.kim.api.identity.PersonService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;
import org.kuali.rice.kew.impl.document.search.DocumentSearchCriteriaBo;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.bo.BusinessObject;

// UAF-6 - Performance improvements to improve user experience for AWS deployment 
public class DocumentSearchCriteriaBoLookupableHelperService 
    extends org.kuali.rice.kew.impl.document.search.DocumentSearchCriteriaBoLookupableHelperService {
    private IdentityService identityService;
    private PersonService personService;
    
    
    @Override
    protected List<DocumentSearchCriteriaBo> populateSearchResults(List<DocumentSearchResult> lookupResults) {
        List<DocumentSearchCriteriaBo> retval = new ArrayList<DocumentSearchCriteriaBo>();

        Map <String, DocumentSearchCriteriaBo> pmap = new HashMap<String, DocumentSearchCriteriaBo>();

        // create the DocumentSearchCriteriaBo here without hitting ldap
        // for principal name - we will store the principal ids and make
        // one ldap call to get all the required Person objects
        for (DocumentSearchResult searchResult : lookupResults) {
            DocumentSearchCriteriaBo bo = new DocumentSearchCriteriaBo(searchResult.getDocument());
            retval.add(bo);
            pmap.put(bo.getInitiatorPrincipalId(), bo);
        }

        if (!pmap.isEmpty()) {
            List <Person> persons = getPersonService().getPeople(pmap.keySet());

            if (persons != null) {
                for (Person p : persons) {
                    pmap.get(p.getPrincipalId()).setInitiatorPerson(p);
                }
            }
        }
        
        return retval;
    }

    protected IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = KimApiServiceLocator.getIdentityService();
        }
        
        return identityService;
    }

    protected PersonService getPersonService() {
        if (personService == null) {
            personService = (PersonService)KimApiServiceLocator.getPersonService();
        }
        
        return personService;
    }

    @Override
    public List<? extends BusinessObject> getSearchResultsUnbounded(Map<String, String> fieldValues) {
        List<DocumentSearchCriteriaBo> retval = (List<DocumentSearchCriteriaBo>)super.getSearchResultsUnbounded(fieldValues);
        populateSearchResultsWithInitiator(retval);
        return retval;
    }

    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        List<DocumentSearchCriteriaBo> retval = (List<DocumentSearchCriteriaBo>)super.getSearchResults(fieldValues);
        populateSearchResultsWithInitiator(retval);
        return retval;
    }
    
    protected void populateSearchResultsWithInitiator(List<DocumentSearchCriteriaBo> searchResults) {
        Map <String, DocumentSearchCriteriaBo> imap = new HashMap<String, DocumentSearchCriteriaBo>();
        
        for (DocumentSearchCriteriaBo bo : searchResults) {
            imap.put(bo.getInitiatorPrincipalId(), bo);
        }

        if (!imap.isEmpty()) {
            List <Person> plist = getPersonService().getPeople(imap.keySet());

            for (Person p : plist) {
                DocumentSearchCriteriaBo bo = imap.get(p.getPrincipalId());

                if (bo != null) {
                    bo.setInitiatorPrincipalName(p.getPrincipalName());
                    bo.setInitiatorPerson(p);
                } 
            }
        }
    }
}