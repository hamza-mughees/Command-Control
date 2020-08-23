# Command & Control

This project uses datagram sockets to connect two clients, worker and command & control, to a server, the broker.

### Worker

Once the worker file is run, a new worker becomes available to do work. The console waits for the user to enter the name of the worker that has become available. Once the user enters the name, the worker class sends this name to the broker, notifiying this particular worker's availablity.

The worker then waits to receive a task from the broker. Once received, the worker completes the task, and sends the solution back to the broker.

### Broker

The broker is the first to be run, it is constantly waiting to receive names of new workers that become available for work. The broker contains the list of all available workers. It also waits for the command & control to send tasks. The broker has the ability to send the amount of available workers to the command & control at request. Once the broker recieves a task from the command & control, it fowards this task to an available worker. It then receives the completed task from the worker and forwards it to the command & control.

### Command & Control

The command and control asks the broker for the number of available workers. Once received the response, the command & control ask the user if they want to send a task to a worker. If yes, the command & control sends a task to the broker, who is to forwad this task to an available worker. The command & control then waits to receive the response from the broker, which is of course the completed task. Once received, this process repeats.
