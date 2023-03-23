package thrift.middleservice;

public class MiddleServiceHandler implements MiddleService.Iface {

    @Override
    public String someMethod(String parameter) {
        return parameter;
    }
}
