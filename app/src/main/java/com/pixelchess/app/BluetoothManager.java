package com.pixelchess.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Owns Bluetooth sockets and byte transport. Game protocol/state live elsewhere. */
public final class BluetoothManager {
  private BluetoothAdapter adapter;
  private volatile BluetoothSocket socket;
  private volatile BluetoothServerSocket serverSocket;
  private volatile boolean closed;
  private OutputStream out;
  private final Object writeLock=new Object();
  private final ExecutorService writer=Executors.newSingleThreadExecutor();
  private long sessionGeneration;

  public interface WriteCallback { void onError(Exception error); }

  public void setAdapter(BluetoothAdapter adapter){this.adapter=adapter;}
  public void hostAndAccept(String serviceName,UUID uuid)throws IOException {
    requireAdapter();
    try{
      serverSocket=adapter.listenUsingRfcommWithServiceRecord(serviceName,uuid);
      if(closed){serverSocket.close();throw new IOException("Conexão cancelada");}
      socket=serverSocket.accept();
      serverSocket.close();
      serverSocket=null;
      if(closed){socket.close();throw new IOException("Conexão cancelada");}
      synchronized(writeLock){out=socket.getOutputStream();sessionGeneration++;}
    }catch(SecurityException denied){
      throw new IOException("Permissão Bluetooth indisponível. Autorize o acesso e tente novamente.",denied);
    }
  }

  public void connect(BluetoothDevice device,UUID uuid)throws IOException {
    requireAdapter();
    if(device==null)throw new IOException("Aparelho Bluetooth não selecionado");
    try{
      socket=device.createRfcommSocketToServiceRecord(uuid);
      adapter.cancelDiscovery();
      if(closed){socket.close();throw new IOException("Conexão cancelada");}
      socket.connect();
      if(closed){socket.close();throw new IOException("Conexão cancelada");}
      synchronized(writeLock){out=socket.getOutputStream();sessionGeneration++;}
    }catch(SecurityException denied){
      throw new IOException("Permissão Bluetooth indisponível. Autorize o acesso e tente novamente.",denied);
    }
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
    final long session;
    synchronized(writeLock){session=sessionGeneration;}
    writer.execute(()->{
      try{
        synchronized(writeLock){
          if(session!=sessionGeneration)return;
          if(out==null)throw new IOException("Sem conexão");
          out.write((message+"\n").getBytes("UTF-8"));
          out.flush();
        }
      }catch(Exception e){if(callback!=null)callback.onError(e);}
    });
  }

  public void close(){
    closed=true;
    try{if(serverSocket!=null)serverSocket.close();}catch(Exception ignored){}
    try{if(socket!=null)socket.close();}catch(Exception ignored){}
    synchronized(writeLock){sessionGeneration++;out=null;}
    serverSocket=null;socket=null;
  }

  void dispose(){close();writer.shutdownNow();}

  private void requireAdapter()throws IOException {
    if(adapter==null)throw new IOException("Bluetooth indisponível");
  }
}
