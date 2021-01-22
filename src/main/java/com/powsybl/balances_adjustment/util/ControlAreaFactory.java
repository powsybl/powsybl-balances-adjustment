/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlAreaFactory implements NetworkAreaFactory {

    private final String controlAreaId;

    public ControlAreaFactory(String controlAreaId) {
        this.controlAreaId = controlAreaId;
    }

    @Override
    public NetworkArea create(Network network) {
        return new ControlArea(network, controlAreaId);
    }

}
