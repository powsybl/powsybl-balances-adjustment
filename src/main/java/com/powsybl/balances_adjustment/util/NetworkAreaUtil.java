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
import com.powsybl.iidm.network.*;

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
            int terminalCc = terminal.getBusView().getBus().getSynchronousComponent().getNum();
            if (cc != -1 && cc != terminalCc) {
                return true;
            }
            cc = terminalCc;
        }
        for (Boundary b : controlArea.getBoundaries()) {
            int boundaryCc;
            if (b.getConnectable() instanceof DanglingLine) {
                DanglingLine dl = (DanglingLine) b.getConnectable();
                boundaryCc = dl.getTerminal().getBusView().getBus().getSynchronousComponent().getNum();
                if (cc != -1 && cc != boundaryCc) {
                    return true;
                }
                cc = boundaryCc;
            } else if (b.getConnectable() instanceof TieLine) {
                TieLine tl = (TieLine) b.getConnectable();
                boundaryCc = tl.getTerminal(b.getSide()).getBusView().getBus().getSynchronousComponent().getNum();
                if (cc != -1 && cc != boundaryCc) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<NetworkAreaFactory> createNetworkAreaFactoryBySynchronousComponent(Network network, String controlAreaId) {
        Map<Integer, Set<Terminal>> terminalsBySynchronousComponent = new HashMap<>();
        Map<Integer, Set<Boundary>> boundariesBySynchronousComponent = new HashMap<>();
        List<NetworkAreaFactory> networkAreaFactories = new ArrayList<>();

        CgmesControlArea controlArea = network.getExtension(CgmesControlAreas.class).getCgmesControlArea(controlAreaId);
        controlArea.getTerminals().forEach(t -> terminalsBySynchronousComponent.computeIfAbsent(t.getBusView().getBus().getSynchronousComponent().getNum(), k -> new HashSet<>()).add(t));
        controlArea.getBoundaries().forEach(b -> {
            if (b.getConnectable() instanceof DanglingLine) {
                DanglingLine dl = (DanglingLine) b.getConnectable();
                boundariesBySynchronousComponent.computeIfAbsent(dl.getTerminal().getBusView().getBus().getSynchronousComponent().getNum(), k -> new HashSet<>()).add(b);
            } else if (b.getConnectable() instanceof TieLine) {
                TieLine tl = (TieLine) b.getConnectable();
                boundariesBySynchronousComponent.computeIfAbsent(tl.getTerminal(b.getSide()).getBusView().getBus().getSynchronousComponent().getNum(), k -> new HashSet<>()).add(b);
            }
        });
        for (int i : Stream.of(terminalsBySynchronousComponent.keySet(), boundariesBySynchronousComponent.keySet()).flatMap(Set::stream).collect(Collectors.toSet())) {
            networkAreaFactories.add(new ControlAreaFactory(terminalsBySynchronousComponent.get(i), boundariesBySynchronousComponent.get(i)));
        }
        return networkAreaFactories;
    }

    public static List<Scalable> createLoadScalables(NetworkArea area) {
        List<Scalable> scalables = new ArrayList<>();
        area.getContainedBusViewBuses().stream()
                .flatMap(Bus::getConnectedTerminalStream)
                .filter(t -> t.getConnectable() instanceof Load) // TODO: should also filter if they are conform loads or not
                .map(t -> (Load) t.getConnectable())
                .forEach(load -> scalables.add(Scalable.onLoad(load.getId())));
        return scalables;
    }

    private NetworkAreaUtil() {
    }
}
