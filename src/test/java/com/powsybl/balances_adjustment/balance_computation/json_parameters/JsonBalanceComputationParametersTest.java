/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.balances_adjustment.balance_computation.json_parameters;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.balances_adjustment.balance_computation.BalanceComputationParameters;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.loadflow.LoadFlowParameters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;

import static org.junit.Assert.*;

/**
 * @author Mohamed Ben Rejeb {@literal <mohamed.benrejeb at rte-france.com>}
 */
public class JsonBalanceComputationParametersTest {
    InMemoryPlatformConfig platformConfig;
    FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void closeFileSystem() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testNoConfig() {
        BalanceComputationParameters parameters = new BalanceComputationParameters();
        BalanceComputationParameters.load();
        assertEquals(parameters.getMaxNumberIterations(), BalanceComputationParameters.DEFAULT_MAX_NUMBER_ITERATIONS);
        assertEquals(parameters.getThresholdNetPosition(), BalanceComputationParameters.DEFAULT_THRESHOLD_NET_POSITION, .01);
    }

    @Test
    public void readError() {
        try {
            JsonBalanceComputationParameters.read(getClass().getResourceAsStream("/balanceComputationParametersError.json"));
            Assert.fail();
        } catch (AssertionError ignored) {
        }
    }

    @Test
    public void readSuccessful() {
        BalanceComputationParameters parameters = JsonBalanceComputationParameters.read(getClass().getResourceAsStream("/balanceComputationParameters.json"));
        assertEquals(11, parameters.getMaxNumberIterations());
        assertEquals(2, parameters.getThresholdNetPosition(), .01);
        LoadFlowParameters actualLoadflowParams =  parameters.getLoadFlowParameters();
        assertEquals("DC_VALUES", actualLoadflowParams.getVoltageInitMode().toString());
        assertTrue(actualLoadflowParams.isTransformerVoltageControlOn());
        assertTrue(actualLoadflowParams.isPhaseShifterRegulationOn());
        assertFalse(actualLoadflowParams.isNoGeneratorReactiveLimits());
        assertFalse(actualLoadflowParams.isSpecificCompatibility());

    }
}
