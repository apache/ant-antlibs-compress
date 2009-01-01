package org.apache.ant.js.compressor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.File;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

import com.yahoo.platform.yui.compressor.YUICompressor;


public class YUICompressorTask extends Task {

	private boolean verbose;
	private boolean nomunge;
	private String type;
	private String lineBreak;
	private boolean preserveSemi;
	private boolean disableOptimization;
	private String charset;
	private String output;
	private String outputPath;
	private boolean mergeFiles;
	private List inputResources = new ArrayList();
	
	public void execute() {
		if(mergeFiles) {
			Commandline cmd = buildArgs();
			YUICompressor.main(cmd.getArguments());
		} else {
			for(Iterator i = inputResources.iterator(); i.hasNext();) {
				FileSet fs = (FileSet)i.next();
				for(Iterator j = fs.iterator(); j.hasNext();) {
					Commandline cmd = buildArgs();
					FileResource f = (FileResource)j.next();
					if(verbose) {
						log("Minifying: "+f.getFile().getAbsolutePath());	
					}
					cmd.createArgument().setValue("-o");
					if(null != outputPath && outputPath.trim() != "") {
						cmd.createArgument().setValue(outputPath + File.separator + f.getFile().getAbsolutePath());
					} else {
						cmd.createArgument().setValue(f.getFile().getAbsolutePath());
					}
					cmd.createArgument().setValue(f.getFile().getAbsolutePath());
					YUICompressor.main(cmd.getArguments());
				}
			
			}		
		}
	}

	protected Commandline buildArgs() {
		Commandline cmd = new Commandline();
		if(verbose) {
			cmd.createArgument().setValue("-v");
		}
		if(nomunge) {
			cmd.createArgument().setValue("--nomunge");
		}
		if(preserveSemi) {
			cmd.createArgument().setValue("--preserve-semi");
		}
		if(disableOptimization) {
			cmd.createArgument().setValue("--disable-optimizations");
		}
		if(null != lineBreak && lineBreak.trim() != "") {
			cmd.createArgument().setValue("--line-break");
			cmd.createArgument().setValue(lineBreak);
		}
		if(null != type && type.trim() != "") {
			cmd.createArgument().setValue("--type");
			cmd.createArgument().setValue(type);
		}
		if(null != charset && charset.trim() != "") {
			cmd.createArgument().setValue("--charset");
			cmd.createArgument().setValue(charset);
		} 
		if(null != output && output.trim() != "" && mergeFiles) {
			cmd.createArgument().setValue("-o");
			cmd.createArgument().setValue(output);
		}
		
		if(mergeFiles) {
			for(Iterator i = inputResources.iterator(); i.hasNext();) {
				FileSet fs = (FileSet)i.next();
				for(Iterator j = fs.iterator(); j.hasNext();) {
					FileResource f = (FileResource)j.next();
					cmd.createArgument().setValue(f.getFile().getAbsolutePath());
				}
			
			}
		}
		log("args: "+cmd.describeCommand());
		return cmd;
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

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
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

	public boolean isMergeFiles() {
		return mergeFiles;
	}

	public void setMergeFiles(boolean m) {
		mergeFiles = m;
	}
}
