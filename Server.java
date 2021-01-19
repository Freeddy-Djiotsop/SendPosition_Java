import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server
{

	private ServerSocket serverSocket;
	
	//hilft data richtig zuzustellen
	private List<Client> clients = new ArrayList<Server.Client>();
	
	
	private Thread threadfromClient1;
	private Thread threadfromClient2;
	

	private int id=0, stopp=0;
	
	public Server()
	{	
		try 
		{
			serverSocket = new ServerSocket(4234);
			acceptConnexion();
		
		}
		catch (IOException e) {}
	}

	public void acceptConnexion()
	{
		while(id<2)
		{
			try 
			{
				System.out.println("Warten auf Verbindung von Client "+(id+1)+" ...");
				Socket client = serverSocket.accept();
				
				DataInputStream fromClient = new DataInputStream(client.getInputStream());
				DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
				
				id++;
				toClient.write(id);
				
				toClient.writeDouble(100);
				toClient.writeDouble(0);
				
				clients.add(new                                       Client(client, id));
				
				if(id==1)
				{
					toClient.writeDouble(100);
					toClient.writeDouble(0);
					threadfromClient1 = new Thread(new RecievePositionFromClient(fromClient, id));
				}
				
				else if(id==2)
				{
					toClient.writeDouble(0);
					toClient.writeDouble(0);
					
					threadfromClient2 = new Thread(new RecievePositionFromClient(fromClient, id));
					
					//The 2 Client are already there we can start
					threadfromClient1.start();
					threadfromClient2.start();
					System.out.println("Threads laufen");
					
				}
				System.out.println("Client "+id+" erfolgreich verbunden");
				
			} 
			catch (IOException e) 
			{System.out.println("konnte Client "+ (id+1) +" nicht verbinden");}
		}
	}
	
	public void sendCoord(double x, double y, int i)
	{
		
		
		for(Client c : clients)
		{
			if(c.id!=i)
			{
				
				try 
				{
					DataOutputStream toClient = new DataOutputStream(c.socket.getOutputStream());
					toClient.writeDouble(x);
					toClient.writeDouble(y);
					System.out.println(x+":"+y+" gesendet");
				} 
				catch (IOException e) 
				{
					
					System.out.println("konnte data an Client "+i+" nicht senden");
					
				}
			}
		}
	}
	
	class Client
	{
		@SuppressWarnings("unused")
		private Socket socket;
		@SuppressWarnings("unused")
		private int id;
		
		public Client(Socket s, int i) {
			socket = s;
			id = i;
		}
	}
	
	class RecievePositionFromClient implements Runnable
	{
		private DataInputStream din;
		private int id;
		
		public RecievePositionFromClient(DataInputStream d, int i)
		{
			din = d;
			id = i;
		}
		
		@Override public void run() 
		{
			while(true)
			{
				try 
				{
						double x = din.readDouble();
						double y = din.readDouble();
						System.out.println(x+":"+y+" empfangen");
						sendCoord(x, y, id);//by sending I specified you sent
				} 
				catch (IOException e) 
				{
					stopp++;
					System.out.println("konnte data von Client "+ id+ " nicht empfangen");
					if(stopp==10)
						System.exit(-1);
				}
			}
		}
	}
	
	
	public static void main(String[] args) { new Server(); }
}
