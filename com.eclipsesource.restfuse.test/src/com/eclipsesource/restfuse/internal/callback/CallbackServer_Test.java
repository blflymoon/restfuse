/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 ******************************************************************************/ 
package com.eclipsesource.restfuse.internal.callback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.eclipsesource.restfuse.DefaultCallbackResource;
import com.eclipsesource.restfuse.Status;
import com.eclipsesource.restfuse.annotation.Callback;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class CallbackServer_Test {
  
  private CallbackServer server;

  @Rule
  public TestWatcher watchman = new TestWatcher() {

    @Override
    public void starting( Description description ) {
      Callback callbackAnnotation = description.getAnnotation( Callback.class );
      CallbackStatement statement = mock( CallbackStatement.class );
      server = new CallbackServer( callbackAnnotation, CallbackServer_Test.this, statement );
      server.start();
      
      ClientResponse response = sendRequest();
      assertEquals( Status.NO_CONTENT.getStatusCode(), response.getStatus() );
      assertEquals( 10000, server.getTimeout() );
    }
    
    @Override
    public void finished( Description description ) {
      try {
        sendRequest();
        fail();
      } catch( ClientHandlerException expected ) {}
    }

    private ClientResponse sendRequest() {
      WebResource resource = Client.create().resource( "http://localhost:10042/test" );
      ClientResponse response = resource.get( ClientResponse.class );
      return response;
    }
    
  };
  
  private class TestResource extends DefaultCallbackResource {
    // no content
  }
  
  @Test
  @Callback( port = 10042, path = "/test", timeout = 10000, resource = TestResource.class )
  public void fakeTestMethod() {
    assertTrue( server.wasCalled() );
    server.stop();
  }
  
}
