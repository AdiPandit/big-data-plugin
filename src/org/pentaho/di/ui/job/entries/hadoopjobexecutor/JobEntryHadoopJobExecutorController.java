/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.di.ui.job.entries.hadoopjobexecutor;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.entries.hadoopjobexecutor.JobEntryHadoopJobExecutor;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.AbstractModelList;

public class JobEntryHadoopJobExecutorController extends AbstractXulEventHandler {

  private static final Class<?> PKG = JobEntryHadoopJobExecutor.class;

  public static final String JOB_ENTRY_NAME = "jobEntryName"; //$NON-NLS-1$
  public static final String HADOOP_JOB_NAME = "hadoopJobName"; //$NON-NLS-1$
  public static final String JAR_URL = "jarUrl"; //$NON-NLS-1$
  public static final String IS_SIMPLE = "isSimple"; //$NON-NLS-1$
  public static final String USER_DEFINED = "userDefined"; //$NON-NLS-1$

  private String jobEntryName;
  private String hadoopJobName;
  private String jarUrl = "";

  private boolean isSimple = true;

  private SimpleConfiguration sConf = new SimpleConfiguration();
  private AdvancedConfiguration aConf = new AdvancedConfiguration();

  private JobEntryHadoopJobExecutor jobEntry;

  private AbstractModelList<UserDefinedItem> userDefined = new AbstractModelList<UserDefinedItem>();

  public void accept() {
    // common/simple
    jobEntry.setName(jobEntryName);
    jobEntry.setHadoopJobName(hadoopJobName);
    jobEntry.setSimple(isSimple);
    jobEntry.setJarUrl(jarUrl);
    jobEntry.setCmdLineArgs(sConf.getCommandLineArgs());
    jobEntry.setSimpleBlocking(sConf.isSimpleBlocking());
    jobEntry.setSimpleLoggingInterval(sConf.getSimpleLoggingInterval());
    // advanced config
    jobEntry.setBlocking(aConf.isBlocking());
    jobEntry.setLoggingInterval(aConf.getLoggingInterval());
    jobEntry.setMapperClass(aConf.getMapperClass());
    jobEntry.setCombinerClass(aConf.getCombinerClass());
    jobEntry.setReducerClass(aConf.getReducerClass());
    jobEntry.setInputPath(aConf.getInputPath());
    jobEntry.setInputFormatClass(aConf.getInputFormatClass());
    jobEntry.setOutputPath(aConf.getOutputPath());
    jobEntry.setOutputKeyClass(aConf.getOutputKeyClass());
    jobEntry.setOutputValueClass(aConf.getOutputValueClass());
    jobEntry.setOutputFormatClass(aConf.getOutputFormatClass());
    jobEntry.setHdfsHostname(aConf.getHdfsHostname());
    jobEntry.setHdfsPort(aConf.getHdfsPort());
    jobEntry.setJobTrackerHostname(aConf.getJobTrackerHostname());
    jobEntry.setJobTrackerPort(aConf.getJobTrackerPort());
    jobEntry.setNumMapTasks(aConf.getNumMapTasks());
    jobEntry.setNumReduceTasks(aConf.getNumReduceTasks());
    jobEntry.setUserDefined(userDefined);
    jobEntry.setWorkingDirectory(aConf.getWorkingDirectory());

    jobEntry.setChanged();

    cancel();
  }

  public void init() {
    if (jobEntry != null) {
      // common/simple
      setName(jobEntry.getName());
      setJobEntryName(jobEntry.getName());
      setHadoopJobName(jobEntry.getHadoopJobName());
      setSimple(jobEntry.isSimple());
      setJarUrl(jobEntry.getJarUrl());
      sConf.setCommandLineArgs(jobEntry.getCmdLineArgs());
      sConf.setSimpleBlocking(jobEntry.isSimpleBlocking());
      sConf.setSimpleLoggingInterval(jobEntry.getSimpleLoggingInterval());
      // advanced config
      userDefined.clear();
      if (jobEntry.getUserDefined() != null) {
        userDefined.addAll(jobEntry.getUserDefined());
      }
      aConf.setBlocking(jobEntry.isBlocking());
      aConf.setLoggingInterval(jobEntry.getLoggingInterval());
      aConf.setMapperClass(jobEntry.getMapperClass());
      aConf.setCombinerClass(jobEntry.getCombinerClass());
      aConf.setReducerClass(jobEntry.getReducerClass());
      aConf.setInputPath(jobEntry.getInputPath());
      aConf.setInputFormatClass(jobEntry.getInputFormatClass());
      aConf.setOutputPath(jobEntry.getOutputPath());
      aConf.setOutputKeyClass(jobEntry.getOutputKeyClass());
      aConf.setOutputValueClass(jobEntry.getOutputValueClass());
      aConf.setOutputFormatClass(jobEntry.getOutputFormatClass());
      aConf.setHdfsHostname(jobEntry.getHdfsHostname());
      aConf.setHdfsPort(jobEntry.getHdfsPort());
      aConf.setJobTrackerHostname(jobEntry.getJobTrackerHostname());
      aConf.setJobTrackerPort(jobEntry.getJobTrackerPort());
      aConf.setNumMapTasks(jobEntry.getNumMapTasks());
      aConf.setNumReduceTasks(jobEntry.getNumReduceTasks());
      aConf.setWorkingDirectory(jobEntry.getWorkingDirectory());
    }
  }

  public void cancel() {
    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getRootElement();
    Shell shell = (Shell) xulDialog.getRootObject();
    if (!shell.isDisposed()) {
      WindowProperty winprop = new WindowProperty(shell);
      PropsUI.getInstance().setScreen(winprop);
      ((Composite) xulDialog.getManagedObject()).dispose();
      shell.dispose();
    }
  }

  public void browseJar() {
    XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getRootElement();
    Shell shell = (Shell) xulDialog.getRootObject();
    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
    dialog.setFilterExtensions(new String[] { "*.jar;*.zip" });
    dialog.setFilterNames(new String[] { "Java Archives (jar)" });
    String prevName = jobEntry.environmentSubstitute(jarUrl);
    String parentFolder = null;
    try {
      parentFolder = KettleVFS.getFilename(KettleVFS.getFileObject(jobEntry.environmentSubstitute(jobEntry.getFilename())).getParent());
    } catch (Exception e) {
      // not that important
    }
    if (!Const.isEmpty(prevName)) {
      try {
        if (KettleVFS.fileExists(prevName)) {
          dialog.setFilterPath(KettleVFS.getFilename(KettleVFS.getFileObject(prevName).getParent()));
        } else {

          if (!prevName.endsWith(".jar") && !prevName.endsWith(".zip")) {
            prevName = "${" + Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY + "}/" + Const.trim(jarUrl) + ".jar";
          }
          if (KettleVFS.fileExists(prevName)) {
            setJarUrl(prevName);
            return;
          }
        }
      } catch (Exception e) {
        dialog.setFilterPath(parentFolder);
      }
    } else if (!Const.isEmpty(parentFolder)) {
      dialog.setFilterPath(parentFolder);
    }

    String fname = dialog.open();
    if (fname != null) {
      File file = new File(fname);
      String name = file.getName();
      String parentFolderSelection = file.getParentFile().toString();

      if (!Const.isEmpty(parentFolder) && parentFolder.equals(parentFolderSelection)) {
        setJarUrl("${" + Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY + "}/" + name);
      } else {
        setJarUrl(fname);
      }
    }
  }

  public void newUserDefinedItem() {
    userDefined.add(new UserDefinedItem());
  }

  public SimpleConfiguration getSimpleConfiguration() {
    return sConf;
  }

  public AdvancedConfiguration getAdvancedConfiguration() {
    return aConf;
  }

  public AbstractModelList<UserDefinedItem> getUserDefined() {
    return userDefined;
  }

  @Override
  public String getName() {
    return "jobEntryController"; //$NON-NLS-1$
  }

  public String getJobEntryName() {
    return jobEntryName;
  }

  public void setJobEntryName(String jobEntryName) {
    String previousVal = this.jobEntryName;
    String newVal = jobEntryName;

    this.jobEntryName = jobEntryName;
    firePropertyChange(JobEntryHadoopJobExecutorController.JOB_ENTRY_NAME, previousVal, newVal);
  }

  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    String previousVal = this.hadoopJobName;
    String newVal = hadoopJobName;

    this.hadoopJobName = hadoopJobName;
    firePropertyChange(JobEntryHadoopJobExecutorController.HADOOP_JOB_NAME, previousVal, newVal);
  }

  public String getJarUrl() {
    return jarUrl;
  }

  public void setJarUrl(String jarUrl) {
    String previousVal = this.jarUrl;
    String newVal = jarUrl;

    this.jarUrl = jarUrl;
    firePropertyChange(JobEntryHadoopJobExecutorController.JAR_URL, previousVal, newVal);
  }

  public boolean isSimple() {
    return isSimple;
  }

  public void setSimple(boolean isSimple) {
    ((XulVbox) getXulDomContainer().getDocumentRoot().getElementById("advanced-configuration")).setVisible(!isSimple); //$NON-NLS-1$
    ((XulVbox) getXulDomContainer().getDocumentRoot().getElementById("simple-configuration")).setVisible(isSimple); //$NON-NLS-1$

    boolean previousVal = this.isSimple;
    boolean newVal = isSimple;

    this.isSimple = isSimple;
    firePropertyChange(JobEntryHadoopJobExecutorController.IS_SIMPLE, previousVal, newVal);
  }

  public void invertSimpleBlocking() {
    sConf.setSimpleBlocking(!sConf.isSimpleBlocking());
  }

  public void invertBlocking() {
    aConf.setBlocking(!aConf.isBlocking());
  }

  public JobEntryHadoopJobExecutor getJobEntry() {
    return jobEntry;
  }

  public void setJobEntry(JobEntryHadoopJobExecutor jobEntry) {
    this.jobEntry = jobEntry;
  }

  public class SimpleConfiguration extends XulEventSourceAdapter {
    public static final String CMD_LINE_ARGS = "commandLineArgs"; //$NON-NLS-1$
    public static final String BLOCKING = "simpleBlocking"; //$NON-NLS-1$
    public static final String LOGGING_INTERVAL = "simpleLoggingInterval"; //$NON-NLS-1$

    private String cmdLineArgs;
    private boolean simpleBlocking;
    private int simpleLoggingInterval = 60;

    public String getCommandLineArgs() {
      return cmdLineArgs;
    }

    public void setCommandLineArgs(String cmdLineArgs) {
      String previousVal = this.cmdLineArgs;
      String newVal = cmdLineArgs;

      this.cmdLineArgs = cmdLineArgs;

      firePropertyChange(SimpleConfiguration.CMD_LINE_ARGS, previousVal, newVal);
    }

    public boolean isSimpleBlocking() {
      return simpleBlocking;
    }

    public void setSimpleBlocking(boolean simpleBlocking) {
      boolean old = this.simpleBlocking;
      this.simpleBlocking = simpleBlocking;
      firePropertyChange(SimpleConfiguration.BLOCKING, old, this.simpleBlocking);
    }

    public int getSimpleLoggingInterval() {
      return simpleLoggingInterval;
    }

    public void setSimpleLoggingInterval(int simpleLoggingInterval) {
      int old = this.simpleLoggingInterval;
      this.simpleLoggingInterval = simpleLoggingInterval;
      firePropertyChange(SimpleConfiguration.LOGGING_INTERVAL, old, this.simpleLoggingInterval);
    }
  }

  public class AdvancedConfiguration extends XulEventSourceAdapter {
    public static final String OUTPUT_KEY_CLASS = "outputKeyClass"; //$NON-NLS-1$
    public static final String OUTPUT_VALUE_CLASS = "outputValueClass"; //$NON-NLS-1$
    public static final String MAPPER_CLASS = "mapperClass"; //$NON-NLS-1$
    public static final String COMBINER_CLASS = "combinerClass"; //$NON-NLS-1$
    public static final String REDUCER_CLASS = "reducerClass"; //$NON-NLS-1$
    public static final String INPUT_FORMAT_CLASS = "inputFormatClass"; //$NON-NLS-1$
    public static final String OUTPUT_FORMAT_CLASS = "outputFormatClass"; //$NON-NLS-1$
    public static final String WORKING_DIRECTORY = "workingDirectory"; //$NON-NLS-1$
    public static final String INPUT_PATH = "inputPath"; //$NON-NLS-1$
    public static final String OUTPUT_PATH = "outputPath"; //$NON-NLS-1$
    public static final String BLOCKING = "blocking"; //$NON-NLS-1$
    public static final String LOGGING_INTERVAL = "loggingInterval"; //$NON-NLS-1$
    public static final String HDFS_HOSTNAME = "hdfsHostname"; //$NON-NLS-1$
    public static final String HDFS_PORT = "hdfsPort"; //$NON-NLS-1$
    public static final String JOB_TRACKER_HOSTNAME = "jobTrackerHostname"; //$NON-NLS-1$
    public static final String JOB_TRACKER_PORT = "jobTrackerPort"; //$NON-NLS-1$
    public static final String NUM_MAP_TASKS = "numMapTasks"; //$NON-NLS-1$
    public static final String NUM_REDUCE_TASKS = "numReduceTasks"; //$NON-NLS-1$

    private String outputKeyClass;
    private String outputValueClass;
    private String mapperClass;
    private String combinerClass;
    private String reducerClass;
    private String inputFormatClass;
    private String outputFormatClass;

    private String workingDirectory;
    private String hdfsHostname;
    private String hdfsPort;
    private String jobTrackerHostname;
    private String jobTrackerPort;
    private String inputPath;
    private String outputPath;

    private int numMapTasks = 1;
    private int numReduceTasks = 1;

    private boolean blocking;
    private int loggingInterval = 60; // 60 seconds

    public String getOutputKeyClass() {
      return outputKeyClass;
    }

    public void setOutputKeyClass(String outputKeyClass) {
      String previousVal = this.outputKeyClass;
      String newVal = outputKeyClass;

      this.outputKeyClass = outputKeyClass;
      firePropertyChange(AdvancedConfiguration.OUTPUT_KEY_CLASS, previousVal, newVal);
    }

    public String getOutputValueClass() {
      return outputValueClass;
    }

    public void setOutputValueClass(String outputValueClass) {
      String previousVal = this.outputValueClass;
      String newVal = outputValueClass;

      this.outputValueClass = outputValueClass;
      firePropertyChange(AdvancedConfiguration.OUTPUT_VALUE_CLASS, previousVal, newVal);
    }

    public String getMapperClass() {
      return mapperClass;
    }

    public void setMapperClass(String mapperClass) {
      String previousVal = this.mapperClass;
      String newVal = mapperClass;

      this.mapperClass = mapperClass;
      firePropertyChange(AdvancedConfiguration.MAPPER_CLASS, previousVal, newVal);
    }

    public String getCombinerClass() {
      return combinerClass;
    }

    public void setCombinerClass(String combinerClass) {
      String previousVal = this.combinerClass;
      String newVal = combinerClass;

      this.combinerClass = combinerClass;
      firePropertyChange(AdvancedConfiguration.COMBINER_CLASS, previousVal, newVal);
    }

    public String getReducerClass() {
      return reducerClass;
    }

    public void setReducerClass(String reducerClass) {
      String previousVal = this.reducerClass;
      String newVal = reducerClass;

      this.reducerClass = reducerClass;
      firePropertyChange(AdvancedConfiguration.REDUCER_CLASS, previousVal, newVal);
    }

    public String getInputFormatClass() {
      return inputFormatClass;
    }

    public void setInputFormatClass(String inputFormatClass) {
      String previousVal = this.inputFormatClass;
      String newVal = inputFormatClass;

      this.inputFormatClass = inputFormatClass;
      firePropertyChange(AdvancedConfiguration.INPUT_FORMAT_CLASS, previousVal, newVal);
    }

    public String getOutputFormatClass() {
      return outputFormatClass;
    }

    public void setOutputFormatClass(String outputFormatClass) {
      String previousVal = this.outputFormatClass;
      String newVal = outputFormatClass;

      this.outputFormatClass = outputFormatClass;
      firePropertyChange(AdvancedConfiguration.OUTPUT_FORMAT_CLASS, previousVal, newVal);
    }

    public String getWorkingDirectory() {
      return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
      String previousVal = this.workingDirectory;
      String newVal = workingDirectory;

      this.workingDirectory = workingDirectory;
      firePropertyChange(AdvancedConfiguration.WORKING_DIRECTORY, previousVal, newVal);
    }

    public String getHdfsHostname() {
      return hdfsHostname;
    }

    public void setHdfsHostname(String hdfsHostname) {
      String previousVal = this.hdfsHostname;
      String newVal = hdfsHostname;

      this.hdfsHostname = hdfsHostname;
      firePropertyChange(AdvancedConfiguration.HDFS_HOSTNAME, previousVal, newVal);
    }

    public String getHdfsPort() {
      return hdfsPort;
    }

    public void setHdfsPort(String hdfsPort) {
      String previousVal = this.hdfsPort;
      String newVal = hdfsPort;

      this.hdfsPort = hdfsPort;
      firePropertyChange(AdvancedConfiguration.HDFS_PORT, previousVal, newVal);
    }

    public String getJobTrackerHostname() {
      return jobTrackerHostname;
    }

    public void setJobTrackerHostname(String jobTrackerHostname) {
      String previousVal = this.jobTrackerHostname;
      String newVal = jobTrackerHostname;

      this.jobTrackerHostname = jobTrackerHostname;
      firePropertyChange(AdvancedConfiguration.JOB_TRACKER_HOSTNAME, previousVal, newVal);
    }

    public String getJobTrackerPort() {
      return jobTrackerPort;
    }

    public void setJobTrackerPort(String jobTrackerPort) {
      String previousVal = this.jobTrackerPort;
      String newVal = jobTrackerPort;

      this.jobTrackerPort = jobTrackerPort;
      firePropertyChange(AdvancedConfiguration.JOB_TRACKER_PORT, previousVal, newVal);
    }

    public String getInputPath() {
      return inputPath;
    }

    public void setInputPath(String inputPath) {
      String previousVal = this.inputPath;
      String newVal = inputPath;

      this.inputPath = inputPath;
      firePropertyChange(AdvancedConfiguration.INPUT_PATH, previousVal, newVal);
    }

    public String getOutputPath() {
      return outputPath;
    }

    public void setOutputPath(String outputPath) {
      String previousVal = this.outputPath;
      String newVal = outputPath;

      this.outputPath = outputPath;
      firePropertyChange(AdvancedConfiguration.OUTPUT_PATH, previousVal, newVal);
    }

    public boolean isBlocking() {
      return blocking;
    }

    public void setBlocking(boolean blocking) {
      boolean previousVal = this.blocking;
      boolean newVal = blocking;

      this.blocking = blocking;
      firePropertyChange(AdvancedConfiguration.BLOCKING, previousVal, newVal);
    }

    public int getLoggingInterval() {
      return loggingInterval;
    }

    public void setLoggingInterval(int loggingInterval) {
      int previousVal = this.loggingInterval;
      int newVal = loggingInterval;

      this.loggingInterval = loggingInterval;
      firePropertyChange(AdvancedConfiguration.LOGGING_INTERVAL, previousVal, newVal);
    }

    public int getNumMapTasks() {
      return numMapTasks;
    }

    public void setNumMapTasks(int numMapTasks) {
      int previousVal = this.numMapTasks;
      int newVal = numMapTasks;

      this.numMapTasks = numMapTasks;
      firePropertyChange(AdvancedConfiguration.NUM_MAP_TASKS, previousVal, newVal);
    }

    public int getNumReduceTasks() {
      return numReduceTasks;
    }

    public void setNumReduceTasks(int numReduceTasks) {
      int previousVal = this.numReduceTasks;
      int newVal = numReduceTasks;

      this.numReduceTasks = numReduceTasks;
      firePropertyChange(AdvancedConfiguration.NUM_REDUCE_TASKS, previousVal, newVal);
    }

  }
}