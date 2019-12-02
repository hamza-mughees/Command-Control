/* Author: Hamza Mughees
 * Worker function:
 * a new worker is available every time this runs
 * takes a name as an input
 * sends this name as the name of this worker to the broker
 * waits to receive a task from the broker
 * completes the task when received and sends it back to the broker
 */

import java.util.Scanner;
import java.net.*;

public class Worker {
	
	public static void main(String[] args) throws Exception
	{
		DatagramSocket socket = new DatagramSocket();
		InetAddress localHostAddress = InetAddress.getLocalHost();
		
		byte[] sendingByteArray;
		byte[] receivingByteArray;
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter name of worker who is volunteering to work");
		String name = scanner.nextLine();
		scanner.close();
		
		name = "Worker;" + name;
		sendingByteArray = name.getBytes();		
		DatagramPacket packetToSend = new DatagramPacket(sendingByteArray, sendingByteArray.length, localHostAddress, 50000);
		socket.send(packetToSend);
				
		while(true)
		{
			receivingByteArray = new byte[1024];
			DatagramPacket packetToReceive = new DatagramPacket(receivingByteArray, receivingByteArray.length);
			socket.receive(packetToReceive);
			String data = new String(packetToReceive.getData()).trim();
			
			if (data.equals("OK"))
				System.out.println(data + "\nWaiting to receive task from broker...");
			else
			{
				int task = Integer.parseInt(data);
				int result = completeTask(task);
				System.out.println("Task completed, sending result to broker");
				String resultString = String.valueOf(result);
				sendingByteArray = ("Worker;" + resultString).getBytes();
				packetToSend = new DatagramPacket(sendingByteArray, sendingByteArray.length, packetToReceive.getAddress(),50000);
				socket.send(packetToSend);
				break;
			}
		}
		
		socket.close();
	}
	
	public static int completeTask(int task)
	{
		int result = 0;
		for (int i=1; i<=task; i++)
			result += i;
		return result;
	}

}
