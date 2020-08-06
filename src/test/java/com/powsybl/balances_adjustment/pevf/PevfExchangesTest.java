/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.timeseries.DoubleTimeSeries;
import com.powsybl.timeseries.StoredDoubleTimeSeries;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Pan European Verification Function.
 * Check XML parsing.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class PevfExchangesTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private static final PevfExchanges EXCHANGES = new PevfExchanges();
    private static final double[] TIMESERIES3_VALUES = new double[] {0.000d, 0.250d, 0.500d, 0.750d, 0.750d};
    private static final double[] TIMESERIES4_VALUES = new double[] {3939.124, 3939.124, 3939.124, 3939.124, 3939.124, 3939.124, 3926.042, 3926.042, 3926.042, 3926.042, 3926.042, 3924.460, 3924.460, 3924.460, 3924.460, 3924.460};

    @BeforeClass
    public static void setUp() throws XMLStreamException {
        InputStream is = PevfExchangesTest.class.getResourceAsStream("/testPEVFMarketDocument_2-0.xml");
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(is);
        PevfExchangesXml.read(EXCHANGES, xmlReader);
    }

    @Test
    public void baseTests() {
        // Getters
        assertEquals("MarketDocument_MRID", EXCHANGES.getMRId());
        assertEquals(1, EXCHANGES.getRevisionNumber());
        assertEquals(StandardMessageType.B19, EXCHANGES.getType());
        assertEquals(StandardProcessType.A01, EXCHANGES.getProcessType());
        assertEquals("SenderMarket", EXCHANGES.getSenderId());
        assertEquals(StandardCodingSchemeType.A01, EXCHANGES.getSenderCodingScheme());
        assertEquals(StandardRoleType.A32, EXCHANGES.getSenderMarketRole());
        assertEquals("ReceiverMarket", EXCHANGES.getReceiverId());
        assertEquals(StandardCodingSchemeType.A01, EXCHANGES.getReceiverCodingScheme());
        assertEquals(StandardRoleType.A33, EXCHANGES.getReceiverMarketRole());
        assertEquals(ZonedDateTime.parse("2020-04-05T14:30:00Z"), EXCHANGES.getCreationDate());
        assertEquals(ZonedDateTime.parse("2020-04-05T22:00Z"), EXCHANGES.getPeriodStart());
        assertEquals(ZonedDateTime.parse("2020-04-06T22:00Z"), EXCHANGES.getPeriodEnd());
        // Optional
        assertEquals(Optional.of("PEVF CGM Export"), EXCHANGES.getDatasetMarketDocumentMRId());
        assertEquals(Optional.of(StandardStatusType.A01), EXCHANGES.getDocStatus());

        // Setters
        assertEquals(EXCHANGES, EXCHANGES.setMRId("MRId"));
        assertEquals("MRId", EXCHANGES.getMRId());
        assertEquals(EXCHANGES, EXCHANGES.setRevisionNumber(2));
        assertEquals(2, EXCHANGES.getRevisionNumber());
        assertEquals(EXCHANGES, EXCHANGES.setType(StandardMessageType.B19));
        assertEquals(StandardMessageType.B19, EXCHANGES.getType());
        assertEquals(EXCHANGES, EXCHANGES.setProcessType(StandardProcessType.A01));
        assertEquals(StandardProcessType.A01, EXCHANGES.getProcessType());
        assertEquals(EXCHANGES, EXCHANGES.setSenderId("SenderId"));
        assertEquals("SenderId", EXCHANGES.getSenderId());
        assertEquals(EXCHANGES, EXCHANGES.setSenderCodingScheme(StandardCodingSchemeType.A02));
        assertEquals(StandardCodingSchemeType.A02, EXCHANGES.getSenderCodingScheme());
        assertEquals(EXCHANGES, EXCHANGES.setSenderMarketRole(StandardRoleType.A33));
        assertEquals(StandardRoleType.A33, EXCHANGES.getSenderMarketRole());
        assertEquals(EXCHANGES, EXCHANGES.setReceiverId("ReceiverId"));
        assertEquals("ReceiverId", EXCHANGES.getReceiverId());
        assertEquals(EXCHANGES, EXCHANGES.setReceiverCodingScheme(StandardCodingSchemeType.A02));
        assertEquals(StandardCodingSchemeType.A02, EXCHANGES.getReceiverCodingScheme());
        assertEquals(EXCHANGES, EXCHANGES.setReceiverMarketRole(StandardRoleType.A32));
        assertEquals(StandardRoleType.A32, EXCHANGES.getReceiverMarketRole());
        assertEquals(EXCHANGES, EXCHANGES.setCreationDate(ZonedDateTime.parse("1983-11-04T14:11:00Z")));
        assertEquals(ZonedDateTime.parse("1983-11-04T14:11:00Z"), EXCHANGES.getCreationDate());
        assertEquals(EXCHANGES, EXCHANGES.setPeriodStart(ZonedDateTime.parse("1982-11-23T02:00:00Z")));
        assertEquals(ZonedDateTime.parse("1982-11-23T02:00:00Z"), EXCHANGES.getPeriodStart());
        assertEquals(EXCHANGES, EXCHANGES.setPeriodEnd(ZonedDateTime.parse("1982-11-23T04:00:00Z")));
        assertEquals(ZonedDateTime.parse("1982-11-23T04:00:00Z"), EXCHANGES.getPeriodEnd());
        // Optional
        assertEquals(EXCHANGES, EXCHANGES.setDatasetMarketDocumentMRId("DatasetMarketDocumentMRId"));
        assertEquals(Optional.of("DatasetMarketDocumentMRId"), EXCHANGES.getDatasetMarketDocumentMRId());
        assertEquals(EXCHANGES, EXCHANGES.setDocStatus(StandardStatusType.A02));
        assertEquals(Optional.of(StandardStatusType.A02), EXCHANGES.getDocStatus());
    }

    @Test
    public void timeSeriesTests() {
        // Time Series
        DoubleTimeSeries timeSeries1 = EXCHANGES.getTimeSeries("TimeSeries1");
        // TimeSeries1 : Check metadata
        assertEquals("TimeSeries1", timeSeries1.getMetadata().getName());
        assertEquals("B63", timeSeries1.getMetadata().getTags().get("businessType"));
        assertEquals("8716867000016", timeSeries1.getMetadata().getTags().get("product"));
        assertEquals("Sender1", timeSeries1.getMetadata().getTags().get("in_Domain.mRID"));
        assertEquals("A01", timeSeries1.getMetadata().getTags().get("in_Domain.mRID.codingScheme"));
        assertEquals("Receiver1", timeSeries1.getMetadata().getTags().get("out_Domain.mRID"));
        assertEquals("A01", timeSeries1.getMetadata().getTags().get("out_Domain.mRID.codingScheme"));
        assertEquals("Sender1_Receiver1", timeSeries1.getMetadata().getTags().get("connectingLine_RegisteredResource.mRID"));
        assertEquals("MAW", timeSeries1.getMetadata().getTags().get("measurement_Unit.name"));
        assertEquals("A03", timeSeries1.getMetadata().getTags().get("curveType"));
        // TimeSeries1 : values
        // Single step, single value
        timeSeries1.stream().forEach(point -> {
            assertEquals(0.0d, point.getValue(), 0d);
            assertEquals(1586124000000L + point.getIndex() * 60 * 60 * 1000, point.getTime());
        });

        // TimeSeries2
        // Multi steps, single value
        DoubleTimeSeries timeSeries2 = EXCHANGES.getTimeSeries("TimeSeries2");
        timeSeries2.stream().forEach(point -> {
            assertEquals(0.02d, point.getValue(), 0d);
            assertEquals(1586120400000L + point.getIndex() * 60 * 60 * 1000, point.getTime());
        });

        // TimeSeries3
        // Each value defined
        DoubleTimeSeries timeSeries3 = EXCHANGES.getTimeSeries("TimeSeries3");
        assertArrayEquals(TIMESERIES3_VALUES, timeSeries3.toArray(), 0.0d);

        // TimeSeries4
        // Each value not defined
        StoredDoubleTimeSeries timeSeries4 = EXCHANGES.getTimeSeries("TimeSeries4");
        assertArrayEquals(TIMESERIES4_VALUES, timeSeries4.toArray(), 0.0d);
    }

    @Test
    public void invalidRevisionNumberTest() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Bad revision number value -1");
        EXCHANGES.setRevisionNumber(-1);
    }

    @Test
    public void coverageTests() {
        // StandardCodingSchemeType
        assertEquals("EIC", StandardCodingSchemeType.A01.getDescription());
        assertEquals("CGM", StandardCodingSchemeType.A02.getDescription());
        // StandardMessageType
        assertEquals("Reporting information market document", StandardMessageType.B19.getDescription());
        // StandardProcessType
        assertEquals("Day ahead", StandardProcessType.A01.getDescription());
        // StandardRoleType
        assertEquals("Market information aggregator", StandardRoleType.A32.getDescription());
        assertEquals("Information receiver", StandardRoleType.A33.getDescription());
        // StandardStatusType
        assertEquals("Intermediate", StandardStatusType.A01.getDescription());
        assertEquals("Final", StandardStatusType.A02.getDescription());
        // StandardBusinessType
        assertEquals("Minimum ATC", StandardBusinessType.B63.getDescription());
        assertEquals("Meter Measurement data", StandardBusinessType.B64.getDescription());
        // StandardCodeType
        assertEquals("Default Time Series applied", StandardReasonCodeType.A26.getDescription());
        assertEquals("Imposed Time Series from nominated party’s Time Series", StandardReasonCodeType.A30.getDescription());
        assertEquals("Global position not in balance", StandardReasonCodeType.A54.getDescription());
        assertEquals("Time series matched", StandardReasonCodeType.A88.getDescription());
        assertEquals("Data not yet available", StandardReasonCodeType.B08.getDescription());
        assertEquals("Data unverified", StandardReasonCodeType.B30.getDescription());
        assertEquals("Data verified", StandardReasonCodeType.B31.getDescription());
        // StandardCurveType
        assertEquals("Sequential fixed size block", StandardCurveType.A01.getDescription());
        assertEquals("Point", StandardCurveType.A02.getDescription());
        assertEquals("Variable sized Block", StandardCurveType.A03.getDescription());
    }
}
