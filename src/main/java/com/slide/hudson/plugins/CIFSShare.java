package com.slide.hudson.plugins;

import hudson.FilePath;
import hudson.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import jcifs.smb.SmbFile;

 


/**
 * <p>
 * This class
 * </p>
 * <p>
 * HeadURL: $HeadURL: http://z-bld-02:8080/zxdev/zxant_test_environment/trunk/formatting/codeTemplates.xml $<br />
 * Date: $Date: 2008-04-22 11:53:34 +0200 (Di, 22 Apr 2008) $<br />
 * Revision: $Revision: 2451 $<br />
 * </p>
 * 
 * @author $Author: ZANOX-COM\fit $
 */
public class CIFSShare {

	/** The Constant DEFAULT_CIFS_PORT. */
	private static final int DEFAULT_SMB_PORT = 445;
	
	/** The server. */
	private String server;

	/** The time out. */
	private int timeOut;

	/** The port. */
	private int port;

	/** The username. */
	private String username;

	/** The password. */
	private String password;
	
	/** The login domain */
	private String domain;

	/** The CIFS dir. */
	private String shareDir = "/";

	/**
	 * Instantiates a new FTP site.
	 */
	public CIFSShare() {

	}

	/**
	 * Instantiates a new FTP site.
	 * 
	 * @param hostname
	 *          the hostname
	 * @param port
	 *          the port
	 * @param timeOut
	 *          the time out
	 * @param username
	 *          the username
	 * @param password
	 *          the password
	 * @param domain
	 * 			the domain         
	 * @param cifsDir
	 *          the cifs dir
	 */
	public CIFSShare(String server, int port, int timeOut, String username, 
			String password, String domain, String cifsDir) {
		this.server = server;
		this.port = port;
		this.timeOut = timeOut;
		this.username = username;
		this.password = password;
		this.domain = domain;
		this.shareDir = cifsDir;
	}

	/**
	 * Instantiates a new CIFS share.
	 * 
	 * @param hostname
	 *          the hostname
	 * @param port
	 *          the port
	 * @param timeOut
	 *          the time out
	 * @param username
	 *          the username
	 * @param password
	 *          the password
	 */
	public CIFSShare(String server, String port, String timeOut, String username, 
			String password, String domain) {
		this.server = server;
		try {
			this.port = Integer.parseInt(port);
			this.timeOut = Integer.parseInt(timeOut);
		} catch (Exception e) {
			this.port = DEFAULT_SMB_PORT;
		}
		this.username = username;
		this.password = password;
		this.domain = domain;
	}

	/**
	 * Gets the time out.
	 * 
	 * @return the time out
	 */
	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * Sets the time out.
	 * 
	 * @param timeOut
	 *          the new time out
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * Gets the hostname.
	 * 
	 * @return the hostname
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Sets the hostname.
	 * 
	 * @param hostname
	 *          the new hostname
	 */
	public void setServer(String server) {
		this.server = server.trim();
	}

	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public String getPort() {
		return "" + port;
	}

	/**
	 * Sets the port.
	 * 
	 * @param port
	 *          the new port
	 */
	public void setPort(String port) {
		if (port != null) {
			try {
				this.port = Integer.parseInt(port);
			} catch (NumberFormatException e) {
				this.port = DEFAULT_SMB_PORT;
			}
		} else {
			this.port = DEFAULT_SMB_PORT;
		}
	}

	/**
	 * Gets the integer port.
	 * 
	 * @return the integer port
	 */
	public int getIntegerPort() {
		return port;
	}

	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 * 
	 * @param username
	 *          the new username
	 */
	public void setUsername(String username) {
		this.username = username.trim();
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password
	 *          the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Gets the domain portion of the URL.
	 * @return the domain of the share login
	 */
	public String getDomain() {
		return domain;
	}
	
	/**
	 * Sets the domain portion of the URL.
	 * @param domain the domain for the share login
	 */
	public void setDomain(String domain) {
		this.domain = domain.trim();
	}

	/**
	 * Gets the SMB share dir.
	 * 
	 * @return the SMB share dir
	 */
	public String getShareDir() {
		return shareDir;
	}

	/**
	 * Sets the ftp dir.
	 * 
	 * @param rootRepositoryPath
	 *          the new ftp dir
	 */
	public void setShareDir(String sharePath) {		
		while(shareDir.startsWith("/") && shareDir.length() > 1) {
			shareDir = shareDir.substring(1);
		}
		this.shareDir = sharePath.trim();
	}
	
	/**
	 * Gets the FULL URL for the share including escaped password.
	 * @return The FULL URL for the share, including password.
	 */
	public String getUrl() {
		return getUrl(true);
	}
	
	/**
	 * Gets the URL corresponding to this share with 
	 * optional password.
	 * @param withPassword true if password should be included.
	 * @return The URL representation of the share.
	 */
	private String getUrl(boolean withPassword) {
		StringBuffer url = new StringBuffer("smb://");
		
		if(username != null && username.length() > 0) {			
			if(domain != null && domain.length() > 0) {
				url.append(Util.rawEncode(domain) + ";");		
			}
			
			url.append(Util.rawEncode(username));
			
			if(withPassword && password != null && password.length() > 0) {
				url.append(":" + Util.rawEncode(password));				
			} 
			
			url.append("@");
		}
		
		url.append(server);
		
		if(port > 0 && port != DEFAULT_SMB_PORT) {
			url.append(":" + port);
		}

		url.append("/");
		
		if(shareDir != null && shareDir.length() > 0) {
			url.append(shareDir);
			
			if(!shareDir.endsWith("/")) {
				url.append("/");
			}
		}
		
		return url.toString();
	}
	
	/**
	 * Gets the display safe (no password) URL for the share.
	 * @return The display safe URL for the share.
	 */
	public String getDisplayUrl() {
		return getUrl(false);
	}

	/**
	 * Uploads a file (or multiple files) to the share defined by this object.
	 */
	public void upload(FilePath filePath, String destDir, Map<String, String> envVars, PrintStream logger) throws IOException, InterruptedException {
		if (filePath.isDirectory()) {
			FilePath[] subfiles = filePath.list("**/*");
			if (subfiles != null) {
				for (int i = 0; i < subfiles.length; i++) {
					upload(subfiles[i], destDir, envVars, logger);
				}
			}
		} else {
			String localfilename = filePath.getName();	
		
			SmbFile remoteFile = new SmbFile(new SmbFile(new SmbFile(
					getUrl()), destDir), localfilename);

			InputStream in = filePath.read();
			
			OutputStream out = remoteFile.getOutputStream();
			byte[] data = new byte[1024 * 1024]; // should make this a global parameter or something
			int read = 0;
			while((read = in.read(data)) > 0) {
				out.write(data, 0, read);
			}
			out.close();
			in.close();
		}

	}
	
	/**
	 * Mkdirs.
	 * 
	 * @param filePath
	 *          the file path
	 * @param logger
	 *          the logger
	 * 
	 * @throws SftpException
	 *           the sftp exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public void mkdirs(String filePath, PrintStream logger) throws IOException {
		SmbFile current = new SmbFile(new SmbFile(getUrl()), filePath);
		if(!current.exists()) {
			logger.println("creating " + getDisplayUrl() + filePath);
			current.mkdirs();
		}
	}
}
;