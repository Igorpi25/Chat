package com.ivanov.tech.chat.service;

import java.util.ArrayList;

import com.ivanov.tech.communicator.service.TransportBase;

public class ChatService extends com.ivanov.tech.communicator.service.CommunicatorService{

    public final static String URL_SERVER="ws://igorpi25.ru:8001";//Websocket server URL and port
	
	@Override
	public ArrayList<TransportBase> createTransports() {
		
		ArrayList<TransportBase> transports=new ArrayList<TransportBase>();		
		
		TransportChat transportchat=new TransportChat(this);		
		transports.add(transportchat);
		
		return transports;
	}

	@Override
	public String getServerUrl() {
		// TODO Auto-generated method stub
		return URL_SERVER;
	}

}
