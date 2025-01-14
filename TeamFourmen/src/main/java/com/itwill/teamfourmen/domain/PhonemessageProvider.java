package com.itwill.teamfourmen.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;

@Component
public class PhonemessageProvider {


	    private DefaultMessageService messageService;

	    @PostConstruct
	    private void init(){
	        this.messageService = NurigoApp.INSTANCE.initialize("NCSIOX3WCYDPNPIL", "X9YNL3DHJ14ERPYXDHFEG42K9SHADMOQ", "https://api.coolsms.co.kr");
	    }
		

	    
	    public boolean sendCertificationphone(String phone, String certificationNumber) {
	    	  Message message = new Message();
		        message.setFrom("01090841760");
		        message.setTo(phone);
		        message.setText("[FOURMEN] 아래의 인증번호를 입력해주세요\n" + certificationNumber);
	    	

	        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
	       return true;
	    	
	    }
	    
	
}
