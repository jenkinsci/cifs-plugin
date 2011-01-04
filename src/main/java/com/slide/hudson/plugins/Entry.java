package com.slide.hudson.plugins;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * <p>
 * This class implements the data object for the CIFS plugin. The fields can be configured in the job
 * configuration page in hudson.
 * </p>
 * <p>
 * HeadURL: $HeadURL$<br />
 * Date: $Date$<br />
 * Revision: $Revision$<br />
 * </p>
 * 
 * @author $Author$
 * 
 */
public final class Entry {
    /**
     * Destination folder for the copy. May contain macros.
     */
    private String filePath;

    /**
     * File name relative to the workspace root to upload. If the sourceFile is directory then all
     * files in that directory will be copied to remote filePath directory recursively
     * <p>
     * May contain macro, wildcard.
     */
    private String sourceFile;
    
    /**
     * True if files should be flattened into a single directory, false if directory structure
     * should be maintained in the target share path.
     */
    private boolean flatten = false;
    
    public Entry() {
    	this.filePath = null;
    	this.sourceFile = null;
    	this.flatten = false;
    }
    
    @DataBoundConstructor
    public Entry(String filePath, String sourceFile, Boolean flatten) {    	
    	this.filePath = filePath;
    	this.sourceFile = sourceFile;
    	this.flatten = flatten;
    }
    
    /**
     * Destination folder for the copy. May contain macros.
     * @return the filePath
     */
    public String getFilePath() {
    	return filePath;
    }

    /**
     * Destination folder for the copy. May contain macros.
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
    	this.filePath = filePath;
    }

    /**
     * File name relative to the workspace root to upload. If the sourceFile is directory then all
     * files in that directory will be copied to remote filePath directory recursively
     * <p>
     * May contain macro, wildcard.
     * @return the sourceFile
     */
    public String getSourceFile() {
        return sourceFile;
    }


    /**
     * File name relative to the workspace root to upload. If the sourceFile is directory then all
     * files in that directory will be copied to remote filePath directory recursively
     * <p>
     * May contain macro, wildcard.
     * @param sourceFile the sourceFile to set
     */
    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }


    /**
     * True if files should be flattened into a single directory, false if directory structure
     * should be maintained in the target share path.
     * @return the flatten
     */
    public boolean getFlatten() {
        return flatten;
    }

    
    /**
     * True if files should be flattened into a single directory, false if directory structure
     * should be maintained in the target share path.
     * @param flatten the flatten to set
     */
    public void setFlatten(Boolean flatten) {
        this.flatten = flatten;
    }

}
