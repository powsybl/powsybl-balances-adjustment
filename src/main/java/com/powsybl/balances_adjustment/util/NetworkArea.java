/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.iidm.network.Network;

/**
 * NetworkArea defines an area for balances adjustment as a net position provider, calculaed on a Network object
 *
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public interface NetworkArea {
    /**
     * Computes the net position of the area on a given network object.
     * Net position sign convention is positive when flows are leaving the area (export) and negative
     * when flows feed the area (import).
     *
     * @param network Network on which to compute the net position
     * @return Sum of the flows leaving the area
     */
    double getNetPosition(Network network);

    /**
     * Utility method in the algorithm to allow cache mechanism to be reset, when changing
     * the network object on which getNetPosition() is applied for example.
     */
    void resetCache();
}
