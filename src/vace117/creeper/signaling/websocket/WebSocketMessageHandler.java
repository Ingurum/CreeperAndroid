/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package vace117.creeper.signaling.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import vace117.creeper.controller.command.CreeperCommand;
import vace117.creeper.controller.command.CreeperCommandType;
import vace117.creeper.logging.CreeperContext;
import vace117.creeper.webrtc.PeerConnectionManager;

public class WebSocketMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String request = frame.text();
        
        JSONObject envelope = (JSONObject) new JSONParser().parse(request);
        
        if ( envelope.keySet().size() == 1 ) {
        	String key = (String) envelope.keySet().iterator().next();
        	if ( envelope.get(key) instanceof JSONObject ) {
	    		JSONObject json = (JSONObject) envelope.get(key);
	
	    		if ( "sdpAnswer".equals(key) ) {
	        		SessionDescription answer = new SessionDescription(
	                		SessionDescription.Type.fromCanonicalForm(json.get("type").toString()), 
	                		json.get("sdp").toString());
	                
	                PeerConnectionManager.getInstance().setRemoteDescription(answer);
	        	}
	        	else if ( "ice".equals(key) ) {
	                IceCandidate candidate = new IceCandidate(
	                        json.get("sdpMid").toString(),
	                        Integer.valueOf(json.get("sdpMLineIndex").toString()),
	                        json.get("candidate").toString());
	                
	                PeerConnectionManager.getInstance().addRemoteIceCandidate(candidate);
	        	}
        	}
        	else if ( "command".equals(key) ) {
        		String commandString = envelope.get(key).toString();
        		
        		CreeperContext.getInstance().controller.dispatchToPi(new CreeperCommand(CreeperCommandType.getCommand(commandString)));
        	}
        }        
    }
}
