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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.kew.api.document.attribute.DocumentAttribute;
import org.kuali.rice.kew.docsearch.SearchableAttributeValue;


// UAF-6 - Performance improvements to improve user experience for AWS deployment 
public class IndexTableSearchHandler extends Thread {
    private static final Logger LOG = Logger.getLogger(IndexTableSearchHandler.class);
    private Exception exception;
    private boolean done = false;
    private DataSource dataSource;
    private String indexTableName;
    private int fetchSize;
    private Collection <String> docids;
    private Map<String, List <DocumentAttribute>> attributeMap = new HashMap<String, List<DocumentAttribute>>();
    
    public IndexTableSearchHandler(DataSource dataSource, String indexTableName, Collection <String> docids, int fetchSize) {
        this.indexTableName = indexTableName;
        this.dataSource = dataSource;
        this.docids = docids;
        this.fetchSize = fetchSize;
        start();
    }

    @Override
    public void run() {
        Connection conn = null;
        ResultSet res = null;
        Statement stmt = null;
        try {
            long start = System.currentTimeMillis();
            conn = dataSource.getConnection();
            conn.setReadOnly(true);
            
            char dataTypeCode = DocumentSearchConstants.TYPE_CODE_BY_ATTRIBUTE_TABLE_MAP.get(indexTableName);
            stmt = conn.createStatement();
            stmt.setFetchSize(fetchSize);
            
            
            for (List<String> inlist : DocumentSearchUtils.getInLists(docids)) {
                res = stmt.executeQuery( getSqlSelect(inlist));
                
                while (res.next()) {
                    String docid = res.getString(1);
                    String keyCd = res.getString(2);
                    String val = res.getString(3);
                    
                    if (StringUtils.isNotBlank(val)) {
                        List <DocumentAttribute> attlist = attributeMap.get(docid);
                    
                        if (attlist == null) {
                            attributeMap.put(docid, attlist = new ArrayList<DocumentAttribute>());
                        }
                    
                        attlist.add(buildDocumentAttribute(dataTypeCode, keyCd, res));
                    }
                }
            } 
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("index table query [" + indexTableName + "] elapsed time(sec): " + (System.currentTimeMillis() - start)/1000);
            }
        }

        catch (Exception ex) {
            exception = ex;
        }
        
        finally {
            done = true;
            DocumentSearchUtils.closeDbObjects(conn, stmt, res);
        }
    }
    
    private String getSqlSelect(List <String> docids) {
        StringBuilder retval = new StringBuilder(256);
        
        retval.append("select distinct ");
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ID_COLUMN);
        retval.append(", ");
        retval.append(DocumentSearchConstants.KEY_CODE_COLUMN);
        retval.append(", ");
        retval.append(DocumentSearchConstants.VAL_COLUMN);
        retval.append(" from "); 
        retval.append(indexTableName);
        retval.append(" where ");
        retval.append(DocumentSearchConstants.DOCUMENT_HEADER_ID_COLUMN);
        retval.append(" ");
        retval.append(DocumentSearchUtils.getSearchClause(DocumentSearchConstants.STRING_TYPE_CODE, docids));

        if (LOG.isDebugEnabled()) {
            LOG.debug("index table select: " + retval.toString());
        }
        
        return retval.toString();
    }
   
    public Exception getException() {
        return exception;
    }

    public boolean isDone() {
        return done;
    }

    public String getIndexTableName() {
        return indexTableName;
    }

    public List<DocumentAttribute> getAttributes(String docid) {
        return attributeMap.get(docid);
    }

    /**
     * 
     * @param dataTypeCode
     * @param keyCd
     * @param res
     * @return
     * @throws SQLException 
     */
    protected DocumentAttribute buildDocumentAttribute(char dataTypeCode, String keyCd, ResultSet res) throws SQLException {
        DocumentAttribute retval = null;
        
        SearchableAttributeValue att = DocumentSearchConstants.SEARCHABLE_ATTRUBUTE_VALUE_BY_TYPE_CODE.get(dataTypeCode);

        if (att != null) {
            att.setSearchableAttributeKey(keyCd);
            att.setupAttributeValue(res, DocumentSearchConstants.VAL_COLUMN);
            
            if (att.getSearchableAttributeValue() != null) {
                retval = att.toDocumentAttribute();
            }
        }

        return retval;
    }
}
