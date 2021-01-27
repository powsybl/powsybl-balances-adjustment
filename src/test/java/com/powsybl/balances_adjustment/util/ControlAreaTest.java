/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.powsybl.cgmes.conformity.test.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlAreaTest {

    @Test
    public void testNetPosition() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlow().dataSource(),
            NetworkFactory.findDefault(), null);

        ControlAreaFactory factory = new ControlAreaFactory("_BECONTROLAREA");
        NetworkArea networkArea = factory.create(network);
        assertTrue(networkArea instanceof ControlArea);
        ControlArea controlArea = (ControlArea) networkArea;
        assertEquals(-205.90011555672567, controlArea.getNetPosition(), 0.0);
    }
}
