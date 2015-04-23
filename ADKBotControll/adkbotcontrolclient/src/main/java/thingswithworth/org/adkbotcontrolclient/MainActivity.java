package thingswithworth.org.adkbotcontrolclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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


public class MainActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    EditText editTextAddress;
    Button connectButton;
    ImageButton forwardButton, leftButton, rightButton, backButton;
    SeekBar speedBar;
    byte speed;
    final String IP_ADDRESS_REGEX = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    final int PORT = 5000;
    private SurfaceView mPreview;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder holder;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextAddress = (EditText)findViewById(R.id.address);
        editTextAddress.setText("128.61.123.115");
        connectButton = (Button)findViewById(R.id.connect);
        connectButton.setEnabled(true);
        mPreview =  (SurfaceView)findViewById(R.id.camera_surface);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        holder = mPreview.getHolder();
        holder.addCallback(this);

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
                try {
                    socket.getOutputStream().write("driveCommand".getBytes());
                    socket.getOutputStream().write((byte) (250));
                    socket.getOutputStream().write((byte) ('S'));
                    socket.getOutputStream().write(speed);
                    socket.getOutputStream().write((byte) (252));
                    socket.getOutputStream().flush();
                }catch(Exception e){
                    Log.wtf("Comm",e);
                }

            }
        });
        forwardButton = (ImageButton) findViewById(R.id.buttonForward);
        leftButton = (ImageButton) findViewById(R.id.buttonLeft);
        rightButton = (ImageButton) findViewById(R.id.buttonRight);
        backButton = (ImageButton) findViewById(R.id.buttonBack);

        forwardButton.setOnTouchListener(new ControlTouchListener('F'));
        leftButton.setOnTouchListener(new ControlTouchListener('L'));
        rightButton.setOnTouchListener(new ControlTouchListener('R'));
        backButton.setOnTouchListener(new ControlTouchListener('B'));
    }


    private class ControlTouchListener implements View.OnTouchListener {
        final char DIR;

        public ControlTouchListener(char where){
            DIR = where;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        socket.getOutputStream().write("driveCommand".getBytes());
                        socket.getOutputStream().write((byte) (250));
                        socket.getOutputStream().write((byte) (DIR));
                        socket.getOutputStream().write(speed);
                        socket.getOutputStream().write((byte) (252));
                        socket.getOutputStream().flush();
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_DOWN:
                        socket.getOutputStream().write("driveCommand".getBytes());
                        socket.getOutputStream().write((byte) (250));
                        socket.getOutputStream().write((byte) ('S'));
                        socket.getOutputStream().write(0);
                        socket.getOutputStream().write((byte) (252));
                        socket.getOutputStream().flush();
                        return true; // if you want to handle the touch event
                }
            } catch (Exception e) {
                Log.wtf("Comm", e);
            }
            return false;
        }
    }


    public void surfaceCreated(SurfaceHolder holder)
    {
        mMediaPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //:(
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
           //:(
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
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
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
