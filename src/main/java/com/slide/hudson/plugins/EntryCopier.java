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

import java.io.IOException;

import java.net.URI;
import java.util.Map;

import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

/**
 * <p>
 * Copies files for the Entry
 * </p>
 *
 * @author Alex Earl
 * @author Christian Knuechel
 */
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

		if(entry.getPurge()) {
		     cifsShare.delete(subRoot, listener.getLogger());
		}

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
