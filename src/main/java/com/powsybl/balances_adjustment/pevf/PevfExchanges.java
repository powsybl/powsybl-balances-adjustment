/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.commons.PowsyblException;
import com.powsybl.timeseries.*;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pan European Verification Function data.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class PevfExchanges {

    // RequireNonNull messages
    private static final String INSTANT_CANNOT_BE_NULL = "Instant cannot be null";
    private static final String ID_CANNOT_BE_NULL = "TimeSeriesId cannot be null";
    private static final String IDS_CANNOT_BE_NULL = "TimeSeriesIds cannot be null";

    /** Document identification. */
    private final String mRID;
    /** Version of the document. */
    private final int revisionNumber;
    /** The coded type of a document. The document type describes the principal characteristic of the document. */
    private final StandardMessageType type;
    /** The identification of the nature of process that the
     document addresses. */
    private final StandardProcessType processType;
    /** The identification of the sender */
    private final String senderId;
    private final StandardCodingSchemeType senderCodingScheme;
    /** The identification of the role played by a market player. */
    private final StandardRoleType senderMarketRole;
    /** The identification of a party in the energy market. */
    private final String receiverId;
    private final StandardCodingSchemeType receiverCodingScheme;
    /** The identification of the role played by the a market player. */
    private final StandardRoleType receiverMarketRole;
    /** The date and time of the creation of the document. */
    private final DateTime creationDate;
    /** This information provides the start and end date and time of the period covered by the document. */
    private final Interval period;

    // Optional data
    /** The identification of an individually predefined dataset in a
     data base system (e. g. Verification Platform). */
    private final String datasetMarketDocumentMRId;
    /** The identification of the condition or position of the document with regard to its standing. A document may be intermediate or final. */
    private final StandardStatusType docStatus;

    // Time Series
    private final Map<String, DoubleTimeSeries> timeSeriesById = new HashMap<>();

    PevfExchanges(String mRID, int revisionNumber, StandardMessageType type, StandardProcessType processType,
                  String senderId, StandardCodingSchemeType senderCodingScheme, StandardRoleType senderMarketRole,
                  String receiverId, StandardCodingSchemeType receiverCodingScheme, StandardRoleType receiverMarketRole,
                  DateTime creationDate, Interval period, String datasetMarketDocumentMRId, StandardStatusType docStatus, Map<String, StoredDoubleTimeSeries> timeSeriesById) {
        this.mRID = Objects.requireNonNull(mRID, "mRID is missing");
        this.revisionNumber = checkRevisionNumber(revisionNumber);
        this.type = Objects.requireNonNull(type, "StandardMessageType is missing");
        this.processType = Objects.requireNonNull(processType, "StandardMessageType is missing");
        this.senderId = Objects.requireNonNull(senderId, "Sender mRID is missing");
        this.senderCodingScheme = Objects.requireNonNull(senderCodingScheme, "Sender codingScheme is missing");
        this.senderMarketRole = Objects.requireNonNull(senderMarketRole, "Sender role is missing");
        this.receiverId = Objects.requireNonNull(receiverId, "Receiver mRID is missing");
        this.receiverCodingScheme = Objects.requireNonNull(receiverCodingScheme, "Receiver codingScheme is missing");
        this.receiverMarketRole = Objects.requireNonNull(receiverMarketRole, "Receiver role is missing");
        this.creationDate = Objects.requireNonNull(creationDate, "Creation DateTime is missing");
        this.period = Objects.requireNonNull(period, "Time interval is missing");
        this.datasetMarketDocumentMRId = datasetMarketDocumentMRId;
        this.docStatus = docStatus;
        this.timeSeriesById.putAll(timeSeriesById);
    }

    // MarketDocument metadata
    public String getMRId() {
        return mRID;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public StandardMessageType getType() {
        return type;
    }

    public StandardProcessType getProcessType() {
        return processType;
    }

    public String getSenderId() {
        return senderId;
    }

    public StandardCodingSchemeType getSenderCodingScheme() {
        return senderCodingScheme;
    }

    public StandardRoleType getSenderMarketRole() {
        return senderMarketRole;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public StandardCodingSchemeType getReceiverCodingScheme() {
        return receiverCodingScheme;
    }

    public StandardRoleType getReceiverMarketRole() {
        return receiverMarketRole;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public Interval getPeriod() {
        return period;
    }

    // MarketDocument optional metadata
    public Optional<String> getDatasetMarketDocumentMRId() {
        return Optional.ofNullable(datasetMarketDocumentMRId);
    }

    Optional<StandardStatusType> getDocStatus() {
        return Optional.ofNullable(docStatus);
    }

    // Utilities
    public Collection<DoubleTimeSeries> getTimeSeries() {
        return Collections.unmodifiableCollection(timeSeriesById.values());
    }

    public DoubleTimeSeries getTimeSeries(String timeSeriesId) {
        Objects.requireNonNull(timeSeriesId, ID_CANNOT_BE_NULL);
        if (!timeSeriesById.containsKey(timeSeriesId)) {
            throw new PowsyblException(String.format("TimeSeries '%s' not found", timeSeriesId));
        }
        return timeSeriesById.get(timeSeriesId);
    }

    public Map<String, Double> getValuesAt(Instant instant) {
        Objects.requireNonNull(instant, INSTANT_CANNOT_BE_NULL);

        return timeSeriesById.keySet().stream()
                .collect(Collectors.toMap(id -> id, id -> getValueAt(getTimeSeries(id), instant)));
    }

    public double getValueAt(String timeSeriesId, Instant instant) {
        Objects.requireNonNull(instant, INSTANT_CANNOT_BE_NULL);

        final DoubleTimeSeries timeSeries = getTimeSeries(timeSeriesId);
        return getValueAt(timeSeries, instant);
    }

    public Map<String, Double> getValueAt(List<String> timeSeriesIds, Instant instant) {
        Objects.requireNonNull(timeSeriesIds, IDS_CANNOT_BE_NULL);
        Objects.requireNonNull(instant, INSTANT_CANNOT_BE_NULL);

        return timeSeriesIds.stream()
                .collect(Collectors.toMap(id -> id, id -> getValueAt(getTimeSeries(id), instant)));
    }

    public Map<String, Double> getValueAt(String[] timeSeriesIds, Instant instant) {
        return getValueAt(Arrays.asList(timeSeriesIds), instant);
    }

    private double getValueAt(DoubleTimeSeries timeSeries, Instant instant) {
        RegularTimeSeriesIndex index = (RegularTimeSeriesIndex) timeSeries.getMetadata().getIndex();
        Instant start = Instant.ofEpochMilli(index.getStartTime());
        Instant end = Instant.ofEpochMilli(index.getEndTime());

        if (instant.isBefore(start) || instant.isAfter(end) || instant.equals(end)) {
            throw new PowsyblException(String.format("%s '%s' is out of bound [%s, %s[", timeSeries.getMetadata().getName(), instant, start, end));
        } else {
            long spacing = index.getSpacing();
            Duration elapsed = Duration.between(start, instant);
            long point = elapsed.toMillis() / spacing;
            return timeSeries.toArray()[(int) point];
        }
    }

    private static int checkRevisionNumber(int revisionNumber) {
        if (revisionNumber < 0 || revisionNumber > 100) {
            throw new IllegalArgumentException("Bad revision number value " + revisionNumber);
        }
        return revisionNumber;
    }
}
