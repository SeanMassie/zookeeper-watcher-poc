package example.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import example.dto.CustomerCohort;
import example.dto.EsIndex;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class EsSourceIT {
    @Test
    public void createAndGetCohortFromZookeeper() {
        ZookeeperPersistanceLayer zookeeperPersistanceLayer = new ZookeeperPersistanceLayer();
        EsIndex newIndex = new EsIndex("new cluster", ImmutableList.of("host1"), "new Index");
        CustomerCohort cohort = new CustomerCohort(UUID.randomUUID(), ImmutableSet.of(1,2), CustomerCohort.State.CREATED, newIndex);

        zookeeperPersistanceLayer.createCohort(cohort);
        Optional<CustomerCohort> responseCohort = zookeeperPersistanceLayer.getCustomerCohort(cohort.getCohortId());

        assertTrue(responseCohort.isPresent());
    }
}
