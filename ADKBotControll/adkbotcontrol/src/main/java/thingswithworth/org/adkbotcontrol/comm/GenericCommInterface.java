package thingswithworth.org.adkbotcontrol.comm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by andrey on 3/20/15.
 */
public class GenericCommInterface implements Runnable{
    private static final String TAG = "Comm";

    private DataInputStream mInputStream;
    private DataOutputStream mOutputStream;
    private HashMap<String,Handler> mHandlers;

    public GenericCommInterface(DataInputStream inputStream,DataOutputStream outputStream){
        mHandlers = new HashMap<String,Handler> ();
        mInputStream = inputStream;
        mOutputStream = outputStream;
    }

    public void addHandler(String header, Handler handler){
        mHandlers.put(header,handler);
    }

    @Override
    public void run() {
        boolean stillRunning = true;
        StringBuilder builder = new StringBuilder();
        ArrayList<Byte> bytesList = new ArrayList<Byte>();
        boolean headerGotten = false;
        while(true) {
            try {
                if(headerGotten && mInputStream.available()>0){
                    char nextChar =mInputStream.readChar();
                    if(nextChar!=':') {
                        builder.append(nextChar);
                    }else{
                        headerGotten = true;
                    }
                }else if( mInputStream.available()>0){
                    byte nextByte = mInputStream.readByte();
                    if(nextByte==Character.LINE_SEPARATOR) {
                        byte[] bytes = new byte[bytesList.size()];
                        for(int i=0;i<bytesList.size();i++)
                            bytes[i] = bytesList.get(i);
                        bytesList.clear();
                        String name = builder.toString();
                        builder = new StringBuilder();
                        if(mHandlers.containsKey(name)) {
                            Handler msgHandler = mHandlers.get(name);
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putByteArray("data",bytes);
                            msg.setData(bundle);
                            msgHandler.handleMessage(msg);
                        }else {
                            //TODO
                        }
                    }else {
                        bytesList.add(nextByte);
                    }
                }
            } catch (EOFException e) {
                e.printStackTrace();
                stillRunning = false;
            } catch (IOException e) {
                e.printStackTrace();
                stillRunning = false;
            }
        }
    }
}
