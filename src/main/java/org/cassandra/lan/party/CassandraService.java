package org.cassandra.lan.party;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CassandraService {

    @Value("${ip.cassandra}")
    private String cassandraIp;

    private Cluster cluster = null;

    private Cluster getCluster() {
        if (cluster == null) {
            try {
                Cluster.Builder clusterBuilder = Cluster.builder()
                        .withoutJMXReporting()
                        .withReconnectionPolicy(new ConstantReconnectionPolicy(1000l));
                clusterBuilder.addContactPoints(cassandraIp);
                cluster = clusterBuilder.build();
                cluster.getMetadata();
            } catch (NoHostAvailableException e) {
                cluster = null;
            }
        }
        return cluster;
    }

    public Set<Host> getHosts() {
        Cluster theCluster = getCluster();
        if (theCluster == null) {
            return new HashSet<>();
        } else {
            return theCluster.getMetadata().getAllHosts();
        }
    }

}
