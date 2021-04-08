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

    public static Scalable createConformLoadScalable(NetworkArea area) {
        List<Load> loads = area.getContainedBusViewBuses().stream()
                .flatMap(Bus::getConnectedTerminalStream)
                .filter(t -> t.getConnectable() instanceof Load)
                .map(t -> (Load) t.getConnectable())
                .filter(load -> load.getP0() >= 0)
                .filter(load -> load.getTerminal().getBusView().getBus().isInMainConnectedComponent())
                .filter(load -> load.getExtension(LoadDetail.class) != null && load.getExtension(LoadDetail.class).getFixedActivePower() == 0 && load.getExtension(LoadDetail.class).getFixedReactivePower() == 0)
                .sorted((l1, l2) -> {
                    if (l1.getP0() == l2.getP0()) {
                        return 0;
                    } else if (l1.getP0() > l2.getP0()) {
                        return -1;
                    }
                    return 1;
                }).collect(Collectors.toList());
        if (loads.isEmpty()) {
            return null;
        }
        float totalP0 = (float) loads.stream().mapToDouble(Load::getP0).sum();
        List<Float> percentages = loads.stream().map(load -> (float) (100f * load.getP0() / totalP0)).collect(Collectors.toList());
        return Scalable.proportional(percentages, loads.stream().map(inj -> (Scalable) Scalable.onLoad(inj.getId())).collect(Collectors.toList()));
    }

    private NetworkAreaUtil() {
    }
}