package Networking ;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class Server {

    private static final String TASK_ENDPOINT = "/task" ;
    private static final  String STATUS_ENDPOINT = "/status" ;

    private final int PORT ;
    private final ONRequestCallBack onRequestCallback;
    private HttpServer server ;

    public Server(int port, ONRequestCallBack onRequestCallback){
        this.onRequestCallback = onRequestCallback;
        this.PORT = port ;
    }

    public void StartServer(){
        try {
            this.server = HttpServer.create(new InetSocketAddress(this.PORT), 0) ;
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }

        HttpContext taskContext = server.createContext(TASK_ENDPOINT) ;
        HttpContext statusContext = server.createContext(STATUS_ENDPOINT) ;

        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start() ;
    }

    public void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }

        byte[] responseBytes = new byte[0];
        try {
            System.out.println("ON coordinator node, requested string is : "+exchange.getRequestBody());
            responseBytes = onRequestCallback.handleRequest(exchange.getRequestBody().readAllBytes());
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendResponse(responseBytes, exchange);
    }


    public void handleStatusCheckRequest(HttpExchange exchange){
        if(!exchange.getRequestMethod().equalsIgnoreCase("get"))
        {
            exchange.close();
            return ;
        }

        String responseString = "Server is alive!!!\n" ;
        sendResponse(responseString.getBytes(), exchange) ;
    }

    public void sendResponse(byte[] responseString, HttpExchange exchange){
        try {
            exchange.sendResponseHeaders(200, responseString.length);
            OutputStream out = exchange.getResponseBody() ;
            out.write(responseString);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        server.stop(10);
    }
}
