/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.cgmes.conversion.elements.areainterchange.CgmesControlArea;
import com.powsybl.cgmes.conversion.extensions.CgmesControlAreaMapping;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlArea implements NetworkArea {

    private final CgmesControlArea cgmesControlArea;

    public ControlArea(Network network, String controlAreaId) {
        CgmesControlAreaMapping cgmesControlAreaMapping = network.getExtension(CgmesControlAreaMapping.class);
        cgmesControlArea = cgmesControlAreaMapping.getCgmesControlAreas().get(controlAreaId);
    }

    @Override
    public double getNetPosition() {
        return 0;
    }

}
