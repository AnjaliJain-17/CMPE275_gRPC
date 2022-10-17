package gash.grpc.route.server;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.xds.shaded.io.envoyproxy.pgv.validate.Validate.BoolRules;
import route.Route;
import route.RouteServiceGrpc;



public class WorkAllocator implements Runnable {

    private BlockingQueue<Route> queue;

    ManagedChannel ch = null;
    RouteServiceGrpc.RouteServiceBlockingStub stub = null;
    int currIndex = 1;
    Map<String,Integer> map = ServerConfiguration.configMap;
    
    boolean isRunning = false;

    public WorkAllocator(BlockingQueue<Route> queue){
        this.queue=queue;
        setupNextServer();
    }

    @Override
    public void run() {

        while(!isRunning){
            try {
            if(queue.size()>0){
                Route content   = queue.poll();
                var r = stub.request(content);
                response(r);
                if(r.getCanAcceptMore()==0){
                    if(!setupNextServer()){
                        System.out.println("Can't accept more request!!! Stopping the leader");
                        isRunning = false;
                        Thread.currentThread().interrupt();
                    };
                }
            }
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        }
        
    }

    private static final void response(Route reply) {
		var payload = new String(reply.getPayload().toByteArray());
      
		System.out.println("reply: " + reply.getId() + ", from: " + reply.getOrigin() + ", payload: " + payload);
	}
    

    private boolean setupNextServer(){
        if(currIndex > map.size()){
            return false;
        }
        ch = ManagedChannelBuilder.forAddress("localhost", map.get(String.valueOf(currIndex))).usePlaintext().build();
        stub = RouteServiceGrpc.newBlockingStub(ch);
        System.out.print("switching to server "+map.get(String.valueOf(currIndex)));
        currIndex++;
        return true;
    }
    
}
