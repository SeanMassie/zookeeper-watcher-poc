package example;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;
import javax.validation.constraints.*;
import javax.validation.constraints.NotEmpty;

public class zookeeperWatcherApiConfiguration extends Configuration {
    @NotEmpty
    private String defaultState = "testing";

    @JsonProperty
    public String getDefaultState() {
        return this.defaultState;
    }

    @JsonProperty
    public void setDefaultState(String state) {
        this.defaultState= state;
    }
}
