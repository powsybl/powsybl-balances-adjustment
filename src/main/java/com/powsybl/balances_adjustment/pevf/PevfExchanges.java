/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.pevf;

/**
 * Pan European Verification Function data.
 *
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class PevfExchanges {

    private String mRID;
    private int revisionNumber;
    private StandardMessageType type;
    private StandardProcessType processType;

    PevfExchanges() {
    }

    public String getmRID() {
        return mRID;
    }

    public void setmRID(String mRID) {
        this.mRID = mRID;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public StandardMessageType getType() {
        return type;
    }

    public void setType(StandardMessageType type) {
        this.type = type;
    }

    public StandardProcessType getProcessType() {
        return processType;
    }

    public void setProcessType(StandardProcessType processType) {
        this.processType = processType;
    }
}
