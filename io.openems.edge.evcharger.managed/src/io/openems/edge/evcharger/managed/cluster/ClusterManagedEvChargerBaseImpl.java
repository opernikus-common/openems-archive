package io.openems.edge.evcharger.managed.cluster;

import java.time.Clock;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.sum.Sum;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.evcharger.api.ManageableEvCharger;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "EvCharger.ClusterManagedEvCharger.Base", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
		EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE //
})
public class ClusterManagedEvChargerBaseImpl extends AbstractOpenemsComponent
		implements ClusterManagedEvChargerBase, EventHandler, OpenemsComponent {

	private final Logger log = LoggerFactory.getLogger(ClusterManagedEvChargerBaseImpl.class);

	@Reference
	private ConfigurationAdmin cm;

	@Reference
	private Sum sum;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ManageableEvCharger charger;

	private Config config;

	public ClusterManagedEvChargerBaseImpl() {
		this(Clock.systemDefaultZone());
	}

	protected ClusterManagedEvChargerBaseImpl(Clock clock) {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ClusterManagedEvChargerBase.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		this.config = config;
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.modify(config);
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Modified
	private void modified(ComponentContext compContext, Config config) {
		this.config = config;
		super.modified(compContext, config.id(), config.alias(), config.enabled());
		this.modify(config);
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE -> this.channelUpdate();
		case EdgeEventConstants.TOPIC_CYCLE_EXECUTE_WRITE -> this.writeUpdate();
		}
	}

	private void modify(Config config) {
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "evchargerClusterBase",
				config.evchargerClusterBase_id())) {
			return;
		}
		if (!config.enabled()) {
			return;
		}
	}

	private void channelUpdate() {

		/*
		 * Fuer Ladepark + Initialisierungsphase zur Detektion von -SessionPhases
		 * -SessionMaxCurrent -SessionMinCurrent (wegen ZoE)
		 */

		this.logDebug(this.log, "Next charge power: " + this.config.alias() + " W");

	}

	private void writeUpdate() {
		// TODO Auto-generated method stub
	}

}
