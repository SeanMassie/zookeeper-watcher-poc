package example.resources;

import example.dto.CustomerCohort;
import example.dto.EsIndex;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.HashMap;
import java.util.Map;

@Path("es-writer")
public class EsWriter {

    protected Map<Integer, EsIndex> candidateCustomerIndexes;

    private ZookeeperPersistanceLayer persistanceLayer;
    public EsWriter(ZookeeperPersistanceLayer persistanceLayer) {
        this.candidateCustomerIndexes = new HashMap<>();
        this.persistanceLayer = persistanceLayer;
        persistanceLayer.onStateChange(CustomerCohort.State.REBUILDING, this::onCohortRebuild);
        persistanceLayer.onStateChange(CustomerCohort.State.SUCCESS, this::onCohortSuccess);
        persistanceLayer.onStateChange(CustomerCohort.State.FAILED, this::onCohortFailure);
    }

    public void onCohortRebuild(final CustomerCohort cohort) {
        for(int id : cohort.getCustomerIds()) {
            candidateCustomerIndexes.put(id, cohort.getNewIndex());
        }
    }

    public void onCohortSuccess(final CustomerCohort cohort) {
        for(int id : cohort.getCustomerIds()) {
            candidateCustomerIndexes.remove(id);
        }
    }

    public void onCohortFailure(final CustomerCohort cohort) {
        for(int id : cohort.getCustomerIds()) {
            candidateCustomerIndexes.remove(id);
        }
    }

    public Map<Integer, EsIndex> getCandidateCustomerIndexes() {
        return this.candidateCustomerIndexes;
    }

    @GET
    @Path("/{cohortId}")
    public EsIndex getCandidateIndexByCustomerid(@PathParam("cohortId") int id) {
       return this.candidateCustomerIndexes.get(id);
    }
}
