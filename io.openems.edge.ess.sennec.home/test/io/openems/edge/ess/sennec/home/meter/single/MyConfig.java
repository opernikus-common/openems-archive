package io.openems.edge.ess.sennec.home.meter.single;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.common.types.MeterType;
import io.openems.common.utils.ConfigUtils;
import io.openems.edge.meter.api.SinglePhase;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private MeterType type;
		private String batteryId;
		private SinglePhase phase;
		private String id = null;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setType(MeterType type) {
			this.type = type;
			return this;
		}

		public Builder setBatteryId(String batteryId) {
			this.batteryId = batteryId;
			return this;
		}

		public Builder setPhase(SinglePhase phase) {
			this.phase = phase;
			return this;
		}

		public MyConfig build() {
			return new MyConfig(this);
		}
	}

	/**
	 * Create a Config builder.
	 *
	 * @return a {@link Builder}
	 */
	public static Builder create() {
		return new Builder();
	}

	private final Builder builder;

	private MyConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}

	@Override
	public SinglePhase phase() {
		return this.builder.phase;
	}

	@Override
	public MeterType type() {
		return this.builder.type;
	}

	@Override
	public String battery_id() {
		return this.builder.batteryId;
	}

	@Override
	public String battery_target() {
		return ConfigUtils.generateReferenceTargetFilter(this.id(), this.battery_id());
	}

}
