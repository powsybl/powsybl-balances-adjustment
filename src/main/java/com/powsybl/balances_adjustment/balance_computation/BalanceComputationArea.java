/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.balance_computation;

import com.powsybl.action.util.Scalable;
import com.powsybl.balances_adjustment.util.NetworkArea;

import java.util.Objects;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class BalanceComputationArea {
    private final String name;
    private final NetworkArea networkArea;
    private final Scalable scalable;
    private final double targetNetPosition;

    public BalanceComputationArea(String name, NetworkArea networkArea, Scalable scalable, double targetNetPosition) {
        this.name = Objects.requireNonNull(name);
        this.networkArea = Objects.requireNonNull(networkArea);
        this.scalable = Objects.requireNonNull(scalable);
        this.targetNetPosition = Objects.requireNonNull(targetNetPosition);
    }

    public String getName() {
        return name;
    }

    public NetworkArea getNetworkArea() {
        return networkArea;
    }

    public Scalable getScalable() {
        return scalable;
    }

    public double getTargetNetPosition() {
        return targetNetPosition;
    }
}
