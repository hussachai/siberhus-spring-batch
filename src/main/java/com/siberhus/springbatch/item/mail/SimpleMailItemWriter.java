package com.siberhus.springbatch.item.mail;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class SimpleMailItemWriter implements ItemWriter<SimpleMailMessage> {
	
	private MailSender mailSender;
    
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void write(List<? extends SimpleMailMessage> messages) {
    	mailSender.send(messages.toArray(new SimpleMailMessage[0]));      
    }
    
}
