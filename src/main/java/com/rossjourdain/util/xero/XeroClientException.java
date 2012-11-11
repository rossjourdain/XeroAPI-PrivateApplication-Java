
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

import com.sun.xml.internal.ws.streaming.DOMStreamReader;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.oauth.OAuthProblemException;
import org.w3c.dom.Element;

/**
 *
 * @author ross
 */
public class XeroClientException extends Exception {

  private ApiExceptionExtended apiException;

  public XeroClientException(String message, OAuthProblemException oAuthProblemException) {

    super(message, oAuthProblemException);

    String oAuthProblemExceptionString = null;

    oAuthProblemExceptionString = XeroXmlManager.oAuthProblemExceptionToXml(oAuthProblemException);
    System.out.println(oAuthProblemExceptionString);
    apiException = (ApiExceptionExtended) XeroXmlManager.xmlToException(oAuthProblemExceptionString);
  }

  public ApiException getApiException() {
    return apiException;
  }

  public void printDetails() {
    
    try {
      
      System.out.println("");
      System.out.println(this.getMessage());
      System.out.println("Message: " + apiException.getMessage());
      System.out.println("Error " + apiException.getErrorNumber() + ": " + apiException.getType());

      Element e = (Element) apiException.getElements().getDataContractBase();
      String elementType = e.getAttribute("xsi:type");

      JAXBContext context = JAXBContext.newInstance(ResponseType.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      // unmarshaller.setEventHandler(new DefaultValidationEventHandler());

      JAXBElement jaxbElement = null;

      System.out.println("Type is: " + elementType);


      if ("Invoice".equals(elementType)) {
        System.out.println("Processing Invoice");
        jaxbElement = unmarshaller.unmarshal(new DOMStreamReader(e), Invoice.class);
        Invoice invoice = (Invoice) jaxbElement.getValue();
        System.out.println("Invoice ID: " + invoice.getInvoiceID());
        if (invoice.getDate() != null) {
          System.out.println("Invoice Date  : " + invoice.getDate().getTime());
        }
      } else if ("Payment".equals(elementType)) {
        System.out.println("Processing Payment");
        jaxbElement = unmarshaller.unmarshal(new DOMStreamReader(e), Payment.class);
        Payment payment = (Payment) jaxbElement.getValue();
        System.out.println("Payment ID  : " + payment.getPaymentID());
        if (payment.getDate() != null) {
          System.out.println("Payment Date  : " + payment.getDate().getTime());
        }
      } else {
        throw new RuntimeException("Unrecognised type: " + elementType);
      }

      if (jaxbElement != null) {
        DataContractBase dataContractBase = (DataContractBase) jaxbElement.getValue();
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

      System.out.println("");
      /* Add this back in if you need more details on the exception */
      //System.out.println("" + apiExceptionString);
      //System.out.println("");
    } catch (JAXBException ex) {
      ex.printStackTrace();
    }
  }
}
