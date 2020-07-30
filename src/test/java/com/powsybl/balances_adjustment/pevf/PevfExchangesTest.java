/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Pan European Verification Function.
 * Check XML parsing.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class PevfExchangesTest {

    @Test
    public void parsingTestV20() throws XMLStreamException {
        InputStream is = getClass().getResourceAsStream("/testPEVFMarketDocument_2-0.xml");
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(is);
        PevfExchanges pevfExchanges = new PevfExchanges();
        PevfExchangesXml.read(pevfExchanges, xmlReader);

        assertEquals("20200405_TA_CGM_000000364", pevfExchanges.getmRID());
        assertEquals(1, pevfExchanges.getRevisionNumber());
        assertEquals(StandardMessageType.B19, pevfExchanges.getType());
        assertEquals(StandardProcessType.A01, pevfExchanges.getProcessType());
    }
}
