
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.OAuthResponseMessage;
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
        ArrayOfInvoice arrayOfExistingInvoices = getInvoices();
        if (arrayOfExistingInvoices != null && arrayOfExistingInvoices.getInvoice() != null) {

            System.out.println("");
            for (Invoice invoice : arrayOfExistingInvoices.getInvoice()) {
                System.out.println("Invoice: " + invoice.getInvoiceID());
            }

            // Retrieve an invoice as a PDF 
            // (can be used to retrieve json too, just change application/pdf to application/json)
            if (!arrayOfExistingInvoices.getInvoice().isEmpty()) {
                getInvoiceAsPdf(arrayOfExistingInvoices.getInvoice().get(0).getInvoiceID());
            }
        }


        // Create an Invoice
        ArrayOfInvoice arrayOfInvoice = new ArrayOfInvoice();
        List<Invoice> invoices = arrayOfInvoice.getInvoice();
        Invoice invoice = new Invoice();

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

        postInvoices(arrayOfInvoice);


        // Create a new Contact
        ArrayOfContact arrayOfContact = new ArrayOfContact();
        List<Contact> contacts = arrayOfContact.getContact();


        Contact contact1 = new Contact();
        contact1.setName("John Smith");
        contact1.setEmailAddress("john@smith.com");
        contacts.add(contact1);
        postContacts(arrayOfContact);


        // Add a payment to an exisiting Invoice
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
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Invoices", null);
            arrayOfInvoices = XeroXmlManager.xmlToInvoices(response.getBodyAsStream());
        } catch (OAuthProblemException ex) {
            handleException("Error getting invoices", ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return arrayOfInvoices;
    }

    private static boolean postContacts(ArrayOfContact arrayOfContact) {
        boolean success = false;
        try {
            String contactsString = XeroXmlManager.contactsToXml(arrayOfContact);
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Contacts", OAuth.newList("xml", contactsString));
            success = true;
        } catch (OAuthProblemException ex) {
            handleException("Error posting contancts", ex);
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
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Invoices", OAuth.newList("xml", contactsString));
        } catch (OAuthProblemException ex) {
            handleException("Error posting invoices", ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void postPayments(ArrayOfPayment arrayOfPayment) {
        try {
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            String paymentsString = XeroXmlManager.paymentsToXml(arrayOfPayment);
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Payments", OAuth.newList("xml", paymentsString));
        } catch (OAuthProblemException ex) {
            handleException("Error posting payments", ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static File getInvoiceAsPdf(String invoiceId) {

        File file = null;
        InputStream in = null;
        FileOutputStream out = null;

        try {

            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();

            OAuthMessage request = accessor.newRequestMessage(OAuthMessage.GET, endpointUrl + "Invoices" + "/" + invoiceId, null);
            request.getHeaders().add(new OAuth.Parameter("Accept", "application/pdf"));
            OAuthResponseMessage response = client.access(request, ParameterStyle.BODY);


            file = new File("Invoice-" + invoiceId + ".pdf");

            if (response != null && response.getHttpResponse() != null && (response.getHttpResponse().getStatusCode() / 2) != 2) {
                in = response.getBodyAsStream();
                out = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } else {
                throw response.toOAuthProblemException();
            }

        } catch (OAuthProblemException ex) {
            handleException("Error getting PDF of invoice " + invoiceId, ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
            }
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException ex) {
            }
        }
        return file;
    }

    private static void handleException(String message, OAuthProblemException oAuthProblemException) {

        String apiExceptionString = XeroXmlManager.oAuthProblemExceptionToXml(oAuthProblemException);


        ApiException apiException = XeroXmlManager.xmlToException(apiExceptionString);

        System.out.println("");
        System.out.println(message);
        System.out.println("Message: " + apiException.getMessage());
        System.out.println("Error " + apiException.getErrorNumber() + ": " + apiException.getType());
        if (apiException.getElements() != null && apiException.getElements().getDataContractBase() != null) {
            List<DataContractBase> dataContractBases = apiException.getElements().getDataContractBase();
            for (DataContractBase dataContractBase : dataContractBases) {

                System.out.println("DataType: " + dataContractBase.getClass().getSimpleName());
                //if(dataContractBase instanceof Invoice) {
                //    System.out.println("Invoice Number: " + ((Invoice)dataContractBase).getInvoiceNumber());
                //} else if(dataContractBase instanceof Invoice) {
                //    System.out.println("Payment ID: " + ((Payment)dataContractBase).getPaymentID());
                //}

                if (dataContractBase.getWarnings() != null && dataContractBase.getWarnings().getWarning() != null) {
                    List<Warning> warnings = dataContractBase.getWarnings().getWarning();
                    for (int i = 0; i < warnings.size(); i++) {
                        Warning warning = warnings.get(i);
                        System.out.println("Warning " + (i + 1) + ": " + warning.getMessage());
                    }
                }
                if (dataContractBase.getValidationErrors() != null && dataContractBase.getValidationErrors().getValidationError() != null) {
                    List<ValidationError> validationErrors = dataContractBase.getValidationErrors().getValidationError();
                    for (int i = 0; i < validationErrors.size(); i++) {
                        ValidationError validationError = validationErrors.get(i);
                        System.out.println("Validation Error " + (i + 1) + ": " + validationError.getMessage());
                    }
                }
            }
        }
        System.out.println("");
        /* Add this back in if you need more details on the exception */
        //System.out.println("" + apiExceptionString);
        //System.out.println("");
    }
}
