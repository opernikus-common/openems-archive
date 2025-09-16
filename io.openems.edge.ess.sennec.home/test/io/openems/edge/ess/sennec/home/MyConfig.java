package io.openems.edge.ess.sennec.home;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.edge.common.sum.GridMode;
import io.openems.edge.ess.api.SinglePhase;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private int capacity;
		private int maxApparentPower;
		private int cycleTime;
		private SinglePhase phase;
		private GridMode gridMode;
		private String id = null;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setCapacity(int capacity) {
			this.capacity = capacity;
			return this;
		}

		public Builder setMaxApparentPower(int maxApparentPower) {
			this.maxApparentPower = maxApparentPower;
			return this;
		}

		public Builder setCycleTime(int cycleTime) {
			this.cycleTime = cycleTime;
			return this;
		}

		public Builder setPhase(SinglePhase phase) {
			this.phase = phase;
			return this;
		}

		public Builder setGridMode(GridMode gridMode) {
			this.gridMode = gridMode;
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
	public String ipAddress() {
		return "127.0.0.1";
	}

	@Override
	public int port() {
		return 502;
	}

	@Override
	public int capacity() {
		return this.builder.capacity;
	}

	@Override
	public int maxApparentPower() {
		return this.builder.maxApparentPower;
	}

	@Override
	public int cycleTime() {
		return this.builder.cycleTime;
	}

	@Override
	public SinglePhase phase() {
		return this.builder.phase;
	}

	@Override
	public GridMode gridMode() {
		return this.builder.gridMode;
	}

	@Override
	public int debugMode() {
		return 0;
	}
}
