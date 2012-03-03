
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

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author ross
 */
public class App {

    public static void main(String[] args) {

        // Prepare the Xero Client
        XeroClient xeroClient = null;
        try {
            XeroClientProperties clientProperties = new XeroClientProperties();
            clientProperties.load(new FileInputStream("./xeroApi.properties"));
            xeroClient = new XeroClient(clientProperties);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Retrieve a list of Invoices
        try {

            ArrayOfInvoice arrayOfExistingInvoices = xeroClient.getInvoices();
            if (arrayOfExistingInvoices != null && arrayOfExistingInvoices.getInvoice() != null) {

                System.out.println("");
                for (Invoice invoice : arrayOfExistingInvoices.getInvoice()) {
                    System.out.println("Invoice: " + invoice.getInvoiceID());
                }

                // Retrieve an invoice as a PDF 
                // (can be used to retrieve json too, just change application/pdf to application/json)
                if (!arrayOfExistingInvoices.getInvoice().isEmpty()) {
                    xeroClient.getInvoiceAsPdf(arrayOfExistingInvoices.getInvoice().get(0).getInvoiceID());
                }
            }

        } catch (XeroClientException ex) {
            ex.printDetails();
        } catch (XeroClientUnexpectedException ex) {
            ex.printStackTrace();
        }

        // Create an Invoice
        Invoice invoice = null;
        try {

            ArrayOfInvoice arrayOfInvoice = new ArrayOfInvoice();
            List<Invoice> invoices = arrayOfInvoice.getInvoice();
            invoice = new Invoice();

            Contact contact = new Contact();
            contact.setName("Jane Smith");
            contact.setEmailAddress("jane@smith.com");
            invoice.setContact(contact);

            ArrayOfLineItem arrayOfLineItem = new ArrayOfLineItem();
            List<LineItem> lineItems = arrayOfLineItem.getLineItem();
            LineItem lineItem = new LineItem();
            lineItem.setAccountCode("200");
            BigDecimal qty = new BigDecimal("2");
            lineItem.setQuantity(qty);
            BigDecimal amnt = new BigDecimal("50.00");
            lineItem.setUnitAmount(amnt);
            lineItem.setDescription("Programming books");
            lineItem.setLineAmount(qty.multiply(amnt));
            lineItems.add(lineItem);
            invoice.setLineItems(arrayOfLineItem);

            invoice.setDate(Calendar.getInstance());
            Calendar due = Calendar.getInstance();
            due.set(due.get(Calendar.YEAR), due.get(Calendar.MONTH) + 1, 20);
            invoice.getLineAmountTypes().add("Inclusive");
            invoice.setDueDate(due);
            invoice.setInvoiceNumber("INV-API-001");
            invoice.setType(InvoiceType.ACCREC);
            invoice.setStatus(InvoiceStatus.AUTHORISED);
            invoices.add(invoice);

            xeroClient.postInvoices(arrayOfInvoice);
        } catch (XeroClientException ex) {
            ex.printDetails();
        } catch (XeroClientUnexpectedException ex) {
            ex.printStackTrace();
        }

        // Create a new Contact
        try {

            ArrayOfContact arrayOfContact = new ArrayOfContact();
            List<Contact> contacts = arrayOfContact.getContact();


            Contact contact1 = new Contact();
            contact1.setName("John Smith");
            contact1.setEmailAddress("john@smith.com");
            contacts.add(contact1);
            xeroClient.postContacts(arrayOfContact);

        } catch (XeroClientException ex) {
            ex.printDetails();
        } catch (XeroClientUnexpectedException ex) {
            ex.printStackTrace();
        }

        // Add a payment to an exisiting Invoice
        try {

            Invoice invoice1 = new Invoice();
            invoice1.setInvoiceNumber("INV-0038");

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

            xeroClient.postPayments(arrayOfPayment);

        } catch (XeroClientException ex) {
            ex.printDetails();
        } catch (XeroClientUnexpectedException ex) {
            ex.printStackTrace();
        }
    }
}
