package io.openems.edge.ess.solarwatt.myreserve.meter;

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
import io.openems.edge.ess.solarwatt.myreserve.EssSolarwattMyReserve;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.common.types.MeterType;
import io.openems.edge.meter.api.SinglePhase;
import io.openems.edge.meter.api.SinglePhaseMeter;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Solarwatt.MyReserve.VirtualMeter.Singlephase", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class MeterSolarwatt extends AbstractOpenemsComponent
		implements SinglePhaseMeter, ElectricityMeter, OpenemsComponent {

	protected Config config = null;
	protected MeterType meterType = MeterType.PRODUCTION;

	private final Logger log = LoggerFactory.getLogger(MeterSolarwatt.class);

	// TODO solarwatt supports threephase

	@Reference
	private ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private EssSolarwattMyReserve battery;

	public MeterSolarwatt() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				SinglePhaseMeter.ChannelId.values() //
		);

		ElectricityMeter.calculateSumActivePowerFromPhases(this);
		ElectricityMeter.calculateSumReactivePowerFromPhases(this);

	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException, OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		this.meterType = this.config.type();

		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "battery", config.battery_id())) {
			return;
		}

		this.logInfo(this.log, "Activate virtual Solarwatt Meter of Type " + this.meterType);
		// this.initializeSymmetricChannelHandling();
		this.mapBatteryPower();

		// TODO these channels must be add :
		// FREQUENCY(Doc.of(OpenemsType.INTEGER) //
		// VOLTAGE(Doc.of(OpenemsType.INTEGER) //
		// CURRENT(Doc.of(OpenemsType.INTEGER) //
		// ACTIVE_POWER_L2(Doc.of(OpenemsType.INTEGER) //
		// ACTIVE_POWER_L3(Doc.of(OpenemsType.INTEGER) //
		// REACTIVE_POWER_L2(Doc.of(OpenemsType.INTEGER) //
		// REACTIVE_POWER_L3(Doc.of(OpenemsType.INTEGER) //
		// VOLTAGE_L2(Doc.of(OpenemsType.INTEGER) //
		// VOLTAGE_L3(Doc.of(OpenemsType.INTEGER) //
		// CURRENT_L2(Doc.of(OpenemsType.INTEGER) //
		// CURRENT_L3(Doc.of(OpenemsType.INTEGER) //

	}

	@Override
	@Deactivate
	protected void deactivate() {
		this.logInfo(this.log, "deactivation");
		super.deactivate();
	}

	private void mapBatteryPower() {

		switch (this.getMeterType()) {
		case GRID:
			this.mapBatteryPowerByInChannel(this.battery.getActivePowerGridChannel());

			// grid - consumption <-> feed to grid
			// production <-> buy from grid
			// mapBatteryEnergyByInChannel(
			// this.battery.getActiveProductionEnergyGridChannel(),
			// this.battery.getActiveConsumptionEnergyGridChannel()
			// );
			// Solarwatt - OpenEMS is exchanged
			this.mapBatteryEnergyByInChannel(this.battery.getActiveConsumptionEnergyGridChannel(),
					this.battery.getActiveProductionEnergyGridChannel());
			break;
		case PRODUCTION:
			this.mapBatteryPowerByInChannel(this.battery.getActivePowerInverterChannel());
			this.mapBatteryEnergyByInChannel(this.battery.getActiveProductionEnergyInverterChannel(),
					this.battery.getActiveConsumptionEnergyInverterChannel());
			break;
		case PRODUCTION_AND_CONSUMPTION:
			// TODO
			// mapBatteryPowerByInChannel(this.battery.getActivePowerInverterChannel());
			this.logError(this.log, "No such virtual Solarwatt meter for PRODUCTION_AND_CONSUMPTION");
			break;
		case MANAGED_CONSUMPTION_METERED:
		case CONSUMPTION_METERED:
			this.mapBatteryPowerByInChannel(this.battery.getActivePowerHouseChannel());
			this.mapBatteryEnergyByInChannel(this.battery.getActiveProductionEnergyHouseChannel(),
					this.battery.getActiveConsumptionEnergyHouseChannel());
			break;
		case CONSUMPTION_NOT_METERED:
			// TODO
			// mapBatteryPowerConsumptionNotMetered();
			this.logError(this.log, "No such virtual Solarwatt meter for CONSUMPTION_NOT_METERED");
			break;
		}
	}

	private void mapBatteryPowerByInChannel(Channel<Integer> channel) {
		channel.onUpdate(newValue -> {
			switch (this.config.phase()) {
			case L1:
				this.updateOutChannel(this.getActivePowerL1Channel(), this.getReactivePowerL1Channel(), newValue);
				break;
			case L2:
				this.updateOutChannel(this.getActivePowerL2Channel(), this.getReactivePowerL2Channel(), newValue);
				break;
			case L3:
				this.updateOutChannel(this.getActivePowerL3Channel(), this.getReactivePowerL2Channel(), newValue);
				break;
			}
		});
	}

	private void mapBatteryEnergyByInChannel(Channel<Long> prodEnergyChannel, Channel<Long> consEnergyChannel) {

		prodEnergyChannel.onUpdate(newValue -> {
			this.getActiveProductionEnergyChannel().setNextValue(newValue);
		});

		consEnergyChannel.onUpdate(newValue -> {
			this.getActiveConsumptionEnergyChannel().setNextValue(newValue);
		});

	}

	protected void updateOutChannel(Channel<Integer> activeChannel, Channel<Integer> reactiveChannel,
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

	@Override
	public SinglePhase getPhase() {
		return this.config.phase();
	}

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
