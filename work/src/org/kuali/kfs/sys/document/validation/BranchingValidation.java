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
package org.kuali.kfs.sys.document.validation;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

/**
 * An abstract class that creates an easy way to branch between validations.  Basically,
 * extenders set a branch map - a map where the key is the name of the branch and the value
 * is the validation to perform to check on that branch.  Extenders also implement the
 * determineBranch method, which returns the name of the branch to validate against;
 * if null is returned, then no validation will occur.
 */
public abstract class BranchingValidation extends ParameterizedValidation implements Validation {
    protected Map<String, Validation> branchMap;
    protected List<ValidationFieldConvertible> parameterProperties;
    protected boolean shouldQuitOnFail = false;

    /**
     * Determines which branch, if any, within the branchMap should be used as the validation to take.
     * @param event the event which triggered this validation
     * @return the name of the branch to take, or a null or empty string to not take any branch and simply pass validation as true
     */
    protected abstract String determineBranch(AttributedDocumentEvent event);
    
    /**
     * Note: these parameter properties only help determine what branching should take place; these properties will not affect in anyway the branch children
     * @see org.kuali.kfs.sys.document.validation.Validation#getParameterProperties()
     */
    public List<ValidationFieldConvertible> getParameterProperties() {
        return this.parameterProperties;
    }

    /**
     * Sets the parameterProperties attribute value.
     * @param parameterProperties The parameterProperties to set.
     */
    public void setParameterProperties(List<ValidationFieldConvertible> parameterProperties) {
        this.parameterProperties = parameterProperties;
    }

    /**
     * @see org.kuali.kfs.sys.document.validation.Validation#shouldQuitOnFail()
     */
    public boolean shouldQuitOnFail() {
        return shouldQuitOnFail;
    }

    /**
     * Sets the shouldQuitOnFail attribute value.
     * @param shouldQuitOnFail The shouldQuitOnFail to set.
     */
    public void setShouldQuitOnFail(boolean shouldQuitOnFail) {
        this.shouldQuitOnFail = shouldQuitOnFail;
    }

    /**
     * 
     * @see org.kuali.kfs.sys.document.validation.Validation#stageValidation(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean stageValidation(AttributedDocumentEvent event) {
        populateParametersFromEvent(event);
        return validate(event);
    }

    /**
     * Using the branch name returned by determineBranch(), validates the event against the corresponding
     * branch in the branch map.  If a null or empty string is returned from determineBrach(), this method
     * simply returns true; if there is no validation in the branchMap for the given name, an IllegalStateException
     * is thrown.
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        String branchName = determineBranch(event);
        if (!StringUtils.isBlank(branchName)) {
            Validation validation = branchMap.get(branchName);
            if (validation == null) {
                throw new IllegalStateException("Branching Validation "+this.getClass().getName()+" cannot find a branch named "+branchName);
            }
            return validation.stageValidation(event);
        } else {
            return true;
        }
    }

    /**
     * Gets the branchMap attribute. 
     * @return Returns the branchMap.
     */
    public Map<String, Validation> getBranchMap() {
        return branchMap;
    }

    /**
     * Sets the branchMap attribute value.
     * @param branchMap The branchMap to set.
     */
    public void setBranchMap(Map<String, Validation> branchMap) {
        this.branchMap = branchMap;
    }

}
