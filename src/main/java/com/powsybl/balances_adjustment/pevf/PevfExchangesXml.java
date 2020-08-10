/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.timeseries.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static com.powsybl.balances_adjustment.pevf.PevfExchangesNames.*;

/**
 * Pan European Verification Function XML parser.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public final class PevfExchangesXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(PevfExchangesXml.class);

    // Log messages
    private static final String UNEXPECTED_TOKEN = "Unexpected token: ";

    private PevfExchangesXml() {
    }

    private static class ParsingContext {

        private String mRID;

        private int revisionNumber;

        private StandardMessageType type;

        private StandardProcessType processType;

        private String senderId;

        private StandardCodingSchemeType senderCodingScheme;

        private StandardRoleType senderMarketRole;

        private String receiverId;

        private StandardCodingSchemeType receiverCodingScheme;

        private StandardRoleType receiverMarketRole;

        private ZonedDateTime creationDate;

        private org.threeten.extra.Interval period;

        private String datasetMarketDocumentMRId;

        private StandardStatusType docStatus;

        private Map<String, StoredDoubleTimeSeries> timeSeriesById = new HashMap<>();
    }

    private static class ParsingTimeSeriesContext {

        private String mRID;

        private  org.threeten.extra.Interval period;

        private Duration spacing;

        private LinkedList<Integer> positions = new LinkedList<>();

        private LinkedList<Double> quantities = new LinkedList<>();

        private Map<String, String> tags = new HashMap<>();

        private String code;

        private String text;
    }

    public static PevfExchanges parse(Reader reader) throws XMLStreamException {
        Objects.requireNonNull(reader);
        ParsingContext context = new ParsingContext();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            // disable resolving of external DTD entities  
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
            try {
                XmlUtil.readUntilEndElement(ROOT, xmlReader,  () -> {
                    switch (xmlReader.getLocalName()) {

                        case MRID:
                            context.mRID = xmlReader.getElementText();
                            break;

                        case REVISION_NUMBER:
                            context.revisionNumber = Integer.parseInt(xmlReader.getElementText());
                            break;

                        case TYPE:
                            context.type = StandardMessageType.valueOf(xmlReader.getElementText());
                            break;

                        case PROCESS_TYPE:
                            context.processType = StandardProcessType.valueOf(xmlReader.getElementText());
                            break;

                        case SENDER_MARKET_PARTICIPANT + "." + MRID:
                            context.senderCodingScheme = StandardCodingSchemeType.valueOf(xmlReader.getAttributeValue(null, CODING_SCHEME));
                            context.senderId = xmlReader.getElementText();
                            break;

                        case SENDER_MARKET_PARTICIPANT + "." + MARKET_ROLE:
                            context.senderMarketRole = StandardRoleType.valueOf(xmlReader.getElementText());
                            break;

                        case RECEIVER_MARKET_PARTICIPANT + "." + MRID:
                            context.receiverCodingScheme = StandardCodingSchemeType.valueOf(xmlReader.getAttributeValue(null, CODING_SCHEME));
                            context.receiverId = xmlReader.getElementText();
                            break;

                        case RECEIVER_MARKET_PARTICIPANT + "." + MARKET_ROLE:
                            context.receiverMarketRole = StandardRoleType.valueOf(xmlReader.getElementText());
                            break;

                        case CREATION_DATETIME:
                            context.creationDate = ZonedDateTime.parse(xmlReader.getElementText());
                            break;

                        case TIME_PERIOD_INTERVAL:
                            context.period = readTimeInterval(xmlReader, TIME_PERIOD_INTERVAL);
                            break;

                        case DATASET_MARKET_DOCUMENT + "." + MRID:
                            context.datasetMarketDocumentMRId = xmlReader.getElementText();
                            break;

                        case DOC_STATUS:
                            context.docStatus = StandardStatusType.valueOf(XmlUtil.readUntilEndElement(VALUE, xmlReader, null));
                            break;

                        case TIMESERIES:
                            StoredDoubleTimeSeries timeSeries = readTimeSeries(xmlReader);
                            context.timeSeriesById.put(timeSeries.getMetadata().getName(), timeSeries);
                            break;

                        case ROOT:
                            // Explicit skip
                            break;

                        default:
                            throw new PowsyblException(UNEXPECTED_TOKEN + xmlReader.getLocalName());
                    }
                });
            } finally {
                xmlReader.close();
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
        // the attributes are checked in the constructor
        return new PevfExchanges(context.mRID, context.revisionNumber, context.type, context.processType,
                                 context.senderId, context.senderCodingScheme, context.senderMarketRole,
                                 context.receiverId, context.receiverCodingScheme, context.receiverMarketRole,
                                 context.creationDate, context.period, context.datasetMarketDocumentMRId, context.docStatus, context.timeSeriesById);
    }

    private static StoredDoubleTimeSeries readTimeSeries(XMLStreamReader xmlReader) throws XMLStreamException {
        ParsingTimeSeriesContext context = new ParsingTimeSeriesContext();

        XmlUtil.readUntilEndElement(TIMESERIES, xmlReader, () -> {
            switch (xmlReader.getLocalName()) {
                case MRID:
                    context.mRID = xmlReader.getElementText();
                    break;

                case BUSINESS_TYPE:
                case PRODUCT:
                case CONNECTING_LINE_REGISTERED_RESOURCE + "." + MRID:
                case MEASUREMENT_UNIT:
                case CURVE_TYPE:
                    context.tags.put(xmlReader.getLocalName(), xmlReader.getElementText());
                    break;

                case IN_DOMAIN + "." + MRID:
                case OUT_DOMAIN + "." + MRID:
                    String codingScheme = xmlReader.getAttributeValue(null, CODING_SCHEME);
                    context.tags.put(xmlReader.getLocalName() + "." + CODING_SCHEME, codingScheme);
                    context.tags.put(xmlReader.getLocalName(), xmlReader.getElementText());
                    break;

                case PERIOD:
                    readPeriod(xmlReader, context);
                    break;

                case REASON:
                    readReason(xmlReader, context);
                    break;

                default:
                    throw new PowsyblException(UNEXPECTED_TOKEN + xmlReader.getLocalName());
            }
        });

        // Log TimeSeries Reason
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("TimeSeries '{}' [{}, {}] - {} ({}) : {}",  context.mRID,
                                                                    context.period.getStart(), context.period.getEnd(),
                                                                    context.code,
                                                                    StandardReasonCodeType.valueOf(context.code).getDescription(),
                                                                    context.text);
        }

        // Create DataChunk
        DoubleDataChunk dataChunk = null;
        // Computed number of steps
        int nbSteps = (int) ((context.period.getEnd().toEpochMilli() - context.period.getStart().toEpochMilli()) / context.spacing.toMillis());
        // Check if all steps are defined or not
        if (context.positions.size() == nbSteps) {
            // Uncompressed chunk
            dataChunk = new UncompressedDoubleDataChunk(0, context.quantities.stream().mapToDouble(d -> d).toArray());
        } else {
            // Compressed chunk
            int[] stepLengths = new int[context.positions.size()];
            if (context.positions.size() > 1) {
                for (int i = 1; i < context.positions.size(); i++) {
                    int lastPosition = context.positions.get(i - 1);
                    int newPosition = context.positions.get(i);
                    stepLengths[i - 1] = newPosition - lastPosition;
                }
                // Last step is computed from nbSteps and last position value
                stepLengths[stepLengths.length - 1] = 1 + (nbSteps - context.positions.getLast());
            } else {
                stepLengths[0] = nbSteps;
            }
            dataChunk = new CompressedDoubleDataChunk(0, nbSteps, context.quantities.stream().mapToDouble(d -> d).toArray(), stepLengths);
        }

        // Compute all instants of current time series
        Instant[] instants = IntStream.iterate(0, i -> i + 1)
                                      .limit(nbSteps)
                                      .mapToObj(i -> context.period.getStart().plusMillis(i * context.spacing.toMillis()))
                                      .toArray(Instant[]::new);
        // Instantiate new time series
        TimeSeriesIndex index = IrregularTimeSeriesIndex.create(instants);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata(context.mRID, TimeSeriesDataType.DOUBLE, context.tags, index);
        // Add new time series into PevfExchanges
        return new StoredDoubleTimeSeries(metadata, dataChunk);
    }

    private static void readPeriod(XMLStreamReader xmlReader, ParsingTimeSeriesContext context) throws XMLStreamException {
        XmlUtil.readUntilEndElement(PERIOD, xmlReader, () -> {
            switch (xmlReader.getLocalName()) {
                case RESOLUTION:
                    String resolution = xmlReader.getElementText();
                    context.spacing = Duration.parse(resolution);
                    break;

                case TIME_INTERVAL:
                    context.period = readTimeInterval(xmlReader, TIME_INTERVAL);
                    break;

                case POINT:
                    readPoint(xmlReader, context);
                    break;

                default:
                    throw new PowsyblException(UNEXPECTED_TOKEN + xmlReader.getLocalName());
            }
        });
    }

    private static org.threeten.extra.Interval readTimeInterval(XMLStreamReader xmlReader, String rootElement) throws XMLStreamException {
        ZonedDateTime[] interval = new ZonedDateTime[2];
        XmlUtil.readUntilEndElement(rootElement, xmlReader, () -> {
            switch (xmlReader.getLocalName()) {
                case START :
                    interval[0] = ZonedDateTime.parse(xmlReader.getElementText());
                    break;

                case END :
                    interval[1] = ZonedDateTime.parse(xmlReader.getElementText());
                    break;

                default:
                    throw new PowsyblException(UNEXPECTED_TOKEN + xmlReader.getLocalName());
            }
        });
        return org.threeten.extra.Interval.of(interval[0].toInstant(), interval[1].toInstant());
    }

    private static void readPoint(XMLStreamReader xmlReader, ParsingTimeSeriesContext context) throws XMLStreamException {
        XmlUtil.readUntilEndElement(POINT, xmlReader, () -> {
            switch (xmlReader.getLocalName()) {
                case POSITION:
                    context.positions.add(Integer.parseInt(xmlReader.getElementText()));
                    break;

                case QUANTITY:
                    context.quantities.add(Double.parseDouble(xmlReader.getElementText()));
                    break;

                default:
                    throw new PowsyblException(UNEXPECTED_TOKEN + xmlReader.getLocalName());
            }
        });
    }

    private static void readReason(XMLStreamReader xmlReader, ParsingTimeSeriesContext context) throws XMLStreamException {
        XmlUtil.readUntilEndElement(REASON, xmlReader, () -> {
            switch (xmlReader.getLocalName()) {
                case CODE:
                    context.code = xmlReader.getElementText();
                    break;

                case TEXT:
                    context.text = xmlReader.getElementText();
                    break;

                default:
                    throw new PowsyblException(UNEXPECTED_TOKEN + xmlReader.getLocalName());
            }
        });
    }
}
