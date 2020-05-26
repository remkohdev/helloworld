package com.example.helloworld;

import org.springframework.data.annotation.Id;
import java.util.UUID;

public class Message {

  public String id;  
  public String sender;
  public String message;
  public String host;


  @Override
  public String toString() {
    return "Message [id=" + id + ", sender=" + sender + ", message=" + message + ", host=" + host + "]";
  }
}
