/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.timeseries.DoubleTimeSeries;
import com.powsybl.timeseries.StoredDoubleTimeSeries;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.stream.XMLStreamException;
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

    private static final double[] TIME_SERIES_1_VALUES = new double[] {0.000d};
    private static final double[] TIME_SERIES_2_VALUES = new double[] {0.020d, 0.020d};
    private static final double[] TIME_SERIES_3_VALUES = new double[] {0.000d, 0.250d, 0.500d, 0.750d};
    private static final double[] TIME_SERIES_4_VALUES = new double[] {3939.124, 3939.124, 3939.124, 3939.124, 3939.124, 3939.124, 3926.042, 3926.042, 3926.042, 3926.042, 3926.042, 3924.460, 3924.460, 3924.460, 3924.460};

    @Test
    public void baseTests() throws XMLStreamException {
        final InputStreamReader reader = new InputStreamReader(PevfExchangesTest.class.getResourceAsStream("/testPEVFMarketDocument_2-0.xml"));
        final PevfExchanges exchanges = PevfExchangesXml.parse(reader);

        // Getters
        assertEquals("MarketDocument_MRID", exchanges.getMRId());
        assertEquals(1, exchanges.getRevisionNumber());
        assertEquals(StandardMessageType.B19, exchanges.getType());
        assertEquals(StandardProcessType.A01, exchanges.getProcessType());
        assertEquals("SenderMarket", exchanges.getSenderId());
        assertEquals(StandardCodingSchemeType.A01, exchanges.getSenderCodingScheme());
        assertEquals(StandardRoleType.A32, exchanges.getSenderMarketRole());
        assertEquals("ReceiverMarket", exchanges.getReceiverId());
        assertEquals(StandardCodingSchemeType.A01, exchanges.getReceiverCodingScheme());
        assertEquals(StandardRoleType.A33, exchanges.getReceiverMarketRole());
        assertEquals(ZonedDateTime.parse("2020-04-05T14:30:00Z"), exchanges.getCreationDate());
        assertEquals(ZonedDateTime.parse("2020-04-05T22:00Z").toInstant(), exchanges.getPeriod().getStart());
        assertEquals(ZonedDateTime.parse("2020-04-06T22:00Z").toInstant(), exchanges.getPeriod().getEnd());
        // Optional
        assertEquals(Optional.of("PEVF CGM Export"), exchanges.getDatasetMarketDocumentMRId());
        assertEquals(Optional.of(StandardStatusType.A01), exchanges.getDocStatus());
    }

    @Test
    public void timeSeriesTests() throws XMLStreamException {
        final InputStreamReader reader = new InputStreamReader(PevfExchangesTest.class.getResourceAsStream("/testPEVFMarketDocument_2-0.xml"));
        final PevfExchanges exchanges = PevfExchangesXml.parse(reader);
        // Time Series
        DoubleTimeSeries timeSeries1 = exchanges.getTimeSeries("TimeSeries1");
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
        assertArrayEquals(TIME_SERIES_1_VALUES, timeSeries1.toArray(), 0.0d);

        // TimeSeries2
        // Multi steps, single value
        DoubleTimeSeries timeSeries2 = exchanges.getTimeSeries("TimeSeries2");
        assertArrayEquals(TIME_SERIES_2_VALUES, timeSeries2.toArray(), 0.0d);

        // TimeSeries3
        // Each value defined
        DoubleTimeSeries timeSeries3 = exchanges.getTimeSeries("TimeSeries3");
        assertArrayEquals(TIME_SERIES_3_VALUES, timeSeries3.toArray(), 0.0d);

        // TimeSeries4
        // Each value not defined
        StoredDoubleTimeSeries timeSeries4 = exchanges.getTimeSeries("TimeSeries4");
        assertArrayEquals(TIME_SERIES_4_VALUES, timeSeries4.toArray(), 0.0d);
    }

    @Test
    public void invalidRevisionNumberTest() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Bad revision number value -1");

        new PevfExchanges("", -1, StandardMessageType.B19, StandardProcessType.A01,
                 "", StandardCodingSchemeType.A01, StandardRoleType.A32,
                "", StandardCodingSchemeType.A02, StandardRoleType.A33,
                          ZonedDateTime.now(), null, "", StandardStatusType.A01, null);
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
