/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import twitter4j.conf.Configuration;
import twitter4j.internal.async.Dispatcher;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.json.DataObjectFactoryUtil;
import twitter4j.internal.json.z_T4JInternalFactory;
import twitter4j.internal.json.z_T4JInternalJSONImplFactory;
import twitter4j.internal.logging.Logger;
import twitter4j.internal.org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.8
 */
abstract class AbstractStreamImplementation implements StreamImplementation {
    protected static final Logger logger = Logger.getLogger(AbstractStreamImplementation.class);

    private boolean streamAlive = true;
    private BufferedReader br;
    private InputStream is;
    private HttpResponse response;
    protected final Dispatcher dispatcher;
    private final Configuration CONF;
    protected z_T4JInternalFactory factory;

    /*package*/

    AbstractStreamImplementation(Dispatcher dispatcher, InputStream stream, Configuration conf) throws IOException {
        this.is = stream;
        this.br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        this.dispatcher = dispatcher;
        this.CONF = conf;
        this.factory = new z_T4JInternalJSONImplFactory(conf);
    }
    /*package*/

    AbstractStreamImplementation(Dispatcher dispatcher, HttpResponse response, Configuration conf) throws IOException {
        this(dispatcher, response.asStream(), conf);
        this.response = response;
    }

    protected String parseLine(String line) {
        return line;
    }

    abstract class StreamEvent implements Runnable {
        String line;

        StreamEvent(String line) {
            this.line = line;
        }
    }

    protected void handleNextElement() throws TwitterException {
        if (!streamAlive) {
            throw new IllegalStateException("Stream already closed.");
        }
        try {
            String line = br.readLine();
            if (null == line) {
                //invalidate this status stream
                throw new IOException("the end of the stream has been reached");
            }
            dispatcher.invokeLater(new StreamEvent(line) {
                public void run() {
                    line = parseLine(line);
                    if (line.length() > 0) {
                        try {
                            if (CONF.isJSONStoreEnabled()) {
                                DataObjectFactoryUtil.clearThreadLocalMap();
                            }
                            JSONObject json = new JSONObject(line); //verify that it can be parsed
                            if (logger.isDebugEnabled()) {
                                logger.debug("Received:", CONF.isPrettyDebugEnabled() ? json.toString(1) : json.toString());
                            }
                            onEvent(line);
                        } catch (Exception ex) {
                            onException(ex);
                        }
                    }
                }
            });

        } catch (IOException ioe) {
            try {
                is.close();
            } catch (IOException ignore) {
            }
            boolean isUnexpectedException = streamAlive;
            streamAlive = false;
            if (isUnexpectedException) {
                throw new TwitterException("Stream closed.", ioe);
            }
        }
    }
    
    public void onEvent(String line) {
        logger.warn("Unhandled event: onEvent");
    }

    public void onException(Exception e) {
        logger.warn("Unhandled event: ", e.getMessage());
    }

    public void close() throws IOException {
        streamAlive = false;
        is.close();
        br.close();
        if (response != null) {
            response.disconnect();
        }
    }

}
