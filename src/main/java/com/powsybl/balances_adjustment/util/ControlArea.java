/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import java.util.*;
import java.util.stream.Collectors;

import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.iidm.network.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

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
        return new ConnectivityInspector<>(createVoltageLevelGraph(network, terminalsAndBoundaries))
                .connectedSetOf(getVoltageLevel(terminalsAndBoundaries.iterator().next()))
                .stream()
                .map(Identifiable::getId)
                .collect(Collectors.toSet());
    }

    private static Graph<VoltageLevel, Object> createVoltageLevelGraph(Network network, Set<Object> terminalsAndBoundaries) {
        Graph<VoltageLevel, Object> voltageLevelGraph = new Pseudograph<>(Object.class);
        network.getVoltageLevelStream().forEach(voltageLevelGraph::addVertex);
        network.getBranchStream()
                .filter(b -> !terminalsAndBoundaries.contains(b.getTerminal1()) && !terminalsAndBoundaries.contains(b.getTerminal2()))
                .filter(b -> {
                    if (b instanceof TieLine) {
                        TieLine tl = (TieLine) b;
                        return !terminalsAndBoundaries.contains(tl.getHalf1().getBoundary()) && !terminalsAndBoundaries.contains(tl.getHalf2().getBoundary());
                    }
                    return true;
                })
                .forEach(b -> voltageLevelGraph.addEdge(b.getTerminal1().getVoltageLevel(), b.getTerminal2().getVoltageLevel()));
        network.getThreeWindingsTransformerStream()
                .filter(twt -> twt.getLegStream().noneMatch(leg -> terminalsAndBoundaries.contains(leg.getTerminal())))
                .forEach(twt -> {
                    voltageLevelGraph.addEdge(twt.getLeg1().getTerminal().getVoltageLevel(), twt.getLeg2().getTerminal().getVoltageLevel());
                    voltageLevelGraph.addEdge(twt.getLeg1().getTerminal().getVoltageLevel(), twt.getLeg3().getTerminal().getVoltageLevel());
                    voltageLevelGraph.addEdge(twt.getLeg2().getTerminal().getVoltageLevel(), twt.getLeg3().getTerminal().getVoltageLevel());
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

    private static double getLeavingFlow(Terminal terminal) {
        return terminal.isConnected() ? terminal.getP() : 0;
    }
}
