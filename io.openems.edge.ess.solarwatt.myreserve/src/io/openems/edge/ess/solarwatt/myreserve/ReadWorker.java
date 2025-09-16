package io.openems.edge.ess.solarwatt.myreserve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.utils.JsonUtils;
import io.openems.common.worker.AbstractWorker;

public class ReadWorker extends AbstractWorker {
	private final Logger log = LoggerFactory.getLogger(ReadWorker.class);

	private static final String URL_REQ_PATH = "/rest/kiwigrid/wizard/devices";

	// battery
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_BAT = "com.kiwigrid.devices.batteryconverter.BatteryConverter";
	private static final String BAT_CURRENT_IN = "CurrentBatteryIn";
	private static final String BAT_CURRENT_OUT = "CurrentBatteryOut";
	private static final String BAT_POWER_IN = "PowerACIn";
	private static final String BAT_POWER_OUT = "PowerACOut";
	private static final String BAT_WORK_IN = "WorkACIn";
	private static final String BAT_WORK_OUT = "WorkACOut";
	private static final String BAT_SOH = "StateOfHealth";
	private static final String BAT_SOC = "StateOfCharge";
	private static final String BAT_STATE = "StateDevice";
	private static final String BAT_VOLT_CELL_MIN = "VoltageBatteryCellMin";
	private static final String BAT_VOLT_CELL_MAX = "VoltageBatteryCellMax";
	private static final String BAT_VOLT_CELL_STRING = "VoltageBatteryString";
	private static final String BAT_VOLT_CELL_MEAN = "VoltageBatteryCellMean";
	private static final String BAT_TEMP_CELL_MIN = "TemperatureBatteryCellMin";
	private static final String BAT_TEMP_CELL_MAX = "TemperatureBatteryCellMax";

	// Kostal inverter
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_KOSTAL_PIKO_INVERTER = "com.kiwigrid.devices.kostal.PIKO";
	private static final String INVERTER_AC_CURRENT_L1 = "ACCurrentL1";
	private static final String INVERTER_AC_CURRENT_L2 = "ACCurrentL2";
	private static final String INVERTER_AC_CURRENT_L3 = "ACCurrentL3";
	private static final String INVERTER_ACTIVE_POWER_L1 = "ActivePowerL1";
	private static final String INVERTER_ACTIVE_POWER_L2 = "ActivePowerL2";
	private static final String INVERTER_ACTIVE_POWER_L3 = "ActivePowerL3";
	private static final String INVERTER_AC_VOLTAGE_L1 = "ACVoltageL1";
	private static final String INVERTER_AC_VOLTAGE_L2 = "ACVoltageL2";
	private static final String INVERTER_AC_VOLTAGE_L3 = "ACVoltageL3";
	private static final String INVERTER_STATE = "StateDevice";

	private static final String INVERTER_POWER_YIELD_SUM = "PowerYieldSum";
	private static final String INVERTER_POWER_AC_OUT_MAX = "PowerACOutMax";
	private static final String INVERTER_POWER_AC_OUT = "PowerACOut";
	private static final String INVERTER_POWER_AC_OUT_LIMIT = "PowerACOutLimit";
	private static final String INVERTER_WORK_AC_OUT = "WorkACOut";

	// My Reserve inverter
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_MY_RESERVE_INVERTER = "com.kiwigrid.devices.solarwatt.MyReserveInverter";

	// PV Plant
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_PVPLANT = "com.kiwigrid.devices.pvplant.PVPlant";
	private static final String PLANT_PRICE_PROFIT = "PriceProfitFeedin";
	// INVERTER_POWER_AC_OUT
	private static final String PLANT_WORK_ANNUAL_YIELD = "WorkAnnualYield";
	private static final String PLANT_POWER_INSTALLED_PEAK = "PowerInstalledPeak";
	// INVERTER_WORK_AC_OUT
	private static final String PLANT_POWER_LIMIT = "PowerLimit";

	// Location
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_LOCATION = "com.kiwigrid.devices.location.Location";
	private static final String LOCATION_POWER_OUT_PRODUCERS = "PowerOutFromProducers";
	private static final String LOCATION_POWER_CONSUMED_FROM_PRODUCERS = "PowerConsumedFromProducers";
	private static final String LOCATION_POWER_IN = "PowerIn";
	private static final String LOCATION_POWER_OUT = "PowerOut";
	private static final String LOCATION_WORK_IN = "WorkIn";
	private static final String LOCATION_WORK_OUT = "WorkOut";
	private static final String LOCATION_WORK_OUT_FROM_STORAGE = "WorkOutFromStorage";
	private static final String LOCATION_POWER_SELF_SUPPLIED = "PowerSelfSupplied";
	private static final String LOCATION_WORK_BUFFERED_FROM_PRODUCER = "WorkBufferedFromProducers";
	private static final String LOCATION_POWER_BUFFERED_FROM_PRODUCER = "PowerBufferedFromProducers";
	private static final String LOCATION_WORK_SELF_CONSUMED = "WorkSelfConsumed";
	private static final String LOCATION_WORK_CONSUMED = "WorkConsumed";
	private static final String LOCATION_POWER_CONSUMED_FROM_GRID = "PowerConsumedFromGrid";
	private static final String LOCATION_POWER_PRODUCED = "PowerProduced";
	private static final String LOCATION_WORK_RELEASED = "WorkReleased";
	private static final String LOCATION_WORK_OUT_FROM_PRODUCER = "WorkOutFromProducers";
	private static final String LOCATION_POWER_CONSUMED = "PowerConsumed";
	private static final String LOCATION_POWER_RELEASED = "PowerReleased";
	private static final String LOCATION_PRICE_WORK_IN = "PriceWorkIn";
	private static final String LOCATION_POWER_BUFFERED = "PowerBuffered";
	private static final String LOCATION_WORK_CONSUMED_FROM_PRODUCERS = "WorkConsumedFromProducers";
	private static final String LOCATION_WORK_PRODUCED = "WorkProduced";
	private static final String LOCATION_WORK_CONSUMED_FROM_GRID = "WorkConsumedFromGrid";
	private static final String LOCATION_POWER_SELF_CONSUMED = "PowerSelfConsumed";
	private static final String LOCATION_WORK_BUFFERED_FROM_GRID = "WorkBufferedFromGrid";
	private static final String LOCATION_WORK_CONSUMED_FROM_STORAGE = "WorkConsumedFromStorage";
	private static final String LOCATION_WORK_BUFFERED = "WorkBuffered";

	// Powermeter
	// ------------------------------------------------------------------------------
	private static final String DEVICE_CLASS_POWERMETER = "com.kiwigrid.devices.solarwatt.MyReservePowermeter";
	// INVERTER_ACTIVE_POWER_L1
	// LOCATION_WORK_OUT
	// LOCATION_POWER_OUT
	// LOCATION_WORK_IN
	// LOCATION_POWER_IN
	// private static final String DEVICE_CONSUMPTION_POWERL3 =
	// "ConsumptionPowerL3";
	// TODO hier gibts viel mehr

	private final EssSolarwattMyReserveImpl parent;
	private final String baseUrl;
	private final boolean debugRest;
	private final boolean debugSolarwatt;
	// private ReadWorker thiz;
	private int dropCyclcesCnt = 0;

	private final int fetchInterval;

	protected ReadWorker(EssSolarwattMyReserveImpl parent, Inet4Address ipAddress, int port, boolean debugRest,
			boolean debugSolarwatt, int fetchInterval) {
		this.parent = parent;
		this.baseUrl = "http://" + ipAddress.getHostAddress() + ":" + port;
		this.debugRest = debugRest;
		this.debugSolarwatt = debugSolarwatt;
		this.fetchInterval = fetchInterval;
		// this.thiz = this;
	}

	@Override
	protected int getCycleTime() {
		return ALWAYS_WAIT_FOR_TRIGGER_NEXT_RUN;
	}

	private JsonObject getDeviceClass(JsonArray items, String deviceClazz) {
		for (var i = 0; i < items.size(); i++) {

			// TODO hier ist wohl noch ein bug drin....

			var devModels = items.get(i).getAsJsonObject().getAsJsonArray("deviceModel");
			for (var j = 0; j < devModels.size(); j++) {
				var e = devModels.get(j);
				var o = e.getAsJsonObject();
				var dc = o.get("deviceClass").getAsString();// .getAsString();
				if (dc.compareTo(deviceClazz) == 0) {
					return items.get(i).getAsJsonObject().getAsJsonObject("tagValues");
				}
			}
		}
		return null;
	}

	@Override
	protected void forever() throws Throwable {

		if (this.dropCyclcesCnt++ % this.fetchInterval != 0) {
			return;
		}
		final var communicationError = new AtomicBoolean(true);

		try {
			// JsonObject solarwattResp = getTestObject();
			var solarwattResp = this.getResponse(URL_REQ_PATH);

			var result = JsonUtils.getAsJsonObject(solarwattResp, "result");
			var items = result.getAsJsonArray("items");

			var tagValues = this.getDeviceClass(items, DEVICE_CLASS_BAT);
			this.handleBattery(tagValues);

			tagValues = this.getDeviceClass(items, DEVICE_CLASS_KOSTAL_PIKO_INVERTER);
			this.handleKostalPicoInverter(tagValues);
			tagValues = this.getDeviceClass(items, DEVICE_CLASS_MY_RESERVE_INVERTER);
			this.handleMyReserveInverter(tagValues);
			tagValues = this.getDeviceClass(items, DEVICE_CLASS_PVPLANT);
			this.handlePvPlant(tagValues);
			tagValues = this.getDeviceClass(items, DEVICE_CLASS_LOCATION);
			this.handleLocation(tagValues);
			tagValues = this.getDeviceClass(items, DEVICE_CLASS_POWERMETER);
			this.handlePowermeter(tagValues);

			/**
			 * Batterieaufladung 0W BatteryConverter -> PowerIn Batterieversorgung 0W
			 * BatteryConverter -> PowerOut Gesamtertrag 468W Location -> powerProduced
			 * Eigenverbrauch 468W Location -> powerSelfConsumed Gesamtverbrauch 573W
			 * Location -> powerConsumed Einspeisung 0 Location -> powerOut Stormzukauf 106W
			 * Location -> powerConsumedFromGrid
			 * 
			 * 
			 * Batterieaufladung 1.6kW BatteryConverter ->WorkIn Batterieversorgung
			 * BatteryConverter ->WorkOut Gesamtertrag 6.8kWh Location -> workProduced
			 * Eigenverbrauch 4.5Wh Location -> workConsumedFromProducer Gesamtverbrauch 12
			 * kWh Location -> workConsumed Einspeisung 1.1kWh Stormzukauf 6.1kWh Location
			 * -> workConsumedFromGrid
			 * 
			 */

			communicationError.set(false);
			if (this.debugSolarwatt) {
				this.log.info("all evaluated");
			}

		} catch (OpenemsNamedException e) {
			communicationError.set(true);
		}
		this.parent._setCommunicationFailed(communicationError.get());
	}

	private void handlePowermeter(JsonObject tagValues) {

		// JsonObject activPower1Obj =
		// tagValues.get(INVERTER_ACTIVE_POWER_L1).getAsJsonObject();
		var workOutObj = tagValues.get(LOCATION_WORK_OUT).getAsJsonObject();
		var workInObj = tagValues.get(LOCATION_WORK_IN).getAsJsonObject();
		var powerInObj = tagValues.get(LOCATION_POWER_IN).getAsJsonObject();
		var powerOutObj = tagValues.get(LOCATION_POWER_OUT).getAsJsonObject();
		// JsonObject consPower3Obj =
		// tagValues.get(DEVICE_CONSUMPTION_POWERL3).getAsJsonObject();

		// TODO here are a lot more values

		// Float activePower1 = getFloat(activPower1Obj);
		var powerIn = this.getFloat(powerInObj);
		var powerOut = this.getFloat(powerOutObj);
		var workIn = this.getFloat(workInObj);
		var workOut = this.getFloat(workOutObj);
		// Float consPower3 = getFloat(consPower3Obj);

		if (this.debugSolarwatt) {
			this.log.info("\n---\nPowermeter:" + "\npowerIn       :" + powerIn + "\npowerOut      :" + powerOut
					+ "\nworkIn        :" + workIn + "\nworkOut       :" + workOut);
		}
	}

	private void handleLocation(JsonObject tagValues) {

		var powerOutProducerObj = tagValues.get(LOCATION_POWER_OUT_PRODUCERS).getAsJsonObject();
		var powerConsumedFromProducersObj = tagValues.get(LOCATION_POWER_CONSUMED_FROM_PRODUCERS).getAsJsonObject();
		var powerInObj = tagValues.get(LOCATION_POWER_IN).getAsJsonObject();
		var powerOutObj = tagValues.get(LOCATION_POWER_OUT).getAsJsonObject();
		var workInObj = tagValues.get(LOCATION_WORK_IN).getAsJsonObject();
		var workOutObj = tagValues.get(LOCATION_WORK_OUT).getAsJsonObject();
		var workOutFromStorageObj = tagValues.get(LOCATION_WORK_OUT_FROM_STORAGE).getAsJsonObject();
		var workBufferedFromProducerObj = tagValues.get(LOCATION_WORK_BUFFERED_FROM_PRODUCER).getAsJsonObject();
		var powerSelfSuppliedObj = tagValues.get(LOCATION_POWER_SELF_SUPPLIED).getAsJsonObject();
		var powerBufferedFromProducerObj = tagValues.get(LOCATION_POWER_BUFFERED_FROM_PRODUCER).getAsJsonObject();
		var workSelfConsumedObj = tagValues.get(LOCATION_WORK_SELF_CONSUMED).getAsJsonObject();
		var powerConsumedFromGridObj = tagValues.get(LOCATION_POWER_CONSUMED_FROM_GRID).getAsJsonObject();
		var powerProducedObj = tagValues.get(LOCATION_POWER_PRODUCED).getAsJsonObject();
		var workReleasedObj = tagValues.get(LOCATION_WORK_RELEASED).getAsJsonObject();
		var workOutFromProducerObj = tagValues.get(LOCATION_WORK_OUT_FROM_PRODUCER).getAsJsonObject();
		var powerReleasedObj = tagValues.get(LOCATION_POWER_RELEASED).getAsJsonObject();
		var privceWorkInObj = tagValues.get(LOCATION_PRICE_WORK_IN).getAsJsonObject();
		var powerBufferedObj = tagValues.get(LOCATION_POWER_BUFFERED).getAsJsonObject();
		var workConsumedFromProducerObj = tagValues.get(LOCATION_WORK_CONSUMED_FROM_PRODUCERS).getAsJsonObject();
		var workProducedObj = tagValues.get(LOCATION_WORK_PRODUCED).getAsJsonObject();
		var workConsumedFromGridObj = tagValues.get(LOCATION_WORK_CONSUMED_FROM_GRID).getAsJsonObject();
		var powerSelfConsumedObj = tagValues.get(LOCATION_POWER_SELF_CONSUMED).getAsJsonObject();
		var workBufferedFromGridObj = tagValues.get(LOCATION_WORK_BUFFERED_FROM_GRID).getAsJsonObject();
		var workConsumedFromStorageObj = tagValues.get(LOCATION_WORK_CONSUMED_FROM_STORAGE).getAsJsonObject();
		var workBufferedObj = tagValues.get(LOCATION_WORK_BUFFERED).getAsJsonObject();

		var powerOutProducer = this.getFloat(powerOutProducerObj);
		var powerConsumedFromProducers = this.getFloat(powerConsumedFromProducersObj);
		var powerIn = this.getFloat(powerInObj);
		var powerOut = this.getFloat(powerOutObj);
		var workIn = this.getFloat(workInObj);
		var workOut = this.getFloat(workOutObj);
		var workOutFromStorage = this.getFloat(workOutFromStorageObj);
		var workBufferedFromProducer = this.getFloat(workBufferedFromProducerObj);
		var powerSelfSupplied = this.getFloat(powerSelfSuppliedObj);
		var powerBufferedFromProducer = this.getFloat(powerBufferedFromProducerObj);
		var workSelfConsumed = this.getFloat(workSelfConsumedObj);
		var powerConsumedFromGrid = this.getFloat(powerConsumedFromGridObj);
		var powerProduced = this.getFloat(powerProducedObj);
		var workReleased = this.getFloat(workReleasedObj);
		var workOutFromProducer = this.getFloat(workOutFromProducerObj);
		var powerReleased = this.getFloat(powerReleasedObj);
		var privceWorkIn = this.getString(privceWorkInObj);
		var powerBuffered = this.getFloat(powerBufferedObj);
		var workConsumedFromProducer = this.getFloat(workConsumedFromProducerObj);
		var workProduced = this.getFloat(workProducedObj);
		var workConsumedFromGrid = this.getFloat(workConsumedFromGridObj);
		var powerSelfConsumed = this.getFloat(powerSelfConsumedObj);
		var workBufferedFromGrid = this.getFloat(workBufferedFromGridObj);
		var workConsumedFromStorage = this.getFloat(workConsumedFromStorageObj);
		var workBuffered = this.getFloat(workBufferedObj);

		/*
		 * Gesamtertrag 468W Location -> powerProduced Eigenverbrauch 468W Location ->
		 * powerSelfConsumed Gesamtverbrauch 573W Location -> powerConsumed Einspeisung
		 * 0 Location -> powerOut Stormzukauf 106W Location -> powerConsumedFromGrid
		 * 
		 * Gesamtertrag 6.8kWh Location -> workProduced Eigenverbrauch 4.5Wh Location ->
		 * workConsumedFromProducer Gesamtverbrauch 12 kWh Location -> workConsumed
		 * Einspeisung 1.1kWh Stormzukauf 6.1kWh Location -> workConsumedFromGrid
		 * 
		 */

		/*
		 * Range: negative values for Consumption (power that is 'leaving the system',
		 * e.g. feed-to-grid); positive for Production (power that is 'entering the
		 * system')
		 */

		if (powerIn > powerOut) {
			// get-from-grid
			this.parent.setActivePowerGrid(Math.round(powerIn));
		} else {
			// feed-to-grid
			this.parent.setActivePowerGrid(-1 * Math.round(powerOut));
		}
		// TODO it is probably not workout
		this.parent.setActiveProductionEnergyGrid(workOut.longValue());
		this.parent.setActiveConsumptionEnergyGrid(workConsumedFromGrid.longValue());

		var powerConsumedObj = tagValues.get(LOCATION_POWER_CONSUMED).getAsJsonObject();
		var workConsumedObj = tagValues.get(LOCATION_WORK_CONSUMED).getAsJsonObject();
		var powerConsumed = this.getFloat(powerConsumedObj);
		var workConsumed = this.getFloat(workConsumedObj);
		this.parent.setActivePowerHouse(powerConsumed.intValue());
		this.parent.setActiveProductionEnergyHouse(0L);
		this.parent.setActiveConsumptionEnergyHouse(workConsumed.longValue());

		if (this.debugSolarwatt) {
			this.log.info("\n---\nLocation:" + "\npowerOutProducer          :" + powerOutProducer
					+ "\npowerConsumedFromProducers:" + powerConsumedFromProducers + "\npowerIn                   :"
					+ powerIn + "\npowerOut                  :" + powerOut + "\nworkIn                    :" + workIn
					+ "\nworkOut                   :" + workOut + "\nworkOutFromStorage        :" + workOutFromStorage
					+ "\nworkBufferedFromProducer  :" + workBufferedFromProducer + "\npowerSelfSupplied         :"
					+ powerSelfSupplied + "\npowerBufferedFromProducer :" + powerBufferedFromProducer
					+ "\nworkSelfConsumed          :" + workSelfConsumed + "\nworkConsumed              :"
					+ workConsumed + "\npowerConsumedFromGrid     :" + powerConsumedFromGrid
					+ "\npowerProduced             :" + powerProduced + "\nworkReleased              :" + workReleased
					+ "\nworkOutFromProducer       :" + workOutFromProducer + "\npowerConsumed             :"
					+ powerConsumed + "\npowerReleased             :" + powerReleased + "\nprivceWorkIn              :"
					+ privceWorkIn + "\npowerBuffered             :" + powerBuffered + "\nworkConsumedFromProducer  :"
					+ workConsumedFromProducer + "\nworkProduced              :" + workProduced
					+ "\nworkConsumedFromGrid      :" + workConsumedFromGrid + "\npowerSelfConsumed         :"
					+ powerSelfConsumed + "\nworkBufferedFromGrid      :" + workBufferedFromGrid
					+ "\nworkConsumedFromStorage   :" + workConsumedFromStorage + "\nworkBuffered              :"
					+ workBuffered);
		}

	}

	private void handlePvPlant(JsonObject tagValues) {

		var stateObj = tagValues.get(PLANT_PRICE_PROFIT).getAsJsonObject();
		var powerAcOutObj = tagValues.get(INVERTER_POWER_AC_OUT).getAsJsonObject();
		var workAcOutObj = tagValues.get(INVERTER_WORK_AC_OUT).getAsJsonObject();
		var workAnnualYieldObj = tagValues.get(PLANT_WORK_ANNUAL_YIELD).getAsJsonObject();
		var powerInstalledPeakObj = tagValues.get(PLANT_POWER_INSTALLED_PEAK).getAsJsonObject();
		var powerLimitObj = tagValues.get(PLANT_POWER_LIMIT).getAsJsonObject();

		var state = this.getString(stateObj);
		var powerAcOut = this.getFloat(powerAcOutObj);
		var workAcOut = this.getFloat(workAcOutObj);

		var workAnnualYield = this.getFloat(workAnnualYieldObj);
		var powerInstalledPeak = this.getFloat(powerInstalledPeakObj);
		var powerLimit = this.getFloat(powerLimitObj);

		this.parent.setActivePowerInverter(powerAcOut.intValue());
		this.parent.setActiveProductionEnergyInverter(workAcOut.longValue());
		this.parent.setActiveConsumptionEnergyInverter(0L);

		if (this.debugSolarwatt) {
			this.log.info("\n---\nPV Plant:" + "\nState              : " + state + "\npowerAcOut         : "
					+ powerAcOut + "\nworkAcOut          : " + workAcOut + "\nworkAnnualYield    : " + workAnnualYield
					+ "\npowerInstalledPeak : " + powerInstalledPeak + "\npowerLimit         : " + powerLimit);

		}
	}

	private void handleMyReserveInverter(JsonObject tagValues) {

		var stateObj = tagValues.get(INVERTER_STATE).getAsJsonObject();

		var powerAcOutMaxObj = tagValues.get(INVERTER_POWER_AC_OUT_MAX).getAsJsonObject();
		var powerAcOutObj = tagValues.get(INVERTER_POWER_AC_OUT).getAsJsonObject();
		var powerAcOutLimitObj = tagValues.get(INVERTER_POWER_AC_OUT_LIMIT).getAsJsonObject();
		var workAcOutObj = tagValues.get(INVERTER_WORK_AC_OUT).getAsJsonObject();

		var state = this.getString(stateObj);

		var powerAcOutMax = this.getFloat(powerAcOutMaxObj);
		var powerAcOut = this.getFloat(powerAcOutObj);
		var powerAcOutLimit = this.getFloat(powerAcOutLimitObj);
		var workAcOut = this.getFloat(workAcOutObj);

		if (this.debugSolarwatt) {
			this.log.info("\n---\nMy Reserve Inverter:" + "\nState          : " + state + "\npowerAcOut     : "
					+ powerAcOut + "\npowerAcOutLimit: " + powerAcOutLimit + "\npowerAcOutMax  : " + powerAcOutMax
					+ "\nworkAcOut      : " + workAcOut);
		}

	}

	private void handleKostalPicoInverter(JsonObject tagValues) {

		var acCur1Obj = tagValues.get(INVERTER_AC_CURRENT_L1).getAsJsonObject();
		var acCur2Obj = tagValues.get(INVERTER_AC_CURRENT_L2).getAsJsonObject();
		var acCur3Obj = tagValues.get(INVERTER_AC_CURRENT_L3).getAsJsonObject();

		var activPower1Obj = tagValues.get(INVERTER_ACTIVE_POWER_L1).getAsJsonObject();
		var activPower2Obj = tagValues.get(INVERTER_ACTIVE_POWER_L2).getAsJsonObject();
		var activPower3Obj = tagValues.get(INVERTER_ACTIVE_POWER_L3).getAsJsonObject();

		var acVolt1Obj = tagValues.get(INVERTER_AC_VOLTAGE_L1).getAsJsonObject();
		var acVolt2Obj = tagValues.get(INVERTER_AC_VOLTAGE_L2).getAsJsonObject();
		var acVolt3Obj = tagValues.get(INVERTER_AC_VOLTAGE_L3).getAsJsonObject();

		var stateObj = tagValues.get(INVERTER_STATE).getAsJsonObject();

		var powerYieldSumObj = tagValues.get(INVERTER_POWER_YIELD_SUM).getAsJsonObject();
		var powerAcOutMaxObj = tagValues.get(INVERTER_POWER_AC_OUT_MAX).getAsJsonObject();
		var powerAcOutObj = tagValues.get(INVERTER_POWER_AC_OUT).getAsJsonObject();
		var powerAcOutLimitObj = tagValues.get(INVERTER_POWER_AC_OUT_LIMIT).getAsJsonObject();
		var workAcOutObj = tagValues.get(INVERTER_WORK_AC_OUT).getAsJsonObject();

		var acCur1 = this.getFloat(acCur1Obj);
		var acCur2 = this.getFloat(acCur2Obj);
		var acCur3 = this.getFloat(acCur3Obj);

		var activePower1 = this.getFloat(activPower1Obj);
		var activePower2 = this.getFloat(activPower2Obj);
		var activePower3 = this.getFloat(activPower3Obj);

		var acVolt1 = this.getFloat(acVolt1Obj);
		var acVolt2 = this.getFloat(acVolt2Obj);
		var acVolt3 = this.getFloat(acVolt3Obj);

		var state = this.getString(stateObj);

		var powerYieldSum = this.getFloat(powerYieldSumObj);
		var powerAcOutMax = this.getFloat(powerAcOutMaxObj);
		var powerAcOut = this.getFloat(powerAcOutObj);
		var powerAcOutLimit = this.getFloat(powerAcOutLimitObj);
		var workAcOut = this.getFloat(workAcOutObj);

		/**
		 * system state 0 = ok 1 = other 2 = not available
		 */
		if (state == null) {
			this.parent.setInverterSystemState(2);
		} else if (state.compareTo("OK") == 0) {
			this.parent.setInverterSystemState(0);
		} else {
			this.parent.setInverterSystemState(1);
		}

		// TODO provide current and voltage channels for each phase for better metering

		// TODO this is inverter power, we need this for grid meter ?

		if (this.debugSolarwatt) {
			this.log.info("\n---\nKostal Piko Inverter:" + "\nacVolt1        : " + acVolt1 + "\nacVolt2        : "
					+ acVolt2 + "\nacVolt3        : " + acVolt3 + "\nacCur1         : " + acCur1 + "\nacCur2         : "
					+ acCur2 + "\nacCur3         : " + acCur3 + "\nState          : " + state + "\nactivePower1   : "
					+ activePower1 + "\nactivePower2   : " + activePower2 + "\nactivePower3   : " + activePower3
					+ "\npowerYieldSum1 : " + powerYieldSum + "\npowerAcOut     : " + powerAcOut + "\npowerAcOutLimit: "
					+ powerAcOutLimit + "\npowerAcOutMax  : " + powerAcOutMax + "\nworkAcOut      : " + workAcOut);
		}

	}

	private void handleBattery(JsonObject tagValues) {
		var sohObj = tagValues.get(BAT_SOH).getAsJsonObject();
		var socObj = tagValues.get(BAT_SOC).getAsJsonObject();

		var batCurInObj = tagValues.get(BAT_CURRENT_IN).getAsJsonObject();
		var batCurOutObj = tagValues.get(BAT_CURRENT_OUT).getAsJsonObject();
		var batStateObj = tagValues.get(BAT_STATE).getAsJsonObject();
		var batPowerInObj = tagValues.get(BAT_POWER_IN).getAsJsonObject();
		var batPowerOutObj = tagValues.get(BAT_POWER_OUT).getAsJsonObject();
		var batWorkInObj = tagValues.get(BAT_WORK_IN).getAsJsonObject();
		var batWorkOutObj = tagValues.get(BAT_WORK_OUT).getAsJsonObject();
		var batVoltCellMinObj = tagValues.get(BAT_VOLT_CELL_MIN).getAsJsonObject();
		var batVoltCellMaxObj = tagValues.get(BAT_VOLT_CELL_MAX).getAsJsonObject();
		var batVoltCellMeanObj = tagValues.get(BAT_VOLT_CELL_MEAN).getAsJsonObject();

		var batVoltStringObj = tagValues.get(BAT_VOLT_CELL_STRING).getAsJsonObject();

		var batTempCellMinObj = tagValues.get(BAT_TEMP_CELL_MIN).getAsJsonObject();
		var batTempCellMaxObj = tagValues.get(BAT_TEMP_CELL_MAX).getAsJsonObject();

		var batSoh = this.getFloat(sohObj);
		var batSoc = this.getFloat(socObj);

		var batCIn = this.getFloat(batCurInObj);
		var batCurOut = this.getFloat(batCurOutObj);

		var batState = this.getString(batStateObj);
		var batPowerIn = this.getFloat(batPowerInObj);
		var batPowerOut = this.getFloat(batPowerOutObj);
		var batWorkIn = this.getFloat(batWorkInObj);
		var batWorkOut = this.getFloat(batWorkOutObj);

		var batVoltCellMin = this.getFloat(batVoltCellMinObj);
		var batVoltCellMax = this.getFloat(batVoltCellMaxObj);
		var batVoltCellMean = this.getFloat(batVoltCellMeanObj);

		var batVoltString = this.getFloat(batVoltStringObj);

		var batTempCellMin = this.getFloat(batTempCellMinObj);
		var batTempCellMax = this.getFloat(batTempCellMaxObj);

		if (this.debugSolarwatt) {
			this.log.info("\n---\nBatteryConverter:" + "\nSOH         : " + batSoh + "\nSOC         : " + batSoc
					+ "\nCurrentIn   : " + batCIn + "\nCurrentOut  : " + batCurOut + "\nState       : " + batState
					+ "\nPowerIn     : " + batPowerIn + "\nPowerOut    : " + batPowerOut + "\nWorkIn      : "
					+ batWorkIn + "\nWorkOut     : " + batWorkOut + "\nVoltCellMin : " + batVoltCellMin
					+ "\nVoltCellMax : " + batVoltCellMax + "\nVoltCellMean: " + batVoltCellMean + "\nVoltString  : "
					+ batVoltString + "\nTempCellMin : " + batTempCellMin + "\nTempCellMax : " + batTempCellMax

			);
		}

		this.parent._setSoc(Math.round(batSoc));
		this.parent.setSoh(Math.round(batSoh));
		/**
		 * system state 0 = ok 1 = other 2 = not available
		 */
		if (batState == null) {
			this.parent.setBatterySystemState(2);
		} else if (batState.compareTo("OK") == 0) {
			this.parent.setBatterySystemState(0);
		} else {
			this.parent.setBatterySystemState(1);
		}

		this.parent._setMinCellVoltage(Math.round(batVoltCellMin * 1000f));
		this.parent._setMaxCellVoltage(Math.round(batVoltCellMax * 1000f));

		this.parent._setMinCellTemperature(Math.round(batTempCellMin));
		this.parent._setMaxCellTemperature(Math.round(batTempCellMax));

		// OpenEMS <-> negative values for Charge; positive for Discharge
		// TODO battery charging, discharging

		if (batPowerIn > batPowerOut) {
			// charge
			this.parent.setBatteryCharging(true);
			// <li>Range: negative values for Charge; positive for Discharge
			this.parent._setActivePower(batPowerIn.intValue() * -1);
		} else {
			// discharge
			this.parent.setBatteryCharging(false);
			// <li>Range: negative values for Charge; positive for Discharge
			this.parent._setActivePower(batPowerOut.intValue());

		}
		this.parent._setReactivePower(0);

		this.parent._setActiveChargeEnergy(Math.round(batWorkIn));
		this.parent._setActiveDischargeEnergy(Math.round(batWorkOut));

		// current
		if (batCIn > batCurOut) {
			var mA = (int) (batCIn * 1000.0);
			this.parent.setBatteryCurrent(mA);
		} else {
			var mA = (int) (batCurOut * 1000.0);
			this.parent.setBatteryCurrent(mA);
		}
		this.parent.setBatteryCharging(true);

		// voltage
		var mV = (int) (batVoltString * 1000.0);
		this.parent.setBatteryVoltage(mV);
	}

	private Float getFloat(JsonObject obj) {
		try {
			return obj.get("value").getAsFloat();
		} catch (Exception e) {
			this.log.error("Invalid element " + obj.toString());
			return 0.0f;
		}
	}

	private String getString(JsonObject obj) {
		return obj.get("value").getAsString();
	}

	// //OpenEMS <-> negative values for Charge; positive for Discharge
	// private void handleBatPower(float _activePower) {
	//
	// float activePower = _activePower;
	// float reactivePower = activePower;
	//
	// this.parent._setActivePower(Math.round(activePower));
	// this.parent._setReactivePower(Math.round(reactivePower));
	//
	// switch (this.parent.getPhase()) {
	// case L1:
	// this.parent._setActivePowerL1(Math.round(activePower));
	// this.parent._setActivePowerL2(0);
	// this.parent._setActivePowerL3(0);
	// this.parent._setReactivePowerL1(Math.round(reactivePower));
	// this.parent._setReactivePowerL2(0);
	// this.parent._setReactivePowerL3(0);
	// break;
	// case L2:
	// this.parent._setActivePowerL1(0);
	// this.parent._setActivePowerL2(Math.round(activePower));
	// this.parent._setActivePowerL3(0);
	// this.parent._setReactivePowerL1(0);
	// this.parent._setReactivePowerL2(Math.round(reactivePower));
	// this.parent._setReactivePowerL3(0);
	// break;
	// case L3:
	// this.parent._setActivePowerL1(0);
	// this.parent._setActivePowerL2(0);
	// this.parent._setActivePowerL3(Math.round(activePower));
	// this.parent._setReactivePowerL1(0);
	// this.parent._setReactivePowerL2(0);
	// this.parent._setReactivePowerL3(Math.round(reactivePower));
	// break;
	// }
	// }

	/**
	 * Gets the JSON response of a HTTPS GET Request.
	 * 
	 * @param path the api path
	 * @return the JsonObject
	 * @throws OpenemsNamedException on error
	 */
	private JsonObject getResponse(String path) throws OpenemsNamedException {
		try {
			var uri = URI.create(this.baseUrl + path);
			var url = uri.toURL();
			var conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json; utf-8");
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoOutput(false);
			conn.setDoInput(true);

			// read result from webserver
			try (var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				var content = reader.lines().collect(Collectors.joining());
				if (this.debugRest) {
					this.log.info(this.baseUrl + " Response: " + content);
				}
				return JsonUtils.parseToJsonObject(content);
			}
		} catch (IOException e) {
			throw new OpenemsException(e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
}
