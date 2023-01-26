package example.resources;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import example.dto.CustomerCohort;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.RetryNTimes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class ZookeeperPersistanceLayer {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperPersistanceLayer.class);
    private CuratorFramework client;
    private String zookeeperPath = "/cohorts";
    private ObjectMapper mapper;

    private ListMultimap<CustomerCohort.State, Consumer<CustomerCohort>> subscribedCallbacks;

    public ZookeeperPersistanceLayer() {
        this.subscribedCallbacks = Multimaps.synchronizedListMultimap(Multimaps.newListMultimap(new HashMap<>(), ArrayList::new));
        int sleepMsBetweenRetries = 100;
        int maxRetries = 3;
        RetryPolicy retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries);
        this.client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();
        this.mapper = new ObjectMapper();

        try {
            if (this.client.checkExists().forPath(this.zookeeperPath) == null) {
                this.client.create().forPath(this.zookeeperPath);
            }

            PathChildrenCache pathChildrenCache = new PathChildrenCache(client, this.zookeeperPath, false);
            pathChildrenCache.start();
            pathChildrenCache.getListenable().addListener(this::nodeCacheListener);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void onStateChange(CustomerCohort.State state, Consumer<CustomerCohort> callback) {
        this.subscribedCallbacks.get(state).add(callback);
    }

    private void nodeCacheListener(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) {
        Optional<CustomerCohort> optionalCohort = Optional.empty();
        try {
           byte[] cohortBytes = curatorFramework.getData().forPath(pathChildrenCacheEvent.getData().getPath());
           optionalCohort = Optional.of(this.mapper.readValue(new String(cohortBytes), CustomerCohort.class));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (optionalCohort.isPresent())  {
            CustomerCohort cohort = optionalCohort.get();
            this.subscribedCallbacks.get(cohort.getState()).forEach(callback -> callback.accept(cohort));
        }
    }
    public Optional<CustomerCohort> getCustomerCohort(UUID cohortId) {
        Optional<CustomerCohort> cohort = Optional.empty();
        try {
            if (this.checkIfChildNodeExistsByUUID(cohortId)) {
                byte[] cohortBytes = this.client.getData().forPath(this.zookeeperPath);
                cohort = Optional.of(this.mapper.readValue(new String(cohortBytes), CustomerCohort.class));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return cohort;
    }

    public Optional<CustomerCohort> createCohort(CustomerCohort cohort) {
        Optional<CustomerCohort> createdCohort = Optional.empty();
        try {
            if (!this.checkIfChildNodeExistsByUUID(cohort.getCohortId())){
                this.client.create().forPath(this.buildZookeeperChildPath(cohort.getCohortId()));
                this.setCohortData(cohort);
                createdCohort = Optional.of(cohort);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return createdCohort;
    }

    public Optional<CustomerCohort> updateCohort(CustomerCohort cohort) {
        Optional<CustomerCohort> updatedCohort = Optional.empty();
        try {
            if (this.checkIfChildNodeExistsByUUID(cohort.getCohortId())){
                this.setCohortData(cohort);
                updatedCohort = Optional.of(cohort);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return updatedCohort;
    }

    private boolean setCohortData(CustomerCohort cohort) {
        boolean cohortUpdated = false;
        try {
            String cohortJson = this.mapper.writeValueAsString(cohort);
            this.client.setData().forPath(this.buildZookeeperChildPath(cohort.getCohortId()), cohortJson.getBytes());
            cohortUpdated = true;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return cohortUpdated;
    }

    private boolean checkIfChildNodeExistsByUUID(UUID cohortId) throws Exception {
        return this.client.checkExists().forPath(this.buildZookeeperChildPath(cohortId)) != null;
    }

    private String buildZookeeperChildPath(UUID uuid) {
        return this.zookeeperPath.concat("/").concat(uuid.toString());
    }

}
