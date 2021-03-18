/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.import_.Importers;
import org.junit.Test;

import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlAreaTest {

    @Test
    public void testNetPosition() {
        Network network = Importers.loadNetwork("controlArea.xiidm", getClass().getResourceAsStream("/controlArea.xiidm"));

        ControlAreaFactory factory = new ControlAreaFactory("_BECONTROLAREA");
        NetworkArea networkArea = factory.create(network);
        assertTrue(networkArea instanceof ControlArea);
        assertEquals(-212.0966807507164d, networkArea.getNetPosition(), 0.00000001d);
        assertEquals(5, networkArea.getVoltageLevelIds().size());
    }
}
