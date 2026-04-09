package io.openems.edge.evcharger.alfen;

import org.junit.Test;

import io.openems.edge.bridge.modbus.test.DummyModbusBridge;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.common.test.DummyConfigurationAdmin;
import io.openems.edge.evcharger.api.data.PhaseRotation;
import io.openems.edge.timedata.test.DummyTimedata;

public class EvcsAlfenImplTest {

	private static final String EVCS_ID = "evcs0";
	private static final String MODBUS_ID = "modbus0";
	private static final int MIN_CURRENT = 6000;
	private static final int MAX_CURRENT = 32000;

	@Test
	public void test() throws Exception {
		new ComponentTest(new EvcsAlfenImpl()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("setModbus", new DummyModbusBridge(MODBUS_ID)) //
				.addReference("timedata", new DummyTimedata("timedata0")) //
				.activate(MyConfig.create() //
						.setId(EVCS_ID) //
						.setModbusId(MODBUS_ID) //
						.setModbusUnitId(1) //
						.setMinCurrent(MIN_CURRENT) //
						.setMaxCurrent(MAX_CURRENT) //
						.setPhaseRotation(PhaseRotation.L1_L2_L3) //
						.build()) //
				.next(new TestCase());
	}

}
