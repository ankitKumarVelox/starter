package com.velox.starter;

import com.aralis.app.api.InstanceInfo;
import com.aralis.app.api.InstanceInfoBuilder;
import com.aralis.application.ApplicationContext;
import com.aralis.application.ApplicationContextBuilder;
import com.aralis.application.VeloxCoreComponents;
import com.aralis.tools.VeloxToolModule;
import com.aralis.tools.configuration.ui.UserSettingScreenProvider;
import com.aralis.vm.ScreenProviderFactory;
import com.aralis.vm.SimpleScreenProviderFactory;
import com.caelo.util.logging.Loggers;
import com.velox.starter.api.StarterSchemaDescriptor;
import com.velox.web.VeloxWebComponents;
import com.velox.web.VeloxWebModule;
import com.velox.web.vertx.ContextRoot;
import org.slf4j.Logger;
import java.time.Instant;

public class Application {
    private final static Logger s_log = Loggers.getLogger();

    public static void main(String[] args) {
        bootstrap("dev").get(VeloxWebComponents.WebServerBuilder)
          .addPort(6061)
          .addContextRoot(ContextRoot.create("/starter"))
          .start();
    }

    public static ApplicationContext bootstrap(String instanceId) {
        InstanceInfo instance = InstanceInfoBuilder.newBuilder().instanceId(instanceId).startTime(Instant.now()).get();
        s_log.info("bootstrapping instance {}", instance.instanceId());

        ApplicationContext context = ApplicationContextBuilder.create()
          .install(new VeloxWebModule())
          .install(new VeloxToolModule())
          .register(VeloxCoreComponents.InstanceInfo, ctx -> instance)
          .register(VeloxCoreComponents.ScreenProviderFactory, Application::createScreenProviderFactory)
          .get();

        context.get(VeloxCoreComponents.DataContextAccessor).addDescriptors(StarterSchemaDescriptor.Descriptors);

        return context;
    }

    private static ScreenProviderFactory createScreenProviderFactory(ApplicationContext ctx) {
        return new SimpleScreenProviderFactory(
          new StarterScreenProvider("Starter", "Velox", "fa-desktop"),
          new UserSettingScreenProvider("User Settings", "Configuration", "fa-user-circle"));
    }
}
