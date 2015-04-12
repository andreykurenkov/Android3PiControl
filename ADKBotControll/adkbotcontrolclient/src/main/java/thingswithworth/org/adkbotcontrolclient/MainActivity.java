package thingswithworth.org.adkbotcontrolclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MainActivity extends Activity {

    EditText editTextAddress;
    Button connectButton;
    ImageButton forwardButton, leftButton, rightButton;
    SeekBar speedBar;
    byte speed;
    final String IP_ADDRESS_REGEX = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    final int PORT = 5000;
    private MediaPlayer mMediaPlayer;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextAddress = (EditText)findViewById(R.id.address);
        connectButton = (Button)findViewById(R.id.connect);
        connectButton.setEnabled(false);
        editTextAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean matchesIP = s.toString().matches(IP_ADDRESS_REGEX);
                connectButton.setEnabled(matchesIP);
            }
        });
        speedBar = (SeekBar) findViewById(R.id.seekBar);
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed = (byte) progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        forwardButton = (ImageButton) findViewById(R.id.buttonForward);
        leftButton = (ImageButton) findViewById(R.id.buttonLeft);
        rightButton = (ImageButton) findViewById(R.id.buttonRight);

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(socket!=null && socket.isConnected()) {
                        socket.getOutputStream().write("driveCommand".getBytes());
                        socket.getOutputStream().write((byte)(250));
                        socket.getOutputStream().write((byte)('F'));
                        socket.getOutputStream().write(speed);
                        socket.getOutputStream().write((byte)(252));
                        socket.getOutputStream().flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(socket!=null && socket.isConnected()) {
                        socket.getOutputStream().write("driveCommand".getBytes());
                        socket.getOutputStream().write((byte)(250));
                        socket.getOutputStream().write((byte)('L'));
                        socket.getOutputStream().write(speed);
                        socket.getOutputStream().write((byte)(252));
                        socket.getOutputStream().flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(socket!=null && socket.isConnected()) {
                        socket.getOutputStream().write("driveCommand".getBytes());
                        socket.getOutputStream().write((byte)(250));
                        socket.getOutputStream().write((byte)('R'));
                        socket.getOutputStream().write(speed);
                        socket.getOutputStream().write((byte)(252));
                        socket.getOutputStream().flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }
    public void onConnect(View view) {
        final ConnectTask connectTask = new ConnectTask(
                editTextAddress.getText().toString(),
                PORT);
        connectTask.execute();
        try {
            socket = connectTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        /*
         ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(pfd.getFileDescriptor());
                mMediaPlayer.prepare();
                mMediaPlayer.start();
         */
    }

    public class ConnectTask extends AsyncTask<Void, Void, Socket> {
        String dstAddress;
        int dstPort;
        String response = "";
        ProgressDialog ringProgressDialog;
        ConnectTask(String addr, int port){
            dstAddress = addr;
            dstPort = port;
            ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait ...",	"Trying to connect", true,true);
        }

        @Override
        protected Socket doInBackground(Void... noparams) {
            Socket socket = null;
            try {
                socket = new Socket(dstAddress, dstPort);
                if(!socket.isConnected())
                    return null;
                return socket;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.wtf("yo",e);
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                Log.wtf("yo",e);
                response = "IOException: " + e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Socket result){
            MainActivity.this.socket = result;
            ringProgressDialog.cancel();
            if(result==null){
                Toast.makeText(MainActivity.this, "Could not connect...", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
