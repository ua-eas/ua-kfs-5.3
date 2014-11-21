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
package org.kuali.kfs.module.tem.document.service;

import java.util.Collection;

import org.kuali.kfs.module.tem.document.TravelRelocationDocument;
import org.kuali.kfs.module.tem.pdf.Coversheet;
import org.kuali.rice.kew.api.exception.WorkflowException;

public interface TravelRelocationService {
    
    /**
     * Adding properties listeners
     * 
     * @param relocation
     */
    void addListenersTo(final TravelRelocationDocument relocation);
    
    /**
     * Locate all {@link TravelRelocationDocument} instances with the same
     * <code>travelDocumentIdentifier</code>
     *
     * @param travelDocumentIdentifier to locate {@link TravelRelocationDocument} instances
     * @return {@link Collection} of {@link TravelRelocationDocument} instances
     */
    public Collection<TravelRelocationDocument> findByIdentifier(String travelDocumentIdentifier);
    
    public TravelRelocationDocument find(final String documentNumber)throws WorkflowException;
       
    /**
     * This method uses the values provided to build and populate a cover sheet associated with a given {@link Document}.
     * 
     * @param document {@link TravelRelocationDocument} to generate a coversheet for 
     * @return {@link Coversheet} instance
     */
    Coversheet generateCoversheetFor(final TravelRelocationDocument document) throws Exception;
    
}
