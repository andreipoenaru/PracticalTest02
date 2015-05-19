package ro.pub.cs.systems.pdsd.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PracticalTest02MainActivity extends Activity {
	
	private ServerThread mServerThread;
	private TextView mPortTextView;
	
	private EditText mOp1;
	private EditText mOp2;
	
	private Button mAdd;
	private Button mMul;
	
	private TextView mAddRes;
	private TextView mMulRes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_practical_test02_main);
		
		mAddRes = (TextView) findViewById(R.id.addRes);
		mMulRes = (TextView) findViewById(R.id.mulRes);
		
		mServerThread = new ServerThread(mAddRes, mMulRes);
		mServerThread.start();
		
		mPortTextView = (TextView) findViewById(R.id.port);
		mPortTextView.setText(Integer.toString(mServerThread.getPort()));
		
		mOp1 = (EditText) findViewById(R.id.op1);
		mOp2 = (EditText) findViewById(R.id.op2);
		
		mAdd = (Button) findViewById(R.id.add);
		mAdd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new ClientThread(mServerThread.getPort(), mAddRes, mMulRes, "add", mOp1.getText().toString(), mOp2.getText().toString()).start();
			}
		});
		
		mMul = (Button) findViewById(R.id.mul);
		mMul.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new ClientThread(mServerThread.getPort(), mAddRes, mMulRes, "mul", mOp1.getText().toString(), mOp2.getText().toString()).start();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mServerThread != null) {
			mServerThread.stopThread();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.practical_test02_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

class ServerThread extends Thread {
	
	private ServerSocket mServerSocket;
	
	private TextView mAddRes;
	private TextView mMulRes;
	
	ServerThread(TextView addRes, TextView mulRes) {
		mAddRes = addRes;
		mMulRes = mulRes;
		
		try {
			mServerSocket = new ServerSocket(0);
		} catch (Exception ex) {
			Log.e("abc", ex.getMessage());
		}
	}
	
	int getPort() {
		return mServerSocket.getLocalPort();
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				new CommThread(mServerSocket.accept(), mAddRes, mMulRes).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void stopThread() {
		interrupt();
		if (mServerSocket != null) {
				try {
					mServerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}

class CommThread extends Thread {
	
	private Socket mSocket;
	
	CommThread(Socket socket, TextView addRes, TextView mulRes) {
		mSocket = socket;
		Log.e("abc", "cons CommThread");
	}
	
	@Override
	public void run() {
		try {
			BufferedReader reader = Utilities.getReader(mSocket);
			PrintWriter writer = Utilities.getWriter(mSocket);
			
			String[] parts = reader.readLine().split(",");
			
			int op1 = Integer.parseInt(parts[1]);
			int op2 = Integer.parseInt(parts[2]);
			
			if (parts[0].equals("mul")) {
				try {
					Thread.sleep(1000);
				} catch (Exception ex) {
					;
				}
			}
			
			int res = 0;
			if (parts[0].equals("add")) {
				res = op1 + op2;
			} else if (parts[0].equals("mul")) {
				res = op1 * op2;
			}
			
			Log.e("abc", Integer.toString(res));
			writer.println(Integer.toString(res));
			
			mSocket.close();
		} catch (Exception ex) {
			Log.e("abc", "eroare CommThread " + ex.toString());
		}
		
	}
}

class ClientThread extends Thread {
	
	int mPort;
	TextView mAddRes;
	TextView mMulRes;
	
	String mOper;
	String mOp1;
	String mOp2;
	
	public ClientThread(int port, TextView addRes, TextView mulRes, String oper, String op1, String op2) {
		mPort = port;
		mAddRes = addRes;
		mMulRes = mulRes;
		
		mOper = oper;
		mOp1 = op1;
		mOp2 = op2;
	}
	
	@Override
	public void run() {
		try {
			Socket socket = new Socket("localhost", mPort);
			
			BufferedReader reader = Utilities.getReader(socket);
			PrintWriter writer = Utilities.getWriter(socket);
			
			writer.println(mOper + "," + mOp1 + "," + mOp2);
			writer.flush();
			
			final String res = reader.readLine();
			
			if (mOper.equals("add")) {
				mAddRes.post(new Runnable() {
					
					@Override
					public void run() {
						mAddRes.setText(res);
					}
				});
			} else if (mOper.equals("mul")) {
				mMulRes.post(new Runnable() {
					
					@Override
					public void run() {
						mMulRes.setText(res);
					}
				});
			}
			
			socket.close();
		} catch (Exception ex) {
			Log.e("abc", "ClientThread " + ex.getMessage());
		}
	}
}
