package com.pixelchess.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Owns Bluetooth sockets and byte transport. Game protocol/state live elsewhere. */
public final class BluetoothManager {
  private BluetoothAdapter adapter;
  private BluetoothSocket socket;
  private BluetoothServerSocket serverSocket;
  private OutputStream out;
  private final Object writeLock=new Object();
  private final ExecutorService writer=Executors.newSingleThreadExecutor();

  public interface WriteCallback { void onError(Exception error); }

  public void setAdapter(BluetoothAdapter adapter){this.adapter=adapter;}
  public BluetoothAdapter adapter(){return adapter;}
  public boolean available(){return adapter!=null;}
  public boolean enabled(){return adapter!=null&&adapter.isEnabled();}
  public Set<BluetoothDevice> bondedDevices(){return adapter.getBondedDevices();}

  public void hostAndAccept(String serviceName,UUID uuid)throws IOException {
    requireAdapter();
    serverSocket=adapter.listenUsingRfcommWithServiceRecord(serviceName,uuid);
    socket=serverSocket.accept();
    serverSocket.close();
    serverSocket=null;
    out=socket.getOutputStream();
  }

  public void connect(BluetoothDevice device,UUID uuid)throws IOException {
    requireAdapter();
    socket=device.createRfcommSocketToServiceRecord(uuid);
    adapter.cancelDiscovery();
    socket.connect();
    out=socket.getOutputStream();
  }

  public BufferedReader reader()throws IOException {
    if(socket==null)throw new IOException("Sem conexão");
    return new BufferedReader(new InputStreamReader(socket.getInputStream()));
  }

  public void write(String message)throws IOException {
    synchronized(writeLock){
      if(out==null)throw new IOException("Sem conexão");
      out.write((message+"\n").getBytes("UTF-8"));
      out.flush();
    }
  }

  /** Preserves invocation order for all gameplay packets. */
  public void writeAsync(String message,WriteCallback callback){
    writer.execute(()->{
      try{write(message);}
      catch(Exception e){if(callback!=null)callback.onError(e);}
    });
  }

  public void close(){
    try{if(serverSocket!=null)serverSocket.close();}catch(Exception ignored){}
    try{if(socket!=null)socket.close();}catch(Exception ignored){}
    serverSocket=null;socket=null;out=null;
  }

  private void requireAdapter()throws IOException {
    if(adapter==null)throw new IOException("Bluetooth indisponível");
  }
}
