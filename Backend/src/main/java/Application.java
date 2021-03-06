import Cluster_managment.LeaderElection;
import Cluster_managment.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Application implements Watcher {
    public static final String ZOOKEEPER_ADDRESS = "localhost:2181" ;
    public static final int SESSION_TIMEOUT = 3000 ;
    private ZooKeeper zooKeeper ;
    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        int port = args.length == 1 ? Integer.parseInt(args[0]) : 8080 ;

        Application application = new Application() ;

        ZooKeeper zooKeeper = application.connectToZookeeper() ;

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.WORKERS_REGISTRY_ZNODE) ;
        ServiceRegistry coordinatorServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.COORDINATORS_REGISTRY_ZNODE) ;
        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, coordinatorServiceRegistry, port) ;

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership();
        leaderElection.electLeader();

        application.run();
        application.close();
        System.out.println("Disconnected from Zookeeper, exiting application");


    }

    public ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this) ;
        return this.zooKeeper ;
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to Zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from Zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
