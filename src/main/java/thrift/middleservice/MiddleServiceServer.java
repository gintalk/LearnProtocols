package thrift.middleservice;

import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public class MiddleServiceServer {

    public static void main(String[] args) {
        simpleServer();
    }

    public static void simpleServer() {
        try {
            TServerTransport serverTransport = new TServerSocket(9090);
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(new MiddleService.Processor<>(new MiddleServiceHandler())));

            server.serve();
        } catch (TException e) {
            e.printStackTrace(System.err);
        }
    }
}
