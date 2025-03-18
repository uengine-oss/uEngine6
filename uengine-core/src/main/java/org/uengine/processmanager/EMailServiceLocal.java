package org.uengine.processmanager;

import java.rmi.RemoteException;
import java.util.List;

public interface EMailServiceLocal {
	void sendMail(String from, String to, String subject, String body) throws Exception;

	void sendMail(String mailfrom, String mailfromName, String mailto, String subject, String text, List filenames,
			String ccmailid, String charSet) throws RemoteException;
}