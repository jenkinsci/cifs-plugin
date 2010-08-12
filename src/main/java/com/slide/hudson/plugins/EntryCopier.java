package com.slide.hudson.plugins;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Map;

import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

public class EntryCopier {
	private AbstractBuild<?, ?> build;
	private BuildListener listener;
	private Map<String, String> envVars;
	private URI workSpaceDir;
	private CIFSShare cifsShare;

	public EntryCopier(AbstractBuild<?, ?> build, BuildListener listener,
			CIFSShare cifsShare) throws IOException, InterruptedException {
		this.build = build;
		this.listener = listener;
		this.cifsShare = cifsShare;
		envVars = build.getEnvironment(listener);
		workSpaceDir = build.getWorkspace().toURI().normalize();
	}

	public int copy(Entry entry) throws IOException, InterruptedException {
		// prepare sources
		String expanded = Util.replaceMacro(entry.sourceFile, envVars);
		FilePath[] sourceFiles = null;
		String baseSourceDir = workSpaceDir.getPath();

		FilePath tmp = new FilePath(build.getWorkspace(), expanded);

		if (tmp.exists() && tmp.isDirectory()) { // Directory
			sourceFiles = tmp.list("**/*");
			baseSourceDir = tmp.toURI().normalize().getPath();
			listener.getLogger().println(
					"Preparing to copy directory : " + baseSourceDir);
		} else { // Files
			sourceFiles = build.getWorkspace().list(expanded);
			baseSourceDir = workSpaceDir.getPath();
			listener.getLogger().println(workSpaceDir);
		}

		if (sourceFiles.length == 0) { // Nothing
			listener.getLogger().println("No file(s) found: " + expanded);
			return 0;
		}

		int fileCount = 0;

		// prepare common destination
		String subRoot = Util.replaceMacro(entry.filePath, envVars);

		cifsShare.mkdirs(subRoot, listener.getLogger());

		for (FilePath sourceFile : sourceFiles) {
			cifsShare
					.upload(sourceFile, subRoot, envVars, listener.getLogger());
			fileCount++;
		}

		listener.getLogger().println(
				"transferred " + fileCount + " files to " + subRoot);
		return fileCount;
	}
}
