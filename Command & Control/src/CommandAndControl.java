/* Author: Hamza Mughees
 * C&C function:
 * asks for number of workers (gets response)
 * asks do you want to send a task (y/n)?
 * if yes (Task: Calculate the sum of the first n integers, enter value of n)
 * else cnc shutdown
 * sends this value to the broker
 * receives calculated result from broker
 * repeats this process until amount of available workers is zero or an invalid entry is made
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class CommandAndControl {
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Command and Control has been activated...");
		
		DatagramSocket socket = new DatagramSocket();
		InetAddress localHostAddress = InetAddress.getLocalHost();
		
		byte[] sendingByteArray;
		byte[] receivingByteArray;
		
		while (true)
		{
			String workerRequest = "C&C;How many workers are available?";
			sendingByteArray = workerRequest.getBytes();
			DatagramPacket packetToSend = new DatagramPacket(sendingByteArray, sendingByteArray.length, localHostAddress, 50000);
			socket.send(packetToSend);
						
			receivingByteArray = new byte[4];
			DatagramPacket packetToReceive = new DatagramPacket(receivingByteArray, receivingByteArray.length);
			socket.receive(packetToReceive);
			int numberOfWorkers = Integer.parseInt(new String(packetToReceive.getData()).trim());
			if (numberOfWorkers > 0)
			{
				System.out.println(numberOfWorkers + " workers are available.\nWould you like to send a task? (y/n)");
				Scanner scanner = new Scanner(System.in);
				String input = scanner.nextLine();
				if (input.equals("y"))
				{
					System.out.println("Task: Calculate the sum of the first n integers.\nEnter any number n: ");
					int number = scanner.nextInt();
					
					sendingByteArray = ("C&C;" + Integer.toString(number)).getBytes();
					packetToSend = new DatagramPacket(sendingByteArray, sendingByteArray.length, localHostAddress, 50000);
					socket.send(packetToSend);
					
					socket.receive(packetToReceive);
					System.out.println("The completed task has been received from the broker\n"
							+ "The sum of the first " + number + " integers = " + new String(packetToReceive.getData()) + "\n");
				}
				else if (input.equals("n"))
				{
					System.out.println("OK, Command & Control is going offline.\n");
					break;
				}
				else 
				{
					System.out.println("Invalid input, Command & Control is going offline.\n");
					break;
				}
			}
			else
			{
				System.out.println("There are no workers available at the moment, Command & Control is going offline\n");
				break;
			}
		}
		
		socket.close();
	}
	
}
