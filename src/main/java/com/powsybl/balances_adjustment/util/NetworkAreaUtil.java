/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.util;

import com.powsybl.action.util.Scalable;
import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class NetworkAreaUtil {

    /**
     * Checks if a CGMES control area with the given ID contains several synchronous components.
     */
    public static boolean containsSeveralSynchronousComponent(Network network, String controlAreaId) {
        CgmesControlArea controlArea = network.getExtension(CgmesControlAreas.class).getCgmesControlArea(controlAreaId);
        int cc = -1;
        for (Terminal terminal : controlArea.getTerminals()) {
            Bus bus = terminal.getBusView().getBus();
            if (bus == null) {
                return true;
            }
            int terminalCc = bus.getSynchronousComponent().getNum();
            if (cc != -1 && cc != terminalCc) {
                return true;
            }
            cc = terminalCc;
        }
        for (Boundary b : controlArea.getBoundaries()) {
            Bus bus = getBusViewBus(b);
            if (bus == null) {
                return true;
            }
            int boundaryCc = bus.getSynchronousComponent().getNum();
            if (cc != -1 && cc != boundaryCc) {
                return true;
            }
            cc = boundaryCc;
        }
        return false;
    }

    private static Bus getBusViewBus(Boundary b) {
        if (b.getConnectable() instanceof DanglingLine) {
            DanglingLine dl = (DanglingLine) b.getConnectable();
            return dl.getTerminal().getBusView().getBus();
        } else if (b.getConnectable() instanceof Line) {
            Line l = (Line) b.getConnectable();
            return l.getTerminal(b.getSide()).getBusView().getBus();
        }
        throw new PowsyblException("Unexpected type of " + b.getConnectable());
    }

    /**
     * For each synchronous component of a CGMES control area with the given ID, a network area is created.
     */
    public static List<NetworkAreaFactory> createNetworkAreaFactoryBySynchronousComponent(Network network, String controlAreaId) {
        Map<Integer, Set<Terminal>> terminalsBySynchronousComponent = new HashMap<>();
        Map<Integer, Set<Boundary>> boundariesBySynchronousComponent = new HashMap<>();
        List<NetworkAreaFactory> networkAreaFactories = new ArrayList<>();

        CgmesControlArea controlArea = network.getExtension(CgmesControlAreas.class).getCgmesControlArea(controlAreaId);
        controlArea.getTerminals().forEach(t -> terminalsBySynchronousComponent.computeIfAbsent(t.getBusView().getBus().getSynchronousComponent().getNum(), k -> new HashSet<>()).add(t));
        controlArea.getBoundaries().forEach(b -> {
            Bus bus = getBusViewBus(b);
            if (bus != null) {
                boundariesBySynchronousComponent.computeIfAbsent(bus.getSynchronousComponent().getNum(), k -> new HashSet<>()).add(b);
            }
        });
        for (int i : Stream.of(terminalsBySynchronousComponent.keySet(), boundariesBySynchronousComponent.keySet()).flatMap(Set::stream).collect(Collectors.toSet())) {
            networkAreaFactories.add(new ControlAreaFactory(terminalsBySynchronousComponent.get(i), boundariesBySynchronousComponent.get(i)));
        }
        return networkAreaFactories;
    }

    /**
     * Create a ProportionalScalable containing all the conform loads contained in a given network area with an associated percentage proportional to their p0.
     * If no conform load is contained in the given network area, the ProportionalScalable contains all the loads contained in the given network area.
     * If no load is contained in the given network area, an exception is thrown.
     * If all selected load (conform or not) have a null p0, an exception is thrown.
     */
    public static Scalable createConformLoadScalable(NetworkArea area) {
        List<Load> loads = area.getContainedBusViewBuses().stream()
                .flatMap(Bus::getConnectedTerminalStream)
                .filter(t -> t.getConnectable() instanceof Load)
                .map(t -> (Load) t.getConnectable())
                .filter(load -> load.getP0() >= 0)
                .filter(load -> load.getExtension(LoadDetail.class) != null && load.getExtension(LoadDetail.class).getVariableActivePower() != 0)
                .collect(Collectors.toList());
        if (loads.isEmpty()) {
            loads = area.getContainedBusViewBuses().stream()
                    .flatMap(Bus::getConnectedTerminalStream)
                    .filter(t -> t.getConnectable() instanceof Load)
                    .map(t -> (Load) t.getConnectable())
                    .filter(load -> load.getP0() >= 0)
                    .collect(Collectors.toList());
            if (loads.isEmpty()) {
                throw new PowsyblException("There is no load in this area");
            }
        }
        float totalP0 = (float) loads.stream().mapToDouble(Load::getP0).sum();
        if (totalP0 == 0.0) {
            throw new PowsyblException("All loads' active power flows is null"); // this case should never happen
        }
        List<Float> percentages = loads.stream().map(load -> (float) (100f * load.getP0() / totalP0)).collect(Collectors.toList());
        return Scalable.proportional(percentages, loads.stream().map(inj -> (Scalable) Scalable.onLoad(inj.getId())).collect(Collectors.toList()));
    }

    private NetworkAreaUtil() {
    }
}
