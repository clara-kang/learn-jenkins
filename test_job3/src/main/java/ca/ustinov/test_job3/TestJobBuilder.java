package ca.ustinov.test_job3;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestJobBuilder extends Builder implements SimpleBuildStep {

    @DataBoundConstructor
    public TestJobBuilder() {

    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        try {
            Jenkins jenkins = Jenkins.getActiveInstance();
            listener.getLogger().println("Jenkins Root Dir: " + jenkins.getRootDir());

            String projectName = "FreeStyleProject_" + build.getNumber();

            //================================================================

            /*FreeStyleProject project = jenkins.createProject(
                    FreeStyleProject.class,
                    projectName
            );

            Descriptor<Builder> shell = jenkins.getBuilder("Shell");
            project.getBuilders().add(shell.newInstance(
                    Stapler.getCurrentRequest(),
                    (new JSONObject()).accumulate("command", "date")
            ));*/

            //=================================================================


            String configXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project>\n" +
                    "  <builders>\n" +
                    "    <hudson.tasks.Shell>\n" +
                    "      <command>date</command>\n" +
                    "    </hudson.tasks.Shell>\n" +
                    "  </builders>\n" +
                    "</project>";

            FreeStyleProject project = (FreeStyleProject) jenkins.createProjectFromXML(
                    projectName,
                    new ByteArrayInputStream(configXml.getBytes(StandardCharsets.UTF_8))
            );

            project.save();
            project.createExecutable().run();

        } catch (Exception e) {
            build.setResult(Result.FAILURE);
            e.printStackTrace(listener.getLogger());
            listener.fatalError(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Test Job Builder 003";
        }

    }
}

