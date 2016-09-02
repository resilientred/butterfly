package com.paypal.butterfly.basic.conditions.pom;

import com.paypal.butterfly.extensions.api.TransformationContext;
import com.paypal.butterfly.extensions.api.utilities.TransformationOperationCondition;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transformation operation condition to check if
 * a particular Maven dependency exists or not
 *
 * @author facarvalho
 */
public class PomDependencyExists extends TransformationOperationCondition<PomDependencyExists> {

    private static final Logger logger = LoggerFactory.getLogger(PomDependencyExists.class);

    private static final String DESCRIPTION = "Check if dependency '%s:%s:%s' exists in POM file %s";

    private String groupId;
    private String artifactId;
    private String version = null;

    public PomDependencyExists() {
    }

    /**
     * Transformation operation condition to check if
     * a particular Maven dependency exists or not
     *
     * @param groupId managed dependency group id
     * @param artifactId managed dependency artifact id
     */
    public PomDependencyExists(String groupId, String artifactId) {
        setGroupId(groupId);
        setArtifactId(artifactId);
    }

    /**
     * Transformation operation condition to check if
     * a particular Maven dependency exists or not
     *
     * @param groupId managed dependency group id
     * @param artifactId managed dependency artifact id
     * @param version managed dependency version
     */
    public PomDependencyExists(String groupId, String artifactId, String version) {
        this(groupId, artifactId);
        setVersion(version);
    }

    public PomDependencyExists setGroupId(String groupId) {
        checkForBlankString("GroupId", groupId);
        this.groupId = groupId;
        return this;
    }

    public PomDependencyExists setArtifactId(String artifactId) {
        checkForBlankString("ArtifactId", artifactId);
        this.artifactId = artifactId;
        return this;
    }

    public PomDependencyExists setVersion(String version) {
        checkForEmptyString("Version", version);
        this.version = version;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return String.format(DESCRIPTION, groupId, artifactId, (version == null ? "" : version), getRelativePath());
    }

    @Override
    protected Boolean execution(File transformedAppFolder, TransformationContext transformationContext) throws Exception {
        File pomFile = getAbsoluteFile(transformedAppFolder, transformationContext);
        MavenXpp3Reader reader = new MavenXpp3Reader();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(pomFile);
            Model model = reader.read(fileInputStream);
            List<Dependency> dependencyList = model.getDependencies();
            dependencyList = dependencyList.stream().filter(item -> item.getGroupId().equals(groupId) && item.getArtifactId().equals(artifactId))
                    .collect(Collectors.toList());
            if(!dependencyList.isEmpty()) {
                Dependency dependency = dependencyList.get(0);
                if (version == null || version.equals(dependency.getVersion())) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Error happened during transformation operation condition evaluation");
            return false;
        }finally {
            if(fileInputStream != null)
                fileInputStream.close();
        }
        return false;
    }

}
