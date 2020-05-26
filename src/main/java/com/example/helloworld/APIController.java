package com.example.helloworld;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@RestController
public class APIController {

  @PostMapping(value = "/api/messages", produces=MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<Message> createMessage(@RequestBody Message message) {
    
    message.message = "Hello "+message.sender+" (direct)";
    String id = UUID.randomUUID().toString();
    message.id = id;

    ResponseEntity<Message> response = new ResponseEntity<Message>(message, HttpStatus.OK);
    return response;
  }

  @PostMapping(value = "/proxy/api/messages", produces=MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<Message> createMessageByProxy(@RequestBody Message newMessage) {
    Message message = null;

    String host = newMessage.host;
    
    if(host!=null){
        // create a proxy message from host
        String url = "http://"+host+"/api/messages";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Message> request = new HttpEntity<Message>(newMessage,headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.POST, request, Message.class);
        Message proxyMessage = (Message) response.getBody();
        String proxyMessageMessage = proxyMessage.message.replaceAll("direct", "proxy");
        proxyMessage.message=proxyMessageMessage;
        message = proxyMessage;

    }else{
        // else create a local message
        ResponseEntity<Message> response = this.createMessage(newMessage);
        Message localMessage = (Message) response.getBody();
        message = localMessage;
    }

    return new ResponseEntity<Message>(message, HttpStatus.OK);
  }

}

