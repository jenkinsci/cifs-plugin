/*
Copyright (c) 2011 Alex Earl, Christian Knuechel

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/


package com.slide.hudson.plugins;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * <p>
 * This class implements the data object for the CIFS plugin. The fields can be configured in the job
 * configuration page in hudson.
 * </p>
 * 
 * @author Alex Earl
 * @author Christian Knuechel
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

    /**
     * True if files should be purged from the destination before copying, false if files should not be purged.
     */
    private boolean purge = false;
    
    public Entry() {
    	this.filePath = null;
    	this.sourceFile = null;
    	this.flatten = false;
	this.purge = false;
    }
    
    @DataBoundConstructor
    public Entry(String filePath, String sourceFile, Boolean flatten, 
		    Boolean purge) {    	
    	this.filePath = filePath;
    	this.sourceFile = sourceFile;
    	this.flatten = flatten;
	this.purge = purge;
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

    /**
     * True if files should be purged from the target directory, false if existing files should be left along.
     * @return the purge
     */
    public boolean getPurge() {
        return purge;
    }

    /**
     * True if files should be purged from the target directory.
     * @param purge the purge to set
     */
    public void setPurge(Boolean purge) {
	this.purge = purge;
    }
}
