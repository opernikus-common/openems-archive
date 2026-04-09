package io.openems.edge.evcharger.alfen;

import java.util.function.Consumer;

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
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.channel.Level;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.FloatDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.FloatQuadruplewordElement;
import io.openems.edge.bridge.modbus.api.element.StringWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC16WriteRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC6WriteRegisterTask;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.common.type.Triple;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.evcharger.api.EvCharger;
import io.openems.edge.evcharger.api.EvChargerTools;
import io.openems.edge.evcharger.api.ManageableEvCharger;
import io.openems.edge.evcharger.api.data.ChargingType;
import io.openems.edge.evcharger.api.data.Iec62196Status;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "EvCharger.Alfen", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE //
})
public class EvcsAlfenImpl extends AbstractOpenemsModbusComponent implements EvcsAlfen, ManageableEvCharger, EvCharger,
		ElectricityMeter, TimedataProvider, ModbusComponent, EventHandler, OpenemsComponent {

	private Config config;
	private Triple<Consumer<Value<Integer>>, Consumer<Value<Integer>>, Consumer<Value<Integer>>> rawCurrentListener;

	@Reference
	protected ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata = null;

	@Override
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	public EvcsAlfenImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				EvCharger.ConfigChannelId.values(), //
				EvCharger.RawChannelId.values(), //
				EvCharger.ChannelId.values(), //
				ManageableEvCharger.ChannelId.values(), //
				EvcsAlfen.ChannelId.values() //
		);
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException {
		this.config = config;
		if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id())) {
			return;
		}
		this._setChargingType(ChargingType.AC);
		this._setMaxCurrent(this.config.maxCurrent());
		this._setMinCurrent(this.config.minCurrent());
		this._setIec62196Status(Iec62196Status.NO_VEHICLE);
		this.installListeners();

		this._setPhaseRotation(this.config.phaseRotation());
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			this.convertStatus();
			this.updateCommunicationState();
			break;
		}
	}

	private void convertStatus() {
		this.channel(EvcsAlfen.ChannelId.ERROR).setNextValue(Level.OK);
		Value<?> mode3StateVal = this.channel(EvcsAlfen.ChannelId.MODE_3_STATE).getNextValue();
		if (!mode3StateVal.isDefined()) {
			this._setIec62196Status(Iec62196Status.UNDEFINED);
			return;
		}
		var error = false;
		var mode3State = (String) mode3StateVal.get();
		if (mode3State.startsWith("A")) {
			this._setIec62196Status(Iec62196Status.NO_VEHICLE);
		} else if (mode3State.startsWith("B")) {
			this._setIec62196Status(Iec62196Status.VEHICLE_DETECTED);
		} else if (mode3State.startsWith("C")) {
			this._setIec62196Status(Iec62196Status.CHARGING);
		} else if (mode3State.startsWith("D")) {
			this._setIec62196Status(Iec62196Status.CHARGING_VENTILATION);
		} else if (mode3State.startsWith("E")) {
			this._setIec62196Status(Iec62196Status.DEACTIVATED);
		} else if (mode3State.startsWith("F")) {
			this._setIec62196Status(Iec62196Status.ERROR);
			error = true;
		} else {
			this._setIec62196Status(Iec62196Status.UNDEFINED);
		}
		this.channel(EvcsAlfen.ChannelId.ERROR).setNextValue(error);
	}

	private void updateCommunicationState() {
		if (this.getModbusCommunicationFailed()) {
			this.channel(EvCharger.RawChannelId.COMMUNICATION_FAILED).setNextValue(Level.FAULT);
		} else {
			this.channel(EvCharger.RawChannelId.COMMUNICATION_FAILED).setNextValue(Level.OK);
		}
	}

	private void installListeners() {
		this.channel(EvcsAlfen.ChannelId.RAW_CURRENT_AS_FLOAT_L1).onSetNextValue(val -> {
			Integer t = TypeUtils.getAsType(OpenemsType.INTEGER, val.get());
			this._setRawCurrentL1(t);
		});
		this.channel(EvcsAlfen.ChannelId.RAW_CURRENT_AS_FLOAT_L2).onSetNextValue(val -> {
			Integer t = TypeUtils.getAsType(OpenemsType.INTEGER, val.get());
			this._setRawCurrentL2(t);
		});
		this.channel(EvcsAlfen.ChannelId.RAW_CURRENT_AS_FLOAT_L3).onSetNextValue(val -> {
			Integer t = TypeUtils.getAsType(OpenemsType.INTEGER, val.get());
			this._setRawCurrentL3(t);
		});

		ElectricityMeter.calculateSumCurrentFromPhases(this);
		EvChargerTools.addActiveConsumptionEnergyListener(this);
		EvChargerTools.addRawChargePowerListener(this);

		// Readd RawCurrent-Listeners as phaseRotation may have been modified.
		EvChargerTools.removeRawCurrentListener(this, this.rawCurrentListener);
		this.rawCurrentListener = EvChargerTools.addRawCurrentListener(this, this.config.phaseRotation());
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() {

		return new ModbusProtocol(this, //

				new FC3ReadRegistersTask(320, Priority.HIGH, //
						this.m(EvcsAlfen.ChannelId.RAW_CURRENT_AS_FLOAT_L1, new FloatDoublewordElement(320), //
								ElementToChannelConverter.SCALE_FACTOR_3), //
						this.m(EvcsAlfen.ChannelId.RAW_CURRENT_AS_FLOAT_L2, new FloatDoublewordElement(322), //
								ElementToChannelConverter.SCALE_FACTOR_3), //
						this.m(EvcsAlfen.ChannelId.RAW_CURRENT_AS_FLOAT_L3, new FloatDoublewordElement(324), //
								ElementToChannelConverter.SCALE_FACTOR_3), //
						new DummyRegisterElement(326, 327), //
						this.m(EvcsAlfen.ChannelId.POWER_FACTOR_L1, new FloatDoublewordElement(328)), //
						this.m(EvcsAlfen.ChannelId.POWER_FACTOR_L2, new FloatDoublewordElement(330)), //
						this.m(EvcsAlfen.ChannelId.POWER_FACTOR_L3, new FloatDoublewordElement(332)), //
						this.m(EvcsAlfen.ChannelId.POWER_FACTOR_SUM, new FloatDoublewordElement(334)), //
						new DummyRegisterElement(336, 337), //
						this.m(EvcsAlfen.ChannelId.CHARGE_POWER_L1, new FloatDoublewordElement(338)), //
						this.m(EvcsAlfen.ChannelId.CHARGE_POWER_L2, new FloatDoublewordElement(340)), //
						this.m(EvcsAlfen.ChannelId.CHARGE_POWER_L3, new FloatDoublewordElement(342)), //
						// TODO check if float to long conversion works
						this.m(EvCharger.RawChannelId.RAW_CHARGE_POWER, new FloatDoublewordElement(344)) //
				), //
				new FC3ReadRegistersTask(374, Priority.LOW, //
						// TODO check if float to long conversion works
						this.m(EvCharger.RawChannelId.RAW_ACTIVE_CONSUMPTION_ENERGY,
								new FloatQuadruplewordElement(374))), //

				new FC3ReadRegistersTask(1201, Priority.HIGH, //
						this.m(EvcsAlfen.ChannelId.MODE_3_STATE, new StringWordElement(1201, 5)), //
						this.m(EvcsAlfen.ChannelId.ACTUAL_APPLIED_MAX_CURRENT, new FloatDoublewordElement(1206)), //
						this.m(EvcsAlfen.ChannelId.MODBUS_SLAVE_MAX_CURRENT_VALID_TIME,
								new UnsignedDoublewordElement(1208)), //
						this.m(EvcsAlfen.ChannelId.MODBUS_SLAVE_MAX_CURRENT, new FloatDoublewordElement(1210)), //
						this.m(EvcsAlfen.ChannelId.ACTIVE_LOAD_BALANCING_SAFE_CURRENT,
								new FloatDoublewordElement(1212)), //
						this.m(EvcsAlfen.ChannelId.MODBUS_SLAVE_RECEIVED_SETPOINT_ACCOUNTED_FOR,
								new UnsignedWordElement(1214)), //
						this.m(EvcsAlfen.ChannelId.CHARGE_USING_1_OR_3_PHASES, new UnsignedWordElement(1215))), //

				new FC16WriteRegistersTask(1210, //
						this.m(EvcsAlfen.ChannelId.MODBUS_SLAVE_MAX_CURRENT, new FloatDoublewordElement(1210))), //

				new FC6WriteRegisterTask(1215, //
						this.m(EvcsAlfen.ChannelId.CHARGE_USING_1_OR_3_PHASES, new UnsignedWordElement(1215))) //
		);

	}

	@Override
	public String debugLog() {
		return this.getState() + "," + this.getStatus() + "," + this.getRawChargePower();
	}

	/**
	 * Indicates if EvCharger is able of phaseLimitation.
	 * 
	 * @return true if phaseLimitation is possible, false else.
	 */
	@Override
	public boolean isNumberOfPhasesLimitable() {
		// TODO implement Alfen can handle 1 or 3 phases
		return false;
	}

	@Override
	public void applyCurrent(int current, int limitPhases) throws Exception {

		// TODO Auto-generated method stub

		// this.setModbusSlaveMaxCurrentWriteValue(current/1000);

	}

	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}

}
