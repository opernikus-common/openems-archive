package io.openems.edge.evcharger.managed.cluster;

import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.common.types.MeterType;

public interface ClusterManagedEvChargerBase extends ElectricityMeter, OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		/**
		 * Gets the average response time of this EvCharger in ms.
		 *
		 * <p/>
		 * This value is computed by OpenEMS. It gives a raw indication, when to expect
		 * a reaction from the chargepoint/car combination.
		 * 
		 * <ul>
		 * <li>Interface: EvChargerContext
		 * <li>Readable
		 * <li>Type: {@link OpenemsType#INTEGER}
		 * <li>Unit: {@link Unit#MILLISECONDS}
		 * </ul>
		 */
		AVG_RESPONSE_TIME(Doc.of(OpenemsType.INTEGER) //
				.unit(Unit.MILLISECONDS) //
				.persistencePriority(PersistencePriority.HIGH)), //
		; //

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}
	
	/**
	 * Gets the type of this Meter.
	 *
	 * @return the {@link MeterType}
	 */
	@Override
	public default MeterType getMeterType() {
		return MeterType.CONSUMPTION_METERED;
	}

}
