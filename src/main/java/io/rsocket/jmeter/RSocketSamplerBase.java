/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.jmeter;

import io.rsocket.frame.FrameType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.parser.BaseParser;
import org.apache.jmeter.protocol.http.parser.LinkExtractorParseException;
import org.apache.jmeter.protocol.http.parser.LinkExtractorParser;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common constants and methods for HTTP samplers
 */
public abstract class RSocketSamplerBase extends AbstractSampler
    implements TestStateListener, TestIterationListener, ThreadListener {

  private static final long serialVersionUID = 243L;

  private static final Logger log = LoggerFactory.getLogger(RSocketSamplerBase.class);

  private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
      Arrays.asList(
          "org.apache.jmeter.config.gui.LoginConfigGui",
          "org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui",
          "org.apache.jmeter.config.gui.SimpleConfigGui",
          "org.apache.jmeter.protocol.http.gui.HeaderPanel",
          "org.apache.jmeter.protocol.http.control.DNSCacheManager",
          "org.apache.jmeter.protocol.http.gui.DNSCachePanel",
          "org.apache.jmeter.protocol.http.gui.AuthPanel",
          "org.apache.jmeter.protocol.http.gui.CacheManagerGui",
          "org.apache.jmeter.protocol.http.gui.CookiePanel",
          "org.apache.jmeter.testbeans.gui.TestBeanGUI"
      ));

  //+ JMX names - do not change
  public static final String METADATA = "RSocketSampler.metadata"; // $NON-NLS-1$

  //+ JMX names - do not change
  public static final String DATA = "RSocketSampler.data"; // $NON-NLS-1$

  public static final String SAMPLE_TIMEOUT = "RSocketSampler.sample_timeout"; // $NON-NLS-1$

  public static final String CONNECT_TIMEOUT = "RSocketSampler.connect_timeout"; // $NON-NLS-1$

  public static final String RESPONSE_TIMEOUT = "RSocketSampler.response_timeout"; // $NON-NLS-1$

  public static final String REQUEST_TYPE = "RSocketSampler.request_type"; // $NON-NLS-1$

  /**
   * This is the encoding used for the content, i.e. the charset name, not the header
   * "Content-Encoding"
   */
  public static final String CONTENT_ENCODING = "RSocketSampler.contentEncoding"; // $NON-NLS-1$

  public static final String ROUTE = "RSocketSampler.route"; // $NON-NLS-1$

  public static final String CONCURRENT_DWN = "RSocketSampler.concurrentDwn"; // $NON-NLS-1$

  public static final String CONCURRENT_POOL = "RSocketSampler.concurrentPool"; // $NON-NLS-1$

  public static final int CONCURRENT_POOL_SIZE = 6; // Default concurrent pool size for download embedded resources

  private static final String CONCURRENT_POOL_DEFAULT = Integer
      .toString(CONCURRENT_POOL_SIZE); // default for concurrent pool

  //- JMX names
  private static final int MAX_BYTES_TO_STORE_PER_REQUEST =
      JMeterUtils.getPropDefault("RSocketSampler.max_bytes_to_store_per_request",
          0); // $NON-NLS-1$ // default value: 0 don't truncate

  private static final int MAX_BUFFER_SIZE =
      JMeterUtils.getPropDefault("RSocketSampler.max_buffer_size", 65 * 1024); // $NON-NLS-1$

  public static final String DEFAULT_REQUEST_TYPE = FrameType.REQUEST_RESPONSE
      .name(); // $NON-NLS-1$

  private static final List<String> REQUESTTYPESLIST;

  static {
    REQUESTTYPESLIST = Arrays.asList(
        FrameType.REQUEST_FNF.name(),
        FrameType.REQUEST_RESPONSE.name(),
        FrameType.REQUEST_STREAM.name(),
        FrameType.REQUEST_CHANNEL.name(),
        FrameType.METADATA_PUSH.name()
    );
  }

  // @see mergeFileProperties
  // Must be private, as the file list needs special handling
  private static final String FILE_ARGS = "RSocketSampler.Files"; // $NON-NLS-1$
  // MIMETYPE is kept for backward compatibility with old test plans
  private static final String MIMETYPE = "RSocketSampler.mimetype"; // $NON-NLS-1$
  // FILE_NAME is kept for backward compatibility with old test plans
  private static final String FILE_NAME = "RSocketSampler.FILE_NAME"; // $NON-NLS-1$
  /* Shown as Parameter Name on the GUI */
  // FILE_FIELD is kept for backward compatibility with old test plans
  private static final String FILE_FIELD = "RSocketSampler.FILE_FIELD"; // $NON-NLS-1$

  public static final String CONTENT_TYPE = "RSocketSampler.CONTENT_TYPE"; // $NON-NLS-1$

  // IMAGE_PARSER now really means EMBEDDED_PARSER
  public static final String IMAGE_PARSER = "RSocketSampler.image_parser"; // $NON-NLS-1$

  // Embedded URLs must match this RE (if provided)
  public static final String EMBEDDED_URL_RE = "RSocketSampler.embedded_url_re"; // $NON-NLS-1$

  // Embedded URLs must not match this RE (if provided)
  public static final String EMBEDDED_URL_EXCLUDE_RE = "RSocketSampler.embedded_url_exclude_re"; // $NON-NLS-1$

  public static final String MONITOR = "RSocketSampler.monitor"; // $NON-NLS-1$

  // Derive the mapping of content types to parsers
  private static final Map<String, String> PARSERS_FOR_CONTENT_TYPE = new ConcurrentHashMap<>();
  // Not synch, but it is not modified after creation

  private static final String RESPONSE_PARSERS = // list of parsers
      JMeterUtils.getProperty("HTTPResponse.parsers");//$NON-NLS-1$

  // Bug 51939
  private static final boolean SEPARATE_CONTAINER =
      JMeterUtils.getPropDefault("RSocketSampler.separate.container", true); // $NON-NLS-1$

  static {
    String[] parsers = JOrphanUtils
        .split(RESPONSE_PARSERS, " ", true);// returns empty array for null
    for (final String parser : parsers) {
      String classname = JMeterUtils.getProperty(parser + ".className");//$NON-NLS-1$
      if (classname == null) {
        log.error("Cannot find .className property for {}, ensure you set property: '{}.className'",
            parser, parser);
        continue;
      }
      String typeList = JMeterUtils.getProperty(parser + ".types");//$NON-NLS-1$
      if (typeList != null) {
        String[] types = JOrphanUtils.split(typeList, " ", true);
        for (final String type : types) {
          registerParser(type, classname);
        }
      } else {
        log.warn(
            "Cannot find .types property for {}, as a consequence parser will not be used, to make it usable, define property:'{}.types'",
            parser, parser);
      }
    }
  }

  ////////////////////// Code ///////////////////////////

  protected RSocketSamplerBase() {
    setMetadata(new Arguments());
  }

  /**
   * Determine if the file should be sent as the entire Content body, i.e. without any additional
   * wrapping.
   *
   * @return true if specified file is to be sent as the body, i.e. there is a single file entry
   * which has a non-empty path and an empty Parameter name.
   */
  public boolean getSendFileAsPostBody() {
    // If there is one file with no parameter name, the file will
    // be sent as post body.
    HTTPFileArg[] files = getHTTPFiles();
    return (files.length == 1)
        && (files[0].getPath().length() > 0)
        && (files[0].getParamName().length() == 0);
  }

  private boolean hasNoMissingFile(HTTPFileArg[] files) {
    for (HTTPFileArg httpFileArg : files) {
      if (StringUtils.isEmpty(httpFileArg.getPath())) {
        log.warn("File {} is invalid as no path is defined", httpFileArg);
        return false;
      }
    }
    return true;
  }

  /**
   * Sets the PATH property; if the request is a GET or DELETE (and the path does not start with
   * http[s]://) it also calls {@link #parseArguments(String, String)} to extract and store any
   * query arguments.
   *
   * @param path            The new Path value
   * @param contentEncoding The encoding used for the querystring parameter values
   */
  public void setRoute(String path) {
    setProperty(ROUTE, path);
  }

  public String getRoute() {
    return getPropertyAsString(ROUTE);
  }

  public void setRequestType(String value) {
    setProperty(REQUEST_TYPE, value);
  }

  public String getRequestType() {
    return getPropertyAsString(REQUEST_TYPE);
  }

  /**
   * Sets the value of the encoding to be used for the content.
   *
   * @param charsetName the name of the encoding to be used
   */
  public void setContentEncoding(String charsetName) {
    setProperty(CONTENT_ENCODING, charsetName);
  }

  /**
   * @return the encoding of the content, i.e. its charset name
   */
  public String getContentEncoding() {
    return getPropertyAsString(CONTENT_ENCODING);
  }

  public void setMonitor(String value) {
    this.setProperty(MONITOR, value);
  }

  public void setMonitor(boolean truth) {
    this.setProperty(MONITOR, truth);
  }

  /**
   * @return boolean
   * @deprecated since 3.2 always returns false
   */
  @Deprecated
  public String getMonitor() {
    return "false";
  }

  /**
   * @return boolean
   * @deprecated since 3.2 always returns false
   */
  @Deprecated
  public boolean isMonitor() {
    return false;
  }

  public void setResponseTimeout(String value) {
    setProperty(RESPONSE_TIMEOUT, value, "");
  }

  public int getResponseTimeout() {
    return getPropertyAsInt(RESPONSE_TIMEOUT, 0);
  }

  // gets called from ctor, so has to be final
  public final void setMetadata(Arguments value) {
    setProperty(new TestElementProperty(METADATA, value));
  }

  public Arguments getMetadata() {
    return (Arguments) getProperty(METADATA).getObjectValue();
  }

  /**
   * @param value Boolean that indicates body will be sent as is
   */
  public void setData(String value) {
    setProperty(DATA, value);
  }

  /**
   * @return boolean that indicates body will be sent as is
   */
  public String getData() {
    return getPropertyAsString(DATA, "");
  }

  @Override
  public String toString() {
    StringBuilder stringBuffer = new StringBuilder();
    stringBuffer.append("requestType: [")
        .append(getRequestType())
        .append("]\n")
        .append("route: [")
        .append(getRoute())
        .append("]");

    return stringBuffer.toString();
  }

  /**
   * Do a sampling and return its results.
   *
   * @param e <code>Entry</code> to be sampled
   * @return results of the sampling
   */
  @Override
  public SampleResult sample(Entry e) {
    return sample();
  }

  /**
   * Perform a sample, and return the results
   *
   * @return results of the sampling
   */
  public SampleResult sample() {
    SampleResult res;
    res = sample(getRoute(), getRequestType());
    if (res != null) {
      res.setSampleLabel(getName());
    }
    return res;
  }

  /**
   * Samples the URL passed in and stores the result in
   * <code>HTTPSampleResult</code>, following redirects and downloading
   * page resources as appropriate.
   * <p>
   * When getting a redirect target, redirects are not followed and resources are not downloaded.
   * The caller will take care of this.
   *
   * @param u                    URL to sample
   * @param method               HTTP method: GET, POST,...
   * @param areFollowingRedirect whether we're getting a redirect target
   * @param depth                Depth of this target in the frame structure. Used only to prevent
   *                             infinite recursion.
   * @return results of the sampling, can be null if u is in CacheManager
   */
  protected abstract ReactiveSampleResult sample(String route, String requestType);

  static void registerParser(String contentType, String className) {
    log.info("Parser for {} is {}", contentType, className);
    PARSERS_FOR_CONTENT_TYPE.put(contentType, className);
  }

  /**
   * Gets parser from {@link HTTPSampleResult#getMediaType()}. Returns null if no parser defined for
   * it
   *
   * @param res {@link HTTPSampleResult}
   * @return {@link LinkExtractorParser}
   * @throws LinkExtractorParseException
   */
  private LinkExtractorParser getParser()
      throws LinkExtractorParseException {
    String parserClassName =
        PARSERS_FOR_CONTENT_TYPE.get(this.getContentEncoding());
    if (parserClassName != null && !parserClassName.isEmpty()) {
      return BaseParser.getParser(parserClassName);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void testEnded() {
    if (isConcurrentDwn()) {
//            ResourcesDownloader.getInstance().shrink();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void testEnded(String host) {
    testEnded();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void testStarted() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void testStarted(String host) {
    testStarted();
  }

  /*
   * Method to set files list to be uploaded.
   *
   * @param value
   *   HTTPFileArgs object that stores file list to be uploaded.
   */
  private void setHTTPFileArgs(HTTPFileArgs value) {
    if (value.getHTTPFileArgCount() > 0) {
      setProperty(new TestElementProperty(FILE_ARGS, value));
    } else {
      removeProperty(FILE_ARGS); // no point saving an empty list
    }
  }

  /*
   * Method to get files list to be uploaded.
   */
  private HTTPFileArgs getHTTPFileArgs() {
    return (HTTPFileArgs) getProperty(FILE_ARGS).getObjectValue();
  }

  /**
   * Get the collection of files as a list. The list is built up from the
   * filename/filefield/mimetype properties, plus any additional entries saved in the FILE_ARGS
   * property.
   * <p>
   * If there are no valid file entries, then an empty list is returned.
   *
   * @return an array of file arguments (never null)
   */
  public HTTPFileArg[] getHTTPFiles() {
    final HTTPFileArgs fileArgs = getHTTPFileArgs();
    return fileArgs == null ? new HTTPFileArg[]{} : fileArgs.asArray();
  }

  public int getHTTPFileCount() {
    return getHTTPFiles().length;
  }

  /**
   * Saves the list of files. The first file is saved in the Filename/field/mimetype properties. Any
   * additional files are saved in the FILE_ARGS array.
   *
   * @param files list of files to save
   */
  public void setHTTPFiles(HTTPFileArg[] files) {
    HTTPFileArgs fileArgs = new HTTPFileArgs();
    // Weed out the empty files
    if (files.length > 0) {
      for (HTTPFileArg file : files) {
        if (file.isNotEmpty()) {
          fileArgs.addHTTPFileArg(file);
        }
      }
    }
    setHTTPFileArgs(fileArgs);
  }

  public static String[] getValidReqyestTypesAsArray() {
    return REQUESTTYPESLIST.toArray(new String[REQUESTTYPESLIST.size()]);
  }

  // Implement these here, to avoid re-implementing for sub-classes
  // (previously these were implemented in all TestElements)
  @Override
  public void threadStarted() {
  }

  @Override
  public void threadFinished() {
  }

  @Override
  public void testIterationStart(LoopIterationEvent event) {
    // NOOP to provide based empty impl and avoid breaking existing implementations
  }

  /**
   * JMeter 2.3.1 and earlier only had fields for one file on the GUI:
   * <ul>
   *   <li>FILE_NAME</li>
   *   <li>FILE_FIELD</li>
   *   <li>MIMETYPE</li>
   * </ul>
   * These were stored in their own individual properties.
   * <p>
   * Version 2.3.3 introduced a list of files, each with their own path, name and mimetype.
   * <p>
   * In order to maintain backwards compatibility of test plans, the 3 original properties
   * were retained; additional file entries are stored in an HTTPFileArgs class.
   * The HTTPFileArgs class was only present if there is more than 1 file; this means that
   * such test plans are backward compatible.
   * <p>
   * Versions after 2.3.4 dispense with the original set of 3 properties.
   * Test plans that use them are converted to use a single HTTPFileArgs list.
   *
   * @see HTTPSamplerBaseConverter
   */
  void mergeFileProperties() {
    JMeterProperty fileName = getProperty(FILE_NAME);
    JMeterProperty paramName = getProperty(FILE_FIELD);
    JMeterProperty mimeType = getProperty(MIMETYPE);
    HTTPFileArg oldStyleFile = new HTTPFileArg(fileName, paramName, mimeType);

    HTTPFileArgs fileArgs = getHTTPFileArgs();

    HTTPFileArgs allFileArgs = new HTTPFileArgs();
    if (oldStyleFile.isNotEmpty()) { // OK, we have an old-style file definition
      allFileArgs.addHTTPFileArg(oldStyleFile); // save it
      // Now deal with any additional file arguments
      if (fileArgs != null) {
        HTTPFileArg[] infiles = fileArgs.asArray();
        for (HTTPFileArg infile : infiles) {
          allFileArgs.addHTTPFileArg(infile);
        }
      }
    } else {
      if (fileArgs != null) { // for new test plans that don't have FILE/PARAM/MIME properties
        allFileArgs = fileArgs;
      }
    }
    // Updated the property lists
    setHTTPFileArgs(allFileArgs);
    removeProperty(FILE_FIELD);
    removeProperty(FILE_NAME);
    removeProperty(MIMETYPE);
  }

  /**
   * Return if used a concurrent thread pool to get embedded resources.
   *
   * @return true if used
   */
  public boolean isConcurrentDwn() {
    return getPropertyAsBoolean(CONCURRENT_DWN, false);
  }

  public void setConcurrentDwn(boolean concurrentDwn) {
    setProperty(CONCURRENT_DWN, concurrentDwn, false);
  }

  /**
   * Get the pool size for concurrent thread pool to get embedded resources.
   *
   * @return the pool size
   */
  public String getConcurrentPool() {
    return getPropertyAsString(CONCURRENT_POOL, CONCURRENT_POOL_DEFAULT);
  }

  public void setConcurrentPool(String poolSize) {
    setProperty(CONCURRENT_POOL, poolSize, CONCURRENT_POOL_DEFAULT);
  }

  /**
   * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
   */
  @Override
  public boolean applies(ConfigTestElement configElement) {
    String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
    return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
  }
}
