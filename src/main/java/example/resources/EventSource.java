package example.resources;

import example.dto.CustomerCohort;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("event")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventSource {
    private ZookeeperPersistanceLayer persistanceLayer;

    public EventSource(ZookeeperPersistanceLayer persistanceLayer) {
        this.persistanceLayer = persistanceLayer;
    }

    @POST
    public CustomerCohort createEvent(CustomerCohort cohort) {
        return this.persistanceLayer.createCohort(cohort).orElseThrow(NotFoundException::new);
    }

    @PUT
    public CustomerCohort updateEvent(CustomerCohort cohort) {
        return this.persistanceLayer.updateCohort(cohort).orElseThrow(NotFoundException::new);
    }
}
