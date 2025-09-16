package io.openems.edge.ess.sennec.home;

import org.junit.Test;

import io.openems.edge.common.sum.GridMode;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.common.test.DummyConfigurationAdmin;
import io.openems.edge.ess.api.SinglePhase;

public class EssSennecHomeV3ImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new EssSennecHomeV3BatteryImpl()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.activate(MyConfig.create() //
						.setId("ess0") //
						.setPhase(SinglePhase.L1) //
						.setGridMode(GridMode.ON_GRID) //
						.setCapacity(50_000) //
						.setCycleTime(10) //
						.build()) //
				.deactivate() //
		;
	}
}
