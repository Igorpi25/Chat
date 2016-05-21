package com.ivanov.tech.chat.service;

import java.util.ArrayList;

import com.ivanov.tech.communicator.service.TransportBase;

public class ChatService extends com.ivanov.tech.communicator.service.CommunicatorService{

	@Override
	public ArrayList<TransportBase> createTransports() {
		
		ArrayList<TransportBase> transports=new ArrayList<TransportBase>();		
		
		TransportChat transportchat=new TransportChat(this);		
		transports.add(transportchat);
		
		return transports;
	}

}
