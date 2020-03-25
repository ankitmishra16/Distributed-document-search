package Networking;

import org.apache.zookeeper.KeeperException;

public interface ONRequestCallBack {

    byte[] handleRequest( byte[] requestPayload) throws KeeperException, InterruptedException;

    String getEndPoint() ;
}
