/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import java.util.*;

import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.math.graph.UndirectedGraph;
import com.powsybl.math.graph.UndirectedGraphImpl;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlArea implements NetworkArea {

    private final Set<Object> terminalsAndBoundaries = new HashSet<>();
    private final Set<String> voltageLevelIdsCache;

    public ControlArea(Network network, String controlAreaId) {
        CgmesControlAreas cgmesControlAreaMapping = network.getExtension(CgmesControlAreas.class);
        terminalsAndBoundaries.addAll(cgmesControlAreaMapping.getCgmesControlArea(controlAreaId).getTerminals());
        terminalsAndBoundaries.addAll(cgmesControlAreaMapping.getCgmesControlArea(controlAreaId).getBoundaries());

        voltageLevelIdsCache = getVoltageLevelIds(network, terminalsAndBoundaries);
    }

    @Override
    public double getNetPosition() {
        return terminalsAndBoundaries.parallelStream().mapToDouble(o -> {
            if (o instanceof Terminal) {
                return getLeavingFlow((Terminal) o);
            } else if (o instanceof Boundary) {
                return -((Boundary) o).getP();
            }
            throw new AssertionError();
        }).sum();
    }

    @Override
    public Collection<String> getVoltageLevelIds() {
        return Collections.unmodifiableSet(voltageLevelIdsCache);
    }

    private static Set<String> getVoltageLevelIds(Network network, Set<Object> terminalsAndBoundaries) {
        Set<String> voltageLevelIds = new HashSet<>();
        Map<VoltageLevel, Integer> vlNums = new HashMap<>();
        UndirectedGraph<Object, Object> voltageLevelGraph = createVoltageLevelGraph(network, vlNums);
        for (Object o : terminalsAndBoundaries) {
            voltageLevelGraph.traverse(vlNums.get(getVoltageLevel(o)), (v1, e, v2) -> findContainedVoltageLevels(voltageLevelGraph.getEdgeObject(e),
                    voltageLevelIds, terminalsAndBoundaries));
        }
        return voltageLevelIds;
    }

    private static UndirectedGraph<Object, Object> createVoltageLevelGraph(Network network, Map<VoltageLevel, Integer> vlNums) {
        UndirectedGraph<Object, Object> voltageLevelGraph = new UndirectedGraphImpl<>();
        network.getVoltageLevelStream().forEach(vl -> {
            int v = voltageLevelGraph.addVertex();
            vlNums.put(vl, v);
        });
        network.getBranchStream().forEach(b -> voltageLevelGraph.addEdge(vlNums.get(b.getTerminal1().getVoltageLevel()), vlNums.get(b.getTerminal2().getVoltageLevel()), b));
        network.getThreeWindingsTransformerStream().forEach(twt -> {
            int v = voltageLevelGraph.addVertex();
            voltageLevelGraph.addEdge(vlNums.get(twt.getLeg1().getTerminal().getVoltageLevel()), v, twt.getLeg1());
            voltageLevelGraph.addEdge(v, vlNums.get(twt.getLeg2().getTerminal().getVoltageLevel()), twt.getLeg2());
            voltageLevelGraph.addEdge(v, vlNums.get(twt.getLeg3().getTerminal().getVoltageLevel()), twt.getLeg3());
        });
        network.getDanglingLineStream().forEach(dl -> {
            int v = voltageLevelGraph.addVertex();
            voltageLevelGraph.addEdge(vlNums.get(dl.getTerminal().getVoltageLevel()), v, dl);
        });
        return voltageLevelGraph;
    }

    private static VoltageLevel getVoltageLevel(Object o) {
        if (o instanceof Terminal) {
            return ((Terminal) o).getVoltageLevel();
        } else if (o instanceof Boundary) {
            return ((Boundary) o).getVoltageLevel();
        } else {
            throw new AssertionError();
        }
    }

    private static TraverseResult findContainedVoltageLevels(Object edge, Set<String> voltageLevelIds, Set<Object> terminalsAndBoundaries) {
        if (edge instanceof Branch) {
            Branch branch = (Branch) edge;
            voltageLevelIds.add(branch.getTerminal1().getVoltageLevel().getId());
            if (containsTieFlows(branch, Branch.Side.ONE, terminalsAndBoundaries)) {
                return TraverseResult.TERMINATE;
            }
            voltageLevelIds.add(branch.getTerminal2().getVoltageLevel().getId());
            if (containsTieFlows(branch, Branch.Side.TWO, terminalsAndBoundaries)) {
                return TraverseResult.TERMINATE;
            }
        } else if (edge instanceof ThreeWindingsTransformer.Leg) {
            ThreeWindingsTransformer.Leg leg = (ThreeWindingsTransformer.Leg) edge;
            voltageLevelIds.add(leg.getTerminal().getVoltageLevel().getId());
            if (terminalsAndBoundaries.contains(leg.getTerminal())) {
                return TraverseResult.TERMINATE;
            }
        } else if (edge instanceof DanglingLine) {
            DanglingLine dl = (DanglingLine) edge;
            voltageLevelIds.add(dl.getTerminal().getVoltageLevel().getId());
            if (terminalsAndBoundaries.contains(dl.getBoundary()) || terminalsAndBoundaries.contains(dl.getTerminal())) {
                return TraverseResult.TERMINATE;
            }
        }
        return TraverseResult.CONTINUE;
    }

    private static boolean containsTieFlows(Branch branch, Branch.Side side, Set<Object> terminalsAndBoundaries) {
        if (terminalsAndBoundaries.contains(branch.getTerminal(side))) {
            return true;
        }
        if (branch instanceof TieLine) {
            TieLine tl = (TieLine) branch;
            return terminalsAndBoundaries.contains(tl.getHalf(side).getBoundary());
        }
        return false;
    }

    private static double getLeavingFlow(Terminal terminal) {
        return terminal.isConnected() ? terminal.getP() : 0;
    }
}
