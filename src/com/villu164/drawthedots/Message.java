package com.villu164.drawthedots;

import android.content.Context;
import android.widget.Toast;

public class Message {
	private Context context;
	public Message(Context context){
		this.context = context;
	}
	
	public void message(String message){
		message(message,false);
	}

	public void message(String message, boolean long_message){
		int message_length = Toast.LENGTH_SHORT;
		if (long_message) message_length = Toast.LENGTH_LONG;
		Toast.makeText(this.context, message,message_length).show();
	}

}
