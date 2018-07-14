package com.deloitte.gitutil.plugin.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.deloitte.gitutil.plugin.model.MailConfigModel;



public class ConfigProvider {

	public static String changeSetPath ;
	public static MailConfigModel mailConfig;
	
	public static void init(String changeSetFilePath, String mailConfigFilePath) throws IOException {
		
		changeSetPath = changeSetFilePath;
		
		Properties prop = new Properties();
//		InputStream input = new FileInputStream("config.properties");
		InputStream input;
		input = new FileInputStream(mailConfigFilePath);
		prop.load(input);
		mailConfig =new MailConfigModel();
		mailConfig.setUsername(prop.getProperty("mail.username"));
		mailConfig.setPassword(prop.getProperty("mail.password"));
		mailConfig.setSmtp_host(prop.getProperty("mail.smtp.host"));
		mailConfig.setSmtp_auth(prop.getProperty("mail.smtp.auth"));
		if(prop.getProperty("mail.smtp.starttls.enable") != null) {
			mailConfig.setSmtp_starttls_enable(prop.getProperty("mail.smtp.starttls.enable"));	
		}
		if(prop.getProperty("mail.smtp.socketFactory.port") != null) {
			mailConfig.setSmtp_socketFactory_port(prop.getProperty("mail.smtp.socketFactory.port"));
		}
		
		mailConfig.setSmtp_port(prop.getProperty("mail.smtp.port"));
		mailConfig.setMail_to(prop.getProperty("mail.to"));
		mailConfig.setSubject(prop.getProperty("mail.subject"));
		mailConfig.setBody(prop.getProperty("mail.body"));
		mailConfig.setRegards(prop.getProperty("mail.regards"));
	//	storagePath =   prop.getProperty("storage");  
	}	
}
	
