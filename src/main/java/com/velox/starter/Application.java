package com.velox.starter;

import com.aralis.df.cache.CachePublisher;
import com.aralis.df.cache.CachePublisherTrackingFactory;
import com.aralis.df.cache.filter.FilterExpressionParser;
import com.aralis.df.cache.helpers.ImmutableFilterListener;
import com.aralis.threads.TimeMeasureUtil;
import com.aralis.tools.support.SupportViewerScreenProvider;
import com.aralis.vm.ScreenProviderFactory;
import com.aralis.vm.SimpleScreenProviderFactory;
import com.caelo.application.ApplicationContext;
import com.caelo.application.VeloxCoreComponents;
import com.caelo.lang.Closer;
import com.caelo.util.concurrent.ExecutorUtil;
import com.caelo.util.logging.Loggers;
import com.velox.app.api.InstanceInfoBuilder;
import com.velox.starter.api.*;
import com.velox.tools.VeloxToolComponents;
import com.velox.tools.ui.UserSettingScreenProvider;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Application {
    private final static Logger s_log = Loggers.getLogger();

    public static void main(String[] args) {
        var instanceId = "dev";
        var instance = InstanceInfoBuilder.newBuilder().instanceId(instanceId).startTime(Instant.now()).get();

        //        var context = ApplicationContextBuilder.create()
        //          .install(new VeloxWebModule())
        //          .install(new VeloxToolModule())
        //          .install(new VeloxConfigModule())
        //          .register(VeloxCoreComponents.InstanceInfo, ctx -> instance)
        //          .register(VeloxCoreComponents.ScreenProviderFactory, Application::createScreenProviderFactory)
        //          .register(VeloxWebComponents.Vertx, Application::createVertx)
        //          .get();
        //
        //        var env = context.get(VeloxCoreComponents.VeloxEnvironment);
        //
        //        var root = ContextRoot.create("/starter");
        //        if (env.isDevelopment()) {
        //            root.addWebRoot("build/extracted-included-webapp/src/main/webapp");
        //        }

        kickOffTimerJoin();

        //        context.get(VeloxWebComponents.WebServerBuilder).addPort(6061).addContextRoot(root).start();

        //        s_log.info("started instance {}, environment {}", instance.instanceId(), env.mode());
    }

    private static void kickOffTimerJoin() {
        var pub = CachePublisherTrackingFactory.Uninstrumented.create(UserTableDescriptor.Descriptor);

        var prefix = "2023_07_21NYCEB_YUINBEL_";
        var format = prefix + "_%09d";

        ArrayList<User> chunk = new ArrayList<>();
        for (int i = 0; i < 10_000_000; ) {
            for (int j = 0; j < 1_000_000; j++, i++) {
                chunk.add(UserBuilder.newBuilder().email(String.format(format, i)).timeKey("currentTime").build());
            }
            pub.publish(chunk);
            chunk.clear();
            s_log.info("Published {}", i);
        }

        int nOfTimes = 5;
        s_log.info("Time taken to search {} : {}ms", nOfTimes, TimeMeasureUtil.measureMS(() -> {
            for (int i = 0; i < nOfTimes; i++) {
                try (var closer = Closer.create()) {
                    var c = 1_000_000 + i;
                    var filter = FilterExpressionParser.parse("(contains email " + c+ ")", UserExtractor.ExtractorsMap);
                    pub.getTable().subscribe(new ImmutableFilterListener<User>(filter, (ps, cs, rs) -> {
                        ps.hashCode();
                    }), closer);
                }
            }
        }));

        var timePub = CachePublisherTrackingFactory.Uninstrumented.create(TimeTableDescriptor.Descriptor);

        var timer = ExecutorUtil.dedicated("TimeJoiner");

        CachePublisher<UserTime, ?> publisher =
          CachePublisherTrackingFactory.Uninstrumented.create(UserTimeTableDescriptor.Descriptor);
        UserTimeJoinBuilderFactory.createMutable((CachePublisher) publisher,
          timer,
          (c) -> {},
          pub.getTable(),
          timePub.getTable());
        ExecutorUtil.scheduleWithFixedDelay(timer, () -> {
            timePub.publish(TimeBuilder.newBuilder().timeKey("currentTime").time(Instant.now()).build());
        }, 1, TimeUnit.SECONDS);
    }

    private static ScreenProviderFactory createScreenProviderFactory(ApplicationContext ctx) {
        return new SimpleScreenProviderFactory(new StarterScreenProvider("Starter",
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
}
