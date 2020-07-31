/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

/**
 * Pan European Verification Function.
 * Identification of the role played by a party.
 *
 *  https://www.entsoe.eu/publications/electronic-data-interchange-edi-library/
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public enum StandardRoleType {
    /**
     * Market information aggregator.
     * Refer to role model definitions in the ENTSO-E Harmonised Role Model Document.
     * A party that collects information from different sources and assembles  it to provide a summary of the market.
     */
    A32("Market information aggregator"),
    /**
     * Information receiver.
     * Refer to role model definitions in the ENTSO-E Harmonised Role Model Document.
     * A party, not necessarily a market participant, which receives information about the market.
     */
    A33("Information receiver");

    private final String description;

    StandardRoleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}