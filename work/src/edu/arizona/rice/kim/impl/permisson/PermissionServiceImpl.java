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

package edu.arizona.rice.kim.impl.permisson;

import java.util.List;
import java.util.Map;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.kim.api.identity.entity.Entity;

// **AZ UPGRADE 3.0-5.3** 
public class PermissionServiceImpl extends org.kuali.rice.kim.impl.permission.PermissionServiceImpl {
    private static final String KUALI_USER_ROLE_ID = "1";
    private IdentityService identityService;

    @Override
    public boolean isAuthorized(String principalId, String namespaceCode, String permissionName, Map<String, String> qualification) throws RiceIllegalArgumentException {
        boolean retval = false;
        // UA UPGRADE - if logging in and user is active employee the let them in
        if (KimConstants.PermissionNames.LOG_IN.equals(permissionName)) {
            retval = isAuthorizedToLogin(principalId);
        } else {
            retval = super.isAuthorized(principalId, namespaceCode, permissionName, qualification);

            if (!retval) {
                retval = isKualiUserAuthorized(getRoleIdsForPermission(namespaceCode, permissionName));
            }
        }
        
        return retval;
    }

    @Override
    public boolean isAuthorizedByTemplate(String principalId, String namespaceCode, String permissionTemplateName, 
            Map<String, String> permissionDetails, Map<String, String> qualification) throws RiceIllegalArgumentException {
        boolean retval = super.isAuthorizedByTemplate(principalId, namespaceCode, permissionTemplateName, permissionDetails, qualification);
        
        if (!retval) {
            retval = isKualiUserAuthorized(getRoleIdsForPermissionTemplate(namespaceCode, permissionTemplateName, permissionDetails));
        }
        
        return retval;
    }

    private boolean isKualiUserAuthorized(List <String> roleids) {
        return ((roleids != null) && roleids.contains(KUALI_USER_ROLE_ID));
    }
    
    
    private boolean isAuthorizedToLogin(String principalId) {
        Entity e = identityService.getEntityByPrincipalId(principalId);
        return (e != null);
    }
    
    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }
}
