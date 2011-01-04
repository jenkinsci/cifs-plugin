package com.slide.hudson.plugins;

import java.io.IOException;

import java.net.URI;
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
		String expanded = Util.replaceMacro(entry.getSourceFile(), envVars);
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
		String subRoot = Util.replaceMacro(entry.getFilePath(), envVars);

		cifsShare.mkdirs(subRoot, listener.getLogger());

		for (FilePath sourceFile : sourceFiles) {
			fileCount += copyFile(entry, sourceFile, subRoot, baseSourceDir);
		}

		listener.getLogger().println(
				"transferred " + fileCount + " files to " + subRoot);
		return fileCount;
	}

	public int copyFile(Entry entry, FilePath sourceFile, String destDir,
			String baseSourceDir) throws IOException, InterruptedException {

		String subRoot = destDir;
		cifsShare.mkdirs(subRoot, listener.getLogger());

		// make flatten backwards compatible
		boolean flatten = entry.getFlatten();
		if (!flatten) {
			if (!destDir.endsWith("/")) {
				destDir += "/";
			}

			String relDir = getRelativeToCopyBaseDirectory(baseSourceDir,
					sourceFile);
			if (relDir.startsWith("/")) {
				relDir = relDir.substring(1);
			}

			subRoot = destDir + relDir;
			cifsShare.mkdirs(subRoot, listener.getLogger());
		}

		// and upload the file in the root or subdir
		return cifsShare.upload(sourceFile, subRoot, envVars, 
				listener.getLogger());
	}

	private String getRelativeToCopyBaseDirectory(String baseDir,
			FilePath sourceFile) throws IOException, InterruptedException {

		URI sourceFileURI = sourceFile.toURI().normalize();
		String relativeSourceFile = sourceFileURI.getPath().replaceFirst(
				baseDir, "");
		int lastSlashIndex = relativeSourceFile.lastIndexOf("/");
		if (lastSlashIndex == -1) {
			return ".";
		} else {
			return relativeSourceFile.substring(0, lastSlashIndex);
		}
	}

}
