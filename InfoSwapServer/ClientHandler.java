package InfoSwapServer;

// ClientHandler class

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

class ClientHandler extends Thread
{
	DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
	DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
	final DataInputStream in;
	final DataOutputStream out;
	final Socket s;
	

	// Constructor
	public ClientHandler(Socket s, InputStream in, OutputStream out)
	{
		this.s = s;
		this.in = new DataInputStream(in);
		this.out = new DataOutputStream(out);
	}

	@Override
	public void run()
	{
		String received;
		while (true)
		{
			
			try {
				
				// receive the answer from client
				received = in.readUTF();
				if (received.equals("Hello server")) {
					out.writeUTF("Hello client");
				}
				else if(received.equals("Exit"))
				{
					System.out.println("Client " + this.s + " sends exit...");
					System.out.println("Closing this connection.");
					this.s.close();
					System.out.println("Connection closed");
					break;
				}
				else {
					out.writeUTF("unrecognised greeting");
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try
		{
			// closing resources
			this.in.close();
			this.out.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
