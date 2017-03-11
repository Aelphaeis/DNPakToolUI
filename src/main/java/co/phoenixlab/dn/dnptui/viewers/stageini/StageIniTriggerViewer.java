package co.phoenixlab.dn.dnptui.viewers.stageini;

import co.phoenixlab.dn.dnptui.viewers.TextViewer;
import co.phoenixlab.dn.subfile.stage.trigger.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.StandardOpenOption.*;

public class StageIniTriggerViewer extends TextViewer {

    private Button exportBtn;
    private AtomicReference<StageTriggers> triggers;
    private Gson gson;

    public StageIniTriggerViewer() {
        this(UTF_8);
    }

    public StageIniTriggerViewer(Charset charset) {
        super(charset);
        gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        triggers = new AtomicReference<>();
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        StageTriggersReader reader = new StageTriggersReader();
        StageTriggers triggers = reader.read(byteBuffer);
        StringBuilder builder = new StringBuilder();
        builder.append(triggers.getNumTriggers()).append(" triggers\n");
        Trigger[] triggers1 = triggers.getTriggers();
        for (int i = 0, triggersLen = triggers1.length; i < triggersLen; i++) {
            Trigger trigger = triggers1[i];
            builder.append(String.format("=== TRIGGER %,d ===\n", i))
                .append('\t').append(trigger.getTriggerParentName()).append('/')
                .append(trigger.getTriggerName()).append('\n')
                    .append("\tunknown3: ").append(trigger.getUnknown3()).append("\n\t")
                    .append(trigger.getNumConditionCalls()).append(" condition script calls\n");
            appendTriggerScriptCalls(builder, trigger.getConditionCalls());
            builder.append("\t").append(trigger.getNumActionCalls()).append(" action script calls\n");
            appendTriggerScriptCalls(builder, trigger.getActionCalls());
            builder.append("\t").append(trigger.getNumEventCalls()).append(" event script calls\n");
            appendTriggerScriptCalls(builder, trigger.getEventCalls());
            if (!trigger.getComment().isEmpty()) {
                builder.append("\tComment: ").append(trigger.getComment()).append('\n');
            }
        }
        this.triggers.set(triggers);
        String content = builder.toString();
        Platform.runLater(() -> textArea.setText(content));
    }

    private void appendTriggerScriptCalls(StringBuilder builder, TriggerScriptCall[] calls) {
        for (int i1 = 0, conditionCallsLength = calls.length; i1 < conditionCallsLength; i1++) {
            TriggerScriptCall scriptCall = calls[i1];
            builder.append("\t\t").append(String.format("%,-4d", i1))
                    .append(" ").append(scriptCall.getScriptName().replace(".lua", "")).append('(');
            TriggerCallParameter[] params = scriptCall.getParams();
            StringJoiner paramJoiner = new StringJoiner(", ");
            for (int i2 = 0, paramsLength = params.length; i2 < paramsLength; i2++) {
                TriggerCallParameter param = params[i2];
                paramJoiner.add(param.toString("%2$s %3$s"));
            }
            builder.append(paramJoiner.toString()).append(")\n");
        }
    }

    @Override
    public void init() {
        super.init();
        exportBtn = new Button("Export JSON");
        exportBtn.setOnAction(this::exportJson);
        toolbar.getChildren().add(exportBtn);
    }

    private void exportJson(ActionEvent event) {
        StageTriggers triggers = this.triggers.get();
        if (triggers == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JSON file", "*.json");
        chooser.getExtensionFilters().add(filter);
        chooser.setInitialFileName("trigger.json");
        chooser.setSelectedExtensionFilter(filter);
        chooser.setTitle("Export to JSON");
        File f = chooser.showSaveDialog(getDisplayNode().getScene().getWindow());
        if (f == null) {
            return;
        }
        Path outPath = f.toPath();
        try (BufferedWriter writer = Files.newBufferedWriter(outPath, UTF_8, CREATE, TRUNCATE_EXISTING)) {
            gson.toJson(triggers, writer);
            writer.flush();
        } catch (IOException e) {
        }
    }
}
