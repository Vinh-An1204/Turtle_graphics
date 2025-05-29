package turtle.ui;

import antlrturtle.LogoLexer;
import antlrturtle.LogoParser;
import turtle.LogoDriver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class TurtleFXUIController {

    @FXML
    private TextArea codeEditor;

    @FXML
    private Slider animationSpeed;

    @FXML
    private Group logoCanvas;

    private TurtleFXCanvasPainter painter;

    private Executor backgroundThread = Executors.newSingleThreadExecutor();

    public void initialize() {
        backgroundThread.execute(() -> {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/Hello.logo")))) {
                final String demoProg = reader.lines().collect(Collectors.joining("\n"));
                JavaFXThreadHelper.runOrDefer(() -> this.codeEditor.setText(demoProg));
            } catch (final IOException e) {
                JavaFXThreadHelper.runOrDefer(() -> this.codeEditor.setText("ERROR"));
            }
        });

        this.painter = new TurtleFXCanvasPainter(this.logoCanvas);

        this.painter.setAnimationDurationMs(this.animationSpeed.valueProperty().longValue());
        this.animationSpeed.valueProperty().addListener((observable, oldValue, newValue) -> this.painter.setAnimationDurationMs(newValue.longValue()));
    }

    @FXML
    public void runProgram(ActionEvent event) {
        this.painter.cls();
        this.backgroundThread.execute(() -> {
            final LogoLexer lexer = new LogoLexer(CharStreams.fromString(this.codeEditor.getText()));
            final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            final LogoParser parser = new LogoParser(tokenStream);

            final ParseTree tree = parser.prog();

            final ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
            final LogoDriver logoDriver = new LogoDriver(this.painter);
            parseTreeWalker.walk(logoDriver, tree);
        });
    }

    @FXML
    public void onAbout() {
        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Logo Turtle");
        alert.setContentText("Turtle graphics project for PPL course\n");

        alert.showAndWait();
    }

    @FXML
    public void onOpen() {
        final FileChooser logoFileChooser = new FileChooser();
        logoFileChooser.setTitle("Select Logo File");
        logoFileChooser.getExtensionFilters().add(new ExtensionFilter("Logo Files", "*.logo"));
        final File logoFile = logoFileChooser.showOpenDialog(null);
        if (Objects.nonNull(logoFile)) {
            try (final BufferedReader fileReader = new BufferedReader(new FileReader(logoFile))) {
                final String logoProgram = fileReader.lines().collect(Collectors.joining("\n"));
                JavaFXThreadHelper.runOrDefer(() -> this.codeEditor.setText(logoProgram));
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
