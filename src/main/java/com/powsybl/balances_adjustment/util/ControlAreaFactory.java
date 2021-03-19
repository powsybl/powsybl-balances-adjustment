/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlAreaFactory implements NetworkAreaFactory {

    private String controlAreaId;
    private final Set<Terminal> terminals = new HashSet<>();
    private final Set<Boundary> boundaries = new HashSet<>();

    public ControlAreaFactory(String controlAreaId) {
        this.controlAreaId = controlAreaId;
    }

    public ControlAreaFactory(Set<Terminal> terminals, Set<Boundary> boundaries) {
        if (terminals == null && boundaries == null) {
            throw new PowsyblException("Undefined tie flows for control area");
        }
        if (terminals != null) {
            this.terminals.addAll(terminals);
        }
        if (boundaries != null) {
            this.boundaries.addAll(boundaries);
        }
    }

    @Override
    public NetworkArea create(Network network) {
        if (controlAreaId != null) {
            return new ControlArea(network, controlAreaId);
        } else if (!terminals.isEmpty() || !boundaries.isEmpty()) {
            return new ControlArea(network, terminals, boundaries);
        }
        throw new PowsyblException("Control area should have a defined controlAreaId or defined sets of tieFlows");
    }

}
