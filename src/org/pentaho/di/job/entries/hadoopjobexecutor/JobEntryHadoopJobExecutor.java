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

package org.pentaho.di.job.entries.hadoopjobexecutor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.job.entries.hadoopjobexecutor.UserDefinedItem;
import org.w3c.dom.Node;

@JobEntry(id = "HadoopJobExecutorPlugin", name = "Hadoop Job Executor", categoryDescription = "Hadoop", description = "Execute Map/Reduce jobs in Hadoop", image = "HDE.png")
public class JobEntryHadoopJobExecutor extends JobEntryBase implements Cloneable, JobEntryInterface {

  private static Class<?> PKG = JobEntryHadoopJobExecutor.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private String hadoopJobName;

  private String jarUrl = "";

  private boolean isSimple = true;

  private String cmdLineArgs;
  private boolean simpleBlocking;
  private int simpleLoggingInterval = 60; // 60 seconds default

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

  private boolean blocking;
  private int loggingInterval = 60; // 60 seconds default

  private int numMapTasks = 1;
  private int numReduceTasks = 1;

  private List<UserDefinedItem> userDefined = new ArrayList<UserDefinedItem>();

  public String getHadoopJobName() {
    return hadoopJobName;
  }

  public void setHadoopJobName(String hadoopJobName) {
    this.hadoopJobName = hadoopJobName;
  }

  public String getJarUrl() {
    return jarUrl;
  }

  public void setJarUrl(String jarUrl) {
    this.jarUrl = jarUrl;
  }

  public boolean isSimple() {
    return isSimple;
  }

  public void setSimple(boolean isSimple) {
    this.isSimple = isSimple;
  }

  public String getCmdLineArgs() {
    return cmdLineArgs;
  }

  public void setCmdLineArgs(String cmdLineArgs) {
    this.cmdLineArgs = cmdLineArgs;
  }

  public String getOutputKeyClass() {
    return outputKeyClass;
  }

  public void setOutputKeyClass(String outputKeyClass) {
    this.outputKeyClass = outputKeyClass;
  }

  public String getOutputValueClass() {
    return outputValueClass;
  }

  public void setOutputValueClass(String outputValueClass) {
    this.outputValueClass = outputValueClass;
  }

  public String getMapperClass() {
    return mapperClass;
  }

  public void setMapperClass(String mapperClass) {
    this.mapperClass = mapperClass;
  }

  public String getCombinerClass() {
    return combinerClass;
  }

  public void setCombinerClass(String combinerClass) {
    this.combinerClass = combinerClass;
  }

  public String getReducerClass() {
    return reducerClass;
  }

  public void setReducerClass(String reducerClass) {
    this.reducerClass = reducerClass;
  }

  public String getInputFormatClass() {
    return inputFormatClass;
  }

  public void setInputFormatClass(String inputFormatClass) {
    this.inputFormatClass = inputFormatClass;
  }

  public String getOutputFormatClass() {
    return outputFormatClass;
  }

  public void setOutputFormatClass(String outputFormatClass) {
    this.outputFormatClass = outputFormatClass;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public String getHdfsHostname() {
    return hdfsHostname;
  }

  public void setHdfsHostname(String hdfsHostname) {
    this.hdfsHostname = hdfsHostname;
  }

  public String getHdfsPort() {
    return hdfsPort;
  }

  public void setHdfsPort(String hdfsPort) {
    this.hdfsPort = hdfsPort;
  }

  public String getJobTrackerHostname() {
    return jobTrackerHostname;
  }

  public void setJobTrackerHostname(String jobTrackerHostname) {
    this.jobTrackerHostname = jobTrackerHostname;
  }

  public String getJobTrackerPort() {
    return jobTrackerPort;
  }

  public void setJobTrackerPort(String jobTrackerPort) {
    this.jobTrackerPort = jobTrackerPort;
  }

  public String getInputPath() {
    return inputPath;
  }

  public void setInputPath(String inputPath) {
    this.inputPath = inputPath;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  public boolean isBlocking() {
    return blocking;
  }

  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
  }

  public int getLoggingInterval() {
    return loggingInterval;
  }

  public void setLoggingInterval(int loggingInterval) {
    this.loggingInterval = loggingInterval;
  }

  public List<UserDefinedItem> getUserDefined() {
    return userDefined;
  }

  public void setUserDefined(List<UserDefinedItem> userDefined) {
    this.userDefined = userDefined;
  }

  public int getNumMapTasks() {
    return numMapTasks;
  }

  public void setNumMapTasks(int numMapTasks) {
    this.numMapTasks = numMapTasks;
  }

  public int getNumReduceTasks() {
    return numReduceTasks;
  }

  public void setNumReduceTasks(int numReduceTasks) {
    this.numReduceTasks = numReduceTasks;
  }

  private void restoreSecurityManager(AtomicInteger counter, SecurityManager sm) {
    if (counter.decrementAndGet() == 0) {
      System.setSecurityManager(sm);
    }
  }
  
  public Result execute(final Result result, int arg1) throws KettleException {
    Log4jFileAppender appender = null;
    String logFileName = "pdi-" + this.getName(); //$NON-NLS-1$
    try
    {
      appender = LogWriter.createFileAppender(logFileName, true, false);
      LogWriter.getInstance().addAppender(appender);
      log.setLogLevel(parentJob.getLogLevel());
    } catch (Exception e)
    {
      logError(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.FailedToOpenLogFile", logFileName, e.toString())); //$NON-NLS-1$
      logError(Const.getStackTracker(e));
    }
    
    try {
      URL resolvedJarUrl = null;
      if (jarUrl.indexOf("://") == -1) {
        // default to file://
        File jarFile = new File(jarUrl);
        resolvedJarUrl = jarFile.toURI().toURL();
      } else {
        resolvedJarUrl = new URL(jarUrl);
      }

      if (log.isDetailed())
        logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.ResolvedJar", resolvedJarUrl.toExternalForm()));

      if (isSimple) {
        final Class mainClass = JarUtility.getMainClassFromManifest(resolvedJarUrl, getClass().getClassLoader());
        final SecurityManager sm = System.getSecurityManager();
        final NoExitSecurityManager nesm = new NoExitSecurityManager(sm);
        System.setSecurityManager(nesm);
        if (log.isDetailed())
          logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.SimpleMode"));

        List<Class<?>> classesWithMains = new ArrayList<Class<?>>();
        if (mainClass == null) {
          classesWithMains.addAll(JarUtility.getClassesInJarWithMain(resolvedJarUrl.toExternalForm(), getClass().getClassLoader()));
        } else {
          classesWithMains.add(mainClass);
        }
        if (!classesWithMains.isEmpty()) {
          final AtomicInteger threads = new AtomicInteger(classesWithMains.size());
          for (final Class<?> clazz : classesWithMains) {
            Runnable r = new Runnable() {
              public void run() {
                try {
                  try {
                    executeMainMethod(clazz);
                  } finally {
                    restoreSecurityManager(threads, sm);
                  }
                } catch (NoExitSecurityManager.NoJvmExitSecurityException ex) {
                  // Only log if we're blocking and waiting for this to complete
                  if (simpleBlocking) {
                    logExitStatus(result, mainClass, ex);
                  }
                } catch (InvocationTargetException ex) {
                  if (ex.getTargetException() instanceof NoExitSecurityManager.NoJvmExitSecurityException) {
                    // Only log if we're blocking and waiting for this to complete
                    if (simpleBlocking) {
                      logExitStatus(result, mainClass, (NoExitSecurityManager.NoJvmExitSecurityException) ex.getTargetException());
                    }
                  } else {
                    throw new RuntimeException(ex);
                  }
                } catch (Exception ex) {
                  throw new RuntimeException(ex);
                }
              }
            };
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
              @Override
              public void uncaughtException(Thread t, Throwable e) {
                restoreSecurityManager(threads, sm);
                if (simpleBlocking) {
                  // Only log if we're blocking and waiting for this to complete
                  logError(BaseMessages.getString(JobEntryHadoopJobExecutor.class, "JobEntryHadoopJobExecutor.FailedToExecuteClass", clazz.getName()), e);
                  result.setNrErrors(result.getNrErrors() + 1);
                  result.setResult(false);
                }
              }
            });
            t.start();

            if (simpleBlocking) {
              // wait until the thread is done
              do {
                logDetailed(BaseMessages.getString(JobEntryHadoopJobExecutor.class, "JobEntryHadoopJobExecutor.Blocking", clazz.getName()));
                t.join(simpleLoggingInterval * 1000);
              } while(!parentJob.isStopped() && t.isAlive());
              if (t.isAlive()) {
                // Kill thread if it's still running. The job must have been stopped.
                t.interrupt();
              }
            }
          }
        }
      } else {
        if (log.isDetailed())
          logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.AdvancedMode"));

        URL[] urls = new URL[] { resolvedJarUrl };
        URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());

        JobConf conf = new JobConf();
        conf.setJobName(hadoopJobName);

        conf.setOutputKeyClass(loader.loadClass(outputKeyClass));
        conf.setOutputValueClass(loader.loadClass(outputValueClass));

        if(mapperClass != null) {
          Class<? extends Mapper> mapper = (Class<? extends Mapper>) loader.loadClass(mapperClass);
          conf.setMapperClass(mapper);
        }
        if(combinerClass != null) {
          Class<? extends Reducer> combiner = (Class<? extends Reducer>) loader.loadClass(combinerClass);
          conf.setCombinerClass(combiner);
        }
        if(reducerClass != null) {
          Class<? extends Reducer> reducer = (Class<? extends Reducer>) loader.loadClass(reducerClass);
          conf.setReducerClass(reducer);
        }

        if(inputFormatClass != null) {
          Class<? extends InputFormat> inputFormat = (Class<? extends InputFormat>) loader.loadClass(inputFormatClass);
          conf.setInputFormat(inputFormat);
        }
        if(outputFormatClass != null) {
          Class<? extends OutputFormat> outputFormat = (Class<? extends OutputFormat>) loader.loadClass(outputFormatClass);
          conf.setOutputFormat(outputFormat);
        }

        String hdfsBaseUrl = "hdfs://" + hdfsHostname + ":" + hdfsPort;
        conf.set("fs.default.name", hdfsBaseUrl);
        conf.set("mapred.job.tracker", jobTrackerHostname + ":" + jobTrackerPort);

        // TODO: this could be a list of input paths apparently
        FileInputFormat.setInputPaths(conf, new Path(hdfsBaseUrl + inputPath));
        FileOutputFormat.setOutputPath(conf, new Path(hdfsBaseUrl + outputPath));

        // process user defined values
        for (UserDefinedItem item : userDefined) {
          if (item.getName() != null && !"".equals(item.getName()) && item.getValue() != null && !"".equals(item.getValue())) {
            String nameS = environmentSubstitute(item.getName());
            String valueS = environmentSubstitute(item.getValue());
            conf.set(nameS, valueS);
          }
        }

        conf.setWorkingDirectory(new Path(hdfsBaseUrl + workingDirectory));
        conf.setJar(jarUrl);

        conf.setNumMapTasks(numMapTasks);
        conf.setNumReduceTasks(numReduceTasks);

        JobClient jobClient = new JobClient(conf);
        RunningJob runningJob = jobClient.submitJob(conf);
        
        if (blocking) {
          try {
            int taskCompletionEventIndex = 0;
            // Stop looping when the Kettle job has been stopped so we can stop the Hadoop job
            while (!parentJob.isStopped() && !runningJob.isComplete()) {
              if (loggingInterval >= 1) {
                printJobStatus(runningJob);
                
                TaskCompletionEvent[] tcEvents = runningJob.getTaskCompletionEvents(taskCompletionEventIndex);
                for(int i = 0; i < tcEvents.length; i++) {
                  String[] diags = runningJob.getTaskDiagnostics(tcEvents[i].getTaskAttemptId());
                  StringBuilder diagsOutput = new StringBuilder();
                  
                  if(diags != null && diags.length > 0) {
                    diagsOutput.append(Const.CR);
                    for(String s : diags) {
                      diagsOutput.append(s);
                      diagsOutput.append(Const.CR);
                    }
                  }
                  
                  switch(tcEvents[i].getTaskStatus()) {
                    case KILLED: {
                      logError(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.TaskDetails", TaskCompletionEvent.Status.KILLED, tcEvents[i].getTaskAttemptId().getTaskID().getId(), tcEvents[i].getTaskAttemptId().getId(), tcEvents[i].getEventId(), diagsOutput)); //$NON-NLS-1$
                    }break;
                    case FAILED: {
                      logError(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.TaskDetails", TaskCompletionEvent.Status.FAILED, tcEvents[i].getTaskAttemptId().getTaskID().getId(), tcEvents[i].getTaskAttemptId().getId(), tcEvents[i].getEventId(), diagsOutput)); //$NON-NLS-1$
                      result.setResult(false);
                    }break;
                    case SUCCEEDED: {
                      logDetailed(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.TaskDetails", TaskCompletionEvent.Status.SUCCEEDED, tcEvents[i].getTaskAttemptId().getTaskID().getId(), tcEvents[i].getTaskAttemptId().getId(), tcEvents[i].getEventId(), diagsOutput)); //$NON-NLS-1$
                    }break;
                  }
                }
                taskCompletionEventIndex += tcEvents.length;
                
                Thread.sleep(loggingInterval * 1000);
              } else {
                Thread.sleep(60000);
              }
            }

            // Stop the hadoop job if it is still running
            if (parentJob.isStopped() && !runningJob.isComplete()) {
              // We must stop the job running on Hadoop
              runningJob.killJob();
              // Indicate this job entry did not complete
              result.setResult(false);
            }
            
            printJobStatus(runningJob);
          } catch (InterruptedException ie) {
            logError(ie.getMessage(), ie);
          }
        }

      }
    } catch (Throwable t) {
      t.printStackTrace();
      result.setStopped(true);
      result.setNrErrors(1);
      result.setResult(false);
      logError(t.getMessage(), t);
    }
    
    if (appender != null)
    {
      LogWriter.getInstance().removeAppender(appender);
      appender.close();
      
      ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_LOG, appender.getFile(), parentJob.getJobname(), getName());
      result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
    }
    
    return result;
  }

  /**
   * Log the status of an attempt to exit the JVM while executing the provided class' main method.
   *
   * @param result Result to update with failure condition if exit status code was not 0
   * @param mainClass Main class we were executing
   * @param ex Exception caught while executing the class provided
   */
  private void logExitStatus(Result result, Class<?> mainClass, NoExitSecurityManager.NoJvmExitSecurityException ex) {
    // Only error if exit code is not 0
    if (ex.getStatus() != 0) {
      result.setStopped(true);
      result.setNrErrors(1);
      result.setResult(false);
      logError(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.FailedToExecuteClass", mainClass.getName()));
    }
  }

  /**
   * Execute the main method of the provided class with the current command line arguments.
   *
   * @param clazz Class with main method to execute
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  protected void executeMainMethod(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
      Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
      String commandLineArgs = environmentSubstitute(cmdLineArgs);
      Object[] args = (commandLineArgs != null) ? new Object[] { commandLineArgs.split(" ") } : new Object[0];
      mainMethod.invoke(null, args);
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  public void printJobStatus(RunningJob runningJob) throws IOException {
    if (log.isBasic()) {
      float setupPercent = runningJob.setupProgress() * 100f;
      float mapPercent = runningJob.mapProgress() * 100f;
      float reducePercent = runningJob.reduceProgress() * 100f;
      logBasic(BaseMessages.getString(PKG, "JobEntryHadoopJobExecutor.RunningPercent", setupPercent, mapPercent, reducePercent));
    }
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    super.loadXML(entrynode, databases, slaveServers);
    hadoopJobName = XMLHandler.getTagValue(entrynode, "hadoop_job_name");
    isSimple = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "simple"));
    jarUrl = XMLHandler.getTagValue(entrynode, "jar_url");
    cmdLineArgs = XMLHandler.getTagValue(entrynode, "command_line_args");
    simpleBlocking = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "simple_blocking"));
    blocking = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "blocking"));
    try {
      simpleLoggingInterval = Integer.parseInt(XMLHandler.getTagValue(entrynode, "simple_logging_interval"));
      loggingInterval = Integer.parseInt(XMLHandler.getTagValue(entrynode, "logging_interval"));
    } catch (NumberFormatException nfe) {
    }

    mapperClass = XMLHandler.getTagValue(entrynode, "mapper_class");
    combinerClass = XMLHandler.getTagValue(entrynode, "combiner_class");
    reducerClass = XMLHandler.getTagValue(entrynode, "reducer_class");
    inputPath = XMLHandler.getTagValue(entrynode, "input_path");
    inputFormatClass = XMLHandler.getTagValue(entrynode, "input_format_class");
    outputPath = XMLHandler.getTagValue(entrynode, "output_path");
    outputKeyClass = XMLHandler.getTagValue(entrynode, "output_key_class");
    outputValueClass = XMLHandler.getTagValue(entrynode, "output_value_class");
    outputFormatClass = XMLHandler.getTagValue(entrynode, "output_format_class");

    hdfsHostname = XMLHandler.getTagValue(entrynode, "hdfs_hostname");
    hdfsPort = XMLHandler.getTagValue(entrynode, "hdfs_port");
    jobTrackerHostname = XMLHandler.getTagValue(entrynode, "job_tracker_hostname");
    jobTrackerPort = XMLHandler.getTagValue(entrynode, "job_tracker_port");
    numMapTasks = Integer.parseInt(XMLHandler.getTagValue(entrynode, "num_map_tasks"));
    numReduceTasks = Integer.parseInt(XMLHandler.getTagValue(entrynode, "num_reduce_tasks"));
    workingDirectory = XMLHandler.getTagValue(entrynode, "working_dir");

    // How many user defined elements?
    userDefined = new ArrayList<UserDefinedItem>();
    Node userDefinedList = XMLHandler.getSubNode(entrynode, "user_defined_list");
    int nrUserDefined = XMLHandler.countNodes(userDefinedList, "user_defined");
    for (int i = 0; i < nrUserDefined; i++) {
      Node userDefinedNode = XMLHandler.getSubNodeByNr(userDefinedList, "user_defined", i);
      String name = XMLHandler.getTagValue(userDefinedNode, "name");
      String value = XMLHandler.getTagValue(userDefinedNode, "value");
      UserDefinedItem item = new UserDefinedItem();
      item.setName(name);
      item.setValue(value);
      userDefined.add(item);
    }
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(1024);
    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_name", hadoopJobName));

    retval.append("      ").append(XMLHandler.addTagValue("simple", isSimple));
    retval.append("      ").append(XMLHandler.addTagValue("jar_url", jarUrl));
    retval.append("      ").append(XMLHandler.addTagValue("command_line_args", cmdLineArgs));
    retval.append("      ").append(XMLHandler.addTagValue("simple_blocking", simpleBlocking));
    retval.append("      ").append(XMLHandler.addTagValue("blocking", blocking));
    retval.append("      ").append(XMLHandler.addTagValue("logging_interval", loggingInterval));
    retval.append("      ").append(XMLHandler.addTagValue("simple_logging_interval", simpleLoggingInterval));
    retval.append("      ").append(XMLHandler.addTagValue("hadoop_job_name", hadoopJobName));

    retval.append("      ").append(XMLHandler.addTagValue("mapper_class", mapperClass));
    retval.append("      ").append(XMLHandler.addTagValue("combiner_class", combinerClass));
    retval.append("      ").append(XMLHandler.addTagValue("reducer_class", reducerClass));
    retval.append("      ").append(XMLHandler.addTagValue("input_path", inputPath));
    retval.append("      ").append(XMLHandler.addTagValue("input_format_class", inputFormatClass));
    retval.append("      ").append(XMLHandler.addTagValue("output_path", outputPath));
    retval.append("      ").append(XMLHandler.addTagValue("output_key_class", outputKeyClass));
    retval.append("      ").append(XMLHandler.addTagValue("output_value_class", outputValueClass));
    retval.append("      ").append(XMLHandler.addTagValue("output_format_class", outputFormatClass));

    retval.append("      ").append(XMLHandler.addTagValue("hdfs_hostname", hdfsHostname));
    retval.append("      ").append(XMLHandler.addTagValue("hdfs_port", hdfsPort));
    retval.append("      ").append(XMLHandler.addTagValue("job_tracker_hostname", jobTrackerHostname));
    retval.append("      ").append(XMLHandler.addTagValue("job_tracker_port", jobTrackerPort));
    retval.append("      ").append(XMLHandler.addTagValue("num_map_tasks", numMapTasks));
    retval.append("      ").append(XMLHandler.addTagValue("num_reduce_tasks", numReduceTasks));
    retval.append("      ").append(XMLHandler.addTagValue("working_dir", workingDirectory));

    retval.append("      <user_defined_list>").append(Const.CR);
    if (userDefined != null) {
      for (UserDefinedItem item : userDefined) {
        if (item.getName() != null && !"".equals(item.getName()) && item.getValue() != null && !"".equals(item.getValue())) {
          retval.append("        <user_defined>").append(Const.CR);
          retval.append("          ").append(XMLHandler.addTagValue("name", item.getName()));
          retval.append("          ").append(XMLHandler.addTagValue("value", item.getValue()));
          retval.append("        </user_defined>").append(Const.CR);
        }
      }
    }
    retval.append("      </user_defined_list>").append(Const.CR);
    return retval.toString();
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    if(rep != null) {
      super.loadRep(rep, id_jobentry, databases, slaveServers);
      
      setHadoopJobName(rep.getJobEntryAttributeString(id_jobentry, "hadoop_job_name"));
      setSimple(rep.getJobEntryAttributeBoolean(id_jobentry, "simple"));

      setJarUrl(rep.getJobEntryAttributeString(id_jobentry, "jar_url"));
      setCmdLineArgs(rep.getJobEntryAttributeString(id_jobentry, "command_line_args"));
      setSimpleBlocking(rep.getJobEntryAttributeBoolean(id_jobentry, "simple_blocking"));
      setBlocking(rep.getJobEntryAttributeBoolean(id_jobentry, "blocking"));
      setSimpleLoggingInterval(new Long(rep.getJobEntryAttributeInteger(id_jobentry, "simple_logging_interval")).intValue());
      setLoggingInterval(new Long(rep.getJobEntryAttributeInteger(id_jobentry, "logging_interval")).intValue());

      setMapperClass(rep.getJobEntryAttributeString(id_jobentry, "mapper_class"));
      setCombinerClass(rep.getJobEntryAttributeString(id_jobentry, "combiner_class"));
      setReducerClass(rep.getJobEntryAttributeString(id_jobentry, "reducer_class"));
      setInputPath(rep.getJobEntryAttributeString(id_jobentry, "input_path"));
      setInputFormatClass(rep.getJobEntryAttributeString(id_jobentry, "input_format_class"));
      setOutputPath(rep.getJobEntryAttributeString(id_jobentry, "output_path"));
      setOutputKeyClass(rep.getJobEntryAttributeString(id_jobentry, "output_key_class"));
      setOutputValueClass(rep.getJobEntryAttributeString(id_jobentry, "output_value_class"));
      setOutputFormatClass(rep.getJobEntryAttributeString(id_jobentry, "output_format_class"));

      setHdfsHostname(rep.getJobEntryAttributeString(id_jobentry, "hdfs_hostname"));
      setHdfsPort(rep.getJobEntryAttributeString(id_jobentry, "hdfs_port"));
      setJobTrackerHostname(rep.getJobEntryAttributeString(id_jobentry, "job_tracker_hostname"));
      setJobTrackerPort(rep.getJobEntryAttributeString(id_jobentry, "job_tracker_port"));
      setNumMapTasks(new Long(rep.getJobEntryAttributeInteger(id_jobentry, "num_map_tasks")).intValue());
      setNumReduceTasks(new Long(rep.getJobEntryAttributeInteger(id_jobentry, "num_reduce_tasks")).intValue());
      setWorkingDirectory(rep.getJobEntryAttributeString(id_jobentry, "working_dir"));

      
      int argnr = rep.countNrJobEntryAttributes(id_jobentry, "user_defined_name");//$NON-NLS-1$
      if(argnr > 0) {
        userDefined = new ArrayList<UserDefinedItem>();
        
        UserDefinedItem item = null;
        for(int i = 0; i < argnr; i++) {
          item = new UserDefinedItem();
          item.setName(rep.getJobEntryAttributeString(id_jobentry, i,"user_defined_name")); //$NON-NLS-1$
          item.setValue(rep.getJobEntryAttributeString(id_jobentry, i,"user_defined_value")); //$NON-NLS-1$
          userDefined.add(item);
        }
      }
    } else {
      throw new KettleException("Unable to save to a repository. The repository is null."); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    if(rep != null) {
      super.saveRep(rep, id_job);
      
      rep.saveJobEntryAttribute(id_job, getObjectId(),"hadoop_job_name", hadoopJobName); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"simple", isSimple); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(),"jar_url", jarUrl); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"command_line_args", cmdLineArgs); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"simple_blocking", simpleBlocking); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"blocking", blocking); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"simple_logging_interval", simpleLoggingInterval); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"logging_interval", loggingInterval); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"hadoop_job_name", hadoopJobName); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(),"mapper_class", mapperClass); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"combiner_class", combinerClass); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"reducer_class", reducerClass); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"input_path", inputPath); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"input_format_class", inputFormatClass); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"output_path", outputPath); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"output_key_class", outputKeyClass); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"output_value_class", outputValueClass); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"output_format_class", outputFormatClass); //$NON-NLS-1$

      rep.saveJobEntryAttribute(id_job, getObjectId(),"hdfs_hostname", hdfsHostname); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"hdfs_port", hdfsPort); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"job_tracker_hostname", jobTrackerHostname); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"job_tracker_port", jobTrackerPort); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"num_map_tasks", numMapTasks); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"num_reduce_tasks", numReduceTasks); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getObjectId(),"working_dir", workingDirectory); //$NON-NLS-1$

      if (userDefined != null) {
        for (int i = 0; i < userDefined.size(); i++) {
          UserDefinedItem item = userDefined.get(i);
          if (item.getName() != null && !"".equals(item.getName()) && item.getValue() != null && !"".equals(item.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
            rep.saveJobEntryAttribute(id_job, getObjectId(), i,"user_defined_name", item.getName()); //$NON-NLS-1$
            rep.saveJobEntryAttribute(id_job, getObjectId(), i,"user_defined_value", item.getValue()); //$NON-NLS-1$
          }
        }
      }
      
    } else {
      throw new KettleException("Unable to save to a repository. The repository is null."); //$NON-NLS-1$
    }
  }
  
  public boolean evaluates()
  {
    return true;
  }

  public boolean isUnconditional()
  {
    return true;
  }

  public int getSimpleLoggingInterval() {
    return simpleLoggingInterval;
  }

  public void setSimpleLoggingInterval(int simpleLoggingInterval) {
    this.simpleLoggingInterval = simpleLoggingInterval;
  }

  public boolean isSimpleBlocking() {
    return simpleBlocking;
  }

  public void setSimpleBlocking(boolean simpleBlocking) {
    this.simpleBlocking = simpleBlocking;
  }
}
