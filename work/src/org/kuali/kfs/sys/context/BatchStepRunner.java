/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.context;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchSpringContext;
import org.kuali.kfs.sys.batch.Job;
import org.kuali.kfs.sys.batch.Step;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.KualiModuleService;
import org.kuali.rice.krad.service.ModuleService;

public class BatchStepRunner {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(BatchStepRunner.class);

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("ERROR: You must pass the name of the step to run on the command line.");
            System.exit(8);
        }
        try {
            Log4jConfigurer.configureLogging(false);
            SpringContextForBatchRunner.initializeKfs();
            String[] stepNames;
            if (args[0].indexOf(",") > 0) {
                stepNames = StringUtils.split(args[0], ",");
            }
            else {
                stepNames = new String[] { args[0] };
            }
            ParameterService parameterService = SpringContext.getBean(ParameterService.class);
            DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
            
            String jobName = args.length >= 2 ? args[1] : KFSConstants.BATCH_STEP_RUNNER_JOB_NAME;
            Date jobRunDate = dateTimeService.getCurrentDate();
            LOG.info("Executing job: " + jobName + " steps: " + Arrays.toString(stepNames));
            for (int i = 0; i < stepNames.length; ++i) {
                Step step = BatchSpringContext.getStep(stepNames[i]);
                if ( step != null ) {
                    Step unProxiedStep = (Step) ProxyUtils.getTargetIfProxied(step);
                    Class<?> stepClass = unProxiedStep.getClass();

                    ModuleService module = SpringContext.getBean(KualiModuleService.class).getResponsibleModuleService( stepClass );
                    Appender ndcAppender = null;
                    String nestedDiagnosticContext =
                            StringUtils.substringAfter( module.getModuleConfiguration().getNamespaceCode(), "-").toLowerCase()
                            + File.separator + step.getName()
                            + "-" + dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate());
                    boolean ndcSet = false;
                    try {
                        ndcAppender = new FileAppender(Logger.getRootLogger().getAppender("LogFile").getLayout(), getLogFileName(nestedDiagnosticContext));
                        ndcAppender.addFilter(new NDCFilter(nestedDiagnosticContext));
                        Logger.getRootLogger().addAppender(ndcAppender);
                        NDC.push(nestedDiagnosticContext);
                        ndcSet = true;
                    } catch (Exception ex) {
                        LOG.warn("Could not initialize custom logging for step: " + step.getName(), ex);
                    }
                    try {
                        if (!Job.runStep(parameterService, jobName, i, step, jobRunDate) ) {
                            System.exit(4);
                        }
                    } catch ( RuntimeException ex ) {
                        throw ex;
                    } finally {
                        if ( ndcSet ) {
                            if (ndcAppender != null) {
                                ndcAppender.close();
                            }
                            Logger.getRootLogger().removeAppender(ndcAppender);
                            NDC.pop();
                        }
                    }
                } else {
                    throw new IllegalArgumentException( "Unable to find step: " + stepNames[i] );
                }
            }
            LOG.info("Finished executing job: " + jobName + " steps: " + Arrays.toString(stepNames));
            System.exit(0);
        }
        catch (Throwable t) {
            System.err.println("ERROR: Exception caught: ");
            t.printStackTrace(System.err);
            System.exit(8);
        }
    }

    protected static String getLogFileName(String nestedDiagnosticContext) {
        return SpringContext.getBean( ConfigurationService.class ).getPropertyValueAsString(KFSConstants.REPORTS_DIRECTORY_KEY)
                + File.separator
                + nestedDiagnosticContext + ".log";
    }

}
