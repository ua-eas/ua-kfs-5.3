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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.CoreApiServiceLocator;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.uif.RemotableAttributeField;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.core.framework.persistence.platform.DatabasePlatform;
import org.kuali.rice.core.framework.persistence.platform.MySQLDatabasePlatform;
import org.kuali.rice.core.framework.persistence.platform.OracleDatabasePlatform;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.document.DocumentStatusCategory;
import org.kuali.rice.kew.api.document.attribute.DocumentAttribute;
import org.kuali.rice.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.rice.kew.api.document.search.DocumentSearchResult;
import org.kuali.rice.kew.api.document.search.DocumentSearchResults;
import org.kuali.rice.kew.api.document.search.RouteNodeLookupLogic;
import org.kuali.rice.kew.docsearch.DocumentSearchCustomizationMediator;
import org.kuali.rice.kew.docsearch.DocumentSearchInternalUtils;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
import org.kuali.rice.kew.engine.node.RouteNode;
import org.kuali.rice.kew.engine.node.service.RouteNodeService;
import org.kuali.rice.kew.impl.document.search.DocumentSearchGenerator;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.util.KRADConstants;

// UAF-6 - Performance improvements to improve user experience for AWS deployment 
public class DocumentSearchDAOJdbcImpl extends org.kuali.rice.kew.docsearch.dao.impl.DocumentSearchDAOJdbcImpl {
    private DataSource dataSource;
    private boolean useInternalSearch = false;
    private DatabasePlatform dbPlatform;
    private PersonService personService;
    private IdentityService identityService;
    private DateTimeService dateTimeService;
    private DocumentTypeService documentTypeService;
    private DocumentSearchCustomizationMediator documentSearchCustomizationMediator;
    private RouteNodeService routeNodeService;
    private boolean databaseSpecificCodeAllowed = false;
    private int customFetchSize = DocumentSearchConstants.DEFAULT_FETCH_SIZE;

    @Override
    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public DocumentSearchResults.Builder findDocuments(DocumentSearchGenerator defaultDocumentSearchGenerator, 
            DocumentSearchCriteria criteria, 
            boolean criteriaModified, 
            List<RemotableAttributeField> searchFields) {
        DocumentSearchResults.Builder retval = null;
        
        long start = System.currentTimeMillis();

        if (useInternalSearch) {
            retval = doInternalSearch(criteria, criteriaModified, searchFields);
        } else {
            retval = super.findDocuments(defaultDocumentSearchGenerator, criteria, criteriaModified, searchFields);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("findDocuments elapsed time(sec): " + (System.currentTimeMillis() - start)/1000);
        }
        
        return retval;
    }
    
    /**
     * 
     * @param criteria
     * @param criteriaModified
     * @param searchFields
     * @return 
     */
    protected DocumentSearchResults.Builder doInternalSearch(DocumentSearchCriteria criteria, 
        boolean criteriaModified, 
        final List<RemotableAttributeField> searchFields) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet res = null;
        
        DocumentSearchResults.Builder retval = DocumentSearchResults.Builder.create(DocumentSearchCriteria.Builder.create(criteria));
        retval.setCriteriaModified(criteriaModified);
        retval.setSearchResults(new ArrayList<DocumentSearchResult.Builder>());
        
        try {
            conn = dataSource.getConnection();
            conn.setReadOnly(true);
            stmt = conn.createStatement();
            int maxRows = getMaxResultCap(criteria) + 1;

            stmt.setMaxRows(maxRows);
            
            int fetchSize = stmt.getFetchSize();
            
            // use default fetch size if document id is included
            // otherwize use custom fetch size
            if (StringUtils.isBlank(criteria.getDocumentId())) {
                fetchSize = customFetchSize;
            } 
            
            
            String sql = getSearchSql(criteria, searchFields);

            res = stmt.executeQuery(sql);

            // run up to the starting row if required
            if (criteria.getStartAtIndex() != null) {
                for (int i = 0; (i < criteria.getStartAtIndex()) && res.next(); ++i) {};
            }

            List <DocumentInformation> results = new ArrayList<DocumentInformation>();

            // load up document information from the query
            while (res.next() && (results.size() < maxRows)) {
                results.add(new DocumentInformation(res));
            }

            // if we have threshold+1 results, then we have more results than we are going to display
            retval.setOverThreshold(res.next());
            if (isUsingAtLeastOneSearchAttribute(criteria)) {
                // now that we have a list of documents, load the attributes
                loadDocumentAttributes(results, fetchSize);
            }

            // generate the DocumentSearchResults.Builder to return
            for (DocumentInformation docinfo : results) {
                retval.getSearchResults().add(docinfo.getSearchResult());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("document search result rows processed: " + retval.getSearchResults().size());
            }
        }
        
        catch (Exception ex) {
            throw new RuntimeException(ex.toString(), ex);
        }
        
        finally {
            DocumentSearchUtils.closeDbObjects(conn, stmt, res);
        }
        
        return retval;
    }


    /**
     * get document search select clause
     * @return 
     */
    protected String getSelectClause() {
        StringBuilder retval = new StringBuilder(512);
        
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[1]);
        retval.append("select ");
        
        // if this is oracle and database-specific code allowed add the parallel hint
        if (databaseSpecificCodeAllowed) {
            if (isOracleDatabase()) {
                retval.append(" /*+ PARALLEL(AUTO) */ ");
            }
        }
        
        retval.append(" distinct");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".DOC_HDR_ID, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".INITR_PRNCPL_ID, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".DOC_HDR_STAT_CD, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".CRTE_DT, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".TTL, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".APP_DOC_STAT, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".STAT_MDFN_DT, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".APRV_DT, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS); 
        retval.append(".FNL_DT, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".APP_DOC_ID, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".RTE_PRNCPL_ID, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".APP_DOC_STAT_MDFN_DT, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_TYPE_ALIAS);
        retval.append(".DOC_TYP_ID, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_TYPE_ALIAS);
        retval.append(".DOC_TYP_NM, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_TYPE_ALIAS);
        retval.append(".LBL, ");
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append(DocumentSearchConstants.DOCUMENT_TYPE_ALIAS);
        retval.append(".DOC_HDLR_URL, ");
        retval.append(DocumentSearchConstants.DOCUMENT_TYPE_ALIAS);
        retval.append(".ACTV_IND ");
        
        return retval.toString();
    }

    /**
     * 
     * @param criteria
     * @param searchFields
     * @param personIdentifiers
     * @param indexTableWhereClause
     * @return 
     */
    protected String getFromClause(DocumentSearchCriteria criteria, 
        List<RemotableAttributeField> searchFields, 
        Map <String, List<String>> personIdentifiers,
        StringBuilder indexTableWhereClause) {
        StringBuilder retval = new StringBuilder(256);

        retval.append(DocumentSearchConstants.FORMAT_OFFSET[1]);
        retval.append("from KREW_DOC_TYP_T ");
        retval.append(DocumentSearchConstants.DOCUMENT_TYPE_ALIAS);
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
        retval.append("join KREW_DOC_HDR_T ");
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(" on (");
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".DOC_TYP_ID = ");
        retval.append(DocumentSearchConstants.DOCUMENT_TYPE_ALIAS);
        retval.append(".DOC_TYP_ID) ");

        if (isActionRequestJoinRequired(personIdentifiers)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append("join KREW_ACTN_RQST_T ");
            retval.append(DocumentSearchConstants.ACTION_REQUEST_ALIAS);
            retval.append(" on (");
            retval.append(DocumentSearchConstants.ACTION_REQUEST_ALIAS);
            retval.append(".DOC_HDR_ID = ");
            retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
            retval.append(".DOC_HDR_ID) ");
        }
        
        if (isActionTakenJoinRequired(personIdentifiers)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append("join KREW_ACTN_TKN_T ");
            retval.append(DocumentSearchConstants.ACTION_TAKEN_ALIAS);
            retval.append(" on (");
            retval.append(DocumentSearchConstants.ACTION_TAKEN_ALIAS);
            retval.append(".DOC_HDR_ID = ");
            retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
            retval.append(".DOC_HDR_ID) ");
        }
        
        if (isRouteNodeJoinRequired(criteria)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append("join KREW_RTE_NODE_INSTN_T ");
            retval.append(DocumentSearchConstants.ROUTE_NODE_INSTANCE_ALIAS);
            retval.append(" on (");
            retval.append(DocumentSearchConstants.ROUTE_NODE_INSTANCE_ALIAS);
            retval.append(".DOC_HDR_ID = ");
            retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
            retval.append(".DOC_HDR_ID)");
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append("join KREW_RTE_NODE_T ");
            retval.append(DocumentSearchConstants.ROUTE_NODE_ALIAS);
            retval.append(" on (");
            retval.append(DocumentSearchConstants.ROUTE_NODE_ALIAS);
            retval.append(".ROUTE_NODE_ID = ");
            retval.append(DocumentSearchConstants.ROUTE_NODE_INSTANCE_ALIAS);
            retval.append(".ROUTE_NODE_ID) ");
        }
        
        if (isStatusTransitionJoinRequired(criteria)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append("join KREW_APP_DOC_STAT_TRAN_T ");
            retval.append(DocumentSearchConstants.DOCUMENT_STATUS_TRAN_ALIAS);
            retval.append(" on (");
            retval.append(DocumentSearchConstants.DOCUMENT_STATUS_TRAN_ALIAS);
            retval.append(".DOC_HDR_ID = ");
            retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
            retval.append(".DOC_HDR_ID) ");
        }
        
        
        // create the index from clause - pass in string builder to 
        // hold the associated index table where clause
        String sql = getIndexTableFromClause(criteria, searchFields, indexTableWhereClause);
        
        if (StringUtils.isNotBlank(sql)) {
            retval.append(sql);
        }
        
        return retval.toString();
    }

    /**
     * 
     * @param personIdentifiers
     * @return 
     */
    protected boolean isActionRequestJoinRequired(Map <String, List<String>> personIdentifiers) {
        return (personIdentifiers.containsKey(DocumentSearchConstants.VIEWER_PRINCIPAL_ID_KEY)
            || personIdentifiers.containsKey(DocumentSearchConstants.VIEWER_GROUP_ID_KEY));
    }
    
    /**
     * 
     * @param personIdentifiers
     * @return 
     */
    protected boolean isActionTakenJoinRequired(Map <String, List<String>> personIdentifiers) {
        return personIdentifiers.containsKey(DocumentSearchConstants.APPROVER_PRINCIPAL_ID_KEY);
    }

    /**
     * 
     * @param criteria
     * @return 
     */
    protected boolean isRouteNodeJoinRequired(DocumentSearchCriteria criteria) {
        return StringUtils.isNotBlank(criteria.getRouteNodeName());
    }
    
    /**
     * 
     * @param criteria
     * @return 
     */
    protected boolean isStatusTransitionJoinRequired(DocumentSearchCriteria criteria) {
        return ((criteria.getDateApplicationDocumentStatusChangedFrom() != null) 
                || (criteria.getDateApplicationDocumentStatusChangedTo() != null));
    }


    /**
     * 
     * @param criteria
     * @param searchFields
     * @param personIdentifiers
     * @param indexTableWhereClause
     * @return 
     */
    protected String getWhereClause(DocumentSearchCriteria criteria, 
        List<RemotableAttributeField> searchFields,
        Map <String, List<String>> personIdentifiers,
        StringBuilder indexTableWhereClause) {
        StringBuilder retval = new StringBuilder(512);
        
        String and = "";
        
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[1]);
        retval.append("where ");
        
        if (StringUtils.isNotBlank(criteria.getDocumentId())) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
            retval.append(".DOC_HDR_ID ");
            retval.append(DocumentSearchUtils.getSearchClause(DocumentSearchConstants.STRING_TYPE_CODE, criteria.getDocumentId()));
            and = DocumentSearchConstants.AND_OPERATOR;
        }

        List <String> l = personIdentifiers.get(DocumentSearchConstants.INITIATOR_PRINCIPAL_ID_KEY);
        
        if (l != null) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            if (l.isEmpty()) {
                retval.append(DocumentSearchConstants.NO_OP_SEARCH_COMPARISON);
            } else {
                retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
                retval.append(".INITR_PRNCPL_ID ");
                retval.append(DocumentSearchUtils.getSearchClause(DocumentSearchConstants.STRING_TYPE_CODE, l));
            }
            
            and = DocumentSearchConstants.AND_OPERATOR;
        }
        

        if (StringUtils.isNotBlank(criteria.getApplicationDocumentId())) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
            retval.append(".APP_DOC_ID ");
            retval.append(DocumentSearchUtils.getSearchClause(DocumentSearchConstants.STRING_TYPE_CODE, criteria.getApplicationDocumentId()));

            and = DocumentSearchConstants.AND_OPERATOR;
        }
        
        if (StringUtils.isNotBlank(criteria.getTitle())) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
            retval.append(".TTL ");
            retval.append(DocumentSearchUtils.getSearchClause(DocumentSearchConstants.STRING_TYPE_CODE, criteria.getTitle().trim().replace("\'", "\'\'")));
            and = DocumentSearchConstants.AND_OPERATOR;
        }

        String sql =  getDocTypeNameWhereSql(criteria);
        
        if (StringUtils.isNotBlank(sql)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(sql);
            and = DocumentSearchConstants.AND_OPERATOR;
        }
        
        sql = getDocumentStatusSql(criteria);

        if (StringUtils.isNotBlank(sql)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(sql);
            and = DocumentSearchConstants.AND_OPERATOR;
        }
        
        sql = getDocRouteNodeSql(criteria);

        if (StringUtils.isNotBlank(sql)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(sql);
            and = DocumentSearchConstants.AND_OPERATOR;
        }

        if ((criteria.getDateCreatedFrom() != null) ||  (criteria.getDateCreatedTo() != null)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(getFromToDateSearchClause(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS, 
                    "CRTE_DT", criteria.getDateCreatedFrom(), criteria.getDateCreatedTo()));
            and = DocumentSearchConstants.AND_OPERATOR;
        }
        
        if ((criteria.getDateApprovedFrom() != null) ||  (criteria.getDateApprovedTo() != null)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(getFromToDateSearchClause(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS, 
                "APRV_DT", criteria.getDateApprovedFrom(), criteria.getDateApprovedTo()));
            and = DocumentSearchConstants.AND_OPERATOR;
        }
        
        if ((criteria.getDateLastModifiedFrom() != null) ||  (criteria.getDateLastModifiedTo() != null)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(getFromToDateSearchClause(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS, 
                "STAT_MDFN_DT", criteria.getDateLastModifiedFrom(), criteria.getDateLastModifiedTo()));
            and = DocumentSearchConstants.AND_OPERATOR;
        }

        if ((criteria.getDateFinalizedFrom() != null) ||  (criteria.getDateFinalizedTo() != null)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(getFromToDateSearchClause(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS, 
                "FNL_DT", criteria.getDateFinalizedFrom(), criteria.getDateFinalizedTo()));
            and = DocumentSearchConstants.AND_OPERATOR;
        }

        List<String> applicationDocumentStatuses = criteria.getApplicationDocumentStatuses();
        
        // deal with legacy usage of applicationDocumentStatus (which is deprecated)
        if (!StringUtils.isBlank(criteria.getApplicationDocumentStatus())) {
            if (!criteria.getApplicationDocumentStatuses().contains(criteria.getApplicationDocumentStatus())) {
                applicationDocumentStatuses = new ArrayList<String>(criteria.getApplicationDocumentStatuses());
                applicationDocumentStatuses.add(criteria.getApplicationDocumentStatus());
            }
        }

        if ((applicationDocumentStatuses != null) && !applicationDocumentStatuses.isEmpty()) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            List <String> inlist = escapeStrings(applicationDocumentStatuses);

            if (isStatusTransitionJoinRequired(criteria)) {
                retval.append(DocumentSearchConstants.DOCUMENT_STATUS_TRAN_ALIAS);
                retval.append(".APP_DOC_STAT_TO ");
            } else {
                retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
                retval.append(".APP_DOC_STAT ");
            }
            
            retval.append(DocumentSearchUtils.getSearchClause(DocumentSearchConstants.STRING_TYPE_CODE, inlist));
            and = DocumentSearchConstants.AND_OPERATOR;
        }

        if ((criteria.getDateApplicationDocumentStatusChangedFrom() != null) 
            ||  (criteria.getDateApplicationDocumentStatusChangedTo() != null)) {
            retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(getFromToDateSearchClause(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS, 
                "APP_DOC_STAT_MDFN_DT", criteria.getDateApplicationDocumentStatusChangedFrom(), 
                criteria.getDateApplicationDocumentStatusChangedTo()));
            and = DocumentSearchConstants.AND_OPERATOR;
        }

        if (StringUtils.isNotBlank(indexTableWhereClause.toString())) {
            indexTableWhereClause.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
            retval.append(and);
            retval.append(indexTableWhereClause.toString());
            and = DocumentSearchConstants.AND_OPERATOR;
        }
        
        return retval.toString();
    }
    
    protected String getOrderByClause(DocumentSearchCriteria criteria) {
        StringBuilder retval = new StringBuilder(128);
        
        retval.append(DocumentSearchConstants.FORMAT_OFFSET[1]);
        retval.append("order by ");
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".CRTE_DT desc ");

        return retval.toString();
    }

    private String getAttributeKeyCdWhereClauseSegment(String tableAlias, String attName) {
        StringBuilder retval = new StringBuilder(128);
        
        retval.append(tableAlias);
        retval.append(".KEY_CD = '");

        if (attName.contains(KewApiConstants.DOCUMENT_ATTRIBUTE_FIELD_PREFIX)) {
            retval.append(getDbPlatform().escapeString(attName.replaceFirst(KewApiConstants.DOCUMENT_ATTRIBUTE_FIELD_PREFIX, "")));
        } else {
            retval.append(getDbPlatform().escapeString(attName));
        }

        retval.append("' ");
        
        return retval.toString();
    }
    
     /**
      * 
      * @param indexTableName
      * @param tableAlias
      * @param searchValues
      * @param caseInsensitiveSearch
      * @return 
      */
    protected String getAttributeValWhereClauseSegment(String indexTableName, 
        String tableAlias, List<String> searchValues, boolean caseInsensitiveSearch) {
        StringBuilder retval = new StringBuilder(256);
        
        char typeCode = DocumentSearchConstants.TYPE_CODE_BY_ATTRIBUTE_TABLE_MAP.get(indexTableName);

        boolean useDefaultProcessing = true;

        // special handling for dates to handle between string - fromdate..todate
        if (typeCode == DocumentSearchConstants.DATE_TYPE_CODE) {
            if (searchValues.get(0).contains(DocumentSearchConstants.DATE_BETWEEN_OPERATOR)) {
                DateTime[] dates = getFromToDatesFromBewteenOperatorString(searchValues.get(0));
                if (dates != null) {
                    useDefaultProcessing = false;
                    retval.append(getFromToDateSearchClause(tableAlias, "VAL", dates[0], dates[1]));
                }
            } 
        } 

        if (useDefaultProcessing) {
            boolean ucaseRequired = isUpperCaseRequired(typeCode, searchValues, caseInsensitiveSearch);
            
            if (ucaseRequired) {
                retval.append("{fn ucase(");
            }
            retval.append(tableAlias);
            retval.append(".VAL");
            
            if (ucaseRequired) {
                retval.append(")}");
            }
            
            retval.append(" ");
            retval.append(DocumentSearchUtils.getSearchClause(typeCode, searchValues, caseInsensitiveSearch));
        }

        return retval.toString();
    }
    
    /**
     * check to see if comparison values contains characters, if all comparison values
     * are numeric no uppercase on query column is required
     * @param typeCode
     * @param values
     * @param caseInsenstiveSearch
     * @return 
     */
    private boolean isUpperCaseRequired(char typeCode, List<String> values, boolean caseInsenstiveSearch) {
        boolean retval = false;
        
        if (caseInsenstiveSearch && (DocumentSearchConstants.STRING_TYPE_CODE == typeCode)) {
            // only need to do uppecase if not all numeric
            for (String s : values) {
                if (!StringUtils.isNumeric(s)) {
                    retval = true;
                    break;
                }
            }
        }
        
        return retval;
    }
    
    private String getIndexTableFromClause(DocumentSearchCriteria criteria, 
        List<RemotableAttributeField> searchFields, StringBuilder indexTableWhereClause) {
        StringBuilder retval = new StringBuilder(512);
        Map<String, StringBuilder> joins = new HashMap<String, StringBuilder>();
        Map<String, Integer> aliasIndex = new HashMap<String, Integer>();
    
        String and = "";
        for (String attname : criteria.getDocumentAttributeValues().keySet()) {
            RemotableAttributeField searchField = getSearchFieldByName(attname, searchFields);
            List<String> searchValues = criteria.getDocumentAttributeValues().get(attname);

            if (!searchValues.isEmpty() && !attname.contains(KRADConstants.CHECKBOX_PRESENT_ON_FORM_ANNOTATION)) {
                String indexTableName = DocumentSearchInternalUtils.getAttributeTableName(searchField);

                Integer indx = aliasIndex.get(indexTableName);
                
                if (indx == null) {
                    aliasIndex.put(indexTableName, indx = Integer.valueOf(1));
                } else {
                    aliasIndex.put(indexTableName, indx = Integer.valueOf(indx + 1));
                }
                
                boolean caseInsensitiveSearch = !DocumentSearchInternalUtils.isLookupCaseSensitive(searchField);
                
                StringBuilder sql = joins.get(indexTableName);
                
                if (sql == null) {
                    joins.put(indexTableName, sql = new StringBuilder(512));
                } 
                
                String indexAlias = (DocumentSearchConstants.DOCUMENT_INDEX_ALIAS + DocumentSearchConstants.TYPE_CODE_BY_ATTRIBUTE_TABLE_MAP.get(indexTableName) + indx);
                sql.append("join ");
                sql.append(indexTableName);
                sql.append(" ");
                sql.append(indexAlias);
                sql.append(" on (");
                sql.append(indexAlias);
                sql.append(".DOC_HDR_ID = ");
                sql.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
                sql.append(".DOC_HDR_ID) ");


                if (StringUtils.isNotBlank(and)) {
                    indexTableWhereClause.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
                }
                indexTableWhereClause.append(and);
                indexTableWhereClause.append(getAttributeKeyCdWhereClauseSegment(indexAlias, attname));
                indexTableWhereClause.append(DocumentSearchConstants.AND_OPERATOR);
                indexTableWhereClause.append(getAttributeValWhereClauseSegment(indexTableName, indexAlias, searchValues, caseInsensitiveSearch));
                and = DocumentSearchConstants.AND_OPERATOR;
            }
        }

        for (String t : DocumentSearchConstants.DOCUMENT_INDEX_TABLES) {
            StringBuilder join = joins.get(t);
            if (join != null) {
                retval.append(DocumentSearchConstants.FORMAT_OFFSET[2]);
                retval.append(join.toString());
            }
        }
        
        return retval.toString();
    }
    
    private DateTime[] getFromToDatesFromBewteenOperatorString(String input) {
        DateTime[] retval = null;
        
        if (StringUtils.isNotBlank(input) && input.contains(DocumentSearchConstants.DATE_BETWEEN_OPERATOR)) {
            int pos1 = input.indexOf(".");
            try {
                retval = new DateTime[] {new DateTime(DocumentSearchConstants.INPUT_DATE_FORMAT.parse(input.substring(0, pos1))), 
                    new DateTime(DocumentSearchConstants.INPUT_DATE_FORMAT.parse(input.substring(pos1+2)))};
            } 
            
            catch (ParseException ex) {
                LOG.error(ex.toString(), ex);
            }
        }
        
        return retval;
    }
    /**
     * get comparison clause for date range checks
     * @param tableAlias
     * @param fieldName
     * @param fromDate
     * @param toDate
     * @return 
     */
    protected String getFromToDateSearchClause(String tableAlias, String fieldName, DateTime fromDate, DateTime toDate) {
        StringBuilder retval = new StringBuilder(128);

        Date nextDay = null;
        if (toDate != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(toDate.toDate());
            c.add(Calendar.DATE, 1);
            nextDay = c.getTime();
        }
        
        if ((fromDate != null) && (toDate != null)) {
            retval.append(tableAlias);
            retval.append(".");
            retval.append(fieldName);
            retval.append(" >= {d '");
            retval.append(DocumentSearchConstants.JDBC_DATE_ESCAPE_FORMAT.format(fromDate.toDate()));
            retval.append("'} and ");
            retval.append(tableAlias);
            retval.append(".");
            retval.append(fieldName);
            retval.append(" < {d '");
            retval.append(DocumentSearchConstants.JDBC_DATE_ESCAPE_FORMAT.format(nextDay));
            retval.append("'} ");
        } else if (fromDate != null) {
            retval.append(tableAlias);
            retval.append(".");
            retval.append(fieldName);
            retval.append(" >= {d '");
            retval.append(DocumentSearchConstants.JDBC_DATE_ESCAPE_FORMAT.format(fromDate.toDate()));
            retval.append("'} ");
        } else if (toDate != null) {
            retval.append(tableAlias);
            retval.append(".");
            retval.append(fieldName);
            retval.append(" < {d '");
            retval.append(DocumentSearchConstants.JDBC_DATE_ESCAPE_FORMAT.format(toDate.toDate()));
            retval.append("'} ");
        } 
        
        return retval.toString();
    }
    
    /**
     * get principal ids for principal name
     * @param principalName
     * @return 
     */
    protected List <String> getPrincipalIdListFromPrincipalName(String principalName) {
        List <String> retval = new ArrayList<String>();

        if (StringUtils.isNotBlank(principalName)) {
            // just eat any exceptions 
            try {
                List <Person> plist = new ArrayList<Person>();
                if (DocumentSearchUtils.hasWildcard(principalName)) {
                    Map<String, String> m = new HashMap<String, String>();
                    m.put(DocumentSearchConstants.PRINCIPAL_NAME_KEY, principalName);

                    // This will search for people with the ability for the valid operands.
                    plist = getPersonService().findPeople(m, true);
                } else {
                    Person p = getPersonService().getPersonByPrincipalName(principalName.trim());
                    if (p != null) {
                        plist.add(p);
                    } 
                }

                if (!plist.isEmpty()) {
                    for(Person p: plist){
                        retval.add(p.getPrincipalId());
                    }
                }
            }

            catch (Exception ex) {
                LOG.warn("exception thrown in getPrincipalIdListFromPrincipalName()", ex);
            }
        }
        
        return retval;
    }
    
    /**
     * create a map of principal ids for identity objects required for the search
     * @param criteria
     * @return 
     */
    protected Map<String, List<String>> loadPersonIdentifiers(DocumentSearchCriteria criteria) {
        Map <String, List<String>> retval = new HashMap<String, List<String>>();

        if (StringUtils.isNotBlank(criteria.getInitiatorPrincipalId())) {
            retval.put(DocumentSearchConstants.INITIATOR_PRINCIPAL_ID_KEY, Arrays.asList(new String[] {criteria.getInitiatorPrincipalId()}));
        } else if (StringUtils.isNotBlank(criteria.getInitiatorPrincipalName())) {
            retval.put(DocumentSearchConstants.INITIATOR_PRINCIPAL_ID_KEY, getPrincipalIdListFromPrincipalName(criteria.getInitiatorPrincipalName()));
        }
        
        if (StringUtils.isNotBlank(criteria.getApproverPrincipalId())) {
            retval.put(DocumentSearchConstants.APPROVER_PRINCIPAL_ID_KEY, Arrays.asList(new String[] {criteria.getApproverPrincipalId()}));
        } else if (StringUtils.isNotBlank(criteria.getInitiatorPrincipalName())) {
            retval.put(DocumentSearchConstants.APPROVER_PRINCIPAL_ID_KEY, getPrincipalIdListFromPrincipalName(criteria.getApproverPrincipalName()));
        }

        if (StringUtils.isNotBlank(criteria.getViewerPrincipalId())) {
            retval.put(DocumentSearchConstants.VIEWER_PRINCIPAL_ID_KEY, Arrays.asList(new String[] {criteria.getViewerPrincipalId()}));
        } else if (StringUtils.isNotBlank(criteria.getViewerPrincipalName())) {
            retval.put(DocumentSearchConstants.VIEWER_PRINCIPAL_ID_KEY, getPrincipalIdListFromPrincipalName(criteria.getViewerPrincipalName()));
        }
        
        if (StringUtils.isNotBlank(criteria.getGroupViewerId())) {
            retval.put(DocumentSearchConstants.VIEWER_GROUP_ID_KEY, Arrays.asList(new String[] {criteria.getGroupViewerId()}));
        }
        

        return retval;
    }
    
    /**
     * 
     * @param criteria
     * @param searchFields
     * @param maxRows
     * @return 
     */
    private String getSearchSql(DocumentSearchCriteria criteria, List<RemotableAttributeField> searchFields) {
        StringBuilder retval = new StringBuilder(1024);
        
        // load identity-related informtion for use in query if required
        Map <String, List<String>> personIdentifiers = loadPersonIdentifiers(criteria);
        
        retval.append(getSelectClause());
        
        // put a buffer into from clause to hold where information
        // for index table joins found during from generation
        StringBuilder indexTableWhereClause = new StringBuilder(256);
        retval.append(getFromClause(criteria, searchFields, personIdentifiers, indexTableWhereClause));
        retval.append(getWhereClause(criteria, searchFields, personIdentifiers, indexTableWhereClause));
        retval.append(getOrderByClause(criteria));
        
        if (LOG.isInfoEnabled()) {
            LOG.info("document search sql");
            LOG.info("------------------------------------------------------------------------------------------");
            LOG.info(retval.toString());
        }

        return retval.toString();
    }

    /**
     * 
     * @param fieldName
     * @param searchFields
     * @return 
     */
    protected RemotableAttributeField getSearchFieldByName(String fieldName, List<RemotableAttributeField> searchFields) {
        for (RemotableAttributeField searchField : searchFields) {
            if (searchField.getName().equals(fieldName)
                    || searchField.getName().equals(KewApiConstants.DOCUMENT_ATTRIBUTE_FIELD_PREFIX + fieldName)) {
                return searchField;
            }
        }
        throw new IllegalStateException("Failed to locate a RemotableAttributeField for fieldName=" + fieldName);
    }

    /**
     * do a db string escape on all string in input list
     * @param inputList
     * @return 
     */
    protected List <String> escapeStrings(List <String> inputList) {
        List <String> retval = new ArrayList<String>();

        for (String s : inputList) {
            retval.add(getDbPlatform().escapeString(s.trim()));
        }
        
        return retval;
    }

    /**
     * 
     * @param criteria
     * @return 
     */
    protected String getDocTypeNameWhereSql(DocumentSearchCriteria criteria) {
        StringBuilder retval = new StringBuilder(128);
        
        List<String> documentTypeNames = new ArrayList<String>();
        
        if (StringUtils.isNotBlank(criteria.getDocumentTypeName())) {
            documentTypeNames.add(criteria.getDocumentTypeName());
        }
        
        documentTypeNames.addAll(criteria.getAdditionalDocumentTypeNames());
        
        String or = "";
        int namecnt = 0;
        
        
        // recursively add child doc types to list
        if (!documentTypeNames.isEmpty()) {
            List <String> childDocTypeNames = new ArrayList<String>();
            for (String documentTypeName : documentTypeNames) {
                if (StringUtils.isNotBlank(documentTypeName)) {
                    DocumentType docType = getDocumentTypeService().findByNameCaseInsensitive(documentTypeName.trim());
                    addChildDocumentTypes(docType, childDocTypeNames);
                    or = DocumentSearchConstants.OR_OPERATOR;
                }
            }
            
            documentTypeNames.addAll(childDocTypeNames);
        }
        
        if (!documentTypeNames.isEmpty()) {
            retval.append("{fn ucase(");
            retval.append(DocumentSearchConstants.DOCUMENT_TYPE_ALIAS);
            retval.append(".DOC_TYP_NM)} ");
            retval.append(DocumentSearchUtils.getSearchClause(DocumentSearchConstants.STRING_TYPE_CODE, documentTypeNames, true));
        }
        
        return retval.toString();
    }
    
    
    /**
     * 
     * @param childDocTypes
     * @param childDocTypeNames 
     */
    private void addChildDocumentTypes(DocumentType docType, List <String> childDocTypeNames) {
        if (docType != null) {
            List<DocumentType> childDocTypes = (List<DocumentType>)docType.getChildrenDocTypes();
        
            if ((childDocTypes != null) && !childDocTypes.isEmpty()) {
                for (DocumentType dt : childDocTypes) {
                    childDocTypeNames.add(dt.getName());
                    addChildDocumentTypes(dt, childDocTypeNames);
                }
            }
        }
    }
    
    /**
     * 
     * @param criteria
     * @return 
     */
    protected String getDocumentStatusSql(DocumentSearchCriteria criteria) {
        StringBuilder retval = new StringBuilder(128);
        
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ALIAS);
        retval.append(".DOC_HDR_STAT_CD ");

        List<String> statusCodes = new ArrayList<String>();
        
        if (((criteria.getDocumentStatuses() == null) || criteria.getDocumentStatuses().isEmpty())
                && ((criteria.getDocumentStatusCategories() == null) || criteria.getDocumentStatusCategories().isEmpty())) {
            retval.append(" != '");
            retval.append(DocumentStatus.INITIATED.getCode());
            retval.append("' ");
        } else {
            // include all given document statuses
            Set<String> hs = new HashSet<String>();

            // add all statuses from each category
            for (DocumentStatusCategory category : criteria.getDocumentStatusCategories()) {
                for (DocumentStatus ds : DocumentStatus.getStatusesForCategory(category)) {
                    if (!hs.contains(ds.getCode())) {
                        hs.add(ds.getCode());
                        statusCodes.add(ds.getCode());
                    }
                }
            }

            retval.append(DocumentSearchUtils.getSearchClause(DocumentSearchConstants.STRING_TYPE_CODE, escapeStrings(statusCodes)));
        }
        
        return retval.toString();
    }

    /**
     * 
     * @param documentTypeName
     * @return 
     */
    protected DocumentType getDocumentTypeByName(String documentTypeName) {
        DocumentType retval = null;
        if (!org.apache.commons.lang.StringUtils.isEmpty(documentTypeName)) {
            retval = getDocumentTypeService().findByNameCaseInsensitive(documentTypeName);
            if (retval == null) {
                throw new RuntimeException("No Valid Document Type Found for document type name '" + documentTypeName + "'");
            }
        }
        
        return retval;
    }
    
    /**
     * 
     * @param criteria
     * @return 
     */
    protected String getDocRouteNodeSql(DocumentSearchCriteria criteria) {
        StringBuilder retval = new StringBuilder(256);
            // -1 is the default 'blank' choice from the route node drop down a number is used 
        // because the ojb RouteNode object is used to render the node choices on the form.
        if (StringUtils.isNotBlank(criteria.getRouteNodeName())) {
            RouteNodeLookupLogic routeNodeLookupLogic = criteria.getRouteNodeLookupLogic();
            
            if (routeNodeLookupLogic == null) {
                routeNodeLookupLogic= RouteNodeLookupLogic.EXACTLY;
            }
            
            retval.append(DocumentSearchConstants.ROUTE_NODE_ALIAS);
            retval.append(".NM ");
            if (RouteNodeLookupLogic.EXACTLY == routeNodeLookupLogic) {
        		retval.append("= '");
                retval.append(getDbPlatform().escapeString(criteria.getRouteNodeName()));
                retval.append("' ");
            } else {
                String comma = "";
                retval.append("in (");
                List<RouteNode> routeNodes = getRouteNodeService().getFlattenedNodes(getDocumentTypeByName(criteria.getDocumentTypeName()), true);
                for (RouteNode routeNode : routeNodes) {
                    if (!criteria.getRouteNodeName().equals(routeNode.getRouteNodeName())) {
                        // below logic should be to add the current node to the criteria if we haven't found the specified node
                        // and the logic qualifier is 'route nodes before specified'... or we have found the specified node and
                        // the logic qualifier is 'route nodes after specified'
                        if ((RouteNodeLookupLogic.BEFORE == routeNodeLookupLogic) 
                                || (RouteNodeLookupLogic.AFTER == routeNodeLookupLogic)) {
                            retval.append(comma);
                            retval.append("'");
                            retval.append(routeNode.getRouteNodeName());
                            retval.append("'");
                            comma = ",";
                        }
                    }
                }
                    
                if (StringUtils.isEmpty(comma)) {
                    retval.append("''");
                }
                
                retval.append(") ");
            }
        }

        return retval.toString();
    }

    /**
     * loads document attributes for all returned documents in a minimum number of queries
     * 
     * @param results
     * @param statement
     * @throws SQLException 
     */
    private void loadDocumentAttributes(List <DocumentInformation> results, int  maxRows) throws Exception {
        Map <String, DocumentInformation> docmap = new HashMap<String, DocumentInformation>();

        // load up a map to facilitate adding attributes
        for (DocumentInformation docinfo : results) {
            docmap.put(docinfo.getDocumentId(), docinfo);
        }

        List <IndexTableSearchHandler> handlers = new ArrayList<IndexTableSearchHandler>();

        for (String tname : DocumentSearchConstants.DOCUMENT_INDEX_TABLES) {
            handlers.add(new IndexTableSearchHandler(dataSource, tname, docmap.keySet(), maxRows));
        }

        while (!isIndexSearchComplete(handlers)) {
            try {
                Thread.sleep(DocumentSearchConstants.DOCUMENT_INDEX_SEARCH_SLEEP_TIME);
            } 

            catch (InterruptedException ex) {};
        }

        Exception exception = getIndexSearchException(handlers);

        if (exception != null) {
            throw exception;
        }
        
        for (DocumentInformation docinfo : results) {
            for (IndexTableSearchHandler h : handlers) {
                List <DocumentAttribute> attlist = h.getAttributes(docinfo.getDocumentId());
                if (attlist != null) {
                    docinfo.getAttributes().addAll(attlist);
                }
            }
        }
    }

    private boolean isIndexSearchComplete(List <IndexTableSearchHandler> handlers) {
        boolean retval = true;
        for (IndexTableSearchHandler h : handlers) {
            if (!h.isDone()) {
                retval = false;
                break;
            }
        }
        
        return retval;
    }

    private Exception getIndexSearchException(List <IndexTableSearchHandler> handlers) {
        Exception retval = null;
        
        for (IndexTableSearchHandler h : handlers) {
            if (h.getException() != null) {
                retval = h.getException();
                break;
            }
        }
        
        return retval;
    }
    
    public void setDbPlatform(DatabasePlatform dbPlatform) {
        this.dbPlatform = dbPlatform;
    }

    protected DatabasePlatform getDbPlatform() {
        if (dbPlatform == null) {
            dbPlatform = (DatabasePlatform) GlobalResourceLoader.getService(RiceConstants.DB_PLATFORM);
        }
        return dbPlatform;
    }
    
    protected PersonService getPersonService() {
        if (personService == null) {
            personService = KimApiServiceLocator.getPersonService();
        }
        
        return personService;
    }
    
    protected IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = KimApiServiceLocator.getIdentityService();
        }
        
        return identityService;
    }
    
    protected DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = CoreApiServiceLocator.getDateTimeService();
        }
        
        return dateTimeService;
    }
    
    protected DocumentTypeService getDocumentTypeService() {
        if (documentTypeService == null) {
            documentTypeService = KEWServiceLocator.getDocumentTypeService();
        }
        
        return documentTypeService;
    }

    protected DocumentSearchCustomizationMediator getDocumentSearchCustomizationMediator() {
        if (documentSearchCustomizationMediator == null) {
            documentSearchCustomizationMediator = KEWServiceLocator.getDocumentSearchCustomizationMediator();
        }
        
        return documentSearchCustomizationMediator;
    }
    
    protected RouteNodeService getRouteNodeService() {
        if (routeNodeService == null) {
            routeNodeService = KEWServiceLocator.getRouteNodeService();
        }
        
        return routeNodeService;
    }
    
    protected boolean isUsingAtLeastOneSearchAttribute(DocumentSearchCriteria criteria) {
        return (!criteria.getDocumentAttributeValues().isEmpty() || StringUtils.isNotBlank(criteria.getDocumentTypeName()));
    }

    public void setUseInternalSearch(boolean useInternalSearch) {
        this.useInternalSearch = useInternalSearch;
    }

    public void setDatabaseSpecificCodeAllowed(boolean databaseSpecificCodeAllowed) {
        this.databaseSpecificCodeAllowed = databaseSpecificCodeAllowed;
    }
    
    private boolean isOracleDatabase() {
        return (getDbPlatform() instanceof OracleDatabasePlatform);
    }

    private boolean isMysqlDatabase() {
        return (getDbPlatform() instanceof MySQLDatabasePlatform);
    }

    public void setCustomFetchSize(int customFetchSize) {
        this.customFetchSize = customFetchSize;
    }
}
