package io.openems.edge.ess.sennec.home.meter.three;

import org.junit.Test;

import io.openems.common.types.MeterType;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.common.test.DummyConfigurationAdmin;
import io.openems.edge.ess.sennec.home.test.DummyEssSennecHomeV3Battery;

public class EssSennecHomeSingleMeterImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new MeterSennecThreephase()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("battery", new DummyEssSennecHomeV3Battery("battery0"))
				.activate(MyConfig.create() //
						.setId("meter0") //
						.setBatteryId("battery0") //
						.setType(MeterType.GRID) //
						.build()) //
				.deactivate() //
		;
	}
}
