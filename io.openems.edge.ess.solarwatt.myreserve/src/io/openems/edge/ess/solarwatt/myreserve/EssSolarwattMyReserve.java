package io.openems.edge.ess.solarwatt.myreserve;

import io.openems.common.channel.Level;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.StateChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.ess.api.AsymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;

public interface EssSolarwattMyReserve extends SymmetricEss, AsymmetricEss, OpenemsComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

	COMMUNICATION_FAILED(Doc.of(Level.FAULT)),

	/**
	 * 0 = ok 1 = other 2 = not available.
	 */
	BATTERY_SYSTEM_STATE(Doc.of(OpenemsType.INTEGER)), //

	/**
	 * KACO 0 = ok 1 = other 2 = not available.
	 */
	INVERTER_SYSTEM_STATE(Doc.of(OpenemsType.INTEGER)), //

	BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER) //
		.unit(Unit.MILLIVOLT)), //

	BATTERY_CURRENT(Doc.of(OpenemsType.INTEGER) //
		.unit(Unit.MILLIAMPERE)), //

	SOH(Doc.of(OpenemsType.INTEGER) //
		.unit(Unit.PERCENT)), //

	BATTERY_CHARGING(Doc.of(OpenemsType.BOOLEAN)), //

	/*
	 * Range: negative values for Consumption (power that is 'leaving the system',
	 * e.g. feed-to-grid); positive for Production (power that is 'entering the
	 * system')
	 */
	ACTIVE_POWER_GRID(Doc.of(OpenemsType.INTEGER) //
		.unit(Unit.WATT)), //
	ACTIVE_PRODUCTION_ENERGY_GRID(Doc.of(OpenemsType.LONG) //
		.unit(Unit.WATT_HOURS)),
	ACTIVE_CONSUMPTION_ENERGY_GRID(Doc.of(OpenemsType.LONG) //
		.unit(Unit.WATT_HOURS)),

	ACTIVE_POWER_HOUSE(Doc.of(OpenemsType.INTEGER) //
		.unit(Unit.WATT)), //
	ACTIVE_PRODUCTION_ENERGY_HOUSE(Doc.of(OpenemsType.LONG) //
		.unit(Unit.WATT_HOURS)),
	ACTIVE_CONSUMPTION_ENERGY_HOUSE(Doc.of(OpenemsType.LONG) //
		.unit(Unit.WATT_HOURS)),

	ACTIVE_POWER_INVERTER(Doc.of(OpenemsType.INTEGER) //
		.unit(Unit.WATT)), //
	ACTIVE_PRODUCTION_ENERGY_INVERTER(Doc.of(OpenemsType.LONG) //
		.unit(Unit.WATT_HOURS)),
	ACTIVE_CONSUMPTION_ENERGY_INVERTER(Doc.of(OpenemsType.LONG) //
		.unit(Unit.WATT_HOURS)),

	;

	private final Doc doc;

	private ChannelId(Doc doc) {
	    this.doc = doc;
	}

	@Override
	public Doc doc() {
	    return this.doc;
	}
    }

    public default StateChannel getCommunicationFailedChannel() {
	return this.channel(ChannelId.COMMUNICATION_FAILED);
    }

    public default Value<Boolean> getCommunicationFailed() {
	return this.getCommunicationFailedChannel().value();
    }

    /**
     * set communication failed.
     * @param value communication failed value
     */
    public default void _setCommunicationFailed(boolean value) {
	this.getCommunicationFailedChannel().setNextValue(value);
    }

    public default Channel<Integer> getBatterySystemStateChannel() {
	return this.channel(ChannelId.BATTERY_SYSTEM_STATE);
    }

    public default Value<Integer> getBatterySystemState() {
	return this.getBatterySystemStateChannel().value();
    }

    public default void setBatterySystemState(Integer val) {
	this.getBatterySystemStateChannel().setNextValue(val);
    }

    public default Channel<Integer> getInverterSystemStateChannel() {
	return this.channel(ChannelId.INVERTER_SYSTEM_STATE);
    }

    public default Value<Integer> getInverterSystemState() {
	return this.getInverterSystemStateChannel().value();
    }

    public default void setInverterSystemState(Integer val) {
	this.getInverterSystemStateChannel().setNextValue(val);
    }

    public default Channel<Integer> getBatteryVoltageChannel() {
	return this.channel(ChannelId.BATTERY_VOLTAGE);
    }

    public default Value<Integer> getBatteryVoltage() {
	return this.getBatteryVoltageChannel().value();
    }

    public default void setBatteryVoltage(Integer val) {
	this.getBatteryVoltageChannel().setNextValue(val);
    }

    public default Channel<Integer> getBatteryCurrentChannel() {
	return this.channel(ChannelId.BATTERY_CURRENT);
    }

    public default Value<Integer> getBatteryCurrent() {
	return this.getBatteryCurrentChannel().value();
    }

    public default void setBatteryCurrent(Integer val) {
	this.getBatteryCurrentChannel().setNextValue(val);
    }

    public default Channel<Integer> getSohChannel() {
	return this.channel(ChannelId.SOH);
    }

    public default Value<Integer> getSoh() {
	return this.getSohChannel().value();
    }

    public default void setSoh(Integer val) {
	this.getSohChannel().setNextValue(val);
    }

    public default Channel<Boolean> getBatteryChargingChannel() {
	return this.channel(ChannelId.BATTERY_CHARGING);
    }

    public default Value<Boolean> getBatteryCharging() {
	return this.getBatteryChargingChannel().value();
    }

    public default void setBatteryCharging(Boolean val) {
	this.getBatteryChargingChannel().setNextValue(val);
    }

    public default Channel<Integer> getActivePowerGridChannel() {
	return this.channel(ChannelId.ACTIVE_POWER_GRID);
    }

    public default Value<Integer> getActivePowerGrid() {
	return this.getActivePowerGridChannel().value();
    }

    public default void setActivePowerGrid(Integer val) {
	this.getActivePowerGridChannel().setNextValue(val);
    }

    public default Channel<Long> getActiveProductionEnergyGridChannel() {
	return this.channel(ChannelId.ACTIVE_PRODUCTION_ENERGY_GRID);
    }

    public default Value<Long> getActiveProductionEnergyGrid() {
	return this.getActiveProductionEnergyGridChannel().value();
    }

    public default void setActiveProductionEnergyGrid(Long val) {
	this.getActiveProductionEnergyGridChannel().setNextValue(val);
    }

    public default Channel<Long> getActiveConsumptionEnergyGridChannel() {
	return this.channel(ChannelId.ACTIVE_CONSUMPTION_ENERGY_GRID);
    }

    public default Value<Long> getActiveConsumptionEnergyGrid() {
	return this.getActiveConsumptionEnergyGridChannel().value();
    }

    public default void setActiveConsumptionEnergyGrid(Long val) {
	this.getActiveConsumptionEnergyGridChannel().setNextValue(val);
    }

    public default Channel<Integer> getActivePowerHouseChannel() {
	return this.channel(ChannelId.ACTIVE_POWER_HOUSE);
    }

    public default Value<Integer> getActivePowerHouse() {
	return this.getActivePowerHouseChannel().value();
    }

    public default void setActivePowerHouse(Integer val) {
	this.getActivePowerHouseChannel().setNextValue(val);
    }

    public default Channel<Long> getActiveProductionEnergyHouseChannel() {
	return this.channel(ChannelId.ACTIVE_PRODUCTION_ENERGY_HOUSE);
    }

    public default Value<Long> getActiveProductionEnergyHouse() {
	return this.getActiveProductionEnergyHouseChannel().value();
    }

    public default void setActiveProductionEnergyHouse(Long val) {
	this.getActiveProductionEnergyHouseChannel().setNextValue(val);
    }

    public default Channel<Long> getActiveConsumptionEnergyHouseChannel() {
	return this.channel(ChannelId.ACTIVE_CONSUMPTION_ENERGY_HOUSE);
    }

    public default Value<Long> getActiveConsumptionEnergyHouse() {
	return this.getActiveConsumptionEnergyHouseChannel().value();
    }

    public default void setActiveConsumptionEnergyHouse(Long val) {
	this.getActiveConsumptionEnergyHouseChannel().setNextValue(val);
    }

    public default Channel<Integer> getActivePowerInverterChannel() {
	return this.channel(ChannelId.ACTIVE_POWER_INVERTER);
    }

    public default Value<Integer> getActivePowerInverter() {
	return this.getActivePowerInverterChannel().value();
    }

    public default void setActivePowerInverter(Integer val) {
	this.getActivePowerInverterChannel().setNextValue(val);
    }

    public default Channel<Long> getActiveProductionEnergyInverterChannel() {
	return this.channel(ChannelId.ACTIVE_PRODUCTION_ENERGY_INVERTER);
    }

    public default Value<Long> getActiveProductionEnergyInverter() {
	return this.getActiveProductionEnergyInverterChannel().value();
    }

    public default void setActiveProductionEnergyInverter(Long val) {
	this.getActiveProductionEnergyInverterChannel().setNextValue(val);
    }

    public default Channel<Long> getActiveConsumptionEnergyInverterChannel() {
	return this.channel(ChannelId.ACTIVE_CONSUMPTION_ENERGY_INVERTER);
    }

    public default Value<Long> getActiveConsumptionEnergyInverter() {
	return this.getActiveConsumptionEnergyInverterChannel().value();
    }

    public default void setActiveConsumptionEnergyInverter(Long val) {
	this.getActiveConsumptionEnergyInverterChannel().setNextValue(val);
    }

}
