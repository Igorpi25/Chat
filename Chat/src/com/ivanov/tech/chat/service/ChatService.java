package com.ivanov.tech.chat.service;

import java.util.ArrayList;

import com.ivanov.tech.communicator.service.TransportBase;
import com.ivanov.tech.profile.service.TransportProfile;

public class ChatService extends com.ivanov.tech.communicator.service.CommunicatorService{

	public final static String URL_DOMEN="igorpi25.ru";//Websocket server URL and port
    public final static String URL_SERVER="ws://"+URL_DOMEN+":8001";//Websocket server URL and port
    public final static String URL_START_SERVER="http://"+URL_DOMEN+"/v2/communicator/start";
	
	@Override
	public ArrayList<TransportBase> createTransports() {
				
		ArrayList<TransportBase> transports=new ArrayList<TransportBase>();		
		
		TransportChat transportchat=new TransportChat(this);		
		TransportProfile transportprofile=new TransportProfile(this);
		
		transports.add(transportchat);
		transports.add(transportprofile);
									
		return transports;
	}

	@Override
	public String getServerUrl() {
		// TODO Auto-generated method stub
		return URL_SERVER;
	}

	@Override
	public String getRestartServerUrl() {		
		return URL_START_SERVER;
	}

	@Override
	public String getCommunicatorServiceClass() {		
		return ChatService.class.getCanonicalName();
	}

}
