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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.kuali.rice.kew.docsearch.DocumentSearchInternalUtils;
import org.kuali.rice.kew.docsearch.SearchableAttributeValue;


// UAF-6 - Performance improvements to improve user experience for AWS deployment 
public class DocumentSearchConstants {
    public static final SimpleDateFormat JDBC_DATE_ESCAPE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    public static final int DEFAULT_FETCH_SIZE = 10;
    
    public static final String DEFAULT_SEARCH_SQL_PREFIX = "select * from (";
    public static final String DEFAULT_SEARCH_SQL_SUFFIX = ") FINAL_SEARCH";

    // database related constants
    public static final int MAX_INLIST_SIZE = 900;
    public static final String DOCUMENT_TYPE_ALIAS   = "DT";
    public static final String DOCUMENT_HEADER_ALIAS = "DH";
    public static final String ACTION_REQUEST_ALIAS = "ACR";
    public static final String ACTION_TAKEN_ALIAS = "ACT";
    public static final String ROUTE_NODE_ALIAS = "RN";
    public static final String ROUTE_NODE_INSTANCE_ALIAS = "RNI";
    public static final String DOCUMENT_STATUS_TRAN_ALIAS = "DST";
    public static final String DOCUMENT_INDEX_ALIAS = "DI";
    public static final String NO_OP_SEARCH_COMPARISON = " 1 = 0 ";
    public static final String DATE_BETWEEN_OPERATOR = "..";
    public static final String AND_OPERATOR = " and ";
    public static final String OR_OPERATOR = " or ";
    
    // keys for some identity/role related map entries
    public static final String PRINCIPAL_NAME_KEY = "principalName";
    public static final String INITIATOR_PRINCIPAL_ID_KEY = "initiatorPrincipalId";
    public static final String VIEWER_PRINCIPAL_ID_KEY = "viewerPrincipalId";
    public static final String APPROVER_PRINCIPAL_ID_KEY = "approverPrincipalId";
    public static final String VIEWER_GROUP_ID_KEY = "viewerGroupId";
    public static final String DOCUMENT_TYPE_NAME_KEY = "documentTypeName";
    
    public static final String USER_ENTRY_WILDCARD_CHAR = "*";
    public static final String DATABASE_WILDCARD_CHAR = "%";
    
    // used for sql query string display format
    public static final String[] FORMAT_OFFSET = {
        "\r\n",
        "\r\n\t",
        "\r\n\t\t",
        "\r\n\t\t\t"
    };
    
    // index table data type codes
    public final static char STRING_TYPE_CODE = 'S';
    public final static char LONG_TYPE_CODE = 'L';
    public final static char FLOAT_TYPE_CODE = 'F';
    public final static char DATE_TYPE_CODE = 'D';

    public static final long DOCUMENT_INDEX_SEARCH_SLEEP_TIME = 250;
        
    public static final String DOCUMENT_HEADER_ID_COLUMN = "doc_hdr_id";
    public static final String KEY_CODE_COLUMN = "key_cd";
    public static final String VAL_COLUMN = "val";

    // index table names
    public static final String KREW_DOC_HDR_EXT_T = "KREW_DOC_HDR_EXT_T";
    public static final String KREW_DOC_HDR_EXT_DT_T = "KREW_DOC_HDR_EXT_DT_T";
    public static final String KREW_DOC_HDR_EXT_LONG_T = "KREW_DOC_HDR_EXT_LONG_T";
    public static final String KREW_DOC_HDR_EXT_FLT_T = "KREW_DOC_HDR_EXT_FLT_T";
    
    public static final String[] DOCUMENT_INDEX_TABLES = {
        KREW_DOC_HDR_EXT_LONG_T,
        KREW_DOC_HDR_EXT_FLT_T,
        KREW_DOC_HDR_EXT_DT_T,
        KREW_DOC_HDR_EXT_T
    };
    
    public static final Map <Character, String> ATTRIBUTE_COLUMN_BY_TYPE_CODE_MAP = new HashMap<Character, String>();
    public static final Map <String, Character> TYPE_CODE_BY_ATTRIBUTE_TABLE_MAP = new HashMap<String, Character>();
    public static final Map<Character, SearchableAttributeValue> SEARCHABLE_ATTRUBUTE_VALUE_BY_TYPE_CODE = new HashMap<Character, SearchableAttributeValue>();
    
    static {
        TYPE_CODE_BY_ATTRIBUTE_TABLE_MAP.put(KREW_DOC_HDR_EXT_T, STRING_TYPE_CODE);
        TYPE_CODE_BY_ATTRIBUTE_TABLE_MAP.put(KREW_DOC_HDR_EXT_DT_T, DATE_TYPE_CODE);
        TYPE_CODE_BY_ATTRIBUTE_TABLE_MAP.put(KREW_DOC_HDR_EXT_LONG_T, LONG_TYPE_CODE);
        TYPE_CODE_BY_ATTRIBUTE_TABLE_MAP.put(KREW_DOC_HDR_EXT_FLT_T, FLOAT_TYPE_CODE);
        
        for (SearchableAttributeValue att : DocumentSearchInternalUtils.getSearchableAttributeValueObjectTypes()) {
            SEARCHABLE_ATTRUBUTE_VALUE_BY_TYPE_CODE.put(TYPE_CODE_BY_ATTRIBUTE_TABLE_MAP.get(att.getAttributeTableName()), att);
        }
    }
}
