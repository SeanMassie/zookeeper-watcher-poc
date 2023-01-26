package example.dto;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CustomerCohort {
    public enum State {
        /**
         * A new cohort, and any dependent resources are being instantiated, prior
         * to rebuilds starting. IE - this is where we'd probably create new ES indices.
         */
        CREATING,

        /**
         * A new cohort rebuild has been created, along with any corresponding dependent
         * resources. We are now ready to start a rebuild.
         */
        CREATED,

        /**
         * The cohort is in the process of being rebuilt. Do we want progress indicators somehow?
         */
        REBUILDING,

        /**
         * The rebuild errored out during its rebuild process. It is allowed to transition
         * either back to a REBUILDING state, or to a FAILED state.
         */
        ERROR,

        /**
         * The rebuild failed irrecoverably, and is in a terminal state. It should make no more
         * state changes from here on out.
         */
        FAILED,

        /**
         * The rebuild finished successfully, and is in a terminal state. It should make no more
         * state changes from here on out.
         */
        SUCCESS,
    }

    private UUID cohortId;

    private Set<Integer> customerIds;

    private State state;

    private EsIndex newIndex;

    public CustomerCohort() {
    }
    public CustomerCohort(UUID cohortId,
                          Set<Integer> customerIds,
                          State state,
                          EsIndex newIndex) {
        this.cohortId = cohortId;
        this.customerIds = customerIds;
        this.state = state;
        this.newIndex = newIndex;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public void setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
    }

    public Set<Integer> getCustomerIds() {
        return customerIds;
    }

    public void setCustomerIds(Set<Integer> customerIds) {
        this.customerIds = customerIds;
    }

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        this.state = newState;
    }

    public EsIndex getNewIndex() {
        return this.newIndex;
    }

    public void setNewIndex(EsIndex newIndex) {
        this.newIndex = newIndex;
    }
}

