/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlAreaTest {

    @Test
    public void testBeControlArea() {
        Network network = Importers.loadNetwork("controlArea.xiidm", getClass().getResourceAsStream("/controlArea.xiidm"));

        assertFalse(NetworkAreaUtil.containsSeveralSynchronousComponent(network, "_BECONTROLAREA"));

        List<NetworkAreaFactory> controlAreaFactories = NetworkAreaUtil.createNetworkAreaFactoryBySynchronousComponent(network, "_BECONTROLAREA");
        assertEquals(1, controlAreaFactories.size());

        NetworkAreaFactory factory = new ControlAreaFactory("_BECONTROLAREA");
        NetworkArea networkArea1 = factory.create(network);
        assertTrue(networkArea1 instanceof ControlArea);
        assertEquals(-212.0966807507164d, networkArea1.getNetPosition(), 0.00000001d);
        assertEquals(5, networkArea1.getContainedBusViewBuses().size());

        NetworkArea networkArea2 = controlAreaFactories.get(0).create(network);
        assertTrue(networkArea2 instanceof ControlArea);
        assertEquals(networkArea1.getNetPosition(), networkArea2.getNetPosition(), 0.0);
        assertTrue(networkArea1.getContainedBusViewBuses().containsAll(networkArea2.getContainedBusViewBuses()));
        assertTrue(networkArea2.getContainedBusViewBuses().containsAll(networkArea1.getContainedBusViewBuses()));
    }

    @Test
    public void testFaultyControlArea() {
        Network network = Importers.loadNetwork("controlArea.xiidm", getClass().getResourceAsStream("/controlArea.xiidm"));

        assertTrue(NetworkAreaUtil.containsSeveralSynchronousComponent(network, "FAULTY"));

        List<NetworkAreaFactory> controlAreaFactories = NetworkAreaUtil.createNetworkAreaFactoryBySynchronousComponent(network, "FAULTY");
        assertEquals(2, controlAreaFactories.size());

    }

    @Test
    public void testNullControlArea() {
        try {
            new ControlAreaFactory(null, null);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Undefined tie flows for control area", e.getMessage());
        }
    }

    @Test
    public void testEmptySetControlArea() {
        Network network = Importers.loadNetwork("controlArea.xiidm", getClass().getResourceAsStream("/controlArea.xiidm"));
        NetworkAreaFactory factory = new ControlAreaFactory(Collections.emptySet(), Collections.emptySet());
        try {
            factory.create(network);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Control area should have a defined controlAreaId or defined sets of tieFlows", e.getMessage());
        }
    }

    @Test
    public void testEmptyControlArea() {
        Network network = Importers.loadNetwork("controlArea.xiidm", getClass().getResourceAsStream("/controlArea.xiidm"));
        network.getExtension(CgmesControlAreas.class).newCgmesControlArea()
                .setId("EMPTY")
                .setName("EMPTY")
                .setEnergyIdentificationCodeEic("EMPTY")
                .add();
        NetworkAreaFactory factory = new ControlAreaFactory("EMPTY");
        try {
            factory.create(network);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Undefined tie flows for control area EMPTY", e.getMessage());
        }
    }
}
