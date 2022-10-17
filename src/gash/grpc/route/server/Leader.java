package gash.grpc.route.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


import com.google.protobuf.ByteString;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import route.RouteServiceGrpc.RouteServiceImplBase;
import route.Route;

/**
 * copyright 2021, gash
 *
 * Gash licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class Leader extends RouteServiceImplBase {

	private static BlockingQueue<Route> storage = new LinkedBlockingDeque<>();

	private Server svr;


	/**
	* Configuration of the server's identity, port, and role
	*/
	private static Properties getConfiguration(final File path) throws IOException {
		if (!path.exists())
			throw new IOException("missing file");

		Properties rtn = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
			rtn.load(fis);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		return rtn;
	}

	/**
	 * TODO refactor this!
	 * 
	 * @param path
	 * @param payload
	 * @return
	 * @throws InterruptedException
	 */
	protected ByteString process(route.Route msg) throws InterruptedException {

		// // TODO placeholder
		// String content = new String(msg.getPayload().toByteArray());
		// System.out.println("-- got: " + msg.getOrigin() + ", path: " + msg.getPath() + ", with: " + content);

		storage.put(msg);
		// TODO complete processing
		final String blank = "blank";
		byte[] raw = blank.getBytes();

		return ByteString.copyFrom(raw);
	}

	public static void main(String[] args) throws Exception {
		// TODO check args!
        String path = args[0];

		Thread thread = new Thread(new WorkAllocator(storage));
     
		try {
			Properties conf = Leader.getConfiguration(new File(path));
       
			RouteServer.configure(conf);

			/* Similar to the socket, waiting for a connection */
			final Leader impl = new Leader();
			impl.start();
			thread.start();
			impl.blockUntilShutdown();
    
        
		} catch (IOException e) {
			// TODO better error message
			e.printStackTrace();}
	
	
	}

	private void start() throws Exception {
		svr = ServerBuilder.forPort(RouteServer.getInstance().getServerPort()).addService(new Leader())
				.build();			
		System.out.println("-- starting leader server");
		svr.start();

      
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Leader.this.stop();
			}
		});
	}

	protected void stop() {
		svr.shutdown();
	}

	private void blockUntilShutdown() throws Exception {
		/* TODO what clean up is required? */
		svr.awaitTermination();
	}

	/**
	 * server received a message!
	 */
	@Override
	public void request(route.Route request, StreamObserver<route.Route> responseObserver) {

		// TODO refactor to use RouteServer to isolate implementation from
		// transportation

		route.Route.Builder builder = route.Route.newBuilder();

		// routing/header information
		builder.setId(RouteServer.getInstance().getNextMessageID());
		builder.setOrigin(RouteServer.getInstance().getServerID());
		builder.setDestination(request.getOrigin());
		builder.setPath(request.getPath());

		// do the work and reply
		try {
			builder.setPayload(process(request));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		route.Route rtn = builder.build();
		responseObserver.onNext(rtn);
		responseObserver.onCompleted();
	}
}
