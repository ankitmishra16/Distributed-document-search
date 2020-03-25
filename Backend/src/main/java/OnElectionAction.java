import Networking.WebClient;
import cluster_managment.OnElectionCallback;
import Cluster_managment.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import worker.worker;
import worker.CoordinatorNode ;
import Networking.Server ;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback{
    private final ServiceRegistry serviceRegistry ;
    private final ServiceRegistry coordinatorServiceRegistry ;
    private final int port;
    private Server webServer;

    public OnElectionAction(ServiceRegistry serviceRegistry, ServiceRegistry coordinatorServiceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.coordinatorServiceRegistry = coordinatorServiceRegistry;
        this.port = port;
    }

    @Override
    public void OnelectedAsLeader() throws KeeperException, InterruptedException {
        serviceRegistry.UnregisterFromCluster();
//        serviceRegistry.updateAddresses();

        if (webServer != null) {
            webServer.stop();
        }

        CoordinatorNode searchCoordinator = new CoordinatorNode(this.serviceRegistry, new WebClient());
        webServer = new Server(port, searchCoordinator);
        webServer.StartServer();

        try {
            String currentServerAddress =
                    String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, searchCoordinator.getEndPoint());
            coordinatorServiceRegistry.RegisterToCluster(currentServerAddress);
        } catch (InterruptedException | UnknownHostException | KeeperException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void AsWorker() {
        worker searchWorker = new worker();
        if (webServer == null) {
            webServer = new Server(port, searchWorker);
            webServer.StartServer();
        }

        try {
            String currentServerAddress =
                    String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, searchWorker.getEndPoint());

            serviceRegistry.RegisterToCluster(currentServerAddress);
        } catch (InterruptedException | UnknownHostException | KeeperException e) {
            e.printStackTrace();
            return;
        }

    }


}
