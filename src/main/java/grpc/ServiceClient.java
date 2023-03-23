package grpc;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Phaser;

public class ServiceClient {

    private final GRPCServiceGrpc.GRPCServiceBlockingStub BLOCKING_STUB;
    private final GRPCServiceGrpc.GRPCServiceStub ASYNC_STUB;
    private final GRPCServiceGrpc.GRPCServiceFutureStub FUTURE_STUB;
    private final Phaser PHASER;

    private ServiceClient() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9091).usePlaintext().build();

        BLOCKING_STUB = GRPCServiceGrpc.newBlockingStub(channel);
        ASYNC_STUB = GRPCServiceGrpc.newStub(channel);
        FUTURE_STUB = GRPCServiceGrpc.newFutureStub(channel);
        PHASER = new Phaser(1);
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ServiceClient client = new ServiceClient();
        GetRequest request = GetRequest.newBuilder().setParam(read()).build();
        client.biStreamAsyncGet(request);
    }

    private static String read() {
        try {
            Path file = Paths.get("src/main/java/thrift/middleservice/input.json");
            BufferedReader br = Files.newBufferedReader(file);

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return "Err";
        }
    }

    private void blockingGet(GetRequest request) {
        long time = System.nanoTime();
        String method = "blocking get";
        System.out.printf("** Start %s\n", method);

        PHASER.arriveAndAwaitAdvance();
        GetResponse response = BLOCKING_STUB.get(request);

        System.out.printf("*** Done %s, elapsed: %d\n", method, ((System.nanoTime() - time) / 1000000000));
        System.out.println(response.getRet().substring(0, 10));
    }

    private void asyncGet(GetRequest request) {
        long time = System.nanoTime();
        String method = "async get";
        System.out.printf("** Start %s\n", method);

        PHASER.register();
        ASYNC_STUB.get(request, new ServiceCallback());
        PHASER.arriveAndAwaitAdvance();

        System.out.printf("*** Done %s, elapsed: %d\n", method, ((System.nanoTime() - time) / 1000000000));
    }

    private void futureGet(GetRequest request) throws ExecutionException, InterruptedException {
        long time = System.nanoTime();
        String method = "future get";
        System.out.printf("** Start %s\n", method);

        ListenableFuture<GetResponse> future = FUTURE_STUB.get(request);

        System.out.printf("*** Done %s, elapsed: %d\n", method, ((System.nanoTime() - time) / 1000000000));

        System.out.println(future.get().getRet());
    }

    private void serverStreamBlockingGet(GetRequest request) {
        long time = System.nanoTime();
        String method = "server streaming blocking get";
        System.out.printf("** Start %s\n", method);

        Iterator<GetResponse> iterator = BLOCKING_STUB.serverStreamGet(request);
        while (iterator.hasNext()) {
            System.out.println(iterator.next().getRet());
        }

        System.out.printf("*** Done %s, elapsed: %d\n", method, ((System.nanoTime() - time) / 1000000000));
    }

    private void serverStreamAsyncGet(GetRequest request) {
        long time = System.nanoTime();
        String method = "server streaming async get";
        System.out.printf("** Start %s\n", method);

        PHASER.register();
        ASYNC_STUB.serverStreamGet(request, new ServiceCallback());

        System.out.printf("*** Done %s, elapsed: %d\n", method, ((System.nanoTime() - time) / 1000000000));

        PHASER.arriveAndAwaitAdvance();
    }

    private void clientStreamAsyncGet(GetRequest request) {
        String[] split = StringUtils.split(request.getParam(), 20);

        long time = System.nanoTime();
        String method = "client streaming async get";
        System.out.printf("** Start %s\n", method);

        PHASER.register();

        StreamObserver<GetRequest> observer = ASYNC_STUB.clientStreamGet(new ServiceCallback());
        for (String s : split) {
            observer.onNext(GetRequest.newBuilder().setParam(s).build());
        }
        observer.onCompleted();

        System.out.printf("*** Done %s, elapsed: %d\n", method, ((System.nanoTime() - time) / 1000000000));

        PHASER.arriveAndAwaitAdvance();
    }

    private void biStreamAsyncGet(GetRequest request) {
        String[] split = StringUtils.split(request.getParam(), 20);

        long time = System.nanoTime();
        String method = "client streaming async get";
        System.out.printf("** Start %s\n", method);

        PHASER.register();

        StreamObserver<GetRequest> observer = ASYNC_STUB.biStreamGet(new ServiceCallback());
        for (String s : split) {
            observer.onNext(GetRequest.newBuilder().setParam(s).build());
        }
        observer.onCompleted();

        System.out.printf("*** Done %s, elapsed: %d\n", method, ((System.nanoTime() - time) / 1000000000));

        PHASER.arriveAndAwaitAdvance();
    }

    private class ServiceCallback implements StreamObserver<GetResponse> {
        @Override
        public void onNext(GetResponse value) {
            System.out.println(Thread.currentThread().getName() + " " + value.getRet());
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace(System.err);
            PHASER.arriveAndDeregister();
        }

        @Override
        public void onCompleted() {
            System.out.println("----");
            PHASER.arriveAndDeregister();
        }
    }
}
