package io.openems.edge.ess.solarwatt.myreserve;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.edge.common.sum.GridMode;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String ipAddress;
		private int port;
		private int maxApparentPower;
		private GridMode gridMode;
		private boolean debugRest;
		private boolean debugSolarwatt;
		private int interval;
		private String id;
		private int capacity;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
			return this;
		}

		public Builder setPort(int port) {
			this.port = port;
			return this;
		}

		public Builder setMaxApparentPower(int maxApparentPower) {
			this.maxApparentPower = maxApparentPower;
			return this;
		}

		public Builder setGridMode(GridMode gridMode) {
			this.gridMode = gridMode;
			return this;
		}

		public Builder setDebugRest(boolean debugRest) {
			this.debugRest = debugRest;
			return this;
		}

		public Builder setDebugSolarwatt(boolean debugSolarwatt) {
			this.debugSolarwatt = debugSolarwatt;
			return this;
		}

		public Builder setInterval(int interval) {
			this.interval = interval;
			return this;
		}

		public Builder setCapacity(int capacity) {
			this.capacity = capacity;
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
		return this.builder.ipAddress;
	}

	@Override
	public int port() {
		return this.builder.port;
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
	public GridMode gridMode() {
		return this.builder.gridMode;
	}

	@Override
	public boolean debugRest() {
		return this.builder.debugRest;
	}

	@Override
	public boolean debugSolarwatt() {
		return this.builder.debugSolarwatt;
	}

	@Override
	public int interval() {
		return this.builder.interval;
	}

}
