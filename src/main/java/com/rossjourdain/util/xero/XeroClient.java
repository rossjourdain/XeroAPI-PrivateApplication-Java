
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

/**
 *
 * @author ross
 */
public class XeroClient {

    private String endpointUrl;
    private String consumerKey;
    private String consumerSecret;
    private String privateKey;

    public XeroClient(String endpointUrl, String consumerKey, String consumerSecret, String privateKey) {
        this.endpointUrl = endpointUrl;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.privateKey = privateKey;
    }

    public XeroClient(XeroClientProperties clientProperties) {
        this.endpointUrl = clientProperties.getEndpointUrl();
        this.consumerKey = clientProperties.getConsumerKey();
        this.consumerSecret = clientProperties.getConsumerSecret();
        this.privateKey = clientProperties.getPrivateKey();
    }

    public OAuthAccessor buildAccessor() {

        OAuthConsumer consumer = new OAuthConsumer(null, consumerKey, null, null);
        consumer.setProperty(RSA_SHA1.PRIVATE_KEY, privateKey);
        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.accessToken = consumerKey;
        accessor.tokenSecret = consumerSecret;

        return accessor;
    }

    public ArrayOfInvoice getInvoices() throws XeroClientException, XeroClientUnexpectedException {
        ArrayOfInvoice arrayOfInvoices = null;
        try {
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Invoices", null);
            arrayOfInvoices = XeroXmlManager.xmlToInvoices(response.getBodyAsStream());
        } catch (OAuthProblemException ex) {
            throw new XeroClientException("Error getting invoices", ex);
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        return arrayOfInvoices;
    }

    public Report getReport(String reportUrl) throws XeroClientException, XeroClientUnexpectedException {
        Report report = null;
        try {
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.GET, endpointUrl + "Reports" + reportUrl, null);
            ResponseType responseType = XeroXmlManager.xmlToResponse(response.getBodyAsStream());
            if (responseType != null && responseType.getReports() != null
                    && responseType.getReports().getReport() != null && responseType.getReports().getReport().size() > 0) {
                report = responseType.getReports().getReport().get(0);
            }
        } catch (OAuthProblemException ex) {
            throw new XeroClientException("Error getting invoices", ex);
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
        return report;
    }

    public void postContacts(ArrayOfContact arrayOfContact) throws XeroClientException, XeroClientUnexpectedException {
        try {
            String contactsString = XeroXmlManager.contactsToXml(arrayOfContact);
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Contacts", OAuth.newList("xml", contactsString));
        } catch (OAuthProblemException ex) {
            throw new XeroClientException("Error posting contancts", ex);
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    public void postInvoices(ArrayOfInvoice arrayOfInvoices) throws XeroClientException, XeroClientUnexpectedException {
        try {
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            String contactsString = XeroXmlManager.invoicesToXml(arrayOfInvoices);
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Invoices", OAuth.newList("xml", contactsString));
        } catch (OAuthProblemException ex) {
            throw new XeroClientException("Error posting invoices", ex);
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    public void postPayments(ArrayOfPayment arrayOfPayment) throws XeroClientException, XeroClientUnexpectedException {
        try {
            OAuthClient client = new OAuthClient(new HttpClient3());
            OAuthAccessor accessor = buildAccessor();
            String paymentsString = XeroXmlManager.paymentsToXml(arrayOfPayment);
            OAuthMessage response = client.invoke(accessor, OAuthMessage.POST, endpointUrl + "Payments", OAuth.newList("xml", paymentsString));
        } catch (OAuthProblemException ex) {
            throw new XeroClientException("Error posting payments", ex);
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
        }
    }

    public File getInvoiceAsPdf(String invoiceId) throws XeroClientException, XeroClientUnexpectedException {

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
            throw new XeroClientException("Error getting PDF of invoice " + invoiceId, ex);
        } catch (Exception ex) {
            throw new XeroClientUnexpectedException("", ex);
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

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
