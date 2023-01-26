package example;

import example.resources.EsWriter;
import example.resources.EventSource;
import example.resources.ZookeeperPersistanceLayer;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class zookeeperWatcherApiApplication extends Application<zookeeperWatcherApiConfiguration> {

    public static void main(final String[] args) throws Exception {
        new zookeeperWatcherApiApplication().run(args);
    }

    @Override
    public String getName() {
        return "zookeeperWatcherApi";
    }

    @Override
    public void initialize(final Bootstrap<zookeeperWatcherApiConfiguration> bootstrap) {
    }

    @Override
    public void run(final zookeeperWatcherApiConfiguration configuration,
                    final Environment environment) {
        ZookeeperPersistanceLayer persistanceLayer = new ZookeeperPersistanceLayer();
        EsWriter writer = new EsWriter(persistanceLayer);
        EventSource eventSource = new EventSource(persistanceLayer);
        environment.jersey().register(eventSource);
        environment.jersey().register(writer);
    }

}
