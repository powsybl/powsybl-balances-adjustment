/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


/**
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 */
public class VoltageLevelsAreaTest {

    private Network testNetwork;
    private VoltageLevelsArea voltageLevelsArea;
    private List<VoltageLevel> voltageLevels = new ArrayList<>();

    @Before
    public void setUp() {
        testNetwork = Importers.loadNetwork("testCase.xiidm", VoltageLevelsAreaTest.class.getResourceAsStream("/testCase.xiidm"));

        voltageLevels = testNetwork.getVoltageLevelStream().filter(v -> v.getId().equals("FFR1AA1") || v.getId().equals("DDE3AA1"))
                .collect(Collectors.toList());

        voltageLevelsArea = new VoltageLevelsArea(voltageLevels);

    }

    @Test
    public void testGetNetPosition() {
        List<Double> flows = new ArrayList<>();
        flows.add(testNetwork.getBranch("FFR1AA1  FFR3AA1  1").getTerminal1().getP());
        flows.add(testNetwork.getBranch("FFR2AA1  FFR3AA1  1").getTerminal1().getP());
        flows.add(testNetwork.getBranch("DDE1AA1  DDE3AA1  1").getTerminal2().getP());
        flows.add(testNetwork.getBranch("DDE2AA1  DDE3AA1  1").getTerminal2().getP());

        assertEquals(flows.stream().mapToDouble(f -> f).sum(), voltageLevelsArea.getNetPosition(testNetwork), 1e-3);
    }
}
