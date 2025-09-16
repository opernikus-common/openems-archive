package io.openems.edge.ess.solarwatt.myreserve;

import org.junit.Test;

import io.openems.edge.common.sum.GridMode;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.ComponentTest;

public class SolarWattEssImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new EssSolarwattMyReserveImpl()) //
				.activate(MyConfig.create() //
						.setId("charger0") //
						.setIpAddress("127.0.0.1") //
						.setCapacity(50_000) //
						.setPort(502) //
						.setGridMode(GridMode.ON_GRID) //
						.setDebugRest(false) //
						.setInterval(10) //
						.build()) //
				.next(new TestCase()) //
				.deactivate();
	}

}
