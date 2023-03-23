package grpc;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ServiceServer {

    public static void main(String[] args) {
        Server server = Grpc.newServerBuilderForPort(9091, InsecureServerCredentials.create())
                .addService(new ServiceHandler())
                .build();

        try {
            server.start();
            System.out.println("**** Server started");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.err.println("*** Added a shutdown hook, meaning this method gets called right before JVM shuts down");
                try {
                    server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
            }));
            server.awaitTermination();
            System.out.println("**** Server shut down");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }
}
