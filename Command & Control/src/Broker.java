/* Author: Hamza Mughees
 * Broker function:
 * contains the list of available workers
 * waits for new workers to volunteer to work by receiving their names
 * waits for C&C to send commands
 * can send the amount of available workers to C&C at request
 * receives a task from C&C and forwards this task to an available worker to complete
 * receives a completed task from a worker and forwards it to C&C
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class Broker {
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("Broker has been activated...");
		
		boolean running = true;
		WorkerThread workerArray[] = new WorkerThread[50];
		int workerCount = 0;
		int cncPort = 0;
		
		byte[] receivingByteArray;
		byte[] sendingByteArray;
		
		while (running) {
			DatagramSocket socket = new DatagramSocket(50000);
			
			receivingByteArray = new byte[1024];
			DatagramPacket packetToReceive = new DatagramPacket(receivingByteArray, receivingByteArray.length);
			socket.receive(packetToReceive);
			socket.close();
			
			String dataString = new String(packetToReceive.getData()).trim();
			
			StringTokenizer stringTokenizer = new StringTokenizer(dataString);	// <- to identify packet sender
			String receivedFrom = stringTokenizer.nextToken(";");
			String command = stringTokenizer.nextToken();
			System.out.println("\n  NEW MESSAGE RECEIVED:\n  FROM: " + receivedFrom + "\n  MESSAGE: " + command + "\n");
			
			if(receivedFrom.equals("C&C"))
			{
				cncPort = packetToReceive.getPort();
				CNCHandler CNCH = new CNCHandler (packetToReceive, command, workerArray);
				CNCH.run();
			}
			else
			{
				if(isNumber(command)) 
				{
					System.out.println("Result received from worker, forwarding to C&C...");
					sendingByteArray = command.getBytes();
					DatagramPacket packetToSend = new DatagramPacket(sendingByteArray, sendingByteArray.length, packetToReceive.getAddress(), cncPort);
					try {
						DatagramSocket tmpSocket = new DatagramSocket();
						tmpSocket.send(packetToSend);
						tmpSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} 
				else
				{
					System.out.println("A new worker is available to work, " + command);
					String workerReply = "OK";
					sendingByteArray = workerReply.getBytes();
					
					DatagramPacket packetToSend = new DatagramPacket(sendingByteArray, sendingByteArray.length, packetToReceive.getAddress(), packetToReceive.getPort());
					try {
						DatagramSocket tmpSocket = new DatagramSocket();
						tmpSocket.send(packetToSend);
						tmpSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					workerArray[workerCount] = new WorkerThread(packetToReceive);
					workerCount++;
				}
			}
		}
	}
	
	public static boolean isNumber(String string)
	{
		try {
			int a = Integer.parseInt(string);
		}
		catch (NumberFormatException | NullPointerException e) {
			return false;
		}
		return true;
	}
	
}

class WorkerThread extends Thread {
	
	DatagramPacket packet;
	String task;
	boolean isAvailable;
	
	WorkerThread(DatagramPacket packet)
	{
		this.packet = packet;
		isAvailable = true; 
	}
	
	public void run()
	{
		if (task != null)
		{
			isAvailable = false;
			byte[] sendingByteArray = task.getBytes();
			
			DatagramPacket packetToSend = new DatagramPacket(sendingByteArray, sendingByteArray.length, packet.getAddress(), packet.getPort());
			try {
				DatagramSocket socket = new DatagramSocket();
				socket.send(packetToSend);
				socket.close();
				byte[] receivingByteArray = new byte[1024];
				DatagramPacket packetToReceive = new DatagramPacket(receivingByteArray, receivingByteArray.length);
				socket.receive(packetToReceive);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setTask(String task)
	{
		this.task = task;
	}
	
	public boolean isAvailable()
	{
		return isAvailable;
	}
	public DatagramPacket getPacket() 
	{
		return this.packet; 
	}
	
	public void setUnavailable() 
	{	       
		this.isAvailable = false; 
	}
}

class CNCHandler extends Thread {
	
	DatagramPacket packet;
	String command; 
	WorkerThread workerArray[];
	int count;
	WorkerThread worker;
	
	CNCHandler(DatagramPacket packet, String command, WorkerThread array[])
	{
		this.packet = packet;
		this.command = command;
		this.workerArray = array;
		count = 0;
	}
	
	public void run() {
		
		String query = "How many workers are available?";
		
		if (this.command.equals(query)) 
		{
			System.out.println("Sending number of available workers to C&C...");
			if (workerArray != null)
				for (int i=0; i<workerArray.length && workerArray[i]!=null; i++)
				{
					worker = workerArray[i];
					if(worker.isAvailable())
						count++;
				}
			else
				count = 0;
			
			String countString = String.valueOf(count);
			byte[] sendingByteArray = countString.getBytes();
			DatagramPacket packetToSend = new DatagramPacket(sendingByteArray, sendingByteArray.length, this.packet.getAddress(), this.packet.getPort());
			try {
				DatagramSocket socket = new DatagramSocket();
				socket.send(packetToSend);
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else 
		{
			System.out.println("Task received from C&C, forwarding to an available worker...");
			if (workerArray != null)
				for (int i=0; i<workerArray.length && workerArray[i]!=null; i++)
				{
					worker = (WorkerThread)workerArray[i];
					if(worker.isAvailable()) 
					{
						worker.setUnavailable();
						byte[] sendingByteArray = command.getBytes();		// task to send to worker
						DatagramPacket packetToSend = new DatagramPacket(sendingByteArray, sendingByteArray.length, 
								worker.getPacket().getAddress(), worker.getPacket().getPort());	// address and port used from each worker packet
						try {
							DatagramSocket socket = new DatagramSocket();
							socket.send(packetToSend);
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					}
				}
		}
	}
}


