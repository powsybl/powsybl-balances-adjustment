/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

/**
 * Pan European Verification Function.
 * Indicates the nature of process that the document addresses.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public enum StandardProcessType {
    /**
     * The information provided concerns a day ahead process.
     */
    A01("Day ahead");

    private final String description;

    StandardProcessType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
