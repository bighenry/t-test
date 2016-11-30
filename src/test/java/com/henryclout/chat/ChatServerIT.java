package com.henryclout.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class ChatServerIT {

	// FIXME: server port should be dynamically allocated.
	private int serverPort = 8081;
	
    @Test
    public void testConnect() throws Exception {
        Socket socket = new Socket("localhost", serverPort);
        socket.close();
    }

    @Test
    public void testMessageSend() throws Exception {
        Socket socket = new Socket("localhost", serverPort);
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println("Test message!");
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

        // Send message, check receipt.
        String testMessage = "Test message!";
        clientOnePrintWriter.println(testMessage);
        clientOnePrintWriter.flush();
        
        if (clientTwoScanner.hasNext()) {
            assertThat(testMessage, equalTo(clientTwoScanner.nextLine()));
        } else {
        	fail("Failed to receive message on second client.");
        }

        clientTwoScanner.close();
        clientOneSocket.close();
        clientTwoSocket.close();
    }
    
//    @Test
    public void testMultipleMessageReceive() throws Exception {
    	List<TestClient> testClients = new LinkedList<>();
    	for (Socket socket : connectMultipleClients()) {
    		testClients.add(new TestClient(socket));
    	}
    	
    	String message = "test message";
    	for (TestClient testClientSender : testClients) {
    		testClientSender.send(message);
    		for (TestClient testClientReceiver : testClients) {
    			if (testClientSender != testClientReceiver) {
    				testClientReceiver.assertReceived(message);
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
    	for (int i = 0; i < 10; i++) {
    		clientSockets.add(new Socket("localhost", serverPort));
    	}
    	return clientSockets;
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
	        printWriter.println("Test message!");
	        printWriter.flush();
		}

		public void close() throws IOException {
    		socket.close();
    	}
    	
    }
}