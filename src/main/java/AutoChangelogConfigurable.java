import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import jnr.a64asm.REG;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class AutoChangelogConfigurable implements Configurable {
    private JBTextField changelogFileName;
    private JBTextField regExp;

    private final Project project;

    public AutoChangelogConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Auto Changelog";
    }

    @Override
    public @Nullable JComponent createComponent() {
        changelogFileName = new JBTextField(ProjectProperties.getProjectProperty(project, ProjectProperties.CHANGELOG_FILE_NAME_KEY));
        regExp = new JBTextField(ProjectProperties.getProjectProperty(project, ProjectProperties.CHANGELOG_LINE_FORMAT_KEY));

        JPanel container = new JPanel(new GridLayoutManager(3, 2));

        addLabel(container, new JLabel("Changelog file name"), 0);
        addField(container, changelogFileName, 0);

        regExp.setEnabled(false);
        addLabel(container, new JLabel("Changelog line format"), 1);
        addField(container, regExp, 1);

        JPanel flex = new JPanel();
        GridConstraints flexConstraints = new GridConstraints();
        flexConstraints.setRow(2);
        flexConstraints.setFill(GridConstraints.FILL_BOTH);
        container.add(flex, flexConstraints);

        return container;
    }

    @Override
    public boolean isModified() {
        boolean modified = !changelogFileName.getText().trim().equals(ProjectProperties.getChangelogFilename(project));
        modified |= !regExp.getText().trim().equals(ProjectProperties.getChangelogLineFormat(project));
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        ProjectProperties.setChangelogFilename(project, changelogFileName.getText());
        ProjectProperties.setChangelogLineFormat(project, regExp.getText());
    }

    private void addLabel(JPanel container, JComponent label, int row) {
        GridConstraints constraints = new GridConstraints();
        constraints.setRow(row);
        constraints.setColumn(0);
        constraints.setFill(GridConstraints.FILL_HORIZONTAL);
        constraints.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(label, constraints);
    }

    private void addField(JPanel container, JComponent field, int row) {
        GridConstraints constraints = new GridConstraints();
        constraints.setHSizePolicy(GridConstraints.SIZEPOLICY_WANT_GROW);
        constraints.setFill(GridConstraints.FILL_HORIZONTAL);
        constraints.setAnchor(GridConstraints.ANCHOR_WEST);
        constraints.setRow(row);
        constraints.setColumn(1);
        constraints.setVSizePolicy(GridConstraints.SIZEPOLICY_CAN_SHRINK);
        container.add(field, constraints);
    }
}
