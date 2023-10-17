package com.velox.starter;

import com.aralis.df.cache.state.DataContextAccessor;
import com.aralis.tools.support.SupportViewerScreenProvider;
import com.aralis.vm.ScreenProviderFactory;
import com.aralis.vm.SimpleScreenProviderFactory;
import com.caelo.application.ApplicationContext;
import com.caelo.application.ApplicationContextBuilder;
import com.caelo.application.VeloxCoreComponents;
import com.caelo.util.logging.Loggers;
import com.velox.app.api.InstanceInfoBuilder;
import com.velox.boule.sm.api.ConnectionBuilder;
import com.velox.boule.sm.api.SMConfig;
import com.velox.boule.sm.api.SMConfigBuilder;
import com.velox.boule.sm.api.SharedSMConfigBuilder;
import com.velox.config.VeloxConfigModule;
import com.velox.starter.api.CustomerBlotterScreen;
import com.velox.starter.api.TrainingSMCommand;
import com.velox.starter.api.User;
import com.velox.starter.api.trainingsm.client.TrainingSMClient;
import com.velox.starter.api.trainingsm.server.TrainingSMHandler;
import com.velox.starter.api.trainingsm.server.TrainingSMServer;
import com.velox.tools.VeloxToolComponents;
import com.velox.tools.VeloxToolModule;
import com.velox.tools.ui.UserSettingScreenProvider;
import com.velox.web.VeloxWebComponents;
import com.velox.web.VeloxWebModule;
import com.velox.web.vertx.ContextRoot;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class Application {
    private final static Logger s_log = Loggers.getLogger();

    public static void main(String[] args) {
        var instanceId = "dev";
        var instance = InstanceInfoBuilder.newBuilder().instanceId(instanceId).startTime(Instant.now()).get();

        var context = ApplicationContextBuilder.create()
          .install(new VeloxWebModule())
          .install(new VeloxToolModule())
          .install(new VeloxConfigModule())
          .register(VeloxCoreComponents.InstanceInfo, ctx -> instance)
          .register(VeloxCoreComponents.ScreenProviderFactory, Application::createScreenProviderFactory)
          .register(VeloxWebComponents.Vertx, Application::createVertx)
          .register(TrainingSMClient.class, Application::createTrainingSMClient)
          .get();

        var env = context.get(VeloxCoreComponents.VeloxEnvironment);

        var root = ContextRoot.create("/starter");
        if (env.isDevelopment()) {
            root.addWebRoot("build/extracted-included-webapp/src/main/webapp");
        }

        context.get(VeloxWebComponents.WebServerBuilder)
          .addPort(6061)
          .addContextRoot(root)
          .start();

        s_log.info("started instance {}, environment {}", instance.instanceId(), env.mode());
    }

    private static ScreenProviderFactory createScreenProviderFactory(ApplicationContext ctx) {
        TrainingSMClient smClient = ctx.get(TrainingSMClient.class);
        return new SimpleScreenProviderFactory(
                new CustomerBlotterScreenProvider(smClient, "Customer", "group","icon2"),
                new StylistBlotterScreenProvider(smClient, "Caption", "group","icon"),
          new StarterScreenProvider(
            "Starter",
            "Velox",
            "fa-solid fa-desktop",
            ctx.get(VeloxCoreComponents.DataContextAccessor).getPublisher(User.class)),
          new SupportViewerScreenProvider(ctx.get(VeloxToolComponents.CachePublisherTracker),
            "Support Viewer",
            "Support",
            "fa-solid fa-phone"),
          new UserSettingScreenProvider("User Settings", "Configuration", "fa-solid fa-circle-user"));
    }

    private static Vertx createVertx(ApplicationContext ctx) {
        var env = ctx.get(VeloxCoreComponents.VeloxEnvironment);
        if (env.isDevelopment()) {
            var options = new VertxOptions();
            options.setFileSystemOptions(new FileSystemOptions().setClassPathResolvingEnabled(false));
            return Vertx.vertx(options);
        } else {
            return Vertx.vertx();
        }
    }

    private static TrainingSMClient createTrainingSMClient(ApplicationContext ctx) throws IOException {
        DataContextAccessor dc = ctx.get(VeloxCoreComponents.DataContextAccessor);
        SMConfig smconfig = SMConfigBuilder.newBuilder()
                .myNodeName("local")
                .sharedConfig(
                        SharedSMConfigBuilder.newBuilder()
                                .instanceName("Stylist")
                                .sessionName("One")
                                .connections(
                                        List.of(ConnectionBuilder.newBuilder()
                                                .nodeName("local")
                                                .hostName("localhost")
                                                .port(12345)
                                                .preferredPrimary(true)
                                                .get())).get())
                .get();

        try {
            TrainingSMServer server = new TrainingSMServer.Builder(smconfig, new TrainingSMHandler() {
            }).build();
            server.run();
            TrainingSMClient client =
                    new TrainingSMClient.Builder("localclient", smconfig.sharedConfig()).dataContext(dc).subscribeAllStates().build();
            return client;
        } catch (Exception e) {
        }
        return null;
    }
}
