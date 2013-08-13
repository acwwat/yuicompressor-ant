/*
 * Copyright (c) 2013, Anthony Wat
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.acwwat.ant.taskdefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * <p>
 * Runs the YUI Compressor. This task provides more control with batch
 * processing and output location through file sets and mapper in Ant over the
 * command-line interface.
 * </p>
 * 
 * <h3>Parameters</h3>
 * <table border="1">
 * <tr>
 * <th>Attribute</th>
 * <th>Description</th>
 * <th>Required</th>
 * </tr>
 * <tr>
 * <td>charSet</td>
 * <td>The character set that the compressor should use to read and write files.
 * </td>
 * <td>No; Defaults to <code>UTF-8</code></td>
 * </tr>
 * <tr>
 * <td>destDir</td>
 * <td>The destination directory for the compressed files. The default location
 * is the same as the source directory from each specified file set if not set.</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>disableOptimizations</td>
 * <td>Whether all micro-optimizations should be disabled by the JavaScript
 * compressor. This corresponds to the <code>--disable-optimizations</code>
 * command-line option. This value has no effect on CSS compression.</td>
 * <td>No; Defaults to <code>false</code></td>
 * </tr>
 * <tr>
 * <td>failOnError</td>
 * <td>Whether this task should fail when it encounters errors when compressing
 * files.</td>
 * <td>No; Defaults to <code>true</code></td>
 * </tr>
 * <tr>
 * <td>lineBreak</td>
 * <td>The column position to insert a line break to split long lines. This
 * corresponds to the <code>--line-break</code> command-line option. Specify 0
 * to insert a line break after each semicolon in JavaScript files, and after
 * each rule in CSS files.</td>
 * <td>No; Defaults to <code>-1</code> (no split)</td>
 * </tr>
 * <tr>
 * <td>noMunge</td>
 * <td>Whether JavaScript files should only be minified but not obfuscated by
 * the JavaScript compressor. This corresponds to the <code>--no-munge</code>
 * command-line option. This value has no effect on CSS compression.</td>
 * <td>No; Defaults to <code>false</code></td>
 * </tr>
 * <tr>
 * <td>preserveSemi</td>
 * <td>Whether all semicolons should be preserved by the JavaScript compressor.
 * This corresponds to the <code>--preserve-semi</code> command-line option.
 * This value has no effect on CSS compression.</td>
 * <td>No; Defaults to <code>false</code></td>
 * </tr>
 * <tr>
 * <td>type</td>
 * <td>The type of input files. This corresponds to the <code>--type</code>
 * command-line option. If not set, this Ant task will detect the type of input
 * files based on file extensions.</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>verbose</td>
 * <td>Whether informational messages and warnings should be displayed. This
 * corresponds to the <code>--verbose</code> command-line option.</td>
 * <td>No; Defaults to <code>false</code></td>
 * </tr>
 * </table>
 * <h3>Nested elements</h3> <h4>fileset</h4>
 * <p>
 * The set of files to be compressed. At least one <code>&lt;fileset&gt;</code>
 * nested element must be specified.
 * </p>
 * <h4>
 * mapper</h4>
 * <p>
 * (Optional) The mapper to map output file names.
 * </p>
 * <h3>Examples</h3>
 * <p>
 * <p>
 * The following example compresses all JavaScript files in the
 * <code>${src.dir}/js</code> directory and all CSS files in the
 * <code>${src.dir}/css</code> directory with default settings. The compressed
 * output will overwrite the original source files.
 * </p>
 * <p>
 * <code>&lt;yuicompressor&gt;<br />
	&nbsp;&nbsp;&nbsp;&nbsp;&lt;fileset dir=&quot;${src.dir}/js&quot; includes=&quot;**&#47;*.js&quot; /&gt;<br />
	&nbsp;&nbsp;&nbsp;&nbsp;&lt;fileset dir=&quot;${src.dir}/css&quot; includes=&quot;**&#47;*.css&quot; /&gt;<br />
&lt;/yuicompressor&gt;</code>
 * </p>
 * The following example compresses all JavaScript files in the
 * <code>${src.dir}</code> directory to the <code>${bin.dir}</code> directory
 * with the suffix <code>-min</code> added to the file name and verbose output
 * enabled.</p>
 * <p>
 * <code>&lt;yuicompressor destdir=&quot;${bin.dir}&quot; verbose=&quot;true&quot;&gt;<br />
	&nbsp;&nbsp;&nbsp;&nbsp;&lt;fileset dir=&quot;${src.dir}&quot; includes=&quot;**&#47;*.js&quot; /&gt;<br />
	&nbsp;&nbsp;&nbsp;&nbsp;&lt;mapper type=&quot;glob&quot; from=&quot;*.js&quot; to=&quot;*-min.js&quot; /&gt;<br />
&lt;/yuicompressor&gt;</code>
 * </p>
 * 
 * 
 * @author Anthony Wat
 */
public class YUICompressorTask extends Task {

	/**
	 * The CSS file type name.
	 */
	public static final String TYPE_CSS = "css";

	/**
	 * The JavaScript file type name.
	 */
	public static final String TYPE_JS = "js";

	/**
	 * The character set that the compressor should use to read and write files.
	 */
	private String charSet = "UTF-8";

	/**
	 * The overriding destination directory.
	 */
	private File destDirOverride = null;

	/**
	 * Whether all micro-optimizations should be disabled by the JavaScript
	 * compressor.
	 */
	private boolean disableOptimizations = false;

	/**
	 * Whether this task should fail when it encounters errors when compressing
	 * files.
	 */
	private boolean failOnError = true;

	/**
	 * The list of source file sets.
	 */
	private List<FileSet> fileSets = new ArrayList<FileSet>();

	/**
	 * The column position to insert a line break to split long lines.
	 */
	private int lineBreak = -1;

	/**
	 * Whether the <code>lineBreak</code> attribute is set in the Ant task.
	 */
	private boolean lineBreakSet = false;

	/**
	 * The mapper that will be used to map destination file names.
	 */
	private Mapper mapper = null;

	/**
	 * Whether JavaScript files should only be minified but not obfuscated by
	 * the JavaScript compressor.
	 */
	private boolean noMunge = false;

	/**
	 * Whether all semicolons should be preserved by the JavaScript compressor.
	 */
	private boolean preserveSemi = false;

	/**
	 * The type of input files.
	 */
	private String typeOverride = null;

	/**
	 * Whether informational messages and warnings should be displayed.
	 */
	private boolean verbose = false;

	/**
	 * Added a source file set.
	 * 
	 * @param fileSet
	 *            The source file set.
	 */
	public void addFileSet(FileSet fileSet) {
		fileSets.add(fileSet);
	}

	/**
	 * Sets the mapper that will be used to map destination file names.
	 * 
	 * @param mapper
	 *            The mapper that will be used to map destination file names.
	 * @throws BuildException
	 *             If a mapper is already specified.
	 */
	public void addMapper(Mapper mapper) throws BuildException {
		if (this.mapper != null) {
			throw new BuildException("Only one nested <mapper> element can "
					+ "be specified.");
		}
		this.mapper = mapper;
	}

	/**
	 * Compresses the source JavaScript or CSS file and saves the output to the
	 * destination file.
	 * 
	 * @param srcFile
	 *            The source file.
	 * @param destFile
	 *            The destination file.
	 * @throws IOException
	 */
	private void compress(File srcFile, File destFile) throws IOException {
		// The logic of this method mirrors that of the main method in the
		// YUICompressor class (version 2.4.8)
		log("Compressing " + srcFile.getAbsolutePath() + ".");
		InputStreamReader in = null;
		OutputStreamWriter out = null;
		try {
			String type = (typeOverride != null) ? typeOverride : srcFile
					.getName()
					.substring(srcFile.getName().lastIndexOf('.') + 1);
			in = new InputStreamReader(new FileInputStream(srcFile), charSet);
			if (type.equalsIgnoreCase(TYPE_JS)) {
				final String filePath = srcFile.getAbsolutePath();
				JavaScriptCompressor compressor = new JavaScriptCompressor(in,
						new ErrorReporter() {

							public void error(String message,
									String sourceName, int line,
									String lineSource, int lineOffset) {
								log("[ERROR] in " + filePath, Project.MSG_ERR);
								if (line < 0) {
									log("  " + message, Project.MSG_ERR);
								} else {
									log("  " + line + ':' + lineOffset + ':'
											+ message, Project.MSG_ERR);
								}
							}

							public EvaluatorException runtimeError(
									String message, String sourceName,
									int line, String lineSource, int lineOffset) {
								error(message, sourceName, line, lineSource,
										lineOffset);
								return new EvaluatorException(message);
							}

							public void warning(String message,
									String sourceName, int line,
									String lineSource, int lineOffset) {
								log("[WARNING] in " + filePath,
										Project.MSG_WARN);
								if (line < 0) {
									log("  " + message, Project.MSG_WARN);
								} else {
									log("  " + line + ':' + lineOffset + ':'
											+ message, Project.MSG_WARN);
								}
							}

						});
				in.close();
				in = null;
				if (!destFile.getParentFile().exists()) {
					destFile.getParentFile().mkdirs();
				}
				out = new OutputStreamWriter(new FileOutputStream(destFile),
						charSet);
				compressor.compress(out, lineBreak, !noMunge, verbose,
						preserveSemi, disableOptimizations);
			} else if (type.equalsIgnoreCase(TYPE_CSS)) {
				CssCompressor compressor = new CssCompressor(in);
				in.close();
				in = null;
				if (!destFile.getParentFile().exists()) {
					destFile.getParentFile().mkdirs();
				}
				out = new OutputStreamWriter(new FileOutputStream(destFile),
						charSet);
				compressor.compress(out, lineBreak);
			} else {
				throw new BuildException("The file extension \"" + type
						+ "\" is not supported.");
			}
		} catch (IOException e) {
			throw new BuildException(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				throw new BuildException(e);
			}
		}
	}

	@Override
	public void execute() throws BuildException {
		validate();
		for (FileSet fileSet : fileSets) {
			DirectoryScanner ds = fileSet.getDirectoryScanner();
			String[] includedFiles = ds.getIncludedFiles();
			for (String includedFile : includedFiles) {
				File srcFile = new File(ds.getBasedir(), includedFile);
				File destDir = (destDirOverride != null) ? destDirOverride : ds
						.getBasedir();
				File destFile = new File(destDir, includedFile);
				if (mapper != null) {
					String[] mappedFileNames = mapper.getImplementation()
							.mapFileName(includedFile);
					if (mappedFileNames == null) {
						log("The specified mapper cannot map the source file \""
								+ includedFile + "\". Skipping.");
						continue;
					} else {
						destFile = new File(ds.getBasedir(), mappedFileNames[0]);
					}
				}
				try {
					compress(srcFile, destFile);
				} catch (Exception e) {
					if (failOnError) {
						throw new BuildException(e);
					} else {
						log(e.getMessage(), e, Project.MSG_ERR);
						continue;
					}
				}
			}
		}
	}

	/**
	 * Sets the character set that the compressor should use to read and write
	 * files. This corresponds to the <code>--charset</code> command-line
	 * option. The default value is <code>UTF-8</code> if not set.
	 * 
	 * @param charSet
	 *            The character set that the compressor should use to read and
	 *            write files.
	 */
	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}

	/**
	 * Sets the destination directory for the compressed files. The default
	 * location is the same as the source directory from each specified file set
	 * if not set.
	 * 
	 * @param destDir
	 *            The destination directory for the compressed files.
	 */
	public void setDestDir(File destDir) {
		this.destDirOverride = destDir;
	}

	/**
	 * Sets whether all micro-optimizations should be disabled by the JavaScript
	 * compressor. This corresponds to the <code>--disable-optimizations</code>
	 * command-line option. This value has no effect on CSS compression. The
	 * default value is <code>false</code> if not set.
	 * 
	 * @param disableOptimizations
	 *            <code>true</code> if all micro-optimizations should be
	 *            disabled by the JavaScript compressor; <code>false</code>
	 *            otherwise.
	 */
	public void setDisableOptimizations(boolean disableOptimizations) {
		this.disableOptimizations = disableOptimizations;
	}

	/**
	 * Sets whether this task should fail when it encounters errors when
	 * compressing files. When set to <code>false</code>, the task will report
	 * errors on compression failure, but will continue with the next file in
	 * the specified file sets. The default value is <code>false</code> if not
	 * set.
	 * 
	 * @param failOnError
	 *            <code>true</code> if this Ant task should fail when it
	 *            encounters errors when compressing files; <code>false</code>
	 *            otherwise.
	 */
	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	/**
	 * Sets the column position to insert a line break to split long lines. This
	 * corresponds to the <code>--line-break</code> command-line option. Specify
	 * 0 to insert a line break after each semicolon in JavaScript files, and
	 * after each rule in CSS files. The default behavior is to not add any line
	 * break to long lines if not set.
	 * 
	 * @param lineBreak
	 *            The column position to insert a line break to split long
	 *            lines.
	 */
	public void setLineBreak(int lineBreak) {
		this.lineBreak = lineBreak;
		lineBreakSet = true;
	}

	/**
	 * Sets whether JavaScript files should only be minified but not obfuscated
	 * by the JavaScript compressor. This corresponds to the
	 * <code>--no-munge</code> command-line option. This value has no effect on
	 * CSS compression. The default value is <code>false</code> if not set.
	 * 
	 * @param noMunge
	 */
	public void setNoMunge(boolean noMunge) {
		this.noMunge = noMunge;
	}

	/**
	 * Sets whether all semicolons should be preserved by the JavaScript
	 * compressor. This corresponds to the <code>--preserve-semi</code>
	 * command-line option. This value has no effect on CSS compression. The
	 * default value is <code>false</code> if not set.
	 * 
	 * @param preserveSemi
	 *            <code>true</code> if all semicolons should be preserved by the
	 *            JavaScript compressor; <code>false</code> otherwise.
	 */
	public void setPreserveSemi(boolean preserveSemi) {
		this.preserveSemi = preserveSemi;
	}

	/**
	 * Sets the type of input files. This corresponds to the <code>--type</code>
	 * command-line option. If not set, this Ant task will detect the type of
	 * input files based on file extensions.
	 * 
	 * @param type
	 *            The type of input files. Must be either <code>js</code> or
	 *            <code>css</code>.
	 */
	public void setType(String type) {
		this.typeOverride = type;
	}

	/**
	 * Sets whether informational messages and warnings should be displayed.
	 * This corresponds to the <code>--verbose</code> command-line option. The
	 * default value is <code>false</code> if not set.
	 * 
	 * @param verbose
	 *            <code>true</code> if information messages and warnings should
	 *            be displayed; <code>false</code> otherwise.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Validates attributes and nested elements.
	 * 
	 * @throws BuildException
	 *             If validation fails.
	 */
	private void validate() throws BuildException {
		if (fileSets.size() == 0) {
			throw new BuildException("At least one nested <fileset> element "
					+ "must be specified.");
		}
		if (!Charset.isSupported(charSet)) {
			throw new BuildException("The \"charset\" attribute specified "
					+ "does not refer to a supported character set.");
		}
		if (lineBreakSet && lineBreak < 0) {
			throw new BuildException("The \"lineBreak\" attribute must be a "
					+ "non-negative integer.");
		}
		if (typeOverride != null) {
			if (!typeOverride.equalsIgnoreCase(TYPE_CSS)
					&& !typeOverride.equalsIgnoreCase(TYPE_JS)) {
				throw new BuildException("The \"type\" attribute must be "
						+ "either \"css\" or \"js\".");
			}
		}
	}

}
