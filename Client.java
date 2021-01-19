import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Client extends JPanel implements KeyListener {


	private static final long serialVersionUID = 1L;
	
	private Thread thread;
	
	private JFrame f = new JFrame();
	private JLabel label = new JLabel();

	private DataOutputStream toServer;
	
	private double x = 0, y = 0, x2=100, y2=0;//für die position
	private int id=0, stopp=0;
	
	public Client()
	{
		connectToServer();
		
		
		addKeyListener(this);
		setFocusable(true);
		setPreferredSize(new Dimension(500,400));
		setBackground(Color.BLACK);
		
		f.setLayout(new BorderLayout());
		f.add(this, BorderLayout.CENTER);
		label.setPreferredSize(new Dimension(100,400));
		f.add(label, BorderLayout.EAST);
		
		f.setVisible(true);
		f.setTitle("Moving Ball "+id);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		if(id==1)
		{
			g2.setColor(Color.YELLOW);
			g2.fill(new Ellipse2D.Double(x, y, 40, 40));
			
			g2.setColor(Color.CYAN);
			g2.fill(new Ellipse2D.Double(x2, y2, 40, 40));
			
		}
		else if(id==2)
		{
			g2.setColor(Color.CYAN);
			g2.fill(new Ellipse2D.Double(x, y, 40, 40));
			
			g2.setColor(Color.YELLOW);
			g2.fill(new Ellipse2D.Double(x2, y2, 40, 40));
		}
		
		
		
	}
	
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		String s="";
		
		switch (id) 
		{
		case 1: 
			{
				switch(key)
				{
				case KeyEvent.VK_UP:
					s +="UP ";
					if(y-5>=0)//Block the going over
						y -= 5;
					break;
					
				case KeyEvent.VK_DOWN:
					s +="Down ";
					if(y+5<=this.getHeight()-40)//help to not exceed the window
						y += 5;
					break;
					
				case KeyEvent.VK_LEFT:
					s +="Left ";
					if(x-5>=0)
						x -= 5;
					break;
					
				case KeyEvent.VK_RIGHT:
					s +="Right ";
					if(x+5<this.getWidth()-40)
						x += 5;
					break;
				}
				try {
					toServer.writeDouble(x);
					toServer.writeDouble(y);
					
				} 
				catch (IOException e1) 
				{
					System.out.println("Client "+id+" konnte nicht senden");
				}
				s +=x+"|"+y;
				label.setText(s);
				repaint();
				break;
			}
		case 2:
			{
				switch(key)
				{
				case KeyEvent.VK_UP:
					s +="UP ";
					if(y2-5>=0)//Block the going over
						y2 -= 5;
					break;
					
				case KeyEvent.VK_DOWN:
					s +="Down ";
					if(y2+5<=this.getHeight()-40)//help to not exceed the window
						y2 += 5;
					break;
					
				case KeyEvent.VK_LEFT:
					s +="Left ";
					if(x2-5>=0)
						x2 -= 5;
					break;
					
				case KeyEvent.VK_RIGHT:
					s +="Right ";
					if(x2+5<this.getWidth()-40)
						x2 += 5;
					break;
				}
			}
			
			try {
				toServer.writeDouble(x2);
				toServer.writeDouble(y2);
				
			} 
			catch (IOException e1) 
			{
				System.out.println("Client "+id+" konnte nicht senden");
			}
			s +=x2+"|"+y2;
			label.setText(s);
			repaint();
			break;
		}
		
		
				
	}
	
	@Override public void keyTyped(KeyEvent e) {}

	@Override public void keyReleased(KeyEvent e) {}
	
	public void updateCoord(double x, double y, int i) 
	{
	
		if(id==2)
		{
			this.x = x;
			this.y = y;
			System.out.println(id+" hat sich updatet");
		}
		if(id==1)
		{			
			this.x2 = x;
			this.y2 = y;
			System.out.println(id+" hat sich updatet");
		}
		
		
		
		repaint();
	}
	
	@SuppressWarnings("resource")
	private void connectToServer() 
	{
		try
		{
			Socket server = new Socket("localhost", 4234);/*connect to server*/
			
			/*Preparing communication with Server*/
			DataInputStream fromServer = new DataInputStream(server.getInputStream());
			toServer = new DataOutputStream(server.getOutputStream());
			
			id = fromServer.read();//Recieve ID
			
			thread = new Thread(new RecieveFromServer(fromServer, id));
			thread.start();
			System.out.println("Thread "+id+" ist gestartet");
		}
		catch (Exception e) {}
		
	}

	class RecieveFromServer implements Runnable
	{
		private DataInputStream din;
		private int id;
		public RecieveFromServer(DataInputStream d, int i) 
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
					System.out.println(id+" Warte auf Daten");
					double x =  din.readDouble();
					double y =  din.readDouble();
					System.out.println(id+" schickt "+x+"|"+y);
					updateCoord(x,y,id);//id help to know you recieve the data
				} 
				catch (IOException e) 
				{
					stopp++;
					System.out.println(id+" konnte data nicht empfangen");
					if(stopp==10)
						System.exit(-1);
				}
			}
			
			
		}
	}
	
	public static void main(String[] args)
	{	
		new Client();
	}
	
}
