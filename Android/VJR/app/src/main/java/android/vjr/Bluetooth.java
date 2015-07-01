package android.vjr;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Bluetooth{
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    public BluetoothAdapter btAdapter;
    private InputStream receiveStream = null;
    private OutputStream sendStream = null;
    public boolean isConnected = false;
    private ReceiverThread receiverThread;
    Handler handler;
    private static final UUID MY_UUID =UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public Bluetooth(Handler hstatus, Handler h, String addrToConnect) {
                try {
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    device =  btAdapter.getRemoteDevice(addrToConnect); //Adress
                    btAdapter.cancelDiscovery();
                    Log.d("Try to connect to : ",device.getName() );
                    //récupération de notre socket
                    socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    receiveStream = socket.getInputStream();// flux de réception de données
                    sendStream = socket.getOutputStream();// flux d'émission de données
                    connect();
                } catch (IOException e) {
                    Log.d("ERROR", "on connection in constructor");
                    e.printStackTrace();
                }
           handler = hstatus;
           receiverThread = new ReceiverThread(h);
    };
    public void sendData(String data) {
        sendData(data, false);
    }
    public void sendData(String data, boolean deleteScheduledData) {
        try {
            if(data.getBytes() != null) {
                sendStream.write(data.getBytes());// écriture des données a envoyer dans le buffer et flush
                sendStream.flush();
            }
          } catch (IOException e) {
            Log.d("ERROR", "on connection in sendData");
            e.printStackTrace();
        }
    }
    public void connect() {
        new Thread() {
            @Override public void run() {
                try {
                    // tentative de connexion
                    socket.connect();
                    // envoi d'un message de statut
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                    // début du processus de réception
                    receiverThread.start();
                    isConnected = true;
                    Log.d("Connected: ",device.getName() );
                } catch (IOException e) {
                    Log.d("N", "Connection Failed : "+e.getMessage());
                    e.printStackTrace();
                    isConnected = false;
                }
            }
        }.start();
    }
    public void close() {
        isConnected = false;
     try{
        socket.close();// fermeture de la connexion
    }catch (IOException e) {
        isConnected = false;
        e.printStackTrace();
    }
        try{
            Log.d("Close : ", socket.toString());
            receiveStream.close();
            sendStream.close();
             socket.close();// fermeture de la connexion

            isConnected = false;
        }catch (IOException e) {
            isConnected = false;
            e.printStackTrace();
        }
        finally {
            if(receiveStream !=null){
                try {

                    receiveStream.close();
                }catch (IOException e) {
                    isConnected = false;
                    e.printStackTrace();
                }
            }
            if(sendStream!=null)

            try {

               sendStream.close();
            }catch (IOException e) {
                isConnected = false;
                e.printStackTrace();
            }
            if(socket!=null)
                try {
                    socket.close();// fermeture de la connexion
                }catch (IOException e) {
                    isConnected = false;
                    e.printStackTrace();
                }



            Log.d("Socket closed ", Boolean.toString(isConnected));
        }
    }
    public BluetoothDevice getDevice() {
        return device;
    }

    private class ReceiverThread extends Thread {
        Handler handler;
        ReceiverThread(Handler h) {
            handler = h;
        }
        @Override public void run() {
            while(isConnected) {
                try {
                    if(receiveStream.available() > 0) {
                        byte buffer[] = new byte[100];
                        int k = receiveStream.read(buffer, 0, 100);
                        if(k > 0) {
                            byte rawdata[] = new byte[k];
                            for(int i=0;i<k;i++)
                                rawdata[i] = buffer[i];
                            String data = new String(rawdata);
                            Message msg = handler.obtainMessage();
                            Bundle b = new Bundle();
                            b.putString("receivedData", data);
                            msg.setData(b);
                            handler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isConnected = false;
                }
            }
        }
    }
}