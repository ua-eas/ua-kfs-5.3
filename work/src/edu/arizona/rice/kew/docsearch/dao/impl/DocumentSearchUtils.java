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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;


public class DocumentSearchUtils {
    /**
     * 
     * @param typeCode
     * @param value
     * @return 
     */
    public static String getSearchClause(char typeCode, String value) {
        return getSearchClause(typeCode, value, false);
    }
    

    /**
     * 
     * @param typeCode
     * @param value
     * @param caseInsensitiveSearch
     * @return 
     */
    public static String getSearchClause(char typeCode, String value, boolean caseInsensitiveSearch) {
        return getSearchClause(typeCode, Arrays.asList(new String[] {value}), caseInsensitiveSearch);
    }

    /**
     * 
     * @param typeCode
     * @param values
     * @return 
     */
    public static String getSearchClause(char typeCode, List <String> values) {
        return getSearchClause(typeCode, values, false);
    }

    /**
     * 
     * @param typeCode
     * @param values
     * @param caseInsensitiveSearch
     * @return 
     */
    public static String getSearchClause(char typeCode, List <String> values, boolean caseInsensitiveSearch) {
        StringBuilder retval = new StringBuilder(256);
        
        if (values.size() > 1) {
            String comma = "";
            retval.append(" in (");
            
            for (String val : values) {
                retval.append(comma);
                if (typeCode == DocumentSearchConstants.STRING_TYPE_CODE) {
                    retval.append("'");
                } else if (typeCode == DocumentSearchConstants.DATE_TYPE_CODE) {
                    retval.append("{d '");
                }
                
                if (caseInsensitiveSearch) {
                    retval.append(val.toUpperCase());
                } else {
                    retval.append(val);
                }
                
                if (typeCode == DocumentSearchConstants.STRING_TYPE_CODE) {
                    retval.append("'");
                } else if (typeCode == DocumentSearchConstants.DATE_TYPE_CODE) {
                    retval.append("'}");
                }
                
                comma = ",";
            }
            
            retval.append(") ");
        } else {
            String value = values.get(0);
            boolean wildcard = hasWildcard(value);
            if ((typeCode == DocumentSearchConstants.STRING_TYPE_CODE) && hasWildcard(value) ) {
                if (wildcard) {
                    retval.append(" like (");
                    value = value.replace(DocumentSearchConstants.USER_ENTRY_WILDCARD_CHAR, DocumentSearchConstants.DATABASE_WILDCARD_CHAR);
                } else {
                    retval.append(" = ");
                }
            } else {
                retval.append(" = ");
            }

            if (typeCode == DocumentSearchConstants.STRING_TYPE_CODE) {
                retval.append("'");
            } else if (typeCode == DocumentSearchConstants.DATE_TYPE_CODE) {
                retval.append(" {d '");
            }

            if (caseInsensitiveSearch) {
                retval.append(value.toUpperCase());
            } else {
                retval.append(value);
            }

            if (typeCode == DocumentSearchConstants.STRING_TYPE_CODE) {
                retval.append("'");
            } else if (typeCode == DocumentSearchConstants.DATE_TYPE_CODE) {
                retval.append("'}");
            }
            
            if (wildcard) {
                retval.append(")");
            }
        }
        
        return retval.toString();
    }

        /**
     * 
     * @param input
     * @return 
     */
    public static boolean hasWildcard(String input) {
        return (StringUtils.isNotBlank(input) 
                && (input.contains(DocumentSearchConstants.USER_ENTRY_WILDCARD_CHAR) 
                || input.contains(DocumentSearchConstants.DATABASE_WILDCARD_CHAR)));
    }
    

    /**
     * build a list of sql query "in" lists that adhere to database limitation (1000 for oracle)
     * this will return a list of in lists that can each be submitted in a query without causing problems
     * @param ids
     * @return 
     */
    public static List <List<String>> getInLists(Collection <String> ids) {
        List <List<String>> retval = new ArrayList<List<String>>();
        
        int i = 0;
        List <String> curlist = null;
        for (String id : ids) {
            if ((((i++) % DocumentSearchConstants.MAX_INLIST_SIZE) == 0) || (curlist == null)) {
                retval.add(curlist = new ArrayList<String>());
            }
            
            curlist.add(id);
        }
        
        return retval;
    }

    public static void closeDbObjects(Connection conn, Statement stmt, ResultSet res) {
        try {
            if (res != null) {
                res.close();
            } 
        }

        catch (Exception ex) {}

        try {
            if (stmt != null) {
                stmt.close();
            } 
        }

        catch (Exception ex) {}

        try {
            if (conn != null) {
                conn.close();
            } 
        }

        catch (Exception ex) {}
    }
}
