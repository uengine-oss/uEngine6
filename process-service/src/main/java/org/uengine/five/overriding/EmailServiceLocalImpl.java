package org.uengine.five.overriding;

import java.rmi.RemoteException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.uengine.five.service.EMailServerSoapBindingImpl;
import org.uengine.processmanager.EMailServiceLocal;

public class EmailServiceLocalImpl implements EMailServiceLocal {

    @Autowired
    EMailServerSoapBindingImpl eMailServerSoapBinding;

    @Override
    public void sendMail(String from, String to, String subject, String body) throws Exception {
        eMailServerSoapBinding.sendMail(from, to, subject, body);
    }

    @Override
    public void sendMail(String mailfrom, String mailfromName, String mailto, String subject, String text,
            List filenames, String ccmailid, String charSet) throws RemoteException {
        eMailServerSoapBinding.sendMail(mailfrom, mailfromName, mailto, subject, text, filenames, ccmailid, charSet);
    }
}
