/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.timeseries.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pan European Verification Function XML parser.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public final class PevfExchangesXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(PevfExchangesXml.class);

    // Log messages
    private static final String UNEXPECTED_TOKEN = "Unexpected token: {}";

    // Metadata
    private static final String ROOT_ELEMENT_NAME = "ReportingInformation_MarketDocument";
    private static final String MRID_ELEMENT_NAME = "mRID";
    private static final String REVISION_NUMBER_ELEMENT_NAME = "revisionNumber";
    private static final String TYPE_ELEMENT_NAME = "type";
    private static final String PROCESS_TYPE_ELEMENT_NAME = "process.processType";
    private static final String SENDER_MARKET_PARTICIPANT_ELEMENT_NAME = "sender_MarketParticipant";
    private static final String CODING_SCHEME_ELEMENT_NAME = "codingScheme";
    private static final String MARKET_ROLE_ELEMENT_NAME = "marketRole.type";
    private static final String RECEIVER_MARKET_PARTICIPANT_ELEMENT_NAME = "receiver_MarketParticipant";
    private static final String CREATION_DATETIME_ELEMENT_NAME = "createdDateTime";
    private static final String TIME_PERIOD_INTERVAL_ELEMENT_NAME = "time_Period.timeInterval";
    private static final String START_ELEMENT_NAME = "start";
    private static final String END_ELEMENT_NAME = "end";
    private static final String DATASET_MARKET_DOCUMENT_ELEMENT_NAME = "dataset_MarketDocument";
    private static final String DOC_STATUS_ELEMENT_NAME = "docStatus";
    private static final String VALUE_ELEMENT_NAME = "value";
    // TimeSeries
    private static final String TIMESERIES_ELEMENT_NAME = "TimeSeries";
    private static final String BUSINESS_TYPE_ELEMENT_NAME = "businessType";
    private static final String PRODUCT_ELEMENT_NAME = "product";
    private static final String IN_DOMAIN_ELEMENT_NAME = "in_Domain";
    private static final String OUT_DOMAIN_ELEMENT_NAME = "out_Domain";
    private static final String CONNECTING_LINE_REGISTERED_RESOURCE_ELEMENT_NAME = "connectingLine_RegisteredResource";
    private static final String MEASUREMENT_UNIT_ELEMENT_NAME = "measurement_Unit.name";
    private static final String CURVE_TYPE_ELEMENT_NAME = "curveType";
    private static final String PERIOD_ELEMENT_NAME = "Period";
    private static final String RESOLUTION_ELEMENT_NAME = "resolution";
    private static final String TIME_INTERVAL_ELEMENT_NAME = "timeInterval";
    private static final String POINT_ELEMENT_NAME = "Point";
    private static final String POSITION_ELEMENT_NAME = "position";
    private static final String QUANTITY_ELEMENT_NAME = "quantity";
    private static final String REASON_ELEMENT_NAME = "Reason";
    private static final String CODE_ELEMENT_NAME = "code";
    private static final String TEXT_ELEMENT_NAME = "text";

    private PevfExchangesXml() {
    }

    public static void read(PevfExchanges pevfExchanges, XMLStreamReader reader) throws XMLStreamException {
        XmlUtil.readUntilEndElement(ROOT_ELEMENT_NAME, reader,  () -> {
            switch (reader.getLocalName()) {
                case ROOT_ELEMENT_NAME:
                    // Nothing to do
                    break;

                case MRID_ELEMENT_NAME:
                    String mRID = reader.getElementText();
                    pevfExchanges.setMRId(mRID);
                    break;

                case REVISION_NUMBER_ELEMENT_NAME:
                    String revisionNumber = reader.getElementText();
                    pevfExchanges.setRevisionNumber(Integer.parseInt(revisionNumber));
                    break;

                case TYPE_ELEMENT_NAME:
                    String type = reader.getElementText();
                    pevfExchanges.setType(StandardMessageType.valueOf(type));
                    break;

                case PROCESS_TYPE_ELEMENT_NAME:
                    String processType = reader.getElementText();
                    pevfExchanges.setProcessType(StandardProcessType.valueOf(processType));
                    break;

                case SENDER_MARKET_PARTICIPANT_ELEMENT_NAME + "." + MRID_ELEMENT_NAME:
                    String senderCodingScheme = reader.getAttributeValue(null, CODING_SCHEME_ELEMENT_NAME);
                    pevfExchanges.setSenderCodingScheme(StandardCodingSchemeType.valueOf(senderCodingScheme));
                    String senderId = reader.getElementText();
                    pevfExchanges.setSenderId(senderId);
                    break;

                case SENDER_MARKET_PARTICIPANT_ELEMENT_NAME + "." + MARKET_ROLE_ELEMENT_NAME:
                    String senderMarketRole = reader.getElementText();
                    pevfExchanges.setSenderMarketRole(StandardRoleType.valueOf(senderMarketRole));
                    break;

                case RECEIVER_MARKET_PARTICIPANT_ELEMENT_NAME + "." + MRID_ELEMENT_NAME:
                    String codingScheme = reader.getAttributeValue(null, CODING_SCHEME_ELEMENT_NAME);
                    pevfExchanges.setReceiverCodingScheme(StandardCodingSchemeType.valueOf(codingScheme));
                    String receiverId = reader.getElementText();
                    pevfExchanges.setReceiverId(receiverId);
                    break;

                case RECEIVER_MARKET_PARTICIPANT_ELEMENT_NAME + "." + MARKET_ROLE_ELEMENT_NAME:
                    String receiverMarketRole = reader.getElementText();
                    pevfExchanges.setReceiverMarketRole(StandardRoleType.valueOf(receiverMarketRole));
                    break;

                case CREATION_DATETIME_ELEMENT_NAME:
                    String creationDate = reader.getElementText();
                    pevfExchanges.setCreationDate(ZonedDateTime.parse(creationDate));
                    break;

                case TIME_PERIOD_INTERVAL_ELEMENT_NAME:
                    AtomicReference<ZonedDateTime> start = new AtomicReference<>();
                    AtomicReference<ZonedDateTime> end = new AtomicReference<>();
                    readTimeInterval(reader, TIME_PERIOD_INTERVAL_ELEMENT_NAME, start, end);
                    pevfExchanges.setPeriodStart(start.get());
                    pevfExchanges.setPeriodEnd(end.get());
                    break;

                case DATASET_MARKET_DOCUMENT_ELEMENT_NAME + "." + MRID_ELEMENT_NAME:
                    String datasetId = reader.getElementText();
                    pevfExchanges.setDatasetMarketDocumentMRId(datasetId);
                    break;

                case DOC_STATUS_ELEMENT_NAME:
                    String value = XmlUtil.readUntilEndElement(VALUE_ELEMENT_NAME, reader, null);
                    pevfExchanges.setDocStatus(StandardStatusType.valueOf(value));
                    break;

                case TIMESERIES_ELEMENT_NAME:
                    readTimeSeries(pevfExchanges, reader);
                    break;

                default:
                    LOGGER.warn(UNEXPECTED_TOKEN, reader.getLocalName());
            }
        });
    }

    private static void readTimeSeries(PevfExchanges pevfExchanges, XMLStreamReader reader) throws XMLStreamException {
        AtomicReference<String> timeSeriesMRID = new AtomicReference<>();
        AtomicReference<ZonedDateTime> start = new AtomicReference<>();
        AtomicReference<ZonedDateTime> end = new AtomicReference<>();
        AtomicReference<Duration> spacing = new AtomicReference<>();
        LinkedList<Integer> positions = new LinkedList<>();
        LinkedList<Double> stepValues = new LinkedList<>();
        Map<String, String> tags = new HashMap<>();
        AtomicReference<String> code = new AtomicReference<>();
        AtomicReference<String> text = new AtomicReference<>();

        XmlUtil.readUntilEndElement(TIMESERIES_ELEMENT_NAME, reader, () -> {
            switch (reader.getLocalName()) {
                case MRID_ELEMENT_NAME:
                    timeSeriesMRID.set(reader.getElementText());
                    break;

                case BUSINESS_TYPE_ELEMENT_NAME:
                case PRODUCT_ELEMENT_NAME:
                case IN_DOMAIN_ELEMENT_NAME + "." + MRID_ELEMENT_NAME:
                case OUT_DOMAIN_ELEMENT_NAME + "." + MRID_ELEMENT_NAME:
                case CONNECTING_LINE_REGISTERED_RESOURCE_ELEMENT_NAME + "." + MRID_ELEMENT_NAME:
                case MEASUREMENT_UNIT_ELEMENT_NAME:
                case CURVE_TYPE_ELEMENT_NAME:
                    if (reader.getLocalName().equals(IN_DOMAIN_ELEMENT_NAME + "." + MRID_ELEMENT_NAME) ||
                        reader.getLocalName().equals(OUT_DOMAIN_ELEMENT_NAME + "." + MRID_ELEMENT_NAME)) {
                        String codingScheme = reader.getAttributeValue(null, CODING_SCHEME_ELEMENT_NAME);
                        tags.put(reader.getLocalName() + "." + CODING_SCHEME_ELEMENT_NAME, codingScheme);
                    }
                    tags.put(reader.getLocalName(), reader.getElementText());
                    break;
                case PERIOD_ELEMENT_NAME:
                    readPeriod(reader, start, end, spacing, positions, stepValues);
                    break;

                case REASON_ELEMENT_NAME:
                    readReason(reader, code, text);
                    break;

                default:
                    LOGGER.warn(UNEXPECTED_TOKEN, reader.getLocalName());
            }
        });

        // Log TimeSeries Reason
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("TimeSeries '{}' [{}, {}] - {} ({}) : {}",  timeSeriesMRID.get(),
                                                                    start.get(), end.get(),
                                                                    code.get(),
                                                                    StandardReasonCodeType.valueOf(code.get()).getDescription(),
                                                                    text.get());
        }

        // Create DataChunk
        DoubleDataChunk dataChunk = null;
        // Computed number of steps
        int nbSteps = java.lang.Math.toIntExact((end.get().toInstant().toEpochMilli() - start.get().toInstant().toEpochMilli()) / spacing.get().toMillis());
        // Check if all steps are defined or not
        if (positions.size() == nbSteps) {
            // Uncompressed chunk
            // Duplication of last value in order to include end datetime Period
            stepValues.add(stepValues.getLast());
            dataChunk = new UncompressedDoubleDataChunk(0, stepValues.stream().mapToDouble(d -> d).toArray());
        } else {
            // Compressed chunk
            LinkedList<Integer> stepLengths = new LinkedList<>();
            if (positions.size() > 1) {
                for (int i = 1; i < positions.size(); i++) {
                    int lastPosition = positions.get(i - 1);
                    int newPosition = positions.get(i);
                    stepLengths.addLast(newPosition - lastPosition);
                }
                stepLengths.addLast(1 + (nbSteps - positions.getLast()));
            } else {
                stepLengths.addLast(nbSteps);
            }
            // Duplication of last value to include end datetime Period
            nbSteps++;
            stepLengths.addLast(stepLengths.removeLast() + 1);
            dataChunk = new CompressedDoubleDataChunk(0, nbSteps, stepValues.stream().mapToDouble(d -> d).toArray(), stepLengths.stream().mapToInt(d -> d).toArray());
        }

        // Instantiate new timeseries
        TimeSeriesIndex index = RegularTimeSeriesIndex.create(start.get().toInstant(), end.get().toInstant(), spacing.get());
        TimeSeriesMetadata metadata = new TimeSeriesMetadata(timeSeriesMRID.get(), TimeSeriesDataType.DOUBLE, tags, index);
        // Add new timeseries into PevfExchanges
        pevfExchanges.addTimeSeries(new StoredDoubleTimeSeries(metadata, dataChunk));
    }

    private static void readPeriod(XMLStreamReader reader, AtomicReference<ZonedDateTime> start, AtomicReference<ZonedDateTime> end, AtomicReference<Duration> spacing, LinkedList<Integer> positions, LinkedList<Double> quantities) throws XMLStreamException {
        XmlUtil.readUntilEndElement(PERIOD_ELEMENT_NAME, reader, () -> {
            switch (reader.getLocalName()) {
                case RESOLUTION_ELEMENT_NAME:
                    String resolution = reader.getElementText();
                    spacing.set(Duration.parse(resolution));
                    break;

                case TIME_INTERVAL_ELEMENT_NAME:
                    readTimeInterval(reader, TIME_INTERVAL_ELEMENT_NAME, start, end);
                    break;

                case POINT_ELEMENT_NAME:
                    readPoint(reader, positions, quantities);
                    break;

                default:
                    LOGGER.warn(UNEXPECTED_TOKEN, reader.getLocalName());
            }
        });
    }

    private static void readTimeInterval(XMLStreamReader reader, String rootElement, AtomicReference<ZonedDateTime> start, AtomicReference<ZonedDateTime> end) throws XMLStreamException {
        XmlUtil.readUntilEndElement(rootElement, reader, () -> {
            switch (reader.getLocalName()) {
                case START_ELEMENT_NAME :
                    String periodStart = reader.getElementText();
                    start.set(ZonedDateTime.parse(periodStart));
                    break;

                case END_ELEMENT_NAME :
                    String periodEnd = reader.getElementText();
                    end.set(ZonedDateTime.parse(periodEnd));
                    break;

                default:
                    LOGGER.warn(UNEXPECTED_TOKEN, reader.getLocalName());
            }
        });
    }

    private static void readPoint(XMLStreamReader reader, LinkedList<Integer> positions, LinkedList<Double> quantities) throws XMLStreamException {
        XmlUtil.readUntilEndElement(POINT_ELEMENT_NAME, reader, () -> {
            switch (reader.getLocalName()) {
                case POSITION_ELEMENT_NAME:
                    String position = reader.getElementText();
                    positions.add(Integer.parseInt(position));
                    break;

                case QUANTITY_ELEMENT_NAME:
                    String quantity = reader.getElementText();
                    quantities.add(Double.parseDouble(quantity));
                    break;

                default:
                    LOGGER.warn(UNEXPECTED_TOKEN, reader.getLocalName());
            }
        });
    }

    private static void readReason(XMLStreamReader reader, AtomicReference<String> code, AtomicReference<String> text) throws XMLStreamException {
        XmlUtil.readUntilEndElement(REASON_ELEMENT_NAME, reader, () -> {
            switch (reader.getLocalName()) {
                case CODE_ELEMENT_NAME:
                    code.set(reader.getElementText());
                    break;

                case TEXT_ELEMENT_NAME:
                    text.set(reader.getElementText());
                    break;

                default:
                    LOGGER.warn(UNEXPECTED_TOKEN, reader.getLocalName());
            }
        });
    }
}
