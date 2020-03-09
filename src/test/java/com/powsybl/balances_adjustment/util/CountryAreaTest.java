/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 */
public class CountryAreaTest {

    private Network testNetwork1;
    private Network testNetwork2;

    private CountryAreaFactory countryAreaFR;
    private CountryAreaFactory countryAreaES;

    @Before
    public void setUp() {
        testNetwork1 = Importers.loadNetwork("testCase.xiidm", CountryAreaTest.class.getResourceAsStream("/testCase.xiidm"));
        testNetwork2 = NetworkTestFactory.createNetwork();

        countryAreaFR = new CountryAreaFactory(Country.FR);
        countryAreaES = new CountryAreaFactory(Country.ES);

    }

    private Stream<Injection> getInjectionStream(Network network) {
        Stream returnStream = Stream.empty();
        returnStream = Stream.concat(network.getGeneratorStream(), returnStream);
        returnStream = Stream.concat(network.getLoadStream(), returnStream);
        returnStream = Stream.concat(network.getDanglingLineStream(), returnStream);
        return returnStream;
    }

    private double getSumFlowCountry(Network network, Country country) {
        double sumFlow = 0;
        List<Injection> injections = getInjectionStream(network).filter(i -> country.equals(i.getTerminal().getVoltageLevel().getSubstation().getCountry().get()))
                .collect(Collectors.toList());
        for (Injection injection : injections) {
            sumFlow += injection.getTerminal().getBusBreakerView().getBus().isInMainConnectedComponent() ? injection.getTerminal().getP() : 0;

        }
        return sumFlow;
    }

    @Test
    public void testGetNetPosition() {
        //Test network with BranchBorder
        assertEquals(0, countryAreaES.create(testNetwork1).getNetPosition(), 1e-3);

        assertEquals(-getSumFlowCountry(testNetwork1, Country.FR), countryAreaFR.create(testNetwork1).getNetPosition(), 1e-3);

        //Test network with HVDCLines
        assertEquals(testNetwork2.getHvdcLine("hvdcLineFrEs").getConverterStation1().getTerminal().getP(), countryAreaFR.create(testNetwork2).getNetPosition(), 1e-3);
        assertEquals(testNetwork2.getHvdcLine("hvdcLineFrEs").getConverterStation2().getTerminal().getP(), countryAreaES.create(testNetwork2).getNetPosition(), 1e-3);
    }
}
