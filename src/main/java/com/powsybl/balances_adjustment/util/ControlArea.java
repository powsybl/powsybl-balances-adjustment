/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import java.util.HashSet;
import java.util.Set;

import com.powsybl.cgmes.conversion.extensions.CgmesControlArea.EquipmentEnd;
import com.powsybl.cgmes.conversion.extensions.CgmesControlAreaMapping;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlArea implements NetworkArea {

    private final Set<Terminal> terminals;

    public ControlArea(Network network, String controlAreaId) {
        CgmesControlAreaMapping cgmesControlAreaMapping = network.getExtension(CgmesControlAreaMapping.class);
        terminals = calculateTerminals(network, cgmesControlAreaMapping.getTerminals(controlAreaId));
    }

    @Override
    public double getNetPosition() {
        return terminals.parallelStream().mapToDouble(this::getLeavingFlow).sum();
    }

    private Set<Terminal> calculateTerminals(Network network, Set<EquipmentEnd> equipmentEnds) {
        Set<Terminal> terminals = new HashSet<>();
        equipmentEnds.forEach(equipmentEnd -> {
            Identifiable<?> i = network.getIdentifiable(equipmentEnd.getEquipmentId());
            if (i instanceof Connectable) {
                Connectable<?> c = (Connectable<?>) i;
                terminals.add(c.getTerminals().get(equipmentEnd.getEnd() - 1));
            }
        });
        return terminals;
    }

    private double getLeavingFlow(Terminal terminal) {
        return terminal.isConnected() ? terminal.getP() : 0;
    }
}
