/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.data_exchange;

/**
 * Pan European Verification Function.
 * The exact business nature identifying the principal characteristic of a time series.
 *
 * https://www.entsoe.eu/publications/electronic-data-interchange-edi-library/
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public enum StandardBusinessType {

    /**
     * The Available Transmission Capacity that must be guaranteed because of regulatory constraints.
     */
    B63("Minimum ATC"),
    /**
     * The data as provided for a meter measurement source.
     */
    B64("Meter Measurement data");

    private final String description;

    StandardBusinessType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
