
/*
 *  Copyright 2011 Ross Jourdain
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.rossjourdain.util.xero;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient3.HttpClient3;
import net.oauth.signature.RSA_SHA1;

public class App {

        private static final String endpointUrl = "https://api.xero.com/api.xro/2.0/";

        private static final String consumerKey = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
        private static final String consumerSecret = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

        private static final String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n"
                + "-----END RSA PRIVATE KEY-----";

        public static void main(String[] args) {

        // Retrieve a list of Invoices
        ArrayOfInvoice arrayOfInvoice = getInvoices();
        for (Invoice invoice : arrayOfInvoice.getInvoice()) {
            System.out.println("Invoice: " + invoice.getInvoiceID());
        }

        
        // Create a new Contact
        ArrayOfContact arrayOfContact = new ArrayOfContact();
        List<Contact> contacts = arrayOfContact.getContact();
        
        
        Contact contact = new Contact();
        contact.setName("John Smith");
        contact.setEmailAddress("john@smith.com");
        contacts.add(contact);
        postContacts(arrayOfContact);


        // Add a payment to an exisiting Invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-0038");

        Account account = new Account();
        account.setCode("090");

        Payment payment = new Payment();
        payment.setAccount(account);
        payment.setInvoice(invoice);
        payment.setAmount(new BigDecimal("20.00"));
        payment.setDate(Calendar.getInstance());

        ArrayOfPayment arrayOfPayment = new ArrayOfPayment();
        List<Payment> payments = arrayOfPayment.getPayment();
        payments.add(payment);

        postPayments(arrayOfPayment);
        
        
    }

    private static OAuthAccessor buildAccessor() {

        OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, null, null);
        consumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.accessToken = consumerKey;
        accessor.tokenSecret = consumerSecret;

        return accessor;
    }

    private static ArrayOfInvoice getInvoices() {
        ArrayOfInvoice arrayOfInvoices = null;
        try {
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage message = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Invoices", null);
            arrayOfInvoices = XeroXmlManager.xmlToInvoices(message.getBodyAsStream());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return arrayOfInvoices;
    }

    private static boolean postContacts(ArrayOfContact arrayOfContact) {
        boolean success = false;
        try {
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            String contactsString = XeroXmlManager.contactsToXml(arrayOfContact);
            client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Contacts", OAuth.newList("xml", contactsString));
            success = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return success;
    }

    private static void postInvoices(ArrayOfInvoice arrayOfInvoices) {
        try {
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            String contactsString = XeroXmlManager.invoicesToXml(arrayOfInvoices);
            client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Invoices", OAuth.newList("xml", contactsString));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void postPayments(ArrayOfPayment arrayOfPayment) {
        OAuthMessage oAuthMessage = null;
        try {
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            String paymentsString = XeroXmlManager.paymentsToXml(arrayOfPayment);
            oAuthMessage = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Payments", OAuth.newList("xml", paymentsString));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void printResponse(ResponseType response) {

        System.out.println("Response: " + response.getStatus() + " " + response.getDateTimeUTC());

        if (response.getInvoices() != null && response.getInvoices().getInvoice() != null) {
            for (Invoice invoice : response.getInvoices().getInvoice()) {
                System.out.println("Invoice: " + invoice.getInvoiceID());
            }
        }
    }
}
