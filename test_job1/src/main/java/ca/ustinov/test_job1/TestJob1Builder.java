package ca.ustinov.test_job1;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TestJob1Builder extends Builder implements SimpleBuildStep {

    private final String cmdString;

    //Getters for using in web interface (config.jelly)
    public String getCmdString() {
        return cmdString;
    }

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public TestJob1Builder(String cmdString) {
        this.cmdString = cmdString;
    }


    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        try {

            Process proc = Runtime.getRuntime().exec(cmdString);
            proc.waitFor();
            int exitValue = proc.exitValue();

            if (exitValue != 0 || getDescriptor().logOutput) {
                String line;

                //process output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        listener.getLogger().println(line);
                    }
                }

                //process error messages
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        listener.error(line);
                    }
                }
            }

            if (exitValue != 0) {
                throw new Exception("Process exit value = " + exitValue);
            }
        } catch (Exception e) {
            build.setResult(Result.FAILURE);
            listener.fatalError(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public String getDisplayName() {
            return "Test Job 1";
        }

        private boolean logOutput;

        public DescriptorImpl() {
            //loading the persisted global configuration
            load();
        }

        // on-the-fly validation
        public FormValidation doCheckCmdString(@QueryParameter String value) throws IOException, ServletException {
            if (value.contains("sudo")) {
                return FormValidation.error("You cannot use sudo here");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with only Free Style Project's type
            return FreeStyleProject.class.isAssignableFrom(aClass);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            logOutput = formData.getBoolean("logOutput");
            save();
            return super.configure(req,formData);
        }

        // global.jelly calls this method to determine the initial state of the checkbox by the naming convention.
        public boolean getLogOutput() {
            return logOutput;
        }

        //for testing
        public void setLogOutput(boolean value) {
             logOutput  = value;
        }
    }
}

