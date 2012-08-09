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

import twitter4j.auth.OAuthSupport;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.0
 */
public interface TwitterStream extends OAuthSupport, TwitterBase {
    /**
     * Adds a ConnectionLifeCycleListener
     *
     * @param listener listener to be added
     * @since Twitter4J 2.1.7
     */
    void addConnectionLifeCycleListener(ConnectionLifeCycleListener listener);

    /**
     * @param listener listener to add
     * @since Twitter4J 2.1.8
     */
    void addListener(StreamListener listener);

    /**
     * Starts listening on all public statuses. Available only to approved parties and requires a signed agreement to access. Please do not contact us about access to the firehose. If your service warrants access to it, we'll contact you.
     *
     * @param count Indicates the number of previous statuses to stream before transitioning to the live stream.
     * @see twitter4j.StatusStream
     * @see <a href="https://dev.twitter.com/docs/streaming-api/methods">Streaming API Methods statuses/firehose</a>
     * @since Twitter4J 2.0.4
     */
    void firehose(final int count);
    
    void jsonStream(String relUrl, int count);

    /**
     * shutdown internal stream consuming thread
     *
     * @since Twitter4J 2.1.7
     */
    void cleanUp();

    /**
     * Shuts down internal dispatcher thread shared by all TwitterStream instances.<br>
     *
     * @since Twitter4J 2.1.9
     */
    void shutdown();
}