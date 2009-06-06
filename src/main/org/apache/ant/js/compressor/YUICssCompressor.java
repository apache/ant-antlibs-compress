/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
package org.apache.ant.js.compressor;

/**
 * Task for compressing CSS files using the Yahoo! UI compressor
 * Requires yui compressor jar in $ANT_HOME/lib; ~/.ant/lib or on CLASSPATH
 * @author kevj@apache.org
 */
public class YUICssCompressor extends YUICompressorTask {

	public void execute() {
		super.setType("css");
		super.execute();
	}
}