package grpc;

import io.grpc.stub.StreamObserver;
import utils.StringUtils;

import java.util.concurrent.TimeoutException;

public class ServiceHandler extends GRPCServiceGrpc.GRPCServiceImplBase {

    @Override
    public void get(GetRequest request, StreamObserver<GetResponse> observer) {
        System.out.println("** Got a request: " + request.getParam().substring(0, 10));
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
        observer.onNext(GetResponse.newBuilder().setRet(request.getParam()).build());
        observer.onCompleted();
        System.out.println("*** Request handled: " + request.getParam().substring(0, 10));
    }

    @Override
    public void serverStreamGet(GetRequest request, StreamObserver<GetResponse> observer) {
        System.out.println("** Got a request: " + request.getParam().substring(0, 10));

        String[] split = StringUtils.split(request.getParam(), 20);
        int error = 0;
        for (String s : split) {
            observer.onNext(GetResponse.newBuilder().setRet(s).build());
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }

            error++;
            if (error == 50) {
                observer.onError(new TimeoutException("Long story bro"));
            }
        }

        observer.onCompleted();
        System.out.println("*** Request handled: " + request.getParam().substring(0, 1000));
    }

    @Override
    public StreamObserver<GetRequest> clientStreamGet(StreamObserver<GetResponse> observer) {
        return new StreamObserver<>() {
            final StringBuilder sb = new StringBuilder();

            @Override
            public void onNext(GetRequest value) {
                System.out.println("** Got a request: " + value.getParam().substring(0, 10));
                sb.append(value.getParam());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace(System.err);
            }

            @Override
            public void onCompleted() {
                observer.onNext(GetResponse.newBuilder().setRet(sb.toString()).build());
                observer.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GetRequest> biStreamGet(StreamObserver<GetResponse> observer) {
        return new StreamObserver<>() {

            int counter = 0;

            @Override
            public void onNext(GetRequest value) {
                if (counter == 20){
                    onCompleted();
                    return;
                }
                observer.onNext(GetResponse.newBuilder().setRet("** " + counter++ + " ** " + value.getParam()).build());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace(System.err);
            }

            @Override
            public void onCompleted() {
                observer.onCompleted();
            }
        };
    }
}
