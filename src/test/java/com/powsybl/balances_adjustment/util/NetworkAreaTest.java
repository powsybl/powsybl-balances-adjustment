/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 */
public class NetworkAreaTest {

    private Network testNetwork1;
    private NetworkArea countryAreaFR;

    private NetworkArea voltageLevelsArea1;

    @Before
    public void setUp() {
        testNetwork1 = Importers.loadNetwork("testCase.xiidm", NetworkAreaTest.class.getResourceAsStream("/testCase.xiidm"));

        List<VoltageLevel> voltageLevels1 = testNetwork1.getVoltageLevelStream().filter(v -> v.getId().equals("FFR1AA1") || v.getId().equals("FFR3AA1"))
                .collect(Collectors.toList());

        voltageLevelsArea1 = new VoltageLevelsArea(testNetwork1, voltageLevels1);

        countryAreaFR = new CountryArea(testNetwork1, Country.FR);

    }

    @Test
    public void testGetNetPosition() {
        assertEquals(countryAreaFR.getNetPosition(), voltageLevelsArea1.getNetPosition(), 1e-3);
    }

}
