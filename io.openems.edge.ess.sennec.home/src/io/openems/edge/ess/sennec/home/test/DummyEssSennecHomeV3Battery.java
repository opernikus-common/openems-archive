package io.openems.edge.ess.sennec.home.test;

import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.ess.api.AsymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;
import io.openems.edge.ess.sennec.home.EssSennecHomeV3Battery;

public class DummyEssSennecHomeV3Battery extends AbstractOpenemsComponent implements EssSennecHomeV3Battery, SymmetricEss, AsymmetricEss, OpenemsComponent {

  public DummyEssSennecHomeV3Battery(String id) {
    super(OpenemsComponent.ChannelId.values(), //
          SymmetricEss.ChannelId.values(), //
          AsymmetricEss.ChannelId.values(), //
		  EssSennecHomeV3Battery.ChannelId.values() //
        );
         for (Channel<?> channel : this.channels()) {
			 channel.nextProcessImage();
          }
          super.activate(null, id, "", true);
  }

}
