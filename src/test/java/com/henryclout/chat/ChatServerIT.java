package com.henryclout.chat;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.henryclout.chat.server.ClientConnectionManager;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class ChatServerIT {

	@Autowired
	private ClientConnectionManager clientConnectionManager;
	
	private static final long WAIT_FOR_CONNECT_TIMEOUT_MS = 5000L;
	private static final int NUMBER_PARALLEL_CONNECTIONS = 10;
	private static final String TEST_MESSAGE = "a test message";
	
	// FIXME: server port should be dynamically allocated.
	private int serverPort = 8081;
	
	@After
	public void waitForConnectionClose() {
		// Wait for connection clean up to prevent treading
		// on the toes of other tests.
        waitForActiveConnectionCount(0);
	}
	
    @Test
    public void testConnect() throws Exception {
        Socket socket = new Socket("localhost", serverPort);
        socket.close();
    }

    @Test
    public void testMessageSend() throws Exception {
        Socket socket = new Socket("localhost", serverPort);
        
        waitForActiveConnectionCount(1);
        
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println(TEST_MESSAGE);
        printWriter.flush();
        printWriter.close();
        socket.close();
    }

    @Test
    public void testMultipleConnect() throws Exception {
    	List<Socket> clientSockets = connectMultipleClients();
    	
    	// Close them all.
    	for (Socket clientSocket : clientSockets) {
    		clientSocket.close();
    	}
    }

    @Test
    public void testMessageReceive() throws Exception {
        Socket clientOneSocket = new Socket("localhost", serverPort);
        PrintWriter clientOnePrintWriter = new PrintWriter(clientOneSocket.getOutputStream());

        Socket clientTwoSocket = new Socket("localhost", serverPort);
        Scanner clientTwoScanner = new Scanner(clientTwoSocket.getInputStream());

        waitForActiveConnectionCount(2);
        
        // Send message, check receipt.
        clientOnePrintWriter.println(TEST_MESSAGE);
        clientOnePrintWriter.flush();
        
        if (clientTwoScanner.hasNext()) {
            assertThat(TEST_MESSAGE, equalTo(clientTwoScanner.nextLine()));
        } else {
        	fail("Failed to receive message on second client.");
        }

        clientTwoScanner.close();
        clientOneSocket.close();
        clientTwoSocket.close();
    }
    
    @Test
    public void testMultipleMessageReceive() throws Exception {
    	List<TestClient> testClients = new LinkedList<>();
    	for (Socket socket : connectMultipleClients()) {
    		testClients.add(new TestClient(socket));
    	}

    	waitForActiveConnectionCount(NUMBER_PARALLEL_CONNECTIONS);

    	for (TestClient testClientSender : testClients) {
    		testClientSender.send(TEST_MESSAGE);
    		for (TestClient testClientReceiver : testClients) {
    			if (testClientSender != testClientReceiver) {
    				testClientReceiver.assertReceived(TEST_MESSAGE);
    			}
    		}
    	}

    	// Close them all.
    	for (TestClient testClient : testClients) {
    		testClient.close();
    	}
    }
    
    private List<Socket> connectMultipleClients() throws UnknownHostException, IOException {
    	List<Socket> clientSockets = new LinkedList<>();
    	for (int i = 0; i < NUMBER_PARALLEL_CONNECTIONS; i++) {
    		clientSockets.add(new Socket("localhost", serverPort));
    	}
    	return clientSockets;
    }
    
    /**
     * Wait for the server to register that the given connection count
     * is active, i.e. fully initialised.
     */
    private void waitForActiveConnectionCount(int activeConnectionCount) {
    	long startTimeMs = System.currentTimeMillis();
    	while (clientConnectionManager.getActiveConnectionCount() != activeConnectionCount) {
    		try {
    			Thread.sleep(100L);
    		} catch (InterruptedException ie) {
    			// Ignore.
    		}
    		if ((System.currentTimeMillis() - startTimeMs) > WAIT_FOR_CONNECT_TIMEOUT_MS) {
    			fail("Failed to initialise connections to server after: " + WAIT_FOR_CONNECT_TIMEOUT_MS + "ms.");
    		}
    	}
    }
    
    private static class TestClient {
    	private Socket socket;
    	private PrintWriter printWriter;
    	private Scanner scanner;
    	
    	TestClient(Socket socket) throws IOException {
    		this.socket = socket;
    		this.printWriter = new PrintWriter(socket.getOutputStream());
    		this.scanner = new Scanner(socket.getInputStream());
    	}
    	
    	public void assertReceived(String message) {
            if (scanner.hasNext()) {
                assertThat(message, equalTo(scanner.nextLine()));
            } else {
            	fail("Failed to receive message on second client.");
            }
		}

		public void send(String message) {
	        printWriter.println(message);
	        printWriter.flush();
		}

		public void close() throws IOException {
    		socket.close();
    	}
    }
}