/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.action.util.Scalable;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlAreaTest {

    @Test
    public void testNetPosition() {
        Network network = Importers.loadNetwork("controlArea.xiidm", getClass().getResourceAsStream("/controlArea.xiidm"));

        assertFalse(NetworkAreaUtil.containsSeveralSynchronousComponent(network, "_BECONTROLAREA"));

        List<NetworkAreaFactory> controlAreaFactories = NetworkAreaUtil.createNetworkAreaFactoryBySynchronousComponent(network, "_BECONTROLAREA");
        assertEquals(1, controlAreaFactories.size());

        NetworkAreaFactory factory = new ControlAreaFactory("_BECONTROLAREA");
        NetworkArea networkArea = factory.create(network);
        assertTrue(networkArea instanceof ControlArea);
        assertEquals(-212.0966807507164d, networkArea.getNetPosition(), 0.00000001d);
        assertEquals(5, networkArea.getContainedBusViewBuses().size());

        List<Scalable> scalables = NetworkAreaUtil.createLoadScalables(networkArea);
        assertEquals(3, scalables.size());
    }
}
