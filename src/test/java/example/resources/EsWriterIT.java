package example.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import example.dto.CustomerCohort;
import example.dto.EsIndex;
import org.junit.Test;

import java.util.UUID;

import static org.awaitility.Awaitility.await;

public class EsWriterIT {
    @Test
    public void reactToOnRebuildAndSuccessEvent() {
        ZookeeperPersistanceLayer zookeeperPersistanceLayer = new ZookeeperPersistanceLayer();
        EsIndex newIndex = new EsIndex("new cluster", ImmutableList.of("host1"), "new Index");
        CustomerCohort cohort = new CustomerCohort(UUID.randomUUID(), ImmutableSet.of(1,2), CustomerCohort.State.CREATED, newIndex);

        zookeeperPersistanceLayer.createCohort(cohort);
        EsWriter writer = new EsWriter(zookeeperPersistanceLayer);

        cohort.setState(CustomerCohort.State.REBUILDING);
        zookeeperPersistanceLayer.updateCohort(cohort);

        // one connection for each EsIndex
        await().until(() -> writer.candidateCustomerIndexes.size() == 2);

        cohort.setState(CustomerCohort.State.SUCCESS);
        zookeeperPersistanceLayer.updateCohort(cohort);

        await().until(() -> writer.candidateCustomerIndexes.size() == 0);
    }

    @Test
    public void reactToOnRebuildAndFailureEvent() {
        ZookeeperPersistanceLayer zookeeperPersistanceLayer = new ZookeeperPersistanceLayer();
        EsIndex newIndex = new EsIndex("new cluster", ImmutableList.of("host1"), "new Index");
        CustomerCohort cohort = new CustomerCohort(UUID.randomUUID(), ImmutableSet.of(1,2), CustomerCohort.State.CREATED, newIndex);

        zookeeperPersistanceLayer.createCohort(cohort);
        EsWriter writer = new EsWriter(zookeeperPersistanceLayer);

        cohort.setState(CustomerCohort.State.REBUILDING);
        zookeeperPersistanceLayer.updateCohort(cohort);

        // one connection for each EsIndex
        await().until(() -> writer.candidateCustomerIndexes.size() == 2);

        cohort.setState(CustomerCohort.State.FAILED);
        zookeeperPersistanceLayer.updateCohort(cohort);

        await().until(() -> writer.candidateCustomerIndexes.size() == 0);
    }
}
