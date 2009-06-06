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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Base task for wrapping the Yahoo! UI compressor library
 * Requires yui compressor jar in $ANT_HOME/lib; ~/.ant/lib or on CLASSPATH
 * @author kevj@apache.org
 */
public class YUICompressorTask extends Task {

	private boolean verbose;
	private boolean nomunge;
	private String type;
	private String lineBreak;
	private boolean preserveSemi;
	private boolean disableOptimization;
	private String charset;
	private String outputPath;
	private List inputResources = new ArrayList();
		
	public void execute() {
		try {
			validate();
			for(Iterator i = inputResources.iterator(); i.hasNext();) {
				FileSet fs = (FileSet)i.next();
				for(Iterator j = fs.iterator(); j.hasNext();) {
					FileResource f = (FileResource)j.next();
					minify(f);
				}
			}		
		} catch (Exception e) {
			log("Error occurred processing file ");
			e.printStackTrace();
		}
	}
	
	private void validate() throws BuildException {
		if(null == type || "".equals(type)) {
			throw new BuildException("Type property must be specified <js|css>");
		}
		if(null == charset || "".equals(charset)) {
			setCharset("UTF-8");
			log("Charset not set; using [UTF-8]");
		}
		if(null == lineBreak || "".equals(lineBreak)) {
			setLineBreak("-1");
			log("Linebreak position not set; using [-1]");
		} else if (lineBreak.matches("\\D+")) {
			throw new BuildException("Linebreak must be an integer");
		}
	}

	/**
	 * @param f the FileResource to compress
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void minify(FileResource f) throws UnsupportedEncodingException,
			FileNotFoundException, IOException {

		Reader in = new InputStreamReader(new FileInputStream(f.getFile().getAbsolutePath()), charset);
		Writer out = getWriter(f);
		if(verbose) {
			log("Minifying: "+f.getFile().getAbsolutePath());	
		}
		
		if("js".equals(getType())) {
			JavaScriptCompressor c = getJavaScriptCompressor(in);
			//in case we are overwriting the input file, ensure that it's closed first
			in.close();
			in = null;
			c.compress(out, 
					Integer.parseInt(getLineBreak()), 
					isNomunge(), 
					isVerbose(), 
					isPreserveSemi(), 
					isDisableOptimization() 
			);
			out.close();
		} else if("css".equals(getType())) {
			CssCompressor c = getCssCompressor(in);
			//in case we are overwriting the input file, ensure that it's closed first
			in.close();
			in = null;
			c.compress(out, Integer.parseInt(getLineBreak()));
			out.close();
		}
	}

	private Writer getWriter(FileResource f) throws IOException {
		Writer out;
		out = new OutputStreamWriter(
				null != getOutputPath() && getOutputPath().trim() != "" ?
						new FileOutputStream(getOutputPath() + File.separator + f.getFile().getName()) :
						f.getOutputStream()
				);
		return out;
	}
	
	protected JavaScriptCompressor getJavaScriptCompressor(Reader in) throws IOException {
		
		JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

            public void warning(String message, String sourceName,
                    int line, String lineSource, int lineOffset) {
                if (line < 0) {
                    log("\n[WARNING] " + message);
                } else {
                    log("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
                }
            }

            public void error(String message, String sourceName,
                    int line, String lineSource, int lineOffset) {
                if (line < 0) {
                    log("\n[ERROR] " + message);
                } else {
                    log("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
                }
            }

            public EvaluatorException runtimeError(String message, String sourceName,
                    int line, String lineSource, int lineOffset) {
                error(message, sourceName, line, lineSource, lineOffset);
                return new EvaluatorException(message);
            }
        });
		
		return compressor;
	}
	
	protected CssCompressor getCssCompressor(Reader in) throws IOException {
		return new CssCompressor(in);
	}
	
	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isNomunge() {
		return nomunge;
	}

	public void setNomunge(boolean nomunge) {
		this.nomunge = nomunge;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLineBreak() {
		return lineBreak;
	}

	public void setLineBreak(String lineBreak) {
		this.lineBreak = lineBreak;
	}

	public boolean isPreserveSemi() {
		return preserveSemi;
	}

	public void setPreserveSemi(boolean preserveSemi) {
		this.preserveSemi = preserveSemi;
	}

	public boolean isDisableOptimization() {
		return disableOptimization;
	}

	public void setDisableOptimization(boolean disableOptimization) {
		this.disableOptimization = disableOptimization;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	
	public void addFileSet(FileSet fs) {
        	add(fs);
	}
	
	public void add(ResourceCollection res) {
		inputResources.add(res);
	}
}