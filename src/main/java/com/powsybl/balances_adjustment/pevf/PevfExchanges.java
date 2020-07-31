/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.commons.PowsyblException;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Pan European Verification Function data.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class PevfExchanges {

    /** Document identification. */
    private String mRID;
    /** Version of the document. */
    private int revisionNumber;
    /** The coded type of a document. The document type describes the principal characteristic of the document. */
    private StandardMessageType type;
    /** The identification of the nature of process that the
     document addresses. */
    private StandardProcessType processType;
    /** The identification of the sender */
    private String senderId;
    private StandardCodingSchemeType senderCodingScheme;
    /** The identification of the role played by a market player. */
    private StandardRoleType senderMarketRole;
    /** The identification of a party in the energy market. */
    private String receiverId;
    private StandardCodingSchemeType receiverCodingScheme;
    /** The identification of the role played by the a market player. */
    private StandardRoleType receiverMarketRole;
    /** The date and time of the creation of the document. */
    private ZonedDateTime creationDate;
    /** This information provides the start and end date and time of the period covered by the document. */
    private ZonedDateTime periodStart;
    private ZonedDateTime periodEnd;

    // Optional data
    /** The identification of an individually predefined dataset in a
     data base system (e. g. Verification Platform). */
    private String datasetMarketDocumentMRId;
    /** The identification of the condition or position of the document with regard to its standing. A document may be intermediate or final. */
    private StandardStatusType docStatus;

    PevfExchanges() {
    }

    public String getMRId() {
        return mRID;
    }

    public PevfExchanges setMRId(String mRID) {
        this.mRID = mRID;
        return this;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public PevfExchanges setRevisionNumber(int revisionNumber) {
        if (revisionNumber < 0) {
            throw new PowsyblException("Unexpected revision number (" + revisionNumber + ")");
        }
        this.revisionNumber = revisionNumber;
        return this;
    }

    public StandardMessageType getType() {
        return type;
    }

    public PevfExchanges setType(StandardMessageType type) {
        Objects.requireNonNull(type);
        this.type = type;
        return this;
    }

    public StandardProcessType getProcessType() {
        return processType;
    }

    public PevfExchanges setProcessType(StandardProcessType processType) {
        Objects.requireNonNull(processType);
        this.processType = processType;
        return this;
    }

    public String getSenderId() {
        return senderId;
    }

    public PevfExchanges setSenderId(String senderId) {
        Objects.requireNonNull(senderId);
        this.senderId = senderId;
        return this;
    }

    public StandardCodingSchemeType getSenderCodingScheme() {
        return senderCodingScheme;
    }

    public PevfExchanges setSenderCodingScheme(StandardCodingSchemeType senderCodingScheme) {
        Objects.requireNonNull(senderCodingScheme);
        this.senderCodingScheme = senderCodingScheme;
        return this;
    }

    public StandardRoleType getSenderMarketRole() {
        return senderMarketRole;
    }

    public PevfExchanges setSenderMarketRole(StandardRoleType senderMarketRole) {
        Objects.requireNonNull(senderMarketRole);
        this.senderMarketRole = senderMarketRole;
        return this;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public PevfExchanges setReceiverId(String receiverId) {
        Objects.requireNonNull(receiverId);
        this.receiverId = receiverId;
        return this;
    }

    public StandardCodingSchemeType getReceiverCodingScheme() {
        return receiverCodingScheme;
    }

    public PevfExchanges setReceiverCodingScheme(StandardCodingSchemeType receiverCodingScheme) {
        Objects.requireNonNull(receiverCodingScheme);
        this.receiverCodingScheme = receiverCodingScheme;
        return this;
    }

    public StandardRoleType getReceiverMarketRole() {
        return receiverMarketRole;
    }

    public PevfExchanges setReceiverMarketRole(StandardRoleType receiverMarketRole) {
        Objects.requireNonNull(receiverMarketRole);
        this.receiverMarketRole = receiverMarketRole;
        return this;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public PevfExchanges setCreationDate(ZonedDateTime creationDate) {
        Objects.requireNonNull(creationDate);
        this.creationDate = creationDate;
        return this;
    }

    public ZonedDateTime getPeriodStart() {
        return periodStart;
    }

    public PevfExchanges setPeriodStart(ZonedDateTime periodStart) {
        Objects.requireNonNull(periodStart);
        this.periodStart = periodStart;
        return this;
    }

    public ZonedDateTime getPeriodEnd() {
        return periodEnd;
    }

    public PevfExchanges setPeriodEnd(ZonedDateTime periodEnd) {
        Objects.requireNonNull(periodEnd);
        this.periodEnd = periodEnd;
        return this;
    }

    public Optional<String> getDatasetMarketDocumentMRId() {
        return Optional.ofNullable(datasetMarketDocumentMRId);
    }

    public PevfExchanges setDatasetMarketDocumentMRId(String datasetMarketDocumentMRId) {
        Objects.requireNonNull(datasetMarketDocumentMRId);
        this.datasetMarketDocumentMRId = datasetMarketDocumentMRId;
        return this;
    }

    public Optional<StandardStatusType> getDocStatus() {
        return Optional.ofNullable(docStatus);
    }

    public PevfExchanges setDocStatus(StandardStatusType docStatus) {
        Objects.requireNonNull(docStatus);
        this.docStatus = docStatus;
        return this;
    }
}
