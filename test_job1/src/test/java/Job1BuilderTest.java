import ca.ustinov.test_job1.TestJob1Builder;
import hudson.tasks.Builder;
import org.jvnet.hudson.test.JenkinsRule;
import org.apache.commons.io.FileUtils;
import hudson.model.*;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.*;


public class Job1BuilderTest {

    @Rule public final JenkinsRule jenkinsRule = new JenkinsRule();

    @Test public void failure() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.getBuildersList().add(new TestJob1Builder("NONSENCE"));

        FreeStyleBuild build = project.scheduleBuild2(0).get();

        String s = FileUtils.readFileToString(build.getLogFile());
        assertTrue(s.contains("Finished: FAILURE"));
    }

    @Test public void successWithLogging() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();

        Builder builder = new TestJob1Builder("uname -a");
        ((TestJob1Builder.DescriptorImpl)builder.getDescriptor()).setLogOutput(true);

        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        String s = FileUtils.readFileToString(build.getLogFile());
        System.out.println(s);
        assertTrue(s.contains("Finished: SUCCESS"));
        assertTrue(s.contains("GNU/Linux"));
    }

    @Test public void successNoLogging() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();

        Builder builder = new TestJob1Builder("uname -a");
        ((TestJob1Builder.DescriptorImpl)builder.getDescriptor()).setLogOutput(false);

        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        String s = FileUtils.readFileToString(build.getLogFile());
        System.out.println(s);
        assertTrue(s.contains("Finished: SUCCESS"));
        assertFalse(s.contains("GNU/Linux"));
    }

}
