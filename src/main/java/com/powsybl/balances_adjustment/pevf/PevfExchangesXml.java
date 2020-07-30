/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

import com.powsybl.commons.xml.XmlUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Pan European Verification Function XML parser.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public final class PevfExchangesXml {

    static final String MRID_ELEMENT_NAME = "mRID";
    static final String REVISION_NUMBER_ELEMENT_NAME = "revisionNumber";
    static final String TYPE_ELEMENT_NAME = "type";
    static final String PROCESS_TYPE_ELEMENT_NAME = "process.processType";

    private PevfExchangesXml() {
    }

    public static void read(PevfExchanges pevfExchanges, XMLStreamReader reader) throws XMLStreamException {
        readMetadata(pevfExchanges, reader);
    }

    protected static void readMetadata(PevfExchanges pevfExchanges, XMLStreamReader reader) throws XMLStreamException {
        String mRID = XmlUtil.readUntilEndElement(MRID_ELEMENT_NAME, reader, null);
        String revisionNumber = XmlUtil.readUntilEndElement(REVISION_NUMBER_ELEMENT_NAME, reader, null);
        String type = XmlUtil.readUntilEndElement(TYPE_ELEMENT_NAME, reader, null);
        String processType = XmlUtil.readUntilEndElement(PROCESS_TYPE_ELEMENT_NAME, reader, null);

        pevfExchanges.setmRID(mRID);
        pevfExchanges.setRevisionNumber(Integer.parseInt(revisionNumber));
        pevfExchanges.setType(StandardMessageType.valueOf(type));
        pevfExchanges.setProcessType(StandardProcessType.valueOf(processType));
    }

    protected static void readTimeSeries(PevfExchanges pevfExchanges, XMLStreamReader reader) throws XMLStreamException {
    }
}
