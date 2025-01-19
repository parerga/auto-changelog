import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PreCommitHookCheckinHandler extends CheckinHandler {
    private final Project project;
    private final CheckinProjectPanel checkinProjectPanel;

    public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        final JCheckBox updateChangelog = new JCheckBox("Update changelog");

        return new RefreshableOnComponent() {
            @Override
            public JComponent getComponent() {
                final JPanel panel = new JPanel(new GridLayout(1, 0));
                panel.add(updateChangelog);
                return panel;
            }

            @Override
            public void refresh() {
            }

            @Override
            public void saveState() {
                ProjectProperties.setChangelogUpdate(project, updateChangelog.isSelected());
            }

            @Override
            public void restoreState() {
                updateChangelog.setSelected(ProjectProperties.getChangelogUpdate(project));
            }
        };
    }

    public PreCommitHookCheckinHandler(final CheckinProjectPanel checkinProjectPanel, final CommitContext commitContext) {
        this.project = checkinProjectPanel.getProject();
        this.checkinProjectPanel = checkinProjectPanel;
    }

    public ReturnResult beforeCheckin(CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
        if (!ProjectProperties.getChangelogUpdate(project))
            return ReturnResult.COMMIT;

        String changelogFileName = ProjectProperties.getChangelogFilename(project);
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        Collection<VirtualFile> roots = checkinProjectPanel.getRoots();

        List<VirtualFile> affectedRoots = new ArrayList<>();

        for (VirtualFile root : roots) {
            boolean affected = isRootAffected(root);
            if (!affected) {
                continue;
            }

            VirtualFile changelogFile = root.findFileByRelativePath(changelogFileName);
            if (changelogFile == null || !changelogFile.exists()) {
                VcsNotifier.getInstance(project).notifyError(null, "", "Changelog doesn't exist, path: " + root.getPath());
                continue;
            }

            if (changeListManager.getStatus(changelogFile) == FileStatus.NOT_CHANGED) {
                affectedRoots.add(root);
            }
        }

        if (!affectedRoots.isEmpty()) {
            ReturnResult updateChangelog = showPromptDialog("Update the changelog with a commit message?", "Pre-Commit");
            if (updateChangelog == ReturnResult.CANCEL)
                return ReturnResult.COMMIT;
        } else {
            return ReturnResult.COMMIT;
        }

        String commitMessage = checkinProjectPanel.getCommitMessage();

        for (VirtualFile root : affectedRoots) {
            VirtualFile changelogFile = root.findFileByRelativePath(changelogFileName);
            if (changelogFile == null) {
                continue;
            }

            try {
                updateChangelog(changelogFile.getPath(), commitMessage);
                changelogFile.refresh(true, false);
            } catch (IOException e) {
                VcsNotifier.getInstance(project).notifyError(null, "", "Failed changelog update. Error: " + e.getMessage());
            }

            LocalChangeList defaultChangeList = changeListManager.getDefaultChangeList();
            changeListManager.moveChangesTo(defaultChangeList, changeListManager.getChange(changelogFile));
        }


        checkinProjectPanel.refresh();
        return ReturnResult.CANCEL;
    }

    private boolean isRootAffected(VirtualFile root) {
        Collection<VirtualFile> selectedFiles = checkinProjectPanel.getVirtualFiles();
        ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);

        for (VirtualFile file : selectedFiles) {
            VirtualFile fileRoot = vcsManager.getVcsRootFor(file);
            if (Objects.equals(fileRoot, root)) {
                return true;
            }
        }

        return false;
    }

    private void updateChangelog(String filename, String text) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        lines.add(0, text);
        Files.write(Paths.get(filename), lines);
    }

    private ReturnResult showPromptDialog(String text, String title) {
        return Messages.showDialog(project, text, title, new String[]{ "Yes", "No" }, 1, Messages.getWarningIcon()) == 0 ? ReturnResult.COMMIT: ReturnResult.CANCEL;
    }
}
