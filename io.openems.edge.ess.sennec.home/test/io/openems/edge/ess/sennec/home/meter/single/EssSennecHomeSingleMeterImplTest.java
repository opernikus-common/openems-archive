package io.openems.edge.ess.sennec.home.meter.single;

import org.junit.Test;

import io.openems.common.types.MeterType;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.common.test.DummyConfigurationAdmin;
import io.openems.edge.ess.sennec.home.test.DummyEssSennecHomeV3Battery;
import io.openems.edge.meter.api.SinglePhase;

public class EssSennecHomeSingleMeterImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new MeterSennecSinglephase()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("battery", new DummyEssSennecHomeV3Battery("battery0"))
				.activate(MyConfig.create() //
						.setId("meter0") //
						.setPhase(SinglePhase.L1) //
						.setBatteryId("battery0") //
						.setType(MeterType.GRID) //
						.build()) //
				.deactivate() //
		;
	}
}
