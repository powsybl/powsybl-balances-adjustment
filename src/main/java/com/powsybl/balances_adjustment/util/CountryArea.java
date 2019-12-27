/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 */
public class CountryArea extends AbstractNetworkArea {

    private final List<Country> countries;

    public CountryArea(Country... countries) {
        this.countries = Arrays.asList(countries);
    }

    @Override
    public List<VoltageLevel> getAreaVoltageLevels(Network network) {
        return network.getVoltageLevelStream()
                .filter(voltageLevel -> voltageLevel.getSubstation().getCountry().isPresent())
                .filter(voltageLevel -> countries.contains(voltageLevel.getSubstation().getCountry().get()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BorderDevice> getBorderThreeWindingTransformer(Network network, List<VoltageLevel> areaVoltageLevels) {
        return new ArrayList<>();
    }

    public List<Country> getCountries() {
        return countries;
    }
}
