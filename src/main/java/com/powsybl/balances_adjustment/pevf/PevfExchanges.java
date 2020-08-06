/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.timeseries.StoredDoubleTimeSeries;

import java.time.ZonedDateTime;
import java.util.*;

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

    // Time Series
    private final Map<String, StoredDoubleTimeSeries> timeSeriesById = new HashMap<>();

    PevfExchanges() {
    }

    // MarketDocument metadata
    public String getMRId() {
        return mRID;
    }

    public PevfExchanges setMRId(String mRID) {
        this.mRID = Objects.requireNonNull(mRID);
        return this;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public PevfExchanges setRevisionNumber(int revisionNumber) {
        if (revisionNumber < 0) {
            throw new IllegalArgumentException("Bad revision number value " + revisionNumber);
        }
        this.revisionNumber = revisionNumber;
        return this;
    }

    public StandardMessageType getType() {
        return type;
    }

    public PevfExchanges setType(StandardMessageType type) {
        this.type = Objects.requireNonNull(type);
        return this;
    }

    public StandardProcessType getProcessType() {
        return processType;
    }

    public PevfExchanges setProcessType(StandardProcessType processType) {
        this.processType = Objects.requireNonNull(processType);
        return this;
    }

    public String getSenderId() {
        return senderId;
    }

    public PevfExchanges setSenderId(String senderId) {
        this.senderId = Objects.requireNonNull(senderId);
        return this;
    }

    public StandardCodingSchemeType getSenderCodingScheme() {
        return senderCodingScheme;
    }

    public PevfExchanges setSenderCodingScheme(StandardCodingSchemeType senderCodingScheme) {
        this.senderCodingScheme = Objects.requireNonNull(senderCodingScheme);
        return this;
    }

    public StandardRoleType getSenderMarketRole() {
        return senderMarketRole;
    }

    public PevfExchanges setSenderMarketRole(StandardRoleType senderMarketRole) {
        this.senderMarketRole = Objects.requireNonNull(senderMarketRole);
        return this;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public PevfExchanges setReceiverId(String receiverId) {
        this.receiverId = Objects.requireNonNull(receiverId);
        return this;
    }

    public StandardCodingSchemeType getReceiverCodingScheme() {
        return receiverCodingScheme;
    }

    public PevfExchanges setReceiverCodingScheme(StandardCodingSchemeType receiverCodingScheme) {
        this.receiverCodingScheme = Objects.requireNonNull(receiverCodingScheme);
        return this;
    }

    public StandardRoleType getReceiverMarketRole() {
        return receiverMarketRole;
    }

    public PevfExchanges setReceiverMarketRole(StandardRoleType receiverMarketRole) {
        this.receiverMarketRole = Objects.requireNonNull(receiverMarketRole);
        return this;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public PevfExchanges setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = Objects.requireNonNull(creationDate);
        return this;
    }

    public ZonedDateTime getPeriodStart() {
        return periodStart;
    }

    public PevfExchanges setPeriodStart(ZonedDateTime periodStart) {
        this.periodStart = Objects.requireNonNull(periodStart);
        return this;
    }

    public ZonedDateTime getPeriodEnd() {
        return periodEnd;
    }

    public PevfExchanges setPeriodEnd(ZonedDateTime periodEnd) {
        this.periodEnd = Objects.requireNonNull(periodEnd);
        return this;
    }

    // MarketDocument optional metadata
    public Optional<String> getDatasetMarketDocumentMRId() {
        return Optional.ofNullable(datasetMarketDocumentMRId);
    }

    public PevfExchanges setDatasetMarketDocumentMRId(String datasetMarketDocumentMRId) {
        this.datasetMarketDocumentMRId = Objects.requireNonNull(datasetMarketDocumentMRId);
        return this;
    }

    public Optional<StandardStatusType> getDocStatus() {
        return Optional.ofNullable(docStatus);
    }

    public PevfExchanges setDocStatus(StandardStatusType docStatus) {
        this.docStatus = Objects.requireNonNull(docStatus);
        return this;
    }

    // TimeSeries
    public void addTimeSeries(StoredDoubleTimeSeries timeseries) {
        timeSeriesById.put(timeseries.getMetadata().getName(), timeseries);
    }

    public StoredDoubleTimeSeries getTimeSeries(String name) {
        return timeSeriesById.get(name);
    }
}
