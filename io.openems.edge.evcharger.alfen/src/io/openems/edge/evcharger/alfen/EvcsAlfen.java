package io.openems.edge.evcharger.alfen;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Level;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.evcharger.api.EvCharger;
import io.openems.edge.evcharger.api.ManageableEvCharger;
import io.openems.edge.meter.api.ElectricityMeter;

public interface EvcsAlfen extends ManageableEvCharger, EvCharger, ElectricityMeter, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

		RAW_CURRENT_AS_FLOAT_L1(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Current as metered on Input Phase L1")), //
		RAW_CURRENT_AS_FLOAT_L2(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Current as metered on Input Phase L2")), //
		RAW_CURRENT_AS_FLOAT_L3(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.MILLIAMPERE) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Current as metered on Input Phase L3")), //
		POWER_FACTOR_L1(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.NONE) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Power factor L1")), //
		POWER_FACTOR_L2(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.NONE) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Power factor L2")), //
		POWER_FACTOR_L3(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.NONE) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Power factor L3")), //
		POWER_FACTOR_SUM(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.NONE) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Power factor sum")), //
		CHARGE_POWER_L1(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Charge Power L1")), //
		CHARGE_POWER_L2(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Charge Power L2")), //
		CHARGE_POWER_L3(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.WATT) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Charge Power L3")), //
		/**
		 * See Modbus specification for details on the Mode 3 state.
		 */
		MODE_3_STATE(Doc.of(OpenemsType.STRING) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Mode 3 state")), //
		ACTUAL_APPLIED_MAX_CURRENT(Doc.of(OpenemsType.FLOAT) //
				.persistencePriority(PersistencePriority.HIGH) //
				.text("Actual applied max current")), //
		MODBUS_SLAVE_MAX_CURRENT_VALID_TIME(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.SECONDS) //
				.persistencePriority(PersistencePriority.LOW) //
				.text("Remaining time before fallback to safe current")), //
		MODBUS_SLAVE_MAX_CURRENT(Doc.of(OpenemsType.FLOAT) //
				.accessMode(AccessMode.READ_WRITE) //
				.unit(Unit.AMPERE) //
				.persistencePriority(PersistencePriority.HIGH) //
				.text("Modbus slave max current")), //
		ACTIVE_LOAD_BALANCING_SAFE_CURRENT(Doc.of(OpenemsType.FLOAT) //
				.unit(Unit.AMPERE) //
				.persistencePriority(PersistencePriority.HIGH) //
				.text("Active load balancing safe current")), //
		MODBUS_SLAVE_RECEIVED_SETPOINT_ACCOUNTED_FOR(Doc.of(OpenemsType.BOOLEAN) //
				.persistencePriority(PersistencePriority.HIGH) //
				.text("Modbus slave received setpoint accounted for")), //
		CHARGE_USING_1_OR_3_PHASES(Doc.of(OpenemsType.INTEGER) //
				.accessMode(AccessMode.READ_WRITE) //
				.unit(Unit.NONE) //
				.persistencePriority(PersistencePriority.HIGH) //
				.text("Charge using 1 or 3 phases")), //
		ERROR(Doc.of(Level.FAULT) //
				.persistencePriority(PersistencePriority.HIGH) //
				.text("Alfen chargepoint indicates an internal error.") //
		);

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	public default WriteChannel<Float> getModbusSlaveMaxCurrentChannel() {
		return this.channel(ChannelId.MODBUS_SLAVE_MAX_CURRENT);
	}

	public default void setModbusSlaveMaxCurrent(Float value) {
		this.getModbusSlaveMaxCurrentChannel().setNextValue(value);
	}

	public default void setModbusSlaveMaxCurrentWriteValue(Float value) throws OpenemsNamedException {
		this.getModbusSlaveMaxCurrentChannel().setNextWriteValue(value);
	}

	public default WriteChannel<Integer> getChargeUsing1or3PhasesChannel() {
		return this.channel(ChannelId.CHARGE_USING_1_OR_3_PHASES);
	}

	public default void setChargeUsing1or3Phases(Integer value) throws OpenemsNamedException {
		this.getChargeUsing1or3PhasesChannel().setNextWriteValue(value);
	}

}
