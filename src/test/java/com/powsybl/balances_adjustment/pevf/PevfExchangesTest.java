/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.time.ZonedDateTime;
import java.util.Optional;

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

    private final PevfExchanges pevfExchanges = new PevfExchanges();

    @Test
    public void parsingTestV20() throws XMLStreamException, IOException {
        InputStream is = getClass().getResourceAsStream("/testPEVFMarketDocument_2-0.xml");
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(is);
        PevfExchangesXml.read(pevfExchanges, xmlReader);

        // Getters
        assertEquals("MarketDocument_MRID", pevfExchanges.getMRId());
        assertEquals(1, pevfExchanges.getRevisionNumber());
        assertEquals(StandardMessageType.B19, pevfExchanges.getType());
        assertEquals(StandardProcessType.A01, pevfExchanges.getProcessType());
        assertEquals("SenderMarket", pevfExchanges.getSenderId());
        assertEquals(StandardCodingSchemeType.A01, pevfExchanges.getSenderCodingScheme());
        assertEquals(StandardRoleType.A32, pevfExchanges.getSenderMarketRole());
        assertEquals("ReceiverMarket", pevfExchanges.getReceiverId());
        assertEquals(StandardCodingSchemeType.A01, pevfExchanges.getReceiverCodingScheme());
        assertEquals(StandardRoleType.A33, pevfExchanges.getReceiverMarketRole());
        assertEquals(ZonedDateTime.parse("2020-04-05T14:30:00Z"), pevfExchanges.getCreationDate());
        assertEquals(ZonedDateTime.parse("2020-04-05T22:00Z"), pevfExchanges.getPeriodStart());
        assertEquals(ZonedDateTime.parse("2020-04-06T22:00Z"), pevfExchanges.getPeriodEnd());
        // Optional
        assertEquals(Optional.of("PEVF CGM Export"), pevfExchanges.getDatasetMarketDocumentMRId());
        assertEquals(Optional.of(StandardStatusType.A01), pevfExchanges.getDocStatus());

        // Setters
        assertEquals(pevfExchanges, pevfExchanges.setMRId("MRId"));
        assertEquals("MRId", pevfExchanges.getMRId());
        assertEquals(pevfExchanges, pevfExchanges.setRevisionNumber(2));
        assertEquals(2, pevfExchanges.getRevisionNumber());
        assertEquals(pevfExchanges, pevfExchanges.setType(StandardMessageType.B19));
        assertEquals(StandardMessageType.B19, pevfExchanges.getType());
        assertEquals(pevfExchanges, pevfExchanges.setProcessType(StandardProcessType.A01));
        assertEquals(StandardProcessType.A01, pevfExchanges.getProcessType());
        assertEquals(pevfExchanges, pevfExchanges.setSenderId("SenderId"));
        assertEquals("SenderId", pevfExchanges.getSenderId());
        assertEquals(pevfExchanges, pevfExchanges.setSenderCodingScheme(StandardCodingSchemeType.A02));
        assertEquals(StandardCodingSchemeType.A02, pevfExchanges.getSenderCodingScheme());
        assertEquals(pevfExchanges, pevfExchanges.setSenderMarketRole(StandardRoleType.A33));
        assertEquals(StandardRoleType.A33, pevfExchanges.getSenderMarketRole());
        assertEquals(pevfExchanges, pevfExchanges.setReceiverId("ReceiverId"));
        assertEquals("ReceiverId", pevfExchanges.getReceiverId());
        assertEquals(pevfExchanges, pevfExchanges.setReceiverCodingScheme(StandardCodingSchemeType.A02));
        assertEquals(StandardCodingSchemeType.A02, pevfExchanges.getReceiverCodingScheme());
        assertEquals(pevfExchanges, pevfExchanges.setReceiverMarketRole(StandardRoleType.A32));
        assertEquals(StandardRoleType.A32, pevfExchanges.getReceiverMarketRole());
        assertEquals(pevfExchanges, pevfExchanges.setCreationDate(ZonedDateTime.parse("1983-11-04T14:11:00Z")));
        assertEquals(ZonedDateTime.parse("1983-11-04T14:11:00Z"), pevfExchanges.getCreationDate());
        assertEquals(pevfExchanges, pevfExchanges.setPeriodStart(ZonedDateTime.parse("1982-11-23T02:00:00Z")));
        assertEquals(ZonedDateTime.parse("1982-11-23T02:00:00Z"), pevfExchanges.getPeriodStart());
        assertEquals(pevfExchanges, pevfExchanges.setPeriodEnd(ZonedDateTime.parse("1982-11-23T04:00:00Z")));
        assertEquals(ZonedDateTime.parse("1982-11-23T04:00:00Z"), pevfExchanges.getPeriodEnd());
        // Optional
        assertEquals(pevfExchanges, pevfExchanges.setDatasetMarketDocumentMRId("DatasetMarketDocumentMRId"));
        assertEquals(Optional.of("DatasetMarketDocumentMRId"), pevfExchanges.getDatasetMarketDocumentMRId());
        assertEquals(pevfExchanges, pevfExchanges.setDocStatus(StandardStatusType.A02));
        assertEquals(Optional.of(StandardStatusType.A02), pevfExchanges.getDocStatus());
    }

    @Test
    public void invalidMRId() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Bad revision number value -1");
        pevfExchanges.setRevisionNumber(-1);
    }

    @Test
    public void coverage() {
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
    }
}
