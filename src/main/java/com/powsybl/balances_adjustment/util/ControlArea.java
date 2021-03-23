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
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class ControlArea implements NetworkArea {

    private final Set<Object> terminalsAndBoundaries = new HashSet<>();
    private final Set<Bus> busesCache;

    public ControlArea(Network network, String controlAreaId) {
        CgmesControlAreas controlAreas = network.getExtension(CgmesControlAreas.class);
        terminalsAndBoundaries.addAll(controlAreas.getCgmesControlArea(controlAreaId).getTerminals());
        terminalsAndBoundaries.addAll(controlAreas.getCgmesControlArea(controlAreaId).getBoundaries());

        busesCache = getContainedBusViewBuses(network, terminalsAndBoundaries);
    }

    public ControlArea(Network network, Set<Terminal> terminals, Set<Boundary> boundaries) {
        terminalsAndBoundaries.addAll(terminals);
        terminalsAndBoundaries.addAll(boundaries);

        busesCache = getContainedBusViewBuses(network, terminalsAndBoundaries);
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
    public Collection<Bus> getContainedBusViewBuses() {
        return Collections.unmodifiableSet(busesCache);
    }

    private static Set<Bus> getContainedBusViewBuses(Network network, Set<Object> terminalsAndBoundaries) {
        Graph<Bus, Object> busesGraph = createBusesGraph(network, terminalsAndBoundaries);
        ConnectivityInspector<Bus, Object> connectivityInspector = new ConnectivityInspector<>(busesGraph);
        Set<Set<Bus>> busesSets = terminalsAndBoundaries.stream()
                .map(ControlArea::getBus)
                .filter(busesGraph::containsVertex) // sometimes the start vertex is not in the graph when the control area is flawed
                .map(connectivityInspector::connectedSetOf)
                .collect(Collectors.toSet());
        if (busesSets.size() > 1) {
            throw new PowsyblException("Control area contains more than one synchronous component. " +
                    "You should use utility methods in NetworkAreaUtil to create proper control areas");
        }
        return busesSets.stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    private static Graph<Bus, Object> createBusesGraph(Network network, Set<Object> terminalsAndBoundaries) {
        Graph<Bus, Object> busesGraph = new Pseudograph<>(Object.class);
        network.getBusView().getBusStream().forEach(busesGraph::addVertex);
        network.getBranchStream()
                .filter(b -> b.getTerminal1().isConnected() && b.getTerminal2().isConnected())
                .filter(b -> !terminalsAndBoundaries.contains(b.getTerminal1()) && !terminalsAndBoundaries.contains(b.getTerminal2()))
                .filter(b -> {
                    if (b instanceof TieLine) {
                        TieLine tl = (TieLine) b;
                        return !terminalsAndBoundaries.contains(tl.getHalf1().getBoundary()) && !terminalsAndBoundaries.contains(tl.getHalf2().getBoundary());
                    }
                    return true;
                })
                .forEach(b -> busesGraph.addEdge(b.getTerminal1().getBusView().getBus(), b.getTerminal2().getBusView().getBus()));
        network.getThreeWindingsTransformerStream()
                .filter(twt -> twt.getLegStream().allMatch(leg -> leg.getTerminal().isConnected()))
                .filter(twt -> twt.getLegStream().noneMatch(leg -> terminalsAndBoundaries.contains(leg.getTerminal())))
                .forEach(twt -> {
                    busesGraph.addEdge(twt.getLeg1().getTerminal().getBusView().getBus(), twt.getLeg2().getTerminal().getBusView().getBus());
                    busesGraph.addEdge(twt.getLeg1().getTerminal().getBusView().getBus(), twt.getLeg3().getTerminal().getBusView().getBus());
                    busesGraph.addEdge(twt.getLeg2().getTerminal().getBusView().getBus(), twt.getLeg3().getTerminal().getBusView().getBus());
                });
        network.getHvdcLineStream()
                .filter(hvdcLine -> hvdcLine.getConverterStation1().getTerminal().isConnected() && hvdcLine.getConverterStation2().getTerminal().isConnected())
                .filter(hvdcLine -> !terminalsAndBoundaries.contains(hvdcLine.getConverterStation1().getTerminal()) && !terminalsAndBoundaries.contains(hvdcLine.getConverterStation2().getTerminal()))
                .forEach(hvdcLine -> busesGraph.addEdge(hvdcLine.getConverterStation1().getTerminal().getBusView().getBus(), hvdcLine.getConverterStation2().getTerminal().getBusView().getBus()));
        return busesGraph;
    }

    private static Bus getBus(Object o) {
        if (o instanceof Terminal) {
            return ((Terminal) o).getBusView().getBus();
        } else if (o instanceof Boundary) {
            Boundary b = (Boundary) o;
            if (b.getConnectable() instanceof DanglingLine) {
                return ((DanglingLine) b.getConnectable()).getTerminal().getBusView().getBus();
            } else if (b.getConnectable() instanceof TieLine) {
                return ((TieLine) b.getConnectable()).getTerminal(b.getSide()).getBusView().getBus();
            }
        }
        throw new AssertionError();
    }

    private static double getLeavingFlow(Terminal terminal) {
        return terminal.isConnected() ? terminal.getP() : 0;
    }
}
