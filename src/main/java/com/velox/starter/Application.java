package com.velox.starter;

import com.aralis.gooey.api.ViewConfigBuilder;
import com.aralis.tools.configuration.UIConfigProvider;
import com.aralis.tools.configuration.ui.UserSettingScreenProvider;
import com.aralis.tools.support.SupportViewerScreenProvider;
import com.aralis.vm.DataGrid;
import com.aralis.vm.ScreenProviderFactory;
import com.aralis.vm.SimpleScreenProviderFactory;
import com.caelo.application.ApplicationContext;
import com.caelo.application.ApplicationContextBuilder;
import com.caelo.application.VeloxCoreComponents;
import com.caelo.util.logging.Loggers;
import com.velox.app.api.InstanceInfoBuilder;
import com.velox.starter.api.StarterSchemaDescriptor;
import com.velox.tools.VeloxToolComponents;
import com.velox.tools.VeloxToolModule;
import com.velox.web.VeloxWebComponents;
import com.velox.web.VeloxWebModule;
import com.velox.web.vertx.ContextRoot;
import org.slf4j.Logger;
import java.time.Instant;

public class Application {
    private final static Logger s_log = Loggers.getLogger();

    public static void main(String[] args) {
        var instanceId = "dev";
        var instance = InstanceInfoBuilder.newBuilder().instanceId(instanceId).startTime(Instant.now()).get();
        s_log.info("bootstrapping instance {}", instance.instanceId());

        var context = ApplicationContextBuilder.create()
          .install(new VeloxWebModule())
          .install(new VeloxToolModule())
          .register(VeloxCoreComponents.InstanceInfo, ctx -> instance)
          .register(VeloxCoreComponents.ScreenProviderFactory, Application::createScreenProviderFactory)
          .get();

        context.get(VeloxCoreComponents.DataContextAccessor).addDescriptors(StarterSchemaDescriptor.Descriptors);

        var registry = context.get(VeloxCoreComponents.DomainControlActionHandlerRegistry);

        registry.register(DataGrid.class, "QuickSettings", (notifier, state, grid, blob, cb) -> {
            final var dg = (DataGrid<?, ?>) grid;
            new QuickGridSettingsScreenProvider(dg, registry).create(state, notifier);
        });

        context.get(VeloxWebComponents.WebServerBuilder)
          .addPort(6061)
          .addContextRoot(ContextRoot.create("/starter"))
          .start();
    }

    private static ScreenProviderFactory createScreenProviderFactory(ApplicationContext ctx) {
        return new SimpleScreenProviderFactory(
          new StarterScreenProvider("Starter", "Velox", "fa-desktop"),
          new SupportViewerScreenProvider(ctx.get(VeloxToolComponents.CachePublisherTracker),
            "Support Viewer",
            "Support",
            "fa-phone"),
          new UserSettingScreenProvider("User Settings", "Configuration", "fa-user-circle"));
    }
}
