package thingswithworth.org.adkbotcontrol.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import thingswithworth.org.adkbotcontrol.R;
import thingswithworth.org.adkbotcontrol.activities.ADKActivity;
import thingswithworth.org.adkbotcontrol.comm.GenericCommInterface;


public class StartActivity extends ADKActivity implements SurfaceHolder.Callback, MediaRecorder.OnInfoListener {
    private static ServerSocket serverSocket;
    private CheckedTextView adkCheckBox, clientCheckBox;
    TextView ipView;
    private static final int SocketServerPORT = 5000;
    private Camera mCamera;
    private SurfaceView mPreview;
    GenericCommInterface serverComm;
    Socket socket;
    private SurfaceHolder mHolder;

    private final Handler SERVER_HANDLER = new Handler(Looper.getMainLooper(),new Handler.Callback(){

        @Override
        public boolean handleMessage(Message msg) {
            byte[] bytes = msg.getData().getByteArray("data");
            DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes));
            try {
                char dir = (char)stream.readByte();
                byte speed = stream.readByte();
                char fullSpeed = (char)(speed/100.0f*255.0f);
                Toast.makeText(StartActivity.this, "Received message "+dir+" "+fullSpeed, Toast.LENGTH_SHORT).show();
                sendDriveCommand(dir, fullSpeed);
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
        mPreview = (SurfaceView) this.findViewById(R.id.camera_surface);
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        ipView.setText(ip);
        mCamera = getCameraInstance();
        mHolder = mPreview.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);

        try {
            if(serverSocket==null) {
                serverSocket = new ServerSocket(); // <-- create an unbound socket first
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(SocketServerPORT)); // <-- now bind it
                ConnectTask task = new ConnectTask();
                task.execute();
            }else{
                if(!serverSocket.isClosed())
                    clientCheckBox.setChecked(true);
            }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            serverSocket.close();
            mCamera.unlock();
            mCamera.stopPreview();
            mCamera.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("StartActivity", "Error setting camera preview: " + e.getMessage());
        }catch (NullPointerException e) {
            Log.d("StartActivity", "No camera!");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.d("StartActivity","Whatup");
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
                Log.v("StartActivity","connected");
                //ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
                //pfd.getFileDescriptor().sync();
                //mMediaRecorder.setOutputFile(pfd.getFileDescriptor());
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

    private Camera getCameraInstance() {

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open();
            return mCamera;
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return null;
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    protected void onUSBAccessoryOpen(){
        Toast.makeText(StartActivity.this, "USB opened", Toast.LENGTH_SHORT).show();
        adkCheckBox.setChecked(true);
    }

    protected void closeAccessory() {
        super.closeAccessory();
        adkCheckBox.setChecked(false);
    }


}
