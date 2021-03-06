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
package org.kuali.kfs.module.cab.document.service;

import java.util.List;
import java.util.Set;

import org.kuali.kfs.module.cab.businessobject.Pretag;
import org.kuali.kfs.module.cab.businessobject.PurchasingAccountsPayableActionHistory;
import org.kuali.kfs.module.cab.businessobject.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.cab.businessobject.PurchasingAccountsPayableItemAsset;
import org.kuali.kfs.module.cab.document.web.PurApLineSession;


/**
 * This class declares methods used by CAB PurAp Line process
 */
public interface PurApLineService {

    /**
     * Check the payments in given asset lines have different object sub types.
     * 
     * @param selectedLines
     * @return
     */
    boolean allocateLinesHasDifferentObjectSubTypes(List<PurchasingAccountsPayableItemAsset> targetLines, PurchasingAccountsPayableItemAsset sourceLine);

    /**
     * Check the payments in given asset lines have different object sub types.
     * 
     * @param selectedLines
     * @return
     */
    boolean mergeLinesHasDifferentObjectSubTypes(List<PurchasingAccountsPayableItemAsset> mergeLines);

    /**
     * Changes percent quantities to a quantity of 1 for selected line item.
     * 
     * @param itemAsset Selected line item.
     * @param actionsTake Action taken history.
     */
    void processPercentPayment(PurchasingAccountsPayableItemAsset itemAsset, List<PurchasingAccountsPayableActionHistory> actionsTaken);

    /**
     * Split the selected line item quantity and create a new line item.
     * 
     * @param itemAsset Selected line item.
     * @param actionsTaken Action taken history.
     * @return
     */
    void processSplit(PurchasingAccountsPayableItemAsset splitItemAsset, List<PurchasingAccountsPayableActionHistory> actionsTakeHistory);

    /**
     * Save purApDoc, item assets and account lines for persistence
     * 
     * @param purApDocs
     * @param purApLineSession
     */
    void processSaveBusinessObjects(List<PurchasingAccountsPayableDocument> purApDocs, PurApLineSession purApLineSession);

    /**
     * Build PurAp document collection and line item collection.
     * 
     * @param purApDocs
     */
    void buildPurApItemAssetList(List<PurchasingAccountsPayableDocument> purApDocs);

    /**
     * Handle additional charge allocate in the same document.
     * 
     * @param selectedLineItem
     * @param allocateTargetLines
     * @param purApLineSession
     * @param purApDocs
     * @return
     */
    boolean processAllocate(PurchasingAccountsPayableItemAsset selectedLineItem, List<PurchasingAccountsPayableItemAsset> allocateTargetLines, List<PurchasingAccountsPayableActionHistory> actionsTakeHistory, List<PurchasingAccountsPayableDocument> purApDocs, boolean initiateFromBatch);

    /**
     * Get the target lines based on allocation line type
     * 
     * @param selectedLineItem
     * @param purApDocs
     * @return
     */
    List<PurchasingAccountsPayableItemAsset> getAllocateTargetLines(PurchasingAccountsPayableItemAsset selectedLineItem, List<PurchasingAccountsPayableDocument> purApDocs);

    /**
     * Get the selected merge lines.
     * 
     * @param isMergeAll
     * @param purApDocs
     * @return
     */
    List<PurchasingAccountsPayableItemAsset> getSelectedMergeLines(boolean isMergeAll, List<PurchasingAccountsPayableDocument> purApDocs);

    /**
     * Reset selectedValue for all line items
     * 
     * @param purApDocs
     */
    void resetSelectedValue(List<PurchasingAccountsPayableDocument> purApDocs);

    /**
     * Merge line items.
     * 
     * @param mergeLines
     * @param purApLineSession
     * @param isMergeAll
     */
    void processMerge(List<PurchasingAccountsPayableItemAsset> mergeLines, List<PurchasingAccountsPayableActionHistory> actionsTakeHistory, boolean isMergeAll);

    /**
     * Check if the merge action is merge all.
     * 
     * @param purApDocs
     * @return
     */
    boolean isMergeAllAction(List<PurchasingAccountsPayableDocument> purApDocs);

    /**
     * For line items in itemAssets if they are not in the same PurAp document, check if there is pending additional charges
     * allocation.
     * 
     * @param itemAssets
     * @return
     */
    boolean isAdditionalChargePending(List<PurchasingAccountsPayableItemAsset> itemAssets);

    /**
     * Check if there is TI indicator exists in the given itemAssets List.
     * 
     * @param itemAssets
     * @return
     */
    boolean isTradeInIndExistInSelectedLines(List<PurchasingAccountsPayableItemAsset> itemAssets);

    /**
     * Check if there is trade-in allowance not allocated yet.
     * 
     * @param purApDocs
     * @return
     */
    boolean isTradeInAllowanceExist(List<PurchasingAccountsPayableDocument> purApDocs);

    /**
     * Check if there is additional charge line exist in all lines.
     * 
     * @param purApDocs
     * @return
     */
    boolean isAdditionalChargeExistInAllLines(List<PurchasingAccountsPayableDocument> purApDocs);

    /**
     * Get preTag if exists for give line item.
     * 
     * @param purchaseOrderIdentifier
     * @param lineItemNumber
     * @return
     */
    Pretag getPreTagLineItem(Integer purchaseOrderIdentifier, Integer lineItemNumber);

    /**
     * In-activate document when all the associated items are inactive.
     * 
     * @param selectedDoc
     */
    void conditionallyUpdateDocumentStatusAsProcessed(PurchasingAccountsPayableDocument selectedDoc);

    /**
     * Check if more than one pre-tagging exists for given itemLineNumber and PO_ID.
     * 
     * @param purchaseOrderIdentifier
     * @param itemLineNumbers
     * @return
     */
    boolean isMultipleTagExisting(Integer purchaseOrderIdentifier, Set<Integer> itemLineNumbers);

    /**
     * Check pretag existing
     * 
     * @param newTag
     * @return
     */
    boolean isPretaggingExisting(Pretag newTag);
}
