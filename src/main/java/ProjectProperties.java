import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

public class ProjectProperties {
    public static final String DEFAULT_CHANGELOG_FILE_NAME = "CHANGELOG";
    public static final String DEFAULT_CHANGELOG_LINE_FORMAT = "${message}";

    public static final String CHANGELOG_FILE_NAME_KEY = "auto-changelog.fileName";
    public static final String CHANGELOG_LINE_FORMAT_KEY = "auto-changelog.lineFormat";
    public static final String CHANGELOG_UPDATE = "auto-changelog.update";

    @NotNull
    public static String getProjectProperty(Project project, String key) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        String property = properties.getValue(key);

        property = property != null ? property.trim() : "";

        if (property.isEmpty()) {
            switch (key) {
                case CHANGELOG_FILE_NAME_KEY:
                    property = DEFAULT_CHANGELOG_FILE_NAME;
                    break;
                case CHANGELOG_LINE_FORMAT_KEY:
                    property = DEFAULT_CHANGELOG_LINE_FORMAT;
                    break;
            }
        }

        return property;
    }

    public static boolean getProjectPropertyBoolean(Project project, String key) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        return properties.getBoolean(key);
    }

    public static void setProjectProperty(Project project, String key, String value) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue(key, value == null ? null : value.trim());
    }

    public static void setProjectProperty(Project project, String key, boolean value) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);
        properties.setValue(key, value);
    }

/*
    @NotNull
    public static HashMap<String, String> getProjectProperties(Project project) {
        HashMap<String, String> result = new HashMap<>();

        result.put(CHANGELOG_FILE_NAME_KEY, ProjectProperties.getProjectProperty(project, CHANGELOG_FILE_NAME_KEY));
        result.put(CHANGELOG_LINE_FORMAT_KEY, ProjectProperties.getProjectProperty(project, CHANGELOG_LINE_FORMAT_KEY));

        return result;
    }

    public static void setProjectProperties(Project project, HashMap<String, String> props) {
        PropertiesComponent properties = PropertiesComponent.getInstance(project);

        for (HashMap.Entry<String, String> set : props.entrySet()) {
            properties.setValue(set.getKey(), set.getValue());
        }
    }
*/

    public static String getChangelogFilename(Project project) {
        return ProjectProperties.getProjectProperty(project, CHANGELOG_FILE_NAME_KEY);
    }

    public static void setChangelogFilename(Project project, String value) {
        ProjectProperties.setProjectProperty(project, CHANGELOG_FILE_NAME_KEY, value);
    }

    public static String getChangelogLineFormat(Project project) {
        return ProjectProperties.getProjectProperty(project, CHANGELOG_LINE_FORMAT_KEY);
    }

    public static void setChangelogLineFormat(Project project, String value) {
        ProjectProperties.setProjectProperty(project, CHANGELOG_LINE_FORMAT_KEY, value);
    }

    public static boolean getChangelogUpdate(Project project) {
        return ProjectProperties.getProjectPropertyBoolean(project, CHANGELOG_UPDATE);
    }

    public static void setChangelogUpdate(Project project, boolean value) {
        ProjectProperties.setProjectProperty(project, CHANGELOG_UPDATE, value);
    }
}
