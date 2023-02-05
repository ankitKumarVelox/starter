package com.velox.starter;

import com.aralis.tools.support.SupportViewerScreenProvider;
import com.aralis.vm.ScreenProviderFactory;
import com.aralis.vm.SimpleScreenProviderFactory;
import com.caelo.application.ApplicationContext;
import com.caelo.application.ApplicationContextBuilder;
import com.caelo.application.VeloxCoreComponents;
import com.caelo.util.logging.Loggers;
import com.velox.app.api.InstanceInfoBuilder;
import com.velox.config.*;
import com.velox.configuration.ui.ConfigurationEditScreenProvider;
import com.velox.starter.api.User;
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
import java.time.Instant;
import java.util.List;

public class Application {
    private final static Logger s_log = Loggers.getLogger();

    public static void main(String[] args) {
        var instanceId = "dev";
        var instance = InstanceInfoBuilder.newBuilder().instanceId(instanceId).startTime(Instant.now()).get();

        // Velox uses dependency injection to manage the app server initialization
        // ApplicationContext is the Velox implementation of the dependency injection pattern
        // Refer to https://portal.veloxfintech.com/doc/velox-docs/v2.4.0/velox_documentation/velox_framework/components_and_modules/components_and_modules.html
        // to learn more about Velox application initialization
        var context = ApplicationContextBuilder.create()
          .install(new VeloxWebModule())
          .install(new VeloxToolModule())
          .install(new VeloxConfigModule())
          .register(VeloxCoreComponents.InstanceInfo, ctx -> instance)
          .register(VeloxCoreComponents.ScreenProviderFactory, Application::createScreenProviderFactory)
          .register(VeloxWebComponents.Vertx, Application::createVertx)
          // The following three components are optional as VeloxConfigModule installs default implementation
          // for all three.
          // ConfigURI specifies the configuration for the config service
          // Refer to https://portal.veloxfintech.com/doc/velox-docs/v2.4.0/velox_documentation/velox_framework/velox_configuration/configservice_configpersistence.html#_configuri
          // to learn more about ConfigURI
          .register(
            VeloxConfigComponents.ConfigURI,
            ctx -> ConfigURIBuilder.newBuilder()
              .serviceName("MyConfig")
              .sessionName("Permanent")
              .nodes(List.of(NodeConfigBuilder.newBuilder()
                .hostName("localhost")
                .port(12345)
                .preferredPrimary(true)
                .nodeName("primary")
                .get()))
              .get())
          // ConfigPersistenceSetting is only needed if you want to run the config service in this process
          // basePath specifies the root path for the config service journal and node name should match
          // one of the node name in the ConfigURI above
          // Refer to https://portal.veloxfintech.com/doc/velox-docs/v2.4.0/velox_documentation/velox_framework/velox_configuration/configservice_configpersistence.html#_configpersistencesetting
          // to learn more about ConfigPersistenceSetting
          .register(
            VeloxConfigComponents.ConfigPersistenceSetting,
            ctx -> ConfigPersistenceSettingBuilder.newBuilder().basePath("journal").nodeName("primary").get())
          // ConfigBootstrapSetting specifies where to load the initial set of configuration data from
          // It's used by ConfigBootstrapInjector which will be kicked off below
          // ConfigBootstrapInjector will only kick in if there is no configuration already stored in the
          // ConfigService. If empty, it will load the configuration first from the previously persisted
          // json file. If such json files don't exist, it will load from resource file checked into the
          // project. resourceFileName is the name of the resource file to load and basePath is where the
          // json files will be stored and loaded. ConfigBootstrapInjector automatically dumps the content
          // of the ConfigService upon process exit.
          // Refer to https://portal.veloxfintech.com/doc/velox-docs/v2.4.0/velox_documentation/velox_framework/velox_configuration/configuration_bootstrap.html
          // to learn more about ConfigBootstrapInjector and ConfigBootstrapSetting
          .register(
            VeloxConfigComponents.ConfigBootstrapSetting,
            ctx -> ConfigBootstrapSettingBuilder.newBuilder()
              .resourceFileName("/config_bootstrap.json")
              .basePath("backup")
              .get())
          .get();

        var env = context.get(VeloxCoreComponents.VeloxEnvironment);
        // kicking off the ConfigBootstrapInjector to registerShutdownHook and hydrate configuration if needed
        context.get(VeloxConfigComponents.ConfigBootstrapInjector);

        var root = ContextRoot.create("/starter");
        if (env.isDevelopment()) {
            root.addWebRoot("build/extracted-included-webapp/src/main/webapp");
        }

        // The following line will kick off the web server on the specified port
        context.get(VeloxWebComponents.WebServerBuilder).addPort(6061).addContextRoot(root).start();

        s_log.info("started instance {}, environment {}", instance.instanceId(), env.mode());
    }

    private static ScreenProviderFactory createScreenProviderFactory(ApplicationContext ctx) {
        ConfigPersistence configPersistence = ctx.get(VeloxConfigComponents.ConfigPersistence);
        return new SimpleScreenProviderFactory(
          new StarterScreenProvider(
            "Starter",
            "Velox",
            "fa-solid fa-desktop",
            ctx.get(VeloxCoreComponents.DataContextAccessor).getPublisher(User.class)),
          new SupportViewerScreenProvider(ctx.get(VeloxToolComponents.CachePublisherTracker),
            "Support Viewer",
            "Support",
            "fa-solid fa-phone"),
          new ConfigurationEditScreenProvider(configPersistence, "Config Editor", "Configuration", "fa-solid fa-cogs"),
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
}
