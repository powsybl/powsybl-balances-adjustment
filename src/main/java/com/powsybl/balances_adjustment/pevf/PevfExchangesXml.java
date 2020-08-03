/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.time.ZonedDateTime;

/**
 * Pan European Verification Function XML parser.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public final class PevfExchangesXml {

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
    private static final String TIME_INTERVAL_ELEMENT_NAME = "time_Period.timeInterval";
    private static final String START_ELEMENT_NAME = "start";
    private static final String END_ELEMENT_NAME = "end";
    private static final String DATASET_MARKET_DOCUMENT_ELEMENT_NAME = "dataset_MarketDocument";
    private static final String DOC_STATUS_ELEMENT_NAME = "docStatus";
    private static final String VALUE_ELEMENT_NAME = "value";
    private static final String TIMESERIES_ELEMENT_NAME = "TimeSeries";

    private PevfExchangesXml() {
    }

    public static void read(PevfExchanges pevfExchanges, XMLStreamReader reader) throws XMLStreamException {
        XmlUtil.readUntilEndElementWithDepth(ROOT_ELEMENT_NAME, reader,  (int depth) -> {
            switch (reader.getLocalName()) {
                case MRID_ELEMENT_NAME: {
                    switch (depth) {
                        case 1: {
                            String mRID = reader.getElementText();
                            pevfExchanges.setMRId(mRID);
                            break;
                        }
                        case 2: {
                            // New TimeSeries
                            String mRID = reader.getElementText();
                            //pevfExchanges.addTimeSeries(new com.powsybl.timeseries.TimeSeries());
                            break;
                        }
                        default:
                            throw new PowsyblException("Unexpected value: " + depth);
                    }
                    break;
                }
                case REVISION_NUMBER_ELEMENT_NAME: {
                    String revisionNumber = reader.getElementText();
                    pevfExchanges.setRevisionNumber(Integer.parseInt(revisionNumber));
                    break;
                }
                case TYPE_ELEMENT_NAME: {
                    String type = reader.getElementText();
                    pevfExchanges.setType(StandardMessageType.valueOf(type));
                    break;
                }
                case PROCESS_TYPE_ELEMENT_NAME: {
                    String processType = reader.getElementText();
                    pevfExchanges.setProcessType(StandardProcessType.valueOf(processType));
                    break;
                }
                case SENDER_MARKET_PARTICIPANT_ELEMENT_NAME + "." + MRID_ELEMENT_NAME: {
                    String codingScheme = reader.getAttributeValue(null, CODING_SCHEME_ELEMENT_NAME);
                    pevfExchanges.setSenderCodingScheme(StandardCodingSchemeType.valueOf(codingScheme));
                    String senderId = reader.getElementText();
                    pevfExchanges.setSenderId(senderId);
                    break;
                }
                case SENDER_MARKET_PARTICIPANT_ELEMENT_NAME + "." + MARKET_ROLE_ELEMENT_NAME: {
                    String senderMarketRole = reader.getElementText();
                    pevfExchanges.setSenderMarketRole(StandardRoleType.valueOf(senderMarketRole));
                    break;
                }
                case RECEIVER_MARKET_PARTICIPANT_ELEMENT_NAME + "." + MRID_ELEMENT_NAME: {
                    String codingScheme = reader.getAttributeValue(null, CODING_SCHEME_ELEMENT_NAME);
                    pevfExchanges.setReceiverCodingScheme(StandardCodingSchemeType.valueOf(codingScheme));
                    String receiverId = reader.getElementText();
                    pevfExchanges.setReceiverId(receiverId);
                    break;
                }
                case RECEIVER_MARKET_PARTICIPANT_ELEMENT_NAME + "." + MARKET_ROLE_ELEMENT_NAME: {
                    String receiverMarketRole = reader.getElementText();
                    pevfExchanges.setReceiverMarketRole(StandardRoleType.valueOf(receiverMarketRole));
                    break;
                }
                case CREATION_DATETIME_ELEMENT_NAME: {
                    String creationDate = reader.getElementText();
                    pevfExchanges.setCreationDate(ZonedDateTime.parse(creationDate));
                    break;
                }
                case TIME_INTERVAL_ELEMENT_NAME: {
                    String periodStart = XmlUtil.readUntilEndElement(START_ELEMENT_NAME, reader, null);
                    String periodEnd = XmlUtil.readUntilEndElement(END_ELEMENT_NAME, reader, null);
                    pevfExchanges.setPeriodStart(ZonedDateTime.parse(periodStart));
                    pevfExchanges.setPeriodEnd(ZonedDateTime.parse(periodEnd));
                    break;
                }
                case DATASET_MARKET_DOCUMENT_ELEMENT_NAME + "." + MRID_ELEMENT_NAME: {
                    String datasetId = reader.getElementText();
                    pevfExchanges.setDatasetMarketDocumentMRId(datasetId);
                    break;
                }
                case DOC_STATUS_ELEMENT_NAME: {
                    String value = XmlUtil.readUntilEndElement(VALUE_ELEMENT_NAME, reader, null);
                    pevfExchanges.setDocStatus(StandardStatusType.valueOf(value));
                    break;
                }
                case TIMESERIES_ELEMENT_NAME: {
                    // Nothing to do
                    break;
                }
                default: {
                    //throw new PowsyblException("Unexpected value: " + reader.getLocalName());
                }
            }
        });
    }
}
