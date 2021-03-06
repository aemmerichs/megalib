package org.softlang.java.preferences;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class MegaLPreference extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public MegaLPreference() {
        super(GRID);
    }

    public void createFieldEditors() {
        addField(new FileFieldEditor("Graphvizz", "Graphvizz bin/dot path:", getFieldEditorParent()));
        addField(new FileFieldEditor("properties", "properties.json path:", getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        // second parameter is typically the plug-in id
        setPreferenceStore(new ScopedPreferenceStore(ConfigurationScope.INSTANCE, "org.softlang.java"));
        setDescription("Settings for the MegaL GraphView");
    }

}