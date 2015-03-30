package thingswithworth.org.adkbotcontrol.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

import thingswithworth.org.adkbotcontrol.R;
import thingswithworth.org.adkbotcontrol.activities.ADKActivity;
import thingswithworth.org.adkbotcontrol.comm.GenericCommInterface;


public class StartActivity extends ADKActivity {
    private ServerSocket serverSocket;
    private CheckedTextView adkCheckBox, clientCheckBox;
    TextView ipView;
    private static final int SocketServerPORT = 5000;
    GenericCommInterface serverComm;
    Socket socket;

    private final Handler SERVER_HANDLER = new Handler(new Handler.Callback(){

        @Override
        public boolean handleMessage(Message msg) {
            byte[] bytes = msg.getData().getByteArray("data");
            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes));
            try {
                char dir = stream.readChar();
                byte speed = stream.readByte();
                char fullSpeed = (char)(speed/100.0f*255.0f);
                sendDriveCommand(dir,fullSpeed);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ipView = (TextView) this.findViewById(R.id.ip_text_content);
        adkCheckBox = (CheckedTextView) this.findViewById(R.id.adk_checkbox);
        clientCheckBox = (CheckedTextView) this.findViewById(R.id.client_checkbox);

        ipView.setText(getIpAddress().toString());
        try {
            serverSocket = new ServerSocket(); // <-- create an unbound socket first
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(SocketServerPORT)); // <-- now bind it
            ConnectTask task = new ConnectTask();
            task.execute();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(StartActivity.this, "Could not open port...", Toast.LENGTH_SHORT).show();
            Log.wtf("Oh no",e);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ConnectTask extends AsyncTask<Void, Void, Socket> {
        @Override
        protected Socket doInBackground(Void... params) {
            try {
                Socket socket = serverSocket.accept();
                DataInputStream serverInput = new DataInputStream(socket.getInputStream());
                DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());
                serverComm = new GenericCommInterface(serverInput,serverOutput);
                serverComm.addHandler("driveCommand",SERVER_HANDLER);
                Thread commThread = new Thread(serverComm,"Server Comm");
                commThread.start();
                Log.v("start","connected");
                return socket;
            } catch (IOException e) {
                Log.wtf("error",e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Socket socket){
            clientCheckBox.setChecked(socket!=null);
        }
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }
}
