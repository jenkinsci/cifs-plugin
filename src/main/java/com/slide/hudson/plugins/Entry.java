package com.slide.hudson.plugins;

/**
 * <p>
 * This class implements the data object for the CIFS plugin. The fields can be configured in the job
 * configuration page in hudson.
 * </p>
 * <p>
 * HeadURL: $HeadURL:
 * http://z-bld-02:8080/zxdev/zxant_test_environment/trunk/formatting/codeTemplates.xml $<br />
 * Date: $Date: 2008-04-22 11:51:32 +0200 (Di, 22 Apr 2008) $<br />
 * Revision: $Revision: 2447 $<br />
 * </p>
 * 
 * @author $Author: ZANOX-COM\fit $
 * 
 */
public final class Entry {

    /**
     * Destination folder for the copy. May contain macros.
     */
    public String filePath;

    /**
     * File name relative to the workspace root to upload. If the sourceFile is directory then all
     * files in that directory will be copied to remote filePath directory recursively
     * <p>
     * May contain macro, wildcard.
     */
    public String sourceFile;
    
    /**
     * True if files should be flattened into a single directory, false if directory structure
     * should be maintained in the target share path.
     */
    public boolean flatten;    
}
