package example;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;


/**
 * These test go through simple use cases with the apache curator library
 */
public class ZookeeperUseCaseExamplesIT {

    @Test
    public void setAndGetDataFromZookeeper() throws Exception {
        int sleepMsBetweenRetries = 100;
        int maxRetries = 3;
        RetryPolicy retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();

        if (client.checkExists().forPath("/head") == null) {
            client.create().forPath("/head", "testing".getBytes());
        } else {
            client.setData().forPath("/head", "testing".getBytes());
        }
        String output = new String(client.getData().forPath("/head"));
        assertTrue("testing".equals(output));
    }

    @Test
    public void singleWatcherExample() {
        int sleepMsBetweenRetries = 100;
        int maxRetries = 3;
        RetryPolicy retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();
        AsyncCuratorFramework async = AsyncCuratorFramework.wrap(client);
        String key = "/test";
        String expected = "my_value";

        async.create().forPath(key);

        List<String> changes = new ArrayList<>();

        async.watched()
                .getData()
                .forPath(key)
                .event()
                .thenAccept(watchedEvent -> {
                    try {
                        String s = new String(client.getData().forPath(watchedEvent.getPath()));
                        System.out.println(s);
                        changes.add(s);
                    } catch (Exception e) {
                        // fail ...
                    }});

        async.setData().forPath(key, expected.getBytes());

        await().until(() -> changes.size() == 1);
    }

    @Test
    public void multihitWatcherExample() throws Exception {
        int sleepMsBetweenRetries = 100;
        int maxRetries = 3;
        String key = "/testing";
        String expected = "my_value";
        RetryPolicy retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();

        if (client.checkExists().forPath(key) == null) {
           client.create().forPath(key);
        }

        List<String> changes = new ArrayList<>();
        NodeCache nodeCache = new NodeCache(client, key);
        nodeCache.start();

        NodeCacheListener listener = () -> {
                changes.add("test");
                System.out.println("hit");
            };
        nodeCache.getListenable().addListener(listener);

        client.setData().forPath(key, expected.getBytes());
        await().until(() -> changes.size() == 1);

        client.setData().forPath(key, expected.getBytes());
        await().until(() -> changes.size() == 2);

        nodeCache.close();
    }

    @Test
    public void multiHitChildWatcher() throws Exception {
        int sleepMsBetweenRetries = 100;
        int maxRetries = 3;
        RetryPolicy retryPolicy = new RetryNTimes(maxRetries, sleepMsBetweenRetries);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
        client.start();

        if (client.checkExists().forPath("/test") == null) {
            client.create().forPath("/test");
        }

        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/test", false);
        pathChildrenCache.start();
        List<String> updatedPaths = new ArrayList<>();

        pathChildrenCache.getListenable().addListener((curator, event) -> {
            updatedPaths.add(event.getData().getPath());
        });

        if (client.checkExists().forPath("/test/first-record") == null) {
            client.create().forPath("/test/first-record");
        }

        client.setData().forPath("/test/first-record", "firstUpdate".getBytes());

        await().until(() -> updatedPaths.contains("/test/first-record"));

        if (client.checkExists().forPath("/test/second-record") == null) {
            client.create().forPath("/test/second-record");
        }

        client.setData().forPath("/test/second-record", "firstUpdate".getBytes());

        await().until(() -> updatedPaths.contains("/test/second-record"));
    }
}
