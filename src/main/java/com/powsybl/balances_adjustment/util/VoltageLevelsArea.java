/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class VoltageLevelsArea implements NetworkArea {

    private final List<VoltageLevel> areaVoltageLevels;

    private List<DanglingLine> danglingLineBordersCache;
    private List<Branch> branchBordersCache;
    private List<ThreeWindingsTransformer> threeWindingsTransformerBordersCache;
    private List<HvdcLine> hvdcLineBordersCache;
    private Network network;

    public VoltageLevelsArea(Network network, List<VoltageLevel> areaVoltageLevels) {
        this.areaVoltageLevels = Objects.requireNonNull(areaVoltageLevels);
        this.network = network;
        resetCache();
    }

    @Override
    public double getNetPosition() {
        cacheAreaBorders();
        double areaNetPostion = 0.;
        for (DanglingLine danglingLine : danglingLineBordersCache) {
            areaNetPostion += getLeavingFlow(danglingLine);
        }
        for (Branch branch : branchBordersCache) {
            areaNetPostion += getLeavingFlow(branch);
        }
        for (ThreeWindingsTransformer threeWindingsTransformer : threeWindingsTransformerBordersCache) {
            areaNetPostion += getLeavingFlow(threeWindingsTransformer);
        }
        for (HvdcLine hvdcLine : hvdcLineBordersCache) {
            areaNetPostion += getLeavingFlow(hvdcLine);
        }
        return areaNetPostion;
    }

    @Override
    public void resetCache() {
        danglingLineBordersCache = new ArrayList<>();
        branchBordersCache = new ArrayList<>();
        threeWindingsTransformerBordersCache = new ArrayList<>();
        hvdcLineBordersCache = new ArrayList<>();
    }

    private void cacheAreaBorders() {
        if (danglingLineBordersCache.isEmpty()) {
            danglingLineBordersCache = network.getDanglingLineStream()
                    .filter(this::isAreaBorder)
                    .collect(Collectors.toList());
        }
        if (branchBordersCache.isEmpty()) {
            branchBordersCache = network.getLineStream()
                    .filter(this::isAreaBorder)
                    .collect(Collectors.toList());
        }
        if (threeWindingsTransformerBordersCache.isEmpty()) {
            threeWindingsTransformerBordersCache = network.getThreeWindingsTransformerStream()
                    .filter(this::isAreaBorder)
                    .collect(Collectors.toList());
        }
        if (hvdcLineBordersCache.isEmpty()) {
            hvdcLineBordersCache = network.getHvdcLineStream()
                    .filter(this::isAreaBorder)
                    .collect(Collectors.toList());
        }
    }

    private boolean isAreaBorder(DanglingLine danglingLine) {
        VoltageLevel voltageLevel = danglingLine.getTerminal().getVoltageLevel();
        return areaVoltageLevels.contains(voltageLevel);
    }

    private boolean isAreaBorder(Line line) {
        VoltageLevel voltageLevelSide1 = line.getTerminal1().getVoltageLevel();
        VoltageLevel voltageLevelSide2 = line.getTerminal2().getVoltageLevel();
        return areaVoltageLevels.contains(voltageLevelSide1) && !areaVoltageLevels.contains(voltageLevelSide2) ||
                !areaVoltageLevels.contains(voltageLevelSide1) && areaVoltageLevels.contains(voltageLevelSide2);
    }

    private boolean isAreaBorder(ThreeWindingsTransformer threeWindingsTransformer) {
        VoltageLevel voltageLevelSide1 = threeWindingsTransformer.getLeg1().getTerminal().getVoltageLevel();
        VoltageLevel voltageLevelSide2 = threeWindingsTransformer.getLeg2().getTerminal().getVoltageLevel();
        VoltageLevel voltageLevelSide3 = threeWindingsTransformer.getLeg3().getTerminal().getVoltageLevel();
        boolean containsOne = areaVoltageLevels.contains(voltageLevelSide1) ||
                areaVoltageLevels.contains(voltageLevelSide2) ||
                areaVoltageLevels.contains(voltageLevelSide3);
        boolean containsAll = areaVoltageLevels.contains(voltageLevelSide1) &&
                areaVoltageLevels.contains(voltageLevelSide2) &&
                areaVoltageLevels.contains(voltageLevelSide3);
        return containsOne && !containsAll;
    }

    private boolean isAreaBorder(HvdcLine hvdcLine) {
        VoltageLevel voltageLevelSide1 = hvdcLine.getConverterStation1().getTerminal().getVoltageLevel();
        VoltageLevel voltageLevelSide2 = hvdcLine.getConverterStation2().getTerminal().getVoltageLevel();
        return areaVoltageLevels.contains(voltageLevelSide1) && !areaVoltageLevels.contains(voltageLevelSide2) ||
                !areaVoltageLevels.contains(voltageLevelSide1) && areaVoltageLevels.contains(voltageLevelSide2);
    }

    private double getLeavingFlow(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected() ? danglingLine.getTerminal().getP() : 0;
    }

    private double getLeavingFlow(Branch branch) {
        double flowSide1 = branch.getTerminal1().isConnected() ? branch.getTerminal1().getP() : 0;
        double flowSide2 = branch.getTerminal2().isConnected() ? branch.getTerminal2().getP() : 0;
        double directFlow = (flowSide1 - flowSide2) / 2;
        return areaVoltageLevels.contains(branch.getTerminal1().getVoltageLevel()) ? directFlow : -directFlow;
    }

    private double getLeavingFlow(HvdcLine hvdcLine) {
        double flowSide1 = hvdcLine.getConverterStation1().getTerminal().isConnected() ? hvdcLine.getConverterStation1().getTerminal().getP() : 0;
        double flowSide2 = hvdcLine.getConverterStation2().getTerminal().isConnected() ? hvdcLine.getConverterStation2().getTerminal().getP() : 0;
        double directFlow = (flowSide1 - flowSide2) / 2;
        return areaVoltageLevels.contains(hvdcLine.getConverterStation1().getTerminal().getVoltageLevel()) ? directFlow : -directFlow;
    }

    private double getLeavingFlow(ThreeWindingsTransformer threeWindingsTransformer) {
        double outsideFlow = 0;
        double insideFlow = 0;
        for (ThreeWindingsTransformer.Side side : ThreeWindingsTransformer.Side.values()) {
            outsideFlow += !areaVoltageLevels.contains(threeWindingsTransformer.getTerminal(side).getVoltageLevel()) && threeWindingsTransformer.getTerminal(side).isConnected()
                    ?  threeWindingsTransformer.getTerminal(side).getP() : 0;
            insideFlow += areaVoltageLevels.contains(threeWindingsTransformer.getTerminal(side).getVoltageLevel()) && threeWindingsTransformer.getTerminal(side).isConnected()
                    ?  threeWindingsTransformer.getTerminal(side).getP() : 0;
        }
        return (insideFlow - outsideFlow) / 2;
    }
}
