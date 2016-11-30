package com.henryclout.chat;

import java.io.PrintWriter;
import java.net.Socket;
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
    	// Connect 10 clients.
    	List<Socket> clientSockets = new LinkedList<>();
    	for (int i = 0; i < 10; i++) {
    		clientSockets.add(new Socket("localhost", serverPort));
    	}
    	
    	// Close them all.
    	for (Socket clientSocket : clientSockets) {
    		clientSocket.close();
    	}
    }

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
}