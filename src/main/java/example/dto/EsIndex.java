package example.dto;

import java.util.List;
import java.util.UUID;

public class EsIndex {
    private String clusterName;
    private List<String> esClusterHosts;
    private String indexName;

    public EsIndex() {}

    public EsIndex(final String clusterName,
                   final List<String> esClusterHosts,
                   final String indexName) {
        this.clusterName = clusterName;
        this.esClusterHosts = esClusterHosts;
        this.indexName = indexName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<String> getEsClusterHosts() {
        return esClusterHosts;
    }

    public void setEsClusterHosts(List<String> esClusterHosts) {
        this.esClusterHosts = esClusterHosts;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }


}
