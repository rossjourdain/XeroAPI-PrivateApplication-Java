
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
  private List<ValidationError> validationErrors;
  private List<Warning> warnings;
  private Object modelObject;

  public XeroClientException(String message, OAuthProblemException oAuthProblemException) {

    super(message, oAuthProblemException);

    String oAuthProblemExceptionString = null;

    oAuthProblemExceptionString = XeroXmlManager.oAuthProblemExceptionToXml(oAuthProblemException);
    System.out.println(oAuthProblemExceptionString);
    apiException = (ApiExceptionExtended) XeroXmlManager.xmlToException(oAuthProblemExceptionString);
    unmarshalAdditionalData();

    /* Add this back in if you need more details on the exception */
    //System.out.println("" + apiExceptionString);
    //System.out.println("");
  }

  public ApiException getApiException() {
    return apiException;
  }

  public List<ValidationError> getValidationErrors() {
    return validationErrors;
  }

  public List<Warning> getWarnings() {
    return warnings;
  }

  public Object getModelObject() {
    return modelObject;
  }

  private void unmarshalAdditionalData() {

    try {

      Element e = (Element) apiException.getElements().getDataContractBase();
      String elementType = e.getAttribute("xsi:type");

      JAXBContext context = JAXBContext.newInstance(ResponseType.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();

      // unmarshaller.setEventHandler(new DefaultValidationEventHandler());

      JAXBElement jaxbElement = null;
      System.out.println("Type is: " + elementType);

      if ("Invoice".equals(elementType)) {
        jaxbElement = unmarshaller.unmarshal(new DOMStreamReader(e), Invoice.class);
        modelObject = (Invoice) jaxbElement.getValue();
      } else if ("Payment".equals(elementType)) {
        jaxbElement = unmarshaller.unmarshal(new DOMStreamReader(e), Payment.class);
        modelObject = (Payment) jaxbElement.getValue();
      } else if ("Contact".equals(elementType)) {
        jaxbElement = unmarshaller.unmarshal(new DOMStreamReader(e), Contact.class);
        modelObject = (Contact) jaxbElement.getValue();
      } else {
        throw new RuntimeException("Unrecognised type: " + elementType);
      }

      if (jaxbElement != null) {
        DataContractBase dataContractBase = (DataContractBase) jaxbElement.getValue();

        if (dataContractBase.getWarnings() != null && dataContractBase.getWarnings().getWarning() != null) {
          warnings = dataContractBase.getWarnings().getWarning();
        }
        if (dataContractBase.getValidationErrors() != null && dataContractBase.getValidationErrors().getValidationError() != null) {
          validationErrors = dataContractBase.getValidationErrors().getValidationError();
        }
      }

    } catch (JAXBException ex) {
      ex.printStackTrace();
    }

  }

  public void printDetails() {

    System.out.println("");
    System.out.println(this.getMessage());
    System.out.println("Message: " + apiException.getMessage());
    System.out.println("Error " + apiException.getErrorNumber() + ": " + apiException.getType());
    
    System.out.println("" + modelObject);
    System.out.println("" + modelObject.getClass());
    
    
    if (modelObject instanceof Invoice) {
        System.out.println("Processing Invoice");
        Invoice invoice = (Invoice) modelObject;

        System.out.println("Invoice ID: " + invoice.getInvoiceID());
        if (invoice.getDate() != null) {
          System.out.println("Invoice Date: " + invoice.getDate().getTime());
        }
      } else if (modelObject instanceof Payment) {
        System.out.println("Processing Payment");
        Payment payment = (Payment) modelObject;

        System.out.println("Payment ID: " + payment.getPaymentID());
        if (payment.getDate() != null) {
          System.out.println("Payment Date: " + payment.getDate().getTime());
        }
      } else if (modelObject instanceof Contact) {
        System.out.println("Processing Contact");
        Contact contact = (Contact) modelObject;

        System.out.println("Contact ID: " + contact.getContactID());
        System.out.println("Contact Name: " + contact.getName());
      } else {
        System.out.println("Unrecognised type: " + modelObject);
      }
    

    for (int i = 0; i < warnings.size(); i++) {
      Warning warning = warnings.get(i);
      System.out.println("Warning " + (i + 1) + ": " + warning.getMessage());
    }

    for (int i = 0; i < validationErrors.size(); i++) {
      ValidationError validationError = validationErrors.get(i);
      System.out.println("Validation Error " + (i + 1) + ": " + validationError.getMessage());
    }

  }
}
