package thrift.middleservice;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MiddleServiceClient {

    public static void main(String[] args) {
        simpleClient();
    }

    public static void simpleClient() {
        try {
            TTransport transport = new TSocket("localhost", 9090);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            MiddleService.Client client = new MiddleService.Client(protocol);

            Path file = Paths.get("src/main/java/thrift/middleservice/input.json");
            BufferedReader br = Files.newBufferedReader(file);

            String line;
            StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null){
                sb.append(line);
            }

            client.someMethod(sb.toString());

            transport.close();
        } catch (TException | IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
