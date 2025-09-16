package io.openems.edge.ess.sennec.home.meter.three;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.ess.sennec.home.EssSennecHomeV3Battery;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.common.types.MeterType;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Sennec.HomeV3.Virtual.Meter.Threephase", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class MeterSennecThreephase extends AbstractOpenemsComponent implements ElectricityMeter, OpenemsComponent {

	protected Config config = null;
	protected MeterType meterType = MeterType.PRODUCTION;

	private final Logger log = LoggerFactory.getLogger(MeterSennecThreephase.class);

	@Reference
	private ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private EssSennecHomeV3Battery battery;

	public MeterSennecThreephase() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values() //
		);

		ElectricityMeter.calculateSumActivePowerFromPhases(this);
		ElectricityMeter.calculateSumReactivePowerFromPhases(this);

	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException, OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		this.meterType = this.config.type();
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "battery", config.battery_id())
				|| !this.config.enabled()) {
			return;
		}
		this.logInfo(this.log, "Activate Sennec virtual Meter of Type " + this.meterType);
		// this.initializeSymmetricChannelHandling();
		this.mapBatteryPower();

	}

	@Override
	@Deactivate
	protected void deactivate() {
		this.logInfo(this.log, "deactivation");
		super.deactivate();
	}

	private void mapBatteryPower() throws OpenemsException {

		switch (this.getMeterType()) {
		case GRID:
			this.mapBatteryChannelForMeterTypeGrid();
			break;
		case PRODUCTION:
		case PRODUCTION_AND_CONSUMPTION:
			this.logError(this.log, "No such sennec virtual meter for PRODUCTION, PRODUCTION_AND_CONSUMPTION");
			throw new OpenemsException(
					"Virtual Sennec Meter does not provide meter type PRODUCTION, PRODUCTION_AND_CONSUMPTION");

		case MANAGED_CONSUMPTION_METERED:
		case CONSUMPTION_METERED:
			this.logError(this.log, "No such sennec virtual meter for CONSUMPTION_NOT_METERED");
			throw new OpenemsException("Virtual Sennec Meter does not provide meter type CONSUMPTION_METERED");

		case CONSUMPTION_NOT_METERED:
			this.logError(this.log, "No such sennec virtual meter for CONSUMPTION_NOT_METERED");
			throw new OpenemsException("Virtual Sennec Meter does not provide meter type CONSUMPTION_NOT_METERED");
		}
	}

	private void mapBatteryChannelForMeterTypeGrid() {

		// phase l1
		this.battery.getGridActivePowerL1Channel().onChange((oldValue, newValue) -> {
			this.updatePowerChannel(this.getActivePowerL1Channel(), this.getReactivePowerL1Channel(), newValue);
		});
		this.battery.getGridVoltageL1Channel().onChange((oldValue, newValue) -> {
			this.getVoltageL1Channel().setNextValue(newValue);
		});

		// phase l2
		this.battery.getGridCurrentL1Channel().onChange((oldValue, newValue) -> {
			this.getCurrentL1Channel().setNextValue(newValue);
		});
		this.battery.getGridActivePowerL2Channel().onChange((oldValue, newValue) -> {
			this.updatePowerChannel(this.getActivePowerL2Channel(), this.getReactivePowerL2Channel(), newValue);
		});
		this.battery.getGridVoltageL2Channel().onChange((oldValue, newValue) -> {
			this.getVoltageL2Channel().setNextValue(newValue);
		});
		this.battery.getGridCurrentL2Channel().onChange((oldValue, newValue) -> {
			this.getCurrentL2Channel().setNextValue(newValue);
		});

		// phase l3
		this.battery.getGridActivePowerL3Channel().onChange((oldValue, newValue) -> {
			this.updatePowerChannel(this.getActivePowerL3Channel(), this.getReactivePowerL3Channel(), newValue);
		});
		this.battery.getGridVoltageL3Channel().onChange((oldValue, newValue) -> {
			this.getVoltageL3Channel().setNextValue(newValue);
		});
		this.battery.getGridCurrentL3Channel().onChange((oldValue, newValue) -> {
			this.getCurrentL3Channel().setNextValue(newValue);
		});

	}

	protected void updatePowerChannel(Channel<Integer> activeChannel, Channel<Integer> reactiveChannel,
			Value<Integer> newValue) {
		var valueOpt = newValue.asOptional();
		if (!valueOpt.isPresent()) {
			activeChannel.setNextValue(null);
			reactiveChannel.setNextValue(null);
			return;
		}
		int activePower = TypeUtils.getAsType(OpenemsType.INTEGER, newValue);
		activeChannel.setNextValue(activePower);
		reactiveChannel.setNextValue(activePower);
	}

	// support symmetricMeter interface

	// private void initializeSymmetricChannelHandling() {
	//
	// // map activePowerL1|L2|L3 to activePower
	// // SinglePhaseMeter.initializeCopyPhaseChannel(this, this.getPhase());
	// // map reactivePowerL1|L2|L3 to reactivePower
	// // initializeCopyPhaseChannelReactivePower();
	// }

	// /**
	// * Initializes Channel listeners. Copies the Active-Power Phase-Channel value
	// to
	// * Active-Power Channel.
	// *
	// * @param meter the AsymmetricMeter
	// * @param phase the Phase
	// */
	// private void initializeCopyPhaseChannelReactivePower() {
	// switch (this.getPhase()) {
	// case L1:
	// this.getReactivePowerL1Channel().onSetNextValue(value -> {
	// this._setReactivePower(value.get());
	// });
	// break;
	// case L2:
	// this.getReactivePowerL2Channel().onSetNextValue(value -> {
	// this._setReactivePower(value.get());
	// });
	// break;
	// case L3:
	// this.getReactivePowerL3Channel().onSetNextValue(value -> {
	// this._setReactivePower(value.get());
	// });
	// break;
	// }
	// }

	@Override
	public MeterType getMeterType() {
		return this.meterType;
	}

	@Override
	public String debugLog() {
		int power = this.getActivePowerChannel().value().asOptional().orElse(0);
		return power + " W," + this.getMeterType().toString();
	}

}
