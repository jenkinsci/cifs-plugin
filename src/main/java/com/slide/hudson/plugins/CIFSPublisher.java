package com.slide.hudson.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

import org.kohsuke.stapler.StaplerRequest;

/**
 * <p>
 * This class implements the ftp publisher process by using the {@link FTPSite}.
 * </p>
 * <p>
 * HeadURL: $HeadURL: http://z-bld-02:8080/zxdev/zxant_test_environment/trunk/formatting/codeTemplates.xml $<br />
 * Date: $Date: 2008-04-22 11:53:34 +0200 (Di, 22 Apr 2008) $<br />
 * Revision: $Revision: 2451 $<br />
 * </p>
 * 
 * @author $Author: ZANOX-COM\fit $
 * 
 */
public class CIFSPublisher extends Notifier {

	/**
	 * Hold an instance of the Descriptor implementation of this publisher.
	 */
	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	private String shareName;
	private final List<Entry> entries = new ArrayList<Entry>();
	private String winsServer;
	
	//private Boolean flatten = true;

//	public void setFlatten(boolean flatten) {
//		this.flatten = flatten;
//	}
//
//	public boolean isFlatten() {
//		return flatten;
//	}
	
	public void setWinsServer(String winsServer) {
		this.winsServer = winsServer;
	}

	public CIFSPublisher() {
		
	}

	/**
	 * The constructor which take a configured ftp site name to publishing the artifacts.
	 * 
	 * @param siteName
	 *          the name of the ftp site configuration to use
	 */
	public CIFSPublisher(String shareName) {
		this.shareName = shareName;
	}

	/**
	 * The getter for the entries field. (this field is set by the UI part of this plugin see config.jelly file)
	 * 
	 * @return the value of the entries field
	 */
	public List<Entry> getEntries() {
		return entries;
	}

	/**
	 * This method returns the configured FTPSite object which match the siteName of the FTPPublisher instance. (see Manage Hudson and System
	 * Configuration point FTP)
	 * 
	 * @return the matching FTPSite or null
	 */
	public CIFSShare getShare() {
		CIFSShare[] shares = DESCRIPTOR.getShares();
		if (shareName == null && shares.length > 0) {
			// default
			return shares[0];
		}
		for (CIFSShare share : shares) {
			if (share.getServer().equals(shareName)) {
				return share;
			}
		}
		return null;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param build
	 * @param launcher
	 * @param listener
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 *           {@inheritDoc}
	 * @see hudson.tasks.BuildStep#perform(hudson.model.Build, hudson.Launcher, hudson.model.BuildListener)
	 */
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		if (build.getResult() == Result.FAILURE || build.getResult() == Result.ABORTED) {
			// build failed. don't post
			return true;
		}

		CIFSShare share = null;
		try {
			share = getShare();
			listener.getLogger().println("Connecting to " + share.getServer());
			
			EntryCopier copier = new EntryCopier(build, listener, share);
			
			if(winsServer != null && winsServer.length() > 0) {
				System.setProperty("jcifs.netbios.wins", winsServer);
			}

			int copied = 0;

			for (Entry e : entries) {
				copied += copier.copy(e);
			}

			listener.getLogger().println("Transfered " + copied + " files.");

		} catch (Throwable th) {
			th.printStackTrace(listener.error("Failed to upload files"));
			build.setResult(Result.UNSTABLE);
		} 

		return true;
	}

	/**
	 * <p>
	 * This class holds the metadata for the FTPPublisher.
	 * </p>
	 * 
	 * @author $Author: ZANOX-COM\fit $
	 * @see Descriptor
	 */
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		private final CopyOnWriteList<CIFSShare> shares = new CopyOnWriteList<CIFSShare>();

		/**
		 * The default constructor.
		 */
		public DescriptorImpl() {
			super(CIFSPublisher.class);
			load();
		}

		/**
		 * The name of the plugin to display them on the project configuration web page.
		 * 
		 * {@inheritDoc}
		 * 
		 * @return {@inheritDoc}
		 * @see hudson.model.Descriptor#getDisplayName()
		 */
		@Override
		public String getDisplayName() {
			return "Publish artifacts to CIFS";
		}

		/**
		 * Return the location of the help document for this publisher.
		 * 
		 * {@inheritDoc}
		 * 
		 * @return {@inheritDoc}
		 * @see hudson.model.Descriptor#getHelpFile()
		 */
		@Override
		public String getHelpFile() {
			return "/plugin/cifspublisher/help.html";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		/**
		 * This method is called by hudson if the user has clicked the add button of the CIFS share hosts point in the System Configuration
		 * web page. It's create a new instance of the {@link CIFSPublisher} class and added all configured CIFS shares to this instance by calling
		 * the method {@link CIFSPublisher#getEntries()} and on it's return value the addAll method is called.
		 * 
		 * {@inheritDoc}
		 * 
		 * @param req
		 *          {@inheritDoc}
		 * @return {@inheritDoc}
		 * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest)
		 */
		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData) {
			CIFSPublisher pub = new CIFSPublisher();
			pub.setWinsServer(formData.getString("winsServer"));
			//pub.setFlatten(formData.getBoolean("flatten"));
			//pub.setUseTimestamps(formData.getBoolean("useTimestamps"));
			req.bindParameters(pub, "publisher.");
			req.bindParameters(pub, "cifs.");
			pub.getEntries().addAll(req.bindParametersToList(Entry.class, "cifs.entry."));
			return pub;
		}

		/**
		 * The getter of the sites field.
		 * 
		 * @return the value of the sites field.
		 */
		public CIFSShare[] getShares() {
			Iterator<CIFSShare> it = shares.iterator();
			int size = 0;
			while (it.hasNext()) {
				it.next();
				size++;
			}
			return shares.toArray(new CIFSShare[size]);
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @param req
		 *          {@inheritDoc}
		 * @return {@inheritDoc}
		 * @see hudson.model.Descriptor#configure(org.kohsuke.stapler.StaplerRequest)
		 */
		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) {
			shares.replaceBy(req.bindParametersToList(CIFSShare.class, "cifs."));
			save();
			return true;
		}
		
		/**
		 * This method validates the current entered CIFS configuration data. That is made by create a CIFS connection.
		 * 
		 * @param request
		 *          the current {@link javax.servlet.http.HttpServletRequest}
		 */
		public FormValidation doLoginCheck(StaplerRequest request) {
			String server = Util.fixEmpty(request.getParameter("server"));
			String domain = Util.fixEmptyAndTrim(request.getParameter("domain"));
			String user = Util.fixEmptyAndTrim(request.getParameter("user"));
			String password = Util.fixEmptyAndTrim(request.getParameter("pass"));
			
			if (server == null) { // server is not entered yet
				return FormValidation.ok();
			}
			
			CIFSShare share = new CIFSShare(server, request.getParameter("port"), request.getParameter("timeOut"), user,
			    password, domain);
			share.setShareName(request.getParameter("shareDir"));
			try {
				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, 
						user, password);
				
				SmbFile serv = new SmbFile(share.getUrl());
				
				if(serv.exists()) {
					SmbFile file = new SmbFile(share.getUrl(), auth);
					if(file.exists() && file.isFile()) {
						return FormValidation.error("Destination is a file");
					} else {
						return FormValidation.ok();
					}
				} else {
					return FormValidation.error("Server does not exist.");
				}
			} catch (Exception e) {
				return FormValidation.error(e.getMessage());
			}
		}
	}
}
