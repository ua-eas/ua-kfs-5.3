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
package edu.arizona.rice.kew.docsearch.dao.impl;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.kuali.rice.kew.api.document.Document;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.document.attribute.DocumentAttribute;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeFactory;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;

// UAF-6 - Performance improvements to improve user experience for AWS deployment 
public class DocumentInformation {
    private String documentId;
    private String initiatorPrincipalId;
    private String documentTypeName;
    private String documentTypeId;
    private String statusCode;
    private String title;
    private String applicationDocumentStatus;
    private String applicationDocumentId;
    private String routePrincipalId;
    private String documentHandlerUrl;
    private Date createDate;
    private Date applicationDocumentStatusModificationDate;
    private Date approvalDate;
    private Date finalDate;
    private Date statusModificationDate;
    private List<DocumentAttribute> attributes;

    public DocumentInformation(ResultSet rs) throws SQLException {
        documentId = rs.getString("DOC_HDR_ID");
        initiatorPrincipalId = rs.getString("INITR_PRNCPL_ID");
        documentTypeName = rs.getString("DOC_TYP_NM");
        documentTypeId = rs.getString("DOC_TYP_ID");
        statusCode = rs.getString("DOC_HDR_STAT_CD");
        title = rs.getString("TTL");
        applicationDocumentStatus = rs.getString("APP_DOC_STAT");
        applicationDocumentId = rs.getString("APP_DOC_ID");
        routePrincipalId = rs.getString("RTE_PRNCPL_ID");
        documentHandlerUrl = rs.getString("DOC_HDLR_URL");
        createDate = rs.getDate("CRTE_DT");
        applicationDocumentStatusModificationDate = rs.getDate("APP_DOC_STAT_MDFN_DT");
        approvalDate = rs.getDate("APRV_DT");
        finalDate = rs.getDate("FNL_DT");
        statusModificationDate = rs.getDate("STAT_MDFN_DT");
    }

    public DocumentSearchResult.Builder getSearchResult() {
        Document.Builder documentBuilder = Document.Builder.create(documentId, initiatorPrincipalId, documentTypeName, documentTypeId);
        DocumentSearchResult.Builder retval = DocumentSearchResult.Builder.create(documentBuilder);
        documentBuilder.setStatus(DocumentStatus.fromCode(statusCode));
        documentBuilder.setTitle(title);
        documentBuilder.setApplicationDocumentStatus(applicationDocumentStatus);
        documentBuilder.setApplicationDocumentId(applicationDocumentId);
        documentBuilder.setRoutedByPrincipalId(routePrincipalId);
        documentBuilder.setDocumentHandlerUrl(documentHandlerUrl);

        if (createDate != null) {
            documentBuilder.setDateCreated(new DateTime(createDate));
        }

        if (applicationDocumentStatusModificationDate != null) {
            documentBuilder.setApplicationDocumentStatusDate(new DateTime(applicationDocumentStatusModificationDate));
        }

        if (approvalDate != null) {
            documentBuilder.setDateApproved(new DateTime(approvalDate));
        }

        if (finalDate != null) {
            documentBuilder.setDateFinalized(new DateTime(finalDate));
        }

        if (statusModificationDate != null) {
            documentBuilder.setDateLastModified(new DateTime(statusModificationDate));
        }

        if (attributes != null) {
            for (DocumentAttribute att : attributes) {
                retval.getDocumentAttributes().add(DocumentAttributeFactory.loadContractIntoBuilder(att));
            }
        }

        return retval;
    }

    public List<DocumentAttribute> getAttributes() {
        if (attributes == null) {
            attributes = new ArrayList<DocumentAttribute>();
        }

        return attributes;
    }

    public String getDocumentId() {
        return documentId;
    }
}
