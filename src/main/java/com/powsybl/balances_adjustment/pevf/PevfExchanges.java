/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.commons.PowsyblException;
import com.powsybl.timeseries.DoublePoint;
import com.powsybl.timeseries.StoredDoubleTimeSeries;
import org.threeten.extra.Interval;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Pan European Verification Function data.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class PevfExchanges {

    // RequireNonNull messages
    private static final String INSTANT_CANNOT_BE_NULL = "Instant cannot be null";
    private static final String ID_CANNOT_BE_NULL = "TimeSeriesId cannot be null";

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
    private org.threeten.extra.Interval period;

    // Optional data
    /** The identification of an individually predefined dataset in a
     data base system (e. g. Verification Platform). */
    private String datasetMarketDocumentMRId;
    /** The identification of the condition or position of the document with regard to its standing. A document may be intermediate or final. */
    private StandardStatusType docStatus;

    // Time Series
    private final HashMap<String, StoredDoubleTimeSeries> timeSeriesById = new HashMap<>();

    PevfExchanges(String mRID, int revisionNumber, StandardMessageType type, StandardProcessType processType,
                  String senderId, StandardCodingSchemeType senderCodingScheme, StandardRoleType senderMarketRole,
                  String receiverId, StandardCodingSchemeType receiverCodingScheme, StandardRoleType receiverMarketRole,
                  ZonedDateTime creationDate, Interval period, String datasetMarketDocumentMRId, StandardStatusType docStatus, Map<String, StoredDoubleTimeSeries> timeSeriesById) {
        this.mRID = Objects.requireNonNull(mRID, "mRID is missing");

        if (revisionNumber < 0 || revisionNumber > 100) {
            throw new IllegalArgumentException("Bad revision number value " + revisionNumber);
        }
        this.revisionNumber = revisionNumber;

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

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public org.threeten.extra.Interval getPeriod() {
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
    public Collection<StoredDoubleTimeSeries> getTimeSeries() {
        return timeSeriesById.values();
    }

    public StoredDoubleTimeSeries getTimeSeries(String timeSeriesId) {
        Objects.requireNonNull(timeSeriesId, ID_CANNOT_BE_NULL);
        return timeSeriesById.get(timeSeriesId);
    }

    public Map<String, Double> getValuesAt(Instant instant) {
        Objects.requireNonNull(instant, INSTANT_CANNOT_BE_NULL);

        Map<String, Double> valueById = new HashMap<>();
        timeSeriesById.forEach((name, timeSeries) -> {
            try {
                valueById.put(name, getValueAt(name, instant));
            } catch (PowsyblException ignored) {
                // Ignored
            }
        });
        return valueById;
    }

    public Map<String, Double> getValuesAt(String instant) {
        Objects.requireNonNull(instant, INSTANT_CANNOT_BE_NULL);
        return getValuesAt(ZonedDateTime.parse(instant).toInstant());
    }

    public double getValueAt(String timeSeriesId, Instant instant) {
        Objects.requireNonNull(timeSeriesId, ID_CANNOT_BE_NULL);
        Objects.requireNonNull(instant, INSTANT_CANNOT_BE_NULL);

        // Filtering by Lower bound
        return timeSeriesById.get(timeSeriesId).stream()
                .reduce((p1, p2) -> Math.abs(p1.getTime() - instant.toEpochMilli()) < Math.abs(p2.getTime() - instant.toEpochMilli()) ? p1 : p2)
                .filter(p -> instant.toEpochMilli() >= p.getTime())
                .map(DoublePoint::getValue)
                .orElseThrow(() -> new PowsyblException(String.format("'%s' not found into '%s'", instant, timeSeriesId)));
    }

    public double getValueAt(String timeSeriesId, String instant) {
        Objects.requireNonNull(instant, INSTANT_CANNOT_BE_NULL);
        return getValueAt(timeSeriesId, ZonedDateTime.parse(instant).toInstant());
    }
}
